/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.client;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.theeyetribe.client.GazeApiManager.IGazeApiConnectionListener;
import com.theeyetribe.client.GazeApiManager.IGazeApiResponseListener;
import com.theeyetribe.client.data.CalibrationResult;
import com.theeyetribe.client.data.CalibrationResult.CalibrationPoint;
import com.theeyetribe.client.data.GazeData;
import com.theeyetribe.client.reply.CalibrationPointEndReply;
import com.theeyetribe.client.reply.ReplyBase;
import com.theeyetribe.client.reply.ReplyFailed;
import com.theeyetribe.client.reply.TrackerGetReply;

/**
 * This singleton is the main entry point of the TET Java Client. It manages all routines associated to gaze control.
 * <p>
 * Using this class a developer can 'calibrate' an eye tracking setup and attach listeners to receive live data streams
 * of {@link com.theeyetribe.client.data.GazeData} updates.
 */
public class GazeManager implements IGazeApiResponseListener, IGazeApiConnectionListener
{
    private final static int INIT_TIME_DELAY_SECONDS = 10;

    protected List<IGazeListener> gazeListeners;
    protected List<ICalibrationResultListener> calibrationResultListeners;
    protected List<ITrackerStateListener> trackerStateListeners;
    protected List<IConnectionStateListener> connectionStateListeners;

    protected ICalibrationProcessHandler calibrationListener;
    protected int totalCalibrationPoints;
    protected int sampledCalibrationPoints;

    protected SingleFrameBlockingQueue<GazeData> gazeQueue;

    protected GazeBroadcaster gazeBroadcaster;

    protected Heartbeat heartbeatHandler;

    protected GazeApiManager apiManager;

    private ThreadPoolExecutor threadPool;

    private SimpleDateFormat sdf;

    protected boolean isActive;

    private static final Object initializationLock = new Object();
    private static boolean isInitializing;
    private static boolean isInitialized;

    protected TrackerState trackerState;
    protected CalibrationResult lastCalibrationResult;
    protected FrameRate frameRate;
    protected ClientMode clientMode;
    protected ApiVersion version;
    protected Boolean isCalibrated;
    protected Boolean isCalibrating;
    protected Integer heartbeatMillis = 3000; // default value
    protected Integer screenIndex;
    protected Integer screenResolutionWidth;
    protected Integer screenResolutionHeight;
    protected Float screenPhysicalWidth;
    protected Float screenPhysicalHeight;

    protected Gson gson;

    private GazeManager()
    {
        gazeListeners = Collections.synchronizedList(new ArrayList<IGazeListener>());
        calibrationResultListeners = Collections.synchronizedList(new ArrayList<ICalibrationResultListener>());
        trackerStateListeners = Collections.synchronizedList(new ArrayList<ITrackerStateListener>());
        connectionStateListeners = Collections.synchronizedList(new ArrayList<IConnectionStateListener>());

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        gazeQueue = new SingleFrameBlockingQueue<GazeData>();
        gazeBroadcaster = new GazeBroadcaster();
        heartbeatHandler = new Heartbeat();
    }

    public static GazeManager getInstance()
    {
        return Holder.INSTANCE;
    }

    private static class Holder
    {
        // thread-safe initialization on demand
        static final GazeManager INSTANCE = new GazeManager();
    }

    /**
     * Activates TET Java Client and all underlying routines using default values. Should be called _only_ once when an
     * application starts up. Calling thread will be locked during initialization.
     * <p>
     * When closing down, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @return portnumber if successfully activated, false otherwise
     */
    public boolean activate(ApiVersion version, ClientMode mode)
    {
        return activate(version, mode, GazeApiManager.DEFAULT_SERVER_HOST, GazeApiManager.DEFAULT_SERVER_PORT);
    }

    /**
     * Activates TET Java Client and all underlying routines using default values. Should be called _only_ once when an
     * application starts up. Calling thread will be locked during initialization.
     * <p>
     * When closing down, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @param listener Listener to notify once the connection to EyeTribe Server has been established
     * @return portnumber if successfully activated, false otherwise
     */
    public boolean activate(ApiVersion version, ClientMode mode, IConnectionStateListener listener)
    {
        addConnectionStateListener(listener);
        return activate(version, mode, GazeApiManager.DEFAULT_SERVER_HOST, GazeApiManager.DEFAULT_SERVER_PORT);
    }

    /**
     * Activates TET Java Client and all underlying routines. Should be called _only_ once when an application starts
     * up. Calling thread will be locked during initialization.
     * <p>
     * When closing down, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param listener Listener to notify once the connection to EyeTribe Server has been established
     * @return portnumber if successfully activated, false otherwise
     */
    public boolean activate(final ApiVersion version, final ClientMode mode, final String hostname,
            final int portnumber, IConnectionStateListener listener)
    {
        addConnectionStateListener(listener);
        return activate(version, mode, hostname, portnumber);
    }

    /**
     * Activates TET Java Client and all underlying routines. Should be called _only_ once when an application starts
     * up. Calling thread will be locked during initialization.
     * <p>
     * When closing down, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @param hostname The host name or IP address where the eye tracking server is running
     * @return portnumber if successfully activated, false otherwise
     */
    public boolean activate(final ApiVersion version, final ClientMode mode, final String hostname, final int portnumber)
    {
        synchronized (getInstance())
        {
            // lock calling thread while initializing
            Thread threadLock = Thread.currentThread();

            synchronized (threadLock)
            {
                synchronized (initializationLock)
                {
                    if (!isActivated())
                    {
                        isInitializing = true;

                        try
                        {
                            threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime()
                                    .availableProcessors());

                            // make sure we do not init networking on calling
                            // thread
                            threadPool.execute(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        if (null == apiManager)
                                            apiManager = new GazeApiManager(GazeManager.this, GazeManager.this);
                                        else
                                            apiManager.close();

                                        apiManager.connect(hostname, portnumber);

                                        if (apiManager.isConnected())
                                        {
                                            apiManager.requestTracker(mode, version);
                                            apiManager.requestAllStates();
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        System.out.println("Exception while connecting to the EyeTribe Server: "
                                                + e.getLocalizedMessage());
                                    }
                                }
                            });

                            // We wait until above requests have been handled by
                            // server or timeout occurs
                            initializationLock.wait(INIT_TIME_DELAY_SECONDS * 1000);

                            if (!isInitialized)
                            {
                                handleInitFailure();

                                System.out.println("Error initializing GazeManager, is EyeTribe Server running?");
                            }
                            else
                            {
                                if (!heartbeatHandler.isAlive())
                                    heartbeatHandler.start();

                                isActive = true;

                                // notify connection listeners
                                onGazeApiConnectionStateChanged(isActivated());
                            }
                        }
                        catch (Exception e)
                        {
                            handleInitFailure();

                            System.out.println("Error initializing GazeManager: " + e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }

            return isActivated();
        }
    }

    private void handleInitFailure()
    {
        synchronized (getInstance())
        {
            if (heartbeatHandler.isAlive())
                heartbeatHandler.stop();

            if (null != apiManager)
                apiManager.close();

            if (null != gson)
                gson = null;

            if (isInitializing)
            {
                synchronized (initializationLock)
                {
                    isInitializing = false;
                    isInitialized = false;
                    initializationLock.notify();
                }
            }

            if (null != threadPool && !threadPool.isShutdown())
            {
                try
                {
                    threadPool.shutdownNow();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Deactivates TET Java Client and all under lying routines. Should be called when a application closes down.
     */
    public void deactivate()
    {
        synchronized (getInstance())
        {
            if (heartbeatHandler.isAlive())
                heartbeatHandler.stop();

            if (null != apiManager)
                apiManager.close();

            if (null != gson)
                gson = null;

            clearListeners();

            if (isInitializing)
            {
                synchronized (initializationLock)
                {
                    isInitializing = false;
                    isInitialized = false;
                    initializationLock.notify();
                }
            }

            if (null != threadPool && !threadPool.isShutdown())
            {
                try
                {
                    threadPool.shutdownNow();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            isActive = false;
            isInitializing = false;
            isInitialized = false;
        }
    }

    /**
     * @deprecated Use isActivated() instead.
     * @return Is the client library connected to EyeTribe Server?
     */
    public boolean isConnected()
    {
        return null != apiManager ? apiManager.isConnected() : false;
    }

    /**
     * @return Is the client library connected to EyeTribe Server and initialized?
     */
    public boolean isActivated()
    {
        // Only possible to call when not initializing
        return (null != apiManager ? apiManager.isConnected() : false) && isActive;
    }

    /**
     * Is the client in the middle of a calibration process?
     */
    public boolean isCalibrating()
    {
        return null != isCalibrating ? isCalibrating : false;
    }

    /**
     * Is the client already calibrated?
     */
    public boolean isCalibrated()
    {
        return null != isCalibrated ? isCalibrated : false;
    }

    /**
     * Index of currently used screen. Used for multi-screen setups.
     */
    public int getScreenIndex()
    {
        return screenIndex;
    }

    /**
     * Physical width of screen in meters.
     */
    public float getScreenPhysicalWidth()
    {
        return screenPhysicalWidth;
    }

    /**
     * Physical height of screen in meters.
     */
    public float getScreenPhysicalHeight()
    {
        return screenPhysicalHeight;
    }

    /**
     * Width of screen resolution in pixels.
     */
    public int getScreenResolutionWidth()
    {
        return screenResolutionWidth;
    }

    /**
     * Height of screen resolution in pixels.
     */
    public int getScreenResolutionHeight()
    {
        return screenResolutionHeight;
    }

    /**
     * The current state of the connected TrackerDevice
     */
    public TrackerState getTrackerState()
    {
        return trackerState;
    }

    /**
     * Length of a heartbeat in milliseconds
     * <p>
     * . The EyeTribe Server defines the desired length of a heartbeat and is in this implementation automatically
     * acquired through the Tracker API.
     */
    public int getHeartbeatMillis()
    {
        return heartbeatMillis;
    }

    /**
     * The latest performed and valid CalibrationResult. Note the result is not necessarily positive and clients should
     * evaluate the result before using.
     */
    public CalibrationResult getLastCalibrationResult()
    {
        return lastCalibrationResult;
    }

    /**
     * Number of frames per second delivered by EyeTribe Server
     */
    public FrameRate getFrameRate()
    {
        return frameRate;
    }

    /**
     * Current API version compliance of EyeTribe Server
     */
    public ApiVersion getVersion()
    {
        return version;
    }

    /**
     * Current running mode of this client
     */
    public ClientMode getClientMode()
    {
        return clientMode;
    }

    /**
     * Initiate a new calibration process. Must be called before any call to {@link #calibrationPointStart(int, int)
     * CalibrationPointStart} or {@link #calibrationPointEnd() calibrationPointEnd}.
     * <p>
     * Any previous (and possible running) calibration process must be completed or aborted before calling this.
     * <p>
     * A full calibration process consists of a number of calls to {@link #calibrationPointStart(int, int)
     * calibrationPointStart} and {@link #calibrationPointEnd() calibrationPointEnd} matching the total number of
     * calibration points set by the numCalibrationPoints parameter.
     * 
     * @param numCalibrationPoints The number of calibration points that will be used in this calibration
     * @param listener The {@link com.theeyetribe.client.ICalibrationProcessHandler} instance that will receive
     *            callbacks during the calibration process
     */
    public void calibrationStart(int numCalibrationPoints, ICalibrationProcessHandler listener)
    {
        if (isActivated())
        {
            sampledCalibrationPoints = 0;
            totalCalibrationPoints = numCalibrationPoints;
            calibrationListener = listener;
            apiManager.requestCalibrationStart(numCalibrationPoints);
        }
        else
            System.out.println("TET Java Client not activated!");
    }

    /**
     * Called for every calibration point during a calibration process. This call should be followed by a call to
     * {@link #calibrationPointEnd() calibrationPointEnd} 1-2 seconds later.
     * <p>
     * The calibration process must be initiated by a call to {@link #calibrationStart(int, ICalibrationProcessHandler)
     * calibrationStart} before calling this.
     * 
     * @param x X coordinate of the calibration point
     * @param y Y coordinate of the calibration point
     */
    public void calibrationPointStart(int x, int y)
    {
        if (isActivated())
        {
            if (isCalibrating)
            {
                apiManager.requestCalibrationPointStart(x, y);
            }
            else
                System.out.println("TET Java Client calibration not started!");
        }
        else
            System.out.println("TET Java Client not activated!");
    }

    /**
     * Called for every calibration point during a calibration process. This should be called 1-2 seconds after
     * {@link #calibrationPointStart(int, int) calibrationPointStart}.
     * <p>
     * The calibration process must be initiated by a call to {@link #calibrationStart(int, ICalibrationProcessHandler)
     * calibrationStart} before calling this.
     */
    public void calibrationPointEnd()
    {
        if (isActivated())
        {
            if (isCalibrating)
            {
                apiManager.requestCalibrationPointEnd();
            }
            else
                System.out.println("TET Java Client calibration not started!");
        }
        else
            System.out.println("TET Java Client not activated!");
    }

    /**
     * Cancels an ongoing calibration process.
     */
    public void calibrationAbort()
    {
        if (isActivated())
        {
            if (isCalibrating)
            {
                apiManager.requestCalibrationAbort();
            }
            else
                System.out.println("TET Java Client calibration not started!");
        }
        else
            System.out.println("TET Java Client not activated!");
    }

    /**
     * Resets calibration state, canceling any previous calibrations.
     */
    public void calibrationClear()
    {
        if (isActivated())
        {
            apiManager.requestCalibrationClear();
        }
        else
            System.out.println("TET Java Client not activated!");
    }

    /**
     * Switch currently active screen. Enabled the user to take control of which screen is used for calibration and gaze
     * control.
     * 
     * @param screenIndex Index of next screen. On windows 'Primary Screen' has index 0
     * @param screenResW Screen resolution width in pixels
     * @param screenResH Screen resolution height in pixels
     * @param screenPsyW Physical Screen width in meters
     * @param screenPsyH Physical Screen height in meters
     */
    public void switchScreen(int screenIndex, int screenResW, int screenResH, float screenPsyW, float screenPsyH)
    {
        if (isActivated())
        {
            apiManager.requestScreenSwitch(screenIndex, screenResW, screenResH, screenPsyW, screenPsyH);
        }
        else
            System.out.println("TET Java Client not activated!");
    }

    /**
     * Adds a {@link com.theeyetribe.client.IGazeListener} to the TET Java client. This listener will receive
     * {@link com.theeyetribe.client.data.GazeData} updates when available
     * 
     * @param listener The {@link com.theeyetribe.client.IGazeListener} instance to add
     */
    public void addGazeListener(IGazeListener listener)
    {
        if (null != listener)
        {
            synchronized (gazeListeners)
            {
                if (!gazeListeners.contains(listener))
                {
                    // if first listener
                    if (gazeListeners.size() == 0)
                    {
                        if (!gazeBroadcaster.isBroadcasting())
                            gazeBroadcaster.start();
                    }

                    gazeListeners.add(listener);
                }
            }
        }
    }

    /**
     * Remove a {@link com.theeyetribe.client.IGazeListener} from the TET Java client.
     * 
     * @param listener The {@link com.theeyetribe.client.IGazeListener} instance to remove
     * @return True if successfully removed, false otherwise
     */
    public boolean removeGazeListener(IGazeListener listener)
    {
        boolean result = false;

        if (null != listener)
        {
            synchronized (gazeListeners)
            {
                if (gazeListeners.contains(listener))
                    result = gazeListeners.remove(listener);

                // if no listeners
                if (gazeListeners.size() == 0)
                {
                    if (null != gazeBroadcaster && gazeBroadcaster.isBroadcasting())
                        gazeBroadcaster.stop();
                }
            }
        }

        return result;
    }

    /**
     * Gets current number of attached {@link com.theeyetribe.client.IGazeListener} instances.
     * 
     * @return Current number of listeners
     */
    public int getNumGazeListeners()
    {
        return gazeListeners.size();
    }

    /**
     * Checks if a given instance of {@link com.theeyetribe.client.IGazeListener} is currently attached.
     * 
     * @param listener The {@link com.theeyetribe.client.IGazeListener} instance check for
     * @return True if already attached, false otherwise
     */
    public boolean hasGazeListener(IGazeListener listener)
    {
        boolean result = false;

        if (null != listener)
        {
            result = gazeListeners.contains(listener);
        }

        return result;
    }

    /**
     * Adds a {@link com.theeyetribe.client.ICalibrationResultListener} to the TET Java client. This listener will
     * recieve {@link com.theeyetribe.client.data.CalibrationResult} updates when available
     * 
     * @param listener The {@link com.theeyetribe.client.data.CalibrationResult} instance to add
     */
    public void addCalibrationResultListener(ICalibrationResultListener listener)
    {
        if (null != listener)
        {
            if (!calibrationResultListeners.contains(listener))
                calibrationResultListeners.add(listener);
        }
    }

    /**
     * Remove a {@link com.theeyetribe.client.ICalibrationResultListener} from the TET Java client.
     * 
     * @param listener The {@link com.theeyetribe.client.ICalibrationResultListener} instance to remove
     * @return True if successfully removed, false otherwise
     */
    public boolean removeCalibrationResultListener(ICalibrationResultListener listener)
    {
        boolean result = false;

        if (null != listener)
        {
            if (calibrationResultListeners.contains(listener))
                result = calibrationResultListeners.remove(listener);
        }

        return result;
    }

    /**
     * Gets current number of attached {@link com.theeyetribe.client.ICalibrationResultListener} instances.
     * 
     * @return Curent number of listeners
     */
    public int getNumCalibrationResultListeners()
    {
        return calibrationResultListeners.size();
    }

    /**
     * Checks if a given instance of {@link com.theeyetribe.client.ICalibrationResultListener} is currently attached.
     * 
     * @param listener The {@link com.theeyetribe.client.ICalibrationResultListener} instance check for
     * @return True if already attached, false otherwise
     */
    public boolean hasCalibrationResultListener(ICalibrationResultListener listener)
    {
        if (null != listener)
            return calibrationResultListeners.contains(listener);

        return false;
    }

    /**
     * Adds a {@link com.theeyetribe.client.ITrackerStateListener} to the TET Java client. This listener will recieve
     * {@link com.theeyetribe.client.GazeManager.TrackerState} updates and updates about change of active screen index.
     * 
     * @param listener The {@link com.theeyetribe.client.ITrackerStateListener} instance to add
     */
    public void addTrackerStateListener(ITrackerStateListener listener)
    {
        if (null != listener)
        {
            if (!trackerStateListeners.contains(listener))
                trackerStateListeners.add(listener);
        }
    }

    /**
     * Remove a {@link com.theeyetribe.client.ITrackerStateListener} from the TET Java client.
     * 
     * @param listener The {@link com.theeyetribe.client.ITrackerStateListener} instance to remove
     * @return True if successfully removed, false otherwise
     */
    public boolean removeTrackerStateListener(ITrackerStateListener listener)
    {
        boolean result = false;

        if (null != listener)
        {
            if (trackerStateListeners.contains(listener))
                result = trackerStateListeners.remove(listener);
        }

        return result;
    }

    /**
     * Gets current number of attached {@link com.theeyetribe.client.ITrackerStateListener} instances.
     * 
     * @return Curent number of listeners
     */
    public int getNumTrackerStateListeners()
    {
        return trackerStateListeners.size();
    }

    /**
     * Checks if a given instance of {@link com.theeyetribe.client.ITrackerStateListener} is currently attached.
     * 
     * @param listener The {@link com.theeyetribe.client.ITrackerStateListener} instance check for
     * @return True if already attached, false otherwise
     */
    public boolean hasTrackerStateListener(ITrackerStateListener listener)
    {
        if (null != listener)
            return true;

        return false;
    }

    /**
     * Adds a {@link com.theeyetribe.client.IConnectionStateListener} to the TET Java client. This listener will receive
     * updates about change in connection state to the EyeTribe Server.
     * 
     * @param listener The {@link com.theeyetribe.client.IConnectionStateListener} instance to add
     */
    public void addConnectionStateListener(IConnectionStateListener listener)
    {
        if (null != listener)
        {
            if (!connectionStateListeners.contains(listener))
                connectionStateListeners.add(listener);
        }
    }

    /**
     * Remove a {@link com.theeyetribe.client.IConnectionStateListener} from the TET Java client.
     * 
     * @param listener The {@link com.theeyetribe.client.IConnectionStateListener} instance to remove
     * @return True if successfully removed, false otherwise
     */
    public boolean removeConnectionStateListener(IConnectionStateListener listener)
    {
        boolean result = false;

        if (null != listener)
        {
            if (connectionStateListeners.contains(listener))
                result = connectionStateListeners.remove(listener);
        }

        return result;
    }

    /**
     * Gets current number of attached {@link com.theeyetribe.client.IConnectionStateListener} instances.
     * 
     * @return Current number of listeners
     */
    public int getNumConnectionStateListeners()
    {
        return connectionStateListeners.size();
    }

    /**
     * Checks if a given instance of {@link com.theeyetribe.client.IConnectionStateListener} is currently attached.
     * 
     * @param listener The {@link com.theeyetribe.client.IConnectionStateListener} instance check for
     * @return True if already attached, false otherwise
     */
    public boolean hasConnectionStateListener(IConnectionStateListener listener)
    {
        if (null != listener)
            return connectionStateListeners.contains(listener);

        return false;
    }

    /**
     * Clear all attached listeners, clears GazeData queue and stop broadcasting
     */
    public void clearListeners()
    {
        if (null != gazeListeners)
        {
            gazeListeners.clear();
        }

        if (null != calibrationResultListeners)
        {
            calibrationResultListeners.clear();
        }

        if (null != trackerStateListeners)
        {
            trackerStateListeners.clear();
        }

        if (null != connectionStateListeners)
        {
            connectionStateListeners.clear();
        }

        if (null != gazeQueue)
        {
            synchronized (gazeQueue)
            {
                gazeQueue.clear();
            }
        }

        if (null != gazeBroadcaster && gazeBroadcaster.isBroadcasting())
            gazeBroadcaster.stop();
    }

    @Override
    public void onGazeApiResponse(final String response)
    {
        threadPool.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(null == gson)
                        gson = new Gson();
                    
                    JsonParser jsonParser = new JsonParser();
                    JsonObject jo = (JsonObject) jsonParser.parse(response);
                    jsonParser = null;

                    ReplyBase reply = gson.fromJson(jo, ReplyBase.class);

                    if (reply.statuscode == HttpURLConnection.HTTP_OK)
                    {
                        if (reply.category.compareTo(Protocol.CATEGORY_TRACKER) == 0)
                        {
                            if (reply.request.compareTo(Protocol.TRACKER_REQUEST_GET) == 0)
                            {
                                TrackerGetReply tgr = gson.fromJson(jo, TrackerGetReply.class);

                                if (null != tgr.values.version)
                                    version = ApiVersion.fromInt(tgr.values.version);

                                if (null != tgr.values.push)
                                {
                                    if (tgr.values.push)
                                        clientMode = ClientMode.PUSH;
                                    else
                                        clientMode = ClientMode.PULL;
                                }

                                if (null != tgr.values.heartbeatInterval)
                                    heartbeatMillis = tgr.values.heartbeatInterval;

                                if (null != tgr.values.frameRate)
                                    frameRate = FrameRate.fromInt(tgr.values.frameRate);

                                if (null != tgr.values.trackerState)
                                {
                                    // if tracker state changed, notify listeners
                                    if (null == trackerState
                                            || tgr.values.trackerState != TrackerState.toInt(trackerState))
                                    {
                                        trackerState = TrackerState.fromInt(tgr.values.trackerState);

                                        synchronized (trackerStateListeners)
                                        {
                                            for (final ITrackerStateListener listener : trackerStateListeners)
                                            {
                                                try
                                                {
                                                    threadPool.execute(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            try
                                                            {
                                                                listener.onTrackerStateChanged(TrackerState
                                                                        .toInt(trackerState));
                                                            }
                                                            catch (Exception e)
                                                            {
                                                                System.out.println("Exception while calling ITrackerStateListener.OnTrackerConnectionChanged() on listener "
                                                                                + listener
                                                                                + ": "
                                                                                + e.getLocalizedMessage());
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                                }
                                                catch (Exception e)
                                                {
                                                    System.out.println("Exception while executing ThreadPool task: "
                                                            + e.getLocalizedMessage());
                                                }
                                            }
                                        }
                                    }
                                }

                                if (null != tgr.values.isCalibrating)
                                    isCalibrating = tgr.values.isCalibrating;
                                if (null != tgr.values.isCalibrated)
                                    isCalibrated = tgr.values.isCalibrated;

                                // if defined in json response, then set
                                if (((JsonObject) jo.get(Protocol.KEY_VALUES)).has(Protocol.TRACKER_CALIBRATIONRESULT))
                                {
                                    // is calibration result different from current?
                                    if (null == lastCalibrationResult
                                            || !lastCalibrationResult.equals(tgr.values.calibrationResult))
                                    {
                                        lastCalibrationResult = tgr.values.calibrationResult;

                                        synchronized (calibrationResultListeners)
                                        {
                                            for (final ICalibrationResultListener listener : calibrationResultListeners)
                                            {
                                                try
                                                {
                                                    threadPool.execute(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            try
                                                            {
                                                                listener.onCalibrationChanged(isCalibrated,
                                                                        lastCalibrationResult);
                                                            }
                                                            catch (Exception e)
                                                            {
                                                                System.out.println("Exception while calling ICalibrationResultListener.OnCalibrationChanged() on listener "
                                                                                + listener
                                                                                + ": "
                                                                                + e.getLocalizedMessage());
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                                }
                                                catch (Exception e)
                                                {
                                                    System.out.println("Exception while executing ThreadPool task: "
                                                            + e.getLocalizedMessage());
                                                }
                                            }
                                        }
                                    }
                                }

                                if (null != tgr.values.screenResolutionWidth)
                                    screenResolutionWidth = tgr.values.screenResolutionWidth;
                                if (null != tgr.values.screenResolutionHeight)
                                    screenResolutionHeight = tgr.values.screenResolutionHeight;
                                if (null != tgr.values.screenPhysicalWidth)
                                    screenPhysicalWidth = tgr.values.screenPhysicalWidth;
                                if (null != tgr.values.screenPhysicalHeight)
                                    screenPhysicalHeight = tgr.values.screenPhysicalHeight;
                                if (null != tgr.values.screenIndex)
                                {
                                    // if screen index changed, broadcast to all
                                    // listeners
                                    if (tgr.values.screenIndex != screenIndex)
                                    {
                                        screenIndex = tgr.values.screenIndex;

                                        synchronized (trackerStateListeners)
                                        {
                                            for (final ITrackerStateListener listener : trackerStateListeners)
                                            {
                                                try
                                                {
                                                    threadPool.execute(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            try
                                                            {
                                                                listener.onScreenStatesChanged(screenIndex,
                                                                        screenResolutionWidth, screenResolutionHeight,
                                                                        screenPhysicalWidth, screenPhysicalHeight);
                                                            }
                                                            catch (Exception e)
                                                            {
                                                                System.out.println("Exception while calling ITrackerStateListener.OnScreenIndexChanged() on listener "
                                                                                + listener
                                                                                + ": "
                                                                                + e.getLocalizedMessage());
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                                }
                                                catch (Exception e)
                                                {
                                                    System.out.println("Exception while executing ThreadPool task: "
                                                            + e.getLocalizedMessage());
                                                }
                                            }
                                        }
                                    }
                                }

                                // Add to high frequency broadcasting queue
                                if (((JsonObject) jo.get(Protocol.KEY_VALUES)).has(Protocol.TRACKER_FRAME))
                                {
                                    // fixing timestamp based on string
                                    // representation, Json 32bit int issue
                                    // TODO: This will eventually be done serverside
                                    if (null != tgr.values.frame.timeStampString
                                            && !tgr.values.frame.timeStampString.isEmpty())
                                    {
                                        Date date;
                                        try
                                        {
                                            date = sdf.parse(tgr.values.frame.timeStampString);
                                            tgr.values.frame.timeStamp = date.getTime(); // UTC
                                        }
                                        catch (Exception e)
                                        {
                                            // consume error
                                        }
                                    }

                                    // make room in queue, if full
                                    while (!gazeQueue.offer(tgr.values.frame))
                                        gazeQueue.poll();
                                }

                                // Special routine used for initialization
                                if (isInitializing)
                                {
                                    // we make sure response is inital get request and not a 'push mode' frame
                                    if (!((JsonObject) jo.get(Protocol.KEY_VALUES)).has(Protocol.TRACKER_FRAME))
                                    {
                                        synchronized (initializationLock)
                                        {
                                            isInitializing = false;
                                            isInitialized = true;
                                            initializationLock.notify();
                                        }
                                    }
                                }
                            }
                            else if (reply.request.compareTo(Protocol.TRACKER_REQUEST_SET) == 0)
                            {
                                // do nothing
                            }
                        }
                        else if (reply.category.compareTo(Protocol.CATEGORY_CALIBRATION) == 0)
                        {
                            if (reply.request.compareTo(Protocol.CALIBRATION_REQUEST_START) == 0)
                            {
                                isCalibrating = true;

                                if (null != calibrationListener)
                                    try
                                    {
                                        calibrationListener.onCalibrationStarted();
                                    }
                                    catch (Exception e)
                                    {
                                        System.out.println("Exception while calling ICalibrationProcessHandler.onCalibrationStarted() "
                                                        + "on listener "
                                                        + calibrationListener
                                                        + ": "
                                                        + e.getLocalizedMessage());
                                        e.printStackTrace();
                                    }
                            }
                            else if (reply.request.compareTo(Protocol.CALIBRATION_REQUEST_POINTSTART) == 0)
                            {

                            }
                            else if (reply.request.compareTo(Protocol.CALIBRATION_REQUEST_POINTEND) == 0)
                            {
                                ++sampledCalibrationPoints;

                                if (null != calibrationListener)
                                {
                                    // Notify calibration listener that a new
                                    // calibration point has been sampled
                                    try
                                    {
                                        calibrationListener.onCalibrationProgress((float) sampledCalibrationPoints
                                                / totalCalibrationPoints);
                                    }
                                    catch (Exception e)
                                    {
                                        System.out.println("Exception while calling ICalibrationProcessHandler.OnCalibrationProgress() on listener "
                                                        + calibrationListener + ": " + e.getLocalizedMessage());
                                        e.printStackTrace();
                                    }

                                    if (sampledCalibrationPoints == totalCalibrationPoints)
                                        // Notify calibration listener that all
                                        // calibration points have been sampled and
                                        // the analysis of the calibration results
                                        // has begun
                                        try
                                        {
                                            calibrationListener.onCalibrationProcessing();
                                        }
                                        catch (Exception e)
                                        {
                                            System.out.println("Exception while calling ICalibrationProcessHandler.OnCalibrationProcessing() on listener "
                                                            + calibrationListener + ": " + e.getLocalizedMessage());
                                            e.printStackTrace();
                                        }
                                }

                                final CalibrationPointEndReply cper = gson.fromJson(jo, CalibrationPointEndReply.class);

                                if (cper == null || cper.values.calibrationResult == null)
                                    return; // not done with calibration yet

                                isCalibrated = cper.values.calibrationResult.result;
                                isCalibrating = !cper.values.calibrationResult.result;

                                // Evaluate resample points
                                for (CalibrationPoint calibPoint : cper.values.calibrationResult.calibpoints)
                                {
                                    if (calibPoint.state == CalibrationPoint.STATE_RESAMPLE
                                            || calibPoint.state == CalibrationPoint.STATE_NO_DATA)
                                    {
                                        --sampledCalibrationPoints;
                                    }
                                }

                                // is calibration result different from current?
                                if (null == lastCalibrationResult
                                        || !lastCalibrationResult.equals(cper.values.calibrationResult))
                                {
                                    lastCalibrationResult = cper.values.calibrationResult;

                                    synchronized (calibrationResultListeners)
                                    {
                                        for (final ICalibrationResultListener listener : calibrationResultListeners)
                                        {
                                            try
                                            {
                                                threadPool.execute(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        try
                                                        {
                                                            listener.onCalibrationChanged(isCalibrated,
                                                                    lastCalibrationResult);
                                                        }
                                                        catch (Exception e)
                                                        {
                                                            System.out.println("Exception while calling ICalibrationResultListener.OnCalibrationChanged() on listener "
                                                                            + listener + ": " + e.getLocalizedMessage());
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                            }
                                            catch (Exception e)
                                            {
                                                System.out.println("Exception while executing ThreadPool task: "
                                                        + e.getLocalizedMessage());
                                            }
                                        }
                                    }
                                }

                                if (null != calibrationListener)
                                {
                                    // Notify calibration listener that calibration
                                    // results are ready for evaluation
                                    try
                                    {
                                        calibrationListener.onCalibrationResult(cper.values.calibrationResult);
                                    }
                                    catch (Exception e)
                                    {
                                        System.out.println("Exception while calling ICalibrationProcessHandler.OnCalibrationResult() on listener "
                                                        + calibrationListener + ": " + e.getLocalizedMessage());
                                        e.printStackTrace();
                                    }
                                }
                            }
                            else if (reply.request.compareTo(Protocol.CALIBRATION_REQUEST_ABORT) == 0)
                            {
                                isCalibrating = false;

                                // restore states of last calibration if any
                                if (isActivated())
                                    apiManager.requestCalibrationStates();
                            }
                            else if (reply.request.compareTo(Protocol.CALIBRATION_REQUEST_CLEAR) == 0)
                            {
                                isCalibrated = false;
                                isCalibrating = false;
                                lastCalibrationResult = null;
                            }

                        }
                        else if (reply.category.compareTo(Protocol.CATEGORY_HEARTBEAT) == 0)
                        {
                            // do nothing
                        }
                        else
                        {
                            ReplyFailed rf = gson.fromJson(jo, ReplyFailed.class);

                            System.out.println("Request FAILED");
                            System.out.println("Category: " + rf.category);
                            System.out.println("Request: " + rf.request);
                            System.out.println("StatusCode: " + rf.statuscode);
                            System.out.println("StatusMessage: " + rf.values.statusMessage);
                        }
                    }
                    else
                    {
                        ReplyFailed rf = gson.fromJson(jo, ReplyFailed.class);

                        /*
                         * JSON Message status code is different from HttpURLConnection.HTTP_OK. Check if special TET
                         * specific status code before handling error
                         */

                        switch (rf.statuscode)
                        {
                        case Protocol.STATUSCODE_CALIBRATION_UPDATE:
                            // The calibration state has changed, clients should
                            // update themselves
                            if (isActivated())
                                apiManager.requestCalibrationStates();
                            break;

                        case Protocol.STATUSCODE_SCREEN_UPDATE:
                            // The primary screen index has changed, clients should
                            // update themselves
                            if (isActivated())
                                apiManager.requestScreenStates();
                            break;

                        case Protocol.STATUSCODE_TRACKER_UPDATE:
                            // The connected Tracker Device has changed state,
                            // clients should update themselves
                            if (isActivated())
                                apiManager.requestTrackerState();
                            break;

                        default:
                            System.out.println("Request FAILED");
                            System.out.println("Category: " + rf.category);
                            System.out.println("Request: " + rf.request);
                            System.out.println("StatusCode: " + rf.statuscode);
                            System.out.println("StatusMessage: " + rf.values.statusMessage);
                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    System.out.println("Exception while executing ThreadPool task: " + e.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public void onGazeApiConnectionStateChanged(final boolean isConnected)
    {
        if (!isInitializing)
        {
            // Notify listeners of change in connection state
            synchronized (connectionStateListeners)
            {
                for (final IConnectionStateListener listener : connectionStateListeners)
                {
                    try
                    {
                        threadPool.execute(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    listener.onConnectionStateChanged(isConnected);
                                }
                                catch (Exception e)
                                {
                                    System.out.println("Exception while calling IConnectionStateListener.onConnectionStateChanged() on listener "
                                                    + listener + ": " + e.getLocalizedMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        System.out.println("Exception while executing ThreadPool task: " + e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    /**
     * Current running mode of this client
     */
    public enum ClientMode
    {
        PUSH(1001), PULL(1002);

        private int clientMode;

        private ClientMode(int clientMode)
        {
            this.clientMode = clientMode;
        }
    }

    /**
     * Current running mode of this client
     */
    public enum ApiVersion
    {
        VERSION_1_0(1);

        private int version;

        private ApiVersion(int version)
        {
            this.version = version;
        }

        private static ApiVersion[] values = null;

        public static ApiVersion fromInt(int i)
        {
            if (ApiVersion.values == null)
                ApiVersion.values = ApiVersion.values();
            for (ApiVersion v : values)
                if (v.version == i)
                    return v;
            return null;
        }

        public static int toInt(ApiVersion v)
        {
            return v.version;
        }
    }

    /**
     * The current state of the connected TrackerDevice.
     */
    public enum TrackerState
    {
        TRACKER_CONNECTED(0), TRACKER_NOT_CONNECTED(1), TRACKER_CONNECTED_BADFW(2), TRACKER_CONNECTED_NOUSB3(3), TRACKER_CONNECTED_NOSTREAM(
                4);

        private int trackerState;

        private TrackerState(int trackerState)
        {
            this.trackerState = trackerState;
        }

        private static TrackerState[] values = null;

        public static TrackerState fromInt(int i)
        {
            if (TrackerState.values == null)
                TrackerState.values = TrackerState.values();
            for (TrackerState state : values)
                if (state.trackerState == i)
                    return state;
            return null;
        }

        public static int toInt(TrackerState ts)
        {
            if (null != ts)
                return ts.trackerState;
            else
                return TRACKER_NOT_CONNECTED.trackerState;
        }
    }

    /**
     * The current state of the connected TrackerDevice.
     */
    public enum FrameRate
    {
        FPS_30(30), FPS_60(60);

        private int frameRate;

        private FrameRate(int frameRate)
        {
            this.frameRate = frameRate;
        }

        private static FrameRate[] values = null;

        public static FrameRate fromInt(int i)
        {
            if (FrameRate.values == null)
            {
                FrameRate.values = FrameRate.values();
            }
            for (FrameRate frate : values)
                if (frate.frameRate == i)
                    return frate;
            return null;
        }

        public static int toInt(FrameRate fr)
        {
            if (null != fr)
                return fr.frameRate;
            else
                return 0;
        }
    }

    /**
     * Threaded broadcaster responsible for distributing GazeData update to all attached listeners.
     */
    private class GazeBroadcaster
    {
        private BroadcastTask task;
        private Thread runner;

        public GazeBroadcaster()
        {
        }

        private synchronized void start()
        {
            if (isBroadcasting())
                stop();

            runner = new Thread(task = new BroadcastTask());
            runner.start();
        }

        private synchronized void stop()
        {
            if (null != task && task.isBroadcasting)
            {
                task.isBroadcasting = false;
                runner.interrupt();
            }
        }

        private synchronized boolean isBroadcasting()
        {
            return null != task && task.isBroadcasting;
        }

        private class BroadcastTask implements Runnable
        {
            private boolean isBroadcasting = true;

            @Override
            public void run()
            {
                while (isBroadcasting)
                {
                    try
                    {
                        // take latest from deque
                        final GazeData gd = gazeQueue.takeLast();

                        synchronized (gazeListeners)
                        {
                            for (final IGazeListener listener : gazeListeners)
                            {
                                try
                                {
                                    /*
                                     * We intentionally avoid using threadPool which otherwise blocks heartbeats if
                                     * breakpoints set client-side in this callback
                                     */
                                    listener.onGazeUpdate(gd);
                                }
                                catch (Exception e)
                                {
                                    System.out.println("Exception while calling IGazeListener.onGazeUpdate() "
                                            + "on listener " + listener + ": " + e.getLocalizedMessage());
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    catch (InterruptedException ie)
                    {
                        // consume
                    }
                    catch (Exception e)
                    {
                        System.out.println("Internal error while broadcasting GazeData");
                    }
                }
            }
        }
    }

    /**
     * Class responsible for sending 'heartbeats' to the underlying TET Java Client Tracker notifying that the client is
     * alive. The EyeTribe Server defines the desired length of a heartbeat and is in this implementation automatically
     * acquired through the Tracker API.
     */
    private class Heartbeat
    {
        private HeartTask task;

        public Heartbeat()
        {
        }

        private synchronized void start()
        {
            if (isAlive())
                stop();

            Thread t = new Thread(task = new HeartTask());
            t.start();
        }

        private synchronized void stop()
        {
            if (null != task && task.isAlive)
                task.isAlive = false;
        }

        private synchronized boolean isAlive()
        {
            return null != task && task.isAlive;
        }

        private class HeartTask implements Runnable
        {
            private boolean isAlive = true;

            @Override
            public void run()
            {
                while (isAlive)
                {
                    try
                    {
                        if (null != apiManager && apiManager.isConnected())
                            apiManager.requestHeartbeat();

                        Thread.sleep(heartbeatMillis);
                    }
                    catch (Exception e)
                    {
                        System.out.println("Internal error while sending heartbeats");
                    }
                }
            }
        }
    }

    private class SingleFrameBlockingQueue<T> extends LinkedBlockingDeque<T>
    {
        public SingleFrameBlockingQueue()
        {
            super(1);
        }
    }
}

/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk;

import com.theeyetribe.clientsdk.data.CalibrationResult;
import com.theeyetribe.clientsdk.data.CalibrationResult.CalibrationPoint;
import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.request.Request;
import com.theeyetribe.clientsdk.response.CalibrationPointEndResponse;
import com.theeyetribe.clientsdk.response.Response;
import com.theeyetribe.clientsdk.response.ResponseFailed;
import com.theeyetribe.clientsdk.response.TrackerGetResponse;
import com.theeyetribe.clientsdk.GazeApiManager.IGazeApiResponseListener;
import com.theeyetribe.clientsdk.GazeApiManager.IGazeApiConnectionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * GazeManagerCore is the core implementation of EyeTribe Java SDK. This class manages all underlying routines
 * associated with communicating with a running EyeTribe Server.
 */
abstract class GazeManagerCore implements IGazeApiResponseListener, IGazeApiConnectionListener
{
    public final static boolean IS_DEBUG_MODE = false;
    
    protected final static long DEFAULT_TIMEOUT_MILLIS = 10 * 1000;

    protected List<IGazeListener> mGazeListeners;
    protected List<ICalibrationResultListener> mCalibrationResultListeners;
    protected List<ITrackerStateListener> mTrackerStateListeners;
    protected List<IScreenStateListener> mScreenStateListeners;
    protected List<IConnectionStateListener> mConnectionStateListeners;

    protected ICalibrationProcessHandler mCalibrationListener;

    protected int totalCalibrationPoints;
    protected int sampledCalibrationPoints;

    protected GazeApiManager apiManager;

    private ThreadPoolExecutor threadPool;

    protected boolean isActive;

    private final Object initializationLock = new Object();
    private boolean isInitializing;
    private boolean isInitialized;

    protected TrackerState trackerState;
    protected CalibrationResult lastCalibrationResult;
    protected FrameRate frameRate;
    protected ApiVersion version;
    protected ClientMode clientMode;
    protected Boolean isCalibrated = false;
    protected Boolean isCalibrating = false;
    protected Integer screenIndex = 0;
    protected Integer screenResolutionWidth = 0;
    protected Integer screenResolutionHeight = 0;
    protected Float screenPhysicalWidth = 0f;
    protected Float screenPhysicalHeight = 0f;

    protected GazeData latestGazeData;

    GazeManagerCore()
    {
        mGazeListeners = Collections.synchronizedList(new ArrayList<>());
        mCalibrationResultListeners = Collections.synchronizedList(new ArrayList<>());
        mTrackerStateListeners = Collections.synchronizedList(new ArrayList<>());
        mScreenStateListeners = Collections.synchronizedList(new ArrayList<>());
        mConnectionStateListeners = Collections.synchronizedList(new ArrayList<>());
        resetEnums();
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines using default values. Latest API version will be used in
     * client mode PUSH. This call is synchronous and calling thread is locked during initialization.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     * 
     * @return true if successfully activated, false otherwise
     */
    public boolean activate()
    {
        return activate(ApiVersion.VERSION_1_0, GazeApiManager.DEFAULT_SERVER_HOST,
                GazeApiManager.DEFAULT_SERVER_PORT);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is synchronous and calling thread is locked
     * during initialization.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     *
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @return true if successfully activated, false otherwise
     * @deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default
     */
    public boolean activate(ApiVersion version, ClientMode mode)
    {
        //Connect using default 'Push' mode
        return activate(version, GazeApiManager.DEFAULT_SERVER_HOST, GazeApiManager.DEFAULT_SERVER_PORT);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is synchronous and calling thread is locked
     * during initialization.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     *
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @param listener Listener to notify once the connection to EyeTribe Server has been established
     * @return true if successfully activated, false otherwise
     * @deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default
     */
    public boolean activate(ApiVersion version, ClientMode mode, IConnectionStateListener listener)
    {
        //Connect using default 'Push' mode
        return activate(version, GazeApiManager.DEFAULT_SERVER_HOST, GazeApiManager.DEFAULT_SERVER_PORT, listener);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is synchronous and calling thread is locked
     * during initialization.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     *
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @param listener Listener to notify once the connection to EyeTribe Server has been established
     * @return true if successfully activated, false otherwise
     * @deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default
     */
    public boolean activate(ApiVersion version, ClientMode mode, String hostname, int portnumber,
                            IConnectionStateListener listener)
    {
        addConnectionStateListener(listener);
        //Connect using default 'Push' mode
        return activate(version, hostname, portnumber);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is synchronous and calling thread is locked
     * during initialization.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     *
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @return true if successfully activated, false otherwise
     * @deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default
     */
    public boolean activate(ApiVersion version, ClientMode mode, String hostname, int portnumber)
    {
        //Connect using default 'Push' mode
        return activate(version, hostname, portnumber, DEFAULT_TIMEOUT_MILLIS);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is synchronous and calling thread is locked
     * during initialization.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     *
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @param timeOut time out in milliseconds of connection attempt
     * @return true if successfully activated, false otherwise
     * @deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default
     */
    public boolean activate(final ApiVersion version, final ClientMode mode, final String hostname,
                            final int portnumber, final long timeOut)
    {
        //Connect using default 'Push' mode
        return activate(version, hostname, portnumber, timeOut);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is synchronous and calling thread is locked
     * during initialization.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @return true if successfully activated, false otherwise
     */
    public boolean activate(ApiVersion version)
    {
        return activate(version, GazeApiManager.DEFAULT_SERVER_HOST, GazeApiManager.DEFAULT_SERVER_PORT);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is synchronous and calling thread is locked
     * during initialization.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param listener Listener to notify once the connection to EyeTribe Server has been established
     * @return true if successfully activated, false otherwise
     */
    public boolean activate(ApiVersion version, IConnectionStateListener listener)
    {
        addConnectionStateListener(listener);
        return activate(version, GazeApiManager.DEFAULT_SERVER_HOST, GazeApiManager.DEFAULT_SERVER_PORT);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is synchronous and calling thread is locked
     * during initialization.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @param listener Listener to notify once the connection to EyeTribe Server has been established
     * @return true if successfully activated, false otherwise
     */
    public boolean activate(ApiVersion version, String hostname, int portnumber,
            IConnectionStateListener listener)
    {
        addConnectionStateListener(listener);
        return activate(version, hostname, portnumber);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is synchronous and calling thread is locked
     * during initialization.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @return true if successfully activated, false otherwise
     */
    public boolean activate(ApiVersion version, String hostname, int portnumber)
    {
        return activate(version, hostname, portnumber, DEFAULT_TIMEOUT_MILLIS);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is synchronous and calling thread is locked
     * during initialization.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @param timeOut time out in milliseconds of connection attempt
     * @return true if successfully activated, false otherwise
     */
    public boolean activate(final ApiVersion version, final String hostname,
            final int portnumber, final long timeOut)
    {
        try
        {
            return activateAsync(version, hostname, portnumber, timeOut).get(timeOut, TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            // consume
        }

        return false;
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines using default values. Latest API version will be used in
     * client mode PUSH. This call is asynchronous.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     *
     * @return a Future representing the pending connection attempt
     */
    public Future<Boolean> activateAsync()
    {
        return activateAsync(ApiVersion.VERSION_1_0, GazeApiManager.DEFAULT_SERVER_HOST,
                GazeApiManager.DEFAULT_SERVER_PORT);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is asynchronous.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     *
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @return a Future representing the pending connection attempt
     * @deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default
     */
    public Future<Boolean> activateAsync(ApiVersion version, ClientMode mode)
    {
        //Connect using default 'Push' mode
        return activateAsync(version, GazeApiManager.DEFAULT_SERVER_HOST, GazeApiManager.DEFAULT_SERVER_PORT);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is asynchronous.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     *
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @param listener Listener to notify once the connection to EyeTribe Server has been established
     * @return a Future representing the pending connection attempt
     * @deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default
     */
    public Future<Boolean> activateAsync(ApiVersion version, ClientMode mode, IConnectionStateListener listener)
    {
        //Connect using default 'Push' mode
        return activateAsync(version, GazeApiManager.DEFAULT_SERVER_HOST, GazeApiManager.DEFAULT_SERVER_PORT, listener);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is asynchronous.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     *
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @param listener Listener to notify once the connection to EyeTribe Server has been established *
     * @return a Future representing the pending connection attempt
     * @deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default
     */
    public Future<Boolean> activateAsync(ApiVersion version, ClientMode mode, String hostname, int portnumber,
                                         IConnectionStateListener listener)
    {
        addConnectionStateListener(listener);
        //Connect using default 'Push' mode
        return activateAsync(version, hostname, portnumber);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is asynchronous.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     *
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @return a Future representing the pending connection attempt
     * @deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default
     */
    public Future<Boolean> activateAsync(ApiVersion version, ClientMode mode, String hostname, int portnumber)
    {
        //Connect using default 'Push' mode
        return activateAsync(version, hostname, portnumber, DEFAULT_TIMEOUT_MILLIS, 1);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is asynchronous.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     *
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @param timeOut time out in milliseconds of connection attempt
     * @return a Future representing the pending connection attempt
     * @deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default
     */
    public Future<Boolean> activateAsync(ApiVersion version, ClientMode mode, String hostname, int portnumber, long timeOut)
    {
        //Connect using default 'Push' mode
        return activateAsync(version, hostname, portnumber, timeOut, 1);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is asynchronous.
     * <p>
     * During the set timeout, an amount of connection retries will be attempted.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     *
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @param timeOut time out in milliseconds of connection attempt
     * @param retries number of times to retry connection attempt in the timeout period
     * @return a Future representing the pending connection attempt
     * @deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default
     */
    public Future<Boolean> activateAsync(final ApiVersion version, final ClientMode mode, final String hostname,
                                         final int portnumber, final long timeOut, final int retries)
    {
        //Connect using default 'Push' mode
        return activateAsync(version, hostname, portnumber, timeOut, retries);
    }
    
    /**
     * Activates EyeTribe Java SDK and all underlying routines using default values. Latest API version will be used in
     * client mode PUSH. This call is asynchronous.
     * <p>
     * During the set timeout, an amount of connection retries will be attempted.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param timeOut time out in milliseconds of connection attempt
     * @param retries number of times to retry connection attempt in the timeout period
     * @return a Future representing the pending connection attempt
     */
    public Future<Boolean> activateAsync(long timeOut, int retries)
    {
        return activateAsync(ApiVersion.VERSION_1_0, GazeApiManager.DEFAULT_SERVER_HOST,
                GazeApiManager.DEFAULT_SERVER_PORT, timeOut, retries);
    }    

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is asynchronous.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @return a Future representing the pending connection attempt
     */
    public Future<Boolean> activateAsync(ApiVersion version)
    {
        return activateAsync(version, GazeApiManager.DEFAULT_SERVER_HOST, GazeApiManager.DEFAULT_SERVER_PORT);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is asynchronous.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param listener Listener to notify once the connection to EyeTribe Server has been established *
     * @return a Future representing the pending connection attempt
     */
    public Future<Boolean> activateAsync(ApiVersion version, IConnectionStateListener listener)
    {
        addConnectionStateListener(listener);
        return activateAsync(version, GazeApiManager.DEFAULT_SERVER_HOST, GazeApiManager.DEFAULT_SERVER_PORT);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is asynchronous.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @param listener Listener to notify once the connection to EyeTribe Server has been established *
     * @return a Future representing the pending connection attempt
     */
    public Future<Boolean> activateAsync(ApiVersion version, String hostname, int portnumber,
            IConnectionStateListener listener)
    {
        addConnectionStateListener(listener);
        return activateAsync(version, hostname, portnumber);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is asynchronous.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @return a Future representing the pending connection attempt
     */
    public Future<Boolean> activateAsync(ApiVersion version, String hostname, int portnumber)
    {
        return activateAsync(version, hostname, portnumber, DEFAULT_TIMEOUT_MILLIS, 1);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is asynchronous.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     *
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @param timeOut time out in milliseconds of connection attempt
     * @return a Future representing the pending connection attempt
     */
    public Future<Boolean> activateAsync(ApiVersion version, String hostname, int portnumber, long timeOut)
    {
        return activateAsync(version, hostname, portnumber, timeOut, 1);
    }

    /**
     * Activates EyeTribe Java SDK and all underlying routines. This call is asynchronous.
     * <p>
     * During the set timeout, an amount of connection retries will be attempted.
     * <p>
     * To shutdown, the {@link #deactivate() deactivate} method must be called.
     * 
     * @param version Version number of the Tracker API that this client will be compliant to
     * @param hostname The host name or IP address where the eye tracking server is running
     * @param portnumber The port number used for the eye tracking server
     * @param timeOut time out in milliseconds of connection attempt
     * @param retries number of times to retry connection attempt in the timeout period
     * @return a Future representing the pending connection attempt
     */
    public Future<Boolean> activateAsync(final ApiVersion version, final String hostname,
            final int portnumber, final long timeOut, final int retries)
    {
        return submitToThreadpool(() ->
        {
            synchronized (initializationLock)
            {
                // is GazeManager already active?
                if (!isActivated())
                {
                    // has calling thread already started an activation process?
                    if(!isInitializing)
                    {
                        int retryDelay = Math.round((float) timeOut / retries);

                        try {
                            int numRetries = 0;

                            while (numRetries++ < retries && !Thread.interrupted()) {
                                long timstampStart = System.currentTimeMillis();

                                if (initialize(version, hostname, portnumber, retryDelay)) {
                                    break; // success, break loop
                                } else {
                                    // Short delay before retrying
                                    long timePassed = System.currentTimeMillis() - timstampStart;

                                    if (timePassed < retryDelay) {
                                        Thread.sleep(retryDelay - timePassed);
                                    }
                                }

                                if (GazeManager.IS_DEBUG_MODE)
                                    System.out.println("activateAsync connection failed, retryDelay: " + retryDelay + ", retry num: " + numRetries);
                            }
                        } catch (InterruptedException e) {
                            // consume
                        } catch (Exception e) {
                            System.out.println("Exception while establishing EyeTribe Server connection: "
                                    + e.getLocalizedMessage());
                        }
                    }
                }

                return isActivated();
            }
        });
    }

    private boolean initialize(final ApiVersion version, final String hostname,
            final int portnumber, final long timeOut)
    {
        isInitializing = true;

        try
        {
            // initialize networking
            if (null == apiManager)
                apiManager = createApiManager(GazeManagerCore.this, GazeManagerCore.this);
            else
                apiManager.close();

            if (apiManager.connect(hostname, portnumber, timeOut))
            {
                apiManager.requestTracker(version);
                apiManager.requestAllStates();

                // We wait until above requests have been handled by
                // server or timeout occurs
                initializationLock.wait(timeOut);

                if (isInitialized)
                {
                    isActive = true;

                    // notify connection listeners
                    onGazeApiConnectionStateChanged(isActivated());
                }
            }

            if (!isInitialized)
            {
                handleInitFailure();

                System.out.println("Error initializing GazeManagerCore, is EyeTribe Server running?");
            }
        }
        catch (InterruptedException e)
        {
            isInitializing = false;
        }
        catch (Exception e)
        {
            handleInitFailure();

            System.out.println("Error initializing GazeManagerCore: " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return isActivated();
    }

    private void handleInitFailure()
    {
        isInitializing = false;

        if (null != apiManager)
            apiManager.close();

        shutDownThreadpool();

        isInitialized = false;
        isActive = false;
    }

    /**
     * Deactivates EyeTribe Java SDK and all under lying routines.
     */
    public void deactivate()
    {
        synchronized (initializationLock)
        {
            isInitializing = false;

            clearListeners();

            if (null != apiManager)
                apiManager.close();

            shutDownThreadpool();

            resetEnums();

            isInitialized = false;
            isActive = false;
        }
    }

    /**
     * @deprecated Use isActivated() instead.
     * @return Is the client library connected to EyeTribe Server?
     */
    public boolean isConnected()
    {
        return null != apiManager && apiManager.isConnected();
    }

    /**
     * Has the GazeManager been connected to the EyeTribe Server and initialized?
     *
     * @return true if activated, false otherwise
     */
    public boolean isActivated()
    {
        // Only possible to call when not initializing
        return (null != apiManager && apiManager.isConnected()) && isActive;
    }

    /**
     * Has a calibration process been started and is ongoing?
     *
     * @return true if calibrating, false otherwise
     */
    public boolean isCalibrating()
    {
        return null != isCalibrating ? isCalibrating : false;
    }

    /**
     * Has the system been calibrated?
     *
     * @return true if calibrated, false otherwise
     */
    public boolean isCalibrated()
    {
        return null != isCalibrated ? isCalibrated : false;
    }

    /**
     * Index of currently used screen. Used for multi-screen setups.
     *
     * @return index number of current screen
     */
    public int getScreenIndex()
    {
        return screenIndex;
    }

    /**
     * Physical width of screen in meters.
     *
     * @return width of screen in meters
     */
    public float getScreenPhysicalWidth()
    {
        return screenPhysicalWidth;
    }

    /**
     * Physical height of screen in meters.
     *
     * @return height of screen in meters
     */
    public float getScreenPhysicalHeight()
    {
        return screenPhysicalHeight;
    }

    /**
     * Width of screen resolution in pixels.
     *
     * @return width of screen in pixels
     */
    public int getScreenResolutionWidth()
    {
        return screenResolutionWidth;
    }

    /**
     * Height of screen resolution in pixels.
     *
     * @return height of screen in pixels
     */
    public int getScreenResolutionHeight()
    {
        return screenResolutionHeight;
    }

    /**
     * The current state of the connected TrackerDevice.
     * <p>
     * The state is represented as a {@link GazeManager.TrackerState TrackerState} enum
     *
     * @return current state of the connected tracker
     */
    public TrackerState getTrackerState()
    {
        return trackerState;
    }

    /**
     * Length of a heartbeat in milliseconds
     * <p>
     * The EyeTribe Server defines the desired length of a heartbeat and is in this implementation automatically
     * acquired through the Tracker API.
     *
     * @return heartbeatlength in milliseconds
     *
     * @deprecated as of EyeTribe Server v.0.9.77 using Heartbeat no longer has any effect
     */
    public int getHeartbeatMillis()
    {
        return 0;
    }

    /**
     * The latest performed and valid CalibrationResult. Note the result is not necessarily positive and clients should
     * evaluate the result before using.
     *
     * @return last valid CalibrationResult if any, null otherwise
     */
    public CalibrationResult getLastCalibrationResult()
    {
        return lastCalibrationResult;
    }

    /**
     * Number of frames per second delivered by EyeTribe Server
     * <p>
     * The state is represented as a {@link GazeManager.FrameRate FrameRate} enum
     *
     * @return frames per second
     */
    public FrameRate getFrameRate()
    {
        return frameRate;
    }

    /**
     * Current API version compliance of EyeTribe Server
     * <p>
     * The compliance level is represented as a {@link GazeManager.ApiVersion ApiVersion} enum
     *
     * @return API compliance level
     */
    public ApiVersion getVersion()
    {
        return version;
    }

    /**
     * If running in ClientMode.PULL, this call request the latest gaze frame from the server. Registered IGazeListener
     * instances will receive the frame.
     * @deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default
     */
    public void framePull()
    {
        //deprecated, no effect
    }

    /**
     * Initiate a new calibration process. Must be called before any calls to {@link #calibrationPointStart(int, int)
     * CalibrationPointStart} or {@link #calibrationPointEnd() calibrationPointEnd}.
     * <p>
     * Any previous (and possible running) calibration process must be completed or aborted before calling this.
     * Otherwise the server will return an error.
     * <p>
     * A full calibration process consists of a number of calls to {@link #calibrationPointStart(int, int)
     * calibrationPointStart} and {@link #calibrationPointEnd() calibrationPointEnd} matching the total number of
     * calibration points set by the numCalibrationPoints parameter.
     * <p>
     * This call is synchronous and calling thread is locked while request is processed.
     * 
     * @param numCalibrationPoints The number of calibration points that will be used in this calibration
     * @param listener The {@link ICalibrationProcessHandler} instance that will receive
     *            callbacks during the calibration process
     * @return true if a new calibration process was successfully started, false otherwise
     */
    public boolean calibrationStart(int numCalibrationPoints, ICalibrationProcessHandler listener)
    {
        try
        {
            return calibrationStartAsync(numCalibrationPoints, listener).get(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            // consume
        }

        return false;
    }

    /**
     * Initiate a new calibration process. Must be called before any calls to {@link #calibrationPointStart(int, int)
     * CalibrationPointStart} or {@link #calibrationPointEnd() calibrationPointEnd}.
     * <p>
     * Any previous (and possible running) calibration process must be completed or aborted before calling this.
     * Otherwise the server will return an error.
     * <p>
     * A full calibration process consists of a number of calls to {@link #calibrationPointStart(int, int)
     * calibrationPointStart} and {@link #calibrationPointEnd() calibrationPointEnd} matching the total number of
     * calibration points set by the numCalibrationPoints parameter.
     * <p>
     * This call is asynchronous.
     * 
     * @param numCalibrationPoints The number of calibration points that will be used in this calibration
     * @param listener The {@link ICalibrationProcessHandler} instance that will receive
     *            callbacks during the calibration process
     * @return a Future representing the pending calibration start attempt
     */
    public Future<Boolean> calibrationStartAsync(final int numCalibrationPoints,
            final ICalibrationProcessHandler listener)
    {
        return submitToThreadpool(() -> {
            if (isActivated())
            {
                if (!isCalibrating())
                {
                    sampledCalibrationPoints = 0;
                    totalCalibrationPoints = numCalibrationPoints;
                    mCalibrationListener = listener;
                    Object lock = apiManager.requestCalibrationStart(numCalibrationPoints);

                    synchronized (lock)
                    {
                        try
                        {
                            lock.wait(DEFAULT_TIMEOUT_MILLIS);
                        }
                        catch (InterruptedException ie)
                        {
                            //consume
                        }
                        catch (Exception e)
                        {
                            System.out.println("Exception while awaiting reply to sync request: " + e.getLocalizedMessage());
                            //e.printStackTrace();
                        }
                    }

                    return isCalibrating();
                }

                System.out.println("Calibration process already started! Abort ongoing calibration to start new.");

                return false;
            }

            System.out.println("EyeTribe Java SDK not activated!");

            return false;
        });
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
                System.out.println("EyeTribe Java SDK calibration not started!");
        }
        else
            System.out.println("EyeTribe Java SDK not activated!");
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
                System.out.println("EyeTribe Java SDK calibration not started!");
        }
        else
            System.out.println("EyeTribe Java SDK not activated!");
    }

    /**
     * Cancels an ongoing calibration process.
     * <p>
     * This call is synchronous and calling thread is locked while request is processed.
     * @return True is request successful, false otherwise
     */
    public boolean calibrationAbort()
    {
        try
        {
            return calibrationAbortAsync().get(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            // consume
        }

        return false;
    }

    /**
     * Cancels an ongoing calibration process.
     * <p>
     * This call is asynchronous.
     * 
     * @return a Future representing the pending calibration abort attempt
     */
    public Future<Boolean> calibrationAbortAsync()
    {
        return submitToThreadpool(() -> {
            if (isActivated()) {
                if (isCalibrating()) {
                    Object lock = apiManager.requestCalibrationAbort();

                    synchronized (lock)
                    {
                        try
                        {
                            lock.wait(DEFAULT_TIMEOUT_MILLIS);
                        }
                        catch (InterruptedException ie)
                        {
                            //consume
                        }
                        catch (Exception e)
                        {
                            System.out.println("Exception while awaiting reply to sync request: " + e.getLocalizedMessage());
                            //e.printStackTrace();
                        }
                    }

                    return !isCalibrating();
                }

                System.out.println("Calling CalibrationAbort(), but calibration process not running.");

                return false;
            }

            System.out.println("EyeTribe Java SDK not activated!");

            return false;
        });
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
            System.out.println("EyeTribe Java SDK not activated!");
    }

    /**
     * Switch currently active screen. Enabled the user to take control of which screen is used for calibration and gaze
     * control.
     * <p>
     * This call is synchronous and calling thread is locked while request is processed.
     * 
     * @param screenIndex Index of next screen. On windows 'Primary Screen' has index 0
     * @param screenResW Screen resolution width in pixels
     * @param screenResH Screen resolution height in pixels
     * @param screenPsyW Physical Screen width in meters
     * @param screenPsyH Physical Screen height in meters
     * @return True if request successful, false otherwise
     */
    public boolean switchScreen(int screenIndex, int screenResW, int screenResH, float screenPsyW, float screenPsyH)
    {
        try
        {
            return switchScreenAsync(screenIndex, screenResW, screenResH, screenPsyW, screenPsyH).get(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            // consume
        }

        return false;
    }

    /**
     * Switch currently active screen. Enabled the user to take control of which screen is used for calibration and gaze
     * control.
     * <p>
     * This call is asynchronous.
     * 
     * @param screenIndex Index of next screen. On windows 'Primary Screen' has index 0
     * @param screenResW Screen resolution width in pixels
     * @param screenResH Screen resolution height in pixels
     * @param screenPsyW Physical Screen width in meters
     * @param screenPsyH Physical Screen height in meters
     * @return a Future representing the pending screen switch attempt
     */
    public Future<Boolean> switchScreenAsync(final int screenIndex, final int screenResW, final int screenResH,
            final float screenPsyW, final float screenPsyH)
    {
        return submitToThreadpool(() -> {
            if (isActivated()) {
                Object lock = apiManager.requestScreenSwitch(screenIndex, screenResW, screenResH, screenPsyW, screenPsyH);

                synchronized (lock)
                {
                    try
                    {
                        lock.wait(DEFAULT_TIMEOUT_MILLIS);
                    }
                    catch (InterruptedException ie)
                    {
                        //consume
                    }
                    catch (Exception e)
                    {
                        System.out.println("Exception while awaiting reply to sync request: " + e.getLocalizedMessage());
                        //e.printStackTrace();
                    }
                }

                return GazeManagerCore.this.screenIndex == screenIndex && GazeManagerCore.this.screenResolutionWidth == screenResW
                        && GazeManagerCore.this.screenResolutionHeight == screenResH && GazeManagerCore.this.screenPhysicalWidth == screenPsyW
                        && GazeManagerCore.this.screenPhysicalHeight == screenPsyH;
            }

            System.out.println("EyeTribe Java SDK not activated!");

            return false;
        });
    }

    /**
     * Adds a {@link IGazeListener} to the EyeTribe Java SDK. This listener will receive
     * {@link GazeData} updates when available
     * 
     * @param listener The {@link IGazeListener} instance to add
     */
    public void addGazeListener(IGazeListener listener)
    {
        if (null != listener)
            if (!mGazeListeners.contains(listener))
                mGazeListeners.add(listener);
    }

    /**
     * Remove a {@link IGazeListener} from the EyeTribe Java SDK.
     * 
     * @param listener The {@link IGazeListener} instance to remove
     * @return True if successfully removed, false otherwise
     */
    public boolean removeGazeListener(IGazeListener listener) {
        return null != listener && mGazeListeners.remove(listener);

    }

    /**
     * Gets current number of attached {@link IGazeListener} instances.
     * 
     * @return Current number of listeners
     */
    public int getNumGazeListeners()
    {
        return mGazeListeners.size();
    }

    /**
     * Checks if a given instance of {@link IGazeListener} is currently attached.
     * 
     * @param listener The {@link IGazeListener} instance check for
     * @return True if already attached, false otherwise
     */
    public boolean hasGazeListener(IGazeListener listener)
    {
        boolean result = false;

        if (null != listener)
            result = mGazeListeners.contains(listener);

        return result;
    }

    /**
     * Adds a {@link ICalibrationResultListener} to the EyeTribe Java SDK. This listener will
     * receive {@link CalibrationResult} updates when available
     * 
     * @param listener The {@link CalibrationResult} instance to add
     */
    public void addCalibrationResultListener(ICalibrationResultListener listener)
    {
        if (null != listener)
            if (!mCalibrationResultListeners.contains(listener))
                mCalibrationResultListeners.add(listener);
    }

    /**
     * Remove a {@link ICalibrationResultListener} from the EyeTribe Java SDK.
     * 
     * @param listener The {@link ICalibrationResultListener} instance to remove
     * @return True if successfully removed, false otherwise
     */
    public boolean removeCalibrationResultListener(ICalibrationResultListener listener) {
        return null != listener && mCalibrationResultListeners.remove(listener);

    }

    /**
     * Gets current number of attached {@link ICalibrationResultListener} instances.
     * 
     * @return current number of listeners
     */
    public int getNumCalibrationResultListeners()
    {
        return mCalibrationResultListeners.size();
    }

    /**
     * Checks if a given instance of {@link ICalibrationResultListener} is currently attached.
     * 
     * @param listener The {@link ICalibrationResultListener} instance check for
     * @return True if already attached, false otherwise
     */
    public boolean hasCalibrationResultListener(ICalibrationResultListener listener) {
        return null != listener && mCalibrationResultListeners.contains(listener);

    }

    /**
     * Adds a {@link ITrackerStateListener} to the EyeTribe Java SDK. This listener will receive
     * {@link TrackerState} updates.
     * 
     * @param listener The {@link ITrackerStateListener} instance to add
     */
    public void addTrackerStateListener(ITrackerStateListener listener)
    {
        if (null != listener)
            if (!mTrackerStateListeners.contains(listener))
                mTrackerStateListeners.add(listener);
    }

    /**
     * Remove a {@link ITrackerStateListener} from the EyeTribe Java SDK.
     * 
     * @param listener The {@link ITrackerStateListener} instance to remove
     * @return True if successfully removed, false otherwise
     */
    public boolean removeTrackerStateListener(ITrackerStateListener listener)
    {
        if (null != listener)
            if (mTrackerStateListeners.contains(listener))
                return mTrackerStateListeners.remove(listener);

        return false;
    }

    /**
     * Gets current number of attached {@link ITrackerStateListener} instances.
     * 
     * @return current number of listeners
     */
    public int getNumTrackerStateListeners()
    {
        return mTrackerStateListeners.size();
    }

    /**
     * Checks if a given instance of {@link ITrackerStateListener} is currently attached.
     * 
     * @param listener The {@link ITrackerStateListener} instance check for
     * @return True if already attached, false otherwise
     */
    public boolean hasTrackerStateListener(ITrackerStateListener listener) {
        return null != listener && mTrackerStateListeners.contains(listener);

    }

    /**
     * Adds a {@link IScreenStateListener} to the EyeTribe Java SDK. This listener will receive
     * updates about change of active screen index.
     *
     * @param listener The {@link IScreenStateListener} instance to add
     */
    public void addScreenStateListener(IScreenStateListener listener)
    {
        if (null != listener)
            if (!mScreenStateListeners.contains(listener))
                mScreenStateListeners.add(listener);
    }

    /**
     * Remove a {@link IScreenStateListener} from the EyeTribe Java SDK.
     *
     * @param listener The {@link IScreenStateListener} instance to remove
     * @return True if successfully removed, false otherwise
     */
    public boolean removeScreenStateListener(IScreenStateListener listener)
    {
        if (null != listener)
            if (mScreenStateListeners.contains(listener))
                return mScreenStateListeners.remove(listener);

        return false;
    }

    /**
     * Gets current number of attached {@link IScreenStateListener} instances.
     *
     * @return current number of listeners
     */
    public int getNumScreenStateListeners()
    {
        return mScreenStateListeners.size();
    }

    /**
     * Checks if a given instance of {@link IScreenStateListener} is currently attached.
     *
     * @param listener The {@link IScreenStateListener} instance check for
     * @return True if already attached, false otherwise
     */
    public boolean hasScreenStateListener(IScreenStateListener listener) {
        return null != listener && mScreenStateListeners.contains(listener);

    }

    /**
     * Adds a {@link IConnectionStateListener} to the EyeTribe Java SDK. This listener will
     * receive updates about change in connection state to the EyeTribe Server.
     * 
     * @param listener The {@link IConnectionStateListener} instance to add
     */
    public void addConnectionStateListener(IConnectionStateListener listener)
    {
        if (null != listener)
            if (!mConnectionStateListeners.contains(listener))
                mConnectionStateListeners.add(listener);
    }

    /**
     * Remove a {@link IConnectionStateListener} from the EyeTribe Java SDK.
     * 
     * @param listener The {@link IConnectionStateListener} instance to remove
     * @return True if successfully removed, false otherwise
     */
    public boolean removeConnectionStateListener(IConnectionStateListener listener)
    {
        if (null != listener)
            if (mConnectionStateListeners.contains(listener))
                return mConnectionStateListeners.remove(listener);

        return false;
    }

    /**
     * Gets current number of attached {@link IConnectionStateListener} instances.
     * 
     * @return Current number of listeners
     */
    public int getNumConnectionStateListeners()
    {
        return mConnectionStateListeners.size();
    }

    /**
     * Checks if a given instance of {@link IConnectionStateListener} is currently attached.
     * 
     * @param listener The {@link IConnectionStateListener} instance check for
     * @return True if already attached, false otherwise
     */
    public boolean hasConnectionStateListener(IConnectionStateListener listener) {
        return null != listener && mConnectionStateListeners.contains(listener);

    }

    /**
     * Clear all attached listeners, clears GazeData queue and stop broadcasting
     */
    public void clearListeners()
    {
        if (null != mGazeListeners)
            mGazeListeners.clear();

        if (null != mCalibrationResultListeners)
            mCalibrationResultListeners.clear();

        if (null != mTrackerStateListeners)
            mTrackerStateListeners.clear();

        if (null != mScreenStateListeners)
            mScreenStateListeners.clear();

        if (null != mConnectionStateListeners)
            mConnectionStateListeners.clear();
    }

    protected <T> Future<T> submitToThreadpool(Callable<T> callable)
    {
        try
        {
            if(null == threadPool)
                threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            return threadPool.submit(callable);
        }
        catch (RejectedExecutionException ree)
        {
            //consume

            if(IS_DEBUG_MODE)
            {
                System.out.println("ThreadPool rejected task: " + ree.getLocalizedMessage());
                ree.printStackTrace();
            }

            return null;
        }
    }

    protected void submitToThreadpool(Runnable runnable)
    {
        try
        {
            if(null == threadPool)
                threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2 +1);

            threadPool.submit(runnable);
        }
        catch (RejectedExecutionException ree)
        {
            //consume

            if(IS_DEBUG_MODE)
            {
                System.out.println("ThreadPool rejected task: " + ree.getLocalizedMessage());
                ree.printStackTrace();
            }
        }
    }

    private void shutDownThreadpool()
    {
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
            threadPool = null;
        }
    }

    @Override
    public void onGazeApiResponse(final Response response, final Request request)
    {
        submitToThreadpool(() ->
        {
            try {
                if (response.category.compareTo(Protocol.CATEGORY_TRACKER) == 0) {
                    if (response.request.compareTo(Protocol.TRACKER_REQUEST_GET) == 0) {
                        TrackerGetResponse tgr = (TrackerGetResponse) response;

                        if (null != tgr.values.version)
                            version = ApiVersion.fromInt(tgr.values.version);

                        if (null != tgr.values.frameRate)
                            frameRate = FrameRate.fromInt(tgr.values.frameRate);

                        if (null != tgr.values.trackerState) {
                            // if tracker state changed, notify listeners
                            if (null == trackerState
                                    || tgr.values.trackerState != TrackerState.toInt(trackerState)) {
                                trackerState = TrackerState.fromInt(tgr.values.trackerState);

                                broadcastToListeners(mTrackerStateListeners, tgr.values.trackerState);
                            }
                        }

                        if (null != tgr.values.isCalibrating)
                            isCalibrating = tgr.values.isCalibrating;
                        if (null != tgr.values.isCalibrated)
                            isCalibrated = tgr.values.isCalibrated;

                        // if defined in json response, then set
                        if (null != tgr.values.calibrationResult) {
                            // is calibration result different from current?
                            if (null == lastCalibrationResult
                                    || !lastCalibrationResult.equals(tgr.values.calibrationResult)) {
                                lastCalibrationResult = tgr.values.calibrationResult;

                                broadcastToListeners(mCalibrationResultListeners, isCalibrated, lastCalibrationResult);
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
                        if (null != tgr.values.screenIndex) {
                            // if screen index changed, broadcast to all listeners
                            if (tgr.values.screenIndex.equals(screenIndex)) {
                                screenIndex = tgr.values.screenIndex;

                                broadcastToListeners(mScreenStateListeners, screenIndex, screenResolutionWidth, screenResolutionHeight,
                                        screenPhysicalWidth, screenPhysicalHeight);
                            }
                        }

                        if (null != tgr.values.frame) {
                            // broadcast frame to all listeners
                            latestGazeData = tgr.values.frame;
                            broadcastToListeners(mGazeListeners, tgr.values.frame);
                        }

                        // Special routine used for initialization
                        if (isInitializing) {
                            // we make sure response is initial get request and not a 'push mode' frame
                            if (null == tgr.values.frame) {
                                synchronized (initializationLock) {
                                    isInitializing = false;
                                    isInitialized = true;
                                    initializationLock.notify();
                                }
                            }
                        }
                    } else if (response.request.compareTo(Protocol.TRACKER_REQUEST_SET) == 0) {
                        // do nothing
                    }
                } else if (response.category.compareTo(Protocol.CATEGORY_CALIBRATION) == 0) {
                    if (response.request.compareTo(Protocol.CALIBRATION_REQUEST_START) == 0) {
                        isCalibrating = true;

                        if (null != mCalibrationListener)
                            try {
                                mCalibrationListener.onCalibrationStarted();
                            } catch (Exception e) {
                                System.out
                                        .println("Exception while calling ICalibrationProcessHandler.onCalibrationStarted() "
                                                + "on listener "
                                                + mCalibrationListener
                                                + ": "
                                                + e.getLocalizedMessage());
                                e.printStackTrace();
                            }
                    } else if (response.request.compareTo(Protocol.CALIBRATION_REQUEST_POINTSTART) == 0) {

                    } else if (response.request.compareTo(Protocol.CALIBRATION_REQUEST_POINTEND) == 0) {

                        final CalibrationPointEndResponse cper = (CalibrationPointEndResponse) response;

                        if (cper == null || cper.values.calibrationResult == null)
                        {
                            ++sampledCalibrationPoints;

                            if (null != mCalibrationListener)
                            {
                                // Notify calibration listener that a new calibration point has been sampled
                                try {
                                    mCalibrationListener.onCalibrationProgress((float) sampledCalibrationPoints
                                            / totalCalibrationPoints);
                                } catch (Exception e) {
                                    System.out
                                            .println("Exception while calling ICalibrationProcessHandler.OnCalibrationProgress() on listener "
                                                    + mCalibrationListener + ": " + e.getLocalizedMessage());
                                    e.printStackTrace();
                                }

                                if (sampledCalibrationPoints == totalCalibrationPoints)
                                {
                                    // Notify calibration listener that all calibration points have been sampled and the
                                    // analysis of the calibration results has begun
                                    try {
                                        mCalibrationListener.onCalibrationProcessing();
                                    } catch (Exception e) {
                                        System.out
                                                .println("Exception while calling ICalibrationProcessHandler.OnCalibrationProcessing() on listener "
                                                        + mCalibrationListener + ": " + e.getLocalizedMessage());
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        else
                        {
                            isCalibrated = cper.values.calibrationResult.result;
                            isCalibrating = !cper.values.calibrationResult.result;

                            // Evaluate resample points, we decrement according to number of points needing resampling
                            for (CalibrationPoint calibPoint : cper.values.calibrationResult.calibpoints) {
                                if (calibPoint.state == CalibrationPoint.STATE_RESAMPLE
                                        || calibPoint.state == CalibrationPoint.STATE_NO_DATA) {
                                    --sampledCalibrationPoints;
                                }
                            }

                            // Notify calibration result listeners if calibration changed
                            if (null == lastCalibrationResult
                                    || !lastCalibrationResult.equals(cper.values.calibrationResult)) {
                                lastCalibrationResult = cper.values.calibrationResult;

                                broadcastToListeners(mCalibrationResultListeners, isCalibrated, cper.values.calibrationResult);
                            }

                            if (null != mCalibrationListener) {

                                // Notify calibration listener that calibration results are ready for evaluation
                                try {
                                    mCalibrationListener.onCalibrationResult(cper.values.calibrationResult);
                                } catch (Exception e) {
                                    System.out
                                            .println("Exception while calling ICalibrationProcessHandler.OnCalibrationResult() on listener "
                                                    + mCalibrationListener + ": " + e.getLocalizedMessage());
                                    e.printStackTrace();
                                }
                            }
                        }

                    } else if (response.request.compareTo(Protocol.CALIBRATION_REQUEST_ABORT) == 0) {
                        isCalibrating = false;

                        // restore states of last calibration if any
                        if (isActivated())
                            apiManager.requestCalibrationStates();
                    } else if (response.request.compareTo(Protocol.CALIBRATION_REQUEST_CLEAR) == 0) {
                        isCalibrated = false;
                        isCalibrating = false;
                        lastCalibrationResult = null;
                    }
                }
                else if(parseApiResponse(response, request))
                {
                    //consume
                }
                else
                {
                    ResponseFailed rf = (ResponseFailed) response;

                    /*
                     * JSON Message status code is different from HttpURLConnection.HTTP_OK. Check if special
                     * EyeTribe API specific status code before handling error
                     */

                    switch (rf.statuscode) {
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

                if (null != request)
                    request.finish();

            }
            catch (Exception e)
            {
                System.out.println("Exception while executing ThreadPool task: " + e.getLocalizedMessage());

                if(IS_DEBUG_MODE)
                    e.printStackTrace();
            }
        });
    }

    private void broadcastToListeners(final List<IGazeListener> listeners, GazeData gazeData)
    {
        broadcastToListeners(IGazeListener.class, listeners, gazeData);
    }

    private void broadcastToListeners(final List<IConnectionStateListener> listeners, boolean isConnected)
    {
        broadcastToListeners(IConnectionStateListener.class, listeners, isConnected);
    }

    private void broadcastToListeners(final List<ITrackerStateListener> listeners, int trackerState)
    {
        broadcastToListeners(ITrackerStateListener.class, listeners, trackerState);
    }

    private void broadcastToListeners(final List<ICalibrationResultListener> listeners, boolean isCalibrated, CalibrationResult calibResult)
    {
        broadcastToListeners(ICalibrationResultListener.class, listeners, isCalibrated, calibResult);
    }

    private void broadcastToListeners(final List<IScreenStateListener> listeners, int screenIndex, int screenResolutionWidth, int screenResolutionHeight,
                                     float screenPhysicalWidth, float screenPhysicalHeight)
    {
        broadcastToListeners(IScreenStateListener.class, listeners, screenIndex, screenResolutionWidth, screenResolutionHeight,
                screenPhysicalWidth, screenPhysicalHeight);
    }

    private void broadcastToListeners(final Class<?> listType, final List<?> listeners, final Object... objs)
    {
        try
        {
            for (Object l : listeners)
            {
                try
                {
                    submitToThreadpool(() ->
                    {
                        if (listType.isAssignableFrom(IGazeListener.class))
                            ((IGazeListener) l).onGazeUpdate((GazeData) objs[0]);
                        else if (listType.isAssignableFrom(IConnectionStateListener.class))
                            ((IConnectionStateListener) l).onConnectionStateChanged((boolean) objs[0]);
                        else if (listType.isAssignableFrom(ICalibrationResultListener.class))
                            ((ICalibrationResultListener) l).onCalibrationChanged((boolean) objs[0], (CalibrationResult) objs[1]);
                        else if (listType.isAssignableFrom(ITrackerStateListener.class))
                            ((ITrackerStateListener) l).onTrackerStateChanged((int) objs[0]);
                        else if (listType.isAssignableFrom(IScreenStateListener.class))
                            ((IScreenStateListener) l).onScreenStatesChanged((int) objs[0], (int) objs[1], (int) objs[2], (float) objs[3], (float) objs[4]);
                    });
                }
                catch (Exception e)
                {
                    System.out
                            .println("Exception while calling listener "
                                    + l.getClass().getSimpleName()
                                    + " on Thread "
                                    + Thread.currentThread()
                                    + ": " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception while broadcasting to listeners: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onGazeApiConnectionStateChanged(final boolean isConnected)
    {
        if (!isInitializing)
        {
            // Notify listeners of change in connection state
            broadcastToListeners(mConnectionStateListeners, isConnected);
        }
    }


    /**
     * Mode in witch the EyeTribe server delivers gaze data stream
     */
    public enum ClientMode
    {
        /**@deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default*/
        PUSH(1001),
        /**@deprecated as of EyeTribe Server v.0.9.77 ClientMode Push is default*/
        PULL(1002);

        private int clientMode;

        private ClientMode(int clientMode)
        {
            this.clientMode = clientMode;
        }
    }

    /**
     * The EyeTribe API compliance levels
     */
    public enum ApiVersion
    {
        /**
         * ApiVersion is undefined. GazeManager not activated
         */
        VERSION_UNDEFINED(0),
        VERSION_1_0(1),
        ;

        private int version;

        ApiVersion(int version)
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
     * State of the connected TrackerDevice.
     */
    public enum TrackerState
    {
        /**
         * Tracker device is detected and working
         */
        TRACKER_CONNECTED(0),
        /**
         * Tracker device is not detected
         */
        TRACKER_NOT_CONNECTED(1),
        /**
         * Tracker device is detected but not working due to wrong/unsupported firmware
         */
        TRACKER_CONNECTED_BADFW(2),
        /**
         * Tracker device is detected but not working due to unsupported USB host
         */
        TRACKER_CONNECTED_NOUSB3(3),
        /**
         * Tracker device is detected but not working due to no stream could be received
         */
        TRACKER_CONNECTED_NOSTREAM(4),
        /**
         * Tracker state is undefined. GazeManager not activated
         */
        TRACKER_UNDEFINED(-1),
        ;

        private int trackerState;

        TrackerState(int trackerState)
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
     * The possible frame rates of the EyeTribe Server
     */
    public enum FrameRate
    {
        /**
         * FrameRate is undefined. GazeManager not activated
         */
        FPS_UNDEFINED(0),
        FPS_30(30),
        FPS_60(60),
        ;

        private int frameRate;

        FrameRate(int frameRate) {
            this.frameRate = frameRate;
        }

        private static FrameRate[] values = null;

        public static FrameRate fromInt(int i) {
            if (FrameRate.values == null) {
                FrameRate.values = FrameRate.values();
            }
            for (FrameRate frate : values)
                if (frate.frameRate == i)
                    return frate;
            return null;
        }

        public static int toInt(FrameRate fr) {
            if (null != fr)
                return fr.frameRate;
            else
                return 0;
        }
    }

    private void resetEnums()
    {
        trackerState = TrackerState.TRACKER_UNDEFINED;
        frameRate = FrameRate.FPS_UNDEFINED;
        version = ApiVersion.VERSION_UNDEFINED;
    }

    abstract protected GazeApiManager createApiManager(IGazeApiResponseListener responseListener, IGazeApiConnectionListener connectionListener);

    abstract protected boolean parseApiResponse(final Response response, final Request request);
}

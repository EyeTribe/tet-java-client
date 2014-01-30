package com.theeyetribe.client;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.theeyetribe.client.GazeApiManager.GazeApiResponseListener;
import com.theeyetribe.client.data.CalibrationResult;
import com.theeyetribe.client.data.GazeData;
import com.theeyetribe.client.reply.CalibrationPointEndReply;
import com.theeyetribe.client.reply.ReplyBase;
import com.theeyetribe.client.reply.ReplyFailed;
import com.theeyetribe.client.reply.TrackerGetReply;

/**
 * This singleton is the main entry point of the TET C# Client. It manages all routines associated to gaze control.
 * <p>
 * Using this class a developer can 'calibrate' an eye tracking setup and attach listeners to receive live data streams
 * of {@link TETCSharpClient.Data.GazeData} updates.
 */
public class GazeManager implements GazeApiResponseListener
{
	private final static int FRAME_QUEUE_SIZE = 10;
	
	private static GazeManager instance;
	
	protected List<IGazeListener> gazeListeners;
	protected List<ICalibrationResultListener> calibrationResultListeners;
	protected List<ITrackerStateListener> trackerStateListeners;
	
	protected ICalibrationProcessHandler calibrationListener;
	protected int totalCalibrationPoints;
	protected int sampledCalibrationPoints;
	
	protected BlockingDeque<GazeData> gazeDeque;
	
	protected GazeBroadcaster gazeBroadcaster;
	
	protected Heartbeat heartbeatHandler;
	
	protected GazeApiManager apiManager;
	
	private ExecutorService threadPool;
	
	protected boolean isActive;
	
    protected Object initializationLock;
    
	protected TrackerState trackerState;
    protected CalibrationResult lastCalibrationResult;
    protected FrameRate frameRate;
    protected ClientMode clientMode;
    protected ApiVersion version;
	protected Boolean isCalibrated;
	protected Boolean isCalibrating;
	protected Integer heartbeatMillis = 3000; //default value
	protected Integer screenIndex;
	protected Integer screenResolutionWidth;
	protected Integer screenResolutionHeight;
	protected Float screenPhysicalWidth;
	protected Float screenPhysicalHeight;
	
	private GazeManager()
	{
		gazeListeners = Collections.synchronizedList( new ArrayList<IGazeListener>() ) ;
		calibrationResultListeners = Collections.synchronizedList( new ArrayList<ICalibrationResultListener>() ) ;
		trackerStateListeners = Collections.synchronizedList( new ArrayList<ITrackerStateListener>() ) ;
		
		gazeDeque = new LinkedBlockingDeque<GazeData>(FRAME_QUEUE_SIZE);
		gazeBroadcaster = new GazeBroadcaster();
		heartbeatHandler = new Heartbeat();
	}

	public static GazeManager getInstance()
	{
		if(null == instance)
			instance = new GazeManager();

		return instance;
	}
	
	/**
	 * Activates TET C# Client and all underlying routines using default values. Should be called _only_
	 * once when an application starts up. Calling thread will be locked during initialization.
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
	 * Activates TET C# Client and all underlying routines. Should be called _only_ once when an 
	 * application starts up. Calling thread will be locked during initialization.
	 * 
	 * @param version Version number of the Tracker API that this client will be compliant to
	 * @param mode Mode though which the client will receive GazeData. Either ClientMode.PUSH or ClientMode.PULL
	 * @param hostname The host name or IP address where the eye tracking server is running
	 * @param mode The port number used for the eye tracking server
	 * @return portnumber if successfully activated, false otherwise
	 */
	public boolean activate(ApiVersion version, ClientMode mode, String hostname, int portnumber)
	{	
        //if already running, deactivate before starting anew		
		if(isActive)
			deactivate();
		
        //lock calling thread while initializing
        initializationLock = Thread.currentThread();
        synchronized (initializationLock)
        {
    		try 
    		{
    			threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    			
    			apiManager = new GazeApiManager(this);
    			apiManager.connect(hostname, portnumber);
    			
    			if(apiManager.isConnected())
    			{
    				apiManager.requestTracker( mode, version);
    				apiManager.requestAllStates();
    				
    				//We wait untill above requests have been handled by server
    				initializationLock.wait();
    				
					if(!heartbeatHandler.isAlive())
						heartbeatHandler.start();
    				
    				isActive = true;
    			}
    			else
    				System.out.println("Error initializing GazeManager");
    		}
    		catch (Exception e) 
    		{
    			System.out.println("Error initializing GazeManager: "+e.getLocalizedMessage());
    		}

            return isActive;
        }
	}
	
	public void deactivate()
	{
		
		if(null != heartbeatHandler && heartbeatHandler.isAlive())
			heartbeatHandler.stop();
		
		if(null != apiManager)
		{
			apiManager.close();
			apiManager = null;
		}
		
		clearListeners();
		
		threadPool.shutdownNow();
		
		isActive = false;
	}
	
	/**
	 * @return Is the client library connected to Tracker Server?
	 */
	public boolean isConnected()
	{
		return null != apiManager ? apiManager.isConnected() : false;
	}
	
	/**
	 * Is the client in the middle of a calibration process?
	 */	
	public boolean isCalibrating()
	{
		return isCalibrating;
	}
	
	/**
	 * Is the client already calibrated?
	 */	
	public boolean isCalibrated()
	{
		return isCalibrated;
	}
	
	/**
	 * Index of currently used screen. Used for multiscreen setups.
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
	 * <p>.
	 * The Tracker Server defines the desired
	 * length of a heartbeat and is in this implementation automatically acquired 
	 * through the Tracker API.
	 */
	public int getHeartbeatMillis()
	{
		return heartbeatMillis;
	}
	
    /**
     * The latest performed and valid CalibrationResult. Note the result is not necessarily positive
     * and clients should evaluate the result before using. 
     */
	public CalibrationResult getLastCalibrationResult()
	{
		return lastCalibrationResult;
	}
	
    /**
     * Number of frames per second delivered by Tracker Server
     */
	public FrameRate getFrameRate()
	{
		return frameRate;
	}	
	
	/**
	 * Current API version compliance of Tracker Server
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

	public void calibrationStart(int numCalibrationPoints, ICalibrationProcessHandler listener)
	{
		if(isActive)
		{
			sampledCalibrationPoints = 0;
			totalCalibrationPoints = numCalibrationPoints;
			calibrationListener = listener;
			apiManager.requestCalibrationStart(numCalibrationPoints);
		}
		else
			System.out.println("TET Java Client not activated!");
	}
	
    public void calibrationPointStart(int x, int y)
	{
		if(isActive)
		{
			if(isCalibrating)
			{
				apiManager.requestCalibrationPointStart(x, y);
			}
			else
				System.out.println("TET Java Client calibration not started!");
		}
		else
			System.out.println("TET Java Client not activated!");
    }

    public void calibrationPointEnd()
	{
		if(isActive)
		{
			if(isCalibrating)
			{
				apiManager.requestCalibrationPointEnd();
			}
			else
				System.out.println("TET Java Client calibration not started!");
		}
		else
			System.out.println("TET Java Client not activated!");
    }

    public void calibrationAbort()
	{
		if(isActive)
		{
			if(isCalibrating)
			{
				apiManager.requestCalibrationAbort();
			}
			else
				System.out.println("TET Java Client calibration not started!");
		}
		else
			System.out.println("TET Java Client not activated!");
    }
    
    public void calibrationClear()
	{
		if(isActive)
		{
			apiManager.requestCalibrationClear();
		}
		else
			System.out.println("TET Java Client not activated!");
    }
    
    public void switchScreen(int screenIndex, int screenResW, int screenResH, int screenPsyW, int screenPsyH)
    {
        if (isActive)
        {
            apiManager.requestScreenSwitch(screenIndex, screenResW, screenResH, screenPsyW, screenPsyH);
        }
        else
        	System.out.println("TET Java Client not activated!");
    }
	
	public void addGazeListener(IGazeListener listener)
	{
		if(null == listener)
			throw new IllegalArgumentException("IGazeListener is NULL! Cannot add listener.");
		
		if(!gazeListeners.contains(listener))
			synchronized (gazeListeners) 
			{
				//if first listener
				if(gazeListeners.size() == 0)
				{
					if(!gazeBroadcaster.isBroadcasting())
						gazeBroadcaster.start();
				}
				
				gazeListeners.add(listener);
			}
	}

	public boolean removeGazeListener(IGazeListener listener)
	{
		boolean result = false;
		
		if(null != listener && null != gazeListeners && gazeListeners.contains(listener))
		{
			synchronized (gazeListeners) 
			{
				result = gazeListeners.remove(listener);
				
				//if no listeners
				if(gazeListeners.size() == 0)
				{
					if(null != gazeBroadcaster && gazeBroadcaster.isBroadcasting())
						gazeBroadcaster.stop();
				}
			}
		}
		
		return result;
	}
	
	public int getNumGazeListeners()
	{
		if(null != gazeListeners)
			return gazeListeners.size();
		
		return -1;
	}

	public boolean hasGazeListener(IGazeListener listener)
	{
		if(null != listener && null != gazeListeners && gazeListeners.contains(listener))
			return true;

		return false;
	}
	
	public void addCalibrationResultListener(ICalibrationResultListener listener)
	{
		if(null == listener)
			throw new IllegalArgumentException("ICalibrationResultListener is NULL! Cannot add listener.");
		
		if(!calibrationResultListeners.contains(listener))
			synchronized (calibrationResultListeners) 
			{		
				calibrationResultListeners.add(listener);
			}
	}

	public boolean removeCalibrationResultListener(ICalibrationResultListener listener)
	{
		boolean result = false;
		
		if(null != listener && null != calibrationResultListeners && calibrationResultListeners.contains(listener))
		{
			synchronized (calibrationResultListeners) 
			{
				result = calibrationResultListeners.remove(listener);
			}
		}
		
		return result;
	}
	
	public int getNumCalibrationResultListeners()
	{
		if(null != calibrationResultListeners)
			return calibrationResultListeners.size();
		
		return -1;
	}

	public boolean hasCalibrationResultListener(ICalibrationResultListener listener)
	{
		if(null != listener && null != calibrationResultListeners && calibrationResultListeners.contains(listener))
			return true;

		return false;
	}
	
	public void addTrackerStateListener(IGazeListener listener)
	{
		if(null == listener)
			throw new IllegalArgumentException("GazeUpdateListener is NULL! Cannot add listener.");
		
		if(!gazeListeners.contains(listener))
			synchronized (gazeListeners) 
			{
				//if first listener
				if(gazeListeners.size() == 0)
				{
					if(!gazeBroadcaster.isBroadcasting())
						gazeBroadcaster.start();
				}
				
				gazeListeners.add(listener);
			}
	}

	public boolean removeTrackerStateListener(IGazeListener listener)
	{
		boolean result = false;
		
		if(null != listener && null != gazeListeners && gazeListeners.contains(listener))
		{
			synchronized (gazeListeners) 
			{
				result = gazeListeners.remove(listener);
				
				//if no listeners
				if(gazeListeners.size() == 0)
				{
					if(null != gazeBroadcaster && gazeBroadcaster.isBroadcasting())
						gazeBroadcaster.stop();
				}
			}
		}
		
		return result;
	}
	
	public int getNumTrackerStateListeners()
	{
		if(null != gazeListeners)
			return gazeListeners.size();
		
		return -1;
	}

	public boolean hasTrackerStateListener(IGazeListener listener)
	{
		if(null != listener && null != gazeListeners && gazeListeners.contains(listener))
			return true;

		return false;
	}

	public void clearListeners() 
	{
		if( null != gazeListeners)
		{
			synchronized (gazeListeners) 
			{
				gazeListeners.clear();
			}
		}
		
		if( null != calibrationResultListeners)
		{
			synchronized (calibrationResultListeners) 
			{
				calibrationResultListeners.clear();
			}
		}
		
		if( null != trackerStateListeners)
		{
			synchronized (trackerStateListeners) 
			{
				trackerStateListeners.clear();
			}
		}
		
		if( null != gazeDeque)
		{
			synchronized (gazeDeque) 
			{
				gazeDeque.clear();
			}
		}		
		
		if(null != gazeBroadcaster && gazeBroadcaster.isBroadcasting())
			gazeBroadcaster.stop();
	}

	@Override
	public void onGazeApiResponse(String response)
	{
		Gson gson = new Gson();
		ReplyBase reply = gson.fromJson(response, ReplyBase.class);
		
		if(reply.statuscode == HttpURLConnection.HTTP_OK)
		{
			if(reply.category.compareTo(Protocol.CATEGORY_TRACKER) == 0)
			{
				if(reply.request.compareTo(Protocol.TRACKER_REQUEST_GET) == 0)
				{
					JsonParser jsonParser = new JsonParser();
					JsonObject jo = (JsonObject)jsonParser.parse(response);
					TrackerGetReply tgr = gson.fromJson(jo, TrackerGetReply.class);
					
					if(null != tgr.values.version)
						version = ApiVersion.fromInt(tgr.values.version);
					
					if(null != tgr.values.push)
					{
                        if (tgr.values.push)
                            clientMode = ClientMode.PUSH;
                        else
                            clientMode = ClientMode.PULL;
					}
					
					if(null != tgr.values.heartbeatInterval)
						heartbeatMillis = tgr.values.heartbeatInterval;
					
					if(null != tgr.values.frameRate)
						frameRate = FrameRate.fromInt(tgr.values.frameRate);		
					
					if(null != tgr.values.trackerState)
                    {
                        //if tracker state changed, notify listeners
                        if (tgr.values.trackerState != TrackerState.toInt(trackerState))
                        {
                        	trackerState = TrackerState.fromInt(tgr.values.trackerState);
                           
							synchronized (trackerStateListeners) 
							{
								for(final ITrackerStateListener listener : trackerStateListeners)
								{
									threadPool.execute(new Runnable()
									{
										@Override
										public void run() 
										{
											try 
											{
												listener.onTrackerStateChanged(TrackerState.toInt(trackerState));
											}
											catch (Exception e) 
											{
												System.out.println("Exception while calling ITrackerStateListener.OnTrackerConnectionChanged() on listener " +
														listener + ": " + e.getLocalizedMessage());
											}
										}
									});
								}
							}
                        }
                    }
					
					//if defined in json response, then set
                    if (((JsonObject)jo.get(Protocol.KEY_VALUES)).has(Protocol.TRACKER_CALIBRATIONRESULT))
                        lastCalibrationResult = tgr.values.calibrationResult;					
					
					if(null != tgr.values.isCalibrating)
						isCalibrating = tgr.values.isCalibrating;
					if(null != tgr.values.isCalibrated)
					{
						if(tgr.values.isCalibrated != isCalibrated)
						{
							//if calibration result changed, broadcast to all listeners
							isCalibrated = tgr.values.isCalibrated;
							synchronized (calibrationResultListeners) 
							{
								for(final ICalibrationResultListener listener : calibrationResultListeners)
								{
									threadPool.execute(new Runnable()
									{
										@Override
										public void run() 
										{
											try 
											{
												listener.onCalibrationChanged(isCalibrated, lastCalibrationResult);
											}
											catch (Exception e) 
											{
												System.out.println("Exception while calling ICalibrationResultListener.OnCalibrationChanged() on listener " +
														listener + ": " + e.getLocalizedMessage());
											}
										}
									});
								}
							}
						}
					}	
					
					if(null != tgr.values.screenResolutionWidth)
						screenResolutionWidth = tgr.values.screenResolutionWidth;
					if(null != tgr.values.screenResolutionHeight)
						screenResolutionHeight = tgr.values.screenResolutionHeight;
					if(null != tgr.values.screenPhysicalWidth)
						screenPhysicalWidth = tgr.values.screenPhysicalWidth;
					if(null != tgr.values.screenPhysicalHeight)
						screenPhysicalHeight = tgr.values.screenPhysicalHeight;					
					if(null != tgr.values.screenIndex)
					{			
						//if screen index changed, broadcast to all listeners
                        if (tgr.values.screenIndex != screenIndex)
                        {
							screenIndex = tgr.values.screenIndex;
                           
							synchronized (trackerStateListeners) 
							{
								for(final ITrackerStateListener listener : trackerStateListeners)
								{
									threadPool.execute(new Runnable()
									{
										@Override
										public void run() 
										{
											try 
											{
												listener.OnScreenStatesChanged(screenIndex, screenResolutionWidth, screenResolutionHeight, screenPhysicalWidth, screenPhysicalHeight);
											}
											catch (Exception e) 
											{
												System.out.println("Exception while calling ITrackerStateListener.OnScreenIndexChanged() on listener " + 
														listener + ": " + e.getLocalizedMessage());
											}
										}
									});
								}
							}
                        }						
					}
										
					//Add to high frequency broadcasting queue
					if(null != tgr.values.frame)
						//make room in queue, if full
						while(!gazeDeque.offer(tgr.values.frame))
							gazeDeque.poll();
					
                    //Special routine used for initialization
                    if (initializationLock != null)
                    {
                        synchronized (initializationLock)
                        {
                            initializationLock.notify();
                            initializationLock = null;
                        }
                    }
				}
				else
				if(reply.request.compareTo(Protocol.TRACKER_REQUEST_SET) == 0)
				{
					//do nothing
				}
			}
			else
			if(reply.category.compareTo(Protocol.CATEGORY_CALIBRATION) == 0)
			{
				if(reply.request.compareTo(Protocol.CALIBRATION_REQUEST_START) == 0)
				{
					isCalibrating = true;
					
					if(null != calibrationListener)
						try 
						{
							calibrationListener.onCalibrationStarted();
						}
						catch (Exception e) 
						{
							System.out.println("Exception while calling ICalibrationProcessHandler.onCalibrationStarted() " +
									"on listener " + calibrationListener + ": " + e.getLocalizedMessage());
						}
				}
				else
				if(reply.request.compareTo(Protocol.CALIBRATION_REQUEST_POINTSTART) == 0)
				{

				}
				else
				if(reply.request.compareTo(Protocol.CALIBRATION_REQUEST_POINTEND) == 0)
				{
					++sampledCalibrationPoints;

                    if (null != calibrationListener)
                    {
                        //Notify calibration listener that a new calibration point has been sampled
                        try
                        {
                            calibrationListener.onCalibrationProgress(sampledCalibrationPoints / totalCalibrationPoints);
                        }
                        catch (Exception e)
                        {
                            System.out.println("Exception while calling ICalibrationProcessHandler.OnCalibrationProgress() on listener " + 
                            		calibrationListener + ": " + e.getLocalizedMessage());
                        }
                        

                        if (sampledCalibrationPoints == totalCalibrationPoints)
                            //Notify calibration listener that all calibration points have been sampled and the analysis of the calirbation results has begun 
                            try
                            {
                                calibrationListener.onCalibrationProcessing();
                            }
                            catch (Exception e)
                            {
                                System.out.println("Exception while calling ICalibrationProcessHandler.OnCalibrationProcessing() on listener " +
                                		calibrationListener + ": " + e.getLocalizedMessage());
                            }
                    }

                    final CalibrationPointEndReply cper = gson.fromJson(response, CalibrationPointEndReply.class);

                    if (cper == null || cper.values.calibresult == null)
                        return; // not done with calibration yet

                    //if calibration state changed, notify listeners
                    if (cper.values.calibresult.result != isCalibrated)
                    {
                        synchronized (calibrationResultListeners) 
    					{
    						for(final ICalibrationResultListener listener : calibrationResultListeners)
    						{
    							threadPool.execute(new Runnable()
    							{
    								@Override
    								public void run() 
    								{
    									try 
    									{
    										listener.onCalibrationChanged(cper.values.calibresult.result, cper.values.calibresult);
    									}
    									catch (Exception e) 
    									{
    	                                    System.out.println("Exception while calling ICalibrationResultListener.OnCalibrationChanged() on listener " +
    	                                    		listener + ": " + e.getLocalizedMessage());
    									}
    								}
    							});
    						}
    					}
                    }

                    isCalibrated = cper.values.calibresult.result;
                    isCalibrating = !cper.values.calibresult.result;

                    if(isCalibrated)
                        lastCalibrationResult = cper.values.calibresult;

                    if (null != calibrationListener)
                    {
                        //Notify calibration listener that calibration results are ready for evaluation
                        try
                        {
                            calibrationListener.onCalibrationResult(cper.values.calibresult);
                        }
                        catch (Exception e)
                        {
                            System.out.println("Exception while calling ICalibrationProcessHandler.OnCalibrationResult() on listener " +
                            		calibrationListener + ": " + e.getLocalizedMessage());
                        }
                    }
				}
				else
				if(reply.request.compareTo(Protocol.CALIBRATION_REQUEST_ABORT) == 0)
				{
					isCalibrating = false;
					
					//restore states of last calibration if any
                    apiManager.requestCalibrationStates();
				}				
				else
				if(reply.request.compareTo(Protocol.CALIBRATION_REQUEST_CLEAR) == 0)
				{
                    isCalibrated = false;
                    isCalibrating = false;
                    lastCalibrationResult = null;
				}
			}
			else
			if(reply.category.compareTo(Protocol.CATEGORY_HEARTBEAT) == 0)
			{
				//do nothing
			}
			else
			{
				ReplyFailed rf = gson.fromJson(response, ReplyFailed.class);
				
            	System.out.println("Request FAILED");
            	System.out.println("Category: " + rf.category);
            	System.out.println("Request: " + rf.request);
            	System.out.println("StatusCode: " + rf.statuscode);
            	System.out.println("StatusMessage: " + rf.values.statusMessage);
			}
		}
		else
		{
			ReplyFailed rf = gson.fromJson(response, ReplyFailed.class);

            /* 
             * JSON Message status code is different from HttpURLConnection.HTTP_OK. Check if special TET 
             * specific status code before handling error 
             */

            switch (rf.statuscode)
            {
                case Protocol.STATUSCODE_CALIBRATION_UPDATE:
                    //The calibration state has changed, clients should update themselves
                    apiManager.requestCalibrationStates();
                    break;

                case Protocol.STATUSCODE_SCREEN_UPDATE:
                    //The primary screen index has changed, clients should update themselves
                    apiManager.requestScreenStates();
                    break;
                    
                case Protocol.STATUSCODE_TRACKER_UPDATE:
                    //The connected Tracker Device has changed state, clients should update themselves
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
	
	/**
	 *  Current running mode of this client
	 */
	public enum ClientMode 
	{
		PUSH(1001),
		PULL(1002);
		
		private int clientMode;
		
		private ClientMode(int clientMode)
		{
			this.clientMode = clientMode;
		}
	}
	
	/**
	 *  Current running mode of this client
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
			if(ApiVersion.values == null)
				ApiVersion.values = ApiVersion.values();
			for(ApiVersion v : values)
				if(v.version == i)
					return v;
			return null;
		}
		public static int toInt(ApiVersion v)
		{
			return v.version;
		}
	}
	
	/**
	 *  The current state of the connected TrackerDevice.
	 */	
	public enum TrackerState
	{
		TRACKER_CONNECTED(0),
        TRACKER_NOT_CONNECTED(1),
        TRACKER_CONNECTED_BADFW(2),
        TRACKER_CONNECTED_NOUSB3(3),
        TRACKER_CONNECTED_NOSTREAM(4);
        
        private int trackerState;

		private TrackerState(int trackerState) 
		{
			this.trackerState = trackerState;
		}
		
		private static TrackerState[] values = null;
		public static TrackerState fromInt(int i) {
			if(TrackerState.values == null)
				TrackerState.values = TrackerState.values();
			for(TrackerState state : values)
				if(state.trackerState == i)
					return state;
			return null;
		}
		public static int toInt(TrackerState ts)
		{
			if(null != ts)
				return ts.trackerState;
			else
				return TRACKER_NOT_CONNECTED.trackerState;
		}		
	}
	
	/**
	 *  The current state of the connected TrackerDevice.
	 */		
	public enum FrameRate
	{
		FPS_30(30),
		FPS_60(60);
		
		private int frameRate;
		
		private FrameRate(int frameRate)
		{
			this.frameRate = frameRate;
		}
		
		private static FrameRate[] values = null;
		public static FrameRate fromInt(int i) {
			if(FrameRate.values == null) {
				FrameRate.values = FrameRate.values();
			}
			for(FrameRate frate : values)
				if(frate.frameRate == i)
					return frate;
			return null;
		}		
	}

	/**
	 *  Threaded broadcaster responsible for distributing GazeData update to all attached listeners.
	 */
	private class GazeBroadcaster implements Runnable
	{
		private boolean isBroadcasting;

		public GazeBroadcaster()
		{
		}
		
		private void start()
		{
			isBroadcasting = true;
			new Thread(this).start();
		}
		
		private void stop() 
		{
			isBroadcasting = false;
			synchronized (this)
			{
				notify();
			}
		}
		
		private boolean isBroadcasting()
		{
			return isBroadcasting;
		}
		
		@Override
		public void run()
		{
			while(isBroadcasting)
			{
				try
				{
					//take latest from deque
					final GazeData gd = gazeDeque.takeLast();
					
					synchronized (gazeListeners) 
					{
						for(final IGazeListener listener : gazeListeners)
							
							threadPool.execute(new Runnable()
							{
								@Override
								public void run() 
								{
									try 
									{
										listener.onGazeUpdate(gd);
									}
									catch (Exception e) 
									{
										System.out.println("Exception while calling GazeUpdateListener.onGazeUpdate() " +
												"on listener " + listener + ": " + e.getLocalizedMessage());
									}									
								}
							});
					}
				} 
				catch (Exception e)
				{
					System.out.println("Internal error while broadcasting GazeData");
				}
			}
		}
	}
	
	/**
	 *  Class responsible for sending 'heartbeats' to the underlying TET C# Client Tracker
	 *  notifying that the client is alive.
	 *  The Tracker Server defines the desired length of a heartbeat and is in this
	 *  implementation automatically acquired through the Tracker API.
	 */	
	private class Heartbeat implements Runnable
	{
		private boolean isAlive;

		public Heartbeat()
		{
		}
		
		private void start()
		{
			isAlive = true;
			new Thread(this).start();
		}
		
		private void stop() 
		{
			isAlive = false;
			synchronized (this)
			{
				notify();
			}
		}
		
		private boolean isAlive()
		{
			return isAlive;
		}
		
		@Override
		public void run()
		{
			while(isAlive)
			{
				try
				{
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

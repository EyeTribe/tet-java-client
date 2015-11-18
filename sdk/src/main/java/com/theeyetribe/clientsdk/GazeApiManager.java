/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.theeyetribe.clientsdk.request.*;
import com.theeyetribe.clientsdk.response.CalibrationPointEndResponse;
import com.theeyetribe.clientsdk.response.Response;
import com.theeyetribe.clientsdk.response.ResponseFailed;
import com.theeyetribe.clientsdk.response.TrackerGetResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Networking class that manages communication with the underlying EyeTribe Server using the Tracker API over TCP Sockets.
 *
 * @see <a href="http://dev.theeyetribe.com/api/#api">EyeTribe API</a>
 */
class GazeApiManager
{
    static String DEFAULT_SERVER_HOST = "localhost";
    static int DEFAULT_SERVER_PORT = 6555;

    private Socket mSocket;

    private IncomingStreamHandler mIncomingStreamHandler;
    private OutgoingStreamHandler mOutgoingStreamHandler;

    private IGazeApiResponseListener mResponseListener;

    private IGazeApiConnectionListener mConnectionListener;

    protected PriorityBlockingQueue<Request<?>> mRequestQueue;

    protected ConcurrentHashMap<Integer, Request<?>> mOngoingRequests;

    protected AtomicInteger mIdGenerator = new AtomicInteger();

    protected Gson mGson;

    public GazeApiManager(IGazeApiResponseListener responseListener)
    {
        this(responseListener, null);
    }

    public GazeApiManager(IGazeApiResponseListener responseListener, IGazeApiConnectionListener connectionListener)
    {
        this.mResponseListener = responseListener;
        this.mConnectionListener = connectionListener;
        this.mGson = new Gson();
    }

    public void requestTracker(GazeManagerCore.ApiVersion version, GazeManagerCore.ClientMode mode)
    {
        TrackerSetRequest tsr = new TrackerSetRequest();

        tsr.values.version = GazeManagerCore.ApiVersion.toInt(version);
        tsr.values.push = mode.equals(GazeManagerCore.ClientMode.PUSH);

        tsr.id = mIdGenerator.incrementAndGet();

        request(tsr);
    }

    public void requestAllStates()
    {
        TrackerGetRequest tgr = new TrackerGetRequest();

        tgr.values = new String[]
        {
            Protocol.TRACKER_ISCALIBRATED,
            Protocol.TRACKER_ISCALIBRATING,
            Protocol.TRACKER_TRACKERSTATE,
            Protocol.TRACKER_SCREEN_INDEX,
            Protocol.TRACKER_SCREEN_RESOLUTION_WIDTH,
            Protocol.TRACKER_SCREEN_RESOLUTION_HEIGHT,
            Protocol.TRACKER_SCREEN_PHYSICAL_WIDTH,
            Protocol.TRACKER_SCREEN_PHYSICAL_HEIGHT,
            Protocol.TRACKER_CALIBRATIONRESULT,
            Protocol.TRACKER_FRAMERATE,
            Protocol.TRACKER_VERSION,
            Protocol.TRACKER_MODE_PUSH
        };

        tgr.id = mIdGenerator.incrementAndGet();

        request(tgr);
    }

    public void requestCalibrationStates()
    {
        TrackerGetRequest tgr = new TrackerGetRequest();

        tgr.values = new String[]
        {
            Protocol.TRACKER_ISCALIBRATED,
            Protocol.TRACKER_ISCALIBRATING,
            Protocol.TRACKER_CALIBRATIONRESULT
        };

        tgr.id = mIdGenerator.incrementAndGet();

        request(tgr);
    }

    public void requestScreenStates()
    {
        TrackerGetRequest tgr = new TrackerGetRequest();

        tgr.values = new String[]
        {
            Protocol.TRACKER_SCREEN_INDEX,
            Protocol.TRACKER_SCREEN_RESOLUTION_WIDTH,
            Protocol.TRACKER_SCREEN_RESOLUTION_HEIGHT,
            Protocol.TRACKER_SCREEN_PHYSICAL_WIDTH,
            Protocol.TRACKER_SCREEN_PHYSICAL_HEIGHT
        };

        tgr.id = mIdGenerator.incrementAndGet();

        request(tgr);
    }

    public void requestTrackerState()
    {
        TrackerGetRequest tgr = new TrackerGetRequest();

        tgr.values = new String[]
        {
            Protocol.TRACKER_TRACKERSTATE,
            Protocol.TRACKER_FRAMERATE
        };

        tgr.id = mIdGenerator.incrementAndGet();

        request(tgr);
    }

    public void requestHeartbeat()
    {
        Request r = new Request<>(Response.class);

        r.category = Protocol.CATEGORY_HEARTBEAT;

        request(r);
    }

    public Object requestCalibrationStart(int pointcount)
    {
        CalibrationStartRequest csr = new CalibrationStartRequest();

        csr.values.pointcount = pointcount;

        csr.id = mIdGenerator.incrementAndGet();

        csr.asyncLock = new Object();

        request(csr);

        return csr.asyncLock;
    }

    public void requestCalibrationPointStart(int x, int y)
    {
        CalibrationPointStartRequest cpsr = new CalibrationPointStartRequest();

        cpsr.values.x = x;
        cpsr.values.y = y;

        cpsr.id = mIdGenerator.incrementAndGet();

        request(cpsr);
    }

    public void requestCalibrationPointEnd()
    {
        CalibrationPointEndRequest cper = new CalibrationPointEndRequest();

        cper.id = mIdGenerator.incrementAndGet();

        request(cper);
    }

    public Object requestCalibrationAbort()
    {
        Request r = new Request<>(Response.class);

        r.category = Protocol.CATEGORY_CALIBRATION;
        r.request = Protocol.CALIBRATION_REQUEST_ABORT;

        r.id = mIdGenerator.incrementAndGet();

        r.asyncLock = new Object();

        request(r);

        return r.asyncLock;
    }

    public void requestCalibrationClear()
    {
        Request r = new Request<>(Response.class);

        r.category = Protocol.CATEGORY_CALIBRATION;
        r.request = Protocol.CALIBRATION_REQUEST_CLEAR;

        r.id = mIdGenerator.incrementAndGet();

        request(r);
    }

    public Object requestScreenSwitch(int screenIndex, int screenResW, int screenResH, float screenPsyW, float screenPsyH)
    {
        TrackerSetRequest tsr = new TrackerSetRequest();

        tsr.values.screenIndex = screenIndex;
        tsr.values.screenResolutionWidth = screenResW;
        tsr.values.screenResolutionHeight = screenResH;
        tsr.values.screenPhysicalWidth = screenPsyW;
        tsr.values.screenPhysicalHeight = screenPsyH;

        tsr.id = mIdGenerator.incrementAndGet();

        tsr.asyncLock = new Object();

        request(tsr);

        return tsr.asyncLock;
    }

    public void requestFrame()
    {
        TrackerGetRequest tgr = new TrackerGetRequest();

        tgr.values = new String[]
        {
            Protocol.TRACKER_FRAME,
        };

        tgr.id = mIdGenerator.incrementAndGet();

        request(tgr);
    }

    public Response parseIncomingProcessResponse(JsonObject json) { return null; }

    public synchronized boolean connect(String host, int port, long timeOut)
    {
        if (isConnected())
            close();

        try
        {
            //init containers
            this.mRequestQueue = new PriorityBlockingQueue<>();
            this.mOngoingRequests = new ConcurrentHashMap<>();

            // connect to socket, with timeout
            mSocket = new Socket();
            mSocket.connect(new InetSocketAddress(host, port), (int) timeOut);
            mSocket.setSoTimeout((int) timeOut);

            // notify connection change
            if (null != mConnectionListener)
                mConnectionListener.onGazeApiConnectionStateChanged(mSocket.isConnected());

            mIncomingStreamHandler = new IncomingStreamHandler();
            mIncomingStreamHandler.start();

            mOutgoingStreamHandler = new OutgoingStreamHandler();
            mOutgoingStreamHandler.start();

            return true;
        }
        catch (SocketTimeoutException ste)
        {
            System.out.println("Socket connection timed out: " + ste.getLocalizedMessage());
        }
        catch (IOException ioe)
        {
            System.out.println("Socket connection not available: " + ioe.getLocalizedMessage());
        }
        catch (Exception e)
        {
            System.out.println("Exception while establishing socket connection: " + e.getLocalizedMessage());
        }

        close();

        return false;
    }

    public synchronized void close()
    {
        try
        {
            if (null != mSocket)
                try
                {
                    mSocket.close();
                }
                catch (Exception e)
                {
                    // consume
                }
            mSocket = null;

            if (null != mIncomingStreamHandler)
                mIncomingStreamHandler.stop();
            mIncomingStreamHandler = null;

            if (null != mOutgoingStreamHandler)
                mOutgoingStreamHandler.stop();
            mOutgoingStreamHandler = null;

            // notify connection change
            if (null != mConnectionListener)
                mConnectionListener.onGazeApiConnectionStateChanged(false);

            if (null != mRequestQueue)
            {
                cancelAllRequests();
                synchronized (mRequestQueue)
                {
                    mRequestQueue.clear();
                }
            }
            mRequestQueue = null;

            if (null != mOngoingRequests)
            {
                Enumeration<Request<?>> reqs = mOngoingRequests.elements();

                while(reqs.hasMoreElements())
                {
                    reqs.nextElement().cancel();
                }
                mOngoingRequests.clear();
            }
            mOngoingRequests = null;
        }
        catch (Exception e)
        {
            System.out.println("Error closing socket: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return null != mSocket && mSocket.isConnected() && !mSocket.isClosed();
    }

    protected void request(Request request)
    {
        synchronized (mRequestQueue)
        {
            mRequestQueue.add(request);
        }
    }

    public void cancelAllRequests()
    {
        synchronized (mRequestQueue)
        {
            for(Request r : mRequestQueue)
            {
                r.cancel();
            }
        }
    }

    private class IncomingStreamHandler implements Runnable
    {
        private BufferedReader reader;

        private Thread runner;

        private synchronized void start()
        {
            stop();;

            runner = new Thread(this);
            runner.start();
        }

        private synchronized void stop()
        {
            if (null != runner)
                synchronized(runner)
                {
                    runner.interrupt();
                }
        }

        @Override
        public void run()
        {
            try
            {
                Request request;
                Response response;
                String responseJson;

                JsonParser jsonParser = new JsonParser();
                JsonObject jo;

                InputStream is = mSocket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                reader = new BufferedReader(isr);

                while (!Thread.interrupted())
                {
                    while ((responseJson = reader.readLine()) != null)
                    {
                        if (!responseJson.isEmpty() && null != mResponseListener)
                        {
                            if(GazeManagerCore.IS_DEBUG_MODE)
                                System.out.println("IN: " + responseJson);

                            jo = (JsonObject) jsonParser.parse(responseJson);
                            int id = null != jo.get(Protocol.KEY_ID) ? jo.get(Protocol.KEY_ID).getAsInt() : 0;
                            request = mOngoingRequests.containsKey(id) ? mOngoingRequests.remove(id) : null;

                            if (jo.get(Protocol.KEY_STATUSCODE).getAsInt() == HttpURLConnection.HTTP_OK)
                            {
                                if(request != null)
                                {
                                    //matching request handles parsing
                                    response = (Response) request.parseJsonResponse(jo, mGson);
                                    response.transitTime = System.currentTimeMillis() - request.timeStamp;
                                }
                                else
                                {
                                    // Incoming message has no id and is a reponse to a process or a pushed gaze data frame
                                    if (jo.get(Protocol.KEY_CATEGORY).getAsString().equals(Protocol.CATEGORY_CALIBRATION))
                                    {
                                        // response is calibration result
                                        response = mGson.fromJson(jo, CalibrationPointEndResponse.class);
                                    }
                                    else if (null != (response = parseIncomingProcessResponse(jo)))
                                    {
                                        // We allow the network layer extensions to optinally handle the process reponse
                                    }
                                    else
                                    {
                                        // response is gaze data frame
                                        response = mGson.fromJson(jo, TrackerGetResponse.class);
                                    }
                                }
                            }
                            else
                            {
                                //request failed
                                response = mGson.fromJson(jo, ResponseFailed.class);

                                if(request != null)
                                    response.transitTime = System.currentTimeMillis() - request.timeStamp;
                            }

                            if(GazeManagerCore.IS_DEBUG_MODE && response.transitTime != 0 )
                                System.out.println("IN: transitTime " + response.transitTime);

                            mResponseListener.onGazeApiResponse(response, request);
                        }
                    }
                }
            }
            catch (IOException ioe)
            {
                // consume
            	
            	if(GazeManagerCore.IS_DEBUG_MODE)
            	{
            		System.out.println("IncomingStreamHandler IO exception: " + ioe.getLocalizedMessage());
            		ioe.printStackTrace();
            	}
            }
            catch (Exception e)
            {
                System.out.println("Exception while establishing incoming socket connection: " + e.getLocalizedMessage());

                //connection has been lost
                close();

                if(GazeManagerCore.IS_DEBUG_MODE)
                	e.printStackTrace();
            }
            finally
            {
                try
                {
                    reader.close();
                }
                catch (Exception e2)
                {
                    // consume
                }
            }

            if(GazeManagerCore.IS_DEBUG_MODE)
                System.out.println("IncomingStreamHandler closing down");
        }
    }

    private class OutgoingStreamHandler implements Runnable
    {
        private final int NUM_WRITE_ATTEMPTS_BEFORE_FAIL = 3;

        private Thread runner;

        private synchronized void start()
        {
            stop();

            runner = new Thread(this);
            runner.start();
        }

        private synchronized void stop()
        {
            if (null != runner)
                synchronized(runner)
                {
                    runner.interrupt();
                }
        }

        @Override
        public void run()
        {
            BufferedWriter writer = null;

            try
            {
                Request request;
                String requestJson;

                OutputStream os = mSocket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                writer = new BufferedWriter(osw);

                while (!Thread.interrupted())
                {
                	request = mRequestQueue.take();

                    if(request.isCancelled())
                        continue;

                    request.timeStamp = System.currentTimeMillis();
                    requestJson = request.toJsonString(mGson);

                	while(true)
                	{
                        try
                        {
                            writer.write(requestJson);
                            writer.newLine();
                            writer.flush();

                            mOngoingRequests.put((Integer) request.id, request);

                            if(GazeManagerCore.IS_DEBUG_MODE)
                                System.out.println("OUT: " + requestJson);

                            break;
                        }
                        catch (IOException ioe)
                        {
                            // Has writing to socket failed and may server be disconnected?
                            if (++request.retryAttempts >= NUM_WRITE_ATTEMPTS_BEFORE_FAIL)
                            {
                                request.finish();
                                mOngoingRequests.remove(request.id);
                                throw new Exception("OutgoingStreamHandler failed writing to stream despite several retires");
                            }
                               
                        	if(GazeManagerCore.IS_DEBUG_MODE)
                        	{
                        		System.out.println("OutgoingStreamHandler IO exception: " + ioe.getLocalizedMessage());
                        		ioe.printStackTrace();
                        	}
                        }
                	}
                }
            }
            catch (InterruptedException e)
            {
                // consume
            	
            	if(GazeManagerCore.IS_DEBUG_MODE)
                	System.out.println("OutgoingStreamHandler interrupted!!!");
            }
            catch (Exception e)
            {
                System.out
                        .println("Exception while establishing outgoing socket connection: " + e.getLocalizedMessage());

                //connection has been lost
                close();

                if(GazeManagerCore.IS_DEBUG_MODE)
                	e.printStackTrace();
                
            }
            finally
            {
                try
                {
                    if(null != writer)
                    {
                        writer.flush();
                        writer.close();
                    }
                }
                catch (Exception e)
                {
                    // consume
                }
                
            }

            if(GazeManagerCore.IS_DEBUG_MODE)
                System.out.println("OutgoingStreamHandler closing down");
        }
    }

    /**
     * Callback interface responsible for handling messages returned from the GazeApiManager
     */
    protected interface IGazeApiResponseListener
    {
        void onGazeApiResponse(Response response, Request request);
    }

    /**
     * Callback interface responsible for handling connection state notifications from the GazeApiManager
     */
    protected interface IGazeApiConnectionListener
    {
        void onGazeApiConnectionStateChanged(boolean isConnected);
    }
}

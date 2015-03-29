/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.Gson;
import com.theeyetribe.client.GazeManager.ApiVersion;
import com.theeyetribe.client.GazeManager.ClientMode;
import com.theeyetribe.client.request.CalibrationPointStartRequest;
import com.theeyetribe.client.request.CalibrationStartRequest;
import com.theeyetribe.client.request.RequestBase;
import com.theeyetribe.client.request.TrackerGetRequest;
import com.theeyetribe.client.request.TrackerSetRequest;

/**
 * This class manages communication with the underlying EyeTribe Server using the Tracker API over TCP Sockets.
 */
class GazeApiManager
{
    static String DEFAULT_SERVER_HOST = "localhost";
    static int DEFAULT_SERVER_PORT = 6555;

    private Socket socket;

    private IncomingStreamHandler incomingStreamHandler;
    private OutgoingStreamHandler outgoingStreamHandler;

    private IGazeApiResponseListener responseListener;

    private IGazeApiConnectionListener connectionListener;

    private BlockingQueue<String> requestQueue;

    private Gson gson;

    public GazeApiManager(IGazeApiResponseListener responseListener)
    {
        this(responseListener, null);
    }

    public GazeApiManager(IGazeApiResponseListener responseListener, IGazeApiConnectionListener connectionListener)
    {
        this.responseListener = responseListener;
        this.connectionListener = connectionListener;
        this.gson = new Gson();
        this.requestQueue = new LinkedBlockingQueue<String>();
    }

    public void requestTracker(ClientMode mode, ApiVersion version)
    {
        TrackerSetRequest gr = new TrackerSetRequest();

        gr.values.version = ApiVersion.toInt(version);
        gr.values.push = mode == GazeManager.ClientMode.PUSH;

        request(gson.toJsonTree(gr, TrackerSetRequest.class).toString());
    }

    public void requestAllStates()
    {
        TrackerGetRequest gr = new TrackerGetRequest();

        gr.values = new String[]
        { Protocol.TRACKER_HEARTBEATINTERVAL, Protocol.TRACKER_ISCALIBRATED, Protocol.TRACKER_ISCALIBRATING,
                Protocol.TRACKER_TRACKERSTATE, Protocol.TRACKER_SCREEN_INDEX, Protocol.TRACKER_SCREEN_RESOLUTION_WIDTH,
                Protocol.TRACKER_SCREEN_RESOLUTION_HEIGHT, Protocol.TRACKER_SCREEN_PHYSICAL_WIDTH,
                Protocol.TRACKER_SCREEN_PHYSICAL_HEIGHT, Protocol.TRACKER_CALIBRATIONRESULT,
                Protocol.TRACKER_FRAMERATE, Protocol.TRACKER_VERSION, Protocol.TRACKER_MODE_PUSH };

        request(gson.toJsonTree(gr, TrackerGetRequest.class).toString());
    }

    public void requestCalibrationStates()
    {
        TrackerGetRequest gr = new TrackerGetRequest();

        gr.category = Protocol.CATEGORY_TRACKER;
        gr.request = Protocol.TRACKER_REQUEST_GET;
        gr.values = new String[]
        { Protocol.TRACKER_ISCALIBRATED, Protocol.TRACKER_ISCALIBRATING, Protocol.TRACKER_CALIBRATIONRESULT };

        request(gson.toJsonTree(gr, TrackerGetRequest.class).toString());
    }

    public void requestScreenStates()
    {
        TrackerGetRequest gr = new TrackerGetRequest();

        gr.values = new String[]
        { Protocol.TRACKER_SCREEN_INDEX, Protocol.TRACKER_SCREEN_RESOLUTION_WIDTH,
                Protocol.TRACKER_SCREEN_RESOLUTION_HEIGHT, Protocol.TRACKER_SCREEN_PHYSICAL_WIDTH,
                Protocol.TRACKER_SCREEN_PHYSICAL_HEIGHT };

        request(gson.toJsonTree(gr, TrackerGetRequest.class).toString());
    }

    public void requestTrackerState()
    {
        TrackerGetRequest gr = new TrackerGetRequest();

        gr.values = new String[]
        { Protocol.TRACKER_TRACKERSTATE, Protocol.TRACKER_FRAMERATE };

        request(gson.toJsonTree(gr, TrackerGetRequest.class).toString());
    }

    public void requestHeartbeat()
    {
        RequestBase gr = new RequestBase();

        gr.category = Protocol.CATEGORY_HEARTBEAT;

        request(gson.toJson(gr));
    }

    public void requestCalibrationStart(int pointcount)
    {
        CalibrationStartRequest gr = new CalibrationStartRequest();

        gr.values.pointcount = pointcount;

        request(gson.toJsonTree(gr, CalibrationStartRequest.class).toString());
    }

    public void requestCalibrationPointStart(int x, int y)
    {
        CalibrationPointStartRequest gr = new CalibrationPointStartRequest();

        gr.values.x = x;
        gr.values.y = y;

        request(gson.toJsonTree(gr, CalibrationPointStartRequest.class).toString());
    }

    public void requestCalibrationPointEnd()
    {
        RequestBase gr = new RequestBase();

        gr.category = Protocol.CATEGORY_CALIBRATION;
        gr.request = Protocol.CALIBRATION_REQUEST_POINTEND;

        request(gson.toJson(gr));
    }

    public void requestCalibrationAbort()
    {
        RequestBase gr = new RequestBase();

        gr.category = Protocol.CATEGORY_CALIBRATION;
        gr.request = Protocol.CALIBRATION_REQUEST_ABORT;

        request(gson.toJson(gr));
    }

    public void requestCalibrationClear()
    {
        RequestBase gr = new RequestBase();

        gr.category = Protocol.CATEGORY_CALIBRATION;
        gr.request = Protocol.CALIBRATION_REQUEST_CLEAR;

        request(gson.toJson(gr));
    }

    public void requestScreenSwitch(int screenIndex, int screenResW, int screenResH, float screenPsyW, float screenPsyH)
    {
        TrackerSetRequest gr = new TrackerSetRequest();

        gr.category = Protocol.CATEGORY_TRACKER;
        gr.request = Protocol.TRACKER_REQUEST_SET;

        gr.values.screenIndex = screenIndex;
        gr.values.screenResulutionWidth = screenResW;
        gr.values.screenResulutionHeight = screenResH;
        gr.values.screenPhysicalWidth = screenPsyW;
        gr.values.screenPhysicalHeight = screenPsyH;

        request(gson.toJsonTree(gr, TrackerSetRequest.class).toString());
    }

    public boolean connect(String host, int port)
    {
        if (isConnected())
            close();

        try
        {
            InetAddress address = InetAddress.getByName(host);
            socket = new Socket(address, port);

            // notify connection change
            if (null != connectionListener)
                connectionListener.onGazeApiConnectionStateChanged(socket.isConnected());

            incomingStreamHandler = new IncomingStreamHandler();
            incomingStreamHandler.start();

            outgoingStreamHandler = new OutgoingStreamHandler();
            outgoingStreamHandler.start();
        }
        catch (IOException ioe)
        {
            System.out.println("Unable to open socket. Is EyeTribe Server running? Exception: "
                    + ioe.getLocalizedMessage());

            close();

            return false;
        }
        catch (Exception e)
        {
            System.out.println("Exception while establishing socket connection. Is EyeTribe Server running? Exception: "
                            + e.getLocalizedMessage());
            close();
            return false;
        }

        return true;
    }

    public void close()
    {
        try
        {
            if (null != socket)
                try
                {
                    socket.close();
                }
                catch (Exception e)
                {
                    // consume
                }

            if (null != incomingStreamHandler)
                incomingStreamHandler.stop();

            if (null != outgoingStreamHandler)
                outgoingStreamHandler.stop();

            // notify connection change
            if (null != connectionListener)
                connectionListener.onGazeApiConnectionStateChanged(false);

            if (null != requestQueue)
                requestQueue.clear();

        }
        catch (Exception e)
        {
            System.out.println("Error closing socket: " + e.getMessage());
        }
    }

    public boolean isConnected()
    {
        if (null != socket)
            return socket.isConnected();

        return false;
    }

    protected void request(String request)
    {
        requestQueue.add(request);
    }

    private class IncomingStreamHandler implements Runnable
    {
        private BufferedReader reader;

        private Thread runner;

        private void start()
        {
            runner = new Thread(this);
            runner.start();
        }

        private void stop()
        {
            synchronized (this)
            {
                runner.interrupt();
            }
        }

        @Override
        public void run()
        {
            try
            {
                String response;

                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                reader = new BufferedReader(isr);

                while (!Thread.interrupted())
                {
                    while ((response = reader.readLine()) != null)
                    {
                        if (!response.isEmpty() && null != responseListener)
                        {
                            responseListener.onGazeApiResponse(response);
                        }
                    }
                }
            }
            catch (IOException ioe)
            {
                // consume
            }
            catch (Exception e)
            {
                System.out.println("Exception while etablishing incoming socket connection: " + e.getLocalizedMessage());
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
        }
    }

    private class OutgoingStreamHandler implements Runnable
    {
        private final int NUM_WRITE_ATTEMPTS_BEFORE_FAIL = 3;
        private int numWriteAttempt;

        private Thread runner;

        private void start()
        {
            runner = new Thread(this);
            runner.start();
        }

        private void stop()
        {
            synchronized (this)
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
                String request = null;

                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                writer = new BufferedWriter(osw);

                while (!Thread.interrupted())
                {
                    try
                    {
                        request = requestQueue.take();

                        writer.write(request);
                        writer.newLine();
                        writer.flush();

                        if (numWriteAttempt > 0)
                            numWriteAttempt = 0;
                    }
                    catch (IOException ioe)
                    {
                        // Has writing to socket failed and may server be disconnected?
                        if (numWriteAttempt++ >= NUM_WRITE_ATTEMPTS_BEFORE_FAIL)
                        {
                            // server must be disconnected, shut down network layer
                            GazeApiManager.this.close();

                            break;
                        }
                        else
                        {
                            // else retry request asap
                            requestQueue.add(request);
                        }
                    }
                }
            }
            catch (InterruptedException e)
            {
                // consume
            }
            catch (Exception e)
            {
                System.out.println("Exception while etablishing outgoing socket connection: " + e.getLocalizedMessage());
            }
            finally
            {
                try
                {
                    writer.close();
                }
                catch (Exception e2)
                {
                    // consume
                }
            }
        }
    }

    /**
     * Callback interface responsible for handling messages returned from the GazeApiManager
     */
    public interface IGazeApiResponseListener
    {
        public void onGazeApiResponse(String response);
    }

    /**
     * Callback interface responsible for handling connection state notifications from the GazeApiManager
     */
    public interface IGazeApiConnectionListener
    {
        public void onGazeApiConnectionStateChanged(boolean isConnected);
    }
}

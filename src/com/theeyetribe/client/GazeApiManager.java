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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
 * This class manages communication with the underlying Tracker Server using the Tracker API over TCP Sockets.
 */
public class GazeApiManager
{
	static String DEFAULT_SERVER_HOST = "localhost";
	static int DEFAULT_SERVER_PORT = 6555;

	private Socket socket;

	private IncommingStreamHandler incommingStreamHandler;
	private OutgoingStreamHandler outgoingStreamHandler;

	private GazeApiResponseListener responseListener;

	private BlockingQueue<String> requestQueue;

	public GazeApiManager(GazeApiResponseListener responseListener) 
	{
		this.responseListener = responseListener;
		this.requestQueue= new LinkedBlockingQueue<String>(); 
	}

	public void requestTracker(ClientMode mode, ApiVersion version)
	{
		Gson gson = new Gson();
		TrackerSetRequest gr = new TrackerSetRequest();

		gr.values.version = ApiVersion.toInt(version);
		gr.values.push = mode == GazeManager.ClientMode.PUSH;

		request(gson.toJsonTree(gr, TrackerSetRequest.class).toString());
	}

	public void requestAllStates()
	{
		Gson gson = new Gson();
		TrackerGetRequest gr = new TrackerGetRequest();

		gr.values = new String[]
				{
				Protocol.TRACKER_HEARTBEATINTERVAL,
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

		request(gson.toJsonTree(gr, TrackerGetRequest.class).toString());
	}

	public void requestCalibrationStates()
	{
		Gson gson = new Gson();
		TrackerGetRequest gr = new TrackerGetRequest();

		gr.category = Protocol.CATEGORY_TRACKER;
		gr.request = Protocol.TRACKER_REQUEST_GET;
		gr.values = new String[]
				{
				Protocol.TRACKER_ISCALIBRATED,
				Protocol.TRACKER_ISCALIBRATING,
				};

		request(gson.toJsonTree(gr, TrackerGetRequest.class).toString());
	}

	public void requestScreenStates()
	{
		Gson gson = new Gson();
		TrackerGetRequest gr = new TrackerGetRequest();

		gr.values = new String[]
				{
				Protocol.TRACKER_SCREEN_INDEX,
				Protocol.TRACKER_SCREEN_RESOLUTION_WIDTH,
				Protocol.TRACKER_SCREEN_RESOLUTION_HEIGHT,
				Protocol.TRACKER_SCREEN_PHYSICAL_WIDTH,
				Protocol.TRACKER_SCREEN_PHYSICAL_HEIGHT
				};

		request(gson.toJsonTree(gr, TrackerGetRequest.class).toString());
	}

	public void requestTrackerState()
	{
		Gson gson = new Gson();
		TrackerGetRequest gr = new TrackerGetRequest();

		gr.values = new String[]
				{
				Protocol.TRACKER_TRACKERSTATE,
				Protocol.TRACKER_FRAMERATE
				};

		request(gson.toJsonTree(gr, TrackerGetRequest.class).toString());
	}

	public void requestHeartbeat()
	{
		Gson gson = new Gson();
		RequestBase gr = new RequestBase();

		gr.category = Protocol.CATEGORY_HEARTBEAT;

		request(gson.toJson(gr));
	}

	public void requestCalibrationStart(int pointcount)
	{
		Gson gson = new Gson();
		CalibrationStartRequest gr = new CalibrationStartRequest();

		gr.values.pointcount = pointcount;

		request(gson.toJsonTree(gr, CalibrationStartRequest.class).toString());
	}

	public void requestCalibrationPointStart(int x, int y)
	{
		Gson gson = new Gson();
		CalibrationPointStartRequest gr = new CalibrationPointStartRequest();

		gr.values.x = x;
		gr.values.y = y;

		request(gson.toJsonTree(gr, CalibrationPointStartRequest.class).toString());
	}

	public void requestCalibrationPointEnd()
	{
		Gson gson = new Gson();
		RequestBase gr = new RequestBase();

		gr.category = Protocol.CATEGORY_CALIBRATION;
		gr.request = Protocol.CALIBRATION_REQUEST_POINTEND;

		request(gson.toJson(gr));
	}

	public void requestCalibrationAbort()
	{
		Gson gson = new Gson();
		RequestBase gr = new RequestBase();

		gr.category = Protocol.CATEGORY_HEARTBEAT;
		gr.request = Protocol.CALIBRATION_REQUEST_ABORT;

		request(gson.toJson(gr));
	}

	public void requestCalibrationClear()
	{
		Gson gson = new Gson();
		RequestBase gr = new RequestBase();

		gr.category = Protocol.CATEGORY_HEARTBEAT;
		gr.request = Protocol.CALIBRATION_REQUEST_CLEAR;

		request(gson.toJson(gr));
	}

	public void requestScreenSwitch(int screenIndex, int screenResW, int screenResH, float screenPsyW, float screenPsyH)
	{
		Gson gson = new Gson();
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
		close();

		try 
		{
			InetAddress address = InetAddress.getByName(host);
			socket = new Socket(address, port);

			incommingStreamHandler = new IncommingStreamHandler();
			incommingStreamHandler.start();

			outgoingStreamHandler = new OutgoingStreamHandler();
			outgoingStreamHandler.start();
		}
		catch (Exception e) 
		{
			System.out.println("Error opening socket. Is Tracker Server running?");
			close();
			return false;
		}

		return true;
	}	

	public void close()
	{
		try 
		{
			if(null != incommingStreamHandler)
				incommingStreamHandler.stop();

			if(null != outgoingStreamHandler)
				outgoingStreamHandler.stop();

			if(null != socket)
				socket.close();

			if(null != requestQueue)
				requestQueue.clear();
		}
		catch (Exception e) 
		{
			System.out.println("Error closing socket");
		}
	}

	public boolean isConnected()
	{
		if(null != socket)
			return socket.isConnected();

		return false;
	}

	protected void request(String request)
	{
		requestQueue.add(request);
	}

	private class IncommingStreamHandler implements Runnable
	{
		private final ExecutorService executor = Executors.newSingleThreadExecutor();

		private void start()
		{
			executor.submit(this);
		}

		private void stop()
		{
			executor.shutdownNow();
		}

		@Override
		public void run() 
		{
			try 
			{
				String response;
				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader reader = new BufferedReader(isr);

				while (!(Thread.currentThread().isInterrupted()))
				{
					response = reader.readLine();

					if(null != responseListener)
						responseListener.onGazeApiResponse(response);
				}
			}
			catch (IOException ioe) 
			{
				//closed down
			}
			catch (Exception e) 
			{
				System.out.println("Exception while running IncommingStreamHandler: "+e.getLocalizedMessage());
			}
		}
	}

	private class OutgoingStreamHandler implements Runnable
	{
		private final ExecutorService executor = Executors.newSingleThreadExecutor();

		private void start()
		{
			executor.submit(this);
		}

		private void stop()
		{
			executor.shutdownNow();
		}

		@Override
		public void run() 
		{
			try 
			{
				String request;
				OutputStream os = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os);
				BufferedWriter writer = new BufferedWriter(osw);

				while (!(Thread.currentThread().isInterrupted()))
				{
					request = requestQueue.take();

					writer.write(request);
					writer.newLine();
					writer.flush();
				}
			}
			catch (InterruptedException ie) 
			{
				//closed down
			}
			catch (Exception e) 
			{
				System.out.println("Exception while running OutgoingStreamHandler: "+e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Callback interface responsible for handling messages returned from the GazeApiManager
	 */
	public interface GazeApiResponseListener
	{
		public void onGazeApiResponse(String response);
	}
}

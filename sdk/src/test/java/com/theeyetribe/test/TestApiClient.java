package com.theeyetribe.test;

import com.google.gson.Gson;
import com.theeyetribe.clientsdk.*;
import com.theeyetribe.clientsdk.data.CalibrationResult;
import com.theeyetribe.clientsdk.data.CalibrationResult.CalibrationPoint;
import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.data.Point2D;
import com.theeyetribe.clientsdk.request.Request;
import com.theeyetribe.clientsdk.request.TrackerSetRequest;
import com.theeyetribe.clientsdk.response.Response;
import com.theeyetribe.clientsdk.utils.CalibUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Test cases related to TET Java Client
 */
public class TestApiClient {

    private CountDownLatch lock = new CountDownLatch(1);

    @Test
    public void testGazeData()
    {
        Gson gson = new Gson();

        GazeData gd = new GazeData();
        gd.leftEye.pupilSize = 11.1d;
        gd.rightEye.pupilCenterCoordinates.x = 800.13d;
        gd.state = 1;

        Assert.assertNotNull(gd.state);
        Assert.assertTrue(!gd.hasRawGazeCoordinates());

        String json = gson.toJson(gd);
        GazeData gd2 = gson.fromJson(json, GazeData.class);

        Assert.assertEquals(gd,gd2);
        Assert.assertEquals(gd.hashCode(),gd2.hashCode());
    }

    @Test
    public void testCalibrationResult()
    {
        Gson gson = new Gson();

        CalibrationResult cr = new CalibrationResult();
        cr.averageErrorDegree = 11.1d;
        cr.averageErrorDegreeRight = 800.13d;
        CalibrationPoint cp = cr.new CalibrationPoint();
        cp.standardDeviation.averageStandardDeviationPixelsLeft = 123.123d;
        cp.coordinates = new Point2D(321.123d, 432.234d);
        cr.calibpoints = new CalibrationPoint[]{cp};

        String json = gson.toJson(cr);
        CalibrationResult cr2 = gson.fromJson(json, CalibrationResult.class);

        Assert.assertEquals(cr,cr2);
        Assert.assertEquals(cr.hashCode(),cr2.hashCode());
    }

    @Test
    public void testRequestBase()
    {
        Gson gson = new Gson();

        Request request = new Request(Response.class);
        request.category = Protocol.CATEGORY_TRACKER;
        request.request = Protocol.TRACKER_REQUEST_SET;

        String json = request.toJsonString(gson);
        Request request2 = gson.fromJson(json, Request.class);

        Assert.assertEquals(json,gson.toJson(request2));
    }

    @Test
    public void testReplyBase()
    {
        Gson gson = new Gson();

        Response reply = new Response();
        reply.category = Protocol.CATEGORY_TRACKER;
        reply.statuscode = 200;

        String json = gson.toJson(reply);
        Response reply2 = gson.fromJson(json, Response.class);

        Assert.assertEquals(json,gson.toJson(reply2));
    }

    @Test
    public void testTrackerSetRequest()
    {
        Gson gson = new Gson();

        TrackerSetRequest request = new TrackerSetRequest();
        request.category = Protocol.CATEGORY_TRACKER;
        request.request = Protocol.TRACKER_REQUEST_SET;
        request.values.version = 1;

        String json = request.toJsonString(gson);
        TrackerSetRequest request2 = gson.fromJson(json, TrackerSetRequest.class);

        Assert.assertEquals(json, gson.toJson(request2));
    }

    @Test
    public void testActivate() throws Exception
    {
        Assert.assertTrue(GazeManager.getInstance().activate());
        Assert.assertTrue(GazeManager.getInstance().isActivated());

        deactivateServer();
    }

    @Test
    public void testActivateAsync() throws Exception
    {
        Future<Boolean> future = GazeManager.getInstance().activateAsync();

        Assert.assertNotNull(future);
        Assert.assertTrue(future.get(5, TimeUnit.SECONDS));

        deactivateServer();
    }

    @Test
    public void testActivateAsyncRetry() throws Exception
    {
        Future<Boolean> future = GazeManager.getInstance().activateAsync(10L * 1000, 3);

        Assert.assertNotNull(future);
        Assert.assertTrue(future.get(11, TimeUnit.SECONDS));

        deactivateServer();
    }

    @Test
    public void testSwitchScreen() throws Exception
    {
        activateServer();

        Assert.assertFalse(GazeManager.getInstance().switchScreen(5, 1980, 1200, .4f, .3f));

        deactivateServer();
    }

    @Test
    public void testSwitchScreenAsync() throws Exception
    {
        activateServer();

        Future<Boolean> future = GazeManager.getInstance().switchScreenAsync(5, 1980, 1200, .4f, .3f);
        Assert.assertNotNull(future);
        Assert.assertFalse(future.get(5, TimeUnit.SECONDS));

        deactivateServer();
    }



    @Test
    public void testCalibrationStart() throws Exception
    {
        activateServer();

        GazeManager.getInstance().calibrationAbort();

        Assert.assertTrue(GazeManager.getInstance().calibrationStart(9, null));

        Assert.assertFalse(GazeManager.getInstance().calibrationStart(9, null));
        Assert.assertTrue(GazeManager.getInstance().calibrationAbort());

        Assert.assertTrue(GazeManager.getInstance().calibrationStart(9, null));
        Assert.assertTrue(GazeManager.getInstance().calibrationAbort());

        deactivateServer();
    }

    @Test
    public void testCalibrationStartAsync() throws Exception
    {
        activateServer();

        GazeManager.getInstance().calibrationAbort();

        Future<Boolean> future = GazeManager.getInstance().calibrationStartAsync(9, null);
        Assert.assertNotNull(future);
        Assert.assertTrue(future.get(5, TimeUnit.SECONDS));

        future = GazeManager.getInstance().calibrationStartAsync(9, null);
        Assert.assertNotNull(future);
        Assert.assertFalse(future.get(5, TimeUnit.SECONDS));

        Assert.assertTrue(GazeManager.getInstance().calibrationAbort());
        future = GazeManager.getInstance().calibrationStartAsync(9, null);
        Assert.assertNotNull(future);
        Assert.assertTrue(future.get(5, TimeUnit.SECONDS));

        Assert.assertTrue(GazeManager.getInstance().calibrationAbort());

        deactivateServer();
    }

    @Test
    public void testGazeDataStream() throws Exception
    {
        activateServer();

        final TestListener listener = new TestListener();

        Assert.assertFalse(GazeManager.getInstance().hasGazeListener(listener));
        GazeManager.getInstance().addGazeListener(listener);

        lock.await(2, TimeUnit.SECONDS);

        Assert.assertTrue(GazeManager.getInstance().hasGazeListener(listener));
        Assert.assertTrue(GazeManager.getInstance().getNumGazeListeners() == 1);

        Assert.assertTrue(listener.hasRecievedGazeData);

        deactivateServer();
    }

    @Test
    public void testGazeDataPull() throws Exception
    {
        Future<Boolean> future = GazeManager.getInstance().activateAsync(
                GazeManager.ApiVersion.VERSION_1_0,
                GazeManager.ClientMode.PULL
        );
        Assert.assertNotNull(future);
        Assert.assertTrue(future.get(5, TimeUnit.SECONDS));

        final TestListener listener = new TestListener();

        Assert.assertFalse(GazeManager.getInstance().hasGazeListener(listener));
        GazeManager.getInstance().addGazeListener(listener);

        GazeManager.getInstance().framePull();

        lock.await(2, TimeUnit.SECONDS);

        Assert.assertTrue(GazeManager.getInstance().hasGazeListener(listener));
        Assert.assertTrue(GazeManager.getInstance().getNumGazeListeners() == 1);

        Assert.assertTrue(listener.hasRecievedGazeData);

        deactivateServer();
    }

    @Test
    public void testRapidActivation() throws Exception
    {
        for (int i = 20; --i >= 0; )
        {
            GazeManager.getInstance().activateAsync();
            GazeManager.getInstance().deactivate();
            Assert.assertFalse(GazeManager.getInstance().isActivated());
        }

        Assert.assertFalse(GazeManager.getInstance().isActivated());

        deactivateServer();
    }

    @Test
    public void testAsyncLocks() throws Exception
    {
        for (int i = 20; --i >= 0; )
        {
            Assert.assertTrue(GazeManager.getInstance().activate());
            Assert.assertTrue(GazeManager.getInstance().isActivated());

            lock.await(1, TimeUnit.SECONDS);

            GazeManager.getInstance().calibrationStartAsync(9, null).get(5, TimeUnit.SECONDS);
            GazeManager.getInstance().calibrationAbortAsync().get(5, TimeUnit.SECONDS);

            lock.await(1, TimeUnit.SECONDS);

            GazeManager.getInstance().deactivate();
            lock.await(1, TimeUnit.SECONDS);
            Assert.assertFalse(GazeManager.getInstance().isActivated());
        }

        Assert.assertFalse(GazeManager.getInstance().isActivated());

        deactivateServer();
    }
    
    @Test
    public void testCalibrationProcessHandler() throws Exception
    {
        activateServer();

        //run test
        final Object obj = new Object();
        final CalibrationProcessHandler handler = new CalibrationProcessHandler();
        GazeManager.getInstance().calibrationAbort();

        List<Point2D> calibPoints = CalibUtils.initCalibrationPoints(3, 3, 1980, 1200, 30, 30, true);

        Assert.assertTrue(GazeManager.getInstance().calibrationStart(9, handler));

        lock.await(2, TimeUnit.SECONDS);

        for(Point2D p : calibPoints)
        {
            GazeManager.getInstance().calibrationPointStart((int)p.x, (int)p.y);

            lock.await(1, TimeUnit.SECONDS);

            GazeManager.getInstance().calibrationPointEnd();

            lock.await(1, TimeUnit.SECONDS);
        }

        lock.await(2, TimeUnit.SECONDS);

        Assert.assertTrue(handler.startWasCalled);
        Assert.assertTrue(handler.progressWasCalled);
        Assert.assertTrue(handler.processingWasCalled);
        Assert.assertTrue(handler.resultWasCalled);
        Assert.assertNotNull(handler.result);

        deactivateServer();
    }

    @Test
    public void testListenerRegistrationRaceCondition() throws Exception
    {
        activateServer();

        //run test
        final Object obj = new Object();

        Thread t = new Thread(() -> {
            try
            {
                ThreadTestListener tl1 = new ThreadTestListener(10 * 1000);
                Thread t1 = new Thread(tl1);
                t1.setName("ThreadTest1");
                t1.start();

                ThreadTestListener tl2 = new ThreadTestListener(10 * 1000);
                Thread t2 = new Thread(tl2);
                t2.setName("ThreadTest2");
                t2.start();

                ThreadTestListener tl3 = new ThreadTestListener(10 * 1000);
                Thread t3 = new Thread(tl3);
                t3.setName("ThreadTest3");
                t3.start();

                ThreadTestListener tl4 = new ThreadTestListener(10 * 1000);
                Thread t4 = new Thread(tl4);
                t4.setName("ThreadTest4");
                t4.start();

                try
                {
                    Thread.sleep(12* 1000);
                }
                catch (InterruptedException e)
                {
                    // consume
                }

                deactivateServer();
            }
            catch(Throwable th)
            {
                //consume
            }

            synchronized (obj)
            {
                obj.notify();
            }
        });
        t.start();

        synchronized (obj)
        {
            try
            {
                obj.wait();
            }
            catch (InterruptedException e)
            {
                // consume
            }
        }

        deactivateServer();

        Assert.assertFalse(GazeManager.getInstance().isActivated());
    }

    class CalibrationProcessHandler implements ICalibrationProcessHandler
    {
        public boolean startWasCalled;
        public boolean progressWasCalled;
        public boolean processingWasCalled;
        public boolean resultWasCalled;

        public CalibrationResult result;

        @Override
        public void onCalibrationStarted()
        {
            if(GazeManager.IS_DEBUG_MODE)
                System.out.println("onCalibrationStarted");

            startWasCalled = true;
        }

        @Override
        public void onCalibrationResult(CalibrationResult calibResult)
        {
            if(GazeManager.IS_DEBUG_MODE)
                System.out.println("onCalibrationResult: " + calibResult.result);

            resultWasCalled = true;
            result = calibResult;
        }

        @Override
        public void onCalibrationProgress(double progress)
        {
            if(GazeManager.IS_DEBUG_MODE)
                System.out.println("onCalibrationProgress: " + progress);

            progressWasCalled = true;
        }

        @Override
        public void onCalibrationProcessing()
        {
            if(GazeManager.IS_DEBUG_MODE)
                System.out.println("onCalibrationProcessing");

            processingWasCalled = true;
        }
    }

    class ThreadTestListener extends TestListener implements Runnable
    {
        private boolean isRunning = true;
        private Random random = new Random();
        int time;
        long timestamp = -1;

        boolean ticker;

        public ThreadTestListener(int time)
        {
            this.time = time;
        }

        @Override
        public void run() {
            while (isRunning)
            {
                try
                {
                    if (timestamp < 0)
                        timestamp = System.currentTimeMillis();

                    long now = System.currentTimeMillis();
                    if (now - timestamp > time)
                    {
                        isRunning = false;
                    }

                    if (!ticker)
                    {
                        GazeManager.getInstance().addGazeListener(this);

                        if(GazeManager.IS_DEBUG_MODE)
                            System.out.println("AddGazeListener: " + GazeManager.getInstance().getNumGazeListeners());

                        GazeManager.getInstance().addConnectionStateListener(this);

                        GazeManager.getInstance().addCalibrationResultListener(this);

                        GazeManager.getInstance().addTrackerStateListener(this);

                        Assert.assertTrue(GazeManager.getInstance().hasGazeListener(this));
                        Assert.assertTrue(GazeManager.getInstance().hasConnectionStateListener(this));
                        Assert.assertTrue(GazeManager.getInstance().hasCalibrationResultListener(this));
                        Assert.assertTrue(GazeManager.getInstance().hasTrackerStateListener(this));
                    }
                    else
                    {
                        GazeManager.getInstance().removeGazeListener(this);

                        if(GazeManager.IS_DEBUG_MODE)
                            System.out.println("RemoveGazeListener: " + GazeManager.getInstance().getNumGazeListeners());

                        GazeManager.getInstance().removeConnectionStateListener(this);

                        GazeManager.getInstance().removeCalibrationResultListener(this);

                        GazeManager.getInstance().removeTrackerStateListener(this);

                        Assert.assertTrue(!GazeManager.getInstance().hasGazeListener(this));
                        Assert.assertTrue(!GazeManager.getInstance().hasConnectionStateListener(this));
                        Assert.assertTrue(!GazeManager.getInstance().hasCalibrationResultListener(this));
                        Assert.assertTrue(!GazeManager.getInstance().hasTrackerStateListener(this));
                    }

                    ticker = !ticker;

                    Thread.sleep(10 + Math.abs(random.nextInt()%250));
                }
                catch (Exception e)
                {
                    System.out.println("e: " + e.getLocalizedMessage());
                }
            }
        }
    }

    class TestListener implements IGazeListener, ICalibrationResultListener, ITrackerStateListener, IConnectionStateListener, IScreenStateListener
    {
        boolean hasRecievedConnecitonStateChange;
        boolean hasRecievedTrackerStateChange;
        boolean hasRecievedScreenStateChange;
        boolean hasRecievedCalibrationStateChange;
        boolean hasRecievedGazeData;

        @Override
        public void onConnectionStateChanged(boolean isConnected) {

            if(!hasRecievedConnecitonStateChange)
                hasRecievedConnecitonStateChange = true;

            //System.out.println("Thread: " + Thread.currentThread().getName() + ", onConnectionStateChanged: " + isConnected);
        }

        @Override
        public void onTrackerStateChanged(int trackerState) {

            if(!hasRecievedTrackerStateChange)
                hasRecievedTrackerStateChange = true;

            //System.out.println("Thread: " + Thread.currentThread().getName() + ", onTrackerStateChanged: " + trackerState);
        }

        @Override
        public void onScreenStatesChanged(int screenIndex,
                                          int screenResolutionWidth, int screenResolutionHeight,
                                          float screenPhysicalWidth, float screenPhysicalHeight) {

            if(!hasRecievedScreenStateChange)
                hasRecievedScreenStateChange = true;

            //System.out.println("Thread: " + Thread.currentThread().getName() + ", OnScreenStatesChanged: " + screenIndex + ", " + screenResolutionWidth + ", " + screenResolutionHeight + ", " + screenPhysicalWidth + ", " + screenPhysicalHeight);
        }

        @Override
        public void onCalibrationChanged(boolean isCalibrated,
                                         CalibrationResult calibResult) {

            if(!hasRecievedCalibrationStateChange)
                hasRecievedCalibrationStateChange = true;

            //System.out.println("Thread: " + Thread.currentThread().getName() + ", onCalibrationChanged: " + isCalibrated + ", " + calibResult.toString());
        }

        @Override
        public void onGazeUpdate(GazeData gazeData) {

            if(!hasRecievedGazeData)
                hasRecievedGazeData = true;

            //System.out.println("Thread: " + Thread.currentThread().getName() + ", onGazeUpdate: " + gazeData.toString());
        }
    }

    private void activateServer() throws Exception
    {
        Future<Boolean> future = GazeManager.getInstance().activateAsync();
        Assert.assertNotNull(future);
        Assert.assertTrue(future.get(5, TimeUnit.SECONDS));
    }

    private void deactivateServer() throws Exception
    {
        GazeManager.getInstance().deactivate();
        lock.await(1, TimeUnit.SECONDS);
        Assert.assertFalse(GazeManager.getInstance().isActivated());
    }
}

/*
 * Copyright (c) 2013-present, The Eye Tribe.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree.
 *
 */
package com.theeyetribe.clientsdk;

import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.request.Request;
import com.theeyetribe.clientsdk.response.Response;

/**
 * GazeManager is the main entry point of the EyeTribe Java SDK. It exposes all routines associated to gaze control.
 * <p>
 * Using this class a developer can connect to the EyeTribe Server, <i>calibrate</i> the eye tracking system and attach
 * listeners to receive live data streams of {@link GazeData} updates. Note that this is a thin wrapper class. The Core
 * SDK implementation can be found in {@link GazeManagerCore}.
 * <p>
 * GazeManager must establish a connection to the EyeTribe Server before it can be used. This is achieved by calling
 * {@link #activate() activate}. GazeManager must be shut down by calling {@link #deactivate() deactivate}.
 * <p>
 * A standard pattern for using GazeManager in a Java FX Application can be seen below:
 * <pre>
 * public class Main extends Application{
 *
 *     public static void main(String[] args)
 *     {
 *         launch(args);
 *     }
 *
 *     \@Override
 *     public void start(Stage primaryStage) throws Exception
 *     {
 *         GazeManager.getInstance().activateAsync();
 *     }
 *
 *     \@Override
 *     public void stop() throws Exception
 *     {
 *         GazeManager.getInstance().deactivate();
 *     }
 * }
 * </pre>
 */
public class GazeManager extends GazeManagerCore
{
    private GazeManager()
    {
        super();
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

    protected GazeApiManager createApiManager(GazeApiManager.IGazeApiResponseListener responseListener, GazeApiManager.IGazeApiConnectionListener connectionListener)
    {
        return new GazeApiManager(responseListener, connectionListener);
    }

    protected boolean parseApiResponse(final Response response, final Request request)
    {
        return false;
    }
}

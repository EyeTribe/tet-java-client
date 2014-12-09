/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.client;

import com.theeyetribe.client.data.GazeData;

/**
 * Callback interface with methods associated to Gaze Tracking. This interface should be implemented by classes that are
 * to receive live GazeData stream.
 * <p>
 * Implementing classes should register for updates through
 * {@link com.theeyetribe.client.GazeManager#addGazeListener(IGazeListener)}.
 */
public interface IGazeListener
{
    /**
     * A notification call back indicating that a new GazeData frame is available. Implementing classes should update
     * themselves accordingly if needed. Register for updates through GazeManager.AddGazeListener().
     * 
     * @param gazeData
     */
    public void onGazeUpdate(GazeData gazeData);
}

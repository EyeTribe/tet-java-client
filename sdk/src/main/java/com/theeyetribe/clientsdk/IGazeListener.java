/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk;

import com.theeyetribe.clientsdk.data.GazeData;

/**
 * Callback interface with methods associated to gaze estimation updates.
 * <p>
 * This interface should be implemented by classes that are to receive live GazeData stream. This could be a class
 * in the 'View' layer that wish to implement interaction using the gaze coordinates of the user.
 * <p>
 * Implementing classes should register for updates through
 * {@link GazeManager#addGazeListener(IGazeListener)}.
 */
public interface IGazeListener
{
    /**
     * A notification call back indicating that a new GazeData frame is available. Implementing classes should update
     * themselves accordingly if needed. Register for updates through {@link GazeManager#addGazeListener(IGazeListener)}.
     * 
     * @param gazeData gaze data frame
     */
    void onGazeUpdate(GazeData gazeData);
}

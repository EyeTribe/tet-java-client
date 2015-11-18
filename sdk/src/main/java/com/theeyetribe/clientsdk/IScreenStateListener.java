/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk;

/**
 * Callback interface with methods associated to the currently active calibration screen in a multi screen setup.
 * <p>
 * This interface should be implemented by classes that are to receive notifications that the main calibration screen
 * has changed and handle these accordingly. This could be a class in the 'View' layer telling the user that the
 * calibration screen has changed.
 * <p>
 * Implementing classes should register for updates through
 * {@link GazeManager#addScreenStateListener(IScreenStateListener)}.
 */
public interface IScreenStateListener
{
    /**
     * A notification call back indicating that main screen index has changed. This is only relevant for multi-screen
     * setups. Implementing classes should update themselves accordingly if needed. Register for updates through
     * {@link GazeManager#addScreenStateListener(IScreenStateListener)}.
     * 
     * @param screenIndex the currently valid screen index
     * @param screenResolutionWidth screen resolution width in pixels
     * @param screenResolutionHeight screen resolution height in pixels
     * @param screenPhysicalWidth physical screen width in meters
     * @param screenPhysicalHeight physical screen height in meters
     */
    void onScreenStatesChanged(int screenIndex, int screenResolutionWidth, int screenResolutionHeight,
                               float screenPhysicalWidth, float screenPhysicalHeight);
}

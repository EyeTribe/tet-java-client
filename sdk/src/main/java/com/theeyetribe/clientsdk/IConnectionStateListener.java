/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk;

/**
 * Callback interface with methods associated to the current state of the connection to the EyeTribe Server.
 * <p>
 * This interface should be implemented by classes that are to receive notifications of changes in the connection
 * state and handle these accordingly. This could be a class in the 'View' layer telling the user that the connection
 * to the EyeTribe Server has been lost.
 * <p>
 * Implementing classes should register for updates through
 * {@link GazeManager#addConnectionStateListener(IConnectionStateListener)}.
 */
public interface IConnectionStateListener
{
    /**
     * A notification call back indicating that the connection state has changed. Use this to detect if connection was
     * lost. Implementing classes should update themselves accordingly if needed. Register for updates through
     * {@link GazeManager#addConnectionStateListener(IConnectionStateListener)}.
     * 
     * @param isConnected the current connection state
     */
    void onConnectionStateChanged(boolean isConnected);
}

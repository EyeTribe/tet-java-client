/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk;

import com.theeyetribe.clientsdk.data.CalibrationResult;

/**
 * Callback interface with methods associated to the changes to calibration state.
 * <p>
 * This interface should be implemented by classes that are to receive only changes to calibration state and who are
 * _not_ to perform the calibration process itself.
 * <p>
 * Implementing classes should register for updates through
 * {@link GazeManager#addCalibrationResultListener(ICalibrationResultListener)}.
 */
public interface ICalibrationResultListener
{
    /**
     * A notification call back indicating that state of calibration has changed. Implementing classes should update
     * themselves accordingly if needed.
     * 
     * @param isCalibrated is the EyeTribe Server calibrated?
     * @param calibResult if calibrated, the currently valid CalibrationResult, otherwise null
     */
    void onCalibrationChanged(boolean isCalibrated, CalibrationResult calibResult);
}

package com.theeyetribe.client;

import com.theeyetribe.client.data.CalibrationResult;

/**
 * Callback interface with methods associated to the changes of CalibrationResult.
 * This interface should be implemented by classes that are to receive only changes
 * in CalibrationResultand who are _not_ to perform the calibration process itself.
 */
public interface ICalibrationResultListener 
{
	/**
	 * A notification call back indicating that state of calibration has changed. 
	 * Implementing classes should update themselves accordingly if needed.
	 * Register for updates through GazeManager.AddCalibrationStateListener().
	 * 
	 * @param isCalibrated is the Tracker Server calibrated?
	 * @param calibResult if calibrated, the currently valid CalibrationResult, otherwise null
	 */
	public void onCalibrationChanged(boolean isCalibrated, CalibrationResult calibResult);
}

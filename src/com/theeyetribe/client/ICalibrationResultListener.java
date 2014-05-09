package com.theeyetribe.client;

import com.theeyetribe.client.data.CalibrationResult;

/**
 * Callback interface with methods associated to the changes of CalibrationResult.
 * This interface should be implemented by classes that are to receive only changes
 * in CalibrationResult and who are _not_ to perform the calibration process itself.
 * <p>
 * Implementing classes should register for updates through 
 * {@link com.theeyetribe.client.GazeManager#addCalibrationResultListener(ICalibrationResultListener)}.
 */
public interface ICalibrationResultListener 
{
	/**
	 * A notification call back indicating that state of calibration has changed. 
	 * Implementing classes should update themselves accordingly if needed.
	 * 
	 * @param isCalibrated is the EyeTribe Server calibrated?
	 * @param calibResult if calibrated, the currently valid CalibrationResult, otherwise null
	 */
	public void onCalibrationChanged(boolean isCalibrated, CalibrationResult calibResult);
}

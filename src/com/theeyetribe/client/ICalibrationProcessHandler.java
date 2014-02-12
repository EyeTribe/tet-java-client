package com.theeyetribe.client;

import com.theeyetribe.client.data.CalibrationResult;

/**
 * Callback interface with methods associated to Calibration process.
 */
public interface ICalibrationProcessHandler 
{
	/**
	 * Called when a calibration process has been started. 
	 */
	public void onCalibrationStarted();

	/**
	 * Called every time tracking of a single calibratioon points has completed.
	 * 
	 * @param progress 'normalized' progress [0..1.0d] 
	 */
	public void onCalibrationProgress(double progress);

	/**
	 * Called when all calibration points have been collected and calibration processing begins.
	 */
	public void onCalibrationProcessing();

	/**
	 * Called when processing of calibration points and calibration as a whole has completed.
	 * 
	 * @param calibResult the result of the calibration process
	 */
	public void onCalibrationResult(final CalibrationResult calibResult);
}

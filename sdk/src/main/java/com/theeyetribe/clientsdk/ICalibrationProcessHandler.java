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
 * Callback interface with methods associated to Calibration process.
 * <p>
 * This interface should be implemented by classes that are to perform the calibration process and that are
 * responsible for validating the outcome.
 * <p>
 * Implementing classes should initiate a calibration process through
 * {@link GazeManager#calibrationStart(int, ICalibrationProcessHandler)}.
 *
 * <p>
 * A standard pattern for using ICalibrationProcessHandler in a Java FX Application can be seen below:
 * <pre>
 * public class SceneCalibrationController implements ICalibrationProcessHandler
 * {
 *     \@Override
 *     public void onCalibrationStarted()
 *     {
 *         //calibration has started, initiate process of showing calibration points in UI
 *     }
 *
 *     \@Override
 *     public void onCalibrationProgress(double progress)
 *     {
 *         //calibration progress has updated, update UI progress indicators if any
 *     }
 *
 *     \@Override
 *     public void onCalibrationProcessing()
 *     {
 *         //processing of calibration result has started, update UI and await result
 *     }
 *
 *     \@Override
 *     public void onCalibrationResult(CalibrationResult calibResult)
 *     {
 *         Platform.runLater(new Runnable() {
 *             \@Override
 *             public void run()
 *             {
 *                 if(calibResult.result)
 *                 {
 *                     //Calibration was a success, update calibration state in UI
 *                 }
 *                 else
 *                 {
 *                     //resampling needed, evaluate results
 *                     for (CalibrationResult.CalibrationPoint calibPoint : calibResult.calibpoints)
 *                     {
 *                         if (calibPoint.state == CalibrationResult.CalibrationPoint.STATE_RESAMPLE ||
 *                                 calibPoint.state == CalibrationResult.CalibrationPoint.STATE_NO_DATA)
 *                         {
 *                             //resampling is needed, gather points for resampling
 *                         }
 *                     }
 *
 *                     //if resampling points found, init the re-calibration of these in UI
 *                 }
 *             }
 *         });
 *     }
 * }
 * </pre>
 */
public interface ICalibrationProcessHandler
{
    /**
     * Called when a calibration process has been started.
     */
    void onCalibrationStarted();

    /**
     * Called every time tracking of a single calibration points has completed.
     * 
     * @param progress 'normalized' progress [0..1.0d]
     */
    void onCalibrationProgress(double progress);

    /**
     * Called when all calibration points have been collected and calibration processing begins.
     */
    void onCalibrationProcessing();

    /**
     * Called when processing of calibration points and calibration as a whole has completed.
     * 
     * @param calibResult the result of the calibration process
     */
    void onCalibrationResult(final CalibrationResult calibResult);
}

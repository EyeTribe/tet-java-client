/*
 * Copyright (c) 2013-present, The Eye Tribe.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree.
 *
 */

package com.theeyetribe.javafx.scenes;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.ICalibrationProcessHandler;
import com.theeyetribe.clientsdk.data.CalibrationResult;
import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.data.Point2D;
import com.theeyetribe.javafx.utils.JavaFxCalibUtils;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Scene controller associated to the calibration scene
 */
public class SceneCalibrationController extends SceneController implements ICalibrationProcessHandler
{
    protected final static int NUM_CALIB_COLUMNS = 3, NUM_CALIB_ROWS = 3;

    private final int NUM_MAX_CALIBRATION_ATTEMPTS = 3;
    private final int NUM_MAX_RESAMPLE_POINTS = 4;

    private final static int ANIM_TXT_FADEINOUT_DELAY_MILLIS = 500;
    private final static int ANIM_TXT_SHOW_DELAY_MILLIS = 1000;

    private final static int ANIM_DOT_START_DELAY_MILLIS = ANIM_TXT_SHOW_DELAY_MILLIS + (2 * ANIM_TXT_FADEINOUT_DELAY_MILLIS);
    private final static int ANIM_DOT_FADEIN_DELAY_MILLIS = 500;
    private final static int ANIM_DOT_BEFORE_SCALE_DELAY_MILLIS = 200;
    private final static int ANIM_DOT_BETWEEN_POINTS_DELAY_MILLIS = 100;
    private final static int ANIM_DOT_PHASE_TIME_MILLIS = 800;

    @FXML
    private ProgressIndicator progress;

    @FXML
    private Group calibPoint;

    @FXML
    private Circle circle;

    @FXML
    private Label instruct;

    private List<Point2D> mPointsResampled = new ArrayList<Point2D>();
    private List<Point2D> mAnchors;
    private int mResampleCount;

    protected double mCalibPadding;

    private Timeline mCalibrationSequence;
    private boolean mIsRecievingFrames;

    @Override
    public void onStart()
    {
        super.onStart();

        mIsRecievingFrames = false;
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if(GazeManager.getInstance().isCalibrating())
            GazeManager.getInstance().calibrationAbort();
    }

    @Override
    public void onGazeUpdate(GazeData gazeData)
    {
        Platform.runLater(() -> {

            if (!mIsRecievingFrames)
            {
                if(null != progress)
                {
                    progress.setVisible(false);

                    mCalibPadding = innerRoot.getWidth() * .075f;

                    resetCalibration();

                    mIsRecievingFrames = true;
                }
            }
        });
    }

    @Override
    public void onCalibrationStarted() {
        System.out.println("onCalibrationStarted");
    }

    @Override
    public void onCalibrationProgress(double progress) {
        System.out.println("onCalibrationProgressUpdate: " + String.format("%.2f", progress));
    }

    @Override
    public void onCalibrationProcessing() {
        System.out.println("onCalibrationProcessing");
    }

    @Override
    public void onCalibrationResult(CalibrationResult calibResult) {
        System.out.println("onCalibrationResult");

        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                if(calibResult.result)
                {
                    //Success
                    try {
                        main.loadEvaluationScene();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    //resampling needed
                    final ArrayList<Point2D> resamplePoints = new ArrayList<Point2D>();

                    //Evaluate results
                    for (CalibrationResult.CalibrationPoint calibPoint : calibResult.calibpoints)
                    {
                        if (calibPoint.state == CalibrationResult.CalibrationPoint.STATE_RESAMPLE || calibPoint.state == CalibrationResult.CalibrationPoint.STATE_NO_DATA)
                        {
                            resamplePoints.add(new Point2D(Math.round(calibPoint.coordinates.x), Math.round(calibPoint.coordinates.y)));
                        }
                    }

                    //Should we abort?
                    if (mResampleCount++ >= NUM_MAX_CALIBRATION_ATTEMPTS || resamplePoints.size() == 0|| resamplePoints.size() >= NUM_MAX_RESAMPLE_POINTS)
                    {
                        stopCalibration();

                        GazeManager.getInstance().calibrationAbort();

                        //TODO: SHow failure in UI?

                        mResampleCount = 0;

                        //We return to main screen if calibration fails
                        try {
                            main.loadMainScene();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return;
                    }

                    resampleCalibration(resamplePoints);
                }
            }
        });
    }

    @Override
    public void onConnectionStateChanged(boolean b) {

    }

    @Override
    public void onScreenStatesChanged(int i, int i1, int i2, float v, float v1) {

    }

    @Override
    public void onTrackerStateChanged(int i) {

    }

    @Override
    public void onCalibrationChanged(boolean b, CalibrationResult calibrationResult)
    {

    }

    public void stopCalibration()
    {
        if( null != mCalibrationSequence)
            mCalibrationSequence.stop();

        if(GazeManager.getInstance().isCalibrating())
            GazeManager.getInstance().calibrationAbort();
    }

    public void resampleCalibration(List<Point2D> anchors)
    {
        if(null != anchors)
        {
            this.mAnchors = anchors;
            for (Point2D anchor : anchors)
            {
                if (hasPointBeenResampled(anchor) == false)
                {
                    mPointsResampled.add(anchor);
                }
                else
                {
                    Point2D adjustedPoint = adjustPointForResampling(anchor);
                    mPointsResampled.add(adjustedPoint);
                    anchor.x = adjustedPoint.x;
                    anchor.y = adjustedPoint.y;
                }
            }

            mCalibrationSequence.stop();
            mCalibrationSequence = initAnimations(instruct, calibPoint, anchors, true);
            mCalibrationSequence.play();
        }
    }
    public void resetCalibration()
    {
        stopCalibration();

        mAnchors = JavaFxCalibUtils.initCalibrationPoints(NUM_CALIB_ROWS, NUM_CALIB_COLUMNS, innerRoot.getWidth(), innerRoot.getHeight(), mCalibPadding, mCalibPadding, true);

        mCalibrationSequence = initAnimations(instruct, calibPoint, mAnchors, false);

        GazeManager.getInstance().calibrationStart(mAnchors.size(), this);

        mCalibrationSequence.play();
    }

    private boolean hasPointBeenResampled(Point2D calPoint)
    {
        if (mPointsResampled == null)
        {
            mPointsResampled = new ArrayList<Point2D>();
            return false;
        }

        for(Point2D ct : mPointsResampled)
        {
            if(ct.equals(calPoint))
                return true;
        }

        return false;
    }

    private Timeline initAnimations(final Node instruct, final Node calibPoint, List<Point2D> anchors, boolean isResample)
    {
        Timeline timeLine = new Timeline();

        Iterator<Point2D> i = anchors.iterator();

        long timeStamp = 0;

        if(!isResample)
        {
            timeStamp += ANIM_TXT_FADEINOUT_DELAY_MILLIS;
            timeLine.getKeyFrames().add(new KeyFrame(
                    new Duration(timeStamp),
                    new KeyValue(instruct.opacityProperty(), 1)
            ));
            timeStamp += ANIM_TXT_SHOW_DELAY_MILLIS;
            timeLine.getKeyFrames().add(new KeyFrame(
                    new Duration(timeStamp)
            ));
            timeStamp += ANIM_TXT_FADEINOUT_DELAY_MILLIS;
            timeLine.getKeyFrames().add(new KeyFrame(
                    new Duration(timeStamp),
                    new KeyValue(instruct.opacityProperty(), 0)
            ));
            timeStamp += ANIM_DOT_START_DELAY_MILLIS;
            timeLine.getKeyFrames().add(new KeyFrame(
                    new Duration(timeStamp)
            ));
        }

        while(i.hasNext()) {

            final Point2D next = i.next();

            //translation
            timeStamp += 1;
            timeLine.getKeyFrames().add(new KeyFrame(
                    new Duration(timeStamp),
                    event -> {
                        calibPoint.setTranslateX(next.x);
                        calibPoint.setTranslateY(next.y);
                    },
                    new KeyValue(calibPoint.opacityProperty(), 0f)
            ));

            //fade in
            timeStamp += ANIM_DOT_FADEIN_DELAY_MILLIS;
            timeLine.getKeyFrames().add(new KeyFrame(
                    new Duration(timeStamp),
                    event -> {

                        javafx.geometry.Point2D calibPointGlobal = calibPoint.localToScreen(0d, 0d);

                        GazeManager.getInstance().calibrationPointStart(
                                (int) Math.round(calibPointGlobal.getX()),
                                (int) Math.round(calibPointGlobal.getY())
                        );
                    },
                    new KeyValue(calibPoint.opacityProperty(), 1f)
            ));

            //scale delay
            timeStamp += ANIM_DOT_BEFORE_SCALE_DELAY_MILLIS;
            timeLine.getKeyFrames().add(new KeyFrame(
                    new Duration(timeStamp)
            ));

            //scale up
            timeStamp += ANIM_DOT_PHASE_TIME_MILLIS >> 1;
            timeLine.getKeyFrames().add(new KeyFrame(
                    new Duration(timeStamp),
                    new KeyValue(calibPoint.scaleXProperty(), 1.4f),
                    new KeyValue(calibPoint.scaleYProperty(), 1.4f),
                    new KeyValue(calibPoint.opacityProperty(), 1f)
            ));

            //scale down
            timeStamp += ANIM_DOT_PHASE_TIME_MILLIS >> 1;
            timeLine.getKeyFrames().add(new KeyFrame(
                    new Duration(timeStamp),
                    event -> {
                        GazeManager.getInstance().calibrationPointEnd();
                    },
                    new KeyValue(calibPoint.scaleXProperty(), 1f),
                    new KeyValue(calibPoint.scaleYProperty(), 1f),
                    new KeyValue(calibPoint.opacityProperty(), 1f)
            ));

            //hide
            timeStamp += 1;
            timeLine.getKeyFrames().add(new KeyFrame(
                    new Duration(timeStamp),
                    new KeyValue(calibPoint.opacityProperty(), 0f)
            ));

            timeStamp += ANIM_DOT_BETWEEN_POINTS_DELAY_MILLIS;
            timeLine.getKeyFrames().add(new KeyFrame(
                    new Duration(timeStamp)
            ));
        }

        return timeLine;
    }

    /**
     * Helper methods that move calibration points towards center before resampling.
     * Increases success rate.
     *
     * @param calPoint
     * @return
     */
    private Point2D adjustPointForResampling(Point2D calPoint)
    {
        Point2D p = new Point2D(calPoint.x, calPoint.y);
        int factor = 7;
        double adjustX = innerRoot.getWidth() / factor;
        double adjustY = innerRoot.getHeight() / factor;

        //TODO: Fix with generic approach, Vector based

        // Determine how to move the point, if point occurs on
        //   Top edge    -> Move down by areaHeight/factor
        //   Bottom edge -> Move up by areaHeight/factor
        //   Left edge   -> Move right by areaWidth/factor
        //   Right edge  -> Move left by areaWidth/factor

        if (p.x == (mCalibPadding))
            p.x += adjustX;
        else if (p.x == innerRoot.getWidth() - (mCalibPadding))
            p.x -= adjustX;

        if (p.y == (mCalibPadding))
            p.y += adjustY;
        else if (p.y == innerRoot.getHeight() - (mCalibPadding))
            p.y -= adjustY;

        return p;
    }
}

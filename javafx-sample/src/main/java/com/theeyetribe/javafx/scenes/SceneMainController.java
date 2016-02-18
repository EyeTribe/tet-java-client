/*
 * Copyright (c) 2013-present, The Eye Tribe.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree.
 *
 */

package com.theeyetribe.javafx.scenes;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.data.CalibrationResult;
import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.data.Point2D;
import com.theeyetribe.clientsdk.utils.GazeUtils;
import com.theeyetribe.javafx.utils.FrameRateGazeDataDeque;
import com.theeyetribe.javafx.utils.GazeFrameCache;
import com.theeyetribe.javafx.utils.JavaFxCalibUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.controlsfx.control.Rating;

/**
 * Scene controller associated to the main scene
 */
public class SceneMainController extends SceneController
{
    private final static long FRAME_CACHE_SIZE = 5000; //5 sec

    @FXML
    private ImageView eyeLeft;

    @FXML
    private ImageView eyeRight;

    @FXML
    private Button calibrationBtn;

    @FXML
    public Button evaluateBtn;

    @FXML
    public Button connectBtn;

    @FXML
    private Label connect;

    @FXML
    private Label fpsLabel;

    @FXML
    private Rating rating;

    private FrameRateGazeDataDeque mFpsCache;
    private int fpsUpdateCycle = 10;
    private int fpsUpdateCount = 0;

    private boolean mIsRecievingFrames;

    public SceneMainController()
    {
        super();
    }

    @FXML
    public void initialize()
    {
        super.initialize();

        mFpsCache = new FrameRateGazeDataDeque(FRAME_CACHE_SIZE);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        mIsRecievingFrames = false;
    }

    @Override
    public void onGazeUpdate(GazeData gazeData)
    {
        Platform.runLater(() -> {

            if (!mIsRecievingFrames)
            {
                progress.setVisible(false);

                updateState();

                mIsRecievingFrames = true;
            }

            double angle = GazeFrameCache.getInstance().getLastEyesAngle();
            double scale = null != GazeFrameCache.getInstance().getLastUserPosition() ? GazeFrameCache.getInstance().getLastUserPosition().z : 1d;
            scale *= 1.5d;

            if (null != GazeFrameCache.getInstance().getLastLeftEye())
            {
                updateEye(
                        root,
                        eyeLeft,
                        GazeFrameCache.getInstance().getLastLeftEye(),
                        angle,
                        scale
                );

                eyeLeft.setVisible(true);
            } else
                eyeLeft.setVisible(false);

            if (null != GazeFrameCache.getInstance().getLastRightEye())
            {
                updateEye(
                        root,
                        eyeRight,
                        GazeFrameCache.getInstance().getLastRightEye(),
                        angle,
                        scale
                );

                eyeRight.setVisible(true);

            } else
                eyeRight.setVisible(false);

            Point2D gaze = GazeFrameCache.getInstance().getLastSmoothedGazeCoordinates();
            if (null != gaze) {
                gazeIndicator.setVisible(true);
                javafx.geometry.Point2D rootAnchor = getGazeIndicatorAnchor(gaze);
                gazeIndicator.setX(rootAnchor.getX());
                gazeIndicator.setY(rootAnchor.getY());
            } else
                gazeIndicator.setVisible(false);

            //remove deque elements until condition fulfilled
            while (!mFpsCache.offerFirst(gazeData))
                mFpsCache.pollLast();

            if ((++fpsUpdateCount % fpsUpdateCycle) == 0) {
                fpsLabel.setText(bundle.getString("label.fps") + " " + String.format("%.2f", mFpsCache.getAvgFramesPerSecond()));
            }
        });
    }

    private void updateEye(Pane root, ImageView eyeImageView, GazeData.Eye eye, double angle, double scale)
    {
        Point2D p = GazeUtils.getRelativeToRect(
                eye.pupilCenterCoordinates,
                (int) Math.round(root.getScene().getWidth()),
                (int) Math.round(root.getScene().getHeight())
        );

        p.x -= eyeImageView.getFitWidth() * .5d;
        p.y -= eyeImageView.getFitHeight() * .5d;

        eyeImageView.setX(p.x);
        eyeImageView.setY(p.y);
        eyeImageView.setRotate(angle);
        eyeImageView.setScaleX(scale);
        eyeImageView.setScaleY(scale);
    }

    @Override
    public void onConnectionStateChanged(boolean isConnected)
    {
        Platform.runLater(() -> updateState());
    }

    @Override
    public void onScreenStatesChanged(int i, int i1, int i2, float v, float v1) {}

    @Override
    public void onTrackerStateChanged(int i) {}

    @FXML
    private void handleCalibrationButtonAction(ActionEvent event)
    {
        try
        {
            main.loadCalibrationScene();
        }
        catch (Exception e)
        {
            System.out.println("Error loading calibration scene " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConnectButtonAction(ActionEvent event)
    {
        //TODO: Handle progress connection timeout

        GazeManager.getInstance().activateAsync();
    }


    @FXML
    private void handleEvaluationButtonAction(ActionEvent event)
    {
        try
        {
            main.loadEvaluationScene();
        }
        catch (Exception e)
        {
            System.out.println("Error loading calibration scene " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onCalibrationChanged(boolean b, CalibrationResult calibrationResult)
    {
        Platform.runLater(() -> updateState());
    }

    private void updateState()
    {
        if(GazeManager.getInstance().isActivated())
        {
            connectBtn.setVisible(false);
            connect.setVisible(false);

            if(GazeManager.getInstance().isCalibrated())
            {
                calibrationBtn.setVisible(true);
                calibrationBtn.setText(bundle.getString("btn.recalibrate"));

                evaluateBtn.setVisible(true);

                rating.setRating(JavaFxCalibUtils.getCalibRating(GazeManager.getInstance().getLastCalibrationResult()));
            }
            else
            {
                calibrationBtn.setVisible(true);
                calibrationBtn.setText(bundle.getString("btn.calibrate"));

                evaluateBtn.setVisible(false);
            }

            rating.setVisible(true);
        }
        else
        {
            connect.setVisible(true);
            connectBtn.setVisible(true);
            calibrationBtn.setVisible(false);
        }

        progress.setVisible(false);
    }
}

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
import com.theeyetribe.javafx.ui.CalibrationButton;
import com.theeyetribe.javafx.utils.GazeFrameCache;
import com.theeyetribe.javafx.utils.JavaFxCalibUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.Rating;

/**
 * Scene controller associated to the evaluation scene
 */
public class SceneEvaluationController extends SceneController
{
    protected final static int NUM_CALIB_COLUMNS = 3, NUM_CALIB_ROWS = 3;

    @FXML
    private AnchorPane gazePointRoot;

    @FXML
    private Rating rating;

    private boolean mIsRecievingFrames;

    public SceneEvaluationController()
    {
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
            GazeFrameCache.getInstance().update(gazeData);

            if (!mIsRecievingFrames)
            {
                if(null != progress)
                {
                    progress.setVisible(false);

                    initGazeButtons();

                    rating.setRating(JavaFxCalibUtils.getCalibRating(GazeManager.getInstance().getLastCalibrationResult()));

                    mIsRecievingFrames = true;
                }
            }

            Point2D gaze = GazeFrameCache.getInstance().getLastSmoothedGazeCoordinates();
            if (null != gaze) {
                gazeIndicator.setVisible(true);
                javafx.geometry.Point2D rootAnchor = getGazeIndicatorAnchor(gaze);
                gazeIndicator.setX(rootAnchor.getX());
                gazeIndicator.setY(rootAnchor.getY());
            } else
                gazeIndicator.setVisible(false);
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
    public void onCalibrationChanged(boolean b, CalibrationResult calibrationResult) {

    }

    private void initGazeButtons()
    {
        double btnWidth = gazePointRoot.getWidth()  / NUM_CALIB_COLUMNS;
        double btnHeight = gazePointRoot.getHeight() / NUM_CALIB_ROWS;

        CalibrationButton calibBtn;
        double x= 0f, y = 0f;
        for(int i = 0; i < NUM_CALIB_ROWS; ++i)
        {
            for(int j = 0; j < NUM_CALIB_COLUMNS; ++j)
            {
                x = j * btnWidth;
                y = i * btnHeight;

                calibBtn = new CalibrationButton();
                calibBtn.setPrefWidth(btnWidth);
                calibBtn.setPrefHeight(btnHeight);
                calibBtn.setTranslateX(x);
                calibBtn.setTranslateY(y);

                gazePointRoot.getChildren().add(calibBtn);
            }
        }
    }

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
    private void handleDoneButtonAction(ActionEvent event)
    {
        try
        {
            main.loadMainScene();
        }
        catch (Exception e)
        {
            System.out.println("Error loading main scene " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}

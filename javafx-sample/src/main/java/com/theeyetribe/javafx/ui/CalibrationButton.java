/*
 * Copyright (c) 2013-present, The Eye Tribe.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree.
 *
 */

package com.theeyetribe.javafx.ui;

import com.theeyetribe.clientsdk.data.GazeData;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

/**
 * Custom gaze UI component that holds a circle that changes color upon gaze interaction
 */
public class CalibrationButton extends GazePane
{
    private Circle mCircle;

    private Color baseColor = Color.WHITE;
    private Color hitColor = Color.RED;

    public CalibrationButton() {
        super();
        init();
    }

    public CalibrationButton(Node... children)
    {
        super(children);
        init();
    }

    private void init()
    {
        Group g = new Group();

        mCircle = new Circle(0,0,40);
        mCircle.setFill(Color.WHITE);
        mCircle.setStrokeWidth(3);
        mCircle.setStrokeMiterLimit(10);
        mCircle.setStrokeType(StrokeType.CENTERED);
        mCircle.setStroke(Color.valueOf("0x333333"));

        Circle inner = new Circle(0,0,8);
        inner.setFill(Color.valueOf("0xFFFFFF00"));
        inner.setStrokeWidth(4);
        inner.setStrokeMiterLimit(10);
        inner.setStrokeType(StrokeType.INSIDE);
        inner.setStroke(Color.valueOf("0x000000"));

        g.getChildren().addAll(mCircle, inner);
        setAlignment(g, Pos.CENTER);

        getChildren().add(g);
    }

    @Override
    public void onGazeUpdate(GazeData gazeData) {
        Platform.runLater(() ->
        {
            CalibrationButton.super.onGazeUpdate(gazeData);

            mCircle.setFill(baseColor.interpolate(hitColor, getHeatFactor()));
        });
    }
}

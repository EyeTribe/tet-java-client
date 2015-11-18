/*
 * Copyright (c) 2013-present, The Eye Tribe.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree.
 *
 */

package com.theeyetribe.javafx.ui;

import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.data.Point2D;
import com.theeyetribe.javafx.utils.GazeFrameCache;
import com.theeyetribe.javafx.utils.JavaFxGazeUtils;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Custom gaze UI component. The component tracks gaze and will 'heat up' when gazed upon. Extending classes
 * can use the heat factor to gradually change color and more based on how long the component has been
 * gazed upon.
 */
public class GazePane extends StackPane implements IGazeListener
{
    private static final long DEFAULT_COOLDOWN_PERIOD_MILLIS = 1000;
    private long mCooldownPeriod = DEFAULT_COOLDOWN_PERIOD_MILLIS;

    protected long mCooldownValue;

    protected boolean mIsGazeColliding;

    public GazePane() {
        super();
    }

    public GazePane(Node... children)
    {
        super(children);
    }

    @Override
    public void onGazeUpdate(GazeData gazeData) {
        Platform.runLater(() -> {
            Point2D gaze = GazeFrameCache.getInstance().getLastSmoothedGazeCoordinates();
            final long lastFrameDelta = GazeFrameCache.getInstance().getLastDelta();

            mIsGazeColliding = false;

            // check bounds
            if (null != gaze && checkCollision(gaze))
            {

                // heating up, capping if too high
                if (mCooldownValue + lastFrameDelta < mCooldownPeriod)
                    mCooldownValue += lastFrameDelta;
                else
                    mCooldownValue = mCooldownPeriod;

                mIsGazeColliding = true;
            }
            else
            {
                // cooling down
                if (mCooldownValue - lastFrameDelta > 0)
                    mCooldownValue -= lastFrameDelta;
                else
                    mCooldownValue = 0;
            }
        });
    }

    protected boolean checkCollision(final Point2D gaze)
    {
        return JavaFxGazeUtils.checkGazeCollision(this, gaze);
    }

    /**
     * Set the time period it takes to 'heat up' and 'cool down' this layout.
     * <p>
     * Setting this period below zero results in instantaneous state change
     *
     * @param mCooldownPeriod
     *            time in millis that the cooldown period should take
     */
    protected void setCooldownPeriod(long mCooldownPeriod) {
        this.mCooldownPeriod = mCooldownPeriod;
    }

    /**
     * Return the heat factor which is an indicator of how long gaze has been
     * upon the layout relative to the set cooldownPeriod
     *
     * @return heatFactor value [ 0.0 : 1.0f]
     */
    public float getHeatFactor() {
        return mCooldownPeriod > 0 ? (float) mCooldownValue / mCooldownPeriod : 0;
    }
}

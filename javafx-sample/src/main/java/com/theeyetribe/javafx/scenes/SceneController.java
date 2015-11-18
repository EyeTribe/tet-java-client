/*
 * Copyright (c) 2013-present, The Eye Tribe.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree.
 *
 */

package com.theeyetribe.javafx.scenes;

import com.theeyetribe.clientsdk.*;
import com.theeyetribe.clientsdk.data.Point2D;
import com.theeyetribe.javafx.Main;
import com.theeyetribe.javafx.utils.JavaFxGazeUtils;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ResourceBundle;

/**
 * Base class for all scene controllers holding common methods
 */
public abstract class SceneController implements IGazeListener, ITrackerStateListener, IScreenStateListener, IConnectionStateListener, ICalibrationResultListener
{
    @FXML
    protected ProgressIndicator progress;

    @FXML
    protected Pane root;

    @FXML
    protected Pane innerRoot;

    @FXML
    protected ImageView gazeIndicator;

    protected Main main;

    @FXML
    protected ResourceBundle bundle;

    protected ListChangeListener<Node> childrenChanged;

    protected boolean isInitialized;

    public void setMain(Main main)
    {
        this.main = main;
    }

    @FXML
    public void initialize()
    {
        //We recursively attach listeners to currently attached childen
        JavaFxGazeUtils.attachTETListenersRecursive(root);

        //We make sure that children added/removed in the future automatically attaches/detaches gaze listeners
        childrenChanged = (ListChangeListener.Change<? extends Node> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Node n : change.getAddedSubList()) {

                        JavaFxGazeUtils.attachTETListenersRecursive(n);

                        if(n instanceof Pane)
                            ((Pane) n).getChildren().addListener(childrenChanged);
                    }
                } else if (change.wasRemoved()) {
                    for (Node n : change.getRemoved()) {

                        JavaFxGazeUtils.detachTETListenersRecursive(n);

                        if(n instanceof Pane)
                            ((Pane) n).getChildren().removeListener(childrenChanged);
                    }
                }
            }
        };
        attachChangeListener(root, childrenChanged);

        bundle = ResourceBundle.getBundle("Bundle");

        isInitialized = true;
    }

    private void attachChangeListener(Node n, ListChangeListener<Node> changeListener)
    {
        if(null != n && n instanceof Pane)
        {
            ((Pane)n).getChildren().addListener(changeListener);

            for (Node child : ((Pane)n).getChildren())
            {
                attachChangeListener(child, changeListener);
            }
        }
    }

    public void onStart()
    {
        JavaFxGazeUtils.attachTETListeners(this);
    }

    public void onStop()
    {
        JavaFxGazeUtils.detachTETListeners(this);
        root.getChildren().clear();
    };

    /**
     * Calculated top left corner of Gaze Indicator based on gaze coordinates, indicator view.
     * <p>
     * The returned anchor point is offset according to the anchor of the Activity ContentView
     * to ensure that possible ActionBar offsets are taken into account.
     *
     * @param gazeCoords
     * @return
     */
    public javafx.geometry.Point2D getGazeIndicatorAnchor(final Point2D gazeCoords)
    {
        if(null != gazeCoords)
            return innerRoot.screenToLocal(gazeCoords.x, gazeCoords.y);

        return null;
    }

    @FXML
    private void handleCloseButtonAction(ActionEvent event)
    {
        main.closeProgram();
    }
}

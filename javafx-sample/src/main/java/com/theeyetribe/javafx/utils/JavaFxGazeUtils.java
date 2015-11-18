/*
 * Copyright (c) 2013-present, The Eye Tribe.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree.
 *
 */

package com.theeyetribe.javafx.utils;

import com.theeyetribe.clientsdk.*;
import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.data.Point2D;
import com.theeyetribe.clientsdk.utils.GazeUtils;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.util.List;

/**
 * Extending {@link com.theeyetribe.clientsdk.utils.GazeUtils GazeUtils} with methods specific to JavaFX
 */
public class JavaFxGazeUtils extends GazeUtils
{
    public static final String TAG = JavaFxGazeUtils.class.getSimpleName();
    
    public static boolean checkGazeCollision(Node n, Point2D gaze)
    {
        if(null == n || null == gaze )
            return false;

        Rectangle roi = new Rectangle();

        return checkViewCollision(n, gaze, roi);
    }
    
    public static void checkGazeCollisionRecursive(Node n, Point2D gaze, List<Node> result)
    {
        if(null == n || null == gaze || null == result)
            return;

        Pane p;
        Rectangle roi = new Rectangle();
        if(n instanceof Pane)
        {
            p = ((Pane)n);

            if(!p.isDisabled() && checkViewCollision(p, gaze, roi))
                result.add(p);

            ObservableList<Node> children = p.getChildren();
            for(int i=0; i<children.size(); ++i)
            {
                Node nextChild = children.get(i);

                if(nextChild instanceof Pane)
                    checkGazeCollisionRecursive(nextChild, gaze, result);
                else if(checkViewCollision(nextChild, gaze, roi))
                    result.add(nextChild);
            }
        }
        else
        {
            if(checkViewCollision(n, gaze, roi))
                result.add(n);
        }
    }

    private static boolean checkViewCollision(Node n, Point2D gaze, Rectangle roi)
    {
        javafx.geometry.Point2D screenCoord = n.localToScreen(0d, 0d);

        roi.setX(Math.round(screenCoord.getX()));
        roi.setY(Math.round(screenCoord.getY()));
        roi.setWidth(n.getBoundsInParent().getWidth());
        roi.setHeight(n.getBoundsInParent().getHeight());

        return roi.contains(
                (int)Math.round(gaze.x),
                (int)Math.round(gaze.y)
        );
    }

    public static void attachTETListenersRecursive(Node n)
    {
        attachTETListenersRecursive(n, false);
    }

    /**
     * Utility method that registers all types of TETListeners for parameter View and its children
     *
     * @param n
     * @param checkVisibility
     */
    public static void attachTETListenersRecursive(Node n, boolean checkVisibility)
    {
        Pane p;
        if(n instanceof Pane)
        {
            p = ((Pane)n);

            if(!checkVisibility || (checkVisibility && p.isVisible()))
                attachTETListeners(p);

            ObservableList<Node> children = p.getChildren();
            for(int i=0; i<children.size(); ++i)
            {
                Node nextChild = children.get(i);

                if(!checkVisibility || (checkVisibility && nextChild.isVisible()))
                {
                    if(nextChild instanceof Pane)
                        attachTETListenersRecursive(nextChild, checkVisibility);
                    else
                        attachTETListeners(nextChild);
                }
            }
        }
        else
        {
            if(!checkVisibility || (checkVisibility && n.isVisible()))
                attachTETListeners(n);
        }
    }

    public static void attachTETListeners(Object o)
    {
        if(o instanceof IGazeListener)
            GazeManager.getInstance().addGazeListener((IGazeListener)o);

        if(o instanceof IConnectionStateListener)
            GazeManager.getInstance().addConnectionStateListener((IConnectionStateListener) o);

        if(o instanceof ITrackerStateListener)
            GazeManager.getInstance().addTrackerStateListener((ITrackerStateListener) o);

        if (o instanceof ICalibrationResultListener)
            GazeManager.getInstance().addCalibrationResultListener((ICalibrationResultListener) o);
    }

    /**
     * Utility method that deregisters all types of TETListeners for parameter View and its children
     *
     * @param n
     */
    public static void detachTETListenersRecursive(Node n)
    {
        Pane p;
        if(n instanceof Pane)
        {
            p = ((Pane)n);
            detachTETListeners(p);

            ObservableList<Node> children = p.getChildren();
            for(int i=0; i<children.size(); ++i)
            {
                Node nextChild = children.get(i);

                if(nextChild instanceof Pane)
                    detachTETListenersRecursive(nextChild);
                else
                    detachTETListeners(nextChild);
            }
        }
        else
        {
            detachTETListeners(n);
        }
    }

    public static void detachTETListeners(Object o)
    {
        if(o instanceof IGazeListener)
            GazeManager.getInstance().removeGazeListener((IGazeListener) o);

        if(o instanceof IConnectionStateListener)
            GazeManager.getInstance().removeConnectionStateListener((IConnectionStateListener) o);

        if(o instanceof ITrackerStateListener)
            GazeManager.getInstance().removeTrackerStateListener((ITrackerStateListener) o);

        if(o instanceof ICalibrationResultListener)
            GazeManager.getInstance().removeCalibrationResultListener((ICalibrationResultListener) o);
    }

    public static float getAvgFramesPerSecond(GazeDataDeque frames)
    {
        float avgMillis;
        if((avgMillis = getAvgMillisFrame(frames)) > 0 )
            return (float)1000/avgMillis;

        return -1;
    }

    public static float getAvgMillisFrame(GazeDataDeque frames)
    {
        GazeData first = frames.peekFirst();
        GazeData last = frames.peekLast();

        if(null != first && null != last)
        {
            float delta = first.timeStamp - last.timeStamp;
            return delta / frames.size();
        }

        return -1;
    }

    /**
     * Clamps a gaze points within the limits of the parameter rect.
     *
     * @param gaze point to clamp
     * @param rect clamping rect
     * @return clamped gaze point
     */
    public static Point2D clampGazeToRect(Point2D gaze, Rectangle rect)
    {
        return clampGazeToRect(gaze, (int)Math.round(rect.getX()), (int)Math.round(rect.getY()), (int)Math.round(rect.getWidth()), (int)Math.round(rect.getHeight()), 0, 0);
    }

    /**
     * Clamps a gaze points within the limits of the parameter rect and taking into account the desired
     * vertical and horizontal margins.
     * <p/>
     * If the gaze point is within the margin, it will be clamped inside the rect.
     *
     * @param gaze point to clamp
     * @param rect clamping rect
     * @param clampMargin clamping margin
     * @return clamped gaze point
     */
    public static Point2D clampGazeToRect(Point2D gaze, Rectangle rect, float clampMargin)
    {
        return clampGazeToRect(gaze, (int)Math.round(rect.getX()), (int)Math.round(rect.getY()), (int)Math.round(rect.getWidth()), (int)Math.round(rect.getHeight()), clampMargin, clampMargin);
    }

    /**
     * Clamps a gaze points within the limits of the parameter rect and taking into account the desired
     * vertical and horizontal margins.
     * <p/>
     * If the gaze point is within the margin, it will be clamped inside the rect.
     *
     * @param gaze point to clamp
     * @param rect clamping rect
     * @param clampHorsMargin horizontal clamping margin
     * @param clampVertMargin vertical clamping margin
     * @return clamped gaze point
     */
    public static Point2D clampGazeToRect(Point2D gaze, Rectangle rect, float clampHorsMargin, float clampVertMargin)
    {
        return clampGazeToRect(gaze, (int)Math.round(rect.getX()), (int)Math.round(rect.getY()), (int)Math.round(rect.getWidth()), (int)Math.round(rect.getHeight()), clampHorsMargin, clampVertMargin);
    }
}

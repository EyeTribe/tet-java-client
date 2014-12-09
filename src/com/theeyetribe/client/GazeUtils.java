/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.client;

import com.theeyetribe.client.data.GazeData;
import com.theeyetribe.client.data.GazeData.Eye;
import com.theeyetribe.client.data.Point2D;

/**
 * Utility methods common to gaze control routines.
 */
public class GazeUtils
{
    /**
     * Find average pupil center of two eyes.
     * 
     * @param leftEye
     * @param rightEye
     * @return the average center point in normalized values
     */
    public static Point2D getEyesCenterNormalized(Eye leftEye, Eye rightEye)
    {
        Point2D eyeCenter = null;

        if (null != leftEye && null != rightEye)
        {
            eyeCenter = new Point2D((leftEye.pupilCenterCoordinates.x + rightEye.pupilCenterCoordinates.x) / 2,
                    (leftEye.pupilCenterCoordinates.y + rightEye.pupilCenterCoordinates.y) / 2);
        }
        else if (null != leftEye)
        {
            eyeCenter = leftEye.pupilCenterCoordinates;
        }
        else if (null != rightEye)
        {
            eyeCenter = rightEye.pupilCenterCoordinates;
        }

        return eyeCenter;
    }

    public static Point2D getEyesCenterNormalized(GazeData gazeData)
    {
        if (null != gazeData)
            return getEyesCenterNormalized(gazeData.leftEye, gazeData.rightEye);
        else
            return null;
    }

    /**
     * Find average pupil center of two eyes.
     * 
     * @param leftEye
     * @param rightEye
     * @param screenWidth
     * @param screenHeight
     * @return the average center point in pixels
     */
    public static Point2D getEyesCenterPixels(Eye leftEye, Eye rightEye, int screenWidth, int screenHeight)
    {
        Point2D center = getEyesCenterNormalized(leftEye, rightEye);

        return getRelativeToScreenSpace(center, screenWidth, screenHeight);
    }

    /**
     * Find average pupil center of two eyes.
     * 
     * @param gazeData gaze data frame to base calculation upon
     * @param screenWidth
     * @param screenHeight
     * @return the average center point in pixels
     */
    public static Point2D getEyesCenterPixels(GazeData gazeData, int screenWidth, int screenHeight)
    {
        if (null != gazeData)
            return getEyesCenterPixels(gazeData.leftEye, gazeData.rightEye, screenWidth, screenHeight);
        else
            return null;
    }

    private static double _MinimumEyesDistance = 0.1f;
    private static double _MaximumEyesDistance = 0.3f;

    /**
     * Calculates distance between pupil centers based on previously recorded min and max values.
     * 
     * @param leftEye
     * @param rightEye
     * @return a normalized value [0..1] if eye present, -1 if not
     */
    public static double getEyesDistanceNormalized(Eye leftEye, Eye rightEye)
    {
        if (null != leftEye && null != rightEye)
        {
            double dist = Math.abs(getDistancePoint2D(leftEye.pupilCenterCoordinates, rightEye.pupilCenterCoordinates));

            if (dist < _MinimumEyesDistance)
                _MinimumEyesDistance = dist;

            if (dist > _MaximumEyesDistance)
                _MaximumEyesDistance = dist;

            // return normalized
            return dist / (_MaximumEyesDistance - _MinimumEyesDistance);
        }

        return -1;
    }

    /**
     * Calculates distance between pupil centers based on previously recorded min and max values.
     * 
     * @param gazeData gaze data frame to base calculation upon
     * @return a normalized value [0..1] if eye present, -1 if not
     */
    public static double getEyesDistanceNormalized(GazeData gazeData)
    {
        return getEyesDistanceNormalized(gazeData.leftEye, gazeData.rightEye);
    }

    /**
     * Calculates distance between two points.
     * 
     * @param a
     * @param b
     * @return distance in pixels between two points
     */
    public static double getDistancePoint2D(Point2D a, Point2D b)
    {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    /**
     * Converts a relative point to screen point in pixels.
     * 
     * @param point in relative values
     * @param screenWidth in pixels
     * @param screenHeight in pixels
     * @return a 2d point in pixels
     */
    public static Point2D getRelativeToScreenSpace(Point2D point, int screenWidth, int screenHeight)
    {
        Point2D screenPoint = null;

        if (null != point)
        {
            screenPoint = new Point2D(point);
            screenPoint.x = Math.round(screenPoint.x * screenWidth);
            screenPoint.y = Math.round(screenPoint.y * screenHeight);
        }

        return screenPoint;
    }

    /**
     * Normalizes a point on screen in screen dims
     * 
     * @param point in pixels
     * @param screenWidth in pixels
     * @param screenHeight in pixels
     * @return a 2d point in relative values
     */
    public static Point2D getNormalizedCoords(Point2D point, int screenWidth, int screenHeight)
    {
        Point2D norm = null;

        if (null != point)
        {
            norm = new Point2D(point);
            norm.x /= screenWidth;
            norm.y /= screenHeight;
        }

        return norm;
    }

    /**
     * Maps eye position of gaze coords in pixels within normalized space [x: -1:1 , y: -1:1]
     * 
     * @param point in pixels
     * @param screenWidth in pixels
     * @param screenHeight in pixels
     * @return a relative point mapped into normalized space [x: -1:1 , y: -1:1]
     */
    public static Point2D getNormalizedMapping(Point2D point, int screenWidth, int screenHeight)
    {
        Point2D normMap = getNormalizedCoords(point, screenWidth, screenHeight);

        if (null != normMap)
        {
            // scale up and shift
            normMap.x *= 2f;
            normMap.x -= 1f;
            normMap.y *= 2f;
            normMap.y -= 1f;
        }

        return normMap;
    }
}

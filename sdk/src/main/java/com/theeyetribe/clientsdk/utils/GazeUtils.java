/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk.utils;

import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.data.GazeData.Eye;
import com.theeyetribe.clientsdk.data.Point2D;
import org.jetbrains.annotations.Nullable;

/**
 * Utility methods common to working with gaze estimation.
 */
public class GazeUtils
{
    protected GazeUtils()
    {
        //ensure non-instantiability
    }

    /**
     * Find average pupil center of two eyes.
     * 
     * @param leftEye left eye
     * @param rightEye right eye
     * @return the average center point in normalized values
     */
    public static @Nullable Point2D getEyesCenterNormalized(Eye leftEye, Eye rightEye)
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

    /**
     * Find average pupil center of two eyes.
     *
     * @param gazeData gaze data frame to base calculation upon
     * @return the average center point in normalized values
     */
    @Nullable
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
     * @param leftEye left eye
     * @param rightEye right eye
     * @param dimWidth width in pixels of area to map relative coordinates to
     * @param dimHeight height in pixels of area to map relative coordinates to
     * @return the average center point in pixels
     */
    @Nullable
    public static Point2D getEyesCenterPixels(Eye leftEye, Eye rightEye, int dimWidth, int dimHeight)
    {
        Point2D center = getEyesCenterNormalized(leftEye, rightEye);

        return getRelativeToRect(center, dimWidth, dimHeight);
    }

    /**
     * Find average pupil center of two eyes.
     * 
     * @param gazeData gaze data frame to base calculation upon
     * @param dimWidth width in pixels of area to map relative coordinates to
     * @param dimHeight height in pixels of area to map relative coordinates to
     * @return the average center point in pixels
     */
    @Nullable
    public static Point2D getEyesCenterPixels(GazeData gazeData, int dimWidth, int dimHeight)
    {
        if (null != gazeData)
            return getEyesCenterPixels(gazeData.leftEye, gazeData.rightEye, dimWidth, dimHeight);
        else
            return null;
    }

    private static double _MinimumEyesDistance = 0.1f;
    private static double _MaximumEyesDistance = 0.3f;

    /**
     * Calculates distance between pupil centers based on previously recorded min and max values.
     * 
     * @param leftEye left eye
     * @param rightEye right eye
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
     * @param a first point
     * @param b second point
     * @return distance in pixels between two points
     */
    public static double getDistancePoint2D(Point2D a, Point2D b)
    {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    /**
     * Converts a relative point to coordinate in rect.
     * <p>
     * Use to map to screen coordinates.
     *
     * @param point in relative values
     * @param dimWidth width of area to map relative coordinates to
     * @param dimHeight height of area to map relative coordinates to
     * @return a 2d point in pixels
     */
    @Nullable
    public static Point2D getRelativeToRect(Point2D point, int dimWidth, int dimHeight)
    {
        Point2D rectPoint = null;

        if (null != point)
        {
            rectPoint = new Point2D(point);
            rectPoint.x = Math.round(rectPoint.x * dimWidth);
            rectPoint.y = Math.round(rectPoint.y * dimHeight);
        }

        return rectPoint;
    }

    /**
     * Clamps a gaze points within the limits of the parameter rect.
     *
     * @param gaze point to clamp
     * @param dimWidth width of the rect
     * @param dimHeight height of the rect
     * @return clamped gaze point
     */
    public static Point2D clampGazeToRect(Point2D gaze, int dimWidth, int dimHeight)
    {
        return clampGazeToRect(gaze, 0, 0, dimWidth, dimHeight, 0, 0);
    }

    /**
     * Clamps a gaze points within the limits of the parameter rect.
     *
     * @param gaze point to clamp
     * @param dimX x coordinate of topleft rect anchor
     * @param dimY y coordinate of topleft rect anchor
     * @param dimWidth width of the rect
     * @param dimHeight height of the rect
     * @return clamped gaze point
     */
    public static Point2D clampGazeToRect(Point2D gaze, int dimX, int dimY, int dimWidth, int dimHeight)
    {
        return clampGazeToRect(gaze, dimX, dimY, dimWidth, dimHeight, 0, 0);
    }

    /**
     * Clamps a gaze points within the limits of the parameter rect and taking into account the desired
     * vertical and horizontal margins.
     * <p>
     * If the gaze point is within the margin, it will be clamped inside the rect.
     *
     * @param gaze point to clamp
     * @param dimWidth width of the rect
     * @param dimHeight height of the rect
     * @param clampMargin clamping margin
     * @return clamped gaze point
     */
    public static Point2D clampGazeToRect(Point2D gaze, int dimWidth, int dimHeight, float clampMargin)
    {
        return clampGazeToRect(gaze, 0, 0, dimWidth, dimHeight, clampMargin, clampMargin);
    }

    /**
     * Clamps a gaze points within the limits of the parameter rect and taking into account the desired
     * vertical and horizontal margins.
     * <p>
     * If the gaze point is within the margin, it will be clamped inside the rect.
     *
     * @param gaze point to clamp
     * @param dimX x coordinate of topleft rect anchor
     * @param dimY y coordinate of topleft rect anchor
     * @param dimWidth width of the rect
     * @param dimHeight height of the rect
     * @param clampHorsMargin horizontal clamping margin
     * @param clampVertMargin vertical clamping margin
     * @return clamped gaze point
     */
    @Nullable
    public static Point2D clampGazeToRect(Point2D gaze, int dimX, int dimY, int dimWidth, int dimHeight, float clampHorsMargin, float clampVertMargin)
    {
        Point2D clamped = null;

        if(null != gaze)
        {
            if(gaze.x < dimX && gaze.x > dimX -clampHorsMargin)
            {
                if(null == clamped)
                    clamped = new Point2D(gaze);

                clamped.x = dimX + 1;
            }

            if(gaze.x > dimX + dimWidth && gaze.x < (dimX + dimWidth + clampHorsMargin))
            {
                if(null == clamped)
                    clamped = new Point2D(gaze);

                clamped.x = dimX + dimWidth - 1;
            }

            if(gaze.y < dimY && gaze.y > dimY - clampVertMargin)
            {
                if(null == clamped)
                    clamped = new Point2D(gaze);

                clamped.y = dimY + 1;
            }

            if(gaze.y > dimY + dimHeight && gaze.y < (dimY + dimHeight + clampVertMargin))
            {
                if(null == clamped)
                    clamped = new Point2D(gaze);

                clamped.y = dimY + dimHeight - 1;
            }
        }

        return null == clamped ? gaze : clamped;
    }

    /**
     * Normalizes a point according to parameter rect dimensions
     * 
     * @param point in pixels
     * @param dimWidth width of area to map relative coordinates to
     * @param dimHeight height of area to map relative coordinates to
     * @return a 2d point in relative values
     */
    @Nullable
    public static Point2D getNormalizedCoords(Point2D point, int dimWidth, int dimHeight)
    {
        Point2D norm = null;

        if (null != point)
        {
            norm = new Point2D(point);
            norm.x /= dimWidth;
            norm.y /= dimHeight;
        }

        return norm;
    }

    /**
     * Maps eye position of gaze coords in pixels within normalized space [x: -1:1 , y: -1:1]
     * 
     * @param point in pixels
     * @param dimWidth width of area to map relative coordinates to
     * @param dimHeight height of area to map relative coordinates to
     * @return a relative point mapped into normalized space [x: -1:1 , y: -1:1]
     */
    @Nullable
    public static Point2D getNormalizedMapping(Point2D point, int dimWidth, int dimHeight)
    {
        Point2D normMap = getNormalizedCoords(point, dimWidth, dimHeight);

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

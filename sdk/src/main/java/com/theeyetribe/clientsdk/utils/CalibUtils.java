/*
 * Copyright (c) 2013-present, The Eye Tribe.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree.
 *
 */

package com.theeyetribe.clientsdk.utils;

import com.theeyetribe.clientsdk.data.CalibrationResult;
import com.theeyetribe.clientsdk.data.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility methods associated to evaluating the quality of a CalibrationResult.
 */
public class CalibUtils
{
    private static final float EPSILON = 1e-005f;

    public static enum CalibQuality
    {
        NONE (0),
        PERFECT (4),
        GOOD (3),
        MODERATE (2),
        POOR (1);

        private int quality;

        CalibQuality(int quality) {
            this.quality = quality;
        }
    }

    protected CalibUtils()
    {
        //ensure non-instantiability
    }

    /**
     * Return CalibQuality based on CalibrationResult object.
     *
     * @param result calibration result to evaluate
     * @return CalibQuality according to CalibrationResult paramenter
     */
    public static CalibQuality getCalibQuality(CalibrationResult result)
    {
        if (result != null && result.averageErrorDegree > EPSILON)
        {
            if (result.averageErrorDegree < 0.5)
            {
                return CalibQuality.PERFECT;
            }
            else if (result.averageErrorDegree < 0.7)
            {
                return CalibQuality.GOOD;
            }
            else if (result.averageErrorDegree < 1)
            {
                return CalibQuality.MODERATE;
            }
            else if (result.averageErrorDegree < 1.5)
            {
                return CalibQuality.POOR;
            }
        }

        return CalibQuality.NONE;
    }

    /**
     * Return an int representation of calibration quality based on CalibrationResult object.
     * <p>
     * Useful for setting 'star rating' UI components explaining the current calibration quality.
     *
     * @param result calibration result to evaluate
     * @return int value from 0-4, higher number equals better calibration
     */
    public static int getCalibRating(CalibrationResult result)
    {
        CalibQuality cq = getCalibQuality(result);

        if (cq.equals(CalibQuality.PERFECT))
        {
            return 4;
        }
        else if (cq.equals(CalibQuality.GOOD))
        {
            return 3;
        }
        else if (cq.equals(CalibQuality.MODERATE))
        {
            return 2;
        }
        else if (cq.equals(CalibQuality.POOR))
        {
            return 1;
        }

        return 0;
    }

    /**
     * Helper method that generates geometric calibration points based on desired rect area.
     * <p>
     * This is useful when implementing a custom calibration UI.
     *
     * @param rows the number of rows in calibration point grid
     * @param columns the number of columns in calibration point grid
     * @param width width of the rect area to spread the calibration points in
     * @param height height of the rect area to spread the calibration points in
     * @return list of calibration points
     */
    public static List<Point2D> initCalibrationPoints(int rows, int columns, int width, int height)
    {
        return initCalibrationPoints(rows, columns, width, height, 0, 0, true);
    }

    /**
     * Helper method that generates geometric calibration points based on desired rect area.
     * <p>
     * This is useful when implementing a custom calibration UI.
     *
     * @param rows the number of rows in calibration point grid
     * @param columns the number of columns in calibration point grid
     * @param width width of the rect area to spread the calibration points in
     * @param height height of the rect area to spread the calibration points in
     * @param shuffle should the returned calibration point be shuffled
     * @return list of calibration points
     */
    public static List<Point2D> initCalibrationPoints(int rows, int columns, int width, int height, boolean shuffle)
    {
        return initCalibrationPoints(rows, columns, width, height, 0, 0, shuffle);
    }

    /**
     * Helper method that generates geometric calibration points based on desired rect area.
     * <p>
     * This is useful when implementing a custom calibration UI.
     *
     * @param rows the number of rows in calibration point grid
     * @param columns the number of columns in calibration point grid
     * @param width width of the rect area to spread the calibration points in
     * @param height height of the rect area to spread the calibration points in
     * @return list of calibration points
     */
    public static List<Point2D> initCalibrationPoints(int rows, int columns, double width, double height)
    {
        return initCalibrationPoints(rows, columns, (int)Math.round(width), (int)Math.round(height), 0, 0, true);
    }

    /**
     * Helper method that generates geometric calibration points based on desired rect area.
     * <p>
     * This is useful when implementing a custom calibration UI.
     *
     * @param rows the number of rows in calibration point grid
     * @param columns the number of columns in calibration point grid
     * @param width width of the rect area to spread the calibration points in
     * @param height height of the rect area to spread the calibration points in
     * @param shuffle should the returned calibration point be shuffled
     * @return list of calibration points
     */
    public static List<Point2D> initCalibrationPoints(int rows, int columns, double width, double height, boolean shuffle)
    {
        return initCalibrationPoints(rows, columns, (int)Math.round(width), (int)Math.round(height), 0, 0, shuffle);
    }

    /**
     * Helper method that generates geometric calibration points based on desired rect area.
     * <p>
     * This is useful when implementing a custom calibration UI.
     *
     * @param rows the number of rows in calibration point grid
     * @param columns the number of columns in calibration point grid
     * @param width width of the rect area to spread the calibration points in
     * @param height height of the rect area to spread the calibration points in
     * @param paddingHors optional horizontal padding in rect area
     * @param paddingVert optional vertical padding in rect area
     * @return list of calibration points
     */
    public static List<Point2D> initCalibrationPoints(int rows, int columns, double width, double height, double paddingHors, double paddingVert)
    {
        return initCalibrationPoints(rows, columns, (int)Math.round(width), (int)Math.round(height), (int)Math.round(paddingHors), (int)Math.round(paddingVert), true);
    }

    /**
     * Helper method that generates geometric calibration points based on desired rect area.
     * <p>
     * This is useful when implementing a custom calibration UI.
     *
     * @param rows the number of rows in calibration point grid
     * @param columns the number of columns in calibration point grid
     * @param width width of the rect area to spread the calibration points in
     * @param height height of the rect area to spread the calibration points in
     * @param paddingHors optional horizontal padding in rect area
     * @param paddingVert optional vertical padding in rect area
     * @param shuffle should the returned calibration point be shuffled
     * @return list of calibration points
     */
    public static List<Point2D> initCalibrationPoints(int rows, int columns, double width, double height, double paddingHors, double paddingVert, boolean shuffle)
    {
        return initCalibrationPoints(rows, columns, (int)Math.round(width), (int)Math.round(height), (int)Math.round(paddingHors), (int)Math.round(paddingVert), shuffle);
    }

    /**
     * Helper method that generates geometric calibration points based on desired rect area.
     * <p>
     * This is useful when implementing a custom calibration UI.
     *
     * @param rows the number of rows in calibration point grid
     * @param columns the number of columns in calibration point grid
     * @param width width of the rect area to spread the calibration points in
     * @param height height of the rect area to spread the calibration points in
     * @param paddingHors optional horizontal padding in rect area
     * @param paddingVert optional vertical padding in rect area
     * @return list of calibration points
     */
    public static List<Point2D> initCalibrationPoints(int rows, int columns, int width, int height, int paddingHors, int paddingVert)
    {
        return initCalibrationPoints(rows, columns, width, height, paddingHors, paddingVert, true);
    }

    /**
     * Helper method that generates geometric calibration points based on desired rect area.
     * <p>
     * This is useful when implementing a custom calibration UI.
     *
     * @param rows the number of rows in calibration point grid
     * @param columns the number of columns in calibration point grid
     * @param width width of the rect area to spread the calibration points in
     * @param height height of the rect area to spread the calibration points in
     * @param paddingHors optional horizontal padding in rect area
     * @param paddingVert optional vertical padding in rect area
     * @param shuffle should the returned calibration point be shuffled
     * @return list of calibration points
     */
    public static List<Point2D> initCalibrationPoints(int rows, int columns, int width, int height, int paddingHors, int paddingVert, boolean shuffle)
    {
        List<Point2D> anchors = new ArrayList<Point2D>();

        double x = 0,y = 0;
        double horsSlice = (double)(width - paddingHors - paddingHors) / (columns - 1);
        double vertSlice = (double)(height - paddingVert - paddingVert) / (rows - 1);
        for(int i = 0; i < columns; i++)
        {
            x = horsSlice * i;

            for(int j = 0; j < rows; j++)
            {
                y = vertSlice * j;

                Point2D p = new Point2D((float)(paddingHors + x), (float)(paddingVert + y));

                anchors.add(p);
            }
        }

        //randomly shuffle points
        if(shuffle)
            Collections.shuffle(anchors);

        return anchors;
    }
}

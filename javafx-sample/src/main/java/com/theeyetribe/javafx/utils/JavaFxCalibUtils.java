/*
 * Copyright (c) 2013-present, The Eye Tribe.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree.
 *
 */

package com.theeyetribe.javafx.utils;

import com.theeyetribe.clientsdk.data.CalibrationResult;
import com.theeyetribe.clientsdk.data.Point2D;
import com.theeyetribe.clientsdk.utils.CalibUtils;
import javafx.scene.layout.Region;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Extending {@link com.theeyetribe.clientsdk.utils.CalibUtils CalibUtils} with methods specific to JavaFX
 */
public class JavaFxCalibUtils extends CalibUtils
{
    public static String getCalibString(@Nonnull ResourceBundle bundle, CalibQuality result)
    {
        if(null == bundle)
            throw new IllegalArgumentException("ResourceBundle cannot be null!");

        if (result.equals(CalibQuality.PERFECT))
        {
            return bundle.getString("calib.state.perfect");
        }
        else if (result.equals(CalibQuality.GOOD))
        {
            return bundle.getString("calib.state.good");
        }
        else if (result.equals(CalibQuality.MODERATE))
        {
            return bundle.getString("calib.state.moderate");
        }
        else if (result.equals(CalibQuality.POOR))
        {
            return bundle.getString("calib.state.poor");
        }

        return bundle.getString("calib.state.none");
    }

    public static String getCalibString(ResourceBundle bundle, CalibrationResult result)
    {
        return getCalibString(bundle, getCalibQuality(result));
    }

    protected List<Point2D> initCalibrationPoints(int rows, int columns, Region region)
    {
        return initCalibrationPoints(rows, columns, region, 0, 0, true);
    }

    protected List<Point2D> initCalibrationPoints(int rows, int columns, Region region, boolean shuffle)
    {
        return initCalibrationPoints(rows, columns, region, 0, 0, shuffle);
    }

    protected List<Point2D> initCalibrationPoints(int rows, int columns, Region region, double paddingHors, double paddingVert)
    {
        return initCalibrationPoints(rows, columns, region, paddingHors, paddingVert, true);
    }

    protected List<Point2D> initCalibrationPoints(int rows, int columns, @Nonnull Region region, double paddingHors, double paddingVert, boolean shuffle)
    {
        if(null == region)
            throw new IllegalArgumentException("Region cannot be null!");

        return initCalibrationPoints(rows, columns, (int)Math.round(region.getWidth()), (int)Math.round(region.getHeight()), paddingHors, paddingVert, shuffle);
    }
}

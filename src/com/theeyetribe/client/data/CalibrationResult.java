/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.client.data;

import java.util.Arrays;

import com.google.gson.annotations.SerializedName;
import com.theeyetribe.client.Protocol;
import com.theeyetribe.client.utils.HashUtils;

/**
 * CalibrationResult holds outcome of a calibration procedure. It defines if calibration was successful or if certain
 * calibration points needs resampling.
 */
public class CalibrationResult
{
    /*
     * Was the calibration sucessful?
     */
    public Boolean result = false;

    /*
     * Average error in degrees
     */
    @SerializedName(Protocol.CALIBRESULT_AVERAGE_ERROR_DEGREES)
    public Double averageErrorDegree = 0d;

    /*
     * Average error in degs, left eye
     */
    @SerializedName(Protocol.CALIBRESULT_AVERAGE_ERROR_LEFT_DEGREES)
    public Double averageErrorDegreeLeft = 0d;

    /*
     * Average error in degs, right eye
     */
    @SerializedName(Protocol.CALIBRESULT_AVERAGE_ERROR_RIGHT_DEGREES)
    public Double averageErrorDegreeRight = 0d;

    /*
     * Complete list of calibrationpoints
     */
    public CalibrationPoint[] calibpoints;

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof CalibrationResult))
            return false;

        CalibrationResult other = (CalibrationResult) o;

        return result == other.result
                && Double.doubleToLongBits(averageErrorDegree) == Double.doubleToLongBits(other.averageErrorDegree)
                && Double.doubleToLongBits(averageErrorDegreeLeft) == Double
                        .doubleToLongBits(other.averageErrorDegreeLeft)
                && Double.doubleToLongBits(averageErrorDegreeRight) == Double
                        .doubleToLongBits(other.averageErrorDegreeRight)
                && Arrays.equals(calibpoints, other.calibpoints);
    }

    @Override
    public int hashCode()
    {
        int hash = 347;
        hash = hash * 199 + HashUtils.hash(result);
        hash = hash * 199 + HashUtils.hash(averageErrorDegree);
        hash = hash * 199 + HashUtils.hash(averageErrorDegreeLeft);
        hash = hash * 199 + HashUtils.hash(averageErrorDegreeRight);

        int hc = calibpoints.length;
        for (CalibrationPoint cp : calibpoints)
            hc = hc * 199 + cp.hashCode();
        hash += hc;

        return hash;
    }

    public class CalibrationPoint
    {
        /*
         * State defines that no data is available for calibration point
         */
        public static final int STATE_NO_DATA = 0;
        /*
         * State defines that calibration point should be resampled
         */
        public static final int STATE_RESAMPLE = 1;
        /*
         * State defines that calibration point was successfully sampled
         */
        public static final int STATE_OK = 2;

        /*
         * State of calib point
         */
        public Integer state = 0;

        /*
         * Coordinates in pixels
         */
        @SerializedName(Protocol.CALIBRESULT_COORDINATES)
        public Point2D coordinates = new Point2D();

        /*
         * Mean estimated coordinates
         */
        @SerializedName(Protocol.CALIBRESULT_MEAN_ESTIMATED_COORDINATES)
        public Point2D meanEstimatedCoords = new Point2D();

        @SerializedName(Protocol.CALIBRESULT_ACCURACIES_DEGREES)
        public Accuracy accuracy = new Accuracy();

        @SerializedName(Protocol.CALIBRESULT_MEAN_ERRORS_PIXELS)
        public MeanError meanError = new MeanError();

        @SerializedName(Protocol.CALIBRESULT_STANDARD_DEVIATION_PIXELS)
        public StandardDeviation standardDeviation = new StandardDeviation();

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            if (!(o instanceof CalibrationPoint))
                return false;

            CalibrationPoint other = (CalibrationPoint) o;

            return state == other.state && coordinates.equals(other.coordinates)
                    && meanEstimatedCoords.equals(other.meanEstimatedCoords) && accuracy.equals(other.accuracy)
                    && meanError.equals(other.meanError) && standardDeviation.equals(other.standardDeviation);
        }

        @Override
        public int hashCode()
        {
            int hash = 157;
            hash = hash * 953 + HashUtils.hash(state);
            hash = hash * 953 + coordinates.hashCode();
            hash = hash * 953 + meanEstimatedCoords.hashCode();
            hash = hash * 953 + accuracy.hashCode();
            hash = hash * 953 + meanError.hashCode();
            hash = hash * 953 + standardDeviation.hashCode();
            return hash;
        }
    }

    public class Accuracy
    {
        /*
         * Accuracy in degrees
         */
        @SerializedName(Protocol.CALIBRESULT_ACCURACY_AVERAGE_DEGREES)
        public Double accuracyDegrees = 0d;

        /*
         * Accuracy in degrees, left eye
         */
        @SerializedName(Protocol.CALIBRESULT_ACCURACY_LEFT_DEGREES)
        public Double accuracyDegreesLeft = 0d;

        /*
         * Accuracy in degrees, right eye
         */
        @SerializedName(Protocol.CALIBRESULT_ACCURACY_RIGHT_DEGREES)
        public Double accuracyDegreesRight = 0d;

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            if (!(o instanceof Accuracy))
                return false;

            Accuracy other = (Accuracy) o;

            return Double.doubleToLongBits(accuracyDegrees) == Double.doubleToLongBits(other.accuracyDegrees)
                    && Double.doubleToLongBits(accuracyDegreesLeft) == Double
                            .doubleToLongBits(other.accuracyDegreesLeft)
                    && Double.doubleToLongBits(accuracyDegreesRight) == Double
                            .doubleToLongBits(other.accuracyDegreesRight);
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = hash * 29 + HashUtils.hash(accuracyDegrees);
            hash = hash * 29 + HashUtils.hash(accuracyDegreesLeft);
            hash = hash * 29 + HashUtils.hash(accuracyDegreesRight);
            return hash;
        }
    }

    public class MeanError
    {
        /*
         * Mean error in pixels
         */
        @SerializedName(Protocol.CALIBRESULT_MEAN_ERROR_AVERAGE_PIXELS)
        public Double meanErrorPixels = 0d;

        /*
         * Mean error in pixels, left eye
         */
        @SerializedName(Protocol.CALIBRESULT_MEAN_ERROR_LEFT_PIXELS)
        public Double meanErrorPixelsLeft = 0d;

        /*
         * Mean error in pixels, right eye
         */
        @SerializedName(Protocol.CALIBRESULT_MEAN_ERROR_RIGHT_PIXELS)
        public Double meanErrorPixelsRight = 0d;

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            if (!(o instanceof MeanError))
                return false;

            MeanError other = (MeanError) o;

            return Double.doubleToLongBits(meanErrorPixels) == Double.doubleToLongBits(other.meanErrorPixels)
                    && Double.doubleToLongBits(meanErrorPixelsLeft) == Double
                            .doubleToLongBits(other.meanErrorPixelsLeft)
                    && Double.doubleToLongBits(meanErrorPixelsRight) == Double
                            .doubleToLongBits(other.meanErrorPixelsRight);
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = hash * 37 + HashUtils.hash(meanErrorPixels);
            hash = hash * 37 + HashUtils.hash(meanErrorPixelsLeft);
            hash = hash * 37 + HashUtils.hash(meanErrorPixelsRight);
            return hash;
        }
    }

    public class StandardDeviation
    {
        /*
         * Average std deviation in pixels
         */
        @SerializedName(Protocol.CALIBRESULT_STANDARD_DEVIATION_AVERAGE_PIXELS)
        public Double averageStandardDeviationPixels = 0d;

        /*
         * Average std deviation in pixels, left eye
         */
        @SerializedName(Protocol.CALIBRESULT_STANDARD_DEVIATION_LEFT_PIXELS)
        public Double averageStandardDeviationPixelsLeft = 0d;

        /*
         * Average std deviation in pixels, right eye
         */
        @SerializedName(Protocol.CALIBRESULT_STANDARD_DEVIATION_RIGHT_PIXELS)
        public Double averageStandardDeviationPixelsRight = 0d;

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            if (!(o instanceof StandardDeviation))
                return false;

            StandardDeviation other = (StandardDeviation) o;

            return Double.doubleToLongBits(averageStandardDeviationPixels) == Double
                    .doubleToLongBits(other.averageStandardDeviationPixels)
                    && Double.doubleToLongBits(averageStandardDeviationPixelsLeft) == Double
                            .doubleToLongBits(other.averageStandardDeviationPixelsLeft)
                    && Double.doubleToLongBits(averageStandardDeviationPixelsRight) == Double
                            .doubleToLongBits(other.averageStandardDeviationPixelsRight);
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = hash * 19 + HashUtils.hash(averageStandardDeviationPixels);
            hash = hash * 19 + HashUtils.hash(averageStandardDeviationPixelsLeft);
            hash = hash * 19 + HashUtils.hash(averageStandardDeviationPixelsRight);
            return hash;
        }
    }
}

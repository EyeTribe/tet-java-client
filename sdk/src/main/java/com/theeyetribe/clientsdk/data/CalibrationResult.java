/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk.data;

import com.google.gson.annotations.SerializedName;
import com.theeyetribe.clientsdk.Protocol;
import com.theeyetribe.clientsdk.utils.HashUtils;

import java.util.Arrays;

/**
 * The outcome of a completed calibration process.
 * <p>
 * The class holds detailed information about the calibration result and states the quality of a calibration and
 * if certain calibration points needs resampling.
 *
 * @see <a href="http://dev.theeyetribe.com/api/#cat_calib">EyeTribe API - Calibration</a>
 */
public class CalibrationResult
{
    /*
     * Was the calibration successful?
     */
    public boolean result = false;

    /*
     * Average error in degrees
     */
    @SerializedName(Protocol.CALIBRESULT_AVERAGE_ERROR_DEGREES)
    public float averageErrorDegree = 0f;

    /*
     * Average error in degrees, left eye
     */
    @SerializedName(Protocol.CALIBRESULT_AVERAGE_ERROR_LEFT_DEGREES)
    public float averageErrorDegreeLeft = 0f;

    /*
     * Average error in degrees, right eye
     */
    @SerializedName(Protocol.CALIBRESULT_AVERAGE_ERROR_RIGHT_DEGREES)
    public float averageErrorDegreeRight = 0f;

    /*
     * Complete list of calibration points
     */
    public CalibrationPoint[] calibpoints = new CalibrationPoint[]{};

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof CalibrationResult))
            return false;

        CalibrationResult other = (CalibrationResult) o;

        return
            this.result == other.result &&
            Float.compare(this.averageErrorDegree, other.averageErrorDegree) == 0 &&
            Float.compare(this.averageErrorDegreeLeft, other.averageErrorDegreeLeft) == 0 &&
            Float.compare(this.averageErrorDegreeRight, other.averageErrorDegreeRight) == 0 &&
            Arrays.equals(this.calibpoints, other.calibpoints);
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

    public static class CalibrationPoint
    {
        /*
         * State defines that no data is available for calibration point
         */
        public static final int STATE_NO_DATA = 0;
        /*
         * State defines that calibration point should be re-sampled
         */
        public static final int STATE_RESAMPLE = 1;
        /*
         * State defines that calibration point was successfully sampled
         */
        public static final int STATE_OK = 2;

        /*
         * State of calib point
         */
        public int state = 0;

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

            return
                this.state == other.state &&
                this.coordinates.equals(other.coordinates) &&
                this.meanEstimatedCoords.equals(other.meanEstimatedCoords) &&
                this.accuracy.equals(other.accuracy) &&
                this.meanError.equals(other.meanError) &&
                this.standardDeviation.equals(other.standardDeviation);
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

    /**
     * Accuracy holds detailed information about calibration accuracy in degrees
     */
    public static class Accuracy
    {
        /*
         * Accuracy in degrees
         */
        @SerializedName(Protocol.CALIBRESULT_ACCURACY_AVERAGE_DEGREES)
        public float accuracyDegrees = 0f;

        /*
         * Accuracy in degrees, left eye
         */
        @SerializedName(Protocol.CALIBRESULT_ACCURACY_LEFT_DEGREES)
        public float accuracyDegreesLeft = 0f;

        /*
         * Accuracy in degrees, right eye
         */
        @SerializedName(Protocol.CALIBRESULT_ACCURACY_RIGHT_DEGREES)
        public float accuracyDegreesRight = 0f;

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            if (!(o instanceof Accuracy))
                return false;

            Accuracy other = (Accuracy) o;

            return
                Float.compare(this.accuracyDegrees, other.accuracyDegrees) == 0 &&
                Float.compare(this.accuracyDegreesLeft, other.accuracyDegreesLeft) == 0 &&
                Float.compare(this.accuracyDegreesRight, other.accuracyDegreesRight) == 0;
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

    /**
     * MeanError holds detailed information about calibration mean error in pixels
     */
    public static class MeanError
    {
        /*
         * Mean error in pixels
         */
        @SerializedName(Protocol.CALIBRESULT_MEAN_ERROR_AVERAGE_PIXELS)
        public float meanErrorPixels = 0f;

        /*
         * Mean error in pixels, left eye
         */
        @SerializedName(Protocol.CALIBRESULT_MEAN_ERROR_LEFT_PIXELS)
        public float meanErrorPixelsLeft = 0f;

        /*
         * Mean error in pixels, right eye
         */
        @SerializedName(Protocol.CALIBRESULT_MEAN_ERROR_RIGHT_PIXELS)
        public float meanErrorPixelsRight = 0f;

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            if (!(o instanceof MeanError))
                return false;

            MeanError other = (MeanError) o;

            return
                Float.compare(this.meanErrorPixels, other.meanErrorPixels) == 0 &&
                Float.compare(this.meanErrorPixelsLeft, other.meanErrorPixelsLeft) == 0 &&
                Float.compare(this.meanErrorPixelsRight, other.meanErrorPixelsRight) == 0;
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

    /**
     * StandardDeviation holds detailed information about calibration standard deviation in pixels
     */
    public static class StandardDeviation
    {
        /*
         * Average std deviation in pixels
         */
        @SerializedName(Protocol.CALIBRESULT_STANDARD_DEVIATION_AVERAGE_PIXELS)
        public float averageStandardDeviationPixels = 0f;

        /*
         * Average std deviation in pixels, left eye
         */
        @SerializedName(Protocol.CALIBRESULT_STANDARD_DEVIATION_LEFT_PIXELS)
        public float averageStandardDeviationPixelsLeft = 0f;

        /*
         * Average std deviation in pixels, right eye
         */
        @SerializedName(Protocol.CALIBRESULT_STANDARD_DEVIATION_RIGHT_PIXELS)
        public float averageStandardDeviationPixelsRight = 0f;

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            if (!(o instanceof StandardDeviation))
                return false;

            StandardDeviation other = (StandardDeviation) o;

            return
                Float.compare(this.averageStandardDeviationPixels, other.averageStandardDeviationPixels) == 0 &&
                Float.compare(this.averageStandardDeviationPixelsLeft, other.averageStandardDeviationPixelsLeft) == 0 &&
                Float.compare(this.averageStandardDeviationPixelsRight, other.averageStandardDeviationPixelsRight) == 0;
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

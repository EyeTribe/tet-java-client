/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.client.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.theeyetribe.client.Protocol;
import com.theeyetribe.client.utils.HashUtils;

/**
 * Contains eye tracking results of a single frame. It holds a state that defines the quality of the current tracking
 * and fine grained tracking details down to eye level.
 */
public class GazeData
{
    /**
     * Set when engine is calibrated and glint tracking successfully.
     */
    public static final int STATE_TRACKING_GAZE = 1;

    /**
     * Set when engine has detected eyes.
     */
    public static final int STATE_TRACKING_EYES = 1 << 1;

    /**
     * Set when engine has detected either face, eyes or glint.
     */
    public static final int STATE_TRACKING_PRESENCE = 1 << 2;

    /**
     * Set when tracking failed in the last process frame.
     */
    public static final int STATE_TRACKING_FAIL = 1 << 3;

    /**
     * Set when tracking has failed consecutively over a period of time defined by engine.
     */
    public static final int STATE_TRACKING_LOST = 1 << 4;

    public Integer state = 0;

    @SerializedName(Protocol.FRAME_TIME)
    public Long timeStamp = 0l;

    @SerializedName(Protocol.FRAME_TIMESTAMP)
    public String timeStampString;

    @SerializedName(Protocol.FRAME_RAW_COORDINATES)
    public Point2D rawCoordinates = new Point2D();

    @SerializedName(Protocol.FRAME_AVERAGE_COORDINATES)
    public Point2D smoothedCoordinates = new Point2D();

    @SerializedName(Protocol.FRAME_LEFT_EYE)
    public Eye leftEye = new Eye();

    @SerializedName(Protocol.FRAME_RIGHT_EYE)
    public Eye rightEye = new Eye();

    @SerializedName(Protocol.FRAME_FIXATION)
    public Boolean isFixated = false;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public GazeData()
    {
        timeStamp = System.currentTimeMillis();

        Date date = new Date(timeStamp);
        timeStampString = sdf.format(date);
    }

    public GazeData(GazeData other)
    {
        this.state = other.state;
        this.timeStamp = other.timeStamp;
        this.timeStampString = other.timeStampString;

        this.rawCoordinates = new Point2D(other.rawCoordinates);
        this.smoothedCoordinates = new Point2D(other.smoothedCoordinates);

        this.leftEye = new Eye(other.leftEye);
        this.rightEye = new Eye(other.rightEye);

        this.isFixated = new Boolean(other.isFixated);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof GazeData)
        {
            GazeData other = (GazeData) o;

            return this.state.intValue() == other.state.intValue()
                    && this.timeStamp.longValue() == other.timeStamp.longValue()
                    && this.timeStampString.equals(other.timeStampString)
                    && this.rawCoordinates.equals(other.rawCoordinates)
                    && this.smoothedCoordinates.equals(other.smoothedCoordinates) && this.leftEye.equals(other.leftEye)
                    && this.rightEye.equals(other.rightEye)
                    && this.isFixated.booleanValue() == other.isFixated.booleanValue();
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 2039;
        hash = hash * 1553 + HashUtils.hash(state);
        hash = hash * 1553 + HashUtils.hash(timeStamp);
        hash = hash * 1553 + timeStampString.hashCode();
        hash = hash * 1553 + rawCoordinates.hashCode();
        hash = hash * 1553 + smoothedCoordinates.hashCode();
        hash = hash * 1553 + leftEye.hashCode();
        hash = hash * 1553 + rightEye.hashCode();
        hash = hash * 1553 + HashUtils.hash(isFixated);
        return hash;
    }

    public void set(GazeData other)
    {
        this.state = other.state;
        this.timeStamp = other.timeStamp;
        this.timeStampString = other.timeStampString;

        this.rawCoordinates = new Point2D(other.rawCoordinates);
        this.smoothedCoordinates = new Point2D(other.smoothedCoordinates);

        this.leftEye = new Eye(other.leftEye);
        this.rightEye = new Eye(other.rightEye);

        this.isFixated = new Boolean(other.isFixated);
    }

    public String stateToString()
    {
        String stateString = "";
        boolean ticker = false;

        if ((STATE_TRACKING_GAZE & state) != 0)
        {
            stateString += "STATE_TRACKING_GAZE";
            ticker = true;
        }

        if ((STATE_TRACKING_GAZE & state) != 0)
        {
            stateString += (ticker ? " | " : "") + "STATE_TRACKING_EYES";
            ticker = true;
        }

        if ((STATE_TRACKING_PRESENCE & state) != 0)
        {
            stateString += (ticker ? " | " : "") + "STATE_TRACKING_PRESENCE";
            ticker = true;
        }

        if ((STATE_TRACKING_FAIL & state) != 0)
        {
            stateString += (ticker ? " | " : "") + "STATE_TRACKING_FAIL";
            ticker = true;
        }

        if ((STATE_TRACKING_LOST & state) != 0)
        {
            stateString += (ticker ? " | " : "") + "STATE_TRACKING_LOST";
            ticker = true;
        }

        return stateString;
    }

    private final int NO_TRACKING_MASK = STATE_TRACKING_LOST | STATE_TRACKING_FAIL;

    public boolean hasSmoothedGazeCoordinates()
    {
        return (state & NO_TRACKING_MASK) == 0 && smoothedCoordinates.x != 0 && smoothedCoordinates.y != 0;
    }

    public boolean hasRawGazeCoordinates()
    {
        return (state & NO_TRACKING_MASK) == 0 && rawCoordinates.x != 0 && rawCoordinates.y != 0;
    }

    /**
     * Contains tracking results of a single eye.
     */
    public class Eye
    {
        @SerializedName(Protocol.FRAME_RAW_COORDINATES)
        public Point2D rawCoordinates = new Point2D();

        @SerializedName(Protocol.FRAME_AVERAGE_COORDINATES)
        public Point2D smoothedCoordinates = new Point2D();

        @SerializedName(Protocol.FRAME_EYE_PUPIL_CENTER)
        public Point2D pupilCenterCoordinates = new Point2D();

        @SerializedName(Protocol.FRAME_EYE_PUPIL_SIZE)
        public Double pupilSize = 0d;

        public Eye()
        {
        }

        public Eye(Eye other)
        {
            this.rawCoordinates = new Point2D(other.rawCoordinates);
            this.smoothedCoordinates = new Point2D(other.smoothedCoordinates);
            this.pupilCenterCoordinates = new Point2D(other.pupilCenterCoordinates);
            this.pupilSize = new Double(other.pupilSize);
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof Eye)
            {
                Eye other = (Eye) o;

                return this.rawCoordinates.equals(other.rawCoordinates)
                        && this.smoothedCoordinates.equals(other.smoothedCoordinates)
                        && this.pupilCenterCoordinates.equals(other.pupilCenterCoordinates)
                        && Double.doubleToLongBits(this.pupilSize) == Double.doubleToLongBits(other.pupilSize);
            }

            return false;
        }

        @Override
        public int hashCode()
        {
            int hash = 337;
            hash = hash * 797 + rawCoordinates.hashCode();
            hash = hash * 797 + smoothedCoordinates.hashCode();
            hash = hash * 797 + pupilCenterCoordinates.hashCode();
            hash = hash * 797 + HashUtils.hash(pupilSize);
            return hash;
        }
    }
}

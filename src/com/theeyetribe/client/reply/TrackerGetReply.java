/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.client.reply;

import com.google.gson.annotations.SerializedName;
import com.theeyetribe.client.Protocol;
import com.theeyetribe.client.data.CalibrationResult;
import com.theeyetribe.client.data.GazeData;

public class TrackerGetReply extends ReplyBase
{
    public class Values
    {
        public Boolean push;
        @SerializedName(Protocol.TRACKER_HEARTBEATINTERVAL)
        public Integer heartbeatInterval;
        @SerializedName(Protocol.TRACKER_VERSION)
        public Integer version;
        @SerializedName(Protocol.TRACKER_ISCALIBRATING)
        public Boolean isCalibrating;
        @SerializedName(Protocol.TRACKER_ISCALIBRATED)
        public Boolean isCalibrated;
        @SerializedName(Protocol.CALIBRATION_CALIBRESULT)
        public CalibrationResult calibrationResult;
        @SerializedName(Protocol.TRACKER_SCREEN_INDEX)
        public Integer screenIndex;
        @SerializedName(Protocol.TRACKER_SCREEN_RESOLUTION_WIDTH)
        public Integer screenResolutionWidth;
        @SerializedName(Protocol.TRACKER_SCREEN_RESOLUTION_HEIGHT)
        public Integer screenResolutionHeight;
        @SerializedName(Protocol.TRACKER_SCREEN_PHYSICAL_WIDTH)
        public Float screenPhysicalWidth;
        @SerializedName(Protocol.TRACKER_SCREEN_PHYSICAL_HEIGHT)
        public Float screenPhysicalHeight;
        @SerializedName(Protocol.TRACKER_TRACKERSTATE)
        public Integer trackerState;
        @SerializedName(Protocol.TRACKER_FRAMERATE)
        public Integer frameRate;
        public GazeData frame;
    }

    public Values values = new Values();
}

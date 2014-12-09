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

public class CalibrationPointEndReply extends ReplyBase
{
    public class Values
    {
        @SerializedName(Protocol.CALIBRATION_CALIBRESULT)
        public CalibrationResult calibrationResult;
    }

    public Values values = new Values();
}

/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk.response;

import com.google.gson.annotations.SerializedName;
import com.theeyetribe.clientsdk.Protocol;
import com.theeyetribe.clientsdk.data.CalibrationResult;

/**
 * CalibrationPointEndResponse is the response to a 'pointend' request of the 'calibation' category in the
 * EyeTribe API.
 *
 * @see <a href="http://dev.theeyetribe.com/api/#cat_calib">EyeTribe API - Calibration</a>
 */
public class CalibrationPointEndResponse extends Response
{
    public static class Values
    {
        @SerializedName(Protocol.CALIBRATION_CALIBRESULT)
        public CalibrationResult calibrationResult;
    }

    public Values values = new Values();
}

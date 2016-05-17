/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk.request;

import com.theeyetribe.clientsdk.Protocol;
import com.theeyetribe.clientsdk.response.Response;

/**
 * CalibrationStartRequest represents a 'pointend' request of the 'calibation' category in the EyeTribe API
 *
 * @see <a href="http://dev.theeyetribe.com/api/#cat_calib">EyeTribe API - Calibration</a>
 */
public class CalibrationStartRequest extends Request<Response>
{
    public static class Values
    {
        public Integer pointcount;
    }

    public Values values = new Values();

    public CalibrationStartRequest()
    {
        super(Response.class);

        this.category = Protocol.CATEGORY_CALIBRATION;
        this.request = Protocol.CALIBRATION_REQUEST_START;
    }
}

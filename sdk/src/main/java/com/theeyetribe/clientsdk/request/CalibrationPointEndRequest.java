/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk.request;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.theeyetribe.clientsdk.Protocol;
import com.theeyetribe.clientsdk.response.CalibrationPointEndResponse;

/**
 * CalibrationPointEndRequest represents a 'pointstart' request of the 'calibation' category in the EyeTribe API
 *
 * @see <a href="http://dev.theeyetribe.com/api/#cat_calib">EyeTribe API - Calibration</a>
 */
public class CalibrationPointEndRequest extends Request<CalibrationPointEndResponse>
{
    public String[] values;

    public CalibrationPointEndRequest()
    {
        super(CalibrationPointEndResponse.class);

        this.category = Protocol.CATEGORY_CALIBRATION;
        this.request = Protocol.CALIBRATION_REQUEST_POINTEND;
    }

    @Override
    public CalibrationPointEndResponse parseJsonResponse(JsonObject response, Gson gson)
    {
        CalibrationPointEndResponse cper = super.parseJsonResponse(response, gson);

        if (!response.has(Protocol.KEY_VALUES) || !((JsonObject) response.get(Protocol.KEY_VALUES)).has(Protocol.CALIBRATION_CALIBRESULT))
            cper.values.calibrationResult = null;

        return cper;
    }
}

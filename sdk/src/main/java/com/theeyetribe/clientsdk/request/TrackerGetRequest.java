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
import com.theeyetribe.clientsdk.response.TrackerGetResponse;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TrackerGetRequest represents a 'get' request of the 'tracker' category in the EyeTribe API
 *
 * @see <a href="http://dev.theeyetribe.com/api/#cat_tracker">EyeTribe API - Tracker</a>
 */
public class TrackerGetRequest extends Request<TrackerGetResponse>
{
    public String[] values;

    private static transient SimpleDateFormat sdf;

    public TrackerGetRequest()
    {
        super(TrackerGetResponse.class);

        this.category = Protocol.CATEGORY_TRACKER;
        this.request = Protocol.TRACKER_REQUEST_GET;
    }

    @Override
    public TrackerGetResponse parseJsonResponse(JsonObject response, Gson gson)
    {
        TrackerGetResponse tgr = super.parseJsonResponse(response, gson);

        if (!response.has(Protocol.KEY_VALUES) || !((JsonObject) response.get(Protocol.KEY_VALUES)).has(Protocol.TRACKER_CALIBRATIONRESULT))
            tgr.values.calibrationResult = null;

        if (!response.has(Protocol.KEY_VALUES) || !((JsonObject) response.get(Protocol.KEY_VALUES)).has(Protocol.TRACKER_FRAME))
        {
            tgr.values.frame = null;
        }
        else
        {
            // fixing timestamp based on string
            // representation, Json 32bit int issue
            // TODO: This will eventually be done serverside

            if(null == sdf)
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

            if (null != tgr.values.frame.timeStampString
                    && !tgr.values.frame.timeStampString.isEmpty()) {
                Date date;
                try {
                    synchronized (sdf)
                    {
                        date = sdf.parse(tgr.values.frame.timeStampString);
                    }
                    tgr.values.frame.timeStamp = date.getTime(); // UTC
                } catch (Exception e) {
                    // consume error
                }
            }
        }

        return tgr;
    }
}

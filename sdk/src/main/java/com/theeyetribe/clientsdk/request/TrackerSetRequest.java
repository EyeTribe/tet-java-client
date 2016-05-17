/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk.request;

import com.google.gson.annotations.SerializedName;
import com.theeyetribe.clientsdk.Protocol;
import com.theeyetribe.clientsdk.response.Response;

/**
 * TrackerSetRequest represents a 'set' request of the 'tracker' category in the EyeTribe API
 *
 * @see <a href="http://dev.theeyetribe.com/api/#cat_tracker">EyeTribe API - Tracker</a>
 */
public class TrackerSetRequest extends Request<Response>
{
    public static class Values
    {
        public Integer version;
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
    }

    public Values values = new Values();

    public TrackerSetRequest()
    {
        super(Response.class);

        this.category = Protocol.CATEGORY_TRACKER;
        this.request = Protocol.TRACKER_REQUEST_SET;
    }
}

/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.client.request;

import com.google.gson.annotations.SerializedName;
import com.theeyetribe.client.Protocol;

public class TrackerSetRequest extends RequestBase
{
    public class Values
    {
        public Boolean push;
        public Integer version;
        @SerializedName(Protocol.TRACKER_SCREEN_INDEX)
        public Integer screenIndex;
        @SerializedName(Protocol.TRACKER_SCREEN_RESOLUTION_WIDTH)
        public Integer screenResulutionWidth;
        @SerializedName(Protocol.TRACKER_SCREEN_RESOLUTION_HEIGHT)
        public Integer screenResulutionHeight;
        @SerializedName(Protocol.TRACKER_SCREEN_PHYSICAL_WIDTH)
        public Float screenPhysicalWidth;
        @SerializedName(Protocol.TRACKER_SCREEN_PHYSICAL_HEIGHT)
        public Float screenPhysicalHeight;
    }

    public Values values = new Values();

    public TrackerSetRequest()
    {
        this.category = Protocol.CATEGORY_TRACKER;
        this.request = Protocol.TRACKER_REQUEST_SET;
    }
}

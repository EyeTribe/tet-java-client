/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.client.request;

import com.theeyetribe.client.Protocol;

public class TrackerGetRequest extends RequestBase
{
    public String[] values;

    public TrackerGetRequest()
    {
        this.category = Protocol.CATEGORY_TRACKER;
        this.request = Protocol.TRACKER_REQUEST_GET;
    }
}

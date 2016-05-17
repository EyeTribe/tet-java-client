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

/**
 * ResponseFailed is the responses to a failed request in the EyeTribe API
 *
 * @see <a href="http://dev.theeyetribe.com/api/#api">EyeTribe API - Client Message</a>
 */
public class ResponseFailed extends Response
{
    public static class Values
    {
        @SerializedName(Protocol.KEY_STATUSMESSAGE)
        public String statusMessage;
    }

    public Values values = new Values();
}

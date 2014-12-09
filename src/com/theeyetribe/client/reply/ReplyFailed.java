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

public class ReplyFailed extends ReplyBase
{
    public class Values
    {
        @SerializedName(Protocol.KEY_STATUSMESSAGE)
        public String statusMessage;
    }

    public Values values = new Values();
}

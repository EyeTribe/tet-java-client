/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk.response;

/**
 * Response is the base class for requests responses in the EyeTribe API
 *
 * @see <a href="http://dev.theeyetribe.com/api/#api">EyeTribe API - Client Message</a>
 */
public class Response
{
    public String category;
    public String request;
    public int id;
    public int statuscode;

    public transient long transitTime;
}

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
import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.utils.HashUtils;

/**
 * Request is the generic base class for requests in the EyeTribe API
 *
 * @see <a href="http://dev.theeyetribe.com/api/#api">EyeTribe API - Client Message</a>
 */
public class Request<T> implements Comparable<Request<T>>
{
    public String category;
    public String request;
    public int id;

    private transient boolean mCanceled;

    public transient long timeStamp;

    public transient int retryAttempts;

    public transient Object asyncLock;

    private transient final Class<T> type;

    public Request(Class<T> type) {
        this.type = type;
    }

    public T parseJsonResponse(JsonObject response, Gson gson)
    {
        if(GazeManager.IS_DEBUG_MODE)
            System.out.println("parseJsonResponse: " + type.getSimpleName());

        return gson.fromJson(response, type);
    }

    public String toJsonString(Gson gson)
    {
        if(GazeManager.IS_DEBUG_MODE)
            System.out.println("toJsonString: " + this.getClass().getSimpleName());

        return gson.toJson(this, this.getClass());
    }

    public void cancel()
    {
        mCanceled = true;
        finish();
    }

    public boolean isCancelled()
    {
        return mCanceled;
    }

    public void finish()
    {
        if(null != asyncLock)
        {
            synchronized (asyncLock)
            {
                asyncLock.notify();
            }
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof Request))
            return false;

        Request other = (Request) o;

        return category.equals(other.category) &&
                request.equals(other.request) &&
                id == other.id;
    }

    @Override
    public int hashCode()
    {
        int hash = 1471;
        hash = hash * 1151  + category.hashCode();
        hash = hash * 1151  + request.hashCode();
        hash = hash * 1151  + HashUtils.hash(id);
        return hash;
    }

    @Override
    public int compareTo(Request other)
    {
        if(this.equals(other))
            return 0;

        if(this.id != 0 && other.id == 0)
            return -1;

        if(this.id == 0 && other.id != 0)
            return 1;

        //if(this.id != 0 && other.id != 0)
            return this.id < other.id ? -1 : 1;
    }
}

/*
 * Copyright (c) 2013-present, The Eye Tribe.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree.
 *
 */

package com.theeyetribe.javafx.utils;

import com.theeyetribe.clientsdk.data.GazeData;

/**
 * Extending GazeDataDeque to support calculation of frame rate
 */
public class FrameRateGazeDataDeque extends GazeDataDeque
{
    private static final long BUFFER_SIZE_MILLIS = 5000;

    public FrameRateGazeDataDeque()
    {
        super(BUFFER_SIZE_MILLIS);
    }

    public FrameRateGazeDataDeque(long timeLimit)
    {
        super(timeLimit);
    }

    public float getAvgFramesPerSecond()
    {
        float avgMillis;
        if((avgMillis = getAvgMillisFrame()) > 0 )
            return 1000f / avgMillis;

        return -1;
    }

    public float getAvgMillisFrame()
    {
        GazeData first = peekFirst();
        GazeData last = peekLast();

        if(null != first && null != last)
        {
            float delta = first.timeStamp - last.timeStamp;

            // only return value when buffer populated
            if (delta > (timeLimit >> 1))
                return delta / size();
        }

        return -1;
    }
}

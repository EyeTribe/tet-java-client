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
    public FrameRateGazeDataDeque(long timeLimit)
    {
        super(timeLimit);
    }

    public float getAvgFramesPerSecond()
    {
        float avgMillis;
        if((avgMillis = getAvgMillisFrame()) > 0 )
            return (float)1000/avgMillis;

        return -1;
    }

    public float getAvgMillisFrame()
    {
        GazeData first = peekFirst();
        GazeData last = peekLast();

        if(null != first && null != last)
        {
            float delta = first.timeStamp - last.timeStamp;
            return delta / size();
        }

        return -1;
    }
}

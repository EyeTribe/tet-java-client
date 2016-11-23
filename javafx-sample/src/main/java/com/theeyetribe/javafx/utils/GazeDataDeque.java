/*
 * Copyright (c) 2013-present, The Eye Tribe.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree.
 *
 */

package com.theeyetribe.javafx.utils;

import com.theeyetribe.clientsdk.data.GazeData;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Cache of latest valid GazeData objects. Based on a time limit, the deque
 * size is moderated as new items are added.
 * 
 * {@code
 * //adding new objects
 * while(!fpsQueue.offerFirst(gazeData))
 *     fpsQueue.pollLast();
 * }
 */
public class GazeDataDeque extends LinkedBlockingDeque<GazeData>
{
    private static final long serialVersionUID = -7224237939138791310L;
    protected long timeLimit;
    
    public GazeDataDeque(long timeLimit)
    {
        super();
        this.timeLimit = timeLimit;
    }

    /**
     * Offer latest GazeData object to deque. Delta time decides if 'old' deque
     * elements should be removed before adding new.
     *
     * @param e
     * @return
     */
    @Override
    public boolean offerFirst(GazeData e) 
    {
        GazeData last = peekLast();

        if(null != last && (e.timeStamp - last.timeStamp) > timeLimit)
            return false;
        else
            return super.offerFirst(e);
    }
    
    public void setTimeLimit(long timeLimit)
    {
        this.timeLimit = timeLimit;
    }
}

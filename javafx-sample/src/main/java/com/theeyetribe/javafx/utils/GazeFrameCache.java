/*
 * Copyright (c) 2013-present, The Eye Tribe.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree.
 *
 */

package com.theeyetribe.javafx.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.data.GazeData.Eye;
import com.theeyetribe.clientsdk.data.Point2D;
import com.theeyetribe.clientsdk.data.Point3D;
import com.theeyetribe.clientsdk.utils.GazeUtils;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * Utility class that maintains a run-time cache of GazeData frames.
 * Based on this cache, the class analyzes the frame history and finds the currently
 * valid gaze data.
 * <p/>
 * Use this class to avoid the 'glitch' effect of occasional poor tracking in the
 * data stream.
 */
public class GazeFrameCache
{
    public final static int DEFAULT_CACHE_TIME_FRAME_MILLIS = 500;
    private final static int NO_TRACKING_MASK = GazeData.STATE_TRACKING_FAIL | GazeData.STATE_TRACKING_LOST;

    private float mMinEyesDistance = 0.1f;
    private float mMaxEyesDistance = 0.4f;

    protected GazeDataDeque mFrames;

    protected Eye mLastLeftEye;
    protected Eye mLastRightEye;

    protected Point2D mLastRawGazeCoords = new Point2D();
    protected Point2D mLastSmoothedGazeCoords = new Point2D();

    protected Point3D mLastUserPosition = new Point3D();

    protected double mLastEyeAngle;

    protected long mUserPosTimeStamp = -1;

    protected long mFrameTimeStamp = System.currentTimeMillis();
    protected long mFrameDelta = 50; //default value, 30 fps

    protected Predicate<GazeData> mValidFramePredicate;
    protected boolean mShouldUpdate;

    //internals
    private Point2D mLastEyesVecHalf = new Point2D(.2f,0f);
    private float mLastEyeDistance = (mMaxEyesDistance - mMinEyesDistance) / mMaxEyesDistance;

    public static GazeFrameCache getInstance()
    {
        return Holder.INSTANCE;
    }

    private static class Holder {
        //thread-safe initialization on demand
        static final GazeFrameCache INSTANCE = new GazeFrameCache(DEFAULT_CACHE_TIME_FRAME_MILLIS);
    }

    private GazeFrameCache(int timeLimit)
    {
        mFrames = new GazeDataDeque(timeLimit);
        mValidFramePredicate = new ValidFrame();

        //init user distance values
        mLastEyesVecHalf = new Point2D(.2f, 0f);
        mLastEyeDistance = 1f - ((mMinEyesDistance + ((mMaxEyesDistance - mMinEyesDistance) * .5f)) / mMaxEyesDistance);
        mLastUserPosition = new Point3D(GazeManager.getInstance().getScreenResolutionWidth() >> 1, GazeManager.getInstance().getScreenResolutionHeight() >> 1, mLastEyeDistance);
    }

    public void setTimeLimit(int timeLimit)
    {
        mFrames.setTimeLimit(timeLimit);
    }

    public synchronized void update(@Nonnull GazeData frame)
    {
        //only update if not contained already
        if(mFrames.contains(frame))
            return;

        //set delta based on continuous stream and not valid frames only
        long now = System.currentTimeMillis();
        mFrameDelta = now - mFrameTimeStamp;
        mFrameTimeStamp = now;

        //remove deque elements until condition fulfilled
        while(!mFrames.offerFirst(frame))
            mFrames.pollLast();

        // update valid gazedata based on store
        Eye right = null, left = null;
        Point2D gazeCoords = null;
        Point2D gazeCoordsSmooth = null;
        Point2D userPos = null;
        double userDist = 0f;
        Point2D eyeDistVecHalf = null;
        GazeData gd;
        Iterable<GazeData> valid = Iterables.filter(mFrames, mValidFramePredicate);
        Iterator<GazeData> iter = valid.iterator();

        while(iter.hasNext())
        {
            gd = iter.next();

            if (null == userPos &&
                    !gd.leftEye.pupilCenterCoordinates.equals(Point2D.ZERO) &&
                    !gd.rightEye.pupilCenterCoordinates.equals(Point2D.ZERO))
            {
                userPos = gd.leftEye.pupilCenterCoordinates.add(gd.rightEye.pupilCenterCoordinates).divide(2);
                eyeDistVecHalf = gd.rightEye.pupilCenterCoordinates.subtract(gd.leftEye.pupilCenterCoordinates).divide(2);
                userDist = GazeUtils.getDistancePoint2D(gd.leftEye.pupilCenterCoordinates, gd.rightEye.pupilCenterCoordinates);

                left = gd.leftEye;
                right = gd.rightEye;
            }
            else if (null == userPos && left == null && !gd.leftEye.pupilCenterCoordinates.equals(Point2D.ZERO))
            {
                left = gd.leftEye;
            }
            else if (null == userPos && right == null && !gd.rightEye.pupilCenterCoordinates.equals(Point2D.ZERO))
            {
                right = gd.rightEye;
            }

            // if gaze coordinates available, cache both raw and smoothed
            if (null == gazeCoords && !gd.rawCoordinates.equals(Point2D.ZERO))
            {
                gazeCoords = gd.rawCoordinates;
                gazeCoordsSmooth = gd.smoothedCoordinates;
            }

            // break loop if valid values found
            if (null != userPos && null != gazeCoords)
                break;
        }

        mLastRawGazeCoords = gazeCoords;
        mLastSmoothedGazeCoords = gazeCoordsSmooth;

        if(null != eyeDistVecHalf && !mLastEyesVecHalf.equals(eyeDistVecHalf))
            mLastEyesVecHalf = eyeDistVecHalf;

        //Update user position values if needed data is valid
        if (null != userPos)
        {
            mLastLeftEye = left;
            mLastRightEye = right;

            //update 'depth' measure
            if (userDist < mMinEyesDistance)
                mMinEyesDistance = (float)userDist;

            if (userDist > mMaxEyesDistance)
                mMaxEyesDistance = (float)userDist;

            mLastEyeDistance = 1f - ((float)userDist / mMaxEyesDistance);

            //update user position
            mLastUserPosition = new Point3D(userPos.x, userPos.y, mLastEyeDistance);

            //map to normalized 3D space
            mLastUserPosition.x = (mLastUserPosition.x * 2) - 1;
            mLastUserPosition.y = (mLastUserPosition.y * 2) - 1;

            mUserPosTimeStamp = now;

            //update angle
            double dy = mLastRightEye.pupilCenterCoordinates.y - mLastLeftEye.pupilCenterCoordinates.y;
            double dx = mLastRightEye.pupilCenterCoordinates.x - mLastLeftEye.pupilCenterCoordinates.x;
            mLastEyeAngle = ((180 / Math.PI * Math.atan2(GazeManager.getInstance().getScreenResolutionHeight() * dy, GazeManager.getInstance().getScreenResolutionWidth() * dx)));
        }
        else if (null != left && null != left.pupilCenterCoordinates)
        {
            mLastLeftEye = left;
            mLastRightEye = null;
            Point2D newPos = mLastLeftEye.pupilCenterCoordinates.add(mLastEyesVecHalf);
            mLastUserPosition = new Point3D(newPos.x, newPos.y, mLastEyeDistance);

            //map to normalized 3D space
            mLastUserPosition.x = (mLastUserPosition.x * 2) - 1;
            mLastUserPosition.y = (mLastUserPosition.y * 2) - 1;

            mUserPosTimeStamp = now;
        }
        else if (null != right && null != right.pupilCenterCoordinates)
        {
            mLastRightEye = right;
            mLastLeftEye = null;
            Point2D newPos = mLastRightEye.pupilCenterCoordinates.subtract(mLastEyesVecHalf);
            mLastUserPosition = new Point3D(newPos.x, newPos.y, mLastEyeDistance);

            //map to normalized 3D space
            mLastUserPosition.x = (mLastUserPosition.x * 2) - 1;
            mLastUserPosition.y = (mLastUserPosition.y * 2) - 1;

            mUserPosTimeStamp = now;
        }
        else
        {
            mLastRightEye = null;
            mLastLeftEye = null;
        }
    }

    /**
     * Position of user in normalized right-handed 3D space with respect to device. Approximated from position of eyes.
     *
     * @return Normalized 3d position
     */
    public Point3D getLastUserPosition()
    {
        return mLastUserPosition;
    }

    public Eye getLastLeftEye()
    {
        return mLastLeftEye;
    }

    public Eye getLastRightEye()
    {
        return mLastRightEye;
    }

    public double getLastEyesAngle()
    {
        return mLastEyeAngle;
    }

    public Point2D getLastRawGazeCoordinates()
    {
        return mLastRawGazeCoords;
    }

    public Point2D getLastSmoothedGazeCoordinates()
    {
        return mLastSmoothedGazeCoords;
    }

    public long getLastDelta()
    {
        return mFrameDelta;
    }

    public long getLastUserPosTimeStamp()
    {
        return mUserPosTimeStamp;
    }

    public long getLastUserPosDelta()
    {
        return System.currentTimeMillis() - mUserPosTimeStamp;
    }

    public ImmutableList<GazeData> getFrameCache()
    {
        return ImmutableList.copyOf(mFrames);
    }

    public void clear()
    {
        mFrames.clear();
    }

    private class ValidFrame implements Predicate<GazeData>
    {
        @Override
        public boolean apply(GazeData gd)
        {
            return ((gd.state & NO_TRACKING_MASK) == 0);
        }
    }
}
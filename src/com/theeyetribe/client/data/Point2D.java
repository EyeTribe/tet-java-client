/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.client.data;

import com.theeyetribe.client.utils.HashUtils;

/**
 * 2D point with double precision used for gaze control routines
 */
public class Point2D
{
    public double x;
    public double y;

    public static final Point2D ZERO = new Point2D();

    public Point2D()
    {
        x = 0;
        y = 0;
    }

    public Point2D(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public Point2D(Point2D point)
    {
        x = point.x;
        y = point.y;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof Point2D))
            return false;

        Point2D other = (Point2D) o;

        return Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x)
                && Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y);
    }

    @Override
    public int hashCode()
    {
        int hash = 571;
        hash = hash * 2777 + HashUtils.hash(x);
        hash = hash * 2777 + HashUtils.hash(y);
        return hash;
    }

    public Point2D add(Point2D p2)
    {
        return new Point2D(this.x + p2.x, this.y + p2.y);
    }

    public Point2D subtract(Point2D p2)
    {
        return new Point2D(this.x - p2.x, this.y - p2.y);
    }

    public Point2D multiply(Point2D p2)
    {
        return new Point2D(this.x * p2.x, this.y * p2.y);
    }

    public Point2D multiply(double k)
    {
        return new Point2D(this.x * k, this.y * k);
    }

    public Point2D divide(double k)
    {
        return new Point2D(this.x / k, this.y / k);
    }

    public double average()
    {
        return (x + y) / 2;
    }

    @Override
    public String toString()
    {
        return "{" + x + ", " + y + "}";
    }
}

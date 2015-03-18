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
 * 3D point with double precision used for gaze control routines
 */
public class Point3D extends Point2D
{
    public double z;

    public static final Point3D ZERO = new Point3D();

    public Point3D()
    {
        super();
        z = 0;
    }

    public Point3D(double x, double y, double z)
    {
        super(x, y);
        this.z = z;
    }

    public Point3D(Point3D other)
    {
        super(other.x, other.y);
        this.z = other.z;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof Point3D))
            return false;

        Point3D other = (Point3D) o;

        return super.equals(other) && Double.doubleToLongBits(z) == Double.doubleToLongBits(other.z);
    }

    @Override
    public int hashCode()
    {
        int hash = super.hashCode();
        hash = hash * 2777 + HashUtils.hash(z);
        return hash;
    }

    public Point3D add(Point3D p2)
    {
        return new Point3D(this.x + p2.x, this.y + p2.y, this.z + p2.z);
    }

    public Point3D subtract(Point3D p2)
    {
        return new Point3D(this.x - p2.x, this.y - p2.y, this.z - p2.z);
    }

    public Point3D multiply(Point3D p2)
    {
        return new Point3D(this.x * p2.x, this.y * p2.y, this.z * p2.z);
    }

    public Point3D multiply(double k)
    {
        return new Point3D(this.x * k, this.y * k, this.z * k);
    }

    public Point3D divide(double k)
    {
        return new Point3D(this.x / k, this.y / k, this.z / k);
    }

    public double average()
    {
        return (x + y + z) / 3;
    }

    @Override
    public String toString()
    {
        return "{" + x + ", " + y + ", " + z + "}";
    }
}

/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.clientsdk.data;

import com.theeyetribe.clientsdk.utils.HashUtils;

/**
 * 3D point with float precision used for gaze control routines
 */
public class Point3D
{
    public float x;
    public float y;
    public float z;

    public static final float EPSILON = 1e-005f;

    public static final Point3D ZERO = new Point3D();

    public Point3D()
    {
    }

    public Point3D(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D(Point3D other)
    {
        this.x = other.x;
        this.y = other.y;
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

        return
            Float.compare(this.x, other.x) == 0 &&
            Float.compare(this.y, other.y) == 0 &&
            Float.compare(this.z, other.z) == 0;
    }

    @Override
    public int hashCode()
    {
        int hash = 571;
        hash = hash * 2777 + HashUtils.hash(x);
        hash = hash * 2777 + HashUtils.hash(y);
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

    public Point3D multiply(float k)
    {
        return new Point3D(this.x * k, this.y * k, this.z * k);
    }

    public Point3D divide(float k)
    {
        return new Point3D(this.x / k, this.y / k, this.z / k);
    }

    public float average()
    {
        return (x + y + z) / 3;
    }

    @Override
    public String toString()
    {
        return "{" + x + ", " + y + ", " + z + "}";
    }
}

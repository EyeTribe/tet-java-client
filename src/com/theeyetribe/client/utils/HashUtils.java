package com.theeyetribe.client.utils;

/**
 * Helper class for hashcode building with Java 6 compliance
 */
public class HashUtils
{
    public static int hash(Boolean value)
    {
        return value ? 1231 : 1237;
    }

    public static int hash(Integer value)
    {
        long bits = Double.doubleToLongBits(value);
        return (int) (bits ^ (bits >>> 32));
    }

    public static int hash(Long value)
    {
        return value.intValue();
    }

    public static int hash(Float value)
    {
        return (int) Float.floatToIntBits(value);
    }

    public static int hash(Double value)
    {
        long bits = Double.doubleToLongBits(value);
        return (int) (bits ^ (bits >>> 32));
    }
}

package com.theeyetribe.clientsdk.utils;

/**
 * Helper class for hashcode building with Java 6 compliance
 */
public class HashUtils
{
    private HashUtils()
    {
        //ensure non-instantiability
    }

    public static int hash(Boolean value)
    {
        return value ? 1231 : 1237;
    }

    public static int hash(Integer value)
    {
        return value.intValue();
    }

    public static int hash(Long value)
    {
        return (int)(value ^ (value >>> 32));
    }

    public static int hash(Float value)
    {
        return Float.floatToIntBits(value);
    }

    public static int hash(Double value)
    {
        long bits = Double.doubleToLongBits(value);
        return hash(bits);
    }
}

package com.sismics.util;

/**
 * Math utilities.
 *
 * @author jtremeaux
 */
public class MathUtil {
    /**
     * Clip a value between min and max.
     *
     * @param value Value
     * @param min Minimum
     * @param max Maximum
     * @return Clipped value
     */
    public static int clip(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}

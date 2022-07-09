package LifeForLife.common;

import java.util.concurrent.ThreadLocalRandom;

public abstract class GeneralUtils {
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Returns a random number between min (inclusive) and max (exclusive)
     * 
     * @param min
     * @param max
     * @return
     */
    public static int randomRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }
}
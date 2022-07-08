package AnteMatter.common;

public abstract class GeneralUtils {
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }
}

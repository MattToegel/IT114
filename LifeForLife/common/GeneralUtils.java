package LifeForLife.common;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

public abstract class GeneralUtils {
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    // https://stackoverflow.com/a/44838176
    public static int countOccurencesInString(String str, String target) {
        if (str == null || target == null) {
            return 0;
        }
        if (str.trim().length() == 0 || target.trim().length() == 0) {
            return 0;
        }
        // chose to use split here since my incoming str can be regex
        // if I chose the commented out code, regex would cause this action to not
        // return the proper value
        return str.split(target, -1).length - 1; // (str.length() - str.replace(target, "").length()) / target.length();
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

    public static String getRandomHexColor() {
        Color color = new Color(
                GeneralUtils.randomRange(80, 251),
                GeneralUtils.randomRange(80, 251),
                GeneralUtils.randomRange(80, 251));
        return '#' + Integer.toHexString(color.getRGB() & 0xffffff | 0x1000000).substring(1);
    }
}
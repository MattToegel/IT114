package core;

import java.util.Random;

public class Helpers {
    static Random random = new Random();

    public static int clamp(int value, int min, int max) {
	return Math.max(min, Math.min(max, value));
    }

    public static int getNumberBetween(int min, int max) {
	if (min == max) {
	    max++;
	}

	return random.nextInt(max - min) + min;
    }

    public static int getNumberBetweenBySeed(int min, int max, long seed) {
	random.setSeed(seed);
	return random.nextInt(max - min) + min;
    }
}
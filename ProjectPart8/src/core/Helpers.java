package core;

import java.awt.Point;
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

    /**
     * Returns a "normalized" direction between 2 points (This creates garbage if
     * called frequently)
     * 
     * @param target
     * @param original
     * @return a new Point() object containing normalized direction
     */
    public static Point getDirectionBetween(Point target, Point original) {
	int dx = target.x - original.x;
	int dy = target.y - original.y;
	if (dx > 0) {
	    dx = 1;
	}
	else if (dx < 0) {
	    dx = -1;
	}
	else {
	    dx = 0;
	}
	if (dy > 0) {
	    dy = 1;
	}
	else if (dy < 0) {
	    dy = -1;
	}
	else {
	    dy = 0;
	}
	return new Point(dx, dy);
    }
}
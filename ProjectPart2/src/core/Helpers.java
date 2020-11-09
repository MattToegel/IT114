package core;

public class Helpers {
    public static int clamp(int value, int min, int max) {
	return Math.max(min, Math.min(max, value));
    }
}
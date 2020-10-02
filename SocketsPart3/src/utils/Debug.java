package utils;

public class Debug {
	private static boolean isDebug = true;

	public static void setDebug(boolean debug) {
		Debug.isDebug = debug;
	}

	/***
	 * Helper method to sysout messages. Let's us easily disable out sysout by
	 * changing the isDebug flag
	 * 
	 * @param message
	 */
	public static void log(String message) {
		if (!isDebug) {
			return;
		}
		System.out.println(message);
	}
}
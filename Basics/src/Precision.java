
public class Precision {
	public static void main(String[] args) {
		// Here we'll see why it's not a good idea to blindly trust floating point
		// numbers (floats and doubles)
		float a = 1f;

		float b = 0f;
		for (int i = 0; i < 10; i++) {
			b += 0.1f;// shorthand for b = b + 0.1f;
			// System.out.println("B: " + b);
		}
		System.out.println("A equals B?" + (a == b));

		System.out.println("A: " + a);
		System.out.println("B: " + b);
		// let's see the same for doubles
		double x = 1d;
		double y = 0d;
		for (int i = 0; i < 10; i++) {
			y += 0.1d;
		}
		System.out.println("X equals Y?" + (x == y));
		System.out.println("X: " + x);
		System.out.println("Y: " + y);
		// logically mentally evaluating the code it looks like it'll be the same
		// but due to floating point precision majority of the time they'll not match
		// whenever you see something like this, anticipate/expect that the values won't
		// add up due to precision issues
	}
}
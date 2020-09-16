public class IfJava2 {
	public static void main(String[] args) {
		// this doesn't work because java is typed
		// boolean value = null;
		// uncomment this to see why this too doesn't work
		// boolean value = (boolean) null;// <-- this casts the value to a specific type
		// (if possible)
		// here we'll use the Boolean class for our sample
		boolean value = Boolean.parseBoolean(null);
		if (value) {
			System.out.println("Value is true");
		}
		System.out.println("If this is the first message we see, the value wasn't truthy");
		value = Boolean.parseBoolean("0");
		if (value) {
			System.out.println("Value is now true");
		}
		System.out.println("If we didn't see the second value message it wasn't truthy");
		value = Boolean.parseBoolean("true");
		if (value) {
			System.out.println("Value is now true");
		}
		// In your IDE, if it support it, mouseover the "parseBoolean" method to see why
		// the output is as it is.
		// If you don't have that ability check here
		// https://docs.oracle.com/javase/7/docs/api/java/lang/Boolean.html
		// then scroll down to parseBoolean
	}
}
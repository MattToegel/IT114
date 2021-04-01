public class Variables {
	public static void main(String[] args) {
		// This is a comment
		/*
		 * This is a multiline comment
		 */

		// TODO this is something I need to check later
		String name = "Bob";
		char firstLetter = 'B';
		int age = 30;
		float height = 6.0f;
		double doubleHeight = 6.0d;
		boolean isAdult = (age >= 18);

		System.out.println("Hello, meet " + name);
		System.out.println("His name begins with " + firstLetter);
		System.out.println("He is " + age + " years old");
		System.out.println("He is " + height + "' tall");
		System.out.println("Based on the legal age of 18 he is an adult " + isAdult);
	}
}
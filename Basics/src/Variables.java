public class Variables {
	public static void main(String[] args) {
		// Here we'll show the basics of defining all the variable data types we just
		// learn
		String name = "Bob";
		char firstLetter = 'B';
		int age = 30;
		float height = 6.0f;
		boolean isAdult = age >= 18;// sample of assigning a value based on a different variable
		// let's output the details
		System.out.println("Hello, meet " + name);
		System.out.println("His name begins with " + firstLetter);
		System.out.println("He is " + age + " years old");
		System.out.println("He is " + height + "' tall");
		System.out.println("Based on the legal age of 18 he is an adult " + isAdult);
	}
}
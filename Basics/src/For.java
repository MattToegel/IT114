
public class For {
	public static void main(String[] args) {
		// you don't normally declare the increment like this
		// but I'm doing it for sake of example
		int increment = 1;// <--change this value and see how it changes
		for (int i = 0; i < 10; i += increment) {
			System.out.println("i is " + i);
		}
	}
}
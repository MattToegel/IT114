import java.util.Scanner;

public class UserInput {
	public static void main(String[] args) {
		// many ways to do this, but we'll use the Scanner class
		Scanner input = new Scanner(System.in);
		System.out.println("Enter some text then hit enter");
		while (input.hasNext()) {
			String message = input.nextLine();
			System.out.println("You entered " + message);
			if (message.equalsIgnoreCase("quit")) {
				System.out.println("We hear ya loud and clear, good bye.");
				break;
			}
		}
		// any time we work with IO or anything that implements a closable we must close
		// it
		// otherwise we can create resource leaks or even lock up our IO
		// later we'll see how to do it automatically
		input.close();
	}
}
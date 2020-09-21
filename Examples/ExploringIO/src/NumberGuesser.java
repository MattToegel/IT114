import java.util.Random;
import java.util.Scanner;

public class NumberGuesser{
	public static void main(String[] args) {
		try (Scanner input = new Scanner(System.in);){
			System.out.println("I picked a random number between 1-10, let's see if you can guess.");
			System.out.println("To exit, type something that's not a number.");
			int number = new Random().nextInt(9)+1;
			System.out.println("Type a number and press enter");
			while(input.hasNext()) {
				int guess = input.nextInt();
				System.out.println("You guessed " + guess);
				if(guess == number) {
					System.out.println("That's right!");
					System.out.println("I picked a random number between 1-10, let's see if you can guess.");
					number = new Random().nextInt(9)+1;
				}
				else {
					System.out.println("That's wrong");
				}
			}
		}
		catch(Exception e) {
			System.out.println("Oh no! What are you going? That's not a number, I can't handle this.");
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
}
import java.util.Random;
import java.util.Scanner;

public class NumberGuesserPart2 {
	public static void main(String[] args) {
		// there's a lot of repeated code and it's not the cleanest. We'll see later how
		// to improve this
		try (Scanner input = new Scanner(System.in);) {
			System.out.println("I picked a random number between 1-10, let's see if you can guess.");
			System.out.println("To exit, type something that's not a number.");
			int number = new Random().nextInt(9) + 1;
			System.out.println("Type a number and press enter");
			// let's make it interesting with levels and strikes
			int level = 1;
			int strikes = 0;
			int maxStrikes = 5;
			while (input.hasNext()) {
				// inner catch to lazily check if the entered value is a number
				// we won't output the error since it should be expected to be a number paring
				// problem
				String message = input.nextLine();
				// early termination check
				if (message.equalsIgnoreCase("quit")) {
					System.out.println("Tired of playing? No problem, see you next time.");
					break;
				}
				int guess = -1;
				try {
					guess = Integer.parseInt(message);
				} catch (Exception e) {
					System.out.println("You didn't enter a number, please try again");
				}
				if (guess > -1) {
					System.out.println("You guessed " + guess);
					if (guess == number) {
						System.out.println("That's right!");
						level++;// level up!
						strikes = 0;// make sure we clear these out, otherwise it's not really fair
						int range = 9 + ((level - 1) * 5);
						System.out.println("Welcome to level " + level);
						System.out.println(
								"I picked a random number between 1-" + (range + 1) + ", let's see if you can guess.");
						number = new Random().nextInt(range) + 1;
					} else {
						System.out.println("That's wrong");
						strikes++;
						if (strikes >= maxStrikes) {
							System.out.println("Uh oh, looks like you need to get some more practice.");
							strikes = 0;
							level--;
							if (level < 1) {
								level = 1;
							}
							int range = 9 + ((level - 1) * 5);
							System.out.println("I picked a random number between 1-" + (range + 1)
									+ ", let's see if you can guess.");
							number = new Random().nextInt(range) + 1;
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Oh no! What are you doing? That's not a number, I can't handle this.");
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
}
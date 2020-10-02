import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class NumberGuesserPart3 {
	static int level = 1;
	static int strikes = 0;
	static int maxStrikes = 5;
	static int number = 0;
	static List<Integer> guesses = new ArrayList<Integer>();

	/***
	 * Gets a random number between 1 and level.
	 * 
	 * @param level (level to use as upper bounds)
	 * @return number between bounds
	 */
	public static int getNumber(int level) {
		int range = 9 + ((level - 1) * 5);
		System.out.println("I picked a random number between 1-" + (range + 1) + ", let's see if you can guess.");
		// bad place to put it, functions should do one particular thing (basically)
		guesses.clear();
		return new Random().nextInt(range) + 1;
	}

	public static void doWin() {
		System.out.println("That's right!");
		level++;// level up!
		strikes = 0;
		System.out.println("Welcome to level " + level);
		number = getNumber(level);
	}

	public static void main(String[] args) {
		// there's a lot of repeated code and it's not the cleanest. We'll see later how
		// to improve this
		try (Scanner input = new Scanner(System.in);) {
			System.out.println("I picked a random number between 1-10, let's see if you can guess.");
			System.out.println("To exit, type something that's not a number.");
			// int number = new Random().nextInt(9) + 1;
			System.out.println("Type a number and press enter");
			// let's make it interesting with levels and strikes

			number = getNumber(level);
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
				if (message.indexOf("recover") > -1) {
					String[] rec = message.split(" ");
					try {
						int recovery = Integer.parseInt(rec[1]);
						strikes -= recovery;
					} catch (NumberFormatException e) {
						System.out.println("You didn't enter a number, please try again");

					} catch (ArrayIndexOutOfBoundsException e2) {
						// it's bad to leave empty but eh
					}
				}
				if (message.indexOf(",") > -1) {
					String[] potentalGuesses = message.split(",");
					// bad way to do it but I'm lazy to refactor
					for (int i = 0; i < potentalGuesses.length; i++) {
						String potential = potentalGuesses[i].trim();
						try {
							int _guess = Integer.parseInt(potential);
							if (_guess == number) {
								doWin();
							}
						} catch (Exception e) {

						}
					}
				}
				int guess = -1;
				try {
					guess = Integer.parseInt(message);
				} catch (NumberFormatException e) {
					System.out.println("You didn't enter a number, please try again");

				}
				if (guess > -1) {
					System.out.println("You guessed " + guess);
					if (guess == number) {
						doWin();
					} else {
						System.out.println("That's wrong");
						strikes++;
						guesses.add(guess);
						String guessesString = "";
						for (Integer g : guesses) {
							guessesString += (guessesString.length() > 0 ? "," : "") + g;
						}
						System.out.println("Guesses [" + guessesString + "]");

						if (strikes >= maxStrikes) {
							System.out.println("Uh oh, looks like you need to get some more practice.");
							System.out.println("The correct number was " + number);
							strikes = 0;
							level--;
							if (level < 1) {
								level = 1;
							}
							// int range = 9 + ((level - 1) * 5);
							// System.out.println("I picked a random number between 1-" + (range + 1)
							// + ", let's see if you can guess.");
							// number = new Random().nextInt(range) + 1;
							number = getNumber(level);
						} else {
							int remainder = maxStrikes - strikes;
							System.out.println("You have " + remainder + "/" + maxStrikes + " attempts remaining");
							if (guess > number) {
								System.out.println("Lower");
							} else if (guess < number) {
								System.out.println("Higher");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Oh no! What are you going? That's not a number, I can't handle this.");
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
}
package Module3;

import java.util.Random;
import java.util.Scanner;

public class NumberGuesser2 {
    public static void main(String[] args) {
        // there's a lot of repeated code and it's not the cleanest. We'll see later how
        // to improve this
        try (Scanner input = new Scanner(System.in);) {
            System.out.println("Welcome to NumberGuesser2.0");
            System.out.println("To exit, type the word 'quit'.");

            // let's make it interesting with levels and strikes
            int level = 1;
            int strikes = 0;
            int maxStrikes = 5;
            int number = -1;
            boolean pickNewRandom = true;
            do {
                if (pickNewRandom) {
                    int range = 9 + ((level - 1) * 5);
                    System.out.println("Welcome to level " + level);
                    System.out.println(
                            "I picked a random number between 1-" + (range + 1) + ", let's see if you can guess.");
                    number = new Random().nextInt(range) + 1;
                    pickNewRandom = false;
                }
                System.out.println("Type a number and press enter");

                String message = input.nextLine();
                // early termination check
                if (message.equalsIgnoreCase("quit")) {
                    System.out.println("Tired of playing? No problem, see you next time.");
                    break;
                }
                int guess = -1;// default value
                try {
                    // inner catch to lazily check if the entered value is a number
                    // we won't output the error since it should be expected to be a number paring
                    // problem
                    guess = Integer.parseInt(message);
                } catch (NumberFormatException e) {
                    System.out.println("You didn't enter a number, please try again");
                }
                if (guess > 0) {
                    System.out.println("You guessed " + guess);
                    if (guess == number) {
                        System.out.println("That's right!");
                        level++;// level up!
                        strikes = 0;// make sure we clear these out, otherwise it's not really fair
                        pickNewRandom = true;

                    } else {
                        System.out.println("That's wrong");
                        strikes++;
                        if (strikes >= maxStrikes) {
                            System.out.println("Uh oh, looks like you need to get some more practice.");
                            strikes = 0;
                            level--;
                            //prevent level from being less than 1
                            if (level < 1) {
                                level = 1;
                            }
                            pickNewRandom = true;
                        }
                    }
                }
            } while (true);
        } catch (Exception e) {
            System.out.println("An unexpected error occurred. Goodbye.");
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        System.out.println("Thanks for playing!");
    }
}

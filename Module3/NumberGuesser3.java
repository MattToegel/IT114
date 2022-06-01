package Module3;

import java.util.Random;
import java.util.Scanner;

public class NumberGuesser3 {
    private int level = 1;
    private int strikes = 0;
    private int maxStrikes = 5;
    private int number = -1;
    private boolean pickNewRandom = true;
    private Random random = new Random();

    /***
     * Gets a random number between 1 and level.
     */
    private void generateNewNumber() {
        int range = 9 + ((level - 1) * 5);
        System.out.println("Welcome to level " + level);
        System.out.println(
                "I picked a random number between 1-" + (range + 1) + ", let's see if you can guess.");
        number = random.nextInt(range) + 1;
    }
    /**
     * Logic to run when the guess is right
     */
    private void win() {
        System.out.println("That's right!");
        level++;// level up!
        strikes = 0;
    }

    private boolean processCommands(String message) {
        boolean processed = false;
        if (message.equalsIgnoreCase("quit")) {
            System.out.println("Tired of playing? No problem, see you next time.");
            processed = true;
        }
        //TODO add other conditions here
        return processed;
    }

    private void lose() {
        System.out.println("Uh oh, looks like you need to get some more practice.");
        System.out.println("The correct number was " + number);
        strikes = 0;
        level--;
        if (level < 1) {
            level = 1;
        }
    }

    private void processGuess(int guess) {
        if (guess <= 0) {
            return;
        }
        System.out.println("You guessed " + guess);
        if (guess == number) {
            win();
            pickNewRandom = true;
        } else {
            System.out.println("That's wrong");
            strikes++;
            if (strikes >= maxStrikes) {
                lose();
                pickNewRandom = true;
            }
        }
    }

    private int getGuess(String message) {
        int guess = -1;
        try {
            guess = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            System.out.println("You didn't enter a number, please try again");

        }
        return guess;
    }
    /**
     * Test
     */
    public void start() {
        try (Scanner input = new Scanner(System.in);) {
            System.out.println("Welcome to NumberGuesser3.0");
            System.out.println("To exit, type the word 'quit'.");
            do {
                if (pickNewRandom) {
                    generateNewNumber();
                    pickNewRandom = false;
                }
                System.out.println("Type a number and press enter");
                //we'll want to use a local variable here
                //so we can feed it into multiple functions
                String message = input.nextLine();
                // early termination check
                if (processCommands(message)) {
                    //command handled; don't proceed with game logic
                    break;
                }
                //this is just to demonstrate we can return a value and pass it into another method
                //int guess = getGuess(message);
                //processGuess(guess);
                //the following line is the same as the above two lines
                processGuess(getGuess(message));
            } while (true);
        } catch (Exception e) {
            System.out.println("An unexpected error occurred. Goodbye.");
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        System.out.println("Thanks for playing!");
    }

    public static void main(String[] args) {
        NumberGuesser3 ng = new NumberGuesser3();
        ng.start();
    }
}

package Project.Common;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Deck class represents a collection of cards with methods to manipulate them.
 */
public class Deck {
    private List<Card> cards;
    private List<Card> defaultCards;
    private static int idCounter = 0;

    /**
     * Constructs a Deck object and loads the cards from a file.
     *
     * @param filePath the path to the file containing card information
     * @throws IOException if an I/O error occurs reading from the file
     */
    public Deck(String filePath) throws IOException {
        this.cards = new ArrayList<>();
        this.defaultCards = new ArrayList<>();
        loadCardsFromFile(filePath);
        resetDeck();
    }

    /**
     * Loads cards from a file and stores them in the defaultCards list.
     *
     * @param filePath the path to the file containing card information
     * @throws IOException if an I/O error occurs reading from the file
     */
    private synchronized void loadCardsFromFile(String filePath) throws IOException {
        // Construct absolute path to the file
        Path fullPath = Paths.get(System.getProperty("user.dir"), filePath);
        if (!Files.exists(fullPath)) {
            throw new IOException("File not found: " + fullPath);
        }
        try (BufferedReader br = Files.newBufferedReader(fullPath)) {
            defaultCards = br.lines()
                    .map(line -> line.split(","))
                    .filter(cardDetails -> cardDetails.length == 3)
                    .map(cardDetails -> new Card(
                            generateID(),
                            cardDetails[0].trim(),
                            cardDetails[1].trim(),
                            Integer.parseInt(cardDetails[2].trim())))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Generates a unique ID for each card.
     *
     * @return a unique integer ID
     */
    private synchronized int generateID() {
        return ++idCounter;
    }

    /**
     * Resets the deck to its default state.
     */
    public synchronized void resetDeck() {
        cards.clear();
        cards.addAll(defaultCards);
    }

    public synchronized Card draw() {
        Card c = null;
        if (!cards.isEmpty()) {
            c = cards.remove(0);
        }
        return c;
    }

    /**
     * Draws a specified number of cards from the top of the deck.
     *
     * @param x the number of cards to draw
     * @return a list of drawn cards
     */
    public synchronized List<Card> draw(int x) {
        List<Card> drawnCards = new ArrayList<>();
        for (int i = 0; i < x && !cards.isEmpty(); i++) {
            drawnCards.add(cards.remove(0));
        }
        return drawnCards;
    }

    /**
     * Picks a specified number of random cards from the deck.
     *
     * @param x the number of cards to pick
     * @return a list of picked cards
     */
    public synchronized List<Card> pick(int x) {
        List<Card> pickedCards = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < x && !cards.isEmpty(); i++) {
            pickedCards.add(cards.remove(random.nextInt(cards.size())));
        }
        return pickedCards;
    }

    /**
     * Shuffles the deck.
     */
    public synchronized void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Puts a list of incoming cards at the bottom of the deck.
     *
     * @param incomingCards the list of cards to be added to the bottom of the deck
     */
    public synchronized void putToBottom(List<Card> incomingCards) {
        cards.addAll(incomingCards);
    }

    /**
     * Puts a list of incoming cards at the top of the deck.
     *
     * @param incomingCards the list of cards to be added to the top of the deck
     */
    public synchronized void putToTop(List<Card> incomingCards) {
        cards.addAll(0, incomingCards);
    }

    /**
     * Gets the list of cards currently in the deck.
     *
     * @return a list of cards in the deck
     */
    public synchronized List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Main method to demonstrate the functionality of the Deck class.
     *
     * @param args command-line arguments
     * @throws IOException if an I/O error occurs reading from the file
     */
    public static void main(String[] args) throws IOException {
        // Load the cards from the "cards.txt" file in the Project directory
        Deck deck = new Deck("/Project/cards.txt");

        System.out.println("Initial deck:");
        deck.getCards().forEach(System.out::println);

        deck.shuffle();
        System.out.println("\nShuffled deck:");
        deck.getCards().forEach(System.out::println);

        List<Card> drawnCards = deck.draw(3);
        System.out.println("\nDrawn cards:");
        drawnCards.forEach(System.out::println);

        System.out.println("\nDeck after drawing cards:");
        deck.getCards().forEach(System.out::println);

        List<Card> pickedCards = deck.pick(2);
        System.out.println("\nPicked cards:");
        pickedCards.forEach(System.out::println);

        System.out.println("\nDeck after picking cards:");
        deck.getCards().forEach(System.out::println);

        deck.resetDeck();
        System.out.println("\nDeck after reset:");
        deck.getCards().forEach(System.out::println);
    }
}

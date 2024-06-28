package Project.Common;

import java.util.ArrayList;
import java.util.List;

/**
 * Common Player data shared between Client and Server
 */
public class Player {
    public static long DEFAULT_CLIENT_ID = -1L;
    private long clientId = Player.DEFAULT_CLIENT_ID;
    private boolean isReady = false;
    private boolean takeTurn = false;

    private List<Card> hand = new ArrayList<Card>();

    public long getClientId() {
        return clientId;
    }

    public boolean didTakeTurn() {
        return takeTurn;
    }

    public void setTakeTurn(boolean tookTurn) {
        this.takeTurn = tookTurn;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public void addToHand(Card card) {
        hand.add(card);
    }

    public void addToHand(List<Card> cards) {
        hand.addAll(cards);
    }

    public Card removeFromHand(Card card) {
        // Important: Since Card is being passed over the socket as Payload data
        // It likely won't be the exact object that's in the Player's hand
        // so hand.remove(card) may not always work.
        // The below logic uses Card.id which is unique so it can find the proper match
        // then that reference will be removed from the hand
        return hand.stream()
                .filter(c -> c.getId() == card.getId())
                .findFirst()
                .map(c -> {
                    hand.remove(c);
                    return c;
                })
                .orElse(null);
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> cards) {
        hand = cards;
    }

    /**
     * Resets all of the data (this is destructive).
     * You may want to make a softer reset for other data
     */
    public void reset() {
        this.clientId = Player.DEFAULT_CLIENT_ID;
        this.isReady = false;
    }
}

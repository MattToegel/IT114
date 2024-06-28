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

    public void addToHand(Card card){
        hand.add(card);
    }
    public void addToHand(List<Card> cards){
        hand.addAll(cards);
    }
    public Card removeFromHand(Card c){
        return hand.remove(hand.indexOf(c));
    }
    public List<Card> getHand(){
        return hand;
    }
    
    /**
     * Resets all of the data (this is destructive).
     * You may want to make a softer reset for other data
     */
    public void reset(){
        this.clientId = Player.DEFAULT_CLIENT_ID;
        this.isReady = false;
    }
}

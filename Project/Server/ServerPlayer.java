package Project.Server;

import java.util.List;

import Project.Common.Card;
import Project.Common.Phase;
import Project.Common.Player;

/**
 * Server-only data about a player
 * Added in ReadyCheck lesson/branch for non-chatroom projects.
 * If chatroom projects want to follow this design update the following in this
 * lesson:
 * Player class renamed to User
 * clientPlayer class renamed to ClientUser (or the original ClientData)
 * ServerPlayer class renamed to ServerUser
 */
public class ServerPlayer extends Player {
    private ServerThread client; // reference to wrapped ServerThread

    public ServerPlayer(ServerThread clientToWrap) {
        client = clientToWrap;
        setClientId(client.getClientId());
    }

    /**
     * Used only for passing the ServerThread to the base class of Room.
     * Favor creating wrapper methods instead of interacting with this directly.
     * 
     * @return ServerThread reference
     */
    public ServerThread getServerThread() {
        return client;
    }
    
    // add any wrapper methods to call on the ServerThread
    // don't used the exposed full ServerThread object
    public boolean removeCardFromHand(Card card){
        return client.removeCardFromHand(card);
    }
    public boolean removeCardsFromHand(List<Card> cards){
        return client.removeCardsFromHand(cards);
    }
    public boolean addCardToHand(Card card){
        return client.addCardToHand(card);
    }
    public boolean addCardsToHand(List<Card> cards){
        return client.addCardsToHand(cards);
    }
    public boolean sendCardsInHand(List<Card> cards){
        return client.sendCardsInHand(cards);
    }
    public boolean sendMove(long clientId, int x, int y){
        return client.sendMove(clientId, x, y);
    }
    public boolean sendTurnStatus(long clientId, boolean didTakeTurn){
        return client.sendTurnStatus(clientId, didTakeTurn);
    }
    public boolean sendGridDimensions(int x, int y) {
        return client.sendGridDimensions(x, y);
    }

    public boolean sendReadyStatus(long clientId, boolean isReady, boolean quiet) {
        return client.sendReadyStatus(clientId, isReady, quiet);
    }

    public boolean sendReadyStatus(long clientId, boolean isReady) {
        return client.sendReadyStatus(clientId, isReady);
    }

    public boolean sendResetReady() {
        return client.sendResetReady();
    }

    public boolean sendCurrentPhase(Phase phase) {
        return client.sendCurrentPhase(phase);
    }
}

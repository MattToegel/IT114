package Project.Server;

import java.util.List;

import Project.Common.Card;
import Project.Common.Phase;
import Project.Common.Player;
import Project.Common.TimerType;
import Project.Common.Tower;

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

    public String getClientName() {
        return client.getClientName();
    }

    // add any wrapper methods to call on the ServerThread
    // don't used the exposed full ServerThread object
    public boolean sendCurrentTime(TimerType timerType, int time) {
        return client.sendCurrentTime(timerType, time);
    }

    public boolean sendCurrentTurn(long clientId) {
        return client.sendCurrentTurn(clientId);
    }

    public boolean sendPlayerCurrentEnergy(long clientId, int energy) {
        return client.sendPlayerCurrentEnergy(clientId, energy);
    }

    public boolean sendTowerStatus(int x, int y, Tower t) {
        return client.sendTowerStatus(x, y, t);
    }

    public boolean sendRemoveCardFromHand(Card card) {
        return client.sendRemoveCardFromHand(card);
    }

    public boolean sendRemoveCardsFromHand(List<Card> cards) {
        return client.sendRemoveCardsFromHand(cards);
    }

    public boolean sendAddCardToHand(Card card) {
        return client.sendAddCardToHand(card);
    }

    public boolean sendAddCardsToHand(List<Card> cards) {
        return client.sendAddCardsToHand(cards);
    }

    public boolean sendCardsInHand(List<Card> cards) {
        return client.sendCardsInHand(cards);
    }

    public boolean sendTurnStatus(long clientId, boolean didTakeTurn) {
        return client.sendTurnStatus(clientId, didTakeTurn);
    }

    public boolean sendGridDimensions(int x, int y, long seed) {
        return client.sendGridDimensions(x, y, seed);
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

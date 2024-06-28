package Project.Server;

import java.io.IOException;
import java.util.List;

import Project.Common.Card;
import Project.Common.Deck;
import Project.Common.Grid;
import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.Player;
import Project.Common.TimedEvent;

public class GameRoom extends BaseGameRoom {

    // used for general rounds (usually phase-based turns)
    private TimedEvent roundTimer = null;

    // used for granular turn handling (usually turn-order turns)
    private TimedEvent turnTimer = null;

    private Grid grid = null;

    private Deck deck = null;

    public GameRoom(String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientAdded(ServerPlayer sp) {
        // sync GameRoom state to new client
        syncCurrentPhase(sp);
        syncReadyStatus(sp);
        if (currentPhase != Phase.READY) {
            syncGridDimensions(sp);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientRemoved(ServerPlayer sp) {

    }

    // timer handlers
    private void startRoundTimer() {
        roundTimer = new TimedEvent(30, () -> onRoundEnd());
        roundTimer.setTickCallback((time) -> System.out.println("Round Time: " + time));
    }

    private void resetRoundTimer() {
        if (roundTimer != null) {
            roundTimer.cancel();
            roundTimer = null;
        }
    }

    private void startTurnTimer() {
        roundTimer = new TimedEvent(30, () -> onTurnEnd());
        roundTimer.setTickCallback((time) -> System.out.println("Turn Time: " + time));
    }

    private void resetTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
    }
    // end timer handlers

    // misc start
    private void drawHands() {
        playersInRoom.values().stream().filter(p -> p.isReady()).forEach(p -> {
            List<Card> cards = deck.draw(3);
            if (cards == null || cards.size() == 0) {
                LoggerUtil.INSTANCE.severe("Failed to draw cards");
            } else {
                p.addToHand(cards);
                syncPlayerHand(p);
            }

        });
    }

    private void eachDrawCard() {
        playersInRoom.values().stream().filter(p -> p.isReady()).forEach(p -> {
            Card card = deck.draw();
            if (card != null) {
                p.addToHand(card);
                syncAddCard(p, card);
            } else {
                LoggerUtil.INSTANCE.severe("Failed to draw a card");
            }
        });
    }
    // misc end

    // lifecycle methods

    /** {@inheritDoc} */
    @Override
    protected void onSessionStart() {
        LoggerUtil.INSTANCE.info("onSessionStart() start");
        changePhase(Phase.IN_PROGRESS);
        grid = new Grid(2, 2);
        try {
            deck = new Deck("Project/Common/cards.txt");
        } catch (IOException e) {
            e.printStackTrace();
            onSessionEnd();
            return;
        }
        deck.shuffle();
        sendGridDimensions();
        drawHands();
        LoggerUtil.INSTANCE.info("onSessionStart() end");
        onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onRoundStart() {
        LoggerUtil.INSTANCE.info("onRoundStart() start");
        resetRoundTimer();
        startRoundTimer();
        eachDrawCard();
        LoggerUtil.INSTANCE.info("onRoundStart() end");
    }

    /** {@inheritDoc} */
    @Override
    protected void onTurnStart() {
        LoggerUtil.INSTANCE.info("onTurnStart() start");
        resetTurnTimer();
        startTurnTimer();
        LoggerUtil.INSTANCE.info("onTurnStart() end");
    }

    // Note: logic between Turn Start and Turn End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onTurnEnd() {
        LoggerUtil.INSTANCE.info("onTurnEnd() start");
        resetTurnTimer(); // reset timer if turn ended without the time expiring

        LoggerUtil.INSTANCE.info("onTurnEnd() end");
    }

    // Note: logic between Round Start and Round End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onRoundEnd() {
        LoggerUtil.INSTANCE.info("onRoundEnd() start");
        resetRoundTimer(); // reset timer if round ended without the time expiring

        LoggerUtil.INSTANCE.info("onRoundEnd() end");
        // example of some end session condition 2
        sendMessage(null, "Too slow populating the grid, you all lose");
        onSessionEnd();
    }

    /** {@inheritDoc} */
    @Override
    protected void onSessionEnd() {
        LoggerUtil.INSTANCE.info("onSessionEnd() start");
        grid.reset();
        resetRoundTimer(); // just in case it's still active if we forgot to end it sooner
        sendGridDimensions();
        sendResetTurnStatus();
        resetReadyStatus();
        changePhase(Phase.READY);
        LoggerUtil.INSTANCE.info("onSessionEnd() end");
    }
    // end lifecycle methods

    // misc logic
    private void checkIfAllTookTurns() {
        long ready = playersInRoom.values().stream().filter(p -> p.isReady()).count();
        long tookTurn = playersInRoom.values().stream().filter(p -> p.isReady() && p.didTakeTurn()).count();
        if (ready == tookTurn) {
            // example of some end session condition 2
            if (grid.areAllCellsOccupied()) {
                sendMessage(null, "Congrats, you filled the grid");
                onSessionEnd();
            } else {
                sendResetTurnStatus();
                resetRoundTimer();
                startRoundTimer();
                sendMessage(null, "Move again");
            }

        }
    }
    // end misc logic

    // send/sync data to ServerPlayer(s)
    private void syncRemoveCard(ServerPlayer sp, Card card) {
        sp.removeCardFromHand(card);
    }

    private void syncAddCard(ServerPlayer sp, Card card) {
        sp.addCardToHand(card);
    }

    private void syncPlayerHand(ServerPlayer sp) {
        sp.sendCardsInHand(sp.getHand());
    }

    /**
     * Sends a movement coordinate of one Player to all Players (including
     * themselves)
     * 
     * @param sp
     * @param x
     * @param y
     */
    private void sendMove(ServerPlayer sp, int x, int y) {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendMove(sp.getClientId(), x, y);
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    /**
     * A shorthand way of telling all clients to reset their local list's turn
     * status
     */
    private void sendResetTurnStatus() {
        playersInRoom.values().removeIf(spInRoom -> {
            spInRoom.setTakeTurn(false); // reset server data
            // using DEFAULT_CLIENT_ID as a trigger, prevents needing a nested loop to
            // update the status of each player to each player
            boolean failedToSend = !spInRoom.sendTurnStatus(Player.DEFAULT_CLIENT_ID, false);
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    /**
     * Sends the turn status of one Player to all Players (including themselves)
     * 
     * @param sp
     */
    private void sendTurnStatus(ServerPlayer sp) {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendTurnStatus(sp.getClientId(), sp.didTakeTurn());
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    private void syncGridDimensions(ServerPlayer sp) {
        sp.sendGridDimensions(grid.getRows(), grid.getCols());
    }

    private void sendGridDimensions() {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendGridDimensions(grid.getRows(), grid.getCols());
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    // end send data to ServerPlayer(s)

    // receive data from ServerThread (GameRoom specific)
    protected void handleDiscardCard(ServerThread st, Card card) {
        try {
            checkCurrentPhase(st, Phase.IN_PROGRESS);
            checkPlayerInRoom(st);
            // TODO finish this
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            syncRemoveCard(sp, card);
            sendMessage(null, String.format("%s discarded %s", st.getClientName(), card));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handleUseCard(ServerThread st, Card card) {
        try {
            checkCurrentPhase(st, Phase.IN_PROGRESS);
            checkPlayerInRoom(st);
            // TODO finish this
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            syncRemoveCard(sp, card);
            sendMessage(null, String.format("%s used %s", st.getClientName(), card));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handleMove(ServerThread st, int x, int y) {
        try {
            checkPlayerInRoom(st);
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            if (sp.didTakeTurn()) {
                st.sendMessage("You already took your turn");
                return;
            }
            if (grid.getCell(x, y).isOccupied()) {
                st.sendMessage("This cell is already occupied");
                return;
            }
            grid.setCell(x, y, true);
            sendMove(sp, x, y);
            sp.setTakeTurn(true);
            sendTurnStatus(sp);
            checkIfAllTookTurns();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // end receive data from ServerThread (GameRoom specific)
}

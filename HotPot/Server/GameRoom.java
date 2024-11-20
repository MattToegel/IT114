package HotPot.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import HotPot.Common.Card;
import HotPot.Common.Deck;
import HotPot.Common.LoggerUtil;
import HotPot.Common.Phase;
import HotPot.Common.Player;
import HotPot.Common.TimedEvent;
import HotPot.Common.TimerType;

public class GameRoom extends BaseGameRoom {

    // used for general rounds (usually phase-based turns)
    private TimedEvent roundTimer = null;

    // used for granular turn handling (usually turn-order turns)
    private TimedEvent turnTimer = null;
    private int round = 0;
    Deck deck = null;
    private List<ServerPlayer> turnOrder = new ArrayList<>();
    private long currentPlayerId;
    private int magicNumber = -1;
    private int originalMagicNumber = -1;

    public GameRoom(String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientAdded(ServerPlayer sp) {
        // sync GameRoom state to new client

        // give a slight delay to allow the Room list content to be sent from the base
        // class
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                syncCurrentPhase(sp);
                syncReadyStatus(sp);
            }
        }.start();

    }

    /** {@inheritDoc} */
    @Override
    protected void onClientRemoved(ServerPlayer sp) {
        // added after Summer 2024 Demo
        // Stops the timers so room can clean up
        LoggerUtil.INSTANCE.info("Player Removed, remaining: " + playersInRoom.size());
        if (playersInRoom.isEmpty()) {
            resetReadyTimer();
            resetTurnTimer();
            resetRoundTimer();
            onSessionEnd();
        }
    }

    // timer handlers
    private void startRoundTimer() {
        roundTimer = new TimedEvent(30, () -> onRoundEnd());
        roundTimer.setTickCallback((time) -> {
            System.out.println("Round Time: " + time);
            sendCurrentTime(TimerType.ROUND, time);
        });
    }

    private void resetRoundTimer() {
        if (roundTimer != null) {
            roundTimer.cancel();
            roundTimer = null;
            sendCurrentTime(TimerType.ROUND, -1);
        }
    }

    private void startTurnTimer() {
        turnTimer = new TimedEvent(30, () -> onTurnEnd());
        turnTimer.setTickCallback((time) -> {
            System.out.println("Turn Time: " + time);
            sendCurrentTime(TimerType.TURN, time);
        });
    }

    private void resetTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
            sendCurrentTime(TimerType.TURN, -1);
        }
    }
    // end timer handlers

    // lifecycle methods

    /** {@inheritDoc} */
    @Override
    protected void onSessionStart() {
        LoggerUtil.INSTANCE.info("onSessionStart() start");
        changePhase(Phase.IN_PROGRESS);
        long count = playersInRoom.values().stream().filter(p -> p.isReady()).count();
        // setup deck
        deck = new Deck((int) count);
        deck.shuffle();

        // setup turns
        turnOrder.clear();
        playersInRoom.values().stream().filter(ServerPlayer::isReady).forEach(p -> {
            turnOrder.add(p); // add ServerPlayer reference
        });

        // draw hands
        turnOrder.forEach(sp -> {
            List<Card> cards = deck.draw(5);
            if (cards == null || cards.size() == 0) {
                LoggerUtil.INSTANCE.severe("Failed to draw cards");
            } else {
                sp.addToHand(cards); // server state
                sp.sendCardsInHand(cards); // sync
            }

        });
        magicNumber = (int) (new Random().nextInt((int) (count * 101)) + (count * 10));
        originalMagicNumber = magicNumber;
        LoggerUtil.INSTANCE.info("Magic Number is " + magicNumber);
        sendVisualPercentage(0);
        LoggerUtil.INSTANCE.info("onSessionStart() end");
        onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onRoundStart() {
        LoggerUtil.INSTANCE.info("onRoundStart() start");
        // This example doesn't use round timer
        // resetRoundTimer();
        // startRoundTimer();
        round++;
        sendGameEvent("Round: " + round);
        // reset taken turn
        sendResetTurnStatus();

        LoggerUtil.INSTANCE.info("onRoundStart() end");
        onTurnStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onTurnStart() {
        LoggerUtil.INSTANCE.info("onTurnStart() start");
        changePhase(Phase.IN_PROGRESS);
        resetTurnTimer();
        startTurnTimer();
        nextPlayer();
        ServerPlayer currentPlayer = getCurrentPlayer();
        sendGameEvent(String.format("It's %s's turn", currentPlayer.getClientName()));
        Card card = deck.draw();
        if (card != null) {
            currentPlayer.addToHand(card);// server state
            currentPlayer.sendAddCardToHand(card);// sync
        }
        LoggerUtil.INSTANCE.info("onTurnStart() end");
    }

    // Note: logic between Turn Start and Turn End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onTurnEnd() {
        LoggerUtil.INSTANCE.info("onTurnEnd() start");
        resetTurnTimer(); // reset timer if turn ended without the time expiring
        // update player turn state
        ServerPlayer sp = getCurrentPlayer();
        sp.setTakeTurn(true); // server state
        sendTurnStatus(sp);// sync
        LoggerUtil.INSTANCE.info("onTurnEnd() end");

        if (magicNumber <= 0) {
            sendGameEvent(String.format("%s ended the session", sp.getClientName()));
            // delay change
            changePhase(Phase.SCORING);
            new TimedEvent(10, () -> {
                onSessionEnd();
            });
            return;
        }

        if (isRoundOver() || deck.isEmpty()) {
            changePhase(Phase.TRANSITION_DELAY);
            TimedEvent t = new TimedEvent(5, () -> {
                // move to next round (there's no benefit other than resetting the turns for
                // this example)
                onRoundEnd(); // next round
            });
            t.setTickCallback((time) -> {
                sendCurrentTime(TimerType.NEXT_ROUND, time);
            });

        } else {
            onTurnStart(); // next player
        }

    }

    // Note: logic between Round Start and Round End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onRoundEnd() {
        LoggerUtil.INSTANCE.info("onRoundEnd() start");
        // This example doesn't use round timer
        // resetRoundTimer(); // reset timer if round ended without the time expiring

        if (deck.isEmpty()) {
            onSessionEnd();
        } else {
            onRoundStart();
        }
        LoggerUtil.INSTANCE.info("onRoundEnd() end");

    }

    /** {@inheritDoc} */
    @Override
    protected void onSessionEnd() {
        LoggerUtil.INSTANCE.info("onSessionEnd() start");
        // reset timers in case end was reached before expiry
        resetTurnTimer();
        sendResetHands();
        sendResetTurnStatus();
        resetReadyStatus();
        changePhase(Phase.READY);
        LoggerUtil.INSTANCE.info("onSessionEnd() end");
    }
    // end lifecycle methods

    // turn helpers start
    private ServerPlayer getCurrentPlayer() {
        return turnOrder.stream()
                .filter(sp -> sp.getClientId() == currentPlayerId)
                .findFirst()
                .orElse(null);

    }

    private int getCurrentPlayerIndex() {
        return IntStream.range(0, turnOrder.size())
                .filter(i -> turnOrder.get(i).getClientId() == currentPlayerId)
                .findFirst()
                .orElse(-1); // Return -1 if not found
    }

    private ServerPlayer nextPlayer() {
        int indexOfCurrentPlayer = getCurrentPlayerIndex();
        System.out.println("Current player index: " + indexOfCurrentPlayer);
        indexOfCurrentPlayer++;
        if (indexOfCurrentPlayer >= turnOrder.size()) {
            indexOfCurrentPlayer = 0;
        }
        currentPlayerId = turnOrder.get(indexOfCurrentPlayer).getClientId();
        System.out.println("Current player id: " + currentPlayerId);
        return getCurrentPlayer();
    }

    private boolean isRoundOver() {
        int check = getCurrentPlayerIndex() + 1;
        return check >= turnOrder.size();
    }
    // turn helpers end

    // send/sync data to ServerPlayer(s)
    private void sendVisualPercentage(int percentage){
        playersInRoom.values().removeIf(spInRoom -> {
          
            // using DEFAULT_CLIENT_ID as a trigger, prevents needing a nested loop to
            // update the status of each player to each player
            boolean failedToSend = !spInRoom.sendVisualPercentage(percentage);
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }
    private void sendResetHands() {
        playersInRoom.values().removeIf(spInRoom -> {
            spInRoom.setHand(null); // reset server data
            // using DEFAULT_CLIENT_ID as a trigger, prevents needing a nested loop to
            // update the status of each player to each player
            boolean failedToSend = !spInRoom.sendCardsInHand(null);
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
     * Sends a game event to all clients
     * 
     * @param str
     */
    private void sendGameEvent(String str) {
        sendGameEvent(str, null);
    }

    /**
     * Sends a game event to specific clients (by id)
     * 
     * @param str
     * @param targets
     */
    private void sendGameEvent(String str, List<Long> targets) {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean canSend = false;
            if (targets != null) {
                if (targets.contains(spInRoom.getClientId())) {
                    canSend = true;
                }
            } else {
                canSend = true;
            }
            if (canSend) {
                boolean failedToSend = !spInRoom.sendGameEvent(str);
                if (failedToSend) {
                    removedClient(spInRoom.getServerThread());
                }
                return failedToSend;
            }
            return false;
        });
    }

    private void sendPointsUpdate(ServerPlayer sp) {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendPointsUpdate(sp.getClientId(), sp.getPoints());
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }
    // end send data to ServerPlayer(s)

    // start custom checks
    /**
     * Early exit (via exception throwing) if the user isn't in the room
     * 
     * @param client
     * @throws Exception
     */
    protected void checkCurrentPlayer(ServerThread client) throws Exception {
        if (currentPlayerId != client.getClientId()) {
            LoggerUtil.INSTANCE.severe("Player isn't the current Player");
            client.sendGameEvent("It's not your turn");
            throw new Exception("Player isn't the current Player");
        }
    }

    private void checkPlayerIsReady(ServerPlayer sp) throws Exception {
        if (!sp.isReady()) {
            sp.sendGameEvent("You weren't ready in time");
            throw new Exception("Player isn't ready");
        }
    }

    private void checkPlayerTookTurn(ServerPlayer sp) throws Exception {
        if (sp.didTakeTurn()) {
            sp.sendGameEvent("You already took your turn");
            throw new Exception("Player already took turn");
        }
    }
    // end custom checks

    // receive data from ServerThread (GameRoom specific)
    protected void handleUseCard(ServerThread st, Card card) {
        try {
            checkCurrentPhase(st, Phase.IN_PROGRESS);
            checkPlayerInRoom(st);
            checkCurrentPlayer(st);

            ServerPlayer sp = playersInRoom.get(st.getClientId());
            checkPlayerIsReady(sp);
            checkPlayerTookTurn(sp);
            if (sp.removeFromHand(card) == null) {// server state
                LoggerUtil.INSTANCE.severe("User doesn't have this card in hand: " + card);
                sp.sendGameEvent("You don't have this card: " + card);
                return;
            }
            sp.sendRemoveCardFromHand(card);// sync
            sendGameEvent(String.format("%s used %s", st.getClientName(), card));
            magicNumber -= card.getValue();
            int percentage = (int) Math.ceil((originalMagicNumber - magicNumber) / (originalMagicNumber * 1.0) * 100);

            sendVisualPercentage(percentage);
            // end session is handled in onTurnEnd()
            if (magicNumber > 0) {
                sp.changePoints(card.getValue());// server state
                sendPointsUpdate(sp); // sync
            }
            onTurnEnd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // end receive data from ServerThread (GameRoom specific)

}

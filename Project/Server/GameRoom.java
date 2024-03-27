package Project.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import Project.Common.Constants;
import Project.Common.Phase;
import Project.Common.TextFX;
import Project.Common.TimedEvent;
import Project.Common.TextFX.Color;

public class GameRoom extends Room {

    private ConcurrentHashMap<Long, ServerPlayer> players = new ConcurrentHashMap<Long, ServerPlayer>();

    private TimedEvent readyCheckTimer = null;
    private TimedEvent turnTimer = null;
    private Phase currentPhase = Phase.READY;
    private long numActivePlayers = 0;
    private boolean canEndSession = false;
    private ServerPlayer currentPlayer = null;
    private List<Long> turnOrder = new ArrayList<Long>();

    public GameRoom(String name) {
        super(name);
    }

    @Override
    protected synchronized void addClient(ServerThread client) {
        super.addClient(client);
        if (!players.containsKey(client.getClientId())) {
            ServerPlayer sp = new ServerPlayer(client);
            players.put(client.getClientId(), sp);
            System.out.println(TextFX.colorize(client.getClientName() + " join GameRoom " + getName(), Color.WHITE));

            // sync game state

            // sync phase
            sp.sendPhase(currentPhase);
            // sync ready state
            players.values().forEach(p -> {
                sp.sendReadyState(p.getClientId(), p.isReady());
                sp.sendPlayerTurnStatus(p.getClientId(), p.didTakeTurn());
            });
            if (currentPlayer != null) {
                sp.sendCurrentPlayerTurn(currentPlayer.getClientId());
            }

        }
    }

    @Override
    protected synchronized void removeClient(ServerThread client) {
        super.removeClient(client);
        // Note: base Room can close (if empty) before GameRoom cleans up (possibly)
        if (players.containsKey(client.getClientId())) {
            players.remove(client.getClientId());
            System.out.println(TextFX.colorize(client.getClientName() + " left GameRoom " + getName(), Color.WHITE));
            // update active players in case an active player left
            numActivePlayers = players.values().stream().filter(ServerPlayer::isReady).count();
        }
    }

    // serverthread interactions
    public synchronized void setReady(ServerThread client) {
        if (currentPhase != Phase.READY) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, "Can't initiate ready check at this time");
            return;
        }
        long playerId = client.getClientId();
        if (players.containsKey(playerId)) {
            // players.get(playerId).setReady(!players.get(playerId).isReady()); //<--
            // toggles ready state
            players.get(playerId).setReady(true);// <-- simply sets the ready state to true
            syncReadyState(players.get(playerId));
            System.out.println(TextFX.colorize(players.get(playerId).getClientName() + " marked themselves as ready ",
                    Color.YELLOW));
            readyCheck();
        } else {
            System.err.println(TextFX.colorize("Player doesn't exist: " + client.getClientName(), Color.RED));
        }
    }

    public synchronized void doTurn(ServerThread client) {
        if (currentPhase != Phase.TURN) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, "You can't do turns just yet");
            return;
        }

        // implementation 1
        long clientId = client.getClientId();
        if (players.containsKey(clientId)) {
            ServerPlayer sp = players.get(clientId);
            // implementation 2 (even though it's nested)
            // check current player's turn
            if (sp.getClientId() != currentPlayer.getClientId()) {
                client.sendMessage(Constants.DEFAULT_CLIENT_ID,
                        "It's not your turn yet");
                return;
            }
            // they can only participate if they're ready
            if (!sp.isReady()) {
                client.sendMessage(Constants.DEFAULT_CLIENT_ID,
                        "Sorry, you weren't ready in time and can't participate");
                return;
            }
            // player can only update their turn "actions" once
            if (!sp.didTakeTurn()) {
                sp.setTakenTurn(true);
                sendMessage(ServerConstants.FROM_ROOM, String.format("%s completed their turn", sp.getClientName()));
                syncUserTookTurn(sp);
                // implemention 2 (end turn immediately)
                if (currentPlayer != null && currentPlayer.didTakeTurn()) {
                    handleEndOfTurn();

                }
            } else {
                client.sendMessage(Constants.DEFAULT_CLIENT_ID, "You already completed your turn, please wait");
            }
        }

    }
    // end serverthread interactions

    private synchronized void readyCheck() {

        if (readyCheckTimer == null) {
            readyCheckTimer = new TimedEvent(30, () -> {
                long numReady = players.values().stream().filter(p -> {
                    return p.isReady();
                }).count();
                // condition 1: start if we have the minimum ready
                boolean meetsMinimum = numReady >= Constants.MINIMUM_REQUIRED_TO_START;
                // condition 2: start if everyone is ready
                int totalPlayers = players.size();
                boolean everyoneIsReady = numReady >= totalPlayers;
                if (meetsMinimum || everyoneIsReady) {
                    start();
                } else {
                    sendMessage(ServerConstants.FROM_ROOM,
                            "Minimum players not met during ready check, please try again");
                    // added after recording as I forgot to reset the ready check
                    players.values().forEach(p -> {
                        p.setReady(false);
                        syncReadyState(p);
                    });
                }
                readyCheckTimer.cancel();
                readyCheckTimer = null;
            });
            readyCheckTimer.setTickCallback((time) -> System.out.println("Ready Countdown: " + time));
        }
    }

    private void changePhase(Phase incomingChange) {
        if (currentPhase != incomingChange) {
            currentPhase = incomingChange;
            syncCurrentPhase();
        }
    }

    private void start() {
        if (currentPhase != Phase.READY) {
            System.err.println("Invalid phase called during start()");
            return;
        }
        canEndSession = false;
        changePhase(Phase.TURN);
        numActivePlayers = players.values().stream().filter(ServerPlayer::isReady).count();
        setupTurns();
        startTurnTimer();
    }

    private void setupTurns() {
        turnOrder = players.values().stream().filter(ServerPlayer::isReady).map(p -> p.getClientId())
                .collect(Collectors.toList());
        Collections.shuffle(turnOrder);
        Long currentPlayerId = turnOrder.get(0);
        currentPlayer = players.get(currentPlayerId);
        System.out.println(TextFX.colorize("First person is " + currentPlayer.getClientName(), Color.YELLOW));
        sendCurrentPlayerTurn();
    }

    private void nextTurn() {

        int index = currentPlayer == null ? 0 : turnOrder.indexOf(currentPlayer.getClientId());
        System.out.println(TextFX.colorize("Current turn index is " + index, Color.YELLOW));
        index++;
        if (index >= turnOrder.size()) {
            index = 0;
        }
        currentPlayer = players.get(turnOrder.get(index));
        System.out.println(TextFX.colorize("Next person is " + currentPlayer.getClientName(), Color.YELLOW));
        sendCurrentPlayerTurn();
    }

    private void startTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
        if (turnTimer == null) {
            // turnTimer = new TimedEvent(60, ()-> {handleEndOfTurn();});
            turnTimer = new TimedEvent(60, this::handleEndOfTurn);
            turnTimer.setTickCallback(this::checkEarlyEndTurn);
            sendMessage(ServerConstants.FROM_ROOM, "Pick your actions");
        }
    }

    private void checkEarlyEndTurn(int timeRemaining) {
        // implementation 1
        /*
         * long numEnded =
         * players.values().stream().filter(ServerPlayer::didTakeTurn).count();
         * if (numEnded >= numActivePlayers) {
         * // end turn early
         * handleEndOfTurn();
         * }
         */

        // implementation 2
        if (currentPlayer != null && currentPlayer.didTakeTurn()) {
            handleEndOfTurn();

        }
    }

    private void handleEndOfTurn() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
        System.out.println(TextFX.colorize("Handling end of turn", Color.YELLOW));
        // option 1 - if they can only do a turn when ready
        List<ServerPlayer> playersToProcess = players.values().stream().filter(ServerPlayer::didTakeTurn).toList();
        // option 2 - double check they are ready and took a turn
        // List<ServerPlayer> playersToProcess =
        // players.values().stream().filter(sp->sp.isReady() &&
        // sp.didTakeTurn()).toList();
        playersToProcess.forEach(p -> {
            sendMessage(ServerConstants.FROM_ROOM, String.format("%s did something for the game", p.getClientName()));
        });

        // TODO end game logic
        if (new Random().nextInt(101) <= 30) {
            canEndSession = true;
            // simulate end game
            end();
        } else {
            resetTurns();
            // implementation 2
            nextTurn();
            // end implementation 2
            startTurnTimer();
        }
    }

    private void resetTurns() {
        players.values().stream().forEach(p -> p.setTakenTurn(false));
        sendResetLocalTurns();
    }

    private void end() {
        System.out.println(TextFX.colorize("Doing game over", Color.YELLOW));
        turnOrder.clear();
        // mark everyone not ready
        players.values().forEach(p -> {
            // TODO fix/optimize, avoid nested loops if/when possible
            p.setReady(false);
            p.setTakenTurn(false);
            // reduce being wasteful
            // syncReadyState(p);
        });
        // depending if this is not called yet, we can clear this state here too
        sendResetLocalReadyState();
        sendResetLocalTurns();
        changePhase(Phase.READY);
        sendMessage(ServerConstants.FROM_ROOM, "Session over!");
        // TODO, eventually will be more optimal to just send that the session ended

    }

    // start send/sync methods
    private void sendCurrentPlayerTurn() {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendCurrentPlayerTurn(currentPlayer == null ? Constants.DEFAULT_CLIENT_ID : currentPlayer.getClientId());
        }
    }

    private void sendResetLocalReadyState() {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendResetLocalReadyState();
        }
    }

    private void sendResetLocalTurns() {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendResetLocalTurns();
        }
    }

    private void syncUserTookTurn(ServerPlayer isp) {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendPlayerTurnStatus(isp.getClientId(), isp.didTakeTurn());
        }
    }
    private void syncCurrentPhase() {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendPhase(currentPhase);
        }
    }

    private void syncReadyState(ServerPlayer csp) {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendReadyState(csp.getClientId(), csp.isReady());
        }
    }
    // end send/sync methods
}

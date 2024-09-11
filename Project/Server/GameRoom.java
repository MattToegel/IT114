package Project.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import Project.Common.Cell;
import Project.Common.Constants;
import Project.Common.Grid;
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
    private Grid grid = new Grid();
    private Random random = new Random();
    private int currentRollValue = -1;
    private int currentStep = -1;
    // controls when the game ends (a "awake" card is drawn)
    private List<Boolean> dragonSleepDeck = new ArrayList<Boolean>();

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
            // TODO implement a better check if grid is actually initialized fully
            if (grid != null && grid.isPopulated()) {
                sp.sendGridDimensions(grid.getRows(), grid.getColumns());
            }
            // sync ready state
            players.values().forEach(p -> {
                sp.sendReadyState(p.getClientId(), p.isReady());
                if (grid != null && grid.isPopulated()) {
                    sp.sendPlayerTurnStatus(p.getClientId(), p.didTakeTurn());
                    sp.sendPlayerPosition(p.getClientId(), p.getCellX(), p.getCellY());
                }
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

    // logic checks
    private void checkCurrentPhase(ServerThread client, Phase check) throws Exception {
        if (currentPhase != check) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    String.format("Current phase is %s, please try again later", currentPhase.name()));
            throw new Exception("Invalid Phase");
        }
    }

    private void checkPlayerInRoom(ServerThread client) throws Exception {
        if (!players.containsKey(client.getClientId())) {
            System.out.println(TextFX.colorize("Player isn't in room", Color.RED));
            throw new Exception("Player isn't in room");
        }
    }

    private void checkPlayerIsReady(ServerPlayer sp) throws Exception {
        if (!sp.isReady()) {
            sp.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    "Sorry, you weren't ready in time and can't participate");
            throw new Exception("Player isn't ready");
        }
    }

    private void checkCurrentTurn(ServerPlayer check) throws Exception {
        if (check.getClientId() != currentPlayer.getClientId()) {
            check.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    "It's not your turn yet");
            throw new Exception("It's not this player's turn");
        }
    }

    private void checkTakenTurn(ServerPlayer check) throws Exception {
        if (check.didTakeTurn()) {
            check.sendMessage(Constants.DEFAULT_CLIENT_ID, "You already completed your turn, please wait");
            throw new Exception("Player already took their turn");
        }
    }

    // end logic checks
    private void handleNextCell(Cell next) {
        if (next != null) {
            currentPlayer.setCell(next);
            currentPlayer.addPath(next);
            sendPlayerPosition(currentPlayer);
        }
        System.out.println(TextFX.colorize(
                String.format("Reached %s,%s", next.getX(), next.getY()), Color.YELLOW));
        sendGameEvent(String.format("%s reached %s,%s", currentPlayer.getClientName(), next.getX(), next.getY()));
        // check if at dragon
        boolean isAtDragon = grid.isAtOrAdjacentToDragon(next);
        if (isAtDragon) {
            currentRollValue = 0;
            System.out.println("Reached Dragon");
            int treasure = random.nextInt(3) + 1;// value is 1-3
            int drawAwakeCards = random.nextInt(4);// 0-3 awake cards
            boolean isAwake = false; // ends the session
            // TODO record points and sync
            int currentPoints = currentPlayer.changePoints(treasure);
            sendPoints(currentPlayer.getClientId(), treasure, currentPoints);
            System.out.println(TextFX.colorize(
                    String.format("Recevied %s treasure and now has %s", treasure, currentPoints), Color.YELLOW));
            for (int i = 0; i < drawAwakeCards; i++) {
                isAwake = dragonSleepDeck.remove(0);
                if (isAwake) {
                    break;
                }
            }
            if (drawAwakeCards > 0) {
                sendMessage(ServerConstants.FROM_ROOM, "The dragon stirs");
                sendGameEvent("<font color=purple>The Dragon Stirs</font>");
            }
            if (!isAwake) {
                List<Cell> starts = grid.getStartCells();
                // move to random start
                Cell randomStart = starts.get(new Random().nextInt(starts.size()));
                Cell currentCell = currentPlayer.getCell();
                currentCell = grid.movePlayer(-1, currentCell, randomStart.getX(), randomStart.getY());
                if (currentCell != null) {
                    currentPlayer.setCell(currentCell);
                    currentPlayer.resetPath();

                    currentPlayer.addPath(next);

                    sendPlayerPosition(currentPlayer);
                }
                grid.print();
            } else {
                sendMessage(ServerConstants.FROM_ROOM, "The Dragon Awakens");
                sendGameEvent("<font color=orange>The Dragon Awakens</font>");
            }
        } else {
            currentStep++;
            if (currentStep >= currentRollValue) {
                currentRollValue = -1;
            }
        }
    }

    private Cell processRollStep(int step, int roll) throws Exception, MultiplePathException {

        // for (int step = 0; step < remainingRollValue; step++) {

        Cell currentCell = currentPlayer.getCell();
        System.out.println(TextFX.colorize("Doing step " + (step + 1) + "/" + roll, Color.CYAN));
        sendGameEvent("Doing step " + (step + 1) + "/" + roll);
        System.out.println(TextFX.colorize(
                String.format("Current %s, %s", currentCell.getX(), currentCell.getY()), Color.YELLOW));
        Cell next = null;
        try {
            next = grid.handleMove(currentPlayer.getClientId(), currentCell, currentPlayer.getPath());
        } catch (MultiplePathException e) {
            // send choices to player
            currentPlayer.sendPathChoices(e.getOptions());
            throw e;
        }
        // this runs if it's a single choice

        grid.print();
        return next;
        // }
    }

    private void processRoll(String choice) {
        Cell currentCell = currentPlayer.getCell();
        long clientId = currentPlayer.getClientId();
        List<Cell> path = currentPlayer.getPath();

        Cell next = null;
        try {
            if (choice == null) {
                next = processRollStep(currentStep, currentRollValue);
            } else {
                next = grid.handleChoiceMove(clientId, currentCell, path, choice);
            }
            if (next != null) {
                handleNextCell(next);
                if (currentRollValue <= 0) {
                    handleEndOfTurn();
                } else {
                    processRoll(null);
                }
            }
        } catch (MultiplePathException mpe) {
            // currentStep++;
            currentPlayer.sendPathChoices(mpe.getOptions());

        } catch (Exception e) {

        }
    }

    // serverthread interactions
    public synchronized void handleDirectionChoice(ServerThread client, String choice) {
        // ensure proper phase
        try {
            checkCurrentPhase(client, Phase.TURN);
            checkPlayerInRoom(client);
            // check player is ready
            ServerPlayer sp = players.get(client.getClientId());
            checkPlayerIsReady(sp);
            // is it their turn
            checkCurrentTurn(sp);
            processRoll(choice);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // ensure user is in room

    }

    public synchronized void doRoll(ServerThread client) {
        final int roll = random.nextInt(6) + 1;// 1 - 6
        System.out.println(TextFX.colorize(
                String.format("Player %s is attempt to roll %s", client.getClientName(), roll), Color.CYAN));

        try {
            // ensure proper phase
            checkCurrentPhase(client, Phase.TURN);
            // ensure user is in room
            checkPlayerInRoom(client);
            // check player is ready
            ServerPlayer sp = players.get(client.getClientId());
            checkPlayerIsReady(sp);
            // is it their turn
            checkCurrentTurn(sp);
            // did they take their turn already
            checkTakenTurn(sp);

            // player can do turn (use roll from above)
            sendRoll(sp.getClientId(), roll);
            currentStep = 0;
            currentRollValue = roll;
            processRoll(null);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Deprecated
    public synchronized void setPosition(ServerThread client, int x, int y) {
        System.out.println(TextFX.colorize(
                String.format("Player %s attempting move to %s,%s", client.getClientName(), x, y), Color.CYAN));
        // ensure proper phase
        if (currentPhase != Phase.TURN) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, "You can't do turns just yet");
            return;
        }
        // ensure user is in room
        if (!players.containsKey(client.getClientId())) {
            System.out.println(TextFX.colorize("Player isn't in room", Color.RED));
            return;
        }
        ServerPlayer sp = players.get(client.getClientId());
        // ensure player is ready

        if (!sp.isReady()) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    "Sorry, you weren't ready in time and can't participate");
            return;
        }
        // implementation 2 (even though it's nested)
        // check current player's turn
        if (sp.getClientId() != currentPlayer.getClientId()) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    "It's not your turn yet");
            return;
        }
        // player can only update their turn "actions" once
        if (sp.didTakeTurn()) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, "You already completed your turn, please wait");
            return;
        }
        // use this to ensure point is in grid (client isn't guaranteed to provide legit
        // data)
        if (!grid.isValidCoordinate(x, y)) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, "Invalid coordinate chosen");
            return;
        }
        // if we got this far, player is eligible for "move"

        // below if condition isn't valid, it'll add the player, then my current logic
        // of setCell will remove the player and add again
        // if (grid.addPlayer(sp.getClientId(), sp.getClientName(), x, y)) {
        Cell newCell = grid.getCell(x, y);
        if (newCell != null) {
            sp.setCell(newCell, false);// <-- this is going to not remove from the cell to simulate my current fake end
                                       // game
            grid.print();
        }
        System.out.println(TextFX.colorize("Recorded position", Color.YELLOW));
        // }

        sp.setTakenTurn(true);

        sendMessage(ServerConstants.FROM_ROOM, String.format("%s completed their turn", sp.getClientName()));
        syncUserTookTurn(sp);
        sendPlayerPosition(currentPlayer);
        // implemention 2 (end turn immediately)
        if (currentPlayer != null && currentPlayer.didTakeTurn()) {
            handleEndOfTurn();

        }

    }

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

    @Deprecated
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
                if (meetsMinimum || (everyoneIsReady && meetsMinimum)) {
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
        // handle immediate start if all players are ready (doesn't allow people to
        // easily join the room after ready check has begun)
        long numReady = players.values().stream().filter(p -> {
            return p.isReady();
        }).count();
        // condition 1: start if we have the minimum ready
        boolean meetsMinimum = numReady >= Constants.MINIMUM_REQUIRED_TO_START;
        // condition 2: start if everyone is ready
        int totalPlayers = players.size();
        boolean everyoneIsReady = numReady >= totalPlayers;
        if ((everyoneIsReady && meetsMinimum)) {
            if (readyCheckTimer != null) {
                readyCheckTimer.cancel();
                readyCheckTimer = null;
            }
            start();
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
        changePhase(Phase.TURN);
        // make 2d grid
        grid.generate(9, 9);
        sendGridDimensions(grid.getRows(), grid.getColumns());
        // build board
        grid.populate(6);
        // set users to random starts
        List<Cell> starts = grid.getStartCells();
        players.values().stream().forEach(p -> {
            Cell start = starts.get(random.nextInt(starts.size()));
            p.setCell(start);
            sendPlayerPosition(p);
        });

        canEndSession = false;

        numActivePlayers = players.values().stream().filter(ServerPlayer::isReady).count();
        // scale sleep deck based on num of players
        int targetCards = (int) numActivePlayers * 3;// 12;
        for (int i = 0; i < targetCards; i++) {
            dragonSleepDeck.add(false);
        }
        int min = dragonSleepDeck.size() / 2;
        int max = dragonSleepDeck.size();
        int index = random.nextInt(max - min) + min;
        System.out.println(TextFX.colorize(
                "Awake card is index " + index + " out of " + dragonSleepDeck.size() + " chances", Color.PURPLE));
        dragonSleepDeck.set(index, true);
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
            System.out.println(TextFX.colorize("Started turn timer", Color.YELLOW));
            turnTimer = new TimedEvent(60, this::handleEndOfTurn);
            // This will call end() via handleEndOfTurn() multiple times
            // turnTimer.setTickCallback(this::checkEarlyEndTurn);
            sendMessage(ServerConstants.FROM_ROOM, "Pick your actions");
        }
    }

    private void handleEndOfTurn() {
        // don't forget to cancel your timer when applicable
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
        currentPlayer.resetPath();
        // updated logic
        // filter for all true values in dragSleepDeck (there should just be 1)
        // if any are NOT found, game over (it was drawn)
        if (grid.isGridFull() || dragonSleepDeck.stream().filter(d -> d).findAny().isEmpty()) {
            canEndSession = true;
            end();
        } else {
            resetTurns();
            nextTurn();
            startTurnTimer();
        }
    }

    private void resetTurns() {
        players.values().stream().forEach(p -> p.setTakenTurn(false));
        sendResetLocalTurns();
    }

    private void end() {
        // temporary way to not rerun end() multiple times
        if (turnOrder.size() == 0) {
            System.out.println(TextFX.colorize("END TRIGGER MULTIPLE TIMES", Color.RED));
            return;
        }
        System.out.println(TextFX.colorize("Doing game over", Color.YELLOW));
        turnOrder.clear();
        // mark everyone not ready
        players.values().forEach(p -> {
            // TODO fix/optimize, avoid nested loops if/when possible
            p.setReady(false);
            p.setTakenTurn(false);
            p.setCell(null);
            p.setPoints(0);// TODO do score board before we reset stuff
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
    private void sendGameEvent(String message) {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendGameEvent(message);
        }
    }

    private void sendPoints(long clientId, int changedPoints, int currentPoints) {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendPoints(clientId, changedPoints, currentPoints);
        }
    }

    private void sendRoll(long clientId, int roll) {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendRoll(clientId, roll);
        }
    }

    private void sendGridDimensions(int x, int y) {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendGridDimensions(x, y);
        }
    }

    private void sendPlayerPosition(ServerPlayer playerWhoMoved) {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendPlayerPosition(playerWhoMoved.getClientId(), playerWhoMoved.getCellX(), playerWhoMoved.getCellY());
        }
    }

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

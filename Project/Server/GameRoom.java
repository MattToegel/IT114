package Project.Server;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import Project.Common.Constants;
import Project.Common.Phase;
import Project.Common.TextFX;
import Project.Common.TimedEvent;
import Project.Common.TextFX.Color;

public class GameRoom extends Room {

    private ConcurrentHashMap<Long, ServerPlayer> players = new ConcurrentHashMap<Long, ServerPlayer>();

    private TimedEvent readyCheckTimer = null;
    private Phase currentPhase = Phase.READY;

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
            });
        }
    }

    @Override
    protected synchronized void removeClient(ServerThread client) {
        super.removeClient(client);
        // Note: base Room can close (if empty) before GameRoom cleans up (possibly)
        if (players.containsKey(client.getClientId())) {
            players.remove(client.getClientId());
            System.out.println(TextFX.colorize(client.getClientName() + " left GameRoom " + getName(), Color.WHITE));
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

    private synchronized void readyCheck() {
        int MINIMUM_REQUIRED_TO_START = 2;
        if (readyCheckTimer == null) {
            readyCheckTimer = new TimedEvent(30, () -> {
                long numReady = players.values().stream().filter(p -> {
                    return p.isReady();
                }).count();
                // condition 1: start if we have the minimum ready
                boolean meetsMinimum = numReady >= MINIMUM_REQUIRED_TO_START;
                // condition 2: start if everyone is ready
                int totalPlayers = players.size();
                boolean everyoneIsReady = numReady >= totalPlayers;
                if (meetsMinimum || everyoneIsReady) {
                    start();
                } else {
                    sendMessage(null, "Minimum players not met during ready check, please try again");
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
        // initialization of game state
        changePhase(Phase.IN_PROGRESS);
        // the session handles user interactions
        sendMessage(null, "Session started, have fun");
        new TimedEvent(30, () -> {
            sendMessage(null, "Session is over, redo ready check");
            end();
        }).setTickCallback((time) -> {
            sendMessage(null, "Time reamining: " + time);
        });
    }

    private void end() {
        // mark everyone not ready
        players.values().forEach(p -> {
            // TODO fix/optimize, avoid nested loops if/when possible
            p.setReady(false);
            syncReadyState(p);
        });
        changePhase(Phase.READY);

    }

    // start send/sync methods
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

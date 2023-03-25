package HNS.server;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import HNS.common.Constants;
import HNS.common.Phase;
import HNS.common.TimedEvent;

public class GameRoom extends Room {
    Phase currentPhase = Phase.READY;
    private static Logger logger = Logger.getLogger(GameRoom.class.getName());
    private TimedEvent readyTimer = null;
    private ConcurrentHashMap<Long, Player> players = new ConcurrentHashMap<Long, Player>();

    public GameRoom(String name) {
        super(name);
    }

    @Override
    protected void addClient(ServerThread client) {
        logger.info("Adding client as player");
        players.computeIfAbsent(client.getClientId(), id -> {
            Player player = new Player(client);
            super.addClient(client);
            logger.info(String.format("Total clients %s", clients.size()));
            return player;
        });
    }

    protected void setReady(ServerThread client) {
        logger.info("Ready check triggered");
        if (currentPhase != Phase.READY) {
            logger.warning(String.format("readyCheck() incorrect phase: %s", Phase.READY.name()));
            return;
        }
        if (readyTimer == null) {
            sendMessage(null, "Ready Check Initiated, 30 seconds to join");
            readyTimer = new TimedEvent(30, () -> {
                readyTimer = null;
                readyCheck(true);
            });
        }
        players.values().stream().filter(p -> p.getClient().getClientId() == client.getClientId()).findFirst()
                .ifPresent(p -> {
                    p.setReady(true);
                    logger.info(String.format("Marked player %s[%s] as ready", p.getClient().getClientName(), p
                            .getClient().getClientId()));
                    syncReadyStatus(p.getClient().getClientId());
                });
        readyCheck(false);
    }

    private void readyCheck(boolean timerExpired) {
        if (currentPhase != Phase.READY) {
            return;
        }
        // two examples for the same result
        // int numReady = players.values().stream().mapToInt((p) -> p.isReady() ? 1 :
        // 0).sum();
        long numReady = players.values().stream().filter(Player::isReady).count();
        if (numReady >= Constants.MINIMUM_PLAYERS) {
            updatePhase(Phase.IN_PROGRESS);
            if (timerExpired) {
                sendMessage(null, "Ready Timer expired, starting session");
            } else if (numReady >= players.size()) {
                sendMessage(null, "Everyone in the room marked themselves ready, starting session");
                if (readyTimer != null) {
                    readyTimer.cancel();
                    readyTimer = null;
                }
            }
            start();
        } else {
            if (timerExpired) {
                resetSession();
                sendMessage(null, "Ready Timer expired, not enough players. Resetting ready check");
            }
        }
    }

    private void start() {
        // TODO example
        sendMessage(null, "Session started");
        new TimedEvent(30, () -> resetSession())
                .setTickCallback((time) -> {
                    sendMessage(null, String.format("Example running session, time remaining: %s", time));
                });
    }

    private void resetSession() {
        players.values().stream().forEach(p -> p.setReady(false));
        updatePhase(Phase.READY);
        sendMessage(null, "Session ended, please intiate ready check to begin a new one");
    }

    private void updatePhase(Phase phase) {
        if (currentPhase == phase) {
            return;
        }
        currentPhase = phase;
        // NOTE: since the collection can yield a removal during iteration, an iterator
        // is better than relying on forEach
        Iterator<Player> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            Player client = iter.next();
            boolean success = client.getClient().sendPhaseSync(currentPhase);
            if (!success) {
                handleDisconnect(client);
            }
        }
    }

    protected void handleDisconnect(Player player) {
        if (players.containsKey(player.getClient().getClientId())) {
            players.remove(player.getClient().getClientId());
            super.handleDisconnect(null, player.getClient());
            logger.info(String.format("Total clients %s", clients.size()));
            sendMessage(null, player.getClient().getClientName() + " disconnected");
            if (players.isEmpty()) {
                close();
            }
        }
    }

    private void syncReadyStatus(long clientId) {
        Iterator<Player> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            Player client = iter.next();
            boolean success = client.getClient().sendReadyStatus(clientId);
            if (!success) {
                handleDisconnect(client);
            }
        }
    }

}

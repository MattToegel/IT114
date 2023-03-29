package HNS.server;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import HNS.common.Cell;
import HNS.common.Constants;
import HNS.common.Grid;
import HNS.common.Phase;
import HNS.common.TimedEvent;

public class GameRoom extends Room {
    Phase currentPhase = Phase.READY;
    private static Logger logger = Logger.getLogger(GameRoom.class.getName());
    private TimedEvent readyTimer = null;
    private ConcurrentHashMap<Long, ServerPlayer> players = new ConcurrentHashMap<Long, ServerPlayer>();
    private Grid grid = new Grid();
    private ServerPlayer currentSeeker = null;
    private int seeksPerRound = 1;
    private int maxSeeksPerRound = 1;
    private int rounds = 0;
    private int maxRounds = 10;
    private TimedEvent roundTimer = null;

    public GameRoom(String name) {
        super(name);
    }

    @Override
    protected void addClient(ServerThread client) {
        logger.info("Adding client as player");
        players.computeIfAbsent(client.getClientId(), id -> {
            ServerPlayer player = new ServerPlayer(client);
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
        long numReady = players.values().stream().filter(ServerPlayer::isReady).count();
        if (numReady >= Constants.MINIMUM_PLAYERS) {

            if (timerExpired) {
                sendMessage(null, "Ready Timer expired, starting session");
                start();
            } else if (numReady >= players.size()) {
                sendMessage(null, "Everyone in the room marked themselves ready, starting session");
                if (readyTimer != null) {
                    readyTimer.cancel();
                    readyTimer = null;
                }
                start();
            }

        } else {
            if (timerExpired) {
                resetSession();
                sendMessage(null, "Ready Timer expired, not enough players. Resetting ready check");
            }
        }
    }

    private void start() {
        grid.build(5, 5);
        rounds = maxRounds;
        startRound();
    }

    private synchronized void resetSession() {
        grid.reset();
        players.values().stream().forEach(p -> {
            p.setPoints(0);
            p.setIsOut(false);
            p.setReady(false);
        });
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
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            ServerPlayer client = iter.next();
            boolean success = client.getClient().sendPhaseSync(currentPhase);
            if (!success) {
                handleDisconnect(client);
            }
        }
    }

    protected void handleDisconnect(ServerPlayer player) {
        if (players.containsKey(player.getClient().getClientId())) {
            players.remove(player.getClient().getClientId());
            super.handleDisconnect(null, player.getClient());
            logger.info(String.format("Total clients %s", clients.size()));
            sendMessage(null, player.getClient().getClientName() + " disconnected");
            if (currentSeeker.isSame(player)) {
                logger.info("Seeker disconnected, picking new seeker");
                pickSeeker();
            }
            if (players.isEmpty()) {
                close();
            }
        }
    }

    private void syncReadyStatus(long clientId) {
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            ServerPlayer client = iter.next();
            boolean success = client.getClient().sendReadyStatus(clientId);
            if (!success) {
                handleDisconnect(client);
            }
        }
    }

    /**
     * Picks initial seeker randomly or next seeker in order.
     * Resets seeks per round based on max seeks per round.
     * If a seeker isn't found the session resets.
     */
    private void pickSeeker() {
        if (currentSeeker == null && players.values().stream().count() > 0) {
            // orElse() triggers when list is empty, shouldn't happen
            currentSeeker = players.values().stream().filter(p -> p.isReady()).findAny().orElse(null);
        } else {
            // find the next player in order or default to first ready
            int currentSeekerIndex = players.values().stream().toList().indexOf(currentSeeker) + 1;
            currentSeeker = players.values().stream().skip(currentSeekerIndex).findFirst().orElse(
                    players.values().stream().filter(p -> p.isReady()).findFirst().orElse(null));
        }
        if (currentSeeker != null) {
            seeksPerRound = maxSeeksPerRound;
            syncSeeker(currentSeeker.getClient().getClientId());
        } else {
            logger.severe("pickSeeker() seeker found as null");
            sendMessage(null, "Couldn't find a valid seeker, ending session");
            resetSession();
        }
    }

    /**
     * Sends everyone the id of the current seeker
     * 
     * @param clientId
     */
    private void syncSeeker(long clientId) {
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            boolean success = sp.getClient().sendSeeker(clientId);
            if (!success) {
                handleDisconnect(sp);
            }
        }
    }

    // GRID STUFF
    /**
     * Given coordinates and a client id, it'll attempt the HIDE or SEEK action
     * based on phase.
     * {@link GameRoom#seek(int, int)} or {@link GameRoom#hide(int, int, long)}
     * 
     * @param x
     * @param y
     * @param clientId
     */
    public void setHidePosition(int x, int y, long clientId) {
        if (currentPhase != Phase.HIDE) {
            sendMessage(null, "You can't hide at this time");
            return;
        }
        if (currentPhase == Phase.HIDE && currentSeeker.getClient().getClientId() != clientId) {
            hide(x, y, clientId);
        } else {
            sendMessage(null, "Sorry the seeker can't hide");
        }
    }

    public void checkSeekPosition(int x, int y, long clientId) {
        if (currentPhase != Phase.SEEK && currentSeeker.getClient().getClientId() == clientId) {
            sendMessage(null, "You can't seek at this time");
            return;
        }
        if (currentPhase == Phase.SEEK && currentSeeker.getClient().getClientId() == clientId) {
            seek(x, y, clientId);
        } else {
            sendMessage(null, "Only the seeker can seek.");
        }
    }

    /**
     * Given valid coordinates, sets the player to that cell.
     * Removes them from any previous cell if applicable.
     * Calls {@link GameRoom#syncHideConfirm(int, int, ServerPlayer)} to all except
     * seeker(s)
     * 
     * @param x
     * @param y
     * @param clientId
     */
    private void hide(int x, int y, long clientId) {
        if (players.containsKey(clientId)) {
            ServerPlayer sp = players.get(clientId);
            if (sp.getCurrentCell() != null) {
                grid.removePlayerFromCell(sp.getCurrentCell().getX(), sp.getCurrentCell().getY(), sp);
            }
            boolean didHide = grid.addPlayerToCell(x, y, sp);
            if (didHide) {
                syncHideConfirm(x, y, sp);
            }
        }
    }

    /**
     * Attempts to find a cell by coordinate and retrieve all the players there.
     * Each player found will be marked out and reward the current seeker.
     * Lastly shifts to {@link GameRoom#checkEnd()}
     * 
     * @param x
     * @param y
     */
    private void seek(int x, int y, long clientId) {
        if (seeksPerRound <= 0) {
            return;
        }
        if (players.containsKey(clientId)) {
            try {
                Cell c = grid.getCell(x, y);
                if (c != null) {
                    seeksPerRound--;// limited seeks per round
                    // get players in cell to mark "out"
                    List<ServerPlayer> players = c.getPlayersInCell().stream().map(p -> (ServerPlayer) p).toList();
                    Iterator<ServerPlayer> iter = players.iterator();
                    while (iter.hasNext()) {
                        ServerPlayer sp = iter.next();
                        sp.setIsOut(true);
                        syncIsOut(sp.getClient().getClientId());
                    }
                    // award 1 point per found player
                    int playersInCell = players.size();
                    if (playersInCell > 0) {
                        currentSeeker.changePoints(playersInCell);
                        sendMessage(null, String.format("Seeker gained %s points!", playersInCell));
                    } else {
                        sendMessage(null, "Seeker didn't find anyone this round");
                    }
                    c.reset();// clear the cell so we don't handle it again
                    checkEnd();
                }
            } catch (Exception e) {
                logger.severe(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Resets the grid.
     * Resets out players.
     * Picks a seeker.
     * Updates Phase to HIDE.
     * Starts Game Timers in phases: HIDE -> SEEK
     */
    private synchronized void startRound() {
        grid.reset();
        players.values().stream().forEach(p -> p.setIsOut(false));
        pickSeeker();
        updatePhase(Phase.HIDE);
        if (roundTimer != null) {
            roundTimer.cancel();
            roundTimer = null;
        }
        roundTimer = new TimedEvent(30, () -> {
            sendMessage(null, "Times up! Time to seek.");
            updatePhase(Phase.SEEK);
            roundTimer = new TimedEvent(30, () -> {
                seeksPerRound = 0;
                sendMessage(null, "Times up! Time to seek.");
                checkEnd();
            });
        });
    }

    /**
     * Checks if all players are found for bonus points (TBD).
     * Checks if seeksPerRound is expired.
     * Resets the session (READY) or goes to next round
     * {@link GameRoom#startRound()}.
     */
    private void checkEnd() {
        boolean didEnd = false;
        // no more hiding players, seeker wins
        if (grid.remaining() == 0) {
            sendMessage(null, "Seeker wins bonus points!");
            didEnd = true;
        }
        // seeks expired, let players hide again
        if (seeksPerRound <= 0) {
            if (roundTimer != null) {
                roundTimer.cancel();
                roundTimer = null;
            }
            sendMessage(null, "Seeker ran out of seeks.");
            int remaining = grid.remaining();
            if (remaining > 0) {
                currentSeeker.changePoints(-remaining);
                sendMessage(null, String.format("There were %s remaining players, seeker loses %s points.",
                        remaining, remaining));
            }
            didEnd = true;
        }
        if (didEnd) {
            rounds--;
            if (rounds <= 0) {// game session over
                sendMessage(null, "Game Over!");
                resetSession();
                return;
            }
            // next round
            sendMessage(null, "Next round!");
            startRound();
        }
    }

    /**
     * Sends all hiders a confirmation of a hiding player's position.
     * This will later update UI for hiders
     * Note: Seeker doesn't receive this to prevent cheating.
     * 
     * @param x
     * @param y
     * @param hider
     */
    private void syncHideConfirm(int x, int y, ServerPlayer hider) {
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            if (sp != currentSeeker && sp.getClient().getClientId() != currentSeeker.getClient().getClientId()) {
                boolean success = sp.getClient().sendHidePosition(x, y, sp.getClient().getClientId());
                if (!success) {
                    handleDisconnect(sp);
                }
            }
        }
    }

    /**
     * Syncs to everyone that a specific client is out for a round
     * 
     * @param clientId
     */
    private void syncIsOut(long clientId) {
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            if (sp != currentSeeker && sp.getClient().getClientId() != currentSeeker.getClient().getClientId()) {
                boolean success = sp.getClient().sendOut(clientId);
                if (!success) {
                    handleDisconnect(sp);
                }
            }
        }
    }
}

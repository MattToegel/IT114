package LifeForLife.server;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import LifeForLife.common.Constants;
import LifeForLife.common.GeneralUtils;
import LifeForLife.common.MyLogger;
import LifeForLife.common.Player;
import LifeForLife.common.Vector2;

public class GameRoom extends Room {

    // private static Logger logger = Logger.getLogger(GameRoom.class.getName());
    private static MyLogger logger = MyLogger.getLogger(GameRoom.class.getName());

    private List<Player> players = Collections.synchronizedList(new ArrayList<Player>());
    private Thread gameLoop = null;
    private Rectangle arenaBounds = new Rectangle(800, 600);
    // Constants

    public GameRoom(String name) {
        super(name);
    }

    @Override
    protected synchronized void addClient(ServerThread client) {
        super.addClient(client);
        Player player = new Player(client);
        players.add(player);
        syncReadyStatus(player);
    }

    @Override
    protected synchronized void removeClient(ServerThread client) {
        super.removeClient(client);

        boolean removed = players.removeIf(p -> p.getClientId() == client.getClientId()); // TODO see if this works w/o
                                                                                          // loop
        logger.info("GameRoom Removed Player: " + (removed ? "true" : "false"));

    }

    public synchronized void setReady(long clientId) {
        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            while (iter.hasNext()) {
                Player p = iter.next();
                if (p != null && !p.isReady() && p.getClientId() == clientId) {
                    p.setIsReady(true);
                    logger.info(p.getClientName() + " is ready");
                    break;
                }
            }
        }
        sendReadyStatus(clientId);
        readyCheck();
    }

    private synchronized void syncReadyStatus(Player incoming) {
        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            while (iter.hasNext()) {
                Player p = iter.next();
                if (p != null && p.isReady() && p.getClientId() != incoming.getClientId()) {
                    boolean messageSent = incoming.getClient().sendReadyStatus(p.getClientId());
                    if (!messageSent) {
                        iter.remove();
                        logger.info("Removed client " + incoming.getClientName());
                        checkClients();
                        sendConnectionStatus(incoming.getClient(), false);
                    }
                }
            }
        }
    }

    private synchronized void sendReadyStatus(long clientId) {
        if (players == null) {
            return;
        }
        synchronized (players) {
            for (int i = players.size() - 1; i >= 0; i--) {
                Player player = players.get(i);
                boolean messageSent = player.getClient().sendReadyStatus(clientId);
                if (!messageSent) {
                    players.remove(i);
                    logger.info("Removed client " + player.getClientName());
                    checkClients();
                    sendConnectionStatus(player.getClient(), false);
                }
            }
        }
    }

    private synchronized void readyCheck() {
        int numReady = 0;
        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            while (iter.hasNext()) {
                Player p = iter.next();
                if (p != null && p.isReady()) {
                    numReady++;
                }
            }
        }
        if (numReady >= Constants.MINIMUM_PLAYERS && numReady >= players.size()) {
            setupGame();
        }
    }

    private void setupGame() {
        logger.info("Initializing Game");

        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            logger.info("Arena: " + arenaBounds);
            while (iter.hasNext()) {
                Player p = iter.next();
                if (p != null && p.isReady()) {
                    p.setLife(Constants.STARTING_LIFE);
                    Vector2 startingPosition = new Vector2(
                            GeneralUtils.randomRange((int) arenaBounds.getMinX(), (int) arenaBounds.getMaxX()),
                            GeneralUtils.randomRange((int) arenaBounds.getMinY(), (int) arenaBounds.getMaxY()));
                    p.setPosition(startingPosition);
                    logger.info(String.format("Player %s moved to %s", p.getClientName(), p.getPosition()));
                    broadcastPositionAndRotation(p);
                    broadcastLife(p);
                    // p.getClient().sendCurrentLife(Constants.STARTING_LIFE);
                }
            }
        }
        logger.info("Ready to play");
        broadcastStart();
        if (gameLoop == null) {
            // TODO GAME LOOP
            gameLoop = new Thread() {
                @Override
                public void run() {
                    logger.info("Game Loop Starting");
                    while (isRunning()) {
                        synchronized (players) {
                            Iterator<Player> iter = players.iterator();
                            while (iter.hasNext()) {
                                Player p = iter.next();
                                if (p != null && p.isReady()) {
                                    p.move(arenaBounds);
                                    broadcastPositionAndRotation(p);
                                }
                            }
                        }
                        try {
                            Thread.sleep(16);
                        } catch (Exception e) {
                        }
                    }
                    logger.info("Game Loop Exiting");
                }
            };
            gameLoop.start();
        }
    }

    private synchronized void broadcastLife(Player playerChanged) {
        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            while (iter.hasNext()) {
                Player p = iter.next();
                if (p != null && p.isReady()) {
                    boolean messageSent = p.getClient().sendCurrentLife(playerChanged.getClientId(),
                            playerChanged.getLife());
                    if (!messageSent) {
                        logger.severe("Failed to send message to " + p.getClientName());
                    }
                }
            }
        }
    }

    private synchronized void broadcastStart() {
        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            while (iter.hasNext()) {
                Player p = iter.next();
                if (p != null && p.isReady()) {
                    boolean messageSent = p.getClient().sendStart();
                    if (!messageSent) {
                        logger.severe("Failed to send message to " + p.getClientName());
                    }
                }
            }
        }
    }

    private synchronized void broadcastPositionAndRotation(Player playerChanged) {
        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            logger.info("Sending player: " + playerChanged);
            while (iter.hasNext()) {
                Player p = iter.next();
                if (p != null && p.isReady()) {

                    boolean messageSent = p.getClient().sendPRH(
                            playerChanged.getClientId(),
                            playerChanged.getPosition(),
                            playerChanged.getHeading(),
                            playerChanged.getRotation());
                    if (!messageSent) {
                        logger.severe("Failed to send message to " + p.getClientName());
                    }
                }
            }
        }
    }

    public void setPlayerHeadingAndRotation(long clientId, Vector2 heading, float rotation) {
        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            while (iter.hasNext()) {
                Player p = iter.next();
                if (p != null && p.isReady() && p.getClientId() == clientId) {
                    p.setHeading(heading);
                    p.setRotation(rotation);
                    break;
                }
            }
        }
    }
}

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
import LifeForLife.common.Projectile;
import LifeForLife.common.ProjectilePool;
import LifeForLife.common.Vector2;

public class GameRoom extends Room {

    // private static Logger logger = Logger.getLogger(GameRoom.class.getName());
    private static MyLogger logger = MyLogger.getLogger(GameRoom.class.getName());

    private List<Player> players = Collections.synchronizedList(new ArrayList<Player>());
    private Thread gameLoop = null;
    private Rectangle arenaBounds = new Rectangle(800, 600);
    private ProjectilePool projectilePool = new ProjectilePool(10);
    // Constants

    public GameRoom(String name) {
        super(name);
    }

    @Override
    protected synchronized void addClient(ServerThread client) {
        super.addClient(client);
        Player player = new Player(client,projectilePool);
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
                    //broadcastPositionAndRotation(p);//removed to avoid deadlock issues
                    //broadcastLife(p);//removed to avoid deadlock issues
                    broadcastGameData();
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
                    /*ProjectilePool.INSTANCE.setSyncCallback((p) -> {
                        //broadcastProjectileSync(p);//removed to avoid deadlock issues
                    });*/
                    projectilePool.setCollisionCallback(hitData -> {
                        if (hitData == null) {
                            logger.info("Hit data received as null");
                            return;
                        }
                        Player hitPlayer = players.stream().filter(p -> p.getClientId() == hitData.targetClientId)
                                .findFirst().orElse(null);
                        if (hitPlayer != null) {
                            if (hitData.didPickup) {
                                hitPlayer.modifyLife(hitData.life);
                                //sendMessage(null,
                                //        String.format("%s gain %s life", hitPlayer.getClientName(), hitData.life));
                            } else {
                                hitPlayer.modifyLife(-hitData.life);
                                Player shooter = players.stream().filter(p->p.getClientId() == hitData.sourceClientId).findFirst().orElse(null);
                                if(shooter != null){
                                    shooter.modifyLife(hitData.life);
                                }
                                //sendMessage(null,
                                 //       String.format("%s lost %s life", hitPlayer.getClientName(), hitData.life));
                            }
                            //broadcastLife(hitPlayer);//removed to avoid deadlock issues
                        }
                    });
                    while (isRunning()) {
                        // move projectiles
                        projectilePool.move(arenaBounds);
                        // move players
                        synchronized (players) {
                            Iterator<Player> iter = players.iterator();
                            while (iter.hasNext()) {
                                Player p = iter.next();
                                if (p != null && p.isReady()) {
                                    p.move(arenaBounds);
                                    //broadcastPositionAndRotation(p);
                                    // check collisions
                                    projectilePool.checkCollision(p);
                                }
                            }
                        }
                        broadcastGameData();
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
    /**
     * Handles periodic syncing of data to avoid deadlock issues
     */
    private void broadcastGameData() {
        List<Projectile> activeProjectiles = projectilePool.getPendingProjectiles();
        List<Player> activePlayers = players.stream().filter(p -> p.hasPendingUpdate()).toList();
        synchronized (players) {
            for (int i = players.size() - 1; i >= 0; i--) {
                Player p = players.get(i);
                if (p != null) {
                    boolean messageSent = true;
                    // sync projectils to player i
                    for (int pri = 0, l = activeProjectiles.size(); pri < l; pri++) {
                        Projectile pr = activeProjectiles.get(pri);
                        if (pr != null) {
                            messageSent = p.getClient().sendProjectileSync(pr.getClientId(),
                                    pr.getProjectileId(),
                                    pr.getPosition(),
                                    pr.getHeading(),
                                    pr.getLife(),
                                    pr.getSpeed());
                            if (!messageSent) {
                                break;
                            }
                        }
                    }
                    for (int pi = 0, l = activePlayers.size(); pi < l; pi++) {
                        Player ap = activePlayers.get(pi);
                        if (ap != null) {
                            //sync life
                            messageSent = p.getClient().sendCurrentLife(ap.getClientId(),
                                    ap.getLife());
                            if (!messageSent) {
                                break;
                            }
                            //sync position/rotation
                            messageSent = p.getClient().sendPRH(
                                    ap.getClientId(),
                                    ap.getPosition(),
                                    ap.getHeading(),
                                    ap.getRotation());
                            if (!messageSent) {
                                break;
                            }
                        }
                    }
                    if (!messageSent) {
                        players.remove(i);
                        logger.severe("Failed to send message to " + p.getClientName());
                        logger.info("Removed client " + p.getClientName());
                        checkClients();
                        sendConnectionStatus(p.getClient(), false);
                    }
                } else {
                    players.remove(i);
                    logger.warning("Removing null player");
                }
            }
            //reset pending projectiles
            for (int pri = 0, l = activeProjectiles.size(); pri < l; pri++) {
                Projectile pr = activeProjectiles.get(pri);
                if (pr != null) {
                    pr.resetPendingUpdate();
                }
            }
            //reset pending players
            for (int pi = 0, l = activePlayers.size(); pi < l; pi++) {
                Player ap = activePlayers.get(pi);
                if (ap != null) {
                    ap.resetPendingUpdate();
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

    public void triggerPlayerShoot(long clientId) {
        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            while (iter.hasNext()) {
                Player p = iter.next();
                if (p != null && p.getClientId() == clientId) {
                    if (p.isReady() && p.getLife() > 1) {
                        // Projectile pr = ProjectilePool.INSTANCE.spawn();
                        // pr.setData(clientId, p.getPosition(), p.getFacingDirection(), 1);
                        Projectile pr = p.shoot(p.getFacingDirection(), 1);

                        if (pr != null) {
                            // sendMessage(null, String.format("Projectile %s %s", pr.getPosition(),
                            // pr.getHeading()));
                            //broadcastProjectileSync(pr);
                            p.modifyLife(-pr.getLife());
                            //broadcastLife(p);
                        }
                    }
                    break;
                }
            }
        }
    }
}

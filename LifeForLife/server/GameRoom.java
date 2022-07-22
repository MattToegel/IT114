package LifeForLife.server;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import LifeForLife.common.Constants;
import LifeForLife.common.Countdown;
import LifeForLife.common.GeneralUtils;
import LifeForLife.common.MyLogger;
import LifeForLife.common.Phase;
import LifeForLife.common.Player;
import LifeForLife.common.Projectile;
import LifeForLife.common.ProjectilePool;
import LifeForLife.common.Throttle;
import LifeForLife.common.Vector2;

public class GameRoom extends Room {

    // private static Logger logger = Logger.getLogger(GameRoom.class.getName());
    private static MyLogger logger = MyLogger.getLogger(GameRoom.class.getName());

    private List<Player> players = Collections.synchronizedList(new ArrayList<Player>());
    private Thread gameLoop = null;
    private Rectangle arenaBounds = new Rectangle(800, 600);
    private ProjectilePool projectilePool = new ProjectilePool(10);
    private Countdown gameTimer;
    private Phase currentPhase = Phase.READY_CHECK;
    private Throttle delayClockSync = new Throttle(250);
    // Constants

    public GameRoom(String name) {
        super(name);
    }

    @Override
    protected synchronized void addClient(ServerThread client) {
        super.addClient(client);
        Player player = new Player(client, projectilePool);
        players.add(player);
        syncReadyStatus(player);
        player.getClient().sendCurrentPhase(currentPhase);
    }

    @Override
    protected synchronized void removeClient(ServerThread client) {
        super.removeClient(client);

        boolean removed = players.removeIf(p -> p.getClientId() == client.getClientId()); // TODO see if this works w/o
                                                                                          // loop
        logger.info("GameRoom Removed Player: " + (removed ? "true" : "false"));
        if(currentPhase == Phase.READY_CHECK){
            readyCheck();
        }
    }

    public synchronized void setReady(long clientId) {
        if(currentPhase != Phase.READY_CHECK){
            sendMessage(null, "Current phase is not ready check");
            return;
        }
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
        if (currentPhase == Phase.READY_CHECK) {
            currentPhase = Phase.BATTLE;
        } else if (currentPhase == Phase.BATTLE) {
            sendMessage(null, "Game is already in progress");
            return;
        }
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
                    p.forceUpdate();
                    logger.info(String.format("Player %s moved to %s", p.getClientName(), p.getPosition()));
                    // broadcastPositionAndRotation(p);//removed to avoid deadlock issues
                    // broadcastLife(p);//removed to avoid deadlock issues
                    broadcastGameData();
                    // p.getClient().sendCurrentLife(Constants.STARTING_LIFE);
                }
            }
        }
        logger.info("Ready to play");
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        gameTimer = new Countdown("Duration", 60 * 5, () -> {
            logger.info("Game Over: Time Expired");
            gameOver();
        });
        broadcastStart();
        if (gameLoop == null) {
            // TODO GAME LOOP
            gameLoop = new Thread() {
                @Override
                public void run() {
                    logger.info("Game Loop Starting");
                    sendMessage(null, """
                            <h2>Rules</h2>
                            Free-For-All
                            <ul>
                                <li>Each shot costs 1 life</li>
                                <li>Each hit returns 1 life</li>
                                <li>Stationary projectiles can be picked up to heal 1 life</li>
                                <li>Game Round lasts for 5 minutes or until there is only 1 person remaining</li>
                                <li>Players at 0 life can pickup stationary projectiles to get back in the game unless a game over condition triggers</li>
                            </ul>
                            """);   
                    /*
                     * ProjectilePool.INSTANCE.setSyncCallback((p) -> {
                     * //broadcastProjectileSync(p);//removed to avoid deadlock issues
                     * });
                     */
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
                                // sendMessage(null,
                                // String.format("%s gain %s life", hitPlayer.getClientName(), hitData.life));
                            } else {
                                hitPlayer.modifyLife(-hitData.life);
                                hitPlayer.addGotHit();
                                Player shooter = players.stream().filter(p -> p.getClientId() == hitData.sourceClientId)
                                        .findFirst().orElse(null);
                                if (shooter != null) {
                                    shooter.modifyLife(hitData.life);
                                    shooter.addHitTarget();
                                }
                                // sendMessage(null,
                                // String.format("%s lost %s life", hitPlayer.getClientName(), hitData.life));
                            }
                            // broadcastLife(hitPlayer);//removed to avoid deadlock issues
                        }
                    });
                    while (isRunning() && currentPhase == Phase.BATTLE) {
                        // move projectiles
                        projectilePool.move(arenaBounds);
                        // move players
                        int playersRemaining = 0;
                        synchronized (players) {
                            Iterator<Player> iter = players.iterator();
                            while (iter.hasNext()) {
                                Player p = iter.next();
                                if (p != null && p.isReady()) {
                                    p.move(arenaBounds);
                                    // broadcastPositionAndRotation(p);

                                    // check collisions
                                    projectilePool.checkCollision(p);
                                    //check players remaining (i.e., still alive)
                                    if (p.getLife() > 0) {
                                        playersRemaining++;
                                    }
                                }
                            }
                        }
                        broadcastGameData();
                        if (playersRemaining <= 1) {
                            logger.info("Game Over: Last man standing");
                            gameOver();
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
                            // sync life
                            messageSent = p.getClient().sendCurrentLife(ap.getClientId(),
                                    ap.getLife());
                            if (!messageSent) {
                                break;
                            }
                            // sync position/rotation
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
                    if (delayClockSync.ready() && gameTimer != null) {
                        messageSent = p.getClient().sendClockSync(gameTimer.getRemainingTime());
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
            // reset pending projectiles
            for (int pri = 0, l = activeProjectiles.size(); pri < l; pri++) {
                Projectile pr = activeProjectiles.get(pri);
                if (pr != null) {
                    pr.resetPendingUpdate();
                }
            }
            // reset pending players
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
                            // broadcastProjectileSync(pr);
                            p.modifyLife(-pr.getLife());
                            // broadcastLife(p);
                        }
                    }
                    break;
                }
            }
        }
    }

    private void broadcastPhase(Phase phase) {
        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            while (iter.hasNext()) {
                Player p = iter.next();
                if (p != null) {
                    p.getClient().sendCurrentPhase(phase);
                }
            }
        }
    }

    private void gameOver() {
        if (currentPhase == Phase.END_GAME) {
            return;
        } else {
            currentPhase = Phase.END_GAME;
            broadcastPhase(currentPhase);
            // player with highest life is winner
            players.sort((a, b) -> {
                if (a.getLife() == b.getLife()) {
                    return 0;
                } else if (a.getLife() < b.getLife()) {
                    return 1;
                }
                return -1;
            });
            Player winner = players.get(0);
            long topLife = winner.getLife();
            // secondary sort if more than one player has the same life value
            List<Player> sameLife = players.stream().filter(p -> p.getLife() == topLife).toList();
            if (sameLife.size() > 1) {
                // secondary sort based on # of targets hit
                sameLife.sort((a, b) -> {
                    if (a.getTargetHits() == b.getTargetHits()) {
                        return 0;
                    } else if (a.getTargetHits() < b.getTargetHits()) {
                        return 1;
                    }
                    return -1;
                });
                winner = sameLife.get(0);
            }
            // TODO can add other sorting metrics in the future

            //generate scoreboard
            List<Player> scoreboard = players.stream().filter(p -> p.isReady()).toList();
            StringBuilder sb = new StringBuilder();
            sb.append("""
                    <h2>Scoreboard</h2>
                    <table>
                    <thead>
                        <tr>
                            <td>Rank</td>
                            <td>Player</td>
                            <td>Life</td>
                            <td>Targets Hit</td>
                            <td>Got Hit</td>
                        </tr>
                    </thead>
                        <tbody>
                            """);
            for (int i = 0, l = scoreboard.size(); i < l; i++) {
                Player p = scoreboard.get(i);
                sb.append(String.format("""
                        <tr style=\"%s\">
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                        </tr>
                        """,
                        p.getClientId() == winner.getClientId() ? "font-weight:bold;" : "",
                        (i+1),
                        p.getClient().getFormattedName(),
                        p.getLife(),
                        p.getTargetHits(),
                        p.getHits()));
                        //reset player data
                        p.setIsReady(false);
                        p.reset();
            }
            sb.append("""
                        </tbody>
                        </table>
                    """);
            final String scoreString = sb.toString();
            sendMessage(null, scoreString);
            projectilePool.reset();
            if (gameTimer != null) {
                gameTimer.cancel();
            }
            gameLoop = null;
            gameTimer = new Countdown("Restart", 5, ()->{
                currentPhase = Phase.READY_CHECK;
                broadcastPhase(currentPhase);
            });
        }
    }
}

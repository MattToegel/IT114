package AnteMatter.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import AnteMatter.common.Constants;
import AnteMatter.common.Countdown;
import AnteMatter.common.MyLogger;
import AnteMatter.common.Phase;
import AnteMatter.common.Player;

public class GameRoom extends Room {
    private long actualMatter;// total ante from all players
    private long estMaxRoundMatter = 0;
    private int round = 0;

    private static MyLogger logger = MyLogger.getLogger(GameRoom.class.getName());

    private List<Player> players = Collections.synchronizedList(new ArrayList<Player>());
    private long currentPlayer = Constants.DEFAULT_CLIENT_ID;
    private Countdown turnTimer;
    private Phase currentPhase = Phase.READY_CHECK;

    public GameRoom(String name) {
        super(name);

    }

    @Override
    protected synchronized void addClient(ServerThread client) {
        super.addClient(client);
        Player player = new Player(client);
        players.add(player);
        player.getClient().sendCurrentPhase(currentPhase);
    }

    @Override
    protected synchronized void removeClient(ServerThread client) {
        super.removeClient(client);

        boolean removed = players.removeIf(p -> p.getClientId() == client.getClientId()); // TODO see if this works w/o
                                                                                          // loop
        logger.info("GameRoom Removed Player: " + (removed ? "true" : "false"));
        checkClients();
        if (currentPhase == Phase.READY_CHECK) {
            readyCheck();
        }
    }

    @Override
    protected void checkClients() {
        if (!getName().equalsIgnoreCase(Constants.LOBBY) && players.size() == 0) {
            close();
        }
    }

    /**
     * Initiated from ServerThread marking that their client is ready.
     * Broadcasts the ready status to all clients (via ServerThread).
     * Ends with a call to readyCheck()
     * 
     * @param clientId
     */
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

    /**
     * Broadcasts to all clients (ServerThread) that a specific client is ready
     * 
     * @param clientId
     */
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

    /**
     * Counts the number of ready clients and compares the total against the minimum
     * player count or the total participants.
     */
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
            currentPhase = Phase.ANTE;
        }

        logger.info("Initializing Game");
        round = 0;
        nextRound();// start off the cycle
    }

    /**
     * Resets estMaxRoundMatter and actualMatter values
     */
    private void resetReward() {
        logger.info("Resetting Matter matters");
        estMaxRoundMatter = 0;
        actualMatter = 0;

    }

    /**
     * Moves to the next round and resets player state based on the round.
     * Round 1 it'll intialize game defaults and shuffle the players list.
     */
    private synchronized void nextRound() {
        logger.info("Preparing next round");
        round++;
        if (round == 1) {
            Collections.shuffle(players);
            resetReward();
        } else {
            estMaxRoundMatter = actualMatter;
        }
        sendMessage(null, "<b>Starting round " + round + "</b>");
        logger.info("Current Estimate: " + estMaxRoundMatter);
        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            while (iter.hasNext()) {
                Player p = iter.next();
                if (p != null && p.isReady()) {
                    p.resetGuess();
                    p.setAnte(0);
                    if (round == 1) {
                        p.setMatter(Constants.STARTING_MATTER);
                    }
                    boolean isOut = p.getMatter() <= 0;
                    p.setIsOut(isOut);

                    // should be 0-10 per player
                    // Using min() is correct logic if each round is guaranteed a winner
                    // using current matter lets rounds roll over if there's not a winner
                    // I mistakenly chose the min() one first
                    estMaxRoundMatter += p.getMatter();// Math.min(p.getMatter(), Constants.STARTING_MATTER);
                    // will only broadcast matter at the beginning of the round
                    broadcastMatter(p);
                }
            }
        }
        logger.info("New Current Estimate: " + estMaxRoundMatter);
        nextPlayer();
    }

    private synchronized void broadcastMatter(Player playerChanged) {
        logger.info("Broadcasting matter");
        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            while (iter.hasNext()) {
                Player p = iter.next();
                if (p != null && p.isReady()) {
                    boolean messageSent = p.getClient().sendCurrentMatter(playerChanged.getClientId(),
                            playerChanged.getMatter());
                    if (!messageSent) {
                        logger.severe("Failed to send message to " + p.getClientName());
                    }
                }
            }
        }
    }

    public void setAnteAndGuess(long clientId, long ante, long guess) {
        if (currentPhase != Phase.ANTE) {
            sendMessage(null, "Not in the ante/guess phase yet");
            return;
        }
        // https://www.baeldung.com/find-list-element-java#5-java-8-stream-api
        Player p = players.stream().filter(player -> player.getClientId() == clientId).findFirst().orElse(null);
        logger.info("setAnteAndGuess player: " + p + " "
                + String.format("Is current player: %s", clientId == currentPlayer));
        if (turnTimer != null) {
            turnTimer.cancel();
        }
        if (p != null && p.isReady() && !p.hasGuess() && clientId == currentPlayer && !p.isOut()
                && ante <= p.getMatter()) {
            logger.info("Valid ante/guess");
            p.setAnte(ante);// record round bet
            p.setGuess(guess);// record round guess
            p.modifyMatter(-ante);// deduct ante (will broadcast beginning of next round)
            sendMessage(null, "<i>" + p.getClientName() + " placed their ante and guess</i>");
        } else {
            logger.info("Invalid ante/guess");
        }
        checkAntes();
    }

    private void checkAntes() {
        logger.info("Checking Antes");
        int total = 0;
        int anted = 0;
        long pendingMatter = 0;
        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            while (iter.hasNext()) {
                Player player = iter.next();
                if (player != null && player.isReady()) {
                    if (!player.isOut()) {
                        total++;
                    }
                    if (player.hasGuess() && player.getAnte() > 0) {
                        anted++;
                        pendingMatter += player.getAnte();
                    }
                }
            }
        }
        logger.info(String.format("Ante Check ante/total %s/%s", anted, total));
        if (anted >= total) {
            actualMatter += pendingMatter;

            // TODO determine potential winners
            logger.info("Calculate Winners. Matter: " + actualMatter);
            checkWinners();
        } else {
            nextPlayer();
        }
    }

    private void checkWinners() {
        logger.info("Checking winners");
        List<Player> winners = new ArrayList<Player>();
        synchronized (players) {
            Iterator<Player> iter = players.iterator();
            while (iter.hasNext()) {
                Player player = iter.next();
                if (player != null && player.isReady() && player.hasGuess()) {
                    if (player.getGuess() == actualMatter) {
                        winners.add(player);
                    }
                }
            }
        }
        int count = winners.size();
        sendMessage(null, String.format("<b><u>There %s %s winner%s this round</u></b>", count == 1 ? "is" : "are", count,
                count == 1 ? "" : "s"));
        long reward = count == 1 ? actualMatter : (long) Math.ceil((double) actualMatter / (double) count);
        logger.info("End of Round Reward: " + reward);
        if (reward > 0 && count > 0) {
            synchronized (winners) {
                Iterator<Player> iter = winners.iterator();
                while (iter.hasNext()) {
                    Player player = iter.next();
                    logger.info(String.format("Checking player %s is ready %s and has guess %s", player.getClientName(),
                            player.isReady(), player.hasGuess()));
                    if (player != null && player.isReady()) {
                        player.modifyMatter(reward);
                        sendMessage(null, String.format("<font color=\"green\" %s received %s matter</font>", player.getClient().getFormattedName(), reward));
                        // broadcastMatter(player);//not needed since nextRound does the same
                    }
                }
            }
            resetReward();

        }
        List<Player> availablePlayers = getAvailablePlayers(Constants.DEFAULT_CLIENT_ID);
        if (availablePlayers.size() <= 1) {
            gameOver();
            return;// don't go to next round
        }
        nextRound();

    }

    private synchronized List<Player> getAvailablePlayers(long clientId) {
        synchronized (players) {
            List<Player> availablePlayers = players.stream().filter(player -> {
                return (player.isReady() && player.getMatter() > 0) || player.getClientId() == clientId;
            }).toList();
            return availablePlayers;
        }
    }

    private void gameOver() {
        if (currentPhase != Phase.REVEAL) {
            currentPhase = Phase.REVEAL;
        } else {
            return;
        }
        logger.info("Game Over, sorting players");
        // sort to get winner
        players.sort((a, b) -> {
            if (a.getMatter() == b.getMatter()) {
                return 0;
            } else if (a.getMatter() < b.getMatter()) {
                return 1;
            }
            return -1;
        });
        logger.info("Last Matter sync up");
        // last sync up of matter
        synchronized (players) {
            for (int i = players.size() - 1; i >= 0; i--) {
                Player player = players.get(i);
                broadcastMatter(player);
            }
        }
        Player winner = players.get(0);
        logger.info("Winner: " + winner);
        sendWinner(winner.getClientId());
        currentPlayer = Constants.DEFAULT_CLIENT_ID;
    }

    /**
     * On first invoke it'll pick the first player (from a shuffled list).
     * Subsequent calls will round-robin to the next player.
     * Calls sendTurn() to broadcast the current player's turn.
     */
    private void nextPlayer() {
        logger.info("Moving to next player");
        List<Player> availablePlayers = getAvailablePlayers(currentPlayer);
        Player p;
        if (currentPlayer == Constants.DEFAULT_CLIENT_ID) {
            p = availablePlayers.get(0);
            currentPlayer = p.getClientId();
        } else {
            // find the current player's index and move to the next person
            p = availablePlayers.stream().filter(player -> player.getClientId() == currentPlayer).findFirst()
                    .orElse(null);
            if(p == null){
                logger.info("Player likely disconnected, attempting to find available player");
                p = availablePlayers.stream().filter(player -> !player.hasGuess() && player.isReady() && !player.isOut()).findFirst()
                    .orElse(null);
                if(p == null && availablePlayers.size() > 1){
                    logger.info("Couldn't find any available players, checking antes");
                    checkAntes();
                    return;
                }
            }
            int index = availablePlayers.indexOf(p);
            if (index > -1) {
                index++;
                // loop the index over if we go out of bounds
                if (index >= availablePlayers.size()) {
                    index = 0;
                }
                p = availablePlayers.get(index);
                currentPlayer = p.getClientId();

            } else {
                gameOver();
                return;
            }
        }
        sendTurn(currentPlayer, estMaxRoundMatter);
        if (turnTimer != null) {
            turnTimer.cancel();
        }
        turnTimer = new Countdown("Turn expires", 30);
        turnTimer.setExpireCallback(() -> {

            sendMessage(null, "<b><font color=\"red\">Turned skipped, auto ante for player</font></b>");
            setAnteAndGuess(currentPlayer, 1, 1);
            // nextPlayer();
        });

    }

    /**
     * Broadcasts the current player's turn and the max guess range
     * 
     * @param currentPlayer
     * @param maxGuess
     */
    private void sendTurn(long currentPlayer, long maxGuess) {
        logger.info("Sending turn data");
        synchronized (players) {
            for (int i = players.size() - 1; i >= 0; i--) {
                Player player = players.get(i);
                boolean messageSent = player.getClient().sendTurn(currentPlayer, maxGuess);
                if (!messageSent) {
                    players.remove(i);
                    handleDisconnect(null, player.getClient());
                }
            }
        }
    }

    private void sendWinner(long winner) {
        logger.info("Sending winner data");
        synchronized (players) {
            for (int i = players.size() - 1; i >= 0; i--) {
                Player player = players.get(i);
                boolean messageSent = player.getClient().sendWinner(winner);
                if (!messageSent) {
                    players.remove(i);
                    handleDisconnect(null, player.getClient());
                }
            }
        }
    }

    public void restartSession() {
        if (currentPlayer == Constants.DEFAULT_CLIENT_ID && round > 0) {
            round = 0;
            synchronized (players) {
                for (int i = players.size() - 1; i >= 0; i--) {
                    Player player = players.get(i);
                    player.setIsReady(false);
                    boolean messageSent = player.getClient().sendRestartNotice();
                    if (!messageSent) {
                        players.remove(i);
                        handleDisconnect(null, player.getClient());
                    }
                }
            }
            if (turnTimer != null) {
                turnTimer.cancel();
            }
            turnTimer = new Countdown("Restart", 29);
            turnTimer.setExpireCallback(() -> {
                currentPhase = Phase.READY_CHECK;
                sendMessage(null, "Commence Ready Check.");
            });
        }
    }
    @Override
    protected void handleDisconnect(Iterator<ServerThread> iter, ServerThread client){
        if(client.getClientId() == currentPlayer){
            nextPlayer();
        }
        super.handleDisconnect(iter, client);
    }
    @Override
    public void close() {
        super.close();
        if (turnTimer != null) {
            turnTimer.cancel();
        }
    }
}

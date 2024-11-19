package Popularity.Server;

import java.util.List;

import Popularity.Common.Cell;
import Popularity.Common.Grid;
import Popularity.Common.LoggerUtil;
import Popularity.Common.OccupiedStatus;
import Popularity.Common.Phase;
import Popularity.Common.Player;
import Popularity.Common.TimedEvent;
import Popularity.Common.TimerType;

public class GameRoom extends BaseGameRoom {

    // used for general rounds (usually phase-based turns)
    private TimedEvent roundTimer = null;

    // used for granular turn handling (usually turn-order turns)
    private TimedEvent turnTimer = null;
    private int round = 0;
    private Grid grid = null;
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
        grid = new Grid(4, 4);
        round = 0; // init as 0; onRoundStart() will increment
        sendGridDimensions();
        LoggerUtil.INSTANCE.info("onSessionStart() end");
        onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onRoundStart() {
        LoggerUtil.INSTANCE.info("onRoundStart() start");
        resetRoundTimer();
        startRoundTimer();
        round++;
        sendGameEvent("Round: " + round);
        changePhase(Phase.IN_PROGRESS);
        grid.reset();
        // reset taken turn
        sendResetTurnStatus();
        LoggerUtil.INSTANCE.info("onRoundStart() end");
    }

    /** {@inheritDoc} */
    @Override
    protected void onTurnStart() {
        LoggerUtil.INSTANCE.info("onTurnStart() start");
        resetTurnTimer();
        startTurnTimer();
        LoggerUtil.INSTANCE.info("onTurnStart() end");
    }

    // Note: logic between Turn Start and Turn End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onTurnEnd() {
        LoggerUtil.INSTANCE.info("onTurnEnd() start");
        resetTurnTimer(); // reset timer if turn ended without the time expiring

        LoggerUtil.INSTANCE.info("onTurnEnd() end");
    }

    // Note: logic between Round Start and Round End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onRoundEnd() {
        LoggerUtil.INSTANCE.info("onRoundEnd() start");
        resetRoundTimer(); // reset timer if round ended without the time expiring
        // check points logic
        Cell mostPopular = grid.getMostPopularCell();
        if(mostPopular != null){
            LoggerUtil.INSTANCE.info(String.format("Found most popular cell: %s", mostPopular));
            for(long clientId : mostPopular.getPlayersInCell()){
                if(playersInRoom.containsKey(clientId)){
                    ServerPlayer sp = playersInRoom.get(clientId);
                    sp.changePoints(1);
                    sendPointsUpdate(sp); // remember, this is a nested loop so the entire operation of this can potentially be a bit expensive
                }
            }
        }
        else{
            LoggerUtil.INSTANCE.warning("No 'most popular' Cell for round " + round);
        }
        if (round >= 3) {
            changePhase(Phase.SCORING);
            new TimedEvent(10, ()->{
                onSessionEnd();
            });
        } else {
            changePhase(Phase.ROUND_DELAY);
            sendOccupiedStatus();
            TimedEvent t = new TimedEvent(5, ()->{
                onRoundStart();
            });
            t.setTickCallback((time)->{
                sendCurrentTime(TimerType.NEXT_ROUND, time);
            });
        }
        LoggerUtil.INSTANCE.info("onRoundEnd() end");

    }

    /** {@inheritDoc} */
    @Override
    protected void onSessionEnd() {
        LoggerUtil.INSTANCE.info("onSessionEnd() start");
        sendResetPoints();
        resetReadyStatus();

        changePhase(Phase.READY);
        LoggerUtil.INSTANCE.info("onSessionEnd() end");
    }
    // end lifecycle methods

    // send/sync data to ServerPlayer(s)
    private void sendResetPoints(){
        playersInRoom.values().removeIf(spInRoom -> {
            spInRoom.setPoints(0);
            boolean failedToSend = !spInRoom.sendPointsUpdate(ServerPlayer.DEFAULT_CLIENT_ID, 0);
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }
    private void sendOccupiedStatus(){
        List<OccupiedStatus> os = grid.getOccupiedStatus();
        
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendOccupiedStatus(os);
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }
   
    private void sendGridDimensions() {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendGridDimensions(grid.getRows(), grid.getCols());
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
            spInRoom.setCoordinate(-1, -1); // reset server data
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
     * @param str
     */
    private void sendGameEvent(String str) {
        sendGameEvent(str, null);
    }

    /**
     * Sends a game event to specific clients (by id)
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
    private void checkPlayerIsReady(ServerPlayer sp) throws Exception {
        if (!sp.isReady()) {
            sp.sendGameEvent("You weren't ready in time");
            throw new Exception("Player isn't ready");
        }
    }

    /**
     * Used to prevent a player from doing multiple actions on their turn
     * @param sp
     * @throws Exception
     */
    private void checkPlayerTookTurn(ServerPlayer sp) throws Exception {
        if (sp.didTakeTurn()) {
            sp.sendGameEvent("You already took your turn");
            throw new Exception("Player already took turn");
        }
    }
    // end custom checks

    // receive data from ServerThread (GameRoom specific)
    protected void handleTurn(ServerThread sender, int x, int y) {
        try {
            // early exit checks
            checkPlayerInRoom(sender);
            checkCurrentPhase(sender, Phase.IN_PROGRESS);

            ServerPlayer sp = playersInRoom.get(sender.getClientId());
            checkPlayerIsReady(sp);
            //checkPlayerTookTurn(sp); // commented out to allow a player to change their location choice
            // get previous coordinate
            int px = sp.getX(), py = sp.getY();
            if(px == x && py == y){
                sender.sendMessage("You're already at this coordinate");
                return;
            }
            if(grid.addPlayerToCellAtCoordinate(x, y, sender.getClientId(), px, py)){
                sp.setCoordinate(x, y);
            }
            else if(px > -1 && py > -1){
                sp.setCoordinate(px, py);
            }
            
            sender.sendTurnConfirm(x, y);
            sendTurnStatus(sp);
            
            if (didAllTakeTurn()) {
                onRoundEnd();
            }

        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handleReady exception", e);
        }
    }

    // end receive data from ServerThread (GameRoom specific)

    // misc logic
    private boolean didAllTakeTurn() {
        long ready = playersInRoom.values().stream().filter(p -> p.isReady()).count();
        long tookTurn = playersInRoom.values().stream().filter(p -> p.isReady() && p.didTakeTurn()).count();
        LoggerUtil.INSTANCE.info(String.format("didAllTakeTurn() %s/%s", tookTurn, ready));
        return ready == tookTurn;
    }
    // end misc logic
}

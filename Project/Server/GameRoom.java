package Project.Server;

import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.TimedEvent;

public class GameRoom extends BaseGameRoom {
    
    // used for general rounds (usually phase-based turns)
    private TimedEvent roundTimer = null;

    // used for granular turn handling (usually turn-order turns)
    private TimedEvent turnTimer = null;
    
    public GameRoom(String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientAdded(ServerPlayer sp){
        // sync GameRoom state to new client
        syncCurrentPhase(sp);
        syncReadyStatus(sp);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientRemoved(ServerPlayer sp){
        // added after Summer 2024 Demo
        // Stops the timers so room can clean up
        LoggerUtil.INSTANCE.info("Player Removed, remaining: " + playersInRoom.size());
        if(playersInRoom.isEmpty()){
            resetReadyTimer();
            resetTurnTimer();
            resetRoundTimer();
            onSessionEnd();
        }
    }

    // timer handlers
    private void startRoundTimer(){
        roundTimer = new TimedEvent(30, ()-> onRoundEnd());
        roundTimer.setTickCallback((time)->System.out.println("Round Time: " + time));
    }
    private void resetRoundTimer(){
        if(roundTimer != null){
            roundTimer.cancel();
            roundTimer = null;
        }
    }

    private void startTurnTimer(){
        turnTimer = new TimedEvent(30, ()-> onTurnEnd());
        turnTimer.setTickCallback((time)->System.out.println("Turn Time: " + time));
    }
    private void resetTurnTimer(){
        if(turnTimer != null){
            turnTimer.cancel();
            turnTimer = null;
        }
    }
    // end timer handlers
    
    // lifecycle methods

    /** {@inheritDoc} */
    @Override
    protected void onSessionStart(){
        LoggerUtil.INSTANCE.info("onSessionStart() start");
        changePhase(Phase.IN_PROGRESS);
        LoggerUtil.INSTANCE.info("onSessionStart() end");
        onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onRoundStart(){
        LoggerUtil.INSTANCE.info("onRoundStart() start");
        resetRoundTimer();
        startRoundTimer();
        LoggerUtil.INSTANCE.info("onRoundStart() end");
    }

    /** {@inheritDoc} */
    @Override
    protected void onTurnStart(){
        LoggerUtil.INSTANCE.info("onTurnStart() start");
        resetTurnTimer();
        startTurnTimer();
        LoggerUtil.INSTANCE.info("onTurnStart() end");
    }
    // Note: logic between Turn Start and Turn End is typically handled via timers and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onTurnEnd(){
        LoggerUtil.INSTANCE.info("onTurnEnd() start");
        resetTurnTimer(); // reset timer if turn ended without the time expiring

        LoggerUtil.INSTANCE.info("onTurnEnd() end");
    }
    // Note: logic between Round Start and Round End is typically handled via timers and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onRoundEnd(){
        LoggerUtil.INSTANCE.info("onRoundEnd() start");
        resetRoundTimer(); // reset timer if round ended without the time expiring

        LoggerUtil.INSTANCE.info("onRoundEnd() end");
        onSessionEnd();
    }

    /** {@inheritDoc} */
    @Override
    protected void onSessionEnd(){
        LoggerUtil.INSTANCE.info("onSessionEnd() start");
        resetReadyStatus();
        changePhase(Phase.READY);
        LoggerUtil.INSTANCE.info("onSessionEnd() end");
    }
    // end lifecycle methods

    

    // send/sync data to ServerPlayer(s)

    
    // end send data to ServerPlayer(s)

    // receive data from ServerThread (GameRoom specific)
    
    // end receive data from ServerThread (GameRoom specific)
}

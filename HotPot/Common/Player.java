package HotPot.Common;

/**
 * Common Player data shared between Client and Server
 */
public class Player {
    public static long DEFAULT_CLIENT_ID = -1L;
    private long clientId = Player.DEFAULT_CLIENT_ID;
    private boolean isReady = false;
    private boolean takeTurn = false;
    private int points = 0;
    public long getClientId() {
        return clientId;
    }
    
    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public boolean isReady() {
        return isReady;
    }
    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }
    public boolean didTakeTurn() {
        return takeTurn;
    }

    public void setTakeTurn(boolean tookTurn) {
        this.takeTurn = tookTurn;
    }
    public void setPoints(int p){
        this.points = p;
    }
    public void changePoints(int p){
        this.points += p;
        this.points = Math.max(this.points, 0); // minimum 0 points
    }
    public int getPoints(){
        return this.points;
    }
    /**
     * Resets all of the data (this is destructive).
     * You may want to make a softer reset for other data
     */
    public void reset(){
        this.clientId = Player.DEFAULT_CLIENT_ID;
        this.isReady = false;
        this.takeTurn = false;
        this.points = 0;
    }
}

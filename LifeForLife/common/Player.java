package LifeForLife.common;

import LifeForLife.server.ServerThread;

public class Player{
    private long life = 0;
    private boolean ready = false;
    private long clientId = Constants.DEFAULT_CLIENT_ID;
    private String clientName = "";
    private ServerThread serverThread;
    /** Server-side constructor */
    public Player(ServerThread st){
        this.serverThread = st;
        this.clientId = serverThread.getClientId();
        this.clientName = serverThread.getClientName();
    }
    public ServerThread getClient(){
        return serverThread;
    }
    /** client-side constructor */
    public Player(long clientId, String clientName) {
        this.clientId = clientId;
        this.clientName = clientName;
        
    }
    public long getClientId(){
        return clientId;
    }
    public void setClientName(String clientName){
        this.clientName = clientName;
    }
    public String getClientName(){
        return clientName;
    }
    public void setIsReady(boolean isReady){
        this.ready = isReady;
    }
    public boolean isReady(){
        return this.ready;
    }
    public void setLife(long life){
        this.life = life;
    }
    public long getLife(){
        return life;
    }
    public void modifyLife(long change){
        life += change;
    }
    
}

package AnteMatter.common;

import AnteMatter.server.ServerThread;

public class Player{
    private long matter = 0;
    private long ante = 0;
    private long guess = 0;
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
    public void resetGuess(){
        guess = 0;
    }
    public boolean hasGuess(){
        return guess > 0;
    }
    public void setGuess(long guess){
        this.guess = guess;
    }
    //Only called server side
    public long getGuess(){
        return guess;
    }
    public void setIsReady(boolean isReady){
        this.ready = isReady;
    }
    public boolean isReady(){
        return this.ready;
    }
    public void setMatter(long matter){
        this.matter = matter;
    }
    public long getMatter(){
        return matter;
    }
    public void modifyMatter(long change){
        matter += change;
    }
    public void setAnte(long ante){
        this.ante = ante;
    }
    public long getAnte(){
        return ante;
    }
}

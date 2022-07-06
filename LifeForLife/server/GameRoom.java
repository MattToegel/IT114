package LifeForLife.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import LifeForLife.common.Constants;
import LifeForLife.common.Player;

public class GameRoom extends Room {
    
    private static Logger logger = Logger.getLogger(GameRoom.class.getName());

    private List<Player> players = Collections.synchronizedList(new ArrayList<Player>());
    //Constants
    
    
    public GameRoom(String name) {
        super(name);
       
    }
    @Override
    protected synchronized void addClient(ServerThread client){
        super.addClient(client);
        Player player = new Player(client);
        players.add(player);
        syncReadyStatus(player);
    }
    @Override
    protected synchronized void removeClient(ServerThread client){
        super.removeClient(client);
        
        boolean removed = players.removeIf(p->p.getClientId()==client.getClientId()); //TODO see if this works w/o loop
        logger.log(Level.INFO, "GameRoom Removed Player: " + (removed?"true":"false"));

    }
    public synchronized void setReady(long clientId){
        synchronized(players){
            Iterator<Player> iter = players.iterator();
            while(iter.hasNext()){
                Player p = iter.next();
                if(p != null && !p.isReady() && p.getClientId() == clientId){
                    p.setIsReady(true);
                    logger.log(Level.INFO, p.getClientName() + " is ready");
                    break;
                }
            }
        }
        sendReadyStatus(clientId);
        readyCheck();
    }
    private synchronized void syncReadyStatus(Player incoming){
        synchronized(players){
            Iterator<Player> iter = players.iterator();
            while(iter.hasNext()){
                Player p = iter.next();
                if(p != null && p.isReady() && p.getClientId() != incoming.getClientId()){
                    boolean messageSent = incoming.getClient().sendReadyStatus(p.getClientId());
                    if (!messageSent) {
                        iter.remove();
                        logger.log(Level.INFO, "Removed client " + incoming.getClientName());
                        checkClients();
                        sendConnectionStatus(incoming.getClient(), false);
                    }
                }
            }
        }
    }
    private synchronized void sendReadyStatus(long clientId){
        if (players == null) {
            return;
        }
        synchronized (players) {
            for (int i = players.size() - 1; i >= 0; i--) {
                Player player = players.get(i);
                boolean messageSent = player.getClient().sendReadyStatus(clientId);
                if (!messageSent) {
                    players.remove(i);
                    logger.log(Level.INFO, "Removed client " + player.getClientName());
                    checkClients();
                    sendConnectionStatus(player.getClient(), false);
                }
            }
        }
    }
    private synchronized void readyCheck(){
        int numReady = 0;
        synchronized(players){
            Iterator<Player> iter = players.iterator();
            while(iter.hasNext()){
                Player p = iter.next();
                if(p != null && p.isReady()){
                    numReady++;
                }
            }
        }
        if(numReady >= Constants.MINIMUM_PLAYERS && numReady >= players.size()){
            setupGame();
        }
    }
    private void setupGame(){
        logger.log(Level.INFO, "Initializing Game");
       
        synchronized(players){
            Iterator<Player> iter = players.iterator();
            while(iter.hasNext()){
                Player p = iter.next();
                if(p != null && p.isReady()){
                    p.setLife(Constants.STARTING_LIFE);
                    broadcastLife(p);
                    //p.getClient().sendCurrentLife(Constants.STARTING_LIFE);
                }
            }
        }
        logger.log(Level.INFO, "Ready to play");
    }
    private synchronized void broadcastLife(Player playerChanged){
        synchronized(players){
            Iterator<Player> iter = players.iterator();
            while(iter.hasNext()){
                Player p = iter.next();
                if(p != null && p.isReady()){
                    boolean messageSent = p.getClient().sendCurrentLife(playerChanged.getClientId(), playerChanged.getLife());
                    if(!messageSent){
                        logger.log(Level.SEVERE, "Failed to send message to " + p.getClientName());
                    }
                }
            }
        }
    }
}

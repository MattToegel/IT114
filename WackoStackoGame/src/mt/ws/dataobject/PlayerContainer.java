package mt.ws.dataobject;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

public class PlayerContainer {
	Hashtable<Integer, Player> players = new Hashtable<Integer, Player>();
	public void addPlayer(int id, Player player) {
		if(!players.containsKey(id)) {
			System.out.println("Added " + id + " with name " + player.getName());
			players.put(id, player);
		}
		else {
			System.out.println(id + " with name " + player.getName() + " already connected");
		}
	}
	/**
	 * remove a player from the hashtable and returns it for further post-processing
	 * @param id
	 * @return
	 */
	public Player removePlayer(int id) {
		return players.remove(id);
	}
	public int getIdByIndex(int index) {
		int i = 0;
		for(int id : players.keySet()) {
			if(i == index) {
				return id;
			}
			i++;
		}
		return -1;
	}
	public Player getPlayerByIndex(int index) {
		int i = 0;
		for(Player p : players.values()) {
			if(i == index) {
				return p;
			}
			i++;
		}
		return null;
	}
	public int getTotalPlayers() {
		return players.size();
	}
	public Player getPlayer(int id) {
		return players.get(id);
	}
	public void movePlayers() {
		for ( Player v : players.values() ) {
		    v.move();
		}
	}
	/*public void paintPlayers(Graphics2D g2d) {
		for ( Player v : players.values() ) {
		    v.re(g2d);
		}
	}*/
	public void updatePlayers(int id, PayloadType type, int x, int y, String extra) {
		Player player = null;
		if(players.containsKey(id)) {
			player = players.get(id);
			switch(type) {
				case CHANGE_DIRECTION:
					//update player direction
					if(player != null) {
						player.setDirection(x, y);
					}
					break;
				case SYNC_POSITION:
					//update player position
					if(player != null) {
						player.setPosition(x, y);
					}
					break;
				case DID_JUMP:
					if(player != null) {
						//TODO should probably force it since
						//it's from server?
						player.tryJump();
					}
					break;
				default:
					break;
			}
		}
	}
	
	public static int calculateDistanceBetweenPoints(
			  Point a, 
			  Point b) {       
	    return (int)Math.sqrt((b.y - a.y) * (b.y - a.y) + (b.x - a.x) * (b.x - a.x));
	}
	//use from server side
	//TODO see GameEngine for collision handling for Physics
}



import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

public class PlayerContainer {
	Hashtable<Integer, Player> players = new Hashtable<Integer, Player>();
	public void AddPlayer(int id, Player player) {
		if(!players.containsKey(id)) {
			System.out.println("Added " + id + " with name " + player.getName());
			players.put(id, player);
		}
		else {
			System.out.println(id + " with name " + player.getName() + " already connected");
		}
	}
	public Player RemovePlayer(int id) {
		return players.remove(id);
	}
	public int getIdByIndex(int index) {
		int i = 0;
		for(int id : players.keySet()) {
			if(i == id) {
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
	public void MovePlayers() {
		for ( Player v : players.values() ) {
		    v.move();
		}
	}
	public void PaintPlayers(Graphics2D g2d) {
		for ( Player v : players.values() ) {
		    v.paint(g2d);
		}
	}
	public void UpdatePlayer(int id, PayloadType type, int x, int y, String extra) {
		Player player = null;
		if(players.containsKey(id)) {
			player = players.get(id);
			switch(type) {
				case DIRECTION:
					//update player direction
					if(player != null) {
						player.setDirection(x, y);
					}
					break;
				case MOVE_SYNC:
					//update player position
					if(player != null) {
						player.setPosition(x, y);
					}
					break;
				case SPEED_BOOST:
					//trigger speed boost visual for player
					if(player != null) {
						player.applyBoost();
					}
					break;
				case TRIGGER_TAG:
					//trigger tag visual for player
					if(player != null) {
						player.tryToTag();
					}
					break;
				case SET_IT:
					//TODO server side should check/set this
					System.out.println(player.getName() + "(" + id + "): Apply SET_IT payload");
					players.forEach((pid, tplayer)->{
						//if same id, set it, else not it
						boolean isIt = pid == id;
						System.out.println(pid + " - " + id + " is it " + isIt);
						tplayer.setIsIt(isIt);
					});
					break;
				case STATS:
					if(player != null) {
						player.syncStats(x, y);
					}
					break;
				default:
					break;
			}
		}
	}
	public synchronized List<Player> getLeaderboard() {
		List<Player> p = new ArrayList<Player>(players.values());
		p.sort(Comparator.comparing(Player::getNumberOfTags).reversed());
		
		return p;
	}
	public Entry<Integer,Player> getCurrentTagger(){
		synchronized(players) {
			for(Entry<Integer, Player> set : players.entrySet()) {
				if(set.getValue().isIt()) {
					return set;
				}
			}
			return null;
		}
	}
	public boolean isThereATagger() {
		return getCurrentTagger() != null;
	}
	public static int calculateDistanceBetweenPoints(
			  Point a, 
			  Point b) {       
	    return (int)Math.sqrt((b.y - a.y) * (b.y - a.y) + (b.x - a.x) * (b.x - a.x));
	}
	//use from server side
	public Entry<Integer,Player> CheckCollisions(Player p) {
		Entry<Integer,Player> tagged = null;
		synchronized(players) {
			for(Entry<Integer, Player> set : players.entrySet()) {
				//if(!p.getName().equals(set.getValue().getName())) {
				if(p.getID() != set.getKey()) {
					System.out.println("Tagger ID: " + p.getID());
					System.out.println("Checking ID: " + set.getKey());
					//get distance between centers
					int dist = calculateDistanceBetweenPoints(p.getPosition(), set.getValue().getPosition());
					//System.out.println("Dist: " + dist);
					//calculate expected distance based on radius
					int rad = (p.getRadius()+set.getValue().getRadius());
					//System.out.println("Rad: " + rad);
					//check if point is within range
					if(dist <= rad) {
						tagged = set;
						break;
					}
				}
			}
		}
		return tagged;
	}
}

import java.awt.Graphics2D;
import java.util.Hashtable;

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
	public synchronized void paintPlayers(Graphics2D g2d) {
		for ( Player v : players.values() ) {
		    v.paint(g2d);
		}
	}
	public boolean updatePlayers(int id, PayloadType type, int x, int y) {
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
				default:
					break;
			}
			return true;
		}
		return false;
	}
	
}
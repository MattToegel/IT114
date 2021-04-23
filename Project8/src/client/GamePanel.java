package client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.GameState;
import common.Marker;
import common.MarkerType;
import common.Player;
import common.Ship;
import common.ShipType;
import core.BaseGamePanel;
import javax.swing.ImageIcon;

public class GamePanel extends BaseGamePanel implements Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1121202275148798015L;
	List<Player> players;
	// player's placed ships and detected ships from hits
	List<Ship> ships = new ArrayList<Ship>();
	// local references of where choices were made by local player
	List<Marker> markers = new ArrayList<Marker>();
	Player myPlayer;
	String playerUsername;// caching it so we don't lose it when room is wiped
	Dimension gameAreaSize = new Dimension();
	private final static Logger log = Logger.getLogger(GamePanel.class.getName());
	private Cursor cursor = new Cursor(50, 50);
	private Blast blast;
	private GameState gameState = GameState.LOBBY;
	private int maxShips = 10;
	private int placedShips = 0;
	Image icon;

	public void setPlayerName(String name) {
		playerUsername = name;
		if (myPlayer != null) {
			myPlayer.setName(playerUsername);
		}
	}

	@Override
	public synchronized void onClientConnect(String clientName, String message) {
		// TODO Auto-generated method stub
		System.out.println("Connected on Game Panel: " + clientName);
		boolean exists = false;
		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			if (p != null && p.getName().equalsIgnoreCase(clientName)) {
				exists = true;
				break;
			}
		}
		if (!exists) {
			Player p = new Player();
			p.setName(clientName);
			players.add(p);
			// want .equals here instead of ==
			// https://www.geeksforgeeks.org/difference-equals-method-java/
			if (clientName.equals(playerUsername)) {
				System.out.println("Reset myPlayer");
				myPlayer = p;
			}
		}
	}

	@Override
	public void onClientDisconnect(String clientName, String message) {

		System.out.println("Disconnected on Game Panel: " + clientName);
		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			if (p != null && !p.getName().equals(playerUsername) && p.getName().equalsIgnoreCase(clientName)) {
				iter.remove();
				break;
			}
		}
	}

	@Override
	public void onMessageReceive(String clientName, String message) {
		System.out.println("Message on Game Panel");

	}

	@Override
	public void onChangeRoom() {
		// don't clear, since we're using iterators to loop, remove via iterator
		// players.clear();
		clearPlayers();
		clearShips();
		clearMarkers();
		myPlayer = null;
		System.out.println("Cleared players");
	}

	@Override
	public void awake() {
		players = new ArrayList<Player>();
	}

	@Override
	public void start() {
		try {
			//credit to: https://gamedev.stackexchange.com/a/163691
			icon = new ImageIcon(new URL("https://i.stack.imgur.com/XbETn.gif")).getImage();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void update() {
		// don't need these for battleship
		// applyControls();
		// localMovePlayers();
	}

	/**
	 * Gets the current state of input to apply movement to our player
	 */
	@Deprecated
	private void applyControls() {
		// not used in battleship
		if (myPlayer != null) {
			int x = 0, y = 0;
			if (KeyStates.W) {
				y = -1;
			}
			if (KeyStates.S) {
				y = 1;
			}
			if (!KeyStates.W && !KeyStates.S) {
				y = 0;
			}
			if (KeyStates.A) {
				x = -1;
			} else if (KeyStates.D) {
				x = 1;
			}
			if (!KeyStates.A && !KeyStates.D) {
				x = 0;
			}
			boolean changed = myPlayer.setDirection(x, y);
			if (changed) {
				// only send data if direction changed, otherwise we're creating unnecessary
				// network traffic
				System.out.println("Direction changed");
				SocketClient.INSTANCE.syncDirection(new Point(x, y));
			}
		}
	}

	/**
	 * This is just an estimate/hint until we receive a position sync from the
	 * server
	 */
	@Deprecated
	private void localMovePlayers() {
		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			if (p != null) {
				p.move();
			}
		}
	}

	@Override
	public void lateUpdate() {
		// stuff that should happen at a slightly different time than stuff in normal
		// update()

	}

	@Override
	public synchronized void draw(Graphics g) {
		setBackground(Color.BLACK);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if(icon != null) {
			g.drawImage(icon, 0, 0, this.getWidth(), this.getHeight(),this);
		}
		//drawPlayers(g);//I don't need to draw them
		drawText(g);
		drawShips(g);
		drawMarkers(g);
		if (cursor != null) {
			cursor.draw(g);
		}
		if(blast != null) {
			blast.draw(g);
		}
	}

	private void clearPlayers() {
		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			iter.remove();
		}
	}

	private void clearShips() {

		Iterator<Ship> iter = ships.iterator();
		while (iter.hasNext()) {
			Ship s = iter.next();
			iter.remove();
		}
	}

	private void clearMarkers() {
		Iterator<Marker> iter = markers.iterator();
		while (iter.hasNext()) {
			Marker m = iter.next();
			iter.remove();
		}
	}

	private synchronized void drawMarkers(Graphics g) {
		Iterator<Marker> iter = markers.iterator();
		while (iter.hasNext()) {
			Marker m = iter.next();
			if (m != null) {
				m.draw(g);
			}
		}
	}

	private synchronized void drawShips(Graphics g) {
		Iterator<Ship> iter = ships.iterator();
		while (iter.hasNext()) {
			Ship s = iter.next();
			if (s != null) {
				s.draw(g);
			}
		}
	}
	@Deprecated
	private synchronized void drawPlayers(Graphics g) {
		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			if (p != null) {
				p.draw(g);
			}
		}
	}

	private void drawText(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Monospaced", Font.PLAIN, 12));
		if (myPlayer != null) {
			g.drawString("Debug MyPlayer: " + myPlayer.toString(), 10, 20);
		}
	}

	private void handleMouseClick(MouseEvent e) {
		switch (gameState) {

		case LOBBY:
			break;
		case PLACEMENT:
			sendPlaceShip(e);
			break;
		case TURNS:
			if (myPlayer.getAttacks() > 0) {
				if (!myPlayer.isAttacking()) {

					// TODO send network request;
					sendAttack(e);
				}
			}
			break;
		case POST_GAME:
			break;
		default:
			break;
		}
	}

	/// attack network
	private void sendAttack(MouseEvent e) {
		if (myPlayer.getAttacks() > 0 && !myPlayer.isAttacking()) {
			myPlayer.isAttacking(true);
			SocketClient.INSTANCE.sendAttack(e.getPoint());
		}
	}

	@Override
	public void onAttackStatus(int markerType, int x, int y) {
		// TODO cursor effect
		// TODO place parker
		Marker m = new Marker(MarkerType.values()[markerType]);
		m.setPosition(new Point(x, y));
		m.setSize(10, 10);
		markers.add(m);
		myPlayer.isAttacking(false);
		myPlayer.setAttacks(myPlayer.getAttacks()-1);
	}
	@Override
	public void onShipStatus(int shipId, int life) {
		Iterator<Ship> iter = ships.iterator();
		while(iter.hasNext()) {
			Ship s = iter.next();
			if(s.getId() == shipId) {
				s.setHealth(life);
				break;
			}
		}
	}
	@Override
	public void onAttackRadius(int x, int y, int radius) {
		if(blast == null) {
			blast = new Blast(x, y, radius);
		}
		else {
			blast.set(x, y, radius);
		}
	}
	///
	/// ship network
	private void sendPlaceShip(MouseEvent e) {
		if (placedShips < maxShips) {
			SocketClient.INSTANCE.sendShipPlacement(e.getPoint());
			// we'll assume our request will be accepted
			placedShips++;
		}
	}

	@Override
	public void onShipPlaced(int shipType, int shipId, int x, int y, int life) {// From Event
		ShipType t = ShipType.values()[shipType];
		Ship s = new Ship();
		s.setName(t.toString());
		s.setPosition(new Point(x, y));
		s.setId(shipId);
		// TODO set life/health
		ships.add(s);
		//Server will tell us from now on
		/*if (ships.size() >= maxShips) {
			gameState = GameState.TURNS;
		}*/
	}
	/// end ship network

	@Override
	public void quit() {
		log.log(Level.INFO, "GamePanel quit");
	}

	@Override
	public void attachListeners() {
		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				cursor.click(e.getPoint().x, e.getPoint().y);
				System.out.println("Clicked: " + e.getPoint());
				handleMouseClick(e);

				/*
				 * Code here just for sake of example Ship s = new Ship(); s.setName("Gunner");
				 * s.setPosition(e.getPoint()); ships.add(s);
				 */

			}
		});
	}

	@Override
	public void onSyncDirection(String clientName, Point direction) {
		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			if (p != null && p.getName().equalsIgnoreCase(clientName)) {
				System.out.println("Syncing direction: " + clientName);
				p.setDirection(direction.x, direction.y);
				System.out.println("From: " + direction);
				System.out.println("To: " + p.getDirection());
				break;
			}
		}
	}

	@Override
	public void onSyncPosition(String clientName, Point position) {
		System.out.println("Got position for " + clientName);
		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			if (p != null && p.getName().equalsIgnoreCase(clientName)) {
				System.out.println(clientName + " set " + position);
				p.setPosition(position);
				break;
			}
		}
	}

	@Override
	public void onGetRoom(String roomName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResize(Point p) {
		// TODO Auto-generated method stub
		gameAreaSize = new Dimension(p.x, p.y);
		this.setPreferredSize(gameAreaSize);
		this.setMinimumSize(gameAreaSize);
		this.setMaximumSize(gameAreaSize);
		this.setSize(gameAreaSize);
		System.out.println(this.getSize());
		this.invalidate();
		this.repaint();
	}

	@Override
	public void onCanAttack(String client, int attacks) {
		// TODO Auto-generated method stub
		Iterator<Player> iter = players.iterator();
		while(iter.hasNext()) {
			Player p = iter.next();
			if(p != null && p.getName().equals(client)) {
				p.setAttacks(attacks);
			}
		}
	}

	@Override
	public void onShipCount(String client, int ships) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPhaseChange(GameState phase) {
		// TODO Auto-generated method stub
		gameState = phase;
	}
}
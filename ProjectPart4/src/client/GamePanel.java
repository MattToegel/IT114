package client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import core.BaseGamePanel;
import core.Helpers;

public class GamePanel extends BaseGamePanel implements Event {

    /**
     * 
     */
    private static final long serialVersionUID = -1121202275148798015L;
    List<Player> players;
    Player myPlayer;
    String playerUsername;// caching it so we don't lose it when room is wiped
    List<Chair> chairs;
    List<Ticket> tickets;
    private final static Logger log = Logger.getLogger(GamePanel.class.getName());
    Dimension gameAreaSize = new Dimension();

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

		// brainstorming scenario
		int players = 15;
		final int chairs = Helpers.getNumberBetween(players, (int) (players * 1.5));
		final Dimension chairSize = new Dimension(25, 25);
		float paddingLeft = .1f;
		float paddingRight = .9f;
		float paddingTop = .1f;
		float paddingBottom = .6f;
		final float chairSpacing = chairSize.height * 1.75f;
		final int chairHalfWidth = (int) (chairSize.width * .5);
		final int screenWidth = getSize().width;
		final int screenHeight = getSize().height;
		for (int i = 0; i < chairs; i++) {
		    Chair chair = new Chair("Chair " + (i + 1));
		    Point chairPosition = new Point();
		    if (i % 2 == 0) {
			chairPosition.x = (int) ((screenWidth * paddingRight) - chairHalfWidth);
		    }
		    else {
			chairPosition.x = (int) (screenWidth * paddingLeft);
		    }
		    chairPosition.y = (int) ((getSize().height * paddingTop) + (chairSpacing * (i / 2)));
		    chair.setPosition(chairPosition);
		    chair.setSize(chairSize.width, chairSize.height);
		    this.chairs.add(chair);
		}
		int tickets = 15;
		paddingLeft = .4f;
		paddingRight = .6f;
		paddingTop = .4f;
		Dimension ticketSize = new Dimension(30, 20);
		for (int i = 0; i < tickets; i++) {
		    long seed = 200 * (i + 1);
		    Ticket ticket = new Ticket("#" + Helpers.getNumberBetweenBySeed(1, 10, seed));
		    Point ticketPosition = new Point();
		    ticketPosition.x = Helpers.getNumberBetweenBySeed((int) (screenWidth * paddingLeft),
			    (int) (screenWidth * paddingRight), seed);
		    ticketPosition.y = Helpers.getNumberBetweenBySeed((int) (screenHeight * paddingTop),
			    (int) (screenHeight * paddingBottom), seed);
		    ticket.setPosition(ticketPosition);
		    ticket.setSize(ticketSize.width, ticketSize.height);
		    System.out.println(ticket.getPosition());
		    this.tickets.add(ticket);
		}
	    }
	}
    }

    @Override
    public void onClientDisconnect(String clientName, String message) {

	// TODO Auto-generated method stub
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
	// TODO Auto-generated method stub
	System.out.println("Message on Game Panel");

    }

    @Override
    public void onChangeRoom() {
	// don't clear, since we're using iterators to loop, remove via iterator
	// players.clear();
	Iterator<Player> iter = players.iterator();
	while (iter.hasNext()) {
	    Player p = iter.next();
	    // if (p != myPlayer) {
	    iter.remove();
	    // }
	}
	myPlayer = null;
	System.out.println("Cleared players");
    }

    @Override
    public void awake() {
	players = new ArrayList<Player>();
	chairs = new ArrayList<Chair>();
	tickets = new ArrayList<Ticket>();
	GamePanel gp = this;
	// fix the loss of focus when typing in chat
	addMouseListener(new MouseAdapter() {

	    @Override
	    public void mousePressed(MouseEvent e) {
		gp.getRootPane().grabFocus();
	    }
	});
    }

    @Override
    public void start() {
	// TODO goes on server side, here for testing

    }

    @Override
    public void update() {
	applyControls();
	localMovePlayers();
    }

    /**
     * Gets the current state of input to apply movement to our player
     */
    private void applyControls() {
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
	    }
	    else if (KeyStates.D) {
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
	drawChairs(g);
	drawTickets(g);
	drawPlayers(g);
	drawText(g);
	drawUI((Graphics2D) g);
    }

    private synchronized void drawChairs(Graphics g) {
	Iterator<Chair> iter = chairs.iterator();
	while (iter.hasNext()) {
	    Chair c = iter.next();
	    if (c != null) {
		c.draw(g);
	    }
	}
    }

    private synchronized void drawTickets(Graphics g) {
	Iterator<Ticket> iter = tickets.iterator();
	while (iter.hasNext()) {
	    Ticket t = iter.next();
	    if (t != null) {
		t.draw(g);
	    }
	}
    }

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

    private void drawUI(Graphics2D g2) {
	Stroke oldStroke = g2.getStroke();
	g2.setStroke(new BasicStroke(2));
	g2.drawRect(0, 0, gameAreaSize.width, gameAreaSize.height);
	g2.setStroke(oldStroke);
    }

    @Override
    public void quit() {
	log.log(Level.INFO, "GamePanel quit");
	this.removeAll();
    }

    @Override
    public void attachListeners() {
	InputMap im = this.getRootPane().getInputMap();
	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "up_pressed");
	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "up_released");
	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "down_pressed");
	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "down_released");
	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "left_pressed");
	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "left_released");
	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "right_pressed");
	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "right_released");
	ActionMap am = this.getRootPane().getActionMap();

	am.put("up_pressed", new MoveAction(KeyEvent.VK_W, true));
	am.put("up_released", new MoveAction(KeyEvent.VK_W, false));

	am.put("down_pressed", new MoveAction(KeyEvent.VK_S, true));
	am.put("down_released", new MoveAction(KeyEvent.VK_S, false));

	am.put("left_pressed", new MoveAction(KeyEvent.VK_A, true));
	am.put("left_released", new MoveAction(KeyEvent.VK_A, false));

	am.put("right_pressed", new MoveAction(KeyEvent.VK_D, true));
	am.put("right_released", new MoveAction(KeyEvent.VK_D, false));
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
}
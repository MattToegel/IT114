package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import core.BaseGamePanel;

public class GamePanel extends BaseGamePanel implements Event {

    List<Player> players;
    Player myPlayer;

    @Override
    public void onClientConnect(String clientName, String message) {
	// TODO Auto-generated method stub
	System.out.println("Connected on Game Panel");
    }

    @Override
    public void onClientDisconnect(String clientName, String message) {
	// TODO Auto-generated method stub
	System.out.println("Disconnected on Game Panel");
    }

    @Override
    public void onMessageReceive(String clientName, String message) {
	// TODO Auto-generated method stub
	System.out.println("Message on Game Panel");

    }

    @Override
    public void onChangeRoom() {

    }

    @Override
    public void awake() {
	players = new ArrayList<Player>();
	myPlayer = new Player();
	players.add(myPlayer);
	// test
	// myPlayer.setActive(false);
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {
	Point dir = myPlayer.getDirection();
	if (KeyStates.W) {
	    dir.y -= 1;
	}
	else if (KeyStates.S) {
	    dir.y += 1;
	}
	else {
	    dir.y = 0;
	}
	if (KeyStates.A) {
	    dir.x -= 1;
	}
	else if (KeyStates.D) {
	    dir.x += 1;
	}
	else {
	    dir.x = 0;
	}
	boolean changed = myPlayer.setDirection(dir.x, dir.y);
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
	// TODO Auto-generated method stub

    }

    @Override
    public void draw(Graphics g) {
	setBackground(Color.BLACK);
	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	Iterator<Player> iter = players.iterator();
	while (iter.hasNext()) {
	    Player p = iter.next();
	    if (p != null) {
		p.draw(g);
	    }
	}
	// Printing texts
	g.setColor(Color.WHITE);
	g.setFont(new Font("Monospaced", Font.PLAIN, 12));
	g.drawString("Debug MyPlayer: " + myPlayer.toString(), 10, 20);
    }

    @Override
    public void quit() {
	// TODO Auto-generated method stub

    }

    @Override
    public void attachListeners() {
	this.getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "up_pressed");
	this.getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "up_released");
	this.getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "down_pressed");
	this.getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "down_released");
	this.getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "left_pressed");
	this.getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "left_released");
	this.getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "right_pressed");
	this.getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "right_released");
	this.getRootPane().getActionMap().put("up_pressed", new MoveAction(KeyEvent.VK_W, true));
	this.getRootPane().getActionMap().put("up_released", new MoveAction(KeyEvent.VK_W, false));

	this.getRootPane().getActionMap().put("down_pressed", new MoveAction(KeyEvent.VK_S, true));
	this.getRootPane().getActionMap().put("down_released", new MoveAction(KeyEvent.VK_S, false));

	this.getRootPane().getActionMap().put("left_pressed", new MoveAction(KeyEvent.VK_A, true));
	this.getRootPane().getActionMap().put("left_released", new MoveAction(KeyEvent.VK_A, false));

	this.getRootPane().getActionMap().put("right_pressed", new MoveAction(KeyEvent.VK_D, true));
	this.getRootPane().getActionMap().put("right_released", new MoveAction(KeyEvent.VK_D, false));
    }
}

class MoveAction extends AbstractAction {
    private static final long serialVersionUID = 5137817329873449021L;
    int key;
    boolean pressed = false;

    MoveAction(int k, boolean pressed) {
	key = k;
	this.pressed = pressed;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	switch (key) {
	case KeyEvent.VK_W:
	    KeyStates.W = pressed;
	    break;
	case KeyEvent.VK_S:
	    KeyStates.S = pressed;
	    break;
	case KeyEvent.VK_A:
	    KeyStates.A = pressed;
	    break;
	case KeyEvent.VK_D:
	    KeyStates.D = pressed;
	    break;
	}
    }

}

class KeyStates {
    public static boolean W = false;
    public static boolean S = false;
    public static boolean A = false;
    public static boolean D = false;
}
package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class GamePanel extends JPanel implements Event {

    Point test = new Point(0, 0);
    int speed = 5;

    @Override
    public void paintComponent(Graphics g) {
	super.paintComponent(g); // paint parent's background
	setBackground(Color.BLACK); // set background color for this JPanel
	// https://www3.ntu.edu.sg/home/ehchua/programming/java/J4b_CustomGraphics.html
	// https://books.trinket.io/thinkjava/appendix-b.html
	// http://www.edu4java.com/en/game/game2.html
	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	// Your custom painting codes. For example,
	// Drawing primitive shapes

	g.setColor(Color.YELLOW); // set the drawing color
	g.drawLine(30, 40, 100, 200);
	g.drawOval(150, 180, 10, 10);
	g.drawRect(200, 210, 20, 30);
	g.setColor(Color.RED); // change the drawing color
	g.fillOval(300, 310, 30, 50);
	g.fillRect(test.x, test.y, 60, 50);
	// Printing texts
	g.setColor(Color.WHITE);
	g.setFont(new Font("Monospaced", Font.PLAIN, 12));
	g.drawString("Testing custom drawing ...", 10, 20);

    }

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
	GamePanel self = this;
	Thread t = new Thread() {
	    @Override
	    public void run() {
		while (self.getParent().getParent().isEnabled()) {
		    // movement
		    if (KeyStates.W) {
			test.y -= speed;
		    }
		    else if (KeyStates.S) {
			test.y += speed;
		    }
		    if (KeyStates.A) {
			test.x -= speed;
		    }
		    else if (KeyStates.D) {
			test.x += speed;
		    }

		    self.repaint();
		    try {
			this.sleep(16);
		    }
		    catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    }
	};
	t.start();
    }

    @Override
    public void onClientConnect(String clientName, String message) {
	// TODO Auto-generated method stub

    }

    @Override
    public void onClientDisconnect(String clientName, String message) {
	// TODO Auto-generated method stub

    }

    @Override
    public void onMessageReceive(String clientName, String message) {
	// TODO Auto-generated method stub

    }

    @Override
    public void onChangeRoom() {
	// TODO Auto-generated method stub

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
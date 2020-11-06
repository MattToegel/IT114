package core;

import java.awt.Graphics;

import javax.swing.JPanel;

public abstract class BaseGamePanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static boolean isRunning = false;
    public static int SLEEP = 16;
    BaseGamePanel bgp;

    public BaseGamePanel() {
	awake();
	isRunning = true;
	bgp = this;
	Thread gameLoop = new Thread() {
	    @Override
	    public void run() {
		bgp.start();
		bgp.attachListeners();
		while (isRunning) {
		    bgp.update();
		    bgp.lateUpdate();
		    bgp.repaint();

		    // give it some rest
		    try {
			Thread.sleep(SLEEP);
		    }
		    catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}
	    }
	};
	gameLoop.start();
    }

    public abstract void awake();

    public abstract void start();

    public abstract void update();

    public abstract void lateUpdate();

    public abstract void draw(Graphics g);

    public abstract void quit();

    @Override
    public void paintComponent(Graphics g) {
	super.paintComponent(g); // paint parent's background
	draw(g);
    }

    public abstract void attachListeners();

}
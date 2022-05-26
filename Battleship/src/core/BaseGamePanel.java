package core;

import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

public abstract class BaseGamePanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 5L;
    // flag used to terminate game loop
    protected boolean isRunning = false;
    // thread sleep time in ms (16 ms is approx 60 frames per second)
    public static int SLEEP = 16;
    BaseGamePanel bgp;
    // when true, disables certain triggers the server version doesn't need
    public boolean isServer = false;
    // by setting this we can have the instance awake() but not start until
    // startGameLoop() is called
    public boolean delayGameLoop = false;// removed static ref, was "breaking" new game rooms since prelobby set it to
					 // true
    private final static Logger log = Logger.getLogger(BaseGamePanel.class.getName());
    Thread gameLoop;

    // constructor triggers the various events
    public BaseGamePanel() {
	awake();
	bgp = this;
	if (!delayGameLoop) {
	    startGameLoop();
	}
    }

    public BaseGamePanel(boolean delay) {
	delayGameLoop = delay;
	awake();
	bgp = this;
	if (!delayGameLoop) {
	    startGameLoop();
	}
    }

    // made it public in case there was a decision to use delayGameLoop = true
    public void startGameLoop() {
	if (gameLoop == null) {
	    isRunning = true;
	    gameLoop = new Thread() {
		@Override
		public void run() {
		    bgp.start();
		    if (!isServer) {
			bgp.attachListeners();
		    }
		    while (isRunning) {
			bgp.update();
			bgp.lateUpdate();
			nextFrame();

			if (!isServer) {
			    bgp.repaint();
			}

			// give it some rest
			try {
			    Thread.sleep(SLEEP);
			}
			catch (InterruptedException e) {
			    e.printStackTrace();
			}
		    }
		    log.log(Level.INFO, "game loop terminated");
		    bgp.quit();
		}
	    };
	    gameLoop.start();
	}
    }

    // called when new instance is created
    public abstract void awake();

    // called when thread is started
    public abstract void start();

    // called every frame
    public abstract void update();

    // called every frame after update has been called
    public abstract void lateUpdate();

    // called every frame if !isServer
    public abstract void draw(Graphics g);

    // called when loop exits
    public abstract void quit();

    // triggers the draw method
    @Override
    public void paintComponent(Graphics g) {
	super.paintComponent(g); // paint parent's background
	draw(g);
    }

    // forces subclasses to determine listeners
    public abstract void attachListeners();

    // don't call this more than once per frame
    protected long frame = 0;

    protected void nextFrame() {
	// we'll do basic frame tracking so we can trigger events
	// less frequently than each frame
	// update frame counter and prevent overflow
	if (Long.MAX_VALUE - 5 <= frame) {
	    frame = Long.MIN_VALUE;
	}
	frame++;
    }

}
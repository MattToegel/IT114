package mt.ws.client;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import mt.ws.client.PlayerControls.CustomKeyListener;
import mt.ws.dataobject.GameObject;
public class GameClient extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6748325367132904432L;
	
	//Player player = new Player();
	
	UIUtils ui = new UIUtils();
	
	static HashMap<String, Component> components = new HashMap<String, Component>();
	static GameState gameState = GameState.LOBBY;
	GameEngine ge;
	static Dimension gameArea;
	/** The canvas to draw to */
	protected JPanel canvas;
	public static Dimension getGameArea() {
		return gameArea;
	}
	public GameClient() {
		super("Wacko Stacko");
		// setup the JFrame
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// add a window listener
		this.addWindowListener(new WindowAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				// before we stop the JVM stop the example
				GameEngine.isRunning = false;
				super.windowClosing(e);
			}
		});
		// create the size of the window
		Dimension size = new Dimension(800, 600);
		// create a canvas to paint to 
		GameClient self = this;
		this.canvas = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;
				self._physicsDraw(g2d);
			}
		};
		this.canvas.setPreferredSize(size);
		this.canvas.setMinimumSize(size);
		this.canvas.setMaximumSize(size);
		
		// add the canvas to the JFrame
		this.add(this.canvas, BorderLayout.CENTER);
		// make the JFrame not resizable
		// (this way I dont have to worry about resize events)
		this.setResizable(false);
		
		// size everything
		this.pack();
		// make sure we are not stopped
		GameEngine.isRunning = true;
		//we'll init GameEngine.world only when we're
		//in game view
	}
	public void toggleRunningState(boolean s) {
		GameEngine.isRunning = s;
	}
	/***
	 * Looks up a component by name and changes visibility based on the toggle boolean value
	 * @param name
	 * Name of component key
	 * @param toggle
	 * Boolean for isVisible
	 */
	void toggleComponent(String name, boolean toggle) {
		if(components.containsKey(name)) {
			components.get(name).setVisible(toggle);
			//((JPanel)components.get(name)).grabFocus();
		}
	}
	/***
	 * Based on Client State we'll hide/show groups of components
	 * @param frame
	 */
	void ChangePanels() {
		switch(GameClient.gameState) {
			case GAME:
				toggleComponent("lobby", false);
				toggleComponent("stuff", true);
				//toggleComponent("game", true);
				this.canvas.setVisible(true);
				//toggleComponent("score", true);
				//toggleComponent("scores", true);
				break;
			case LOBBY:
				toggleComponent("stuff", false);
				toggleComponent("lobby", true);
				this.canvas.setVisible(false);
				
				//toggleComponent("game", false);
				//toggleComponent("score", false);
				//toggleComponent("scores", false);
				break;
			default:
				break;
		}
		this.pack();
        this.revalidate();
        this.repaint();
	}
	/***
	 * Safely gets a UI element from components hashmap
	 * @param name
	 * @return null if key not exists
	 */
	public Component GetUIElement(String name) {
		if(components.containsKey(name)) {
			return components.get(name);
		}
		return null;
	}
	void startGameUI() {
		GameEngine.last = System.nanoTime();
		// don't allow AWT to paint the canvas since we are
		this.setVisible(true);
		this.canvas.setVisible(true);
		this.canvas.setIgnoreRepaint(true);
		// enable double buffering (the JFrame has to be
		// visible before this can be done)
		//this.canvas.createBufferStrategy(3);
	}
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException ex) {
		} catch (InstantiationException ex) {
		} catch (IllegalAccessException ex) {
		} catch (UnsupportedLookAndFeelException ex) {
		}
		GameClient window = new GameClient();
		InitLobby(window);
		
		//InitGameCanvas();
		
		//GameClient gc = (GameClient)components.get("game");
		//gc.ChangePanels();
		window.pack();
		window.setVisible(true);
		
	}
	
	public static void InitLobby(GameClient window) {
		JPanel lobby = new JPanel();
		components.put("lobby", lobby);
		lobby.setName("lobby");
		lobby.setBorder(BorderFactory.createLineBorder(Color.black));
		JPanel container = new JPanel();
		JTextField name = new JTextField(20);
		name.setText("Guest");
		JTextField host = new JTextField(15);
		host.setText("127.0.0.1");
		JTextField port = new JTextField(4);
		port.setText("3111");
		JButton connectButton = new JButton();
		connectButton.setText("Connect");
		connectButton.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        //your actions
		    	//host.getText().trim(), 
				//port.getText().trim()
		    	if(window != null) {
		    		window.StartGameLoop(
		    				host.getText().trim(),
		    				Integer.parseInt(port.getText().trim()),
		    				name.getText().trim());
		    	}
		    }
		});
		JTextField message = new JTextField(60);
		message.setEditable(false);
		container.add(name);
		container.add(host);
		container.add(port);
		container.add(connectButton);
		lobby.setLayout(new BoxLayout(lobby, BoxLayout.PAGE_AXIS));
		lobby.add(container);
		lobby.add(message);
		components.put("lobby.message", message);
		
		JTextField stuff = new JTextField(10);
		stuff.setEditable(false);
		stuff.setText("Test");
		window.add(stuff, BorderLayout.LINE_START);
		components.put("stuff", stuff);
		//myFrame.add(scores, BorderLayout.NORTH);
		window.add(lobby, BorderLayout.NORTH);
		
		
	}
	void StartGameLoop(String host, int port, String playername) {
		GameClient.gameState = GameState.GAME;
    	ChangePanels();
        toggleRunningState(true);
		//gc.run();
        //setCanvas();
        startGameUI();
        ge = new GameEngine();
        ge.SetUI(this);
        ge.connect(host,port, playername);
        
        ge.start();
        //TODO fix input
        //KeyListener l = new CustomKeyListener();
        //this.addKeyListener(l);
		//this.canvas.addKeyListener(l);
        PlayerControls.setKeyBindings(this.canvas.getInputMap(), this.canvas.getActionMap());
    	this.canvas.grabFocus();
        run();
	}
	
	/*public void UpdatePlayerName(String str) {
		for(int i = 0; i < GameEngine.players.size(); i++) {
			GameEngine.players.get(i).setName(str);
		}
	}*/
	
	/**
	 * Renders the example.
	 * @param g the graphics object to render to
	 */
	protected void render(Graphics2D g) {
		if(ge == null || ge.world == null) {
			return;
		}
		// lets draw over everything with a white background
		g.setColor(Color.WHITE);
		g.fillRect(-400, -300, 800, 600);
		//g.fillRect(0, 0, gameArea.width, gameArea.height);
		// lets move the view up some
		g.translate(0.0, 1.0 * GameEngine.SCALE);
		
		// draw all the objects in the world
		for (int i = 0; i < ge.world.getBodyCount(); i++) {
			// get the object
			GameObject go = (GameObject) ge.world.getBody(i);
			// draw the object
			go.render(g);
		}
		
	}
	void _physicsDraw(Graphics2D g) {
		//Graphics2D g = (Graphics2D)this.canvas.getBufferStrategy().getDrawGraphics();
		
		AffineTransform yFlip = AffineTransform.getScaleInstance(1, -1);
		AffineTransform move = AffineTransform.getTranslateInstance(400, -300);
		g.transform(yFlip);
		g.transform(move);
		this.render(g);
		g.dispose();
		
		//let's see if we need this
		/*BufferStrategy strategy = this.canvas.getBufferStrategy();
		if (!strategy.contentsLost()) {
			strategy.show();
		}*/
		Toolkit.getDefaultToolkit().sync();
		
	}
	public void draw() {
		this.canvas.repaint();
		//_physicsDraw();
	}
	//running logic (each frame what do we do?)
	public void run() {
		/*while(GameEngine.isRunning) {
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
	}
}
enum GameState{
	LOBBY,
	GAME
}
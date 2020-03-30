import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

public class GameClient extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6748325367132904432L;
	SocketClient client;
	public static boolean isRunning = true;
	//Player player = new Player();
	List<Player> players = new ArrayList<Player>();
	UI ui = new UI();
	
	static HashMap<String, Component> components = new HashMap<String, Component>();
	static GameState gameState = GameState.LOBBY;
	public static JFrame myFrame;
	public GameClient() {
		
		
	}
	public void toggleRunningState(boolean s) {
		isRunning = s;
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
				toggleComponent("game", true);
				//toggleComponent("score", true);
				//toggleComponent("scores", true);
				break;
			case LOBBY:
				toggleComponent("lobby", true);
				toggleComponent("game", false);
				//toggleComponent("score", false);
				//toggleComponent("scores", false);
				break;
			default:
				break;
		}
		myFrame.pack();
        myFrame.revalidate();
        myFrame.repaint();
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
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException ex) {
		} catch (InstantiationException ex) {
		} catch (IllegalAccessException ex) {
		} catch (UnsupportedLookAndFeelException ex) {
		}
		JFrame frame = new JFrame("WackoStacko");
		
		//Terminates program when we click the x (close) button)
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setSize(new Dimension(600,600));
		GameClient.myFrame = frame;
		InitLobby();
		
		InitGameCanvas();
		
		GameClient gc = (GameClient)components.get("game");
		gc.ChangePanels();
		GameClient.myFrame.pack();
		GameClient.myFrame.setVisible(true);
		
	}
	public static GameClient InitGameCanvas() {
		//Game area
		JPanel playArea = new GameClient();
		//set the local reference of myFrame for easier use
		components.put("game", playArea);
		playArea.setPreferredSize(new Dimension(600,600));
		//playArea.getBounds()
		
		myFrame.add(playArea, BorderLayout.CENTER);
		return (GameClient)playArea;
	}
	public static void InitLobby() {
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
		    	GameClient gc = (GameClient)components.get("game");
		    	if(gc != null) {
		    		
		    		gc.client = SocketClient.connect(host.getText().trim(),
		    				Integer.parseInt(port.getText().trim()));
		    		if(gc.client != null) {
		    			gc.client.SetGameClient(gc);
		    			gc.client.setClientName(name.getText().trim());
		    		}
		    		gc.StartGameLoop();
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
		//myFrame.add(scores, BorderLayout.NORTH);
		myFrame.add(lobby, BorderLayout.NORTH);
		
	}
	public void UpdatePlayerName(String str) {
		for(int i = 0; i < players.size(); i++) {
			players.get(i).name = str;
		}
	}
	void StartGameLoop() {
		GameClient.gameState = GameState.GAME;
		GameClient self = this;
    	ChangePanels();
        toggleRunningState(true);
		//gc.run();
        Thread gameLoop = new Thread() {
        	@Override
        	public void run() {
        		//Init players
        		for(int i = 0; i < 5; i++) {
        			Player p = new Player();
        			self.players.add(p);
        			System.out.println("Added player " + i);
        		}
        		self.run();
        	}
        };
        gameLoop.start();
    	
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		
		for(int i = 0; i < players.size(); i++) {
			players.get(i).draw(g2d);
		}
		//player.draw(g2d);
		//System.out.println(getFPS(FPSOldTime));
		//Sample of enabling/disabling FPS and showing on screen
		ui.draw(g2d);
	}
	//running logic (each frame what do we do?)
	public void run() {
		
		Random random = new Random();
		//give a random direction
		//based on 0 or 1
		for(int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			player.direction.x = random.nextInt(2) == 0?-1:1;
			player.direction.y = random.nextInt(2) == 0?-1:1;
			
			//give a random speed between 1 and 3
			player.speed.x = random.nextInt(3)+1;
			player.speed.y = random.nextInt(3)+1;
		}
		while(isRunning) {
			//apply direction and speed
			for(int i = 0; i < players.size(); i++) {
				Player player = players.get(i);
				player.move(this.getBounds());
				if(player.changedDirection) {
					client.SyncDirection(player.lastDirection);
				}
			}
			
			myFrame.repaint();
			try {
				Thread.sleep(16);//16 ms is ~60 fps
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
enum GameState{
	LOBBY,
	GAME
}


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class TagGame extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8002395537343443859L;
	
	static Dimension playArea = new Dimension(600, 600);
	GameEngine gameEngine = null;
	static HashMap<String, Component> components = new HashMap<String, Component>();
	public TagGame() {
		
	}
	public void toggleRunningState(boolean s) {
		if(gameEngine != null) {
			GameEngine.isRunning = s;
		}
	}
	void toggleComponent(String name, boolean toggle) {
		if(components.containsKey(name)) {
			components.get(name).setVisible(toggle);
		}
	}
	void ChangePanels(JFrame frame) {
		switch(GameEngine.gameState) {
			case GAME:
				toggleComponent("lobby", false);
				toggleComponent("game", true);
				toggleComponent("score", true);
				toggleComponent("scores", true);
				break;
			case LOBBY:
				toggleComponent("lobby", true);
				toggleComponent("game", false);
				toggleComponent("score", false);
				toggleComponent("scores", false);
				break;
			default:
				break;
		}
		frame.pack();
        frame.revalidate();
        frame.repaint();
	}
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException ex) {
		} catch (InstantiationException ex) {
		} catch (IllegalAccessException ex) {
		} catch (UnsupportedLookAndFeelException ex) {
		}
		JFrame frame = new JFrame("Tag Game");
		JPanel gameCanvas = new TagGame();
		components.put("game", gameCanvas);
		gameCanvas.setMaximumSize(playArea);
		gameCanvas.setSize(playArea);
		gameCanvas.setPreferredSize(playArea);
		gameCanvas.setBorder(BorderFactory.createLineBorder(Color.black));
		gameCanvas.setVisible(false);
		
		JPanel scores = new JPanel();
		JTextArea f = new JTextArea();
		components.put("score", f);
		scores.setLayout(new BorderLayout());
		scores.add(f, BorderLayout.PAGE_START);
		f.setText("Scores");
		f.setPreferredSize(new Dimension(600, 20));
		scores.setVisible(false);
		
		components.put("scores", scores);
		
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
		JButton start = new JButton();
		start.setText("Connect");
		JTextField message = new JTextField(60);
		message.setEditable(false);
		
		components.put("lobby.message", message);
		
		start.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        //your actions
		    	((TagGame) gameCanvas).initialize();
		    	GameEngine refGE = ((TagGame)gameCanvas).gameEngine;
		    	try {
		    		refGE.doSocketConnect(
		    							host.getText().trim(), 
		    							port.getText().trim());
		    		System.out.println("Connected");
		    		((JTextField)components.get("lobby.message")).setText("Connected, loading into game");
		    		
			    	int l = (name.getText().length() <7?name.getText().length():7);
			    	refGE.localPlayer.name = name.getText().substring(0, l);
			    	frame.setTitle("Tag Game - " + refGE.localPlayer.name);
			        try {
						Thread.sleep(50);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
			        gameCanvas.setBorder(BorderFactory.createLineBorder(Color.black));
			        GameEngine.gameState = GameState.GAME;
			    	((TagGame)gameCanvas).ChangePanels(frame);
			        ((TagGame)gameCanvas).toggleRunningState(true);
			        refGE.run();
			        
			    	gameCanvas.requestFocusInWindow();
			    	PlayerControls.setKeyBindings(gameCanvas.getInputMap(), gameCanvas.getActionMap());
			    	
		    	}
		    	catch(Exception ex) {
		    		GameEngine.isRunning = false;
		    		System.out.println("Client not connected");
		    		((JTextField)components.get("lobby.message")).setText("Failed to connect: " + ex.getMessage());
		    	}
		    }
		});
		container.add(name);
		container.add(host);
		container.add(port);
		container.add(start);
		lobby.setLayout(new BoxLayout(lobby, BoxLayout.PAGE_AXIS));
		lobby.add(container);
		lobby.add(message);
		frame.setLayout(new BorderLayout());
		frame.add(scores, BorderLayout.NORTH);
		frame.add(lobby, BorderLayout.SOUTH);
		
		frame.add(gameCanvas, BorderLayout.CENTER);
		
		
		frame.pack();
		frame.setVisible(true);
		GameEngine.gameState = GameState.LOBBY;
		((TagGame)gameCanvas).ChangePanels(frame);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// do something
				((TagGame)gameCanvas).toggleRunningState(false);
				try {
					((TagGame)gameCanvas).gameEngine.CloseConnection();
				}
				catch(Exception ex) {
					System.out.println("Game Engine was null, safe to ignore");
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		
		//((TagGame)gameCanvas).start();
		
	}
	boolean flagRenderHint = false;
	Graphics2D g2d;
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g2d = (Graphics2D) g;
		if(!flagRenderHint) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		if(gameEngine != null) {
			gameEngine.paint(g2d);
		}
		g2d.dispose();
	}
	
	public void showScores(String str) {
		if(components.containsKey("score")) {
			((JTextArea)components.get("score")).setText(str);
		}
	}
	public void initialize() {
		gameEngine = new GameEngine(this, playArea);
	}
}
class TagAction extends AbstractAction{
	private static final long serialVersionUID = 397012257495431091L;

	@Override
	public void actionPerformed(ActionEvent e) {
		PlayerControls.SPACE_DOWN = true;
	}
	
}
class MoveAction extends AbstractAction{
	private static final long serialVersionUID = 5137817329873449021L;
	int x,y;
	boolean pressed = false;
	MoveAction(boolean pressed, int x, int y){
		this.x = x;
		this.y = y;
		this.pressed = pressed;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if (x == -1) {
			PlayerControls.LEFT_DOWN = pressed;
		}
		if (x == 1) {
			PlayerControls.RIGHT_DOWN = pressed;
		}
		if (y == -1) {
			PlayerControls.UP_DOWN = pressed;
		}
		if (y == 1) {
			PlayerControls.DOWN_DOWN = pressed;
		}
	}
	
}

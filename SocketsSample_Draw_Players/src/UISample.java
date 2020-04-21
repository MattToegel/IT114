import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class UISample extends JFrame implements OnReceive{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1743140661830829511L;
	static SocketClient client;
	static JPanel playArea;
	static PlayerContainer pc = new PlayerContainer();
	Player local;
	static Dimension size = new Dimension(400,600);
	public UISample() {
		super("Callable SocketClient");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// add a window listener
		this.addWindowListener(new WindowAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				// before we stop the JVM stop the example
				//client.isRunning = false;
				super.windowClosing(e);
			}
		});
	}
	public static Dimension GetPlayArea() {
		//playArea.getSize() works on client, but
		//when server uses PlayerContainer, its players don't
		//have access to playArea. Hard code it for now
		return size;//playArea.getSize();
	}
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException ex) {
		} catch (InstantiationException ex) {
		} catch (IllegalAccessException ex) {
		} catch (UnsupportedLookAndFeelException ex) {
		}
		UISample window = new UISample();
		window.setLayout(new BorderLayout());
		JPanel connectionDetails = new JPanel();
		JTextField host = new JTextField();
		host.setText("127.0.0.1");
		JTextField port = new JTextField();
		port.setText("3001");
		JButton connect = new JButton();
		
		connect.setText("Connect");
		connectionDetails.add(host);
		connectionDetails.add(port);
		connectionDetails.add(connect);
		window.add(connectionDetails, BorderLayout.NORTH);
		JPanel area = new JPanel();
		area.setLayout(new BorderLayout());
		window.add(area, BorderLayout.CENTER);
		JPanel playArea = new JPanel(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 2428478549031523456L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				//use the assigned background color
				g.setColor(this.getBackground());
				//grab the current dimensions
				Dimension d = this.getSize();
			    g.fillRect(0, 0, d.width, d.height);
			    g.setColor(Color.white);
			    Graphics2D g2d = (Graphics2D)g;
			    UISample.pc.movePlayers();
			    UISample.pc.paintPlayers(g2d);
			}
		};
		
		connect.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	client = new SocketClient();
		    	int _port = -1;
		    	try {
		    		_port = Integer.parseInt(port.getText());
		    	}
		    	catch(Exception num) {
		    		System.out.println("Port not a number");
		    	}
		    	if(_port > -1) {
			    	client = SocketClient.connect(host.getText(), _port);
			    	client.registerTransformListener(window);
			    	client.postConnectionData();
			    	connect.setEnabled(false);
			    	
			    	PlayerControls.setKeyBindings(playArea.getInputMap(), playArea.getActionMap());
			    	playArea.grabFocus();
			    	window.run();
		    	}
		    }
		});
		//Account for the connection panel in the play area layout
		//this helps the offset between the client and the server
		//there really should be a better way, but this works for example sake
		window.pack();
		size.height += connectionDetails.getSize().height;
		//end adjustment for connection panel
		UISample.playArea = playArea;
		playArea.setPreferredSize(size);
		playArea.setMinimumSize(size);
		playArea.setMaximumSize(size);
		playArea.setBorder(BorderFactory.createLineBorder(Color.black));
		area.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		area.add(playArea, BorderLayout.CENTER);
		
		Dimension frameSize = size;
		frameSize.width = (int) (frameSize.width * 1.1f);
		frameSize.height = (int) (frameSize.height * 1.1f);
		window.setPreferredSize(frameSize);
		window.setMinimumSize(frameSize);
		window.setMaximumSize(frameSize);
		window.setResizable(false);
		window.pack();
		window.setVisible(true);
	}
	public void run() {
		Thread gameLoop = new Thread() {
			@Override
			public void run() {
				while(true) {
					if(local != null) {
						PlayerControls.applyControls(local, client);
					}
					playArea.repaint();
					try {
						Thread.sleep(16);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		gameLoop.setDaemon(true);
		gameLoop.start();
	}
	@Override
	public void onReceivedMessage(String msg) {
		System.out.println("Unhandled, but here's the message: " + msg);
	}
	@Override
	public void onReceivedTransform(int playerId, PayloadType type, int x, int y) {
		if (local == null) {
			return;
		}
		pc.updatePlayers(playerId, type, x, y);
	}
	@Override
	public void onPlayerConnected(int id, int x, int y, String name) {
		System.out.println(name + " connected");
		Player p = new Player(name);
		p.setPosition(x, y);
		p.setDirection(0,0);
		p.setID(id);
		if(local == null) {
			//TODO slight chance this player isn't us, but should be fine
			local = p;
		}
		pc.addPlayer(id, p);
	}
	@Override
	public void onPlayerDisconnected(int id) {
		pc.removePlayer(id);
	}
}


import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Consumer;

public class GameEngine {
	PlayerContainer players = new PlayerContainer();
	Dimension playArea = new Dimension(0,0);
	static boolean isRunning = false;
	NetworkClient client;
	LocalPlayer localPlayer;
	TagGame ui;
	static GameState gameState = GameState.LOBBY;
	public GameEngine (TagGame ui, Dimension playArea) {
		this.ui = ui;
		this.playArea = playArea;
		this.localPlayer = new LocalPlayer();
		//start();
	}
	public static float lerp(float a, float b, float f)
	{
	    return a + f * (b - a);
	}
	public static double distance(
			  double x1, 
			  double y1, 
			  double x2, 
			  double y2) {       
	    return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}
	public void run() {
		Thread gameLoop = new Thread() {
			@Override
			public void run() {
				sendConnectPayload();
				Consumer<Payload> c = payload -> processFromServer(payload);
				while (isRunning) {
					//process messages from server
					client.handleQueuedMessages(c, 10);
					//apply current control state
					PlayerControls.applyControls(localPlayer.address, localPlayer.player, client);
					//locally move the players
					players.MovePlayers();
					//redraw the UI/players
					ui.repaint();
					try {
						Thread.sleep(16);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
				System.out.println("Gameloop exiting");
			}
		};
		gameLoop.start();
		System.out.println("Gameloop starting");
	}
	public boolean doSocketConnect(String host, String port) throws NumberFormatException, UnknownHostException, IOException {
		System.out.println("Connecting " + host + ":" + port);
		if(client == null) {
			client = new NetworkClient();
		}
		return client.connect(host, Integer.parseInt(port));
	}
	public void CloseConnection() {
		//TODO send disc
		client.disconnect(localPlayer.address);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.Terminate();
	}
	void sendConnectPayload() {
		String ipAddress = "";
		try {
			ipAddress = InetAddress.getLocalHost().getHostAddress().trim();
			System.out.println("My IP Address: " + ipAddress);
		}
		catch(UnknownHostException e) {
			e.printStackTrace();
		}
		//TODO local testing append name, doesn't really matter though
		ipAddress += localPlayer.name;
		//end TODO
		localPlayer.address = ipAddress;
		
		//send 0,0 coords, server will fill and echo back
		client.Send(localPlayer.address, PayloadType.CONNECT,0,0, localPlayer.name);
	}
	public void paint(Graphics2D g2d) {
		players.PaintPlayers(g2d);
	}
	void processFromServer(Payload p) {
		if(p.payloadType == PayloadType.CONNECT || p.payloadType == PayloadType.SYNC) {
			//connection from server, create new player and track locally
			Player sync = players.getPlayer(p.ipAddress);
			if(sync != null) {
				sync.setPosition(p.x, p.y);
				if(localPlayer.player == null && localPlayer.address.equals(p.ipAddress)) {
					System.out.println("Stored my player existing");
					localPlayer.player = sync;
					localPlayer.name = sync.getName();
				}
			}
			else {
				sync = new Player(p.extra, playArea);
				sync.setPosition(p.x, p.y);
				players.AddPlayer(p.ipAddress, sync);
				if(p.ipAddress.contentEquals(localPlayer.address)) {
					//This should be the server giving us our player
					//so cache it for easier use
					sync.setName(sync.getName());
					localPlayer.player = sync;
					localPlayer.name = sync.getName();
					
					System.out.println("Created local player");
				}
			}
		}
		else if(p.payloadType == PayloadType.DISCONNECT) {
			//disconnection from server, update local track of players
			players.RemovePlayer(p.ipAddress);
			System.out.println("Removing player for " + p.ipAddress);
		}
		else {
			//check if we have the respective player
			//if not, try to add if the information is available
			if(p.ipAddress != null) {
				Player c = players.getPlayer(p.ipAddress);
				if(c == null) {
					if(p.extra != null) {
						c = new Player(p.extra, playArea);
						players.AddPlayer(p.ipAddress, c);
					}
				}
				//try to update the player with whatever payload we received
				players.UpdatePlayer(p.ipAddress, p.payloadType, p.x, p.y, p.extra);
			}
			if(p.payloadType == PayloadType.STATS) {
				//handled via player update, but we'll update score list here
				List<Player> leaderboard = players.getLeaderboard();
				String show = "| ";
				int total = leaderboard.size();
				for(int i = 0; i < 3; i++) {
					if(i == total) {
						break;
					}
					Player l = leaderboard.get(i);
					show += l.getName() + "(Tagged: #" + l.getNumberTagged() +") | ";
							
				}
				ui.showScores(show);
			}
		}
	}
}
class LocalPlayer {
	public String address;
	public String name;
	public Player player;
}

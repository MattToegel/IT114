package mt.ws.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mt.ws.dataobject.Player;
import mt.ws.network.client.SocketClient;

public class GameEngine extends _GameEngine{
	
	public static List<Player> players = new ArrayList<Player>();
	GameClient ui;
	SocketClient client;
	public void connect(String host, int port, String clientName) {
		client = SocketClient.connect(host, port);
		if(client != null && SocketClient.isConnected) {
			client.setClientName(clientName);
		}
	}
	public void SetUI(GameClient ui) {
		this.ui = ui;
	}
	@Override
	protected void Awake() {
		// TODO Auto-generated method stub
		System.out.println("Game Engine Awake");
		
	}

	@Override
	protected void OnStart() {
		// TODO Auto-generated method stub
		System.out.println("Game Engine Start");
		for(int i = 0; i < 5; i++) {
			Player p = new Player();
			players.add(p);
			System.out.println("Added player " + i);
		}
		
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
			System.out.println("Set player defaults " + i);
		}
	}

	@Override
	protected void Update() {
		// TODO Auto-generated method stub
		if(ui == null) {
			return;
		}
		for(int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			player.move(ui.getBounds());
			if(SocketClient.isConnected && player.changedDirection) {
				client.SyncDirection(player.getLastDirection());
			}
		}
	}

	@Override
	protected void End() {
		// TODO Auto-generated method stub
		System.out.println("Game Engine End");
	}

	@Override
	protected void UILoop() {
		// TODO Auto-generated method stub
		if(ui == null) {
			return;
		}
		ui.draw();
	}

}

package mt.ws.client;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;

import mt.ws.dataobject.GameObject;
import mt.ws.dataobject.Player;
import mt.ws.network.client.SocketClient;
import ws.dyn4j.framework.SimulationBody;

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
		for(int i = 0; i < 1; i++) {
			Player p = new Player();
			players.add(p);
			System.out.println("Added player " + i);
		}
		
		Random random = new Random();
		//give a random direction
		//based on 0 or 1
		for(int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			//player.direction.x = random.nextInt(2) == 0?-1:1;
			//player.direction.y = random.nextInt(2) == 0?-1:1;
			
			//give a random speed between 1 and 3
			player.speed.x = random.nextInt(3)+1;
			player.speed.y = random.nextInt(3)+1;
			System.out.println("Set player defaults " + i);
		}
		//TODO add this later _initWorld();
	}

	@Override
	protected void Update() {
		// TODO Auto-generated method stub
		if(ui == null) {
			return;
		}
		
		for(int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			PlayerControls.applyControls(player, client);
			if(player.isJumping()) {
				player.direction.y = -1;
			}
			else {
				player.direction.y = 1;
			}
			player.move(ui.getGameArea());
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
	
	//Game logic
	
	
	//reference: https://github.com/dyn4j/dyn4j-samples/blob/master/src/main/java/org/dyn4j/samples/SimplePlatformer.java
	/** The dynamics engine */
	protected World world;
	private static final Object FLOOR_BODY = new Object();
	void _initWorld() {
		this.world = new World();
		// create the floor
		Rectangle floorRect = new Rectangle(15.0, 1.0);
		GameObject floor = new GameObject();
		floor.addFixture(new BodyFixture(floorRect));
		floor.setMass(MassType.INFINITE);
		// move the floor down a bit
		floor.translate(0.0, -4.0);
		this.world.addBody(floor);
	}
	void _obstacles(){
		// some obstacles
		final int n = 5;
		for (int i = 0; i < n; i++) {
			SimulationBody sb = new SimulationBody();
			double w = 1.0;
			double h = Math.random() * 0.3 + 0.1;
			sb.addFixture(Geometry.createIsoscelesTriangle(w, h));
			sb.translate((Math.random() > 0.5 ? -1 : 1) * Math.random() * 5.0, h * 0.5 - 2.9);
			sb.setMass(MassType.INFINITE);
			sb.setUserData(FLOOR_BODY);
			world.addBody(sb);
		}
	}
}

package mt.ws.client;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.Step;
import org.dyn4j.dynamics.StepAdapter;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

import mt.ws.dataobject.GameObject;
import mt.ws.dataobject.Player;
import mt.ws.dataobject.PlayerContainer;
import mt.ws.network.client.SocketClient;

public class GameEngine extends _GameEngine{
	GameClient ui;
	SocketClient client;
	public static float SCALE = 45;
	/** The conversion factor from nano to base */
	public static final double NANO_TO_BASE = 1.0e9;
	/** The time stamp for the last iteration */
	public static long last;
	static PlayerContainer players = new PlayerContainer();
	Player localPlayerRef;
	double gravity = 1;
	public void connect(String host, int port, String clientName) {
		client = SocketClient.connect(host, port);
		client.setGameEngine(this);
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
		_initWorld();
	}

	@Override
	protected void OnStart() {
		// TODO Auto-generated method stub
		System.out.println("Game Engine Start");
		
	}

	@Override
	protected void Update() {
		// TODO Auto-generated method stub
		if(ui == null) {
			return;
		}
		
		
		//do local things
		if(localPlayerRef != null) {
			PlayerControls.applyControls(localPlayerRef, client);
			if(localPlayerRef.isJumping()) {
				localPlayerRef.overrideGravity(-2);
			}
			else {
				localPlayerRef.overrideGravity(1);
			}
		}
		players.movePlayers();
		
		// update the World
        
        // get the current time
        long time = System.nanoTime();
        // get the elapsed time from the last iteration
        long diff = time - GameEngine.last;
        // set the last time
        GameEngine.last = time;
    	// convert from nanoseconds to seconds
    	double elapsedTime = diff / NANO_TO_BASE;
        // update the world with the elapsed time
        this.world.update(elapsedTime);
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
	public void setPlayerName(String name) {
		
	}
	public void addPlayer(int id, int x, int y, boolean isMe, String name) {
		Player newPlayer = new Player();
		newPlayer.setName(name);
		newPlayer.setPosition(x, y);
		newPlayer.setID(id);
		players.addPlayer(id, newPlayer);
		if(isMe) {
			localPlayerRef = newPlayer;
			System.out.println("Created Local Player");
		}
		else {
			System.out.println("Created remote player");
		}
		Rectangle rectShape = new Rectangle(1, 1);
		BodyFixture bf = new BodyFixture(rectShape);
		bf.setFriction(.2);
		newPlayer.addFixture(rectShape);
		newPlayer.setMass(MassType.NORMAL);
		newPlayer.setLinearVelocity(new Vector2(0,0));
		newPlayer.setUserData(BOX_BODY);
		Player.setScale(GameEngine.SCALE);
		this.world.addBody(newPlayer);
	}
	public void removePlayer(int id) {
		Player removedPlayer = players.removePlayer(id);
		this.world.removeBody(removedPlayer);
	}
	//reference: https://github.com/dyn4j/dyn4j-samples/blob/master/src/main/java/org/dyn4j/samples/SimplePlatformer.java
	/** The dynamics engine */
	protected World world;
	private static final Object FLOOR_BODY = new Object();
	private static final Object WALL_BODY = new Object();
	private static final Object GOAL_BODY = new Object();
	private static final Object BOX_BODY = new Object();
	private ScheduledThreadPoolExecutor exec;

	void _initWorld() {
		this.world = new World();
		// create the floor
		Rectangle floorRect = new Rectangle(18.0, 1.0);
		GameObject floor = new GameObject();
		BodyFixture bf = new BodyFixture(floorRect);
		bf.setFriction(.2);
		floor.addFixture(bf);
		floor.setMass(MassType.INFINITE);
		// move the floor down a bit
		floor.translate(0.0, -8.0);
		floor.setUserData(FLOOR_BODY);
		this.world.addBody(floor);
		//create and add a wall
		Rectangle wallRect = new Rectangle(1,18);
		GameObject leftWall = new GameObject();
		BodyFixture lwdf = new BodyFixture(wallRect);
		leftWall.addFixture(lwdf);
		leftWall.setMass(MassType.INFINITE);
		leftWall.translate(-9, 0);
		leftWall.setUserData(WALL_BODY);
		this.world.addBody(leftWall);
		//add another
		GameObject rightWall = new GameObject();
		BodyFixture rwbf = new BodyFixture(wallRect);
		rightWall.addFixture(rwbf);
		rightWall.setMass(MassType.INFINITE);
		rightWall.translate(9, 0);
		rightWall.setUserData(WALL_BODY);
		this.world.addBody(rightWall);
		//add the goal spot
		BodyFixture goalbf = new BodyFixture(floorRect);
		GameObject goal = new GameObject();
		goal.addFixture(goalbf);
		goal.setMass(MassType.INFINITE);
		goal.translate(0, 6);
		goal.setUserData(GOAL_BODY);
		this.world.addBody(goal);
		World world = this.world;
		Random r = new Random();
		exec = new ScheduledThreadPoolExecutor(3);
		timer(r);
		this.world.addListener(new StepAdapter() {
			@Override
			public void begin(Step step, World world) {
				// at the beginning of each world step, check if the body is in
				// contact with any of the floor bodies
				
				for (int i = 0; i < world.getBodyCount(); i++) {
					boolean isGround = false;
					
					GameObject go = (GameObject)world.getBody(i);
					List<Body> bodies = go.getInContactBodies(false);
					for (int k = 0; k < bodies.size(); k++) {
						Object ud = bodies.get(k).getUserData();
						if (ud == FLOOR_BODY || ud == BOX_BODY) {
							isGround = true;
							break;
						}
						else if(ud == GOAL_BODY) {
							//win
						}
					}
					go.isGrounded = isGround;
				}
			}
		});
	}
	void timer(Random r) {
		exec.schedule(()->{
			_spawnBoxes();
      	  timer(r);
        }, (r.nextInt(15)+5), TimeUnit.SECONDS);
	}
	void _spawnBoxes() {
		Rectangle rect = new Rectangle(1,1);
		GameObject box = new GameObject();
		box.addFixture(rect);
		box.setMass(MassType.NORMAL);
		Random r = new Random();
		int coin = r.nextInt(1);
		coin = coin>0?1:-1;
		double x = r.nextInt(5) * coin;
		box.translate(x, 6);
		box.setUserData(BOX_BODY);
		this.world.addBody(box);
	}
}

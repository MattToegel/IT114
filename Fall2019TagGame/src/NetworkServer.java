

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Queue;
import java.util.Random;

public class NetworkServer{
	int port = -1;
	static int id = 0;
	//private Thread clientListenThread = null;
	List<ServerThread> clients = new ArrayList<ServerThread>();
	public static boolean isRunning = true;
	Queue<Payload> outMessages = new LinkedList<Payload>();
	Dimension playArea = new Dimension(600,600);
	PlayerContainer players = new PlayerContainer();
	Random random = new Random();
	static boolean verbose = true;
	private ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
	public NetworkServer() {
		EstMem.Start();
		isRunning = true;
		EstMem.Snapshot();
		exec.scheduleAtFixedRate(()->{
			EstMem.Snapshot();
		}, 5, 5, TimeUnit.SECONDS);
	}
	public static void Output(String str) {
		if(verbose) {
			System.out.println(str);
		}
	}
	public synchronized void sendToAllClientsExcept(Payload payload, int excludeId) {
		//iterate through all clients and attempt to send the message to each
		if(payload.payloadType != PayloadType.MOVE_SYNC) {
			//ignore MOVE_SYNC to cut down on log spam
			NetworkServer.Output("Sending message to " + clients.size() + " clients");
		}
		for(int i = 0; i < clients.size(); i++) {
			if(clients.get(i).id == excludeId) {
				continue;
			}
			try {
				clients.get(i).send(payload);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		cleanupStaleClients();
	}
	public synchronized void sendToClientById(int target, Payload payload) {
		//TODO for single client
		for(int i = 0; i < clients.size(); i++) {
			if(clients.get(i).id == target) {
				try {
					clients.get(i).send(payload);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		cleanupStaleClients();
	}
	void cleanupStaleClients() {
		Iterator<ServerThread> it = clients.iterator();
		while(it.hasNext()) {
			ServerThread s = it.next();
			if(s.isClosed()) {
				s.cleanup();
				outMessages.add(
						new Payload(s.id, PayloadType.DISCONNECT)
						);
				s.stopThread();
				it.remove();
			}
		}
	}
	private void start(int port) {
		this.port = port;
		System.out.println("Waiting for client");
		try(ServerSocket serverSocket = new ServerSocket(port);){
			trySendMessagesToClients();
			runGameLoop();
			listenForConnections(serverSocket);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				isRunning = false;
				Thread.sleep(50);
				System.out.println("closing server socket");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	void trySendMessagesToClients() {
		Thread messageSender = new Thread() {
			@Override
			public void run() {
				System.out.println("Starting Message Sender");
				while(isRunning) {
					Payload payloadOut = outMessages.poll();
					if(payloadOut != null) {
						//TODO send message to client(s)
						if(payloadOut.target > -1) {
							sendToClientById(payloadOut.target, payloadOut);
						}
						else {
							//Note: we're currently not using the exclusion
							sendToAllClientsExcept(payloadOut, -1);
						}
					}
					else {
						//if we don't have a message take a rest
						try {
							Thread.sleep(16);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				System.out.println("Message Sender Thread stopping");
			}
		};
		messageSender.start();
	}
	void runGameLoop() {
		Thread gameLoop = new Thread() {
			@Override
			public void run() {
				int syncCounter = 0;
				System.out.println("Server game loop starting");
				while(isRunning) {
					players.movePlayers();
					syncCounter++;
					//every thread.sleep * 20ms force sync all players
					//we don't want to do this too often for bandwidth concerns
					if(syncCounter > 20) {
						syncCounter = 0;
						for(Entry<Integer, Player> p : players.players.entrySet()) {
							outMessages.add(
									new Payload(p.getKey(), PayloadType.MOVE_SYNC,
											p.getValue().getPosition().x, p.getValue().getPosition().y)
									);
						}
					}
					try {
						Thread.sleep(16);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("Server game loop stopping");
			}
		};
		gameLoop.start();
	}
	void listenForConnections(ServerSocket serverSocket) {
		while(isRunning) {
			try {
				//TODO listen for new connections
				Socket client = serverSocket.accept();
				System.out.println("Client connected");
				ServerThread thread = new ServerThread(client, this);
				thread.start();//start client thread
				clients.add(thread);//add to client pool
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] arg) {
		System.out.println("Starting Server");
		NetworkServer server = new NetworkServer();
		int port = -1;
		if(arg.length > 0){
			try{
				port = Integer.parseInt(arg[0]);
			}
			catch(Exception e){
				System.out.println("Invalid port: " + arg[0]);
			}		
		}
		if(port > -1){
			System.out.println("Server listening on port " + port);
			server.start(port);
		}
		System.out.println("Server Stopped");
	}
}
//Class to hold client connection and prevent it from blocking the main thread of the server
class ServerThread extends Thread{
	private Socket client;
	private String clientName;
	public int id;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean isRunning = false;
	private NetworkServer server;
	public ServerThread(Socket myClient, NetworkServer server) throws IOException {
		this.client = myClient;
		this.server = server;
		isRunning = true;
		out = new ObjectOutputStream(client.getOutputStream());
		in = new ObjectInputStream(client.getInputStream());
		System.out.println("Spawned thread for client " + this.id);
	}
	public boolean isClosed() {
		return client.isClosed();
	}
	Point dir = new Point(-2,-2);
	private void createAndSync(int id, String name) {
		this.id = id;
		Player newPlayer = new Player(name);
		newPlayer.setID(id);
		server.players.addPlayer(id, newPlayer);
		//generate random position and no direction
		//account for radius offset so we don't get stuck in a wall
		int offset = newPlayer.getRadius() + 1;
		int x = server.random.nextInt(server.playArea.width - offset) + offset;
		int y = server.random.nextInt(server.playArea.height - offset) + offset;
		newPlayer.setPosition(x, y);
		newPlayer.setDirection(0, 0);
		//update newly connected player's id
		//this only goes to the new player
		server.outMessages.add(
				new Payload(id, PayloadType.ACK, x, y, name, id));
		//Send Player Connect for new client to all clients
		server.outMessages.add(
				new Payload(id, PayloadType.CONNECT,x, y, name)
				);
		//Send Move Sync Payload for new client to all clients
		server.outMessages.add(
				new Payload(id, PayloadType.MOVE_SYNC, x, y, name)
				);
		//Send Direction Payload for new client to all clients
		server.outMessages.add(
				new Payload(id, PayloadType.DIRECTION, 0, 0, name)
				);
		//send sync details for each previously connected client to newly connected client
		server.players.players.forEach((pid, p)->{
				//NetworkServer.Output("Adding sync message for " + id + " about " + pid);
				//send sync to target client
				server.outMessages.add(
						new Payload(pid, PayloadType.SYNC, p.getPosition().x, 
								p.getPosition().y, p.getName(), id)
						);
				//send direction to target client
				server.outMessages.add(
						new Payload(pid, PayloadType.DIRECTION, p.getDirection().x,
								p.getDirection().y, p.getName(), id)
						);
				//send stats for each to target client
				server.outMessages.add(
						new Payload(pid, PayloadType.STATS, p.getNumberOfTags(), p.getNumberTagged(), null, id)
						);
			
		});
		Entry<Integer,Player> tagger = server.players.getCurrentTagger();
		if(tagger != null) {
			//send IT to all
			server.outMessages.add(
					new Payload(tagger.getKey(), PayloadType.SET_IT)
					);
		}
		if(server.players.getTotalPlayers() > 1) {
			checkTagger();
		}
	}
	private void handleTagging(int id) throws Exception {
		Player player = server.players.getPlayer(id);
		if(player != null && player.isIt()) {
			NetworkServer.Output(player.getID() + " is tagging");
			Entry<Integer,Player> tagged = server.players.checkCollisions(player);
			if(tagged != null) {
				Player taggedPlayer = tagged.getValue();
				System.out.println("Tagged " + taggedPlayer.getName() + "(" + taggedPlayer.getID() + ")");
				if(taggedPlayer.getID() == id) {
					throw new Exception("Somehow we tagged ourself");
				}
				//update server state
				server.players.updatePlayers(tagged.getKey(), PayloadType.SET_IT,
						taggedPlayer.getPosition().x,
						taggedPlayer.getPosition().y,
						taggedPlayer.getName());
				//tagged, make player it and tell all clients
				server.outMessages.add(
						new Payload(tagged.getKey(), PayloadType.SET_IT, 0,0, taggedPlayer.getName())
						);
				
				//update stats of both players and sync with all clients
				taggedPlayer.incrementTagged();
				player.incrementTags();
				server.outMessages.add(
						new Payload(id, PayloadType.STATS, player.getNumberOfTags(), player.getNumberTagged())
						);
				server.outMessages.add(
						new Payload(tagged.getKey(), PayloadType.STATS, taggedPlayer.getNumberOfTags(), taggedPlayer.getNumberTagged())
						);
			}
			else {
				NetworkServer.Output("No collisions for Tag");
			}
		}
	}
	synchronized void processPayload(int id, Payload payloadIn) {
		Player player = null;
		//NetworkServer.Output("Processing payload from " + clientIp);
		//NetworkServer.Output("Type: " + payloadIn.payloadType.toString());
		switch(payloadIn.payloadType) {
			case CONNECT:
				//add player to internal map
				System.out.println("Player connected with name " + payloadIn.extra);
				payloadIn.extra = WordBlackList.filteredName(payloadIn.extra);
				NetworkServer.id++;
				createAndSync(NetworkServer.id, payloadIn.extra);
				break;
			case DIRECTION:
				//blindly update direction
				player = server.players.getPlayer(id);
				dir.x = payloadIn.x;
				dir.y = payloadIn.y;
				if(player.setDirection(dir)) {
					//if direction changed, send to clients
					server.outMessages.add(
							new Payload(id, PayloadType.DIRECTION, dir.x, dir.y)
							);
				}
				break;
			case DISCONNECT:
				//TODO make sure other client can't cause a different client to disconnect
				player = server.players.removePlayer(id);
				if(player != null) {
					server.outMessages.add(
							new Payload(id, PayloadType.DISCONNECT)
							);
				}
				
				checkTagger();
				break;
			/*case MOVE_SYNC:
				//server sends this, doesn't receive it
				break;
			case SET_IT:
				//server sends this, doesn't receive it
				break;
			case SPEED_BOOST:
				//server sends this, doesn't receive it
				break;*/
			case TRIGGER_TAG:
				//check collision and see if tagged
				NetworkServer.Output("Handling trigger tag for " + id);
				try {
					handleTagging(id);
				}
				catch(Exception e) {
					NetworkServer.Output(e.getMessage());
				}
				break;
			default:
				break;
			
		}
	}
	void checkTagger(){
		if(!server.players.isThereATagger() && server.players.getTotalPlayers() > 1) {
			Thread pickATagger = new Thread() {
				@Override
				public void run() {
					System.out.println("Trying to pick tagger");
					int rp = server.random.nextInt((server.players.getTotalPlayers() - 1));
					System.out.println("Using index " + rp);
					int setItForID = -1;
					while(isRunning && setItForID == -1) {
						setItForID = server.players.getIdByIndex(rp);
						if(setItForID > -1) {
							server.outMessages.add(
									new Payload(setItForID, PayloadType.SET_IT)
									);
							System.out.println("Making " + setItForID + " IT");
							server.players.updatePlayers(setItForID, PayloadType.SET_IT,0,0,"");
						}
					}
					System.out.println("Tagger thread stopping");
				}
			};
			pickATagger.start();
			
		}
	}
	@Override
	public void run() {
		try{
			Payload fromClient;
			while(isRunning && (fromClient = (Payload)in.readObject()) != null) {
				System.out.println("Received: " + fromClient);
				System.out.println("Client IP: " + fromClient.id);
				processPayload(fromClient.id,fromClient);
			}
		}
		catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("Server cleaning up IO for " + clientName);
			server.outMessages.add(
						new Payload(id, PayloadType.DISCONNECT)
						);
			cleanup();
		}
	}
	public void stopThread() {
		isRunning = false;
	}
	public void send(Payload msg) throws IOException {
		out.writeObject(msg);
	}
	void cleanup() {
		if(in != null) {
			try{in.close();}
			catch(Exception e) { System.out.println("Input already closed");}
		}
		if(out != null) {
			try {out.close();}
			catch(Exception e) {System.out.println("Output already closed");}
		}
	}
	
}

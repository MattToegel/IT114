

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
import java.util.Queue;
import java.util.Random;

public class NetworkServer{
	int port = -1;
	//private Thread clientListenThread = null;
	List<ServerThread> clients = new ArrayList<ServerThread>();
	public static boolean isRunning = true;
	Queue<Payload> outMessages = new LinkedList<Payload>();
	Dimension playArea = new Dimension(600,600);
	PlayerContainer players = new PlayerContainer();
	Random random = new Random();
	static boolean verbose = true;
	public NetworkServer() {
		isRunning = true;
	}
	public static void Output(String str) {
		if(verbose) {
			System.out.println(str);
		}
	}
	public synchronized void broadcast(Payload payload, String excludeIp) {
		//iterate through all clients and attempt to send the message to each
		if(payload.payloadType != PayloadType.MOVE_SYNC) {
			//ignore MOVE_SYNC to cut down on log spam
			NetworkServer.Output("Sending message to " + clients.size() + " clients");
		}
		//TODO ensure closed clients are removed from the list
		for(int i = 0; i < clients.size(); i++) {
			if(clients.get(i).ipAddress.equals(excludeIp)) {
				continue;
			}
			try {
				clients.get(i).send(payload);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		//cleanup
		cleanupStaleClients();
	}
	public synchronized void sync(String target, Payload payload) {
		//TODO for single client
		for(int i = 0; i < clients.size(); i++) {
			if(clients.get(i).ipAddress.equals(target)) {
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
				outMessages.add(
						new Payload(s.ipAddress, PayloadType.DISCONNECT)
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
			Thread messageSender = new Thread() {
				@Override
				public void run() {
					System.out.println("Starting Message Sender");
					while(isRunning) {
						Payload payloadOut = outMessages.poll();
						if(payloadOut != null) {
							//broadcast(payloadOut,"");
							if(payloadOut.payloadType != PayloadType.SYNC) {
								broadcast(payloadOut, "");
							}
							else {
								sync(payloadOut.target,payloadOut);
							}
						}
						else {
							try {
								Thread.sleep(8);
								
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					System.out.println("Message Sender Thread stopping");
				}
			};
			messageSender.start();
			Thread gameLoop = new Thread() {
				@Override
				public void run() {
					int syncCounter = 0;
					System.out.println("Server game loop starting");
					while(isRunning) {
						players.MovePlayers();
						syncCounter++;
						if(syncCounter > 20) {
							syncCounter = 0;
							for(Entry<String, Player> p : players.players.entrySet()) {
								outMessages.add(
										new Payload(p.getKey(), PayloadType.MOVE_SYNC, p.getValue().getPosition().x, p.getValue().getPosition().y, null)
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
			while(isRunning) {
				try {
					//use Consumer class to pass a callback to the thread for broadcasting messages
					//to all clients
					//Consumer<Payload,String> callback = ;
					Socket client = serverSocket.accept();
					System.out.println("Client connected");
					ServerThread thread = new ServerThread(client, this);
					thread.start();//start client thread
					clients.add(thread);//add to client pool
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
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
	public String ipAddress;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean isRunning = false;
	
	private NetworkServer server;
	public ServerThread(Socket myClient, NetworkServer server) throws IOException {
		this.client = myClient;
		this.server = server;
		this.ipAddress = myClient.getLocalAddress().getHostAddress();
		//this.clientName = clientName;
		//this.callback = callback;
		isRunning = true;
		out = new ObjectOutputStream(client.getOutputStream());
		in = new ObjectInputStream(client.getInputStream());
		System.out.println("Spawned thread for client " + this.ipAddress);
	}
	public boolean isClosed() {
		return client.isClosed();
	}
	Point dir = new Point(-2,-2);
	private void createAndSync(String clientIp, String name) {
		Player player = new Player(name, server.playArea);
		server.players.AddPlayer(clientIp, player);
		//generate random position and no direction
		int x = server.random.nextInt(server.playArea.width - 45) + 15;
		int y = server.random.nextInt(server.playArea.height - 45) + 15;
		player.setPosition(x, y);
		player.setDirection(0, 0);
		//send across to clients
		//Send Player Connect Payload
		server.outMessages.add(
				new Payload(clientIp, PayloadType.CONNECT,x, y, name)
				);
		//Send Move Sync Payload
		server.outMessages.add(
				new Payload(clientIp, PayloadType.MOVE_SYNC, x, y, name)
				);
		//Send Direction Payload
		server.outMessages.add(
				new Payload(clientIp, PayloadType.DIRECTION, 0, 0, name)
				);
		//send sync details for each previously connected client to newly connected client
		server.players.players.forEach((ip, p)->{
			
			NetworkServer.Output("Adding sync message for " + clientIp + " about " + ip);
				//send sync to target client
				server.outMessages.add(
						new Payload(ip, PayloadType.SYNC, p.getPosition().x, 
								p.getPosition().y, p.getName(), clientIp)
						);
				//send direction to target client
				server.outMessages.add(
						new Payload(ip, PayloadType.DIRECTION, p.getDirection().x,
								p.getDirection().y, p.getName(), clientIp)
						);
				Entry<String,Player> tagger = server.players.getCurrentTagger();
				if(tagger != null) {
					//send IT to all
					server.outMessages.add(
							new Payload(tagger.getKey(), PayloadType.SET_IT)
							);
				}
				//send stats to all
				server.outMessages.add(
						new Payload(ip, PayloadType.STATS, p.getNumberOfTags(), p.getNumberTagged())
						);
			
		});
		if(server.players.getTotalPlayers() > 1) {
			checkTagger();
		}
	}
	private void handleTagging(String clientIp) {
		Player player = server.players.getPlayer(clientIp);
		if(player != null && player.isIt()) {
			NetworkServer.Output("Tagger is tagging");
			Entry<String,Player> tagged = server.players.CheckCollisions(player);
			if(tagged != null) {
				Player taggedPlayer = tagged.getValue();
				System.out.println("Tagged " + taggedPlayer.getName());
				
				//add ip to player object
				//update server state
				server.players.UpdatePlayer(tagged.getKey(), PayloadType.SET_IT,
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
						new Payload(clientIp, PayloadType.STATS, player.getNumberOfTags(), player.getNumberTagged())
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
	synchronized void processPayload(String clientIp, Payload payloadIn) {
		Player player = null;
		//NetworkServer.Output("Processing payload from " + clientIp);
		//NetworkServer.Output("Type: " + payloadIn.payloadType.toString());
		switch(payloadIn.payloadType) {
			case CONNECT:
				//add player to internal map
				System.out.println("Player connected with name " + payloadIn.extra);
				payloadIn.extra = WordBlackList.filteredName(payloadIn.extra);
				createAndSync(clientIp, payloadIn.extra);
				break;
			case DIRECTION:
				//blindly update direction
				player = server.players.getPlayer(clientIp);
				dir.x = payloadIn.x;
				dir.y = payloadIn.y;
				if(player.setDirection(dir)) {
					//if direction changed, send to clients
					server.outMessages.add(
							new Payload(clientIp, PayloadType.DIRECTION, dir.x, dir.y)
							);
				}
				break;
			case DISCONNECT:
				//TODO make sure other client can't cause a different client to disconnect
				player = server.players.RemovePlayer(clientIp);
				if(player != null) {
					server.outMessages.add(
							new Payload(clientIp, PayloadType.DISCONNECT)
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
				NetworkServer.Output("Handling trigger tag for " + clientIp);
				handleTagging(clientIp);
				break;
			default:
				break;
			
		}
	}
	void checkTagger() {
		if(!server.players.isThereATagger() && server.players.getTotalPlayers() > 1) {
			Thread pickATagger = new Thread() {
				@Override
				public void run() {
					System.out.println("Trying to pick tagger");
					int rp = server.random.nextInt((server.players.getTotalPlayers() - 1));
					System.out.println("Using index " + rp);
					String setItForAddress = null;
					while(isRunning && setItForAddress == null) {
						setItForAddress = server.players.getAddressByIndex(rp);
						if(setItForAddress==null) {
							System.out.println("Found null client");
							continue;
						}
						server.outMessages.add(
								new Payload(setItForAddress, PayloadType.SET_IT)
								);
						System.out.println("Making " + setItForAddress + " IT");
						server.players.UpdatePlayer(setItForAddress, PayloadType.SET_IT,0,0,"");
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
				System.out.println("Client IP: " + fromClient.ipAddress);
				processPayload(fromClient.ipAddress,fromClient);
			}
		}
		catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("Server cleaning up IO for " + clientName);
			server.outMessages.add(
						new Payload(ipAddress, PayloadType.DISCONNECT)
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

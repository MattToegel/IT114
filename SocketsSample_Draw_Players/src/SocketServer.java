import java.awt.Point;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class SocketServer {
	int port = 3002;
	public static boolean isRunning = true;
	private List<ServerThread> clients = new ArrayList<ServerThread>();
	public static int ClientID = 0;
	PlayerContainer pc = new PlayerContainer();
	public synchronized int getNextId() {
		ClientID++;
		return ClientID;
	}
	public synchronized void syncPlayers(int id) {
		for(var containerSet : pc.players.entrySet()) {
			Payload payload = new Payload();
			payload.setId(containerSet.getKey());
			payload.setPayloadType(PayloadType.CONNECT);
			Point pos = containerSet.getValue().getPosition();
			payload.setX(pos.x);
			payload.setY(pos.y);
			payload.setMessage(containerSet.getValue().getName());
			sendToSpecificClient(payload, id);
		}
	}
	public synchronized void syncMove() {
		for(var containerSet : pc.players.entrySet()) {
			Payload payload = new Payload();
			payload.setId(containerSet.getKey());
			payload.setPayloadType(PayloadType.MOVE_SYNC);
			Point pos = containerSet.getValue().getPosition();
			payload.setX(pos.x);
			payload.setY(pos.y);
			broadcast(payload);
		}
	}
	public synchronized void updateDirection(Payload p) {
	
		if( pc.updatePlayers(p.getId(), p.getPayloadType(), p.getX(), p.getY())) {
			broadcast(p);
		}
	}
	public synchronized void removePlayer(int id) {
		pc.removePlayer(id);
		Payload p = new Payload();
		p.setPayloadType(PayloadType.DISCONNECT);
		p.setId(id);
		broadcast(p);
	}
	public synchronized void addPlayer(Payload payload) {
		Player p = new Player(payload.getMessage());
		payload.setPayloadType(PayloadType.CONNECT);
		p.setDirection(0,0);
		p.setPosition(200, 200);
		p.setID(payload.getId());
		pc.addPlayer(payload.getId(), p);
		payload.setX(200);
		payload.setY(200);
		Payload self = new Payload();
		self.setId(payload.getId());
		self.setX(payload.getX());
		self.setY(payload.getY());
		self.setMessage(payload.getMessage());
		self.setPayloadType(PayloadType.SELF);
		sendToSpecificClient(self, self.getId());
		broadcast(payload);
		//sync player
		syncPlayers(self.getId());
	}
	private void start(int port) {
		this.port = port;
		startServerGameLoop();
		
		System.out.println("Waiting for client");
		try (ServerSocket serverSocket = new ServerSocket(port);) {
			while(SocketServer.isRunning) {
				try {
					Socket client = serverSocket.accept();
					System.out.println("Client connecting...");
					//Server thread is the server's representation of the client
					ServerThread thread = new ServerThread(client, this);
					thread.start();
					thread.setClientId(getNextId());
					//add client thread to list of clients
					clients.add(thread);
					System.out.println("Client added to clients pool");
				}
				catch(IOException e) {
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
	
	void startServerGameLoop() {
		System.out.println("Preparing Queue Reader");
		Thread gameLoop = new Thread() {
			long counter = 0;
			@Override
			public void run() {
				while(isRunning) {
					pc.movePlayers();
					counter++;
					//every 5th frame sync movement to clients
					if(counter % 5 == 0) {
						syncMove();
					}
					if(counter < 0) {//in case we overflow
						counter = 0;
					}
					try {
						Thread.sleep(16);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		gameLoop.start();
		System.out.println("Started Server Gameloop");
	}
	public synchronized void sendToSpecificClient(Payload payload, int id) {
		//updating payload type here
		//payload.setPayloadType(PayloadType.SELF);
		System.out.println("Sending message to client " + id);
		Iterator<ServerThread> iter = clients.iterator();
		while(iter.hasNext()) {
			ServerThread client = iter.next();
			if(client.getClientId() == id) {
				boolean messageSent = client.send(payload);
				if(!messageSent) {
					//if we got false, due to update of send()
					//we can assume the client lost connection
					//so let's clean it up
					iter.remove();
					removePlayer(client.getClientId());
					System.out.println("Removed client " + client.getClientId());
				}
				break;//break loop since we did what we needed
			}
		}
	}
	public synchronized void broadcast(Payload payload, String name) {
		String msg = payload.getMessage();
		payload.setMessage(
				//prepending client name to front of message
				(name!=null?name:"[Name Error]") 
				//including original message if not null (with a prepended colon)
				+ (msg != null?": "+ msg:"")
		);
		broadcast(payload);
	}
	public synchronized void broadcast(Payload payload) {
		//Commented out because it gets spammy
		//System.out.println("Sending message to " + clients.size() + " clients");
		
		Iterator<ServerThread> iter = clients.iterator();
		while(iter.hasNext()) {
			ServerThread client = iter.next();
			boolean messageSent = client.send(payload);
			if(!messageSent) {
				//if we got false, due to update of send()
				//we can assume the client lost connection
				//so let's clean it up
				iter.remove();
				removePlayer(client.getClientId());
				System.out.println("Removed client " + client.getClientId());
			}
		}
	}
	public static void main(String[] args) {
		//let's allow port to be passed as a command line arg
		//in eclipse you can set this via "Run Configurations" 
		//	-> "Arguments" -> type the port in the text box -> Apply
		int port = 3001;//make some default
		if(args.length >= 1) {
			String arg = args[0];
			try {
				port = Integer.parseInt(arg);
			}
			catch(Exception e) {
				//ignore this, we know it was a parsing issue
			}
		}
		System.out.println("Starting Server");
		SocketServer server = new SocketServer();
		System.out.println("Listening on port " + port);
		server.start(port);
		System.out.println("Server Stopped");
	}
}
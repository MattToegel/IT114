import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

public class SampleSocketServer{
	int port = -1;
	//private Thread clientListenThread = null;
	List<ServerThread> clients = new ArrayList<ServerThread>();
	HashMap<String, String[]> moves = new HashMap<String,String[]>();
	public static boolean isRunning = true;
	public SampleSocketServer() {
		isRunning = true;
	}
	/***
	 * Send the same payload to all connected clients.
	 * @param payload
	 */
	public synchronized void broadcast(Payload payload) {
		//iterate through all clients and attempt to send the message to each
		System.out.println("Sending message to " + clients.size() + " clients");
		//TODO ensure closed clients are removed from the list
		for(int i = 0; i < clients.size(); i++) {
			clients.get(i).send(payload);
		}
	}
	public void removeClient(ServerThread client) {
		Iterator<ServerThread> it = clients.iterator();
		while(it.hasNext()) {
			ServerThread s = it.next();
			if(s == client) {
				System.out.println("Matched client");
				it.remove();
			}
			
		}
	}
	void cleanupClients() {
		if(clients.size() == 0) {
			//we don't need to loop or spam if we don't have clients
			return;
		}
		//use an iterator here so we can remove elements mid loop/iteration
		Iterator<ServerThread> it = clients.iterator();
		System.out.println("Start Cleanup count " + clients.size());
		while(it.hasNext()) {
			ServerThread s = it.next();
			if(s.isClosed()) {
				//payload should have some value to tell all "other" clients which client disconnected
				//so they can clean up any local tracking/refs or show some sort of feedback
				broadcast(new Payload(PayloadType.DISCONNECT, null));
				s.stopThread();
				it.remove();
			}
		}
		System.out.println("End Cleanup count " + clients.size());
	}
	/***
	 * Send a payload to a client based on index (basically in order of connection)
	 * @param index
	 * @param payload
	 */
	public synchronized void sendToClientByIndex(int index, Payload payload) {
		//TODO validate index is in bounds
		clients.get(index).send(payload);
	}
	/***
	 * Send a payload to a client based on a value defined in ServerThread
	 * @param name
	 * @param payload
	 */
	public synchronized void sendToClientByName(String name, Payload payload) {
		for(int i = 0; i < clients.size(); i++) {
			if(clients.get(i).getClientName().equals(name)) {
				clients.get(i).send(payload);
				break;//jump out of loop
			}
		}
	}
	/***
	 * Separate thread to periodically check to clean up clients that may have been disconnected
	 */
	void runCleanupThread() {
		Thread cleanupThread = new Thread() {
			@Override
			public void run() {
				while(SampleSocketServer.isRunning) {
					cleanupClients();
					try {
						Thread.sleep(1000*30);//30 seconds
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("Cleanup thread exited");
			}
		};
		cleanupThread.start();
	}
	private void start(int port) {
		this.port = port;
		System.out.println("Waiting for client");
		runCleanupThread();
		try(ServerSocket serverSocket = new ServerSocket(port);){
			while(SampleSocketServer.isRunning) {
				try {
					Socket client = serverSocket.accept();
					System.out.println("Client connected");
					ServerThread thread = new ServerThread(client, 
							"Client[" + clients.size() + "]",
							this);
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
	void processMatch(String kvpClient, String fromClient, Entry<String, String[]> kvp) {
		String[] choices = kvp.getValue();
		Payload p;
		if(choices[0] == "rock" && choices[1] == "scissors") {
			//0 beats 1
			p = new Payload(PayloadType.MESSAGE, 
					kvpClient + " beat " + fromClient + "[" + choices[0] + " - " + choices[1] + "]");
			broadcast(p);
		}
		else if(choices[0] == "rock" && choices[1] == "paper") {
			//1 beats 0
			p = new Payload(PayloadType.MESSAGE, 
					kvpClient + " loses to " + fromClient + "[" + choices[0] + " - " + choices[1] + "]");
			broadcast(p);
		}
		else if(choices[0] == "rock" && choices[1] == "rock") {
			//tie
			p = new Payload(PayloadType.MESSAGE, 
					kvpClient + " ties " + fromClient + "[" + choices[0] + " - " + choices[1] + "]");
			broadcast(p);
		}
		else if(choices[0] == "paper" && choices[1] == "scissors") {
			p = new Payload(PayloadType.MESSAGE, 
					kvpClient + " loses to " + fromClient + "[" + choices[0] + " - " + choices[1] + "]");
			broadcast(p);
		}
		else if(choices[0] == "paper" && choices[1] == "paper") {
			p = new Payload(PayloadType.MESSAGE, 
					kvpClient + " ties " + fromClient + "[" + choices[0] + " - " + choices[1] + "]");
			broadcast(p);
		}
		else if(choices[0] == "paper" && choices[1] == "rock") {
			p = new Payload(PayloadType.MESSAGE, 
					kvpClient + " beat " + fromClient + "[" + choices[0] + " - " + choices[1] + "]");
			broadcast(p);
		}
		else if(choices[0] == "scissors" && choices[1] == "scissors") {
			p = new Payload(PayloadType.MESSAGE, 
					kvpClient + " ties " + fromClient + "[" + choices[0] + " - " + choices[1] + "]");
			broadcast(p);
		}
		else if(choices[0] == "scissors" && choices[1] == "paper") {
			p = new Payload(PayloadType.MESSAGE, 
					kvpClient + " loses to " + fromClient + "[" + choices[0] + " - " + choices[1] + "]");
			broadcast(p);
		}
		else if(choices[0] == "scissors" && choices[1] == "rock") {
			p = new Payload(PayloadType.MESSAGE, 
					kvpClient + " beat " + fromClient + "[" + choices[0] + " - " + choices[1] + "]");
			broadcast(p);
		}
	}
	public void HandleChoice(String clientName, String choice) {
		for(Entry<String, String[]> kvp : moves.entrySet()) {
			if(kvp.getValue().length == 1 
					&& !kvp.getKey().equals(clientName)) {
				String[] v = kvp.getValue();
				v[1] = choice;
				processMatch(kvp.getKey(), clientName,kvp);
			}
			else if(kvp.getValue().length == 0 
					|| kvp.getValue().length == 2 && kvp.getKey().equals(clientName)) {
				kvp.setValue(new String[] {choice,""});
			}
		}
	}
	public static void main(String[] arg) {
		System.out.println("Starting Server");
		SampleSocketServer server = new SampleSocketServer();
		int port = -1;//port should be coming from command line arguments
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
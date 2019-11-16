import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class SampleSocketServer{
	int port = -1;
	static int clientId = 0;
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
	public synchronized void broadcast(Payload payload, String except) {
		//iterate through all clients and attempt to send the message to each
		System.out.println("Sending message to " + clients.size() + " clients");
		//TODO ensure closed clients are removed from the list
		for(int i = 0; i < clients.size(); i++) {
			if(except != null && clients.get(i).getClientName().equals(except)) {
				continue;
			}
			clients.get(i).send(payload);
		}
	}
	public synchronized void broadcast(Payload payload) {
		broadcast(payload, null);
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
			//we don't need to iterate or spam if we don't have clients
			return;
		}
		//use an iterator here so we can remove elements mid loop/iteration
		Iterator<ServerThread> it = clients.iterator();
		int start = clients.size();
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
		int diff = start - clients.size();
		if(diff != 0) {
			System.out.println("Cleaned up " + diff + " clients");
		}
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
							"id_" + clientId,
							this);
					thread.start();//start client thread
					clients.add(thread);//add to client pool
					clientId++;
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
	/***
	 * Determines winner of the two choices
	 * @param choice1
	 * @param choice2
	 * @return
	 * 0 = tie, 1 = win, -1 = lose
	 */
	int checkMatch(String choice1, String choice2) {
		List<String> c = Arrays.asList("rock", "paper", "scissors");
		int a = c.indexOf(choice1);
		int b = c.indexOf(choice2);
		//math to find result without having all permutations
		//see: https://stackoverflow.com/questions/11377117/rock-paper-scissors-determine-win-loss-tie-using-math
		if(a==b) {
			//tie
			return 0;
		}
		if((a - b + 3) % 3 == 1) {
			//win
			return 1;
		}
		else {
			//lose
			return -1;
		}
	}
	void processMatch(String kvpClient, String fromClient, Entry<String, String[]> kvp) {
		String[] choices = kvp.getValue();
		System.out.println("Checking " + choices[0] + " with " + choices[1]);
		//converts the String choices to a -1, 0, 1 choice to define a winner, loser, tie
		int result = checkMatch(choices[0], choices[1]);
		String message = "";
		if(result == 0) {
			message = kvpClient + " ties " + fromClient + "[" + choices[0] + " - " + choices[1] + "]";
		}
		else if (result == 1) {
			message = kvpClient + " beat " + fromClient + "[" + choices[0] + " - " + choices[1] + "]";
		}
		else {
			message = kvpClient + " loses to " + fromClient + "[" + choices[0] + " - " + choices[1] + "]";
		}
		//TODO Send the resulting message to all clients
		System.out.println("processMatch(): " + message);
		broadcast(new Payload(PayloadType.MESSAGE, message));
	}
	public void HandleChoice(String clientName, String choice) {
		System.out.println("Handling choice " + choice + " from " + clientName);
		boolean foundAGame = false;
		//loop through list of player moves
		//find a game to apply choice to
		for(Entry<String, String[]> kvp : moves.entrySet()) {
			if(!kvp.getKey().equals(clientName)) {
				String[] value = kvp.getValue();
				//checks if second move is available
				//if so, then we use that slot and calculate the winner
				if("".equals(value[1])) {
					foundAGame = true;
					value[1] = choice;
					kvp.setValue(value);
					System.out.println(clientName + " completed game with " + kvp.getKey() + " processing results");
					processMatch(kvp.getKey(), clientName, kvp);
				}
				break;
			}
		}
		//otherwise create/restart our game, or wait for opponent
		if(!foundAGame) {
			String[] myGame = moves.get(clientName);
			if(myGame == null || !"".equals(myGame[1])) {
				System.out.println("Creating or resetting game for " + clientName);
				moves.put(clientName, new String[] {choice, ""});
			}
			else {
				System.out.println(clientName + " waiting for opponent");
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
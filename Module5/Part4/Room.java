package Module5.Part4;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Room {
	private Server server;// used to refer to accessible server functions
	private String name;
	private List<ServerThread> clients = new ArrayList<ServerThread>();
	private boolean isRunning = false;

	public Room(String name, Server server) {
		this.name = name;
		this.server = server;
		isRunning = true;
	}

	private void info(String message) {
		System.out.println(String.format("Room[%s]: %s", name, message));
	}
	
	public String getRoomName() {
		return name;
	}

	protected synchronized void addClient(ServerThread client) {
		if(!isRunning){
			return;
		}
		client.setCurrentRoom(this);
		if (clients.indexOf(client) > -1) {
			info("Attempting to add a client that already exists");
		} else {
			clients.add(client);
			new Thread() {
				@Override
				public void run() {
					//slight delay to let potentially new client to finish
					//binding input/output streams
					//comment out the Thread.sleep to see what happens
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					sendMessage(client, "joined the room " + getRoomName());
				}
			}.start();
			
		}
	}

	protected synchronized void removeClient(ServerThread client) {
		if(!isRunning){
			return;
		}
		clients.remove(client);
		// we don't need to broadcast it to the server
		// only to our own Room
		if(clients.size() > 0){
			sendMessage(client, "left the room");
		}
		checkClients();
	}
	/***
	 * Checks the number of clients.
	 * If zero, begins the cleanup process to dispose of the room
	 */
	private void checkClients(){
		//Cleanup if room is empty and not lobby
		if(!name.equalsIgnoreCase("lobby") && clients.size() == 0){
			cleanup();
		}
	}
	/***
	 * Helper function to process messages to trigger different functionality.
	 * 
	 * @param message The original message being sent
	 * @param client  The sender of the message (since they'll be the ones
	 *                triggering the actions)
	 */
	private boolean processCommands(String message, ServerThread client) {
		boolean wasCommand = false;
		try {
			if (message.startsWith("/")) {
				String[] comm = message.split("/");
				String part1 = comm[1];
				String[] comm2 = part1.split(" ");
				String command = comm2[0];
				String roomName;
				switch (command) {
					case "createroom":
						roomName = comm2[1];
						if (server.createNewRoom(roomName)) {
							server.joinRoom(roomName, client);
						} else {
							client.send(String.format("Room %s already exists", roomName));
						}
						wasCommand = true;
						break;
					case "joinroom":
						roomName = comm2[1];
						if (!server.joinRoom(roomName, client)) {
							client.send(String.format("Room %s doesn't exist", roomName));
						}
						wasCommand = true;
						break;
					case "disconnect":
					case "logout":
					case "logoff":
						//client.setCurrentRoom(null);
						client.disconnect();
						removeClient(client);
						wasCommand = true;
						break;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wasCommand;
	}

	/***
	 * Takes a sender and a message and broadcasts the message to all clients in
	 * this room. Client is mostly passed for command purposes but we can also use
	 * it to extract other client info.
	 * 
	 * @param sender  The client sending the message
	 * @param message The message to broadcast inside the room
	 */
	protected synchronized void sendMessage(ServerThread sender, String message) {
		if(!isRunning){
			return;
		}
		info("Sending message to " + clients.size() + " clients");
		if (sender != null && processCommands(message, sender)) {
			// it was a command, don't broadcast
			return;
		}
		Iterator<ServerThread> iter = clients.iterator();
		message = String.format("User[%s]: %s",
				sender == null ? "Room" : sender.getName(),
				message);
		while (iter.hasNext()) {
			ServerThread client = iter.next();
			boolean messageSent = client.send(message);
			if (!messageSent) {
				iter.remove();
				info("Removed client " + client.getName());
				checkClients();
				sendMessage(null, client.getName() + " disconnected");
			}
		}
	}
	protected void cleanup(){
		server.removeRoom(this);
		server = null;
		isRunning = false;
		clients = null;
	}
}

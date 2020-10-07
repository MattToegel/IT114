import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import utils.Debug;

public class SocketServer {
	int port = 3000;
	public static boolean isRunning = false;
	private List<Room> rooms = new ArrayList<Room>();
	private Room lobby;// here for convenience

	private void start(int port) {
		this.port = port;
		Debug.log("Waiting for client");
		try (ServerSocket serverSocket = new ServerSocket(port);) {
			isRunning = true;
			// create a lobby on start
			lobby = new Room("Lobby", this);
			rooms.add(lobby);
			while (SocketServer.isRunning) {
				try {
					Socket client = serverSocket.accept();
					Debug.log("Client connecting...");
					// Server thread is the server's representation of the client
					ServerThread thread = new ServerThread(client, lobby);
					thread.start();
					// add client thread to our room's list of clients
					lobby.addClient(thread);
					Debug.log("Client added to clients pool");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				isRunning = false;
				// Thread.sleep(50);
				Debug.log("closing server socket");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected Room getLobby() {
		return lobby;
	}

	/***
	 * Helper function to check if room exists by case insensitive name
	 * 
	 * @param roomName The name of the room to look for
	 * @return matched Room or null if not found
	 */
	private Room getRoom(String roomName) {
		for (int i = 0, l = rooms.size(); i < l; i++) {
			if (rooms.get(i).getName().equalsIgnoreCase(roomName)) {
				return rooms.get(i);
			}
		}
		return null;
	}

	/***
	 * Attempts to join a room by name. Will remove client from old room and add
	 * them to the new room.
	 * 
	 * @param roomName The desired room to join
	 * @param client   The client moving rooms
	 * @return true if reassign worked; false if new room doesn't exist
	 */
	protected synchronized boolean joinRoom(String roomName, ServerThread client) {
		Room newRoom = getRoom(roomName);
		Room oldRoom = client.getCurrentRoom();
		if (newRoom != null) {
			if (oldRoom != null) {
				Debug.log(client.getName() + " leaving room " + oldRoom.getName());
				oldRoom.removeClient(client);
			}
			Debug.log(client.getName() + " joining room " + newRoom.getName());
			newRoom.addClient(client);
			return true;
		}
		return false;
	}

	/***
	 * Attempts to create a room with given name if it doesn't exist already.
	 * 
	 * @param roomName The desired room to create
	 * @return true if it was created and false if it exists
	 */
	protected synchronized boolean createNewRoom(String roomName) {
		if (getRoom(roomName) != null) {
			// TODO can't create room
			Debug.log("Room already exists");
			return false;
		} else {
			Room room = new Room(roomName, this);
			rooms.add(room);
			Debug.log("Created new room: " + roomName);
			return true;
		}
	}

	public static void main(String[] args) {
		// let's allow port to be passed as a command line arg
		// in eclipse you can set this via "Run Configurations"
		// -> "Arguments" -> type the port in the text box -> Apply
		int port = -1;// make some default
		if (args.length >= 1) {
			String arg = args[0];
			try {

				port = Integer.parseInt(arg);
			} catch (Exception e) {
				// ignore this, we know it was a parsing issue
			}
		}
		if (port > -1) {
			Debug.log("Starting Server");
			SocketServer server = new SocketServer();
			Debug.log("Listening on port " + port);
			server.start(port);
			Debug.log("Server Stopped");
		}
	}
}
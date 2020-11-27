package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketServer {
    int port = 3000;
    public static boolean isRunning = false;
    private List<Room> rooms = new ArrayList<Room>();
    private Room lobby;// here for convenience
    private List<Room> isolatedPrelobbies = new ArrayList<Room>();
    private final static String PRELOBBY = "PreLobby";
    protected final static String LOBBY = "Lobby";
    private final static Logger log = Logger.getLogger(SocketServer.class.getName());

    private void start(int port) {
	this.port = port;
	log.log(Level.INFO, "Waiting for client");
	try (ServerSocket serverSocket = new ServerSocket(port);) {
	    isRunning = true;
	    // create a lobby on start
	    Room.setServer(this);
	    lobby = new Room(LOBBY);// , this);
	    rooms.add(lobby);
	    while (SocketServer.isRunning) {
		try {
		    Socket client = serverSocket.accept();
		    log.log(Level.INFO, "Client connecting...");
		    // Server thread is the server's representation of the client
		    ServerThread thread = new ServerThread(client, lobby);
		    thread.start();
		    // create a dummy room until we get further client details
		    // technically once a user fully joins this lobby will be destroyed
		    // but we'll track it in an array so we can attempt to clean it up just in case
		    Room prelobby = new Room(PRELOBBY, true);// , this);
		    prelobby.addClient(thread);
		    isolatedPrelobbies.add(prelobby);

		    log.log(Level.INFO, "Client added to clients pool");
		}
		catch (IOException e) {
		    e.printStackTrace();
		}
	    }

	}
	catch (IOException e) {
	    e.printStackTrace();
	}
	finally {
	    try {
		isRunning = false;
		cleanup();
		log.log(Level.INFO, "closing server socket");
	    }
	    catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    protected void cleanupRoom(Room r) {
	Iterator<Room> iter = isolatedPrelobbies.iterator();
	while (iter.hasNext()) {
	    Room check = iter.next();
	    if (check.equals(r)) {
		iter.remove();
		log.log(Level.INFO, "Removed " + check.getName() + " from prelobbies");
		break;
	    }
	}
    }

    private void cleanup() {
	Iterator<Room> iter = this.rooms.iterator();
	while (iter.hasNext()) {
	    Room r = iter.next();
	    try {
		r.close();
		iter.remove();
	    }
	    catch (Exception e) {
		// it's ok to ignore this one
	    }
	}
	Iterator<Room> pl = isolatedPrelobbies.iterator();
	while (pl.hasNext()) {
	    Room r = pl.next();
	    try {
		r.close();
		pl.remove();
	    }
	    catch (Exception e) {
		// it's ok to ignore this one
	    }
	}
	try {
	    lobby.close();
	    log.log(Level.WARNING, "Lobby closed");
	}
	catch (Exception e) {
	    // ok to ignore this too
	}
    }

    protected Room getLobby() {
	return lobby;
    }

    protected List<String> getRooms(String room) {
	// not the most efficient way to do it, but it works
	List<String> roomNames = new ArrayList<String>();
	Iterator<Room> iter = rooms.iterator();
	// part 2, limit returned rooms
	int i = 0;
	int max = 10;// lets get up to 10 rooms
	while (iter.hasNext()) {
	    Room r = iter.next();
	    // Part 2 added room name filter for searches
	    if ((r != null && r.getName() != null)
		    && (room == null || (room != null && r.getName().toLowerCase().contains(room.toLowerCase())))) {
		roomNames.add(r.getName());
		i++;
	    }
	    if (i > max) {
		break;
	    }

	}
	return roomNames;
    }

    /***
     * Special helper to join the lobby and close the previous room client was in if
     * it's marked as Prelobby. Mostly used for prelobby once the server receives
     * more client details.
     * 
     * @param client
     */
    protected void joinLobby(ServerThread client) {
	Room prelobby = client.getCurrentRoom();
	if (joinRoom(LOBBY, client)) {
	    if (prelobby != null) {
		prelobby.removeClient(client);
		log.log(Level.INFO, "Added " + client.getClientName() + " to Lobby; Prelobby should self destruct");
	    }
	    else {
		log.log(Level.WARNING, "Prelobby was null for " + client.getClientName());
	    }
	}
	else {
	    log.log(Level.INFO, "Problem moving " + client.getClientName() + " to lobby");
	}
    }

    /***
     * Helper function to check if room exists by case insensitive name
     * 
     * @param roomName The name of the room to look for
     * @return matched Room or null if not found
     */
    private Room getRoom(String roomName) {
	Iterator<Room> iter = rooms.iterator();
	while (iter.hasNext()) {
	    Room r = iter.next();
	    if (r != null && r.getName() != null && r.getName().equalsIgnoreCase(roomName)) {
		return r;
	    }
	}
	/*
	 * for (int i = 0, l = rooms.size(); i < l; i++) { Room r = rooms.get(i); if (r
	 * == null || r.getName() == null) { continue; } if
	 * (r.getName().equalsIgnoreCase(roomName)) { return r; } }
	 */
	log.log(Level.WARNING, "Error getting room " + roomName);
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
	if (roomName == null || roomName.equalsIgnoreCase(PRELOBBY)) {
	    log.log(Level.WARNING, "Room is either null or " + PRELOBBY);
	    return false;
	}
	Room newRoom = getRoom(roomName);
	Room oldRoom = client.getCurrentRoom();
	if (newRoom != null) {
	    if (oldRoom != null) {
		log.log(Level.INFO, client.getClientName() + " leaving room " + oldRoom.getName());
		oldRoom.removeClient(client);
	    }
	    else {
		log.log(Level.WARNING, "old room is null for " + client.getClientName());
	    }
	    log.log(Level.INFO, client.getClientName() + " joining room " + newRoom.getName());
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
	if (roomName == null || roomName.equalsIgnoreCase(PRELOBBY)) {
	    return false;
	}
	if (getRoom(roomName) != null) {
	    // TODO can't create room
	    log.log(Level.INFO, "Room already exists");
	    return false;
	}
	else {
	    Room room = new Room(roomName);// , this);
	    rooms.add(room);
	    log.log(Level.INFO, "Created new room: " + roomName);
	    return true;
	}
    }

    public static void main(String[] args) {
	// let's allow port to be passed as a command line arg
	// in eclipse you can set this via "Run Configurations"
	// -> "Arguments" -> type the port in the text box -> Apply
	int port = -1;
	try {
	    port = Integer.parseInt(args[0]);
	}
	catch (Exception e) {
	    // ignore this, we know it was a parsing issue
	}
	if (port > -1) {
	    log.log(Level.INFO, "Starting Server");
	    SocketServer server = new SocketServer();
	    log.log(Level.INFO, "Listening on port " + port);
	    server.start(port);
	    log.log(Level.INFO, "Server Stopped");
	}
    }
}
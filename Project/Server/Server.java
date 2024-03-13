package Project.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import Project.Common.Constants;
import Project.Common.TextFX;
import Project.Common.TextFX.Color;

import java.util.List;

public enum Server {
    INSTANCE;

    int port = 3001;
    private List<Room> rooms = new ArrayList<Room>();
    private Room lobby = null;// default room
    private Queue<ServerThread> incomingClients = new LinkedList<ServerThread>();
    private boolean isRunning = true;
    private long nextClientId = 1;// <-- uniquely identifies clients (could use a UUID but we're keeping it basic)
    private void start(int port) {
        this.port = port;
        // server listening
        try (ServerSocket serverSocket = new ServerSocket(port);) {
            Socket incoming_client = null;
            System.out.println("Server is listening on port " + port);
            startQueueManager();
            // create a lobby on start
            lobby = new Room(Constants.LOBBY);
            rooms.add(lobby);
            do {
                System.out.println("waiting for next client");
                if (incoming_client != null) {
                    System.out.println("Client connected");
                    ServerThread sClient = new ServerThread(incoming_client);

                    sClient.start();
                    incomingClients.add(sClient);
                    incoming_client = null;

                }
            } while ((incoming_client = serverSocket.accept()) != null);
        } catch (IOException e) {
            System.err.println("Error accepting connection");
            e.printStackTrace();
        } finally {
            System.out.println("closing server socket");
        }
    }

    private void startQueueManager() {
        System.out.println(TextFX.colorize("Starting queue manager", Color.PURPLE));
        new Thread() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                        System.err.println("Some thread problem");
                        e.printStackTrace();
                    }
                    if (incomingClients.size() > 0) {
                        ServerThread incomingClient = incomingClients.peek();
                        if (incomingClient.isRunning() && incomingClient.getClientName() != null) {
                            handleIncomingClient(incomingClient);
                            incomingClients.poll();// <-- remove the handled client
                        }
                    }
                }
                System.out.println(TextFX.colorize("Terminating queue manager", Color.PURPLE));
            }
        }.start();
    }

    private void handleIncomingClient(ServerThread incomingClient) {
        incomingClient.setClientId(nextClientId);
        // incomingClient.sendClientId(nextClientId);
        nextClientId++;
        if (nextClientId < 0) {
            nextClientId = 1; // <-- by some lucky chance it overflows, restart the count
        }
        joinRoom(Constants.LOBBY, incomingClient);
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
        Room newRoom = roomName.equalsIgnoreCase(Constants.LOBBY) ? lobby : getRoom(roomName);
        Room oldRoom = client.getCurrentRoom();
        if (newRoom != null) {
            if (oldRoom != null) {
                System.out.println(client.getName() + " leaving room " + oldRoom.getName());
                oldRoom.removeClient(client);
            }
            System.out.println(client.getName() + " joining room " + newRoom.getName());
            newRoom.addClient(client);
            return true;
        } else {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    String.format("Room %s wasn't found, please try another", roomName));
        }
        return false;
    }

    /**
     * Gets a result of rooms similar to the search string, up to 10
     * 
     * @param searchString
     * @return
     */
    protected synchronized List<String> listRooms(String searchString) {
        return listRooms(searchString, 10);
    }

    /**
     * Gets a result of rooms similar to the search string, between 1-100 results
     * 
     * @param searchString
     * @param limit
     * @return
     */
    protected synchronized List<String> listRooms(String searchString, int limit) {
        // TODO
        if (limit < 1 || limit > 100) {
            return null;
        }
        List<String> matchedRooms = new ArrayList<String>();
        Iterator<Room> iter = rooms.iterator();
        while (iter.hasNext()) {
            Room currentRoom = iter.next();
            System.out.println("Checking Room " + currentRoom.getName());
            // if we don't have a particular search, simply add the room
            if (searchString == null || searchString.isBlank()) {
                matchedRooms.add(currentRoom.getName());
            } // if we have a particular search and the room name is "like" the search string,
              // add the room
            else if (currentRoom.getName().toLowerCase().contains(searchString.toLowerCase())) {
                matchedRooms.add(currentRoom.getName());
            }
            if (matchedRooms.size() >= limit) {
                break;
            }
        }

        return matchedRooms;
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
            System.out.println(String.format("Room %s already exists", roomName));
            return false;
        } else {
            Room room = new Room(roomName);
            rooms.add(room);
            System.out.println("Created new room: " + roomName);
            return true;
        }
    }

    protected synchronized void removeRoom(Room r) {
        if (rooms.removeIf(room -> room == r)) {
            System.out.println("Removed empty room " + r.getName());
        }
    }

    protected synchronized void broadcast(String message) {
        if (processCommand(message)) {

            return;
        }
        // loop over rooms and send out the message
        Iterator<Room> it = rooms.iterator();
        while (it.hasNext()) {
            Room room = it.next();
            if (room != null) {
                room.sendMessage(null, message);
            }
        }
    }

    private boolean processCommand(String message) {
        System.out.println("Checking command: " + message);
        // TODO
        return false;
    }

    public static void main(String[] args) {
        System.out.println("Starting Server");
        Server server = Server.INSTANCE;// new Server();
        int port = 3000;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // can ignore, will either be index out of bounds or type mismatch
            // will default to the defined value prior to the try/catch
        }
        server.start(port);
        System.out.println("Server Stopped");
    }
}
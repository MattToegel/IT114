package Module6.Part8.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public enum Server {
    INSTANCE;
    int port = 3001;
    // connected clients
    // private List<ServerThread> clients = new ArrayList<ServerThread>();
    private List<Room> rooms = new ArrayList<Room>();
    private Room lobby = null;// default room
    private long nextClientId = 1;

    private Queue<ServerThread> incomingClients = new LinkedList<ServerThread>();
    // https://www.geeksforgeeks.org/killing-threads-in-java/
    private volatile boolean isRunning = false;

    private void start(int port) {
        this.port = port;
        // server listening
        try (ServerSocket serverSocket = new ServerSocket(port);) {
            Socket incoming_client = null;
            System.out.println("Server is listening on port " + port);
            isRunning = true;
            startQueueManager();
            // create a lobby on start
            lobby = new Room("Lobby");
            rooms.add(lobby);
            do {
                System.out.println("waiting for next client");
                if (incoming_client != null) {
                    System.out.println("Client connected");
                    ServerThread sClient = new ServerThread(incoming_client, lobby);
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

    void startQueueManager() {
        // Queue manager thread to wait for the ServerThread thread to start
        // before officially handing them off to a room and opening them for
        // communication
        new Thread() {
            @Override
            public void run() {
                // slight delay to let potentially new client to finish
                // binding input/output streams
                // comment out the Thread.sleep to see what happens
                while (isRunning) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (incomingClients.size() > 0) {
                        ServerThread ic = incomingClients.peek();
                        if (ic != null) {
                            //wait for the thread to start and for the client to send the client name (username)
                            if (ic.isRunning() && ic.getClientName() != null) {
                                handleIncomingClient(ic);
                                incomingClients.poll();
                            }
                        }
                    }
                }
            }
        }.start();
    }

    void handleIncomingClient(ServerThread client) {
        client.setClientId(nextClientId);// server reference
        client.sendClientId(nextClientId);// client reference
        nextClientId++;
        if (nextClientId < 0) {// will use overflow to reset our counter
            nextClientId = 1;
        }
        joinRoom("lobby", client);
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
        Room newRoom = roomName.equalsIgnoreCase("lobby") ? lobby : getRoom(roomName);
        Room oldRoom = client.getCurrentRoom();
        if (newRoom != null) {
            if (oldRoom != null && oldRoom != newRoom) {
                System.out.println(client.getName() + " leaving room " + oldRoom.getName());
                oldRoom.removeClient(client);
                client.sendResetUserList();
            }
            System.out.println(client.getName() + " joining room " + newRoom.getName());
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
            System.out.println(String.format("Room %s already exists", roomName));
            return false;
        } else {
            Room room = new Room(roomName);
            rooms.add(room);
            System.out.println("Created new room: " + roomName);
            return true;
        }
    }
    /**
     * Returns Rooms with names having a partial match with query.
     * Hard coded to a limit of 10.
     * @param query
     * @return
     */
    protected synchronized List<String> getRooms(String query){
        return getRooms(query, 10);
    }
    /**
     * Returns Rooms with names having a partial match with query.
     * @param query
     * @param limit The maximum records to return
     * @return
     */
    protected synchronized List<String> getRooms(String query, int limit){
        List<String> matchedRooms = new ArrayList<String>();
        synchronized(rooms){
            Iterator<Room> iter = rooms.iterator();
            while(iter.hasNext()){
                Room r = iter.next();
                if(r.isRunning() && r.getName().toLowerCase().contains(query.toLowerCase())){
                    matchedRooms.add(r.getName());
                    if(matchedRooms.size() >= limit){
                        break;
                    }
                }
            }
        }
        return matchedRooms;
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
        Server server = Server.INSTANCE;
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
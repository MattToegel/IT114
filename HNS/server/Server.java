package HNS.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import HNS.common.Constants;

public enum Server {
    INSTANCE;

    int port = 3000;
    private static Logger logger = Logger.getLogger(Server.class.getName());
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
            logger.info(String.format("Server is listening on port %s", port));
            isRunning = true;
            Room.server = this;
            startQueueManager();
            // create a lobby on start
            lobby = new Room(Constants.LOBBY);
            rooms.add(lobby);
            do {
                logger.info("Waiting for next client");
                if (incoming_client != null) {
                    logger.info("Client connected");
                    ServerThread sClient = new ServerThread(incoming_client, lobby);
                    sClient.start();
                    incomingClients.add(sClient);
                    incoming_client = null;

                }
            } while ((incoming_client = serverSocket.accept()) != null);
        } catch (IOException e) {
            logger.severe("Error accepting connection");
            e.printStackTrace();
        } finally {
            logger.info("Closing Server Socket");
        }
    }

    void startQueueManager() {
        // Queue manager thread to wait for the ServerThread thread to start
        // before officially handing them off to a room and opening them for
        // communication
        new Thread() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (incomingClients.size() > 0) {
                        ServerThread ic = incomingClients.peek();
                        if (ic != null) {
                            // wait for the thread to start and for the client to send the client name
                            // (username)
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
        joinRoom(Constants.LOBBY, client);
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
        if (newRoom != null && roomName != null) {
            if (oldRoom != null && oldRoom != newRoom) {
                logger.info(String.format("Client %s leaving old room %s", client.getClientName(), oldRoom.getName()));
                oldRoom.removeClient(client);
            }
            logger.info(String.format("Client %s joining new room %s", client.getClientName(), newRoom.getName()));
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
            // TODO Room exists; can't create room
            logger.info(String.format("Room %s already exists", roomName));
            return false;
        } else {
            // TODO, all non-lobby rooms will be games
            // Room room = new Room(roomName); //chatroom project can just use regular rooms
            GameRoom room = new GameRoom(roomName); // all other projects
            rooms.add(room);
            logger.info(String.format("Created new room %s", roomName));
            return true;
        }
    }

    /**
     * Returns Rooms with names having a partial match with query.
     * Hard coded to a limit of 10.
     * 
     * @param query
     * @return
     */
    protected synchronized List<String> getRooms(String query) {
        return getRooms(query, 10);
    }

    /**
     * Returns Rooms with names having a partial match with query.
     * 
     * @param query
     * @param limit The maximum records to return
     * @return
     */
    protected synchronized List<String> getRooms(String query, int limit) {
        List<String> matchedRooms = new ArrayList<String>();
        synchronized (rooms) {
            Iterator<Room> iter = rooms.iterator();
            while (iter.hasNext()) {
                Room r = iter.next();
                if (r.isRunning() && r.getName().toLowerCase().contains(query.toLowerCase())) {
                    matchedRooms.add(r.getName());
                    if (matchedRooms.size() >= limit) {
                        break;
                    }
                }
            }
        }
        return matchedRooms;
    }

    protected synchronized void removeRoom(Room r) {
        if (rooms.removeIf(room -> room == r)) {
            logger.info(String.format("Removed empty room %s", r.getName()));
        }
    }

    /**
     * Send message to all rooms
     * 
     * @param message
     */
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
        Server.logger.info("Starting server");
        int port = Server.INSTANCE.port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // can ignore, will either be index out of bounds or type mismatch
            // will default to the defined value prior to the try/catch
        }
        Server.INSTANCE.start(port);
        Server.logger.info("Server stopped");
    }
}

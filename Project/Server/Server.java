package Project.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import Project.Common.LoggerUtil;

public enum Server {
    INSTANCE;

    {
        // statically initialize the server-side LoggerUtil
        LoggerUtil.LoggerConfig config = new LoggerUtil.LoggerConfig();
        config.setFileSizeLimit(2048 * 1024); // 2MB
        config.setFileCount(1);
        config.setLogLocation("server.log");
        // Set the logger configuration
        LoggerUtil.INSTANCE.setConfig(config);
    }
    private int port = 3000;
    // Use ConcurrentHashMap for thread-safe room management
    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private boolean isRunning = true;
    private long nextClientId = 1;

    private Server() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LoggerUtil.INSTANCE.info("JVM is shutting down. Perform cleanup tasks.");
            shutdown();
        }));
    }

    private void start(int port) {
        this.port = port;
        // server listening
        LoggerUtil.INSTANCE.info("Listening on port " + this.port);
        // Simplified client connection loop
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            createRoom(Room.LOBBY);// create the first room
            while (isRunning) {
                LoggerUtil.INSTANCE.info("Waiting for next client");
                Socket incomingClient = serverSocket.accept(); // blocking action, waits for a client connection
                LoggerUtil.INSTANCE.info("Client connected");
                // wrap socket in a ServerThread, pass a callback to notify the Server they're
                // initialized
                ServerThread sClient = new ServerThread(incomingClient, this::onClientInitialized);
                // start the thread (typically an external entity manages the lifecycle and we
                // don't have the thread start itself)
                sClient.start();
            }
        } catch (IOException e) {
            LoggerUtil.INSTANCE.severe("Error accepting connection", e);
        } finally {
            shutdown();
            LoggerUtil.INSTANCE.info("Closing server socket");
        }
    }

    /**
     * Gracefully disconnect clients
     */
    private void shutdown() {
        try {
            // chose removeIf over forEach to avoid potential
            // ConcurrentModificationException
            // since empty rooms tell the server to remove themselves
            rooms.values().removeIf(room -> {
                room.disconnectAll();
                return true;
            });
        } catch (Exception e) {
            LoggerUtil.INSTANCE.info("Error cleaning up rooms", e);
        }
    }

    /**
     * Callback passed to ServerThread to inform Server they're ready to receive
     * data
     * 
     * @param sClient
     */
    private void onClientInitialized(ServerThread sClient) {
        sClient.sendClientId(nextClientId);
        nextClientId++;
        if (nextClientId < 0) {
            nextClientId = 1;
        }
        // add to lobby room
        LoggerUtil.INSTANCE.info(String.format("Server: *%s[%s] initialized*",
                sClient.getClientName(), sClient.getClientId()));
        joinRoom(Room.LOBBY, sClient);
    }

    /**
     * Attempts to create a new Room and add it to the tracked rooms collection
     * 
     * @param name Unique name of the room
     * @return true if it was created and false if it wasn't
     */
    protected boolean createRoom(String name) {
        final String nameCheck = name.toLowerCase();
        if (rooms.containsKey(nameCheck)) {
            return false;
        }
        Room room = new Room(name);
        rooms.put(nameCheck, room);
        LoggerUtil.INSTANCE.info(String.format("Created new Room %s", name));
        return true;
    }

    /**
     * Attempts to move a client (ServerThread) between rooms
     * 
     * @param name   the target room to join
     * @param client the client moving
     * @return true if the move was successful, false otherwise
     */
    protected boolean joinRoom(String name, ServerThread client) {
        final String nameCheck = name.toLowerCase();
        if (!rooms.containsKey(nameCheck)) {
            return false;
        }
        Room current = client.getCurrentRoom();
        if (current != null) {
            current.removedClient(client);
        }
        Room next = rooms.get(nameCheck);
        next.addClient(client);
        return true;
    }

    protected List<String> listRooms(String roomQuery) {
        final String nameCheck = roomQuery.toLowerCase();
        return rooms.values().stream()
                .filter(room -> room.getName().toLowerCase().contains(nameCheck))// find partially matched rooms
                .map(room -> room.getName())// map room to String (name)
                .collect(Collectors.toList()); // return a mutable list
    }

    protected void removeRoom(Room room) {
        rooms.remove(room.getName().toLowerCase());
        LoggerUtil.INSTANCE.info(String.format("Server removed room %s", room.getName()));
    }

    public static void main(String[] args) {
        LoggerUtil.INSTANCE.info("Server Starting");
        Server server = Server.INSTANCE;
        int port = 3000;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // can ignore, will either be index out of bounds or type mismatch
            // will default to the defined value prior to the try/catch
        }
        server.start(port);
        LoggerUtil.INSTANCE.info("Server Stopped");
    }
}

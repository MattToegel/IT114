package HNS.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import HNS.common.Constants;
import HNS.common.Grid;
import HNS.common.GridPayload;
import HNS.common.Payload;
import HNS.common.PayloadType;
import HNS.common.Phase;
import HNS.common.Player;
import HNS.common.PointsPayload;
import HNS.common.PositionPayload;
import HNS.common.RoomResultPayload;

public enum Client {
    Instance;

    Socket server = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    final String ipAddressPattern = "/connect\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{3,5})";
    final String localhostPattern = "/connect\\s+(localhost:\\d{3,5})";
    boolean isRunning = false;
    private Thread inputThread;
    private Thread fromServerThread;
    private String clientName = "";
    private long myClientId = Constants.DEFAULT_CLIENT_ID;
    private boolean isSeeker = false;
    private static Logger logger = Logger.getLogger(Client.class.getName());

    // private Hashtable<Long, String> userList = new Hashtable<Long, String>();
    private ConcurrentHashMap<Long, Player> players = new ConcurrentHashMap<Long, Player>();
    private Grid grid;
    private Phase currentPhase = Phase.READY;

    public boolean isConnected() {
        if (server == null) {
            return false;
        }
        // https://stackoverflow.com/a/10241044
        // Note: these check the client's end of the socket connect; therefore they
        // don't really help determine
        // if the server had a problem
        return server.isConnected() && !server.isClosed() && !server.isInputShutdown() && !server.isOutputShutdown();

    }

    /**
     * Takes an ip address and a port to attempt a socket connection to a server.
     * 
     * @param address
     * @param port
     * @return true if connection was successful
     */
    private boolean connect(String address, int port) {
        try {
            server = new Socket(address, port);
            // channel to send to server
            out = new ObjectOutputStream(server.getOutputStream());
            // channel to listen to server
            in = new ObjectInputStream(server.getInputStream());
            logger.info("Client connected");
            listenForServerPayload();
            sendConnect();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected();
    }

    /**
     * <p>
     * Check if the string contains the <i>connect</i> command
     * followed by an ip address and port or localhost and port.
     * </p>
     * <p>
     * Example format: 123.123.123:3000
     * </p>
     * <p>
     * Example format: localhost:3000
     * </p>
     * https://www.w3schools.com/java/java_regex.asp
     * 
     * @param text
     * @return
     */
    @Deprecated // remove in Milestone3
    private boolean isConnection(String text) {
        // https://www.w3schools.com/java/java_regex.asp
        return text.matches(ipAddressPattern)
                || text.matches(localhostPattern);
    }

    @Deprecated // remove in Milestone3
    private boolean isQuit(String text) {
        return text.equalsIgnoreCase("/quit");
    }

    @Deprecated // remove in Milestone3
    private boolean isName(String text) {
        if (text.startsWith("/name")) {
            String[] parts = text.split(" ");
            if (parts.length >= 2) {
                clientName = parts[1].trim();
                System.out.println("Name set to " + clientName);
            }
            return true;
        }
        return false;
    }

    /**
     * Controller for handling various text commands from the client
     * <p>
     * Add more here as needed
     * </p>
     * 
     * @param text
     * @return true if a text was a command or triggered a command
     */
    @Deprecated // removing in Milestone3
    private boolean processClientCommand(String text) throws IOException {
        if (isConnection(text)) {
            if (clientName.isBlank()) {
                System.out.println("You must set your name before you can connect via: /name your_name");
                return true;
            }
            // replaces multiple spaces with single space
            // splits on the space after connect (gives us host and port)
            // splits on : to get host as index 0 and port as index 1
            String[] parts = text.trim().replaceAll(" +", " ").split(" ")[1].split(":");
            connect(parts[0].trim(), Integer.parseInt(parts[1].trim()));
            return true;
        } else if (isQuit(text)) {
            sendDisconnect();
            isRunning = false;
            return true;
        } else if (isName(text)) {
            return true;
        } else if (text.startsWith("/joinroom")) {
            String roomName = text.replace("/joinroom", "").trim();
            sendJoinRoom(roomName);
            return true;
        } else if (text.startsWith("/createroom")) {
            String roomName = text.replace("/createroom", "").trim();
            sendCreateRoom(roomName);
            return true;
        } else if (text.startsWith("/rooms")) {
            String query = text.replace("/rooms", "").trim();
            sendListRooms(query);
            return true;
        } else if (text.equalsIgnoreCase("/users")) {
            Iterator<Entry<Long, Player>> iter = players.entrySet().iterator();
            System.out.println("Listing Local User List:");
            if (players.size() == 0) {
                System.out.println("No local users in list");
            }
            while (iter.hasNext()) {
                Entry<Long, Player> user = iter.next();
                System.out.println(
                        String.format("%s[%s]", ((ClientPlayer) user.getValue()).getClientName(), user.getKey()));
            }
            return true;
        } else if (text.equalsIgnoreCase("/ready")) {
            sendReadyStatus();
        } else if (text.startsWith("/hide")) {
            String data = text.replace("/hide", "");
            String[] parts = data.split(",");
            if (parts.length >= 2) {
                try {
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    sendHidePosition(x, y);
                } catch (Exception e) {
                    System.out.println("Invalid coordinate, please try again in the format of /hide x,y");
                }
            }
            return true;// definitely don't share this one :)
        } else if (text.startsWith("/seek")) {
            String data = text.replace("/seek", "");
            String[] parts = data.split(",");
            if (parts.length >= 2) {
                try {
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    sendSeekPosition(x, y);
                } catch (Exception e) {
                    System.out.println("Invalid coordinate, please try again in the format of /seek x,y");
                }
            }
            return true;
        } else if (text.equalsIgnoreCase("/grid")) {
            System.out.println("Displaying grid data");
            if (grid != null) {
                System.out.println(grid.toString());
            } else {
                System.out.println("Grid isn't setup yet");
            }
        }
        return false;
    }

    // Send methods
    protected void sendSeekPosition(int x, int y) throws IOException {
        PositionPayload pp = new PositionPayload(PayloadType.SEEK);
        pp.setCoord(x, y);
        out.writeObject(pp);
    }

    protected void sendHidePosition(int x, int y) throws IOException {
        PositionPayload pp = new PositionPayload();
        pp.setCoord(x, y);
        out.writeObject(pp);
    }

    protected void sendReadyStatus() throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.READY);
        out.writeObject(p);
    }

    protected void sendListRooms(String query) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.GET_ROOMS);
        p.setMessage(query);
        out.writeObject(p);
    }

    protected void sendJoinRoom(String roomName) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.JOIN_ROOM);
        p.setMessage(roomName);
        out.writeObject(p);
    }

    protected void sendCreateRoom(String roomName) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CREATE_ROOM);
        p.setMessage(roomName);
        out.writeObject(p);
    }

    protected void sendDisconnect() throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.DISCONNECT);
        out.writeObject(p);
    }

    protected void sendConnect() throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CONNECT);
        p.setClientName(clientName);
        out.writeObject(p);
    }

    protected void sendMessage(String message) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setMessage(message);
        p.setClientName(clientName);
        out.writeObject(p);
    }

    // end send methods
    @Deprecated // remove in Milestone3
    private void listenForKeyboard() {
        inputThread = new Thread() {
            @Override
            public void run() {
                logger.info("Listening for input");
                try (Scanner si = new Scanner(System.in);) {
                    String line = "";
                    isRunning = true;
                    while (isRunning) {
                        try {
                            logger.info("Waiting for input");
                            line = si.nextLine();
                            if (!processClientCommand(line)) {
                                if (isConnected()) {
                                    if (line != null && line.trim().length() > 0) {
                                        sendMessage(line);
                                    }

                                } else {
                                    logger.info("Not connected to server");
                                }
                            }
                        } catch (Exception e) {
                            logger.warning("Connection dropped");
                            break;
                        }
                    }
                    logger.info("Exited loop");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    close();
                }
            }
        };
        inputThread.start();
    }

    private void listenForServerPayload() {
        fromServerThread = new Thread() {
            @Override
            public void run() {
                try {
                    Payload fromServer;

                    // while we're connected, listen for objects from server
                    while (isRunning && !server.isClosed() && !server.isInputShutdown()
                            && (fromServer = (Payload) in.readObject()) != null) {

                        logger.info("Debug Info: " + fromServer);
                        processPayload(fromServer);

                    }
                    logger.info("listenForServerPayload() loop exited");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    logger.info("Stopped listening to server input");
                    close();
                }
            }
        };
        fromServerThread.start();// start the thread
    }

    protected String getClientNameById(long clientId) {
        if (players.containsKey(clientId)) {
            return ((ClientPlayer) players.get(clientId)).getClientName();
        }
        if (clientId == Constants.DEFAULT_CLIENT_ID) {
            return "[Server]";
        }
        return "unkown user";
    }

    private void addPlayer(long clientId, String clientName) {
        if (!players.containsKey(clientId)) {
            ClientPlayer cp = new ClientPlayer(clientId, clientName);
            players.put(clientId, cp);
        }
    }

    private void removePlayer(long clientId) {
        if (players.containsKey(clientId)) {
            players.remove(clientId);
        }
    }

    /**
     * Processes incoming payloads from ServerThread
     * 
     * @param p
     */
    private void processPayload(Payload p) {
        switch (p.getPayloadType()) {
            case CONNECT:

                addPlayer(p.getClientId(), p.getClientName());
                System.out.println(String.format("*%s %s*",
                        p.getClientName(),
                        p.getMessage()));
                break;
            case DISCONNECT:
                removePlayer(p.getClientId());
                if (p.getClientId() == myClientId) {
                    myClientId = Constants.DEFAULT_CLIENT_ID;
                    isSeeker = false;
                }
                System.out.println(String.format("*%s %s*",
                        p.getClientName(),
                        p.getMessage()));
                break;
            case SYNC_CLIENT:
                addPlayer(p.getClientId(), p.getClientName());
                break;
            case MESSAGE:
                System.out.println(Constants.ANSI_CYAN + String.format("%s: %s",
                        getClientNameById(p.getClientId()),
                        p.getMessage()) + Constants.ANSI_RESET);
                break;
            case CLIENT_ID:
                if (myClientId == Constants.DEFAULT_CLIENT_ID) {
                    myClientId = p.getClientId();
                } else {
                    logger.warning("Receiving client id despite already being set");
                }
                break;
            case GET_ROOMS:
                RoomResultPayload rp = (RoomResultPayload) p;
                System.out.println("Received Room List:");
                if (rp.getMessage() != null) {
                    System.out.println(rp.getMessage());
                } else {
                    for (int i = 0, l = rp.getRooms().length; i < l; i++) {
                        System.out.println(String.format("%s) %s", (i + 1), rp.getRooms()[i]));
                    }
                }
                break;
            case RESET_USER_LIST:
                players.clear();
                break;
            case READY:
                System.out.println(String.format("Player %s is ready", getClientNameById(p.getClientId()))
                        + Constants.ANSI_RESET);
                if (players.containsKey(p.getClientId())) {
                    players.get(p.getClientId()).setReady(true);
                }
                break;
            case PHASE:
                System.out.println(Constants.ANSI_YELLOW + String.format("The current phase is %s", p.getMessage())
                        + Constants.ANSI_RESET);
                currentPhase = Phase.valueOf(p.getMessage());
                break;
            case SEEKER:
                isSeeker = p.getClientId() == myClientId;
                if (isSeeker) {
                    System.out.println(Constants.ANSI_GREEN + "You are the seeker" + Constants.ANSI_RESET);
                } else {
                    System.out.println(Constants.ANSI_GREEN + getClientNameById(p.getClientId()) + " is the seeker"
                            + Constants.ANSI_RESET);
                }
                break;
            case HIDE:
                try {
                    PositionPayload pp = (PositionPayload) p;
                    if (players.containsKey(pp.getClientId())) {
                        grid.removePlayerFromCell(pp.getX(), pp.getY(), pp.getClientId());
                        grid.addPlayerToCell(pp.getX(), pp.getY(), players.get(pp.getClientId()));
                        System.out
                                .println(Constants.ANSI_BLUE + String.format("Player %s is hiding at [%s,%s]",
                                        getClientNameById(p.getClientId()),
                                        pp.getX(), pp.getY()) + Constants.ANSI_RESET);
                    }
                } catch (Exception e) {
                    logger.severe(Constants.ANSI_RED + String.format("Error handling position payload: %s", e)
                            + Constants.ANSI_RESET);
                }
                break;
            case OUT:
                if (p.getClientId() == Constants.DEFAULT_CLIENT_ID) {
                    players.values().stream().forEach(player -> player.setIsOut(false));
                    System.out.println("Resetting out players");
                } else {
                    System.out.println(
                            Constants.ANSI_BLUE + String.format("Player %s is out!", getClientNameById(p.getClientId()))
                                    + Constants.ANSI_RESET);
                    if (players.containsKey(p.getClientId())) {
                        players.get(p.getClientId()).setIsOut(true);
                    }
                }
                break;
            case GRID:
                if (grid == null) {
                    grid = new Grid();
                    grid.build(5, 5);// TODO keep in sync with server, later server should send this info
                }
                try {
                    GridPayload gp = (GridPayload) p;
                    if (gp.getGrid() == null) {
                        grid.reset();
                    } else {
                        grid.importData(gp.getGrid());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case POINTS:
                try {
                    PointsPayload pp = (PointsPayload) p;
                    if (players.containsKey(p.getClientId())) {
                        players.get(p.getClientId()).setPoints(pp.getPoints());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            default:
                logger.warning(Constants.ANSI_RED + String.format("Unhandled Payload type: %s", p.getPayloadType())
                        + Constants.ANSI_RESET);
                break;

        }
    }

    @Deprecated // removing in Milestone3
    public void start() throws IOException {
        listenForKeyboard();
    }

    private void close() {
        myClientId = Constants.DEFAULT_CLIENT_ID;
        players.clear();
        try {
            inputThread.interrupt();
        } catch (Exception e) {
            System.out.println("Error interrupting input");
            e.printStackTrace();
        }
        try {
            fromServerThread.interrupt();
        } catch (Exception e) {
            System.out.println("Error interrupting listener");
            e.printStackTrace();
        }
        try {
            System.out.println("Closing output stream");
            out.close();
        } catch (NullPointerException ne) {
            System.out.println("Server was never opened so this exception is ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Closing input stream");
            in.close();
        } catch (NullPointerException ne) {
            System.out.println("Server was never opened so this exception is ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Closing connection");
            server.close();
            System.out.println("Closed socket");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ne) {
            System.out.println("Server was never opened so this exception is ok");
        }
    }

    @Deprecated // removing in Milestone3
    public static void main(String[] args) {
        try {
            // if start is private, it's valid here since this main is part of the class
            Client.Instance.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

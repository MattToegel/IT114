package HNS.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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
    INSTANCE;

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
    List<IClientEvents> listeners = new ArrayList<IClientEvents>();

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

    public Grid getGrid() {
        return grid;
    }

    public boolean isCurrentPhase(Phase phase) {
        return currentPhase == phase;
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public void addListener(IClientEvents listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
    }

    /**
     * Takes an ip address and a port to attempt a socket connection to a server.
     * 
     * @param address
     * @param port
     * @param clientName
     * @param listener
     * @return true if connection was successful
     */
    boolean connect(String address, int port, String clientName, IClientEvents listener) {
        try {
            addListener(listener);
            this.clientName = clientName;
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

    public void removeListener(IClientEvents listener) {
        listeners.remove(listener);
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

    public void sendReadyStatus() throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.READY);
        out.writeObject(p);
    }

    public void sendListRooms(String query) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.GET_ROOMS);
        p.setMessage(query);
        out.writeObject(p);
    }

    public void sendJoinRoom(String roomName) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.JOIN_ROOM);
        p.setMessage(roomName);
        out.writeObject(p);
    }

    public void sendCreateRoom(String roomName) throws IOException {
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

    public void sendMessage(String message) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setMessage(message);
        p.setClientName(clientName);
        out.writeObject(p);
    }

    private void listenForServerPayload() {
        fromServerThread = new Thread() {
            @Override
            public void run() {
                try {
                    Payload fromServer;
                    isRunning = true;
                    // while we're connected, listen for objects from server
                    while (isRunning && !server.isClosed() && !server.isInputShutdown()
                            && (fromServer = (Payload) in.readObject()) != null) {

                        logger.info("Debug Info: " + fromServer);
                        processPayload(fromServer);

                    }
                    logger.info("listenForServerPayload() loop exited");
                } catch (Exception e) {
                    logger.severe("Exception in payload");
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
        try {
        switch (p.getPayloadType()) {
            case CONNECT:

                addPlayer(p.getClientId(), p.getClientName());
                logger.info(String.format("*%s %s*",
                        p.getClientName(),
                        p.getMessage()));
                listeners.forEach(l -> l.onClientConnect(
                        p.getClientId(), p.getClientName(),
                        String.format("*%s %s*", p.getClientName(), p.getMessage())));
                break;
            case DISCONNECT:
                removePlayer(p.getClientId());
                if (p.getClientId() == myClientId) {
                    myClientId = Constants.DEFAULT_CLIENT_ID;
                    isSeeker = false;
                }
                logger.info(String.format("*%s %s*",
                        p.getClientName(),
                        p.getMessage()));
                listeners.forEach(l -> l.onClientDisconnect(
                        p.getClientId(), p.getClientName(), (String.format("*%s %s*",
                                p.getClientName(),
                                p.getMessage()))));
                break;
            case SYNC_CLIENT:
                addPlayer(p.getClientId(), p.getClientName());
                listeners.forEach(l -> l.onSyncClient(
                        p.getClientId(), p.getClientName()));
                break;
            case MESSAGE:
                System.out.println(Constants.ANSI_CYAN + String.format("%s: %s",
                        getClientNameById(p.getClientId()),
                        p.getMessage()) + Constants.ANSI_RESET);
                listeners.forEach(l -> l.onMessageReceive(
                        p.getClientId(), p.getMessage()));
                break;
            case CLIENT_ID:
                if (myClientId == Constants.DEFAULT_CLIENT_ID) {
                    myClientId = p.getClientId();
                } else {
                    logger.warning("Receiving client id despite already being set");
                }
                listeners.forEach(l -> l.onReceiveClientId(
                        p.getClientId()));
                break;
            case GET_ROOMS:
                RoomResultPayload rp = (RoomResultPayload) p;
                logger.info("Received Room List:");
                if (rp.getMessage() != null) {
                    logger.info(rp.getMessage());
                } else {
                    for (int i = 0, l = rp.getRooms().length; i < l; i++) {
                        logger.info(String.format("%s) %s", (i + 1), rp.getRooms()[i]));
                    }
                }
                listeners.forEach(l -> l.onReceiveRoomList(
                        rp.getRooms(), p.getMessage()));
                break;
            case RESET_USER_LIST:
                players.clear();
                listeners.forEach(l -> l.onResetUserList());
                break;
            case READY:
                logger.info(String.format("Player %s is ready", getClientNameById(p.getClientId()))
                        + Constants.ANSI_RESET);
                if (players.containsKey(p.getClientId())) {
                    players.get(p.getClientId()).setReady(true);
                }
                listeners.forEach(l -> l.onReceiveReady(p.getClientId()));
                long count = players.values().stream().filter(Player::isReady).count();
                listeners.forEach(l -> l.onReceiveReadyCount(count));
                break;
            case PHASE:
                logger.info(Constants.ANSI_YELLOW + String.format("The current phase is %s", p.getMessage())
                        + Constants.ANSI_RESET);
                currentPhase = Phase.valueOf(p.getMessage());
                listeners.forEach(l -> l.onReceivePhase(Phase.valueOf(p.getMessage())));
                break;
            case SEEKER:
                isSeeker = p.getClientId() == myClientId;
                if (isSeeker) {
                    logger.info(Constants.ANSI_GREEN + "You are the seeker" + Constants.ANSI_RESET);
                } else {
                    logger.info(Constants.ANSI_GREEN + getClientNameById(p.getClientId()) + " is the seeker"
                            + Constants.ANSI_RESET);
                }
                listeners.forEach(l -> l.onReceiveSeeker(p.getClientId()));
                break;
            case HIDE:
                try {
                    PositionPayload pp = (PositionPayload) p;
                    if (players.containsKey(pp.getClientId())) {
                        ClientPlayer cp = (ClientPlayer) players.get(pp.getClientId());
                        if (cp.getCurrentCell() != null) {
                            grid.removePlayerFromCell(cp.getCurrentCell().getX(), cp.getCurrentCell().getY(),
                                    pp.getClientId());
                        }
                        grid.addPlayerToCell(pp.getX(), pp.getY(), cp);
                        logger.info(Constants.ANSI_BLUE + String.format("Player %s is hiding at [%s,%s]",
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
                    logger.info("Resetting out players");
                } else {
                    logger.info(
                            Constants.ANSI_BLUE + String.format("Player %s is out!", getClientNameById(p.getClientId()))
                                    + Constants.ANSI_RESET);
                    if (players.containsKey(p.getClientId())) {
                        players.get(p.getClientId()).setIsOut(true);
                    }
                }
                listeners.forEach(l -> l.onReceiveOut(p.getClientId()));
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
                    listeners.forEach(l -> l.onReceiveGrid(grid));
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
                    listeners.forEach(l -> l.onReceivePoints(pp.getClientId(), pp.getPoints()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            default:
                logger.warning(Constants.ANSI_RED + String.format("Unhandled Payload type: %s", p.getPayloadType())
                        + Constants.ANSI_RESET);
                break;

        }
    } catch (Exception e) {
        logger.severe("Payload handling problem");
        e.printStackTrace();
    }
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

}

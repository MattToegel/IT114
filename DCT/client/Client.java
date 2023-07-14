package DCT.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.logging.Logger;

import DCT.common.CharacterPayload;
import DCT.common.Constants;
import DCT.common.Grid;
import DCT.common.Payload;
import DCT.common.PayloadType;
import DCT.common.PositionPayload;
import DCT.common.RoomResultPayload;
import DCT.common.Character.CharacterType;
import DCT.common.CellPayload;
import DCT.common.Character;

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
    // private String clientName = "";
    private ClientPlayer myPlayer = new ClientPlayer();
    private long myClientId = Constants.DEFAULT_CLIENT_ID;
    private static Logger logger = Logger.getLogger(Client.class.getName());

    private Hashtable<Long, ClientPlayer> userList = new Hashtable<Long, ClientPlayer>();

    Grid clientGrid = new Grid();

    private static IClientEvents events;

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
     * @param username
     * @param callback (for triggering UI events)
     * @return true if connection was successful
     */
    public boolean connect(String address, int port, String username, IClientEvents callback) {
        // TODO validate
        // this.clientName = username;
        myPlayer.setClientName(username);
        Client.events = callback;
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

    // Send methods
    protected void sendMove(int x, int y) throws IOException {
        PositionPayload pp = new PositionPayload();
        pp.setCoord(x, y);
        out.writeObject(pp);
    }

    protected void sendLoadCharacter(String characterCode) throws IOException {
        CharacterPayload cp = new CharacterPayload();
        Character c = new Character();
        c.setCode(characterCode);
        cp.setCharacter(c);
        out.writeObject(cp);
    }

    protected void sendCreateCharacter(CharacterType characterType) throws IOException {
        CharacterPayload cp = new CharacterPayload();
        cp.setCharacterType(characterType);
        out.writeObject(cp);
    }

    protected void sendReadyStatus() throws IOException {
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
        p.setClientName(myPlayer.getClientName());
        out.writeObject(p);
    }

    public void sendMessage(String message) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setMessage(message);
        p.setClientName(myPlayer.getClientName());
        out.writeObject(p);
    }

    // end send methods

    private void listenForServerPayload() {
        isRunning = true;
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

    protected String getClientNameById(long id) {
        if (userList.containsKey(id)) {
            return userList.get(id).getClientName();
        }
        if (id == Constants.DEFAULT_CLIENT_ID) {
            return "[Server]";
        }
        return "unkown user";
    }

    /**
     * Processes incoming payloads from ServerThread
     * 
     * @param p
     */
    private void processPayload(Payload p) {
        switch (p.getPayloadType()) {
            case CONNECT:
                if (!userList.containsKey(p.getClientId())) {
                    ClientPlayer cp = new ClientPlayer();
                    cp.setClientName(p.getClientName());
                    cp.setClientId(p.getClientId());
                    userList.put(p.getClientId(), cp);
                }
                System.out.println(String.format("*%s %s*",
                        p.getClientName(),
                        p.getMessage()));
                events.onClientConnect(p.getClientId(), p.getClientName(), p.getMessage());
                break;
            case DISCONNECT:
                if (userList.containsKey(p.getClientId())) {
                    userList.remove(p.getClientId());
                }
                if (p.getClientId() == myClientId) {
                    myClientId = Constants.DEFAULT_CLIENT_ID;
                }
                System.out.println(String.format("*%s %s*",
                        p.getClientName(),
                        p.getMessage()));
                events.onClientDisconnect(p.getClientId(), p.getClientName(), p.getMessage());
                break;
            case SYNC_CLIENT:
                if (!userList.containsKey(p.getClientId())) {
                    ClientPlayer cp = new ClientPlayer();
                    cp.setClientName(p.getClientName());
                    cp.setClientId(p.getClientId());
                    userList.put(p.getClientId(), cp);
                }
                events.onSyncClient(p.getClientId(), p.getClientName());
                break;
            case MESSAGE:
                System.out.println(String.format("%s: %s",
                        getClientNameById(p.getClientId()),
                        p.getMessage()));
                events.onMessageReceive(p.getClientId(), p.getMessage());
                break;
            case CLIENT_ID:
                if (myClientId == Constants.DEFAULT_CLIENT_ID) {
                    myClientId = p.getClientId();
                    myPlayer.setClientId(myClientId);
                    userList.put(myClientId, myPlayer);
                } else {
                    logger.warning("Receiving client id despite already being set");
                }
                events.onReceiveClientId(p.getClientId());
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
                events.onReceiveRoomList(rp.getRooms(), rp.getMessage());
                break;
            case RESET_USER_LIST:
                userList.clear();
                events.onResetUserList();
                break;
            case READY:
                System.out.println(String.format("Player %s is ready", getClientNameById(p.getClientId())));
                break;
            case PHASE:
                System.out.println(String.format("The current phase is %s", p.getMessage()));
                break;
            case CHARACTER:
                CharacterPayload cp = (CharacterPayload) p;
                System.out.println("Created Character");
                Character character = cp.getCharacter();

                if (userList.containsKey(cp.getClientId())) {
                    logger.info("Assigning character to " + cp.getClientId());
                    userList.get(cp.getClientId()).assignCharacter(character);
                }
                if (cp.getClientId() == myClientId) {
                    // myPlayer.assignCharacter(character);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Character created: ").append(character.getName()).append("\n");
                    sb.append("Character level: ").append(character.getLevel()).append("\n");
                    sb.append("Character type: ").append(character.getType()).append("\n");
                    sb.append("Character action type: ").append(character.getActionType()).append("\n");
                    sb.append("Character stats: ").append("\n");
                    sb.append("Attack: ").append(character.getAttack()).append("\n");
                    sb.append("Vitality: ").append(character.getVitality()).append("\n");
                    sb.append("Defense: ").append(character.getDefense()).append("\n");
                    sb.append("Will: ").append(character.getWill()).append("\n");
                    sb.append("Luck: ").append(character.getLuck()).append("\n");
                    sb.append("Progression Rate: ").append(character.getProgressionRate()).append("\n");
                    sb.append("Range: ").append(character.getRange()).append("\n");
                    System.out.println(sb.toString());
                }
                break;
            case TURN:
                System.out.println(String.format("Current Player: %s", getClientNameById(p.getClientId())));
                break;
            case GRID:
                try {
                    PositionPayload pp = (PositionPayload) p;
                    clientGrid.buildBasic(pp.getX(), pp.getY());
                    clientGrid.print();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CELL:
                try {
                    CellPayload cellPayload = (CellPayload) p;
                    clientGrid.update(cellPayload.getCellData(), userList);
                    clientGrid.print();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case GRID_RESET:
                if (clientGrid != null) {
                    clientGrid.reset();
                    System.out.println("Grid Reset");
                    clientGrid.print();
                }
                break;
            default:
                logger.warning(String.format("Unhandled Payload type: %s", p.getPayloadType()));
                break;

        }
    }

    private void close() {
        myClientId = Constants.DEFAULT_CLIENT_ID;
        userList.clear();
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
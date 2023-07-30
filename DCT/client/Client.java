package DCT.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import DCT.common.Cell;
import DCT.common.CellData;
import DCT.common.CellPayload;
import DCT.common.CellType;
import DCT.common.Character;
import DCT.common.Character.CharacterType;
import DCT.common.PoorMansDB.AsyncFileWriter;
import DCT.common.CharacterPayload;
import DCT.common.Constants;
import DCT.common.DoorCell;
import DCT.common.Grid;
import DCT.common.Payload;
import DCT.common.PayloadType;
import DCT.common.Phase;
import DCT.common.PositionPayload;
import DCT.common.RoomResultPayload;

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

    private static List<IClientEvents> events = new ArrayList<IClientEvents>();

    public void addCallback(IClientEvents e) {
        events.add(e);
    }

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
        addCallback(callback);
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
    public void sendEndTurn() throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.END_TURN);
        out.writeObject(p);
    }

    public void sendHeal(int x, int y) throws IOException {
        PositionPayload pp = new PositionPayload();
        pp.setCoord(x, y);
        pp.setPayloadType(PayloadType.HEAL);
        out.writeObject(pp);
    }

    public void sendAttack(int x, int y) throws IOException {
        PositionPayload pp = new PositionPayload();
        pp.setCoord(x, y);
        pp.setPayloadType(PayloadType.ATTACK);
        out.writeObject(pp);
    }

    public void sendMove(int x, int y) throws IOException {
        PositionPayload pp = new PositionPayload();
        pp.setCoord(x, y);
        out.writeObject(pp);
    }

    public void sendLoadCharacter(String characterCode) throws IOException {
        CharacterPayload cp = new CharacterPayload();
        Character c = new Character();
        c.setCode(characterCode);
        cp.setCharacter(c);
        out.writeObject(cp);
    }

    public void sendCreateCharacter(CharacterType characterType) throws IOException {
        CharacterPayload cp = new CharacterPayload();
        cp.setCharacterType(characterType);
        out.writeObject(cp);
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

    public String getClientNameById(long id) {
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
                events.forEach(e -> {
                    e.onClientConnect(p.getClientId(), p.getClientName(), p.getMessage());
                });

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
                events.forEach(e -> {
                    e.onClientDisconnect(p.getClientId(), p.getClientName(), p.getMessage());
                });

                break;
            case SYNC_CLIENT:
                if (!userList.containsKey(p.getClientId())) {
                    ClientPlayer cp = new ClientPlayer();
                    cp.setClientName(p.getClientName());
                    cp.setClientId(p.getClientId());
                    userList.put(p.getClientId(), cp);
                }
                events.forEach(e -> {
                    e.onSyncClient(p.getClientId(), p.getClientName());
                });

                break;
            case MESSAGE:
                System.out.println(String.format("%s: %s",
                        getClientNameById(p.getClientId()),
                        p.getMessage()));
                events.forEach(e -> {
                    e.onMessageReceive(p.getClientId(), p.getMessage());
                });

                break;
            case CLIENT_ID:
                if (myClientId == Constants.DEFAULT_CLIENT_ID) {
                    myClientId = p.getClientId();
                    myPlayer.setClientId(myClientId);
                    userList.put(myClientId, myPlayer);
                } else {
                    logger.warning("Receiving client id despite already being set");
                }
                events.forEach(e -> {
                    e.onReceiveClientId(p.getClientId());
                });

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
                events.forEach(e -> {
                    e.onReceiveRoomList(rp.getRooms(), rp.getMessage());
                });

                break;
            case RESET_USER_LIST:
                userList.clear();
                events.forEach(e -> {
                    e.onResetUserList();
                });

                break;
            case READY:
                System.out.println(String.format("Player %s is ready", getClientNameById(p.getClientId())));
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceiveReady(p.getClientId());
                    }
                });
                break;
            case PHASE:
                System.out.println(String.format("The current phase is %s", p.getMessage()));
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceivePhase(Enum.valueOf(Phase.class, p.getMessage()));
                    }
                });
                break;
            case CHARACTER:
                CharacterPayload cp = (CharacterPayload) p;
                System.out.println("Created Character");
                Character character = cp.getCharacter();
                if (character.getName().equals(null)) {
                    events.forEach(e -> {
                        e.onMessageReceive(Constants.DEFAULT_CLIENT_ID, character.getCode());
                    });
                } else {
                    if (userList.containsKey(cp.getClientId())) {
                        logger.info("Assigning character to " + cp.getClientId());
                        userList.get(cp.getClientId()).assignCharacter(character);
                    }
                    if (cp.getClientId() == myClientId) {
                        // myPlayer.assignCharacter(character);

                        logger.info(character.toString());
                        events.forEach(e -> {
                            e.onMessageReceive(Constants.DEFAULT_CLIENT_ID, character.toString());
                        });
                        Path filePath = Paths.get(System.getProperty("user.dir"), "Saves", "Characters",
                                character.getName().replace("'", "_").replace(" ", "-") + ".data");
                        if (character.getCode() == null || character.getCode().length() == 0) {
                            logger.severe("Character received without code: " + character.toString());
                        } else {
                            AsyncFileWriter.writeFileContent(filePath.toString(), character, (success) -> {
                                logger.info(
                                        String.format("Wrote file %s successfully %s", filePath.toString(), success));
                            });
                        }
                    }

                    events.forEach(e -> {
                        if (e instanceof IGameEvents) {
                            ((IGameEvents) e).onReceiveCharacter(p.getClientId(), character);
                        }
                    });
                }

                break;
            case TURN:
                System.out.println(String.format("Current Player: %s", getClientNameById(p.getClientId())));
                /*
                 * events.forEach(e -> {
                 * if (e instanceof IGameEvents) {
                 * ((IGameEvents) e).onReceiveTurn(p.getClientId());
                 * }
                 * });
                 */
                break;
            case GRID:
                try {
                    PositionPayload pp = (PositionPayload) p;
                    clientGrid.buildBasic(pp.getX(), pp.getY());
                    clientGrid.print();
                    events.forEach(e -> {
                        if (e instanceof IGameEvents) {
                            ((IGameEvents) e).onReceiveGrid(pp.getX(), pp.getY());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case CELL:
                try {
                    CellPayload cellPayload = (CellPayload) p;
                    clientGrid.update(cellPayload.getCellData());
                    clientGrid.print();
                    events.forEach(e -> {
                        if (e instanceof IGameEvents) {
                            List<CellData> cellData = cellPayload.getCellData();
                            List<Cell> cells = new ArrayList<Cell>();
                            for (CellData cd : cellData) {
                                Cell c = clientGrid.getCell(cd.getX(), cd.getY());
                                c.setCellType(cd.getCellType());
                                c.setBlocked(cd.isBlocked());
                                if (c instanceof DoorCell) {
                                    ((DoorCell) c).setLocked(cd.isLocked());
                                    ((DoorCell) c).setEnd(cd.getCellType() == CellType.END_DOOR ? true : false);
                                    // TBD ((DoorCell)c).setOpen();
                                }
                                cells.add(c);
                            }
                            ((IGameEvents) e).onReceiveCell(cells);
                        }
                    });
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
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceiveGrid(-1, -1);
                    }
                });
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
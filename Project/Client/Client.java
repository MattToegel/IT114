package Project.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import Project.Client.Interfaces.IConnectionEvents;
import Project.Client.Interfaces.IEnergyEvents;
import Project.Client.Interfaces.IGridEvents;
import Project.Client.Interfaces.ICardEvents;
import Project.Client.Interfaces.IClientEvents;
import Project.Client.Interfaces.IMessageEvents;
import Project.Client.Interfaces.IPhaseEvent;
import Project.Client.Interfaces.IReadyEvent;
import Project.Client.Interfaces.IRoomEvents;
import Project.Client.Interfaces.ITimeEvents;
import Project.Client.Interfaces.ITowerEvents;
import Project.Client.Interfaces.ITurnEvents;
import Project.Common.Card;
import Project.Common.CardPayload;
import Project.Common.ConnectionPayload;
import Project.Common.Constants;
import Project.Common.EnergyPayload;
import Project.Common.Grid;
import Project.Common.LoggerUtil;
import Project.Common.Payload;
import Project.Common.PayloadType;
import Project.Common.Phase;
import Project.Common.ReadyPayload;
import Project.Common.RoomResultsPayload;
import Project.Common.TextFX;
import Project.Common.TimerPayload;
import Project.Common.TimerType;
import Project.Common.Tower;
import Project.Common.TowerPayload;
import Project.Common.XYPayload;
import Project.Common.TextFX.Color;

/**
 * Demoing bi-directional communication between client and server in a
 * multi-client scenario
 */
public enum Client {
    INSTANCE;

    {
        // TODO moved to ClientUI (this repeat doesn't do anything since config is set
        // only once)

        // statically initialize the client-side LoggerUtil
        LoggerUtil.LoggerConfig config = new LoggerUtil.LoggerConfig();
        config.setFileSizeLimit(2048 * 1024); // 2MB
        config.setFileCount(1);
        config.setLogLocation("client.log");
        // Set the logger configuration
        LoggerUtil.INSTANCE.setConfig(config);
    }
    private Socket server = null;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    final Pattern ipAddressPattern = Pattern
            .compile("/connect\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{3,5})");
    final Pattern localhostPattern = Pattern.compile("/connect\\s+(localhost:\\d{3,5})");
    private volatile boolean isRunning = true; // volatile for thread-safe visibility
    private ConcurrentHashMap<Long, ClientPlayer> knownClients = new ConcurrentHashMap<>();
    private ClientPlayer myData;
    private Phase currentPhase = Phase.READY;

    // constants (used to reduce potential types when using them in code)
    private final String COMMAND_CHARACTER = "/";
    private final String CREATE_ROOM = "createroom";
    private final String JOIN_ROOM = "joinroom";
    private final String LIST_ROOMS = "listrooms";
    private final String DISCONNECT = "disconnect";
    private final String LOGOFF = "logoff";
    private final String LOGOUT = "logout";
    private final String SINGLE_SPACE = " ";
    // other constants
    private final String READY = "ready";
    private final String HAND = "hand";
    private final String USE = "use";
    private final String DISCARD = "discard";
    private final String PLACE = "place";
    private final String ATTACK = "attack";
    private final String ALLOCATE = "allocate";
    private final String END = "end";

    private Grid grid = null;

    // callback that updates the UI
    private static List<IClientEvents> events = new ArrayList<IClientEvents>();

    public void addCallback(IClientEvents e) {
        events.add(e);
    }

    // needs to be private now that the enum logic is handling this
    private Client() {
        LoggerUtil.INSTANCE.info("Client Created");
        myData = new ClientPlayer();
    }

    public boolean isConnected() {
        if (server == null) {
            return false;
        }
        // https://stackoverflow.com/a/10241044
        // Note: these check the client's end of the socket connect; therefore they
        // don't really help determine if the server had a problem
        // and is just for lesson's sake
        return server.isConnected() && !server.isClosed() && !server.isInputShutdown() && !server.isOutputShutdown();
    }

    /**
     * Takes an IP address and a port to attempt a socket connection to a server.
     * 
     * @param address
     * @param port
     * @return true if connection was successful
     */
    @Deprecated
    private boolean connect(String address, int port) {
        try {
            server = new Socket(address, port);
            // channel to send to server
            out = new ObjectOutputStream(server.getOutputStream());
            // channel to listen to server
            in = new ObjectInputStream(server.getInputStream());
            LoggerUtil.INSTANCE.info("Client connected");
            // Use CompletableFuture to run listenToServer() in a separate thread
            CompletableFuture.runAsync(this::listenToServer);
        } catch (UnknownHostException e) {
            LoggerUtil.INSTANCE.warning("Unknown host", e);
        } catch (IOException e) {
            LoggerUtil.INSTANCE.severe("IOException", e);
        }
        return isConnected();
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
        myData.setClientName(username);
        addCallback(callback);
        try {
            server = new Socket(address, port);
            // channel to send to server
            out = new ObjectOutputStream(server.getOutputStream());
            // channel to listen to server
            in = new ObjectInputStream(server.getInputStream());
            LoggerUtil.INSTANCE.info("Client connected");
            // Use CompletableFuture to run listenToServer() in a separate thread
            CompletableFuture.runAsync(this::listenToServer);
            sendClientName();
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
     * followed by an IP address and port or localhost and port.
     * </p>
     * <p>
     * Example format: 123.123.123.123:3000
     * </p>
     * <p>
     * Example format: localhost:3000
     * </p>
     * https://www.w3schools.com/java/java_regex.asp
     * 
     * @param text
     * @return true if the text is a valid connection command
     */
    private boolean isConnection(String text) {
        Matcher ipMatcher = ipAddressPattern.matcher(text);
        Matcher localhostMatcher = localhostPattern.matcher(text);
        return ipMatcher.matches() || localhostMatcher.matches();
    }

    /**
     * Controller for handling various text commands.
     * <p>
     * Add more here as needed
     * </p>
     * 
     * @param text
     * @return true if the text was a command or triggered a command
     * @throws IOException
     */
    private boolean processClientCommand(String text) throws IOException {
        if (isConnection(text)) {
            if (myData.getClientName() == null || myData.getClientName().length() == 0) {
                System.out.println(TextFX.colorize("Name must be set first via /name command", Color.RED));
                return true;
            }
            // replaces multiple spaces with a single space
            // splits on the space after connect (gives us host and port)
            // splits on : to get host as index 0 and port as index 1
            String[] parts = text.trim().replaceAll(" +", " ").split(" ")[1].split(":");
            connect(parts[0].trim(), Integer.parseInt(parts[1].trim()));
            sendClientName();
            return true;
        } else if ("/quit".equalsIgnoreCase(text)) {
            close();
            return true;
        } else if (text.startsWith("/name")) {
            myData.setClientName(text.replace("/name", "").trim());
            System.out.println(TextFX.colorize("Set client name to " + myData.getClientName(), Color.CYAN));
            return true;
        } else if (text.equalsIgnoreCase("/users")) {
            // chatroom version
            /*
             * System.out.println(
             * String.join("\n", knownClients.values().stream()
             * .map(c -> String.format("%s(%s)", c.getClientName(),
             * c.getClientId())).toList()));
             */
            // non-chatroom version
            /**
             * System.out.println(
             * String.join("\n", knownClients.values().stream()
             * .map(c -> String.format("%s(%s) %s", c.getClientName(), c.getClientId(),
             * c.isReady() ? "[x]" : "[ ]"))
             * .toList()));
             */
            // updated to show turn status
            /*
             * System.out.println(
             * String.join("\n", knownClients.values().stream()
             * .map(c -> String.format("%s(%s) %s %s", c.getClientName(), c.getClientId(),
             * c.isReady() ? "[R]" : "[ ]", c.didTakeTurn() ? "[T]" : "[ ]"))
             * .toList()));
             */
            System.out.println(
                    String.join("\n", knownClients.values().stream()
                            .map(c -> String.format("%s(%s) %s %s E: %s/%s", c.getClientName(), c.getClientId(),
                                    c.isReady() ? "[R]" : "[ ]", c.didTakeTurn() ? "[T]" : "[ ]", c.getEnergy(),
                                    c.getEnergyCap()))
                            .toList()));
            return true;
        } else { // logic previously from Room.java
            // decided to make this as separate block to separate the core client-side items
            // vs the ones that generally are used after connection and that send requests
            if (text.startsWith(COMMAND_CHARACTER)) {
                boolean wasCommand = false;
                String fullCommand = text.replace(COMMAND_CHARACTER, "");
                String part1 = fullCommand;
                String[] commandParts = part1.split(SINGLE_SPACE, 2);// using limit so spaces in the command value
                                                                     // aren't split
                final String command = commandParts[0];
                final String commandValue = commandParts.length >= 2 ? commandParts[1] : "";
                switch (command) {
                    case CREATE_ROOM:
                        sendCreateRoom(commandValue);
                        wasCommand = true;
                        break;
                    case JOIN_ROOM:
                        sendJoinRoom(commandValue);
                        wasCommand = true;
                        break;
                    case LIST_ROOMS:
                        sendListRooms(commandValue);
                        wasCommand = true;
                        break;
                    // Note: these are to disconnect, they're not for changing rooms
                    case DISCONNECT:
                    case LOGOFF:
                    case LOGOUT:
                        sendDisconnect();
                        wasCommand = true;
                        break;
                    // others
                    case READY:
                        sendReady();
                        wasCommand = true;
                        break;
                    case HAND:
                        showHand();
                        wasCommand = true;
                        break;
                    case USE:
                        try {
                            String[] parts = commandValue.split(" ");
                            int cardOffset = Integer.parseInt(parts[0]) - 1;
                            Card c = myData.getHand().get(cardOffset);

                            int x = -1;
                            int y = -1;

                            if (parts.length == 2) {// handle new format /card <number> <x>,<y>
                                String[] coordinates = parts[1].split(",");
                                x = Integer.parseInt(coordinates[0]);
                                y = Integer.parseInt(coordinates[1]);
                            }

                            sendUseCard(c, x, y);
                        } catch (Exception e) {
                            System.out.println(
                                    TextFX.colorize(
                                            "Invalid command format, try /card card_number or /card card_number x,y (see /hand first)",
                                            Color.RED));
                        }
                        wasCommand = true;
                        break;
                    case DISCARD:
                        try {
                            int cardOffset = Integer.parseInt(commandValue) - 1;
                            Card c = myData.getHand().get(cardOffset);
                            sendDiscardCard(c);
                        } catch (Exception e) {
                            System.out.println(
                                    TextFX.colorize(
                                            "Invalid command format, try /discard card_number (see /hand first)",
                                            Color.RED));
                        }
                        wasCommand = true;
                        break;
                    case PLACE:
                        try {
                            String[] parts = commandValue.split(",");
                            int x = Integer.parseInt(parts[0]);
                            int y = Integer.parseInt(parts[1]);
                            sendPlace(x, y);
                        } catch (Exception e) {
                            System.out.println(TextFX.colorize("Invalid command format, try /place #,#", Color.RED));
                        }
                        wasCommand = true;
                        break;
                    case ATTACK:
                        try {
                            String[] parts = commandValue.split(",", 2);
                            int x = Integer.parseInt(parts[0]);
                            int y = Integer.parseInt(parts[1].split(" ")[0]);
                            String[] numberStrings = parts[1].split(" ")[1].split(",");

                            // Convert the String array to a List<Long>
                            List<Long> towerIds = Arrays.stream(numberStrings)
                                    .map(Long::parseLong)
                                    .collect(Collectors.toList());
                            sendAttack(x, y, towerIds);
                        } catch (Exception e) {
                            System.out.println(TextFX.colorize("Invalid command format, try /attack #,# #", Color.RED));
                        }
                        wasCommand = true;
                        break;
                    case ALLOCATE:
                        try {
                            String[] parts = commandValue.split(",", 2);
                            int x = Integer.parseInt(parts[0]);
                            int y = Integer.parseInt(parts[1].split(" ")[0]);
                            int energy = Integer.parseInt(parts[1].split(" ")[1]);
                            sendAllocate(x, y, energy);
                        } catch (Exception e) {
                            System.out
                                    .println(TextFX.colorize("Invalid command format, try /allocate #,# #", Color.RED));
                        }
                        wasCommand = true;
                        break;
                    case END:
                        sendEndTurn();
                        wasCommand = true;
                        break;
                }
                return wasCommand;
            }
        }
        return false;
    }

    public boolean isInRange(int x, int y, Tower tower) {
        Tower target = grid.getValidCellsWithinRangeBoundingBox(x, y, tower.getRange()).stream()
                .filter(c -> c.getTower() != null && c.getTower().getId() == tower.getId()).map(c -> c.getTower())
                .findFirst().orElse(null);
        return target != null;
    }

    public List<Card> getMyHand() {
        return myData.getHand();
    }

    public int getMyEnergy() {
        LoggerUtil.INSTANCE.info("My Energy: " + myData.getEnergy());
        return myData.getEnergy();
    }

    public long getMyClientId() {
        return myData.getClientId();
    }

    private void showHand() {
        System.out.println("Your hand:");

        List<Card> hand = myData.getHand();
        String result = IntStream.range(0, hand.size())
                .mapToObj(i -> String.format("%d) %s", i + 1, hand.get(i)))
                .collect(Collectors.joining("\n"));

        System.out.println(result);
    }

    public void clientSideGameEvent(String str) {
        events.forEach(event -> {
            if (event instanceof IMessageEvents) {
                // Note: using -2 to target GameEventPanel
                ((IMessageEvents) event).onMessageReceive(Constants.GAME_EVENT_CHANNEL, str);
            }
        });
    }

    // send methods to pass data to the ServerThread
    public void sendEndTurn() throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.END_TURN);
        send(p);
    }

    public void sendAllocate(int x, int y, int energy) throws IOException {
        EnergyPayload ep = new EnergyPayload();
        ep.setPayloadType(PayloadType.TOWER_ALLOCATE);
        ep.setEnergy(energy);
        ep.setX(x);
        ep.setY(y);
        send(ep);
    }

    public void sendAttack(int x, int y, List<Long> targets) throws IOException {
        TowerPayload tp = new TowerPayload(x, y);
        tp.setPayloadType(PayloadType.TOWER_ATTACK);
        tp.setTowerIds(targets);
        send(tp);
    }

    public void sendPlace(int x, int y) throws Exception {
        // check local grid first
        if (grid.getCell(x, y).isOccupied()) {
            System.out
                    .println(TextFX.colorize("That coordinate is already occupied, please try another", Color.YELLOW));
            throw new Exception("That coordinate is already occupied, please try another");
        }
        XYPayload p = new XYPayload(x, y);
        p.setPayloadType(PayloadType.TOWER_PLACE);
        send(p);
    }

    /**
     * Passes the desired card data and a potential target x,y for the effect
     * 
     * @param c
     * @param x
     * @param y
     * @throws IOException
     */
    public void sendUseCard(Card c, int x, int y) throws IOException {
        CardPayload cp = new CardPayload();
        cp.setCard(c);
        cp.setX(x);
        cp.setY(y);
        cp.setPayloadType(PayloadType.USE_CARD);
        send(cp);
    }

    public void sendDiscardCard(Card c) throws IOException {
        CardPayload cp = new CardPayload();
        cp.setCard(c);
        cp.setPayloadType(PayloadType.REMOVE_CARD);
        send(cp);
    }

    /**
     * Sends the client's intent to be ready.
     * Can also be used to toggle the ready state if coded on the server-side
     * 
     * @throws IOException
     */
    public void sendReady() throws IOException {
        ReadyPayload rp = new ReadyPayload();
        rp.setReady(true); // <- techically not needed as we'll use the payload type as a trigger
        send(rp);
    }

    /**
     * Sends a search to the server-side to get a list of potentially matching Rooms
     * 
     * @param roomQuery optional partial match search String
     * @throws IOException
     */
    public void sendListRooms(String roomQuery) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.ROOM_LIST);
        p.setMessage(roomQuery);
        send(p);
    }

    /**
     * Sends the room name we intend to create
     * 
     * @param room
     * @throws IOException
     */
    public void sendCreateRoom(String room) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.ROOM_CREATE);
        p.setMessage(room);
        send(p);
    }

    /**
     * Sends the room name we intend to join
     * 
     * @param room
     * @throws IOException
     */
    public void sendJoinRoom(String room) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.ROOM_JOIN);
        p.setMessage(room);
        send(p);
    }

    /**
     * Tells the server-side we want to disconnect
     * 
     * @throws IOException
     */
    void sendDisconnect() throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.DISCONNECT);
        send(p);
    }

    /**
     * Sends desired message over the socket
     * 
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        if (processClientCommand(message)) {
            return;
        }
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setMessage(message);
        send(p);
    }

    /**
     * Sends chosen client name after socket handshake
     * 
     * @throws IOException
     */
    private void sendClientName() throws IOException {
        if (myData.getClientName() == null || myData.getClientName().length() == 0) {
            System.out.println(TextFX.colorize("Name must be set first via /name command", Color.RED));
            return;
        }
        ConnectionPayload cp = new ConnectionPayload();
        cp.setClientName(myData.getClientName());
        send(cp);
    }

    /**
     * Generic send that passes any Payload over the socket (to ServerThread)
     * 
     * @param p
     * @throws IOException
     */
    private void send(Payload p) throws IOException {
        try {
            out.writeObject(p);
            out.flush();
        } catch (IOException e) {
            LoggerUtil.INSTANCE.severe("Socket send exception", e);
            throw e;
        }

    }
    // end send methods

    public void start() throws IOException {
        LoggerUtil.INSTANCE.info("Client starting");

        // Use CompletableFuture to run listenToInput() in a separate thread
        CompletableFuture<Void> inputFuture = CompletableFuture.runAsync(this::listenToInput);

        // Wait for inputFuture to complete to ensure proper termination
        inputFuture.join();
    }

    /**
     * Listens for messages from the server
     */
    private void listenToServer() {
        try {
            while (isRunning && isConnected()) {
                Payload fromServer = (Payload) in.readObject(); // blocking read
                if (fromServer != null) {
                    // System.out.println(fromServer);
                    processPayload(fromServer);
                } else {
                    LoggerUtil.INSTANCE.info("Server disconnected");
                    break;
                }
            }
        } catch (ClassCastException | ClassNotFoundException cce) {
            LoggerUtil.INSTANCE.severe("Error reading object as specified type: ", cce);
        } catch (IOException e) {
            if (isRunning) {
                LoggerUtil.INSTANCE.info("Connection dropped", e);
            }
        } finally {
            closeServerConnection();
        }
        LoggerUtil.INSTANCE.info("listenToServer thread stopped");
    }

    /**
     * Listens for keyboard input from the user
     */
    @Deprecated
    private void listenToInput() {
        try (Scanner si = new Scanner(System.in)) {
            System.out.println("Waiting for input"); // moved here to avoid console spam
            while (isRunning) { // Run until isRunning is false
                String line = si.nextLine();
                LoggerUtil.INSTANCE.severe(
                        "You shouldn't be using terminal input for Milestone 3. Interaction should be done through the UI");
                if (!processClientCommand(line)) {
                    if (isConnected()) {
                        sendMessage(line);
                    } else {
                        System.out.println(
                                "Not connected to server (hint: type `/connect host:port` without the quotes and replace host/port with the necessary info)");
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Error in listentToInput()", e);
        }
        LoggerUtil.INSTANCE.info("listenToInput thread stopped");
    }

    /**
     * Closes the client connection and associated resources
     */
    private void close() {
        isRunning = false;
        closeServerConnection();
        LoggerUtil.INSTANCE.info("Client terminated");
        // System.exit(0); // Terminate the application
    }

    /**
     * Closes the server connection and associated resources
     */
    private void closeServerConnection() {
        myData.reset();
        knownClients.clear();
        try {
            if (out != null) {
                LoggerUtil.INSTANCE.info("Closing output stream");
                out.close();
            }
        } catch (Exception e) {
            LoggerUtil.INSTANCE.info("Error closing output stream", e);
        }
        try {
            if (in != null) {
                LoggerUtil.INSTANCE.info("Closing input stream");
                in.close();
            }
        } catch (Exception e) {
            LoggerUtil.INSTANCE.info("Error closing input stream", e);
        }
        try {
            if (server != null) {
                LoggerUtil.INSTANCE.info("Closing connection");
                server.close();
                LoggerUtil.INSTANCE.info("Closed socket");
            }
        } catch (IOException e) {
            LoggerUtil.INSTANCE.info("Error closing socket", e);
        }
    }

    public static void main(String[] args) {
        Client client = Client.INSTANCE;
        try {
            client.start();
        } catch (IOException e) {
            LoggerUtil.INSTANCE.info("Exception from main()", e);
        }
    }

    /**
     * Handles received message from the ServerThread
     * 
     * @param payload
     */
    private void processPayload(Payload payload) {
        try {
            if (payload.getPayloadType() != PayloadType.TIME) {
                LoggerUtil.INSTANCE.info("Received Payload: " + payload);
            }
            switch (payload.getPayloadType()) {
                case PayloadType.CLIENT_ID: // get id assigned
                    ConnectionPayload cp = (ConnectionPayload) payload;
                    processClientData(cp.getClientId(), cp.getClientName());
                    break;
                case PayloadType.SYNC_CLIENT: // silent add
                    cp = (ConnectionPayload) payload;
                    processClientSync(cp.getClientId(), cp.getClientName());
                    break;
                case PayloadType.DISCONNECT: // remove a disconnected client (mostly for the specific message vs leaving
                                             // a room)
                    cp = (ConnectionPayload) payload;
                    processDisconnect(cp.getClientId(), cp.getClientName());
                    // note: we want this to cascade
                case PayloadType.ROOM_JOIN: // add/remove client info from known clients
                    cp = (ConnectionPayload) payload;
                    processRoomAction(cp.getClientId(), cp.getClientName(), cp.getMessage(), cp.isConnect());
                    break;
                case PayloadType.ROOM_LIST:
                    RoomResultsPayload rrp = (RoomResultsPayload) payload;
                    processRoomsList(rrp.getRooms(), rrp.getMessage());
                    break;
                case PayloadType.MESSAGE: // displays a received message
                    processMessage(payload.getClientId(), payload.getMessage());
                    break;
                case PayloadType.READY:
                    ReadyPayload rp = (ReadyPayload) payload;
                    processReadyStatus(rp.getClientId(), rp.isReady(), false);
                    break;
                case PayloadType.SYNC_READY:
                    ReadyPayload qrp = (ReadyPayload) payload;
                    processReadyStatus(qrp.getClientId(), qrp.isReady(), true);
                    break;
                case PayloadType.RESET_READY:
                    // note no data necessary as this is just a trigger
                    processResetReady();
                    break;
                case PayloadType.PHASE:
                    processPhase(payload.getMessage());
                    break;
                case PayloadType.GRID_DIMENSION:
                    XYPayload gd = (XYPayload) payload;
                    // clientId is holding the seed value which makes sure the Random class produces
                    // the same set of random values
                    long seed = gd.getClientId();
                    processGridDimension(gd.getX(), gd.getY(), seed);
                    break;
                case PayloadType.TURN:
                    ReadyPayload tp = (ReadyPayload) payload;
                    processTurnStatus(tp.getClientId(), tp.isReady());
                    break;
                case PayloadType.CARDS_IN_HAND:
                    CardPayload hand = (CardPayload) payload;
                    processHand(hand.getClientId(), hand.getCards());
                    break;
                case PayloadType.ADD_CARD:
                    CardPayload add = (CardPayload) payload;
                    processAddCard(add.getClientId(), add.getCard());
                    break;
                case PayloadType.REMOVE_CARD:
                    CardPayload remove = (CardPayload) payload;
                    processRemoveCard(remove.getClientId(), remove.getCard());
                    break;
                case PayloadType.TOWER_STATUS:
                    TowerPayload towerStatus = (TowerPayload) payload;
                    processTowerStatus(towerStatus.getClientId(), towerStatus.getX(), towerStatus.getY(),
                            towerStatus.getTower());
                    break;
                case PayloadType.ENERGY:
                    EnergyPayload userEnergy = (EnergyPayload) payload;
                    processEnergy(userEnergy.getClientId(), userEnergy.getEnergy());
                    break;
                case PayloadType.CURRENT_TURN:
                    processCurrentTurn(payload.getClientId());
                    break;
                case PayloadType.TIME:
                    TimerPayload timerPayload = (TimerPayload) payload;
                    processCurrentTimer(timerPayload.getTimerType(), timerPayload.getTime());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Could not process Payload: " + payload, e);
        }
    }

    /**
     * Returns the ClientName of a specific Client by ID.
     * 
     * @param id
     * @return the name, or Room if id is -1, or [Unknown] if failed to find
     */
    public String getClientNameFromId(long id) {
        if (id == ClientPlayer.DEFAULT_CLIENT_ID) {
            return "Room";
        }
        if (knownClients.containsKey(id)) {
            return knownClients.get(id).getClientName();
        }
        return "[Unknown]";
    }

    // payload processors
    private void processCurrentTimer(TimerType timerType, int time) {
        events.forEach(event -> {
            if (event instanceof ITimeEvents) {
                ((ITimeEvents) event).onTimerUpdate(timerType, time);
            }
        });
    }

    private void processCurrentTurn(long clientId) {
        // fixed after demo, needed to adjust the null checks
        ClientPlayer cp = knownClients.get(clientId);
        if (cp != null) {
            System.out.println(String.format("It's %s[%s]'s turn", cp.getClientName(), cp.getClientId()));
        }
        events.forEach(event -> {
            if (event instanceof ITurnEvents) {
                ((ITurnEvents) event).onCurrentTurn(clientId);
            }
            if (event instanceof IMessageEvents && cp != null) {
                ((IMessageEvents) event).onMessageReceive(Constants.GAME_EVENT_CHANNEL,
                        String.format("It's %s[%s]'s turn", cp.getClientName(), cp.getClientId()));
            }
        });
    }

    private void processEnergy(long clientId, int energy) {
        ClientPlayer cp = knownClients.get(clientId);
        if (cp != null) {
            cp.setEnergy(energy);
        } else {
            // added after demo
            knownClients.values().forEach(c -> c.setEnergy(0));
        }

        events.forEach(event -> {
            if (event instanceof IEnergyEvents) {
                ((IEnergyEvents) event).onUpdateEnergy(clientId, energy);
            }
        });
    }

    private void processTowerStatus(long clientId, int x, int y, Tower tower) {
        ClientPlayer cp = knownClients.get(clientId);
        if (grid.getCell(x, y).getTower() == null) {
            System.out.println(
                    TextFX.colorize(String.format("%s placed tower at %s,%s", cp.getClientName(), x, y), Color.CYAN));
            grid.setCell(x, y, tower);
        } else {
            if (tower.getHealth() > 0) {
                grid.getCell(x, y).updateTower(tower);
            } else {
                grid.getCell(x, y).removeTower();
            }
        }
        events.forEach(event -> {
            if (event instanceof ITowerEvents) {
                ((ITowerEvents) event).onReceiveTowerStatus(x, y, tower);
            }
        });
        LoggerUtil.INSTANCE.info("Grid: " + grid);
    }

    private void processRemoveCard(long clientId, Card card) {
        // Note: generally the player will only know their own hand
        // I chose to utilize clientId just in case there are future implementations
        // where you can see info about other players
        if (clientId == myData.getClientId()) {
            myData.removeFromHand(card);
            // Note: We may need to leverage an additional PayloadType
            // to distinguish between Use/Discard; I didn't for this lesson
            System.out.println("Used/Discarded Card " + card);
            events.forEach(event -> {
                if (event instanceof ICardEvents) {
                    ((ICardEvents) event).onRemoveCard(card);
                }
            });
        }
    }

    private void processAddCard(long clientId, Card card) {
        // Note: generally the player will only know their own hand
        // I chose to utilize clientId just in case there are future implementations
        // where you can see info about other players
        if (clientId == myData.getClientId()) {
            myData.addToHand(card);
            System.out.println("Received Card " + card);
            events.forEach(event -> {
                if (event instanceof ICardEvents) {
                    ((ICardEvents) event).onAddCard(card);
                }
            });
        }
    }

    private void processHand(long clientId, List<Card> cards) {
        // Note: generally the player will only know their own hand
        // I chose to utilize clientId just in case there are future implementations
        // where you can see info about other players
        if (clientId == myData.getClientId()) {
            myData.setHand(cards);
            showHand();
            events.forEach(event -> {
                if (event instanceof ICardEvents) {
                    ((ICardEvents) event).onSetCards(cards);
                }
            });
        }
    }

    private void processResetTurns() {
        knownClients.values().forEach(cp -> cp.setTakeTurn(false));
    }

    private void processTurnStatus(long clientId, boolean didTakeTurn) {
        if (clientId < 1) {
            processResetTurns();
            return;
        }
        ClientPlayer cp = knownClients.get(clientId);
        cp.setTakeTurn(didTakeTurn);
        if (didTakeTurn) {
            System.out
                    .println(TextFX.colorize(String.format("%s finished their turn", cp.getClientName()), Color.CYAN));
            events.forEach(event -> {
                if (event instanceof IMessageEvents) {
                    ((IMessageEvents) event).onMessageReceive(Constants.GAME_EVENT_CHANNEL,
                            String.format("%s[%s] finished their turn", cp.getClientName(), cp.getClientId()));
                }
            });
        }
    }

    /**
     * 
     * @param x    rows
     * @param y    cols
     * @param seed random seed to ensure same random numbers occur
     */
    private void processGridDimension(int x, int y, long seed) {
        if (x > 0 && y > 0) {
            grid = new Grid(x, y, seed);
        } else {
            grid.reset();
            // added other cleanup
            knownClients.values().forEach(c -> {
                c.clearTowers();
                c.setEnergy(0);
            });
        }
        LoggerUtil.INSTANCE.info("Grid: " + grid);
        events.forEach(event -> {
            if (event instanceof IGridEvents) {
                ((IGridEvents) event).onReceiveGrid(grid);
            }
        });
    }

    private void processPhase(String phase) {
        currentPhase = Enum.valueOf(Phase.class, phase);
        System.out.println(TextFX.colorize("Current phase is " + currentPhase.name(), Color.YELLOW));
        events.forEach(event -> {
            if (event instanceof IPhaseEvent) {
                ((IPhaseEvent) event).onReceivePhase(currentPhase);
            }
        });
    }

    private void processResetReady() {
        knownClients.values().forEach(cp -> cp.setReady(false));
        System.out.println("Ready status reset for everyone");

    }

    private void processReadyStatus(long clientId, boolean isReady, boolean quiet) {
        if (!knownClients.containsKey(clientId)) {
            LoggerUtil.INSTANCE.severe(String.format("Received ready status [%s] for client id %s who is not known",
                    isReady ? "ready" : "not ready", clientId));
            return;
        }
        ClientPlayer cp = knownClients.get(clientId);
        cp.setReady(isReady);
        if (!quiet) {
            System.out.println(
                    String.format("%s[%s] is %s", cp.getClientName(), cp.getClientId(),
                            isReady ? "ready" : "not ready"));
            events.forEach(event -> {
                if (event instanceof IReadyEvent) {
                    ((IReadyEvent) event).onReceiveReady(clientId, isReady);
                }
            });
        }
    }

    private void processRoomsList(List<String> rooms, String message) {
        // invoke onReceiveRoomList callback
        events.forEach(event -> {
            if (event instanceof IRoomEvents) {
                ((IRoomEvents) event).onReceiveRoomList(rooms, message);
            }
        });

        if (rooms == null || rooms.size() == 0) {
            System.out.println(
                    TextFX.colorize("No rooms found matching your query",
                            Color.RED));
            return;
        }
        System.out.println(TextFX.colorize("Room Results:", Color.PURPLE));
        System.out.println(
                String.join("\n", rooms));

    }

    private void processDisconnect(long clientId, String clientName) {
        // invoke onClientDisconnect callback
        events.forEach(event -> {
            if (event instanceof IConnectionEvents) {
                ((IConnectionEvents) event).onClientDisconnect(clientId, clientName);
            }
        });
        System.out.println(
                TextFX.colorize(String.format("*%s disconnected*",
                        clientId == myData.getClientId() ? "You" : clientName),
                        Color.RED));
        if (clientId == myData.getClientId()) {
            closeServerConnection();
        }
    }

    private void processClientData(long clientId, String clientName) {
        if (myData.getClientId() == ClientPlayer.DEFAULT_CLIENT_ID) {
            myData.setClientId(clientId);
            myData.setClientName(clientName);
            // invoke onReceiveClientId callback
            events.forEach(event -> {
                if (event instanceof IConnectionEvents) {
                    ((IConnectionEvents) event).onReceiveClientId(clientId);
                }
            });
            // knownClients.put(cp.getClientId(), myData);// <-- this is handled later
        }
    }

    private void processMessage(long clientId, String message) {
        String name = knownClients.containsKey(clientId) ? knownClients.get(clientId).getClientName() : "Room";
        System.out.println(TextFX.colorize(String.format("%s: %s", name, message), Color.BLUE));
        // invoke onMessageReceive callback
        events.forEach(event -> {
            if (event instanceof IMessageEvents) {
                ((IMessageEvents) event).onMessageReceive(clientId, message);
            }
        });
    }

    private void processClientSync(long clientId, String clientName) {

        if (!knownClients.containsKey(clientId)) {
            // fix to have the correct reference set in knownClients
            if (clientId == myData.getClientId()) {
                knownClients.put(clientId, myData);
            } else {
                ClientPlayer cd = new ClientPlayer();
                cd.setClientId(clientId);
                cd.setClientName(clientName);
                knownClients.put(clientId, cd);
            }

            // invoke onSyncClient callback
            events.forEach(event -> {
                if (event instanceof IConnectionEvents) {
                    ((IConnectionEvents) event).onSyncClient(clientId, clientName);
                }
            });
        }
    }

    private void processRoomAction(long clientId, String clientName, String message, boolean isJoin) {

        if (isJoin && !knownClients.containsKey(clientId)) {
            // fix to have the correct reference set in knownClients
            if (clientId == myData.getClientId()) {
                knownClients.put(clientId, myData);
            } else {
                ClientPlayer cd = new ClientPlayer();
                cd.setClientId(clientId);
                cd.setClientName(clientName);
                knownClients.put(clientId, cd);
            }
            System.out.println(TextFX
                    .colorize(String.format("*%s[%s] joined the Room %s*", clientName, clientId, message),
                            Color.GREEN));
            // invoke onRoomJoin callback
            events.forEach(event -> {
                if (event instanceof IRoomEvents) {
                    ((IRoomEvents) event).onRoomAction(clientId, clientName, message, isJoin);
                }
            });
        } else if (!isJoin) {
            ClientPlayer removed = knownClients.remove(clientId);
            if (removed != null) {
                System.out.println(
                        TextFX.colorize(String.format("*%s[%s] left the Room %s*", clientName, clientId, message),
                                Color.YELLOW));
                // invoke onRoomJoin callback
                events.forEach(event -> {
                    if (event instanceof IRoomEvents) {
                        ((IRoomEvents) event).onRoomAction(clientId, clientName, message, isJoin);
                    }
                });
            }
            // clear our list
            if (clientId == myData.getClientId()) {
                knownClients.clear();
                // invoke onResetUserList()
                events.forEach(event -> {
                    if (event instanceof IConnectionEvents) {
                        ((IConnectionEvents) event).onResetUserList();
                    }
                });
            }
        }
    }
    // end payload processors

}
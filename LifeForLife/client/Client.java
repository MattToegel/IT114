package LifeForLife.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import LifeForLife.common.MyLogger;
import LifeForLife.common.PRHPayload;
import LifeForLife.common.Payload;
import LifeForLife.common.PayloadType;
import LifeForLife.common.ProjectilePayload;
import LifeForLife.common.RoomResultPayload;
import LifeForLife.common.Vector2;

//Enum Singleton: https://www.geeksforgeeks.org/advantages-and-disadvantages-of-using-enum-as-singleton-in-java/
public enum Client {
    INSTANCE;

    Socket server = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    boolean isRunning = false;
    private Thread fromServerThread;
    private String clientName = "";
    // private static Logger logger = Logger.getLogger(Client.class.getName());
    private static MyLogger logger = MyLogger.getLogger(Client.class.getName());
    private static List<IClientEvents> events = new ArrayList<IClientEvents>();

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

    public void addCallback(IClientEvents e) {
        events.add(e);
    }

    /**
     * Takes an ip address and a port to attempt a socket connection to a server.
     * 
     * @param address
     * @param port
     * @return true if connection was successful
     */
    public boolean connect(String address, int port, String username, IClientEvents callback) {
        // TODO validate
        this.clientName = username;
        addCallback(callback);
        try {
            server = new Socket(address, port);
            // channel to send to server
            out = new ObjectOutputStream(server.getOutputStream());
            // channel to listen to server
            in = new ObjectInputStream(server.getInputStream());
            logger.info("Client connected");
            listenForServerMessage();
            sendConnect();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected();
    }

    public void sendShoot() throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.SHOOT);
        send(p);
    }

    public void sendHeadingAndRotation(Vector2 heading, float rotation) throws IOException, NullPointerException {
        PRHPayload p = new PRHPayload();
        p.setHeading(heading);
        p.setRotation(rotation);
        send(p);
    }

    // Send methods TODO add other utility methods for sending here
    // NOTE: Can change this to protected or public if you plan to separate the
    // sendConnect action and the socket handshake
    public void sendReady() throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.READY);
        send(p);
    }

    public void sendCreateRoom(String room) throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CREATE_ROOM);
        p.setMessage(room);
        send(p);
    }

    public void sendJoinRoom(String room) throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.JOIN_ROOM);
        p.setMessage(room);
        send(p);
    }

    public void sendGetRooms(String query) throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.GET_ROOMS);
        p.setMessage(query);
        send(p);
    }

    private void sendConnect() throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CONNECT);
        p.setClientName(clientName);
        send(p);
    }

    public void sendDisconnect() throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.DISCONNECT);
        send(p);
    }

    public void sendMessage(String message) throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setMessage(message);
        p.setClientName(clientName);
        send(p);
    }

    // keep this private as utility methods should be the only Payload creators
    private synchronized void send(Payload p) throws IOException, NullPointerException {
        // logger.fine("Sending Payload: " + p);
        synchronized (out) {
            out.writeObject(p);// TODO force throw each
        }
        // logger.fine("Sent Payload: " + p);
    }

    // end send methods

    private void listenForServerMessage() {
        fromServerThread = new Thread() {
            @Override
            public void run() {
                try {
                    Payload fromServer;
                    logger.info("Listening for server messages");
                    // while we're connected, listen for strings from server
                    while (!server.isClosed() && !server.isInputShutdown()
                            && (fromServer = (Payload) in.readObject()) != null) {

                        logger.fine("Debug Info: " + fromServer);
                        processPayload(fromServer);

                    }
                    logger.info("Loop exited");
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!server.isClosed()) {
                        logger.info("Server closed connection");
                    } else {
                        logger.info("Connection closed");
                    }
                } finally {
                    close();
                    logger.info("Stopped listening to server input");
                }
            }
        };
        fromServerThread.start();// start the thread
    }

    private void processPayload(Payload p) {
        logger.fine("Received Payload: " + p);
        if (events == null && events.size() == 0) {
            logger.fine("Events not initialize/set" + p);
            return;
        }
        // TODO handle NPE
        if (p == null) {
            logger.severe("Payload is null!");
            return;
        }
        switch (p.getPayloadType()) {
            case CONNECT:
                events.forEach(e -> e.onClientConnect(p.getClientId(), p.getClientName(), p.getMessage()));
                break;
            case DISCONNECT:
                events.forEach(e -> e.onClientDisconnect(p.getClientId(), p.getClientName(), p.getMessage()));
                break;
            case MESSAGE:
                events.forEach(e -> e.onMessageReceive(p.getClientId(), p.getMessage()));
                break;
            case CLIENT_ID:
                events.forEach(e -> e.onReceiveClientId(p.getClientId()));
                break;
            case RESET_USER_LIST:
                events.forEach(e -> e.onResetUserList());
                break;
            case SYNC_CLIENT:
                events.forEach(e -> e.onSyncClient(p.getClientId(), p.getClientName()));
                break;
            case GET_ROOMS:
                events.forEach(e -> e.onReceiveRoomList(((RoomResultPayload) p).getRooms(), p.getMessage()));
                break;
            case JOIN_ROOM:
                events.forEach(e -> e.onRoomJoin(p.getMessage()));
                break;
            case READY:
                events.forEach(e -> e.onReceiveReady(p.getClientId()));
                break;
            case LIFE:
                events.forEach(e -> e.onReceiveLifeUpdate(p.getClientId(), p.getNumber()));
                break;
            case START:
                events.forEach(e -> e.onReceiveStart());
                break;
            case SYNC_POSITION_ROTATION:
                events.forEach(e -> e.onReceivePositionAndRotation(
                        p.getClientId(),
                        ((PRHPayload) p).getPosition(),
                        ((PRHPayload) p).getHeading(),
                        ((PRHPayload) p).getRotation()));
                break;
            case SYNC_PROJECTILE:
                ProjectilePayload pp = (ProjectilePayload) p;
                // logger.info("Projectile position: " + pp.getPosition());
                events.forEach(e -> e.onReceiveProjectileSync(pp.getClientId(), pp.getProjectileId(),
                        pp.getPosition(), pp.getHeading(), pp.getLife(), pp.getSpeed()));
                break;
            default:
                logger.warning("Unhandled payload type");
                break;

        }
    }

    private void close() {
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
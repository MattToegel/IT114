package Module6.Part8.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import Module6.Part8.common.Payload;
import Module6.Part8.common.PayloadType;
import Module6.Part8.common.RoomResultPayload;

//Enum Singleton: https://www.geeksforgeeks.org/advantages-and-disadvantages-of-using-enum-as-singleton-in-java/
public enum Client {
    INSTANCE;

    Socket server = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    boolean isRunning = false;
     private Thread fromServerThread;
    private String clientName = "";
    private static Logger logger = Logger.getLogger(Client.class.getName());
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
     * @return true if connection was successful
     */
    public boolean connect(String address, int port, String username, IClientEvents callback) {
        // TODO validate
        this.clientName = username;
        Client.events = callback;
        try {
            server = new Socket(address, port);
            // channel to send to server
            out = new ObjectOutputStream(server.getOutputStream());
            // channel to listen to server
            in = new ObjectInputStream(server.getInputStream());
            logger.log(Level.INFO, "Client connected");
            listenForServerMessage();
            sendConnect();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected();
    }

    // Send methods TODO add other utility methods for sending here
    // NOTE: Can change this to protected or public if you plan to separate the
    // sendConnect action and the socket handshake
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

    public void sendMessage(String message) throws IOException, NullPointerException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setMessage(message);
        p.setClientName(clientName);
        send(p);
    }

    // keep this private as utility methods should be the only Payload creators
    private void send(Payload p) throws IOException, NullPointerException {
        logger.log(Level.FINE, "Sending Payload: " + p);
        out.writeObject(p);//TODO force throw each
        logger.log(Level.INFO, "Sent Payload: " + p);
    }

    // end send methods

    private void listenForServerMessage() {
        fromServerThread = new Thread() {
            @Override
            public void run() {
                try {
                    Payload fromServer;
                    logger.log(Level.INFO, "Listening for server messages");
                    // while we're connected, listen for strings from server
                    while (!server.isClosed() && !server.isInputShutdown()
                            && (fromServer = (Payload) in.readObject()) != null) {

                        System.out.println("Debug Info: " + fromServer);
                        processPayload(fromServer);

                    }
                    System.out.println("Loop exited");
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!server.isClosed()) {
                        System.out.println("Server closed connection");
                    } else {
                        System.out.println("Connection closed");
                    }
                } finally {
                    close();
                    System.out.println("Stopped listening to server input");
                }
            }
        };
        fromServerThread.start();// start the thread
    }

    private void processPayload(Payload p) {
        logger.log(Level.FINE, "Received Payload: " + p);
        if (events == null) {
            logger.log(Level.FINER, "Events not initialize/set" + p);
            return;
        }
        switch (p.getPayloadType()) {
            case CONNECT:
                events.onClientConnect(p.getClientId(), p.getClientName(), p.getMessage());
                break;
            case DISCONNECT:
                events.onClientDisconnect(p.getClientId(), p.getClientName(), p.getMessage());
                break;
            case MESSAGE:
                events.onMessageReceive(p.getClientId(), p.getMessage());
                break;
            case CLIENT_ID:
                events.onReceiveClientId(p.getClientId());
                break;
            case RESET_USER_LIST:
                events.onResetUserList();
                break;
            case SYNC_CLIENT:
                events.onSyncClient(p.getClientId(), p.getClientName());
                break;
            case GET_ROOMS:
                events.onReceiveRoomList(((RoomResultPayload)p).getRooms(), p.getMessage());
                break;
            case JOIN_ROOM:
                events.onRoomJoin(p.getMessage());
                break;
            default:
                logger.log(Level.WARNING, "Unhandled payload type");
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
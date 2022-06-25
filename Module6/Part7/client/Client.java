package Module6.Part7.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import Module6.Part7.common.Payload;
import Module6.Part7.common.PayloadType;

//Enum Singleton: https://www.geeksforgeeks.org/advantages-and-disadvantages-of-using-enum-as-singleton-in-java/
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
    @Deprecated
    private boolean isConnection(String text) {
        // https://www.w3schools.com/java/java_regex.asp
        return text.matches(ipAddressPattern)
                || text.matches(localhostPattern);
    }

    @Deprecated
    private boolean isQuit(String text) {
        return text.equalsIgnoreCase("/quit");
    }

    @Deprecated
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
     * Controller for handling various text commands.
     * <p>
     * Add more here as needed
     * </p>
     * 
     * @param text
     * @return true if a text was a command or triggered a command
     */
    @Deprecated
    private boolean processCommand(String text) {
        if (isConnection(text)) {
            if (clientName.isBlank()) {
                System.out.println("You must set your name before you can connect via: /name your_name");
                return true;
            }
            // replaces multiple spaces with single space
            // splits on the space after connect (gives us host and port)
            // splits on : to get host as index 0 and port as index 1
            String[] parts = text.trim().replaceAll(" +", " ").split(" ")[1].split(":");
            connect(parts[0].trim(), Integer.parseInt(parts[1].trim()), null, null);
            return true;
        } else if (isQuit(text)) {
            isRunning = false;
            return true;
        } else if (isName(text)) {
            return true;
        }
        return false;
    }

    // Send methods TODO add other utility methods for sending here
    // NOTE: Can change this to protected or public if you plan to separate the
    // sendConnect action and the socket handshake
    private void sendConnect() throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CONNECT);
        p.setClientName(clientName);
        send(p);
    }

    public void sendMessage(String message) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setMessage(message);
        p.setClientName(clientName);
        send(p);
    }

    // keep this private as utility methods should be the only Payload creators
    private void send(Payload p) throws IOException {
        logger.log(Level.FINE, "Sending Payload: " + p);
        out.writeObject(p);
        logger.log(Level.INFO, "Sent Payload: " + p);
    }

    // end send methods
    @Deprecated
    private void listenForKeyboard() {
        inputThread = new Thread() {
            @Override
            public void run() {
                System.out.println("Listening for input");
                try (Scanner si = new Scanner(System.in);) {
                    String line = "";
                    isRunning = true;
                    while (isRunning) {
                        try {
                            System.out.println("Waiting for input");
                            line = si.nextLine();
                            if (!processCommand(line)) {
                                if (isConnected()) {
                                    if (line != null && line.trim().length() > 0) {
                                        sendMessage(line);
                                    }

                                } else {
                                    System.out.println("Not connected to server");
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Connection dropped");
                            break;
                        }
                    }
                    System.out.println("Exited loop");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    close();
                }
            }
        };
        inputThread.start();
    }

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
            default:
                logger.log(Level.WARNING, "Unhandled payload type");
                break;

        }
    }

    @Deprecated
    public void start() throws IOException {
        listenForKeyboard();
    }

    private void close() {
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

    /*
     * public static void main(String[] args) {
     * Client client = new Client();
     * 
     * try {
     * // if start is private, it's valid here since this main is part of the class
     * client.start();
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     * }
     */

}
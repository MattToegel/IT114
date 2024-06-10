package Module4.Part3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private int port = 3000;
    // connected clients
    // Use ConcurrentHashMap for thread-safe client management
    private final ConcurrentHashMap<Long, ServerThread> connectedClients = new ConcurrentHashMap<>();
    private boolean isRunning = true;

    private void start(int port) {
        this.port = port;
        // server listening
        System.out.println("Listening on port " + this.port);
        // Simplified client connection loop
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (isRunning) {
                System.out.println("Waiting for next client");
                Socket incomingClient = serverSocket.accept(); // blocking action, waits for a client connection
                System.out.println("Client connected");
                // wrap socket in a ServerThread, pass a callback to notify the Server they're initialized
                ServerThread sClient = new ServerThread(incomingClient, this, this::onClientInitialized);
                // start the thread (typically an external entity manages the lifecycle and we
                // don't have the thread start itself)
                sClient.start();
            }
        } catch (IOException e) {
            System.err.println("Error accepting connection");
            e.printStackTrace();
        } finally {
            System.out.println("Closing server socket");
        }
    }
    /**
     * Callback passed to ServerThread to inform Server they're ready to receive data
     * @param sClient
     */
    private void onClientInitialized(ServerThread sClient) {
        // add to connected clients list
        connectedClients.put(sClient.getClientId(), sClient);
        relay(String.format("*User[%s] connected*", sClient.getClientId()), null);
    }
    /**
     * Takes a ServerThread and removes them from the Server
     * Adding the synchronized keyword ensures that only one thread can execute
     * these methods at a time,
     * preventing concurrent modification issues and ensuring thread safety
     * 
     * @param client
     */
    protected synchronized void disconnect(ServerThread client) {
        long id = client.getClientId();
        client.disconnect();
        connectedClients.remove(id);
        // Improved logging with user ID
        relay("User[" + id + "] disconnected", null);
    }

    /**
     * Relays the message from the sender to all connectedClients
     * Internally calls processCommand and evaluates as necessary.
     * Note: Clients that fail to receive a message get removed from
     * connectedClients.
     * Adding the synchronized keyword ensures that only one thread can execute
     * these methods at a time,
     * preventing concurrent modification issues and ensuring thread safety
     * 
     * @param message
     * @param sender ServerThread (client) sending the message or null if it's a server-generated message
     */
    protected synchronized void relay(String message, ServerThread sender) {
        if (sender != null && processCommand(message, sender)) {

            return;
        }
        // let's temporarily use the thread id as the client identifier to
        // show in all client's chat. This isn't good practice since it's subject to
        // change as clients connect/disconnect
        // Note: any desired changes to the message must be done before this line
        String senderString = sender == null ? "Server" : String.format("User[%s]", sender.getClientId());
        final String formattedMessage = String.format("%s: %s", senderString, message);
        // end temp identifier

        // loop over clients and send out the message; remove client if message failed
        // to be sent
        // Note: this uses a lambda expression for each item in the values() collection,
        // it's one way we can safely remove items during iteration
        
        connectedClients.values().removeIf(client -> {
            boolean failedToSend = !client.send(formattedMessage);
            if (failedToSend) {
                System.out.println(String.format("Removing disconnected client[%s] from list", client.getClientId()));
                disconnect(client);
            }
            return failedToSend;
        });
    }

    /**
     * Attempts to see if the message is a command and process its action
     * 
     * @param message
     * @param sender
     * @return true if it was a command, false otherwise
     */
    private boolean processCommand(String message, ServerThread sender) {
        if(sender == null){
            return false;
        }
        System.out.println("Checking command: " + message);
        // disconnect
        if ("/disconnect".equalsIgnoreCase(message)) {
            ServerThread removedClient = connectedClients.get(sender.getClientId());
            if (removedClient != null) {
                disconnect(removedClient);
            }
            return true;
        }
        // add more "else if" as needed
        return false;
    }

    public static void main(String[] args) {
        System.out.println("Server Starting");
        Server server = new Server();
        int port = 3000;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // can ignore, will either be index out of bounds or type mismatch
            // will default to the defined value prior to the try/catch
        }
        server.start(port);
        System.out.println("Server Stopped");
    }
}

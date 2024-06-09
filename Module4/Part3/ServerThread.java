package Module4.Part3;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

/**
 * A server-side representation of a single client
 */
public class ServerThread extends Thread {
    private Socket client; // communication directly to "my" client
    private boolean isRunning = false; //control variable to stop this thread
    private ObjectOutputStream out; //exposed here for send()
    private Server server;// ref to our server so we can call methods on it
    // more easily

    /**
     * A wrapper method so we don't need to keep typing out the long/complex sysout line inside
     * @param message
     */
    private void info(String message) {
        System.out.println(String.format("Thread[%s]: %s", getClientId(), message));
    }

    /**
     * Wraps the Socket connection and takes a Server reference
     * @param myClient
     * @param server
     */
    protected ServerThread(Socket myClient, Server server) {
        Objects.requireNonNull(myClient, "Client socket cannot be null");
        Objects.requireNonNull(server, "Server cannot be null");
        info("ServerThread created");
        // get communication channels to single client
        this.client = myClient;
        this.server = server;

    }
    public long getClientId(){
        return threadId();
    }
    /**
     * One of the two ways to get this to exit the listen loop
     */
    protected void disconnect() {
        info("Thread being disconnected by server");
        isRunning = false;
        this.interrupt(); // breaks out of blocking read in the run() method
        cleanup();
    }

    protected boolean send(String message) {
        // added a boolean so we can see if the send was successful
        try {
            out.writeObject(message);
            return true;
        } catch (IOException e) {
            info("Error sending message to client (most likely disconnected)");
            // comment this out to inspect the stack trace
            // e.printStackTrace();
            cleanup();
            return false;
        }
    }

    @Override
    public void run() {
        info("Thread starting");
        try (ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());) {
            this.out = out;
            isRunning = true;
            String fromClient;
            /**
             * isRunning is a flag to let us manage the loop exit condition
             * fromClient (in.readObject()) is a blocking method that waits until data is received
             *  - null would likely mean a disconnect so we use a "set and check" logic to alternatively exit the loop
             */
            while (isRunning) {
                try{
                    fromClient = (String) in.readObject(); // blocking method
                    if (fromClient != null) {
                        info("Received from my client: " + fromClient);
                        server.relay(fromClient, this);
                    }
                    else{
                        throw new IOException("Connection interrupted"); // Specific exception for a clean break
                    }
                }
                catch (IOException e) {
                    if (Thread.currentThread().isInterrupted()) {
                        info("Thread interrupted during read (likely from the disconnect() method)");
                        break;
                    }
                    info("IO exception while reading from client");
                    e.printStackTrace();
                    break;
                }
            } // close while loop
        } catch (Exception e) {
            // happens when client disconnects
            info("General Exception");
            e.printStackTrace();
            info("My Client disconnected");
        } finally {
            isRunning = false;
            info("Exited thread loop. Cleaning up connection");
            cleanup();
        }
    }

    private void cleanup() {
        info("ServerThread cleanup() start");
        try {
            client.close();
        } catch (IOException e) {
            info("Client already closed");
        }
        info("ServerThread cleanup() end");
    }
}

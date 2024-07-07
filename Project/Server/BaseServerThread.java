package Project.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Project.Common.LoggerUtil;
import Project.Common.Payload;

/**
 * Base class the handles the underlying connection between Client and Server-side
 */
public abstract class BaseServerThread extends Thread {
    
    protected boolean isRunning = false; // control variable to stop this thread
    protected ObjectOutputStream out; // exposed here for send()
    protected Socket client; // communication directly to "my" client
    
    /**
     * A wrapper method so we don't need to keep typing out the long/complex sysout
     * line inside
     * 
     * @param message
     */
    protected abstract void info(String message);

    /**
     * Triggered when object is fully initialized
     */
    protected abstract void onInitialized();

    /**
     * Receives a Payload and passes data to proper handler
     * @param payload
     */
    protected abstract void processPayload(Payload payload);

    /**
     * Sends the payload over the socket
     * 
     * @param payload
     * @return true if no errors were encountered
     */
    protected boolean send(Payload payload) {
        if(!isRunning){
            return true;
        }
        try {
            LoggerUtil.INSTANCE.info("Sending Payload: " + payload);
            out.writeObject(payload);
            out.flush();
            return true;
        } catch (IOException e) {
            info("Error sending message to client (most likely disconnected)");
            // comment this out to inspect the stack trace
            // e.printStackTrace();
            cleanup();
            return false;
        }
    }
    
    /**
     * One of the two ways to get this to exit the listen loop
     */
    protected void disconnect() {
        info("Thread being disconnected by server");
        isRunning = false;
        this.interrupt(); // breaks out of blocking read in the run() method
        cleanup(); // good practice to ensure data is written out immediately
    }

    @Override
    public void run() {
        info("Thread starting");
        try (ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());) {
            this.out = out;
            isRunning = true;
            //onInitialized();
            Payload fromClient;
            /**
             * isRunning is a flag to let us manage the loop exit condition
             * fromClient (in.readObject()) is a blocking method that waits until data is received
             *  - null would likely mean a disconnect so we use a "set and check" logic to alternatively exit the loop
             */
            while (isRunning) {
                try{
                    fromClient = (Payload) in.readObject(); // blocking method
                    if (fromClient != null) {
                        info("Received from my client: " + fromClient);
                        processPayload(fromClient);
                    }
                    else{
                        throw new IOException("Connection interrupted"); // Specific exception for a clean break
                    }
                }
                catch (ClassCastException | ClassNotFoundException cce) {
                    LoggerUtil.INSTANCE.severe("Error reading object as specified type: ", cce);
                }
                catch (IOException e) {
                    if (Thread.currentThread().isInterrupted()) {
                        info("Thread interrupted during read (likely from the disconnect() method)");
                        break;
                    }
                    LoggerUtil.INSTANCE.severe("IO exception while reading from client",e);
                    break;
                }
            } // close while loop
        } catch (Exception e) {
            // happens when client disconnects
            LoggerUtil.INSTANCE.severe("General Exception: ", e);
            info("My Client disconnected");
        } finally {
            isRunning = false;
            info("Exited thread loop. Cleaning up connection");
            cleanup();
        }
    }

    protected void cleanup() {
        info("ServerThread cleanup() start");
        try {
            client.close();
        } catch (IOException e) {
            info("Client already closed");
        }
        
        info("ServerThread cleanup() end");
    }
}

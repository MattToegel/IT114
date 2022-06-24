package Module6.Part6.client;

public interface IClientEvents {
    /**
     * Triggered when a client connects
     * @param clientName
     * @param message
     */
    void onClientConnect(String clientName, String message);
    /**
     * Triggered when a client disconnects
     * @param clientName
     * @param message
     */
    void onClientDisconnect(String clientName, String message);
    /**
     * Triggered when a message is received
     * @param clientName
     * @param message
     */
    void onMessageReceive(String clientName, String message);
}

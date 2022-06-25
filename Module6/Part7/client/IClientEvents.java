package Module6.Part7.client;

public interface IClientEvents {
    /**
     * Triggered when a client connects
     * @param clientName
     * @param message
     */
    void onClientConnect(long id, String clientName, String message);
    /**
     * Triggered when a client disconnects
     * @param clientName
     * @param message
     */
    void onClientDisconnect(long id, String clientName, String message);
    /**
     * Triggered when a message is received
     * @param clientName
     * @param message
     */
    void onMessageReceive(long id, String message);

    /**
     * Received the server-given id for our client reference
     * @param id
     */
    void onReceiveClientId(long id);

    /**
     * Used to sync existing clients
     * @param id
     * @param clientName
     */
    void onSyncClient(long id, String clientName);

    /**
     * Triggered when we need to clear the user list, likely during a room transition
     */
    void onResetUserList();
}

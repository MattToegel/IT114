package Project.Client.Interfaces;

/**
 * Interface for handling client connection events.
 */
public interface IConnectionEvents extends IClientEvents {
    // no need for onClientConnect() as IRoomEvents handles it as joining

    /**
     * Triggered when a client disconnects.
     *
     * @param id         The client ID.
     * @param clientName The client name.
     */
    void onClientDisconnect(long id, String clientName);

    /**
     * Received the server-given ID for our client reference.
     *
     * @param id The client ID.
     */
    void onReceiveClientId(long id);

    /**
     * Used to sync existing clients.
     *
     * @param id         The client ID.
     * @param clientName The client name.
     */
    void onSyncClient(long id, String clientName);

    /**
     * Triggered when we need to clear the user list, likely during a room
     * transition.
     */
    void onResetUserList();
}

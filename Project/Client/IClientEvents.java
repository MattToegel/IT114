package Project.Client;

import java.util.List;

public interface IClientEvents {
    /**
     * Triggered when a client connects
     * 
     * @param clientName
     * @param message
     */
    void onClientConnect(long id, String clientName, String message);

    /**
     * Triggered when a client disconnects
     * 
     * @param clientName
     * @param message
     */
    void onClientDisconnect(long id, String clientName, String message);

    /**
     * Triggered when a message is received
     * 
     * @param clientName
     * @param message
     */
    void onMessageReceive(long id, String message);

    /**
     * Received the server-given id for our client reference
     * 
     * @param id
     */
    void onReceiveClientId(long id);

    /**
     * Used to sync existing clients
     * 
     * @param id
     * @param clientName
     */
    void onSyncClient(long id, String clientName);

    /**
     * Triggered when we need to clear the user list, likely during a room
     * transition
     */
    void onResetUserList();

    /**
     * Received Room list from server
     * 
     * @param rooms   list of rooms or null if error
     * @param message a message related to the action, may be null (usually if
     *                rooms.length > 0)
     */
    void onReceiveRoomList(List<String> rooms, String message);

    /**
     * Receives the Room name when the client is added to the Room
     * 
     * @param roomName
     */
    void onRoomJoin(String roomName);
}

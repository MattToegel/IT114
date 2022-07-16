package LifeForLife.client;

import LifeForLife.common.Vector2;

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
    void onReceiveRoomList(String[] rooms, String message);

    /**
     * Receives the Room name when the client is added to the Room
     * 
     * @param roomName
     */
    void onRoomJoin(String roomName);

    /**
     * Receives a player's ready state
     * 
     * @param clientId
     */
    void onReceiveReady(long clientId);

    /**
     * Receives a player's current life value
     * 
     * @param clientId
     * @param currentLife
     */
    void onReceiveLifeUpdate(long clientId, long currentLife);

    /**
     * Receives the game start event from the server
     */
    void onReceiveStart();

    /**
     * Receives transformation related data for a specific player
     * 
     * @param clientId
     * @param position
     * @param heading
     * @param rotation
     */
    void onReceivePositionAndRotation(long clientId, Vector2 position, Vector2 heading, float rotation);
    /**
     * Receives projectile updates to add/update on the client-side
     * @param clientId
     * @param projectileId
     * @param position
     * @param heading
     * @param life
     * @param speed
     */
    void onReceiveProjectileSync(long clientId, long projectileId, Vector2 position, Vector2 heading, long life, int speed);
}

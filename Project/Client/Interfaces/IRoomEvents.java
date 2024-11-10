package Project.Client.Interfaces;

import java.util.List;

/**
 * Interface for handling room events.
 */
public interface IRoomEvents extends IClientEvents {
    /**
     * Received room list from server.
     *
     * @param rooms   List of rooms or null if error.
     * @param message A message related to the action, may be null (usually if
     *                rooms.length > 0).
     */
    void onReceiveRoomList(List<String> rooms, String message);

    /**
     * Receives the room name when the client is added to the room.
     *
     * @param roomName The room name.
     */
    void onRoomAction(long clientId, String clientName, String roomName, boolean isJoin);
}

package Popularity.Server;

import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import Popularity.Common.PayloadType;
import Popularity.Common.Phase;
import Popularity.Common.PointsPayload;
import Popularity.Common.ReadyPayload;
import Popularity.Common.RoomResultsPayload;
import Popularity.Common.TimerPayload;
import Popularity.Common.TimerType;
import Popularity.Common.Payload;

import Popularity.Common.ConnectionPayload;
import Popularity.Common.Constants;
import Popularity.Common.LoggerUtil;

/**
 * A server-side representation of a single client.
 * This class is more about the data and abstracted communication
 */
public class ServerThread extends BaseServerThread {
    public static final long DEFAULT_CLIENT_ID = -1;
    private Room currentRoom;
    private long clientId;
    private String clientName;
    private Consumer<ServerThread> onInitializationComplete; // callback to inform when this object is ready

    /**
     * Wraps the Socket connection and takes a Server reference and a callback
     * 
     * @param myClient
     * @param server
     * @param onInitializationComplete method to inform listener that this object is
     *                                 ready
     */
    protected ServerThread(Socket myClient, Consumer<ServerThread> onInitializationComplete) {
        Objects.requireNonNull(myClient, "Client socket cannot be null");
        Objects.requireNonNull(onInitializationComplete, "callback cannot be null");
        info("ServerThread created");
        // get communication channels to single client
        this.client = myClient;
        this.clientId = ServerThread.DEFAULT_CLIENT_ID;// this is updated later by the server
        this.onInitializationComplete = onInitializationComplete;

    }

    public void setClientName(String name) {
        if (name == null) {
            throw new NullPointerException("Client name can't be null");
        }
        this.clientName = name;
        onInitialized();
    }

    public String getClientName() {
        return clientName;
    }

    public long getClientId() {
        return this.clientId;
    }

    protected Room getCurrentRoom() {
        return this.currentRoom;
    }

    protected void setCurrentRoom(Room room) {
        if (room == null) {
            throw new NullPointerException("Room argument can't be null");
        }
        currentRoom = room;
    }

    @Override
    protected void onInitialized() {
        onInitializationComplete.accept(this); // Notify server that initialization is complete
    }

    @Override
    protected void info(String message) {
        LoggerUtil.INSTANCE.info(String.format("ServerThread[%s(%s)]: %s", getClientName(), getClientId(), message));
    }

    @Override
    protected void cleanup() {
        currentRoom = null;
        super.cleanup();
    }

    @Override
    protected void disconnect() {
        // sendDisconnect(clientId, clientName);
        super.disconnect();
    }

    // handle received message from the Client
    @Override
    protected void processPayload(Payload payload) {
        try {
            switch (payload.getPayloadType()) {
                case CLIENT_CONNECT:
                    ConnectionPayload cp = (ConnectionPayload) payload;
                    setClientName(cp.getClientName());
                    break;
                case MESSAGE:
                    currentRoom.sendMessage(this, payload.getMessage());
                    break;
                case ROOM_CREATE:
                    currentRoom.handleCreateRoom(this, payload.getMessage());
                    break;
                case ROOM_JOIN:
                    currentRoom.handleJoinRoom(this, payload.getMessage());
                    break;
                case ROOM_LIST:
                    currentRoom.handleListRooms(this, payload.getMessage());
                    break;
                case DISCONNECT:
                    currentRoom.disconnect(this);
                    break;
                case READY:
                    // no data needed as the intent will be used as the trigger
                    try {
                        // cast to GameRoom as the subclass will handle all Game logic
                        ((GameRoom) currentRoom).handleReady(this);
                    } catch (Exception e) {
                        sendMessage("You must be in a GameRoom to do the ready check");
                    }
                    break;
                case EXAMPLE_TURN:
                    try {
                        // cast to GameRoom as the subclass will handle all Game logic
                        ((GameRoom) currentRoom).handleTurn(this);
                    } catch (Exception e) {
                        sendMessage("You must be in a GameRoom to do the example turn");
                    }
                default:
                    break;
            }
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Could not process Payload: " + payload, e);

        }
    }

    // send methods specific to non-chatroom
    /**
     * Syncs a specific client's points
     * 
     * @param clientId
     * @param points
     * @return
     */
    public boolean sendPointsUpdate(long clientId, int points) {
        PointsPayload rp = new PointsPayload();
        rp.setPoints(points);
        rp.setClientId(clientId);
        return send(rp);
    }

    /**
     * Syncs the current time of a specific TimerType
     * 
     * @param timerType
     * @param time
     * @return
     */
    public boolean sendCurrentTime(TimerType timerType, int time) {
        TimerPayload tp = new TimerPayload();
        tp.setTime(time);
        tp.setTimerType(timerType);
        return send(tp);
    }

    /**
     * Sends a message as a GAME_EVENT for non-chat UI
     * 
     * @param str
     * @return
     */
    public boolean sendGameEvent(String str) {
        return sendMessage(Constants.GAME_EVENT_CHANNEL, str);
    }

    /**
     * Syncs a specific client's turn status
     * 
     * @param clientId
     * @param didTakeTurn
     * @return
     */
    public boolean sendTurnStatus(long clientId, boolean didTakeTurn) {
        ReadyPayload rp = new ReadyPayload();
        rp.setPayloadType(PayloadType.EXAMPLE_TURN);
        rp.setReady(didTakeTurn);
        rp.setClientId(clientId);
        return send(rp);
    }

    /**
     * Syncs the currnet phase to the client
     * 
     * @param phase
     * @return
     */
    public boolean sendCurrentPhase(Phase phase) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.PHASE);
        p.setMessage(phase.name());
        return send(p);
    }

    /**
     * Sends a trigger to have the client-side reset their list of READY state
     * 
     * @return
     */
    public boolean sendResetReady() {
        ReadyPayload rp = new ReadyPayload();
        rp.setPayloadType(PayloadType.RESET_READY);
        return send(rp);
    }

    public boolean sendReadyStatus(long clientId, boolean isReady) {
        return sendReadyStatus(clientId, isReady, false);
    }

    /**
     * Sync ready status of client id
     * 
     * @param clientId who
     * @param isReady  ready or not
     * @param quiet    silently mark ready
     * @return
     */
    public boolean sendReadyStatus(long clientId, boolean isReady, boolean quiet) {
        ReadyPayload rp = new ReadyPayload();
        rp.setClientId(clientId);
        rp.setReady(isReady);
        if (quiet) {
            rp.setPayloadType(PayloadType.SYNC_READY);
        }
        return send(rp);
    }
    // send methods to pass data back to the Client

    public boolean sendRooms(List<String> rooms) {
        RoomResultsPayload rrp = new RoomResultsPayload();
        rrp.setRooms(rooms);
        return send(rrp);
    }

    public boolean sendClientSync(long clientId, String clientName) {
        ConnectionPayload cp = new ConnectionPayload();
        cp.setClientId(clientId);
        cp.setClientName(clientName);
        cp.setConnect(true);
        cp.setPayloadType(PayloadType.SYNC_CLIENT);
        return send(cp);
    }

    /**
     * Overload of sendMessage used for server-side generated messages
     * 
     * @param message
     * @return @see {@link #send(Payload)}
     */
    public boolean sendMessage(String message) {
        return sendMessage(ServerThread.DEFAULT_CLIENT_ID, message);
    }

    /**
     * Sends a message with the author/source identifier
     * 
     * @param senderId
     * @param message
     * @return @see {@link #send(Payload)}
     */
    public boolean sendMessage(long senderId, String message) {
        Payload p = new Payload();
        p.setClientId(senderId);
        p.setMessage(message);
        p.setPayloadType(PayloadType.MESSAGE);
        return send(p);
    }

    /**
     * Tells the client information about a client joining/leaving a room
     * 
     * @param clientId   their unique identifier
     * @param clientName their name
     * @param room       the room
     * @param isJoin     true for join, false for leaivng
     * @return success of sending the payload
     */
    public boolean sendRoomAction(long clientId, String clientName, String room, boolean isJoin) {
        ConnectionPayload cp = new ConnectionPayload();
        cp.setPayloadType(PayloadType.ROOM_JOIN);
        cp.setConnect(isJoin); // <-- determine if join or leave
        cp.setMessage(room);
        cp.setClientId(clientId);
        cp.setClientName(clientName);
        return send(cp);
    }

    /**
     * Tells the client information about a disconnect (similar to leaving a room)
     * 
     * @param clientId   their unique identifier
     * @param clientName their name
     * @return success of sending the payload
     */
    public boolean sendDisconnect(long clientId, String clientName) {
        ConnectionPayload cp = new ConnectionPayload();
        cp.setPayloadType(PayloadType.DISCONNECT);
        cp.setConnect(false);
        cp.setClientId(clientId);
        cp.setClientName(clientName);
        return send(cp);
    }

    /**
     * Sends (and sets) this client their id (typically when they first connect)
     * 
     * @param clientId
     * @return success of sending the payload
     */
    public boolean sendClientId(long clientId) {
        this.clientId = clientId;
        ConnectionPayload cp = new ConnectionPayload();
        cp.setPayloadType(PayloadType.CLIENT_ID);
        cp.setConnect(true);
        cp.setClientId(clientId);
        cp.setClientName(clientName);
        return send(cp);
    }

    // end send methods
}

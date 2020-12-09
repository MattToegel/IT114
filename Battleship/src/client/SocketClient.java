package client;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import server.Payload;
import server.PayloadType;

public enum SocketClient {
    INSTANCE; // see https://dzone.com/articles/java-singletons-using-enum "Making Singletons
	      // with Enum"

    private static Socket server;
    private static Thread fromServerThread;
    private static Thread clientThread;
    private static String clientName;
    private static ObjectOutputStream out;
    private final static Logger log = Logger.getLogger(SocketClient.class.getName());
    private static List<Event> events = new ArrayList<Event>();// change from event to list<event>

    private Payload buildMessage(String message) {
	Payload payload = new Payload();
	payload.setPayloadType(PayloadType.MESSAGE);
	payload.setClientName(clientName);
	payload.setMessage(message);
	return payload;
    }

    private Payload buildConnectionStatus(String name, boolean isConnect) {
	Payload payload = new Payload();
	if (isConnect) {
	    payload.setPayloadType(PayloadType.CONNECT);
	}
	else {
	    payload.setPayloadType(PayloadType.DISCONNECT);
	}
	payload.setClientName(name);
	return payload;
    }

    private void sendPayload(Payload p) {
	try {
	    System.out.println("Sending: " + p);
	    out.writeObject(p);
	}
	catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private void listenForServerMessage(ObjectInputStream in) {
	if (fromServerThread != null) {
	    log.log(Level.INFO, "Server Listener is likely already running");
	    return;
	}
	// Thread to listen for responses from server so it doesn't block main thread
	fromServerThread = new Thread() {
	    @Override
	    public void run() {
		try {
		    Payload fromServer;
		    // while we're connected, listen for Payloads from server
		    while (!server.isClosed() && (fromServer = (Payload) in.readObject()) != null) {
			System.out.println("Received from SERVER: " + fromServer);
			processPayload(fromServer);
		    }
		}
		catch (Exception e) {
		    if (!server.isClosed()) {
			e.printStackTrace();
			log.log(Level.INFO, "Server closed connection");
		    }
		    else {
			log.log(Level.INFO, "Connection closed");
		    }
		}
		finally {
		    close();
		    log.log(Level.INFO, "Stopped listening to server input");
		}
	    }
	};
	fromServerThread.start();// start the thread
    }

    private void sendOnClientConnect(String name, String message) {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onClientConnect(name, message);
	    }
	}
    }

    private void sendOnClientDisconnect(String name, String message) {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onClientDisconnect(name, message);
	    }
	}
    }

    private void sendOnMessage(String name, String message) {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onMessageReceive(name, message);
	    }
	}
    }

    private void sendOnChangeRoom() {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onChangeRoom();
	    }
	}
    }

    private void sendSyncDirection(String clientName, Point direction) {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onSyncDirection(clientName, direction);
	    }
	}
    }

    private void sendSyncPosition(String clientName, Point position) {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onSyncPosition(clientName, position);
	    }
	}
    }

    private void sendRoom(String roomName) {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onGetRoom(roomName);
	    }
	}
    }

    private void sendSize(Point p) {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onResize(p);
	    }
	}
    }

    private void sendChair(String name, Point position, Point dimension, String sitter) {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onGetChair(name, position, dimension, sitter);

	    }
	}
    }

    private void sendResetChairs() {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onResetChairs();
	    }
	}
    }

    private void sendTicket(String name, Point position, Point dimension, String holder) {// boolean flag) {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onGetTicket(name, position, dimension, holder);
	    }
	}
    }

    private void sendResetTickets() {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onResetTickets();
	    }
	}
    }

    private void sendCountdown(String message, int duration) {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onSetCountdown(message, duration);
	    }
	}
    }

    private void sendToggleLock(boolean isLocked) {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onToggleLock(isLocked);
	    }
	}
    }

    private void sendUpdateCollector(int chairIndex) {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onUpdateTicketCollector(chairIndex);
	    }
	}
    }

    private void sendKick(String clientName) {
	Iterator<Event> iter = events.iterator();
	while (iter.hasNext()) {
	    Event e = iter.next();
	    if (e != null) {
		e.onPlayerKicked(clientName);
	    }
	}
    }

    /***
     * Determine any special logic for different PayloadTypes
     * 
     * @param p
     */
    private void processPayload(Payload p) {

	switch (p.getPayloadType()) {
	case CONNECT:
	    sendOnClientConnect(p.getClientName(), p.getMessage());
	    break;
	case DISCONNECT:
	    sendOnClientDisconnect(p.getClientName(), p.getMessage());
	    break;
	case MESSAGE:
	    sendOnMessage(p.getClientName(), p.getMessage());
	    break;
	case CLEAR_PLAYERS:
	    sendOnChangeRoom();
	    break;
	case SYNC_DIRECTION:
	    sendSyncDirection(p.getClientName(), p.getPoint());
	    break;
	case SYNC_POSITION:
	    sendSyncPosition(p.getClientName(), p.getPoint());
	    break;
	case GET_ROOMS:
	    // reply from ServerThread
	    sendRoom(p.getMessage());
	    break;
	case SYNC_GAME_SIZE:
	    sendSize(p.getPoint());
	    break;
	case SYNC_CHAIR:
	    // we'll use null to reset and not null to add
	    if (p.getMessage() != null) {
		sendChair(p.getMessage(), p.getPoint(), p.getPoint2(), p.getClientName());
	    }
	    else {
		sendResetChairs();
	    }
	    break;
	case SYNC_TICKET:
	    // we'll use null to reset and not null to add
	    if (p.getMessage() != null) {
		// changed from flag to passing client name
		sendTicket(p.getMessage(), p.getPoint(), p.getPoint2(), p.getClientName());// p.getFlag());
	    }
	    else {
		sendResetTickets();
	    }
	    break;
	case SET_COUNTDOWN:
	    sendCountdown(p.getMessage(), p.getNumber());
	    break;
	case TOGGLE_LOCK:
	    sendToggleLock(p.getFlag());
	    break;
	case UPDATE_COLLECTOR:
	    sendUpdateCollector(p.getNumber());
	    break;
	case KICK_PLAYER:
	    sendKick(p.getClientName());
	    break;
	default:
	    log.log(Level.WARNING, "unhandled payload on client" + p);
	    break;

	}
    }

    // TODO Start public methods here

    public void registerCallbackListener(Event e) {
	events.add(e);
	log.log(Level.INFO, "Attached listener");
    }

    public void removeCallbackListener(Event e) {
	events.remove(e);
    }

    public boolean connectAndStart(String address, String port) throws IOException {
	if (connect(address, port)) {
	    return start();
	}
	return false;
    }

    public boolean connect(String address, String port) {
	try {
	    server = new Socket(address, Integer.parseInt(port));
	    log.log(Level.INFO, "Client connected");
	    return true;
	}
	catch (UnknownHostException e) {
	    e.printStackTrace();
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
	return false;
    }

    public void setUsername(String username) {
	clientName = username;
	sendPayload(buildConnectionStatus(clientName, true));
    }

    public void sendMessage(String message) {
	sendPayload(buildMessage(message));
    }

    public void sendCreateRoom(String room) {
	Payload p = new Payload();
	p.setPayloadType(PayloadType.CREATE_ROOM);
	p.setMessage(room);
	sendPayload(p);
    }

    public void sendJoinRoom(String room) {
	Payload p = new Payload();
	p.setPayloadType(PayloadType.JOIN_ROOM);
	p.setMessage(room);
	sendPayload(p);
    }

    public void sendGetRooms(String query) {
	Payload p = new Payload();
	p.setPayloadType(PayloadType.GET_ROOMS);
	p.setMessage(query);
	sendPayload(p);
    }

    /**
     * Sends desired to change direction to server
     * 
     * @param dir
     */
    public void syncDirection(Point dir) {
	Payload p = new Payload();
	// no need to add clientName here since ServerThread has the info
	// so let's save a few bytes
	p.setPayloadType(PayloadType.SYNC_DIRECTION);
	p.setPoint(dir);
	sendPayload(p);
    }

    public void syncPickupTicket() {
	Payload p = new Payload();
	p.setPayloadType(PayloadType.PICKUP_TICKET);
	sendPayload(p);
    }

    /**
     * we won't be syncing position from the client since our server is the one
     * that'll do it so creating this unused method as a reminder not to use/make it
     */
    @Deprecated
    public void syncPosition() {
	log.log(Level.SEVERE, "My sample doesn't use this");
    }

    public boolean start() throws IOException {
	if (server == null) {
	    log.log(Level.WARNING, "Server is null");
	    return false;
	}
	if (clientThread != null && clientThread.isAlive()) {
	    log.log(Level.SEVERE, "Client thread is already active");
	    return false;
	}
	if (clientThread != null) {
	    clientThread.interrupt();
	    clientThread = null;
	}
	log.log(Level.INFO, "Client Started");
	clientThread = new Thread() {
	    @Override
	    public void run() {

		// listen to console, server in, and write to server out
		try (ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(server.getInputStream());) {
		    SocketClient.out = out;

		    // starts new thread
		    listenForServerMessage(in);

		    // Keep main thread alive until the socket is closed
		    // initialize/do everything before this line
		    // (Without this line the program would stop after the first message
		    while (!server.isClosed()) {
			Thread.sleep(50);
		    }
		    log.log(Level.INFO, "Client Thread stopping");
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
		finally {
		    close();
		}
	    }
	};
	clientThread.start();
	return true;
    }

    public void close() {
	if (server != null && !server.isClosed()) {
	    try {
		server.close();
		log.log(Level.INFO, "Closed Socket");
	    }
	    catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
}

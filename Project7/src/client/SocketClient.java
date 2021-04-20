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

import common.PayloadType;
import server.Payload;

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
		} else {
			payload.setPayloadType(PayloadType.DISCONNECT);
		}
		payload.setClientName(name);
		return payload;
	}

	private void sendPayload(Payload p) {
		try {
			out.writeObject(p);
		} catch (IOException e) {
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
						processPayload(fromServer);
					}
				} catch (Exception e) {
					if (!server.isClosed()) {
						e.printStackTrace();
						log.log(Level.INFO, "Server closed connection");
					} else {
						log.log(Level.INFO, "Connection closed");
					}
				} finally {
					close();
					log.log(Level.INFO, "Stopped listening to server input");
				}
			}
		};
		fromServerThread.start();// start the thread
	}

	private void receiveClientConnect(String name, String message) {
		Iterator<Event> iter = events.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if (e != null) {
				e.onClientConnect(name, message);
			}
		}
	}

	private void receiveClientDisconnect(String name, String message) {
		Iterator<Event> iter = events.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if (e != null) {
				e.onClientDisconnect(name, message);
			}
		}
	}

	private void receiveMessage(String name, String message) {
		Iterator<Event> iter = events.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if (e != null) {
				e.onMessageReceive(name, message);
			}
		}
	}

	private void receiveRoomChange() {
		Iterator<Event> iter = events.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if (e != null) {
				e.onChangeRoom();
			}
		}
	}

	private void receiveSyncDirection(String clientName, Point direction) {
		Iterator<Event> iter = events.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if (e != null) {
				e.onSyncDirection(clientName, direction);
			}
		}
	}

	private void receiveSyncPosition(String clientName, Point position) {
		Iterator<Event> iter = events.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if (e != null) {
				e.onSyncPosition(clientName, position);
			}
		}
	}

	private void receiveRoom(String roomName) {
		Iterator<Event> iter = events.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if (e != null) {
				e.onGetRoom(roomName);
			}
		}
	}

	private void receiveSize(Point p) {
		Iterator<Event> iter = events.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if (e != null) {
				e.onResize(p);
			}
		}
	}

	private void receiveShipPlacement(int shipType, int shipId, int x, int y, int health) {
		Iterator<Event> iter = events.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if (e != null) {
				e.onShipPlaced(shipType, shipId, x, y, health);
			}
		}
	}

	private void receiveAttackStatus(int markerType, int x, int y) {
		Iterator<Event> iter = events.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if (e != null) {
				e.onAttackStatus(markerType, x, y);
			}
		}
	}
	private void receiveShipStatus(int shipId, int life) {
		Iterator<Event> iter = events.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if (e != null) {
				e.onShipStatus(shipId, life);
			}
		}
	}
	
	private void receiveAttackRadius(int x, int y, int radius) {
		Iterator<Event> iter = events.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if (e != null) {
				e.onAttackRadius(x, y, radius);
			}
		}
	}
	
	private void receiveCanAttack(String client, int attacks) {
		Iterator<Event> iter = events.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if (e != null) {
				e.onCanAttack(client, attacks);
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
			receiveClientConnect(p.getClientName(), p.getMessage());
			break;
		case DISCONNECT:
			receiveClientDisconnect(p.getClientName(), p.getMessage());
			break;
		case MESSAGE:
			receiveMessage(p.getClientName(), p.getMessage());
			break;
		case CLEAR_PLAYERS:
			receiveRoomChange();
			break;
		case SYNC_DIRECTION:
			receiveSyncDirection(p.getClientName(), p.getPoint());
			break;
		case SYNC_POSITION:
			receiveSyncPosition(p.getClientName(), p.getPoint());
			break;
		case GET_ROOMS:
			// reply from ServerThread
			receiveRoom(p.getMessage());
			break;
		case SYNC_GAME_SIZE:
			receiveSize(p.getPoint());
			break;
		case PLACE_SHIP:
			receiveShipPlacement(Integer.parseInt(p.getMessage()), Integer.parseInt(p.getClientName()), p.getPoint().x, p.getPoint().y, p.getNumber());
			break;
		case ATTACK:
			receiveAttackStatus(p.getNumber(), p.getPoint().x, p.getPoint().y);
			break;
		case SHIP_STATUS:
			receiveShipStatus(Integer.parseInt(p.getClientName()), p.getNumber());
			break;
		case ATTACK_RADIUS:
			receiveAttackRadius(p.getPoint().x, p.getPoint().y, p.getNumber());
			break;
		case CAN_ATTACK:
			receiveCanAttack(p.getClientName(), p.getNumber());
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
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
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

	public void sendShipPlacement(Point coord) {
		Payload p = new Payload();
		p.setPayloadType(PayloadType.PLACE_SHIP);
		p.setPoint(coord);
		sendPayload(p);
	}

	public void sendAttack(Point coord) {
		Payload p = new Payload();
		p.setPayloadType(PayloadType.ATTACK);
		p.setPoint(coord);
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
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
package client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import server.Payload;
import server.PayloadType;
//part 6
public class SocketClient {
	private static Socket server;
	private static Thread fromServerThread;
	private static Thread clientThread;
	private static String clientName;
	private static ObjectOutputStream out;
	private final static Logger log = Logger.getLogger(SocketClient.class.getName());
	private static Event event;
	
	private static Payload buildMessage(String message) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.MESSAGE);
		payload.setClientName(clientName);
		payload.setMessage(message);
		return payload;
	}

	private static Payload buildConnectionStatus(String name, boolean isConnect) {
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

	private static void sendPayload(Payload p) {
		try {
			out.writeObject(p);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void listenForServerMessage(ObjectInputStream in) {
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

	/***
	 * Determine any special logic for different PayloadTypes
	 * 
	 * @param p
	 */
	private static void processPayload(Payload p) {

		switch (p.getPayloadType()) {
		case CONNECT:
			if (event != null) {
				event.onClientConnect(p.getClientName(), p.getMessage());
			}
			break;
		case DISCONNECT:
			if (event != null) {
				event.onClientDisconnect(p.getClientName(), p.getMessage());
			}
			break;
		case MESSAGE:
			if (event != null) {
				event.onMessageReceive(p.getClientName(), p.getMessage());
			}
			break;
		default:
			log.log(Level.WARNING, "unhandled payload on client" + p);
			break;

		}
	}

	// TODO Start public methods here
	public static void callbackListener(Event e) {
		event = e;
		log.log(Level.INFO, "Attached listener");
	}

	public static boolean connectAndStart(String address, String port) throws IOException {
		if (connect(address, port)) {
			return start();
		}
		return false;
	}

	public static boolean connect(String address, String port) {
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

	public static void setUsername(String username) {
		clientName = username;
		sendPayload(buildConnectionStatus(clientName, true));
	}

	public static void sendMessage(String message) {
		sendPayload(buildMessage(message));
	}

	public static boolean start() throws IOException {
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

	public static void close() {
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
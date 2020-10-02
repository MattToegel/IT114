import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import utils.Debug;

public class SocketServer {
	int port = 3000;
	public static boolean isRunning = false;
	private List<ServerThread> clients = new ArrayList<ServerThread>();

	private void start(int port) {
		this.port = port;
		Debug.log("Waiting for client");
		try (ServerSocket serverSocket = new ServerSocket(port);) {
			isRunning = true;
			while (SocketServer.isRunning) {
				try {
					Socket client = serverSocket.accept();
					Debug.log("Client connecting...");
					// Server thread is the server's representation of the client
					ServerThread thread = new ServerThread(client, this);
					thread.start();
					// add client thread to list of clients
					clients.add(thread);
					Debug.log("Client added to clients pool");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				isRunning = false;
				// Thread.sleep(50);
				Debug.log("closing server socket");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected synchronized void disconnect(ServerThread client) {
		long id = client.getId();
		clients.remove(client);
		broadcast("disconnected", id);
	}

	// Broadcast given message to everyone connected
	public synchronized void broadcast(String message, long id) {
		// let's temporarily use the thread id as the client identifier to
		// show in all client's chat. This isn't good practice since it's subject to
		// change as clients connect/disconnect
		message = String.format("User[%d]: %s", id, message);
		// end temp identifier

		// loop over clients and send out the message
		Iterator<ServerThread> it = clients.iterator();
		while (it.hasNext()) {
			ServerThread client = it.next();
			boolean wasSuccessful = client.send(message);
			if (!wasSuccessful) {
				Debug.log("Removing disconnected client from list");
				it.remove();
				broadcast("Disconnected", id);
			}
		}
	}

	public static void main(String[] args) {
		// let's allow port to be passed as a command line arg
		// in eclipse you can set this via "Run Configurations"
		// -> "Arguments" -> type the port in the text box -> Apply
		int port = -1;// make some default
		if (args.length >= 1) {
			String arg = args[0];
			try {

				port = Integer.parseInt(arg);
			} catch (Exception e) {
				// ignore this, we know it was a parsing issue
			}
		}
		if (port > -1) {
			Debug.log("Starting Server");
			SocketServer server = new SocketServer();
			Debug.log("Listening on port " + port);
			server.start(port);
			Debug.log("Server Stopped");
		}
	}
}
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//part 4
public class ServerThread extends Thread {
	private Socket client;
	private ObjectInputStream in;// from client
	private ObjectOutputStream out;// to client
	private boolean isRunning = false;
	private Room currentRoom;// what room we are in, should be lobby by default

	protected synchronized Room getCurrentRoom() {
		return currentRoom;
	}

	protected synchronized void setCurrentRoom(Room room) {
		if (room != null) {
			currentRoom = room;
		} else {
			System.out.println("Passed in room was null, this shouldn't happen");
		}
	}

	public ServerThread(Socket myClient, Room room) throws IOException {
		this.client = myClient;
		this.currentRoom = room;
		out = new ObjectOutputStream(client.getOutputStream());
		in = new ObjectInputStream(client.getInputStream());
	}

	/***
	 * Sends the message to the client represented by this ServerThread
	 * 
	 * @param message
	 * @return
	 */
	protected boolean send(String message) {
		// added a boolean so we can see if the send was successful
		try {
			out.writeObject(message);
			return true;
		} catch (IOException e) {
			System.out.println("Error sending message to client (most likely disconnected)");
			e.printStackTrace();
			cleanup();
			return false;
		}
	}

	@Override
	public void run() {
		try {
			isRunning = true;
			String fromClient;
			while (isRunning && // flag to let us easily control the loop
					!client.isClosed() // breaks the loop if our connection closes
					&& (fromClient = (String) in.readObject()) != null // reads an object from inputStream (null would
																		// likely mean a disconnect)
			) {
				// keep this one as sysout otherwise if we turn of System.out.println we'll not
				// see
				// messages
				System.out.println("Received from client: " + fromClient);
				currentRoom.sendMessage(this, fromClient);
			} // close while loop
		} catch (Exception e) {
			// happens when client disconnects
			e.printStackTrace();
			System.out.println("Client Disconnected");
		} finally {
			isRunning = false;
			System.out.println("Cleaning up connection for ServerThread");
			cleanup();
		}
	}

	private void cleanup() {
		if (currentRoom != null) {
			System.out.println(getName() + " removing self from room " + currentRoom.getName());
			currentRoom.removeClient(this);
		}
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				System.out.println("Input already closed");
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				System.out.println("Client already closed");
			}
		}
		if (client != null && !client.isClosed()) {
			try {
				client.shutdownInput();
			} catch (IOException e) {
				System.out.println("Socket/Input already closed");
			}
			try {
				client.shutdownOutput();
			} catch (IOException e) {
				System.out.println("Socket/Output already closed");
			}
			try {
				client.close();
			} catch (IOException e) {
				System.out.println("Client already closed");
			}
		}
	}
}
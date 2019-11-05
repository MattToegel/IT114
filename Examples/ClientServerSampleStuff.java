//This doesn't work, it's purely sample/code demo from class
import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ServerThing {
	List<ServerThread> clients = new ArrayList<ServerThread>();
	public void run() {
		
	}
	public void Broadcast(Payload p) throws IOException {
		for(int i = 0; i < clients.size(); i++) {
			clients.get(i).send(p);
		}
	}
	public void SendToClientByIndex(int index, Payload p) throws IOException {
		clients.get(index).send(p);
	}
	public void SendToClientByID(int id, Payload p) throws IOException {
		for(int i = 0; i < clients.size(); i++) {
			if(clients.get(i).id == id) {
				clients.get(i).send(p);
				return;
			}
		}
	}
	public void blah() {
		HashMap<String, Piece> anchors = new HashMap<String, Piece>();
		anchors.put("GamePiece1", new Piece(new Point(2,2), new Dimension(64,64)));
		anchors.put("GamePiece2", new Piece(new Point(3,3), new Dimension(64,64)));
		anchors.put("GamePiece3", new Piece(new Point(5,5), new Dimension(64,64)));
	}
}
class Piece{
	public Point position;
	public Dimension size;
	public Piece(Point p, Dimension s) {
		this.position = p;
		this.size = s;
	}
	
}
//Class to hold client connection and prevent it from blocking the main thread of the server
class ServerThread extends Thread{
	private Socket client;
	private String clientName;
	public int id;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean isRunning = false;
	private ServerThing server;
	public ServerThread(Socket myClient, ServerThing parent) throws IOException {
		this.server = parent;
		this.client = myClient;
		isRunning = true;
		out = new ObjectOutputStream(client.getOutputStream());
		in = new ObjectInputStream(client.getInputStream());
		System.out.println("Spawned thread for client " + this.id);
	}
	public boolean isClosed() {
		return client.isClosed();
	}
	synchronized void processPayload(Payload payloadIn) throws IOException {
		switch(payloadIn.payloadType) {
			//case ACK:
				//this is just to me, update values from server
				//break;
			case ROLL_IT:
				Random random = new Random();
				int roll = random.nextInt(7);
				System.out.println("[Server]: You rolled: " + roll);
				
				//send to just this client
				out.writeObject(new Payload(PayloadType.ROLL_IT, "You rolled " + roll));
				//send message out to all clients [roll]
				server.Broadcast(new Payload(PayloadType.ROLL_IT, "Client x rolled " + roll));
				break;
			case CONNECT:
				//locally create a new player reference
				//add to local list to track updates (visuals)
				break;
			case DISCONNECT:
				//remove disconnected player from my list
				break;
			default:
				break;
			
		}
	}
	@Override
	public void run() {
		try{
			Payload fromClient;
			while(isRunning && (fromClient = (Payload)in.readObject()) != null) {
				System.out.println("Received: " + fromClient);
				System.out.println("Client Message: " + fromClient.message);
				processPayload(fromClient);
			}
		}
		catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("Server cleaning up IO for " + clientName);
			server.outMessages.add(
						new Payload(id, PayloadType.DISCONNECT)
						);
			cleanup();
		}
	}
	public void stopThread() {
		isRunning = false;
	}
	public void send(Payload msg) throws IOException {
		out.writeObject(msg);
	}
	void cleanup() {
		if(in != null) {
			try{in.close();}
			catch(Exception e) { System.out.println("Input already closed");}
		}
		if(out != null) {
			try {out.close();}
			catch(Exception e) {System.out.println("Output already closed");}
		}
	}
	
}
class ClientThing{
	NetworkClient client;
	void Run() {
		
		//Scanner scan;
		//scan.readli
	}
	public void RollIt() {
		client.Send(new Payload(PayloadType.ROLL_IT, "I don't need to enter this"));
	}
}
class NetworkClient{
	ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
	
	public void Send(Payload p) {
		out.writeObject(p);
	}

}

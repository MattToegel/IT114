import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThreadPart4 extends Thread{
	private Socket client;
	private ObjectInputStream in;//from client
	private ObjectOutputStream out;//to client
	private boolean isRunning = false;
	private SampleSocketServerPart4 server;//ref to our server so we can call methods on it
	//more easily
	private String clientName = "Anon";
	public ServerThreadPart4(Socket myClient, SampleSocketServerPart4 server) throws IOException {
		this.client = myClient;
		this.server = server;
		isRunning = true;
		out = new ObjectOutputStream(client.getOutputStream());
		in = new ObjectInputStream(client.getInputStream());
		
		//let everyone know we're here...
		//we actually can't do this here
		//when we send the message, we aren't in the clients list yet
		//so we won't see that we connected. Jump down to run()
		//broadcastConnected();
	}
	void broadcastConnected() {
		PayloadPart4 payload = new PayloadPart4();
		payload.setPayloadType(PayloadTypePart4.CONNECT);
		//note we don't need to specify message as it'll be handle by the server
		//for this case
		//we can send our name instead of id
		//server.broadcast(payload, this.getId());
		server.broadcast(payload, this.clientName);
	}
	void broadcastDisconnected() {
		//let everyone know we're here
		PayloadPart4 payload = new PayloadPart4();
		payload.setPayloadType(PayloadTypePart4.DISCONNECT);
		//note we don't need to specify message as it'll be handle by the server
		//for this case
		//we can send our name instead of id
		//server.broadcast(payload, this.getId());
		server.broadcast(payload, this.clientName);
	}
	public boolean send(PayloadPart4 payload) {
		try {
			out.writeObject(payload);
			return true;
		}
		catch(IOException e) {
			System.out.println("Error sending message to client");
			e.printStackTrace();
			cleanup();
			return false;
		}
	}
	@Deprecated
	public boolean send(String message) {
		//added a boolean so we can see if the send was successful
		PayloadPart4 payload = new PayloadPart4();
		payload.setPayloadType(PayloadTypePart4.MESSAGE);
		payload.setMessage(message);
		return send(payload);
	}
	@Override
	public void run() {
		try {
			//here we can let people know. We should be on the list
			//so we'll see that we connected
			//if we're using client name then we can comment this part out and use
			//it only when we get a connect payload from our client
			//broadcastConnected();
			PayloadPart4 fromClient;
			while(isRunning 
					&& !client.isClosed()
					&& (fromClient = (PayloadPart4)in.readObject()) != null) {//open while loop
				processPayload(fromClient);
			}//close while loop
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Terminating Client");
		}
		finally {
			//we're going to try to send our disconnect message, but it could fail
			broadcastDisconnected();
			//TODO
			System.out.println("Server Cleanup");
			cleanup();
		}
	}
	private void processPayload(PayloadPart4 payload) {
		System.out.println("Received from client: " + payload);
		switch(payload.getPayloadType()) {
		case CONNECT:
			String m = payload.getMessage();
			if(m != null) {
				m = WordBlackList.filter(m);
				this.clientName = m;
			}
			broadcastConnected();
			break;
		case DISCONNECT:
			System.out.println("Received disconnect");
			break;
		case MESSAGE:
			//we can just pass the whole payload onward
			payload.setMessage(WordBlackList.filter(payload.getMessage()));
			server.broadcast(payload, this.clientName);
			break;
		default:
			System.out.println("Unhandled payload type from client " + payload.getPayloadType());
			break;
		}
	}
	private void cleanup() {
		if(in != null) {
			try {in.close();}
			catch(IOException e) {System.out.println("Input already closed");}
		}
		if(out != null) {
			try {out.close();}
			catch(IOException e) {System.out.println("Client already closed");}
		}
		if(client != null && !client.isClosed()) {
			try {client.shutdownInput();}
			catch(IOException e) {System.out.println("Socket/Input already closed");}
			try {client.shutdownOutput();}
			catch(IOException e) {System.out.println("Socket/Output already closed");}
			try {client.close();}
			catch(IOException e) {System.out.println("Client already closed");}
		}
	}
}

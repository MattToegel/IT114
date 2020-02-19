import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SampleSocketServerPart4 {
	int port = 3002;
	public static boolean isRunning = true;
	private List<ServerThreadPart4> clients = new ArrayList<ServerThreadPart4>();
	private void start(int port) {
		this.port = port;
		System.out.println("Waiting for client");
		try (ServerSocket serverSocket = new ServerSocket(port);) {
			while(SampleSocketServerPart4.isRunning) {
				try {
					Socket client = serverSocket.accept();
					System.out.println("Client connecting...");
					//Server thread is the server's representation of the client
					ServerThreadPart4 thread = new ServerThreadPart4(client, this);
					thread.start();
					//add client thread to list of clients
					clients.add(thread);
					System.out.println("Client added to clients pool");
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				isRunning = false;
				Thread.sleep(50);
				System.out.println("closing server socket");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Deprecated
	int getClientIndexByThreadId(long id) {
		for(int i = 0, l = clients.size(); i < l;i++) {
			if(clients.get(i).getId() == id) {
				return i;
			}
		}
		return -1;
	}
	public synchronized void broadcast(PayloadPart4 payload, String name) {
		String msg = payload.getMessage();
		payload.setMessage(
				//prepending client name to front of message
				(name!=null?name:"[Name Error]") 
				//including original message if not null (with a prepended colon)
				+ (msg != null?": "+ msg:"")
		);
		broadcast(payload);
	}
	public synchronized void broadcast(PayloadPart4 payload) {
		System.out.println("Sending message to " + clients.size() + " clients");
		Iterator<ServerThreadPart4> iter = clients.iterator();
		while(iter.hasNext()) {
			ServerThreadPart4 client = iter.next();
			boolean messageSent = client.send(payload);
			if(!messageSent) {
				//if we got false, due to update of send()
				//we can assume the client lost connection
				//so let's clean it up
				iter.remove();
				System.out.println("Removed client " + client.getId());
			}
		}
	}
	//Broadcast given payload to everyone connected
	public synchronized void broadcast(PayloadPart4 payload, long id) {
		//let's temporarily use the index as the client identifier to
		//show in all client's chat. You'll see why this is a bad idea
		//when clients disconnect/reconnect.
		int from = getClientIndexByThreadId(id);
		String msg = payload.getMessage();
		payload.setMessage(
				//prepending client name to front of message
				(from>-1?"Client[" + from+"]":"unknown") 
				//including original message if not null (with a prepended colon)
				+ (msg != null?": "+ msg:"")
		);
		//end temp identifier (maybe this won't be too temporary as I've reused
		//it in a few samples now)
		broadcast(payload);
		
	}
	//Broadcast given message to everyone connected
	public synchronized void broadcast(String message, long id) {
		PayloadPart4 payload = new PayloadPart4();
		payload.setPayloadType(PayloadTypePart4.MESSAGE);
		payload.setMessage(message);
		broadcast(payload, id);
	}
	

	public static void main(String[] args) {
		//let's allow port to be passed as a command line arg
		//in eclipse you can set this via "Run Configurations" 
		//	-> "Arguments" -> type the port in the text box -> Apply
		int port = 3002;//make some default
		if(args.length >= 1) {
			String arg = args[0];
			try {
				port = Integer.parseInt(arg);
			}
			catch(Exception e) {
				//ignore this, we know it was a parsing issue
			}
		}
		System.out.println("Starting Server");
		SampleSocketServerPart4 server = new SampleSocketServerPart4();
		System.out.println("Listening on port " + port);
		server.start(port);
		System.out.println("Server Stopped");
	}
}
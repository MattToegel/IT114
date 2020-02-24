import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class SampleSocketClientPart5 {
	Socket server;
	
	public void connect(String address, int port) {
		try {
			server = new Socket(address, port);
			System.out.println("Client connected");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void start() throws IOException {
		if(server == null) {
			return;
		}
		System.out.println("Client Started");
		//listen to console, server in, and write to server out
		try(Scanner si = new Scanner(System.in);
				ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(server.getInputStream());){
			//let's block the thread for a sec to gather a username
			String name ="";
			do {
				System.out.println("Please enter a username to continue");
				name = si.nextLine();
				if(name == null || name.trim().length() == 0) {
					name="";
				}
			}
			while(!server.isClosed() && name != null && name.length() == 0);
			//we should have a name, let's tell our server
			PayloadPart5 p = new PayloadPart5();
			//we can also default payloadtype in payload
			//to a desired value, though it's good to be clear
			//what we're sending
			p.setPayloadType(PayloadTypePart5.CONNECT);
			p.setMessage(name);
			out.writeObject(p);
			
			
			//Thread to listen for keyboard input so main thread isn't blocked
			Thread inputThread = new Thread() {
				@Override
				public void run() {
					try {
						while(!server.isClosed()) {
							System.out.println("Waiting for input");
							String line = si.nextLine();
							if(!"quit".equalsIgnoreCase(line) && line != null) {
								//grab line and throw it into a payload object
								PayloadPart5 p = new PayloadPart5();
								//we can also default payloadtype in payload
								//to a desired value, though it's good to be clear
								//what we're sending
								p.setPayloadType(PayloadTypePart5.MESSAGE);
								p.setMessage(line);
								out.writeObject(p);
							}
							else {
								System.out.println("Stopping input thread");
								//we're quitting so tell server we disconnected so it can broadcast
								PayloadPart5 p = new PayloadPart5();
								p.setPayloadType(PayloadTypePart5.DISCONNECT);
								p.setMessage("bye");
								out.writeObject(p);
								break;
							}
						}
					}
					catch(Exception e) {
						System.out.println("Client shutdown");
					}
					finally {
						close();
					}
				}
			};
			inputThread.start();//start the thread
			
			//Thread to listen for responses from server so it doesn't block main thread
			Thread fromServerThread = new Thread() {
				@Override
				public void run() {
					try {
						PayloadPart5 fromServer;
						//while we're connected, listen for payloads from server
						while(!server.isClosed() && (fromServer = (PayloadPart5)in.readObject()) != null) {
							//System.out.println(fromServer);
							processPayload(fromServer);
						}
						System.out.println("Stopping server listen thread");
					}
					catch (Exception e) {
						if(!server.isClosed()) {
							e.printStackTrace();
							System.out.println("Server closed connection");
						}
						else {
							System.out.println("Connection closed");
						}
					}
					finally {
						close();
					}
				}
			};
			fromServerThread.start();//start the thread
			
			//Keep main thread alive until the socket is closed
			//initialize/do everything before this line
			while(!server.isClosed()) {
				Thread.sleep(50);
			}
			System.out.println("Exited loop");
			System.exit(0);//force close
			//TODO implement cleaner closure when server stops
			//without this, it still waits for input before terminating
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			close();
		}
	}
	private void processPayload(PayloadPart5 payload) {
		System.out.println(payload);
		switch(payload.getPayloadType()) {
		case CONNECT:
			System.out.println(
					String.format("Client \"%s\" connected", payload.getMessage())
			);
			break;
		case DISCONNECT:
			System.out.println(
					String.format("Client \"%s\" disconnected", payload.getMessage())
			);
			break;
		case MESSAGE:
			System.out.println(
					String.format("%s", payload.getMessage())
			);
			break;
		default:
			System.out.println("Unhandled payload type: " + payload.getPayloadType().toString());
			break;
		}
	}
	private void close() {
		if(server != null) {
			try {
				server.close();
				System.out.println("Closed socket");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		SampleSocketClientPart5 client = new SampleSocketClientPart5();
		client.connect("127.0.0.1", 3002);
		try {
			//if start is private, it's valid here since this main is part of the class
			client.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
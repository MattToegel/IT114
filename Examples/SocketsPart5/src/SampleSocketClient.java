import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class SampleSocketClient {
	Socket server;
	static ObjectOutputStream out;
	public Queue<String> messages = new LinkedList<String>();
	public void connect(String address, int port) {
		try {
			//create new socket to destination and port
			server = new Socket(address, port);
			System.out.println("Client connected");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void sendChoice(String choice) {
		try {
			out.writeObject(new Payload(PayloadType.CHOICE, choice));
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
				
				ObjectInputStream in = new ObjectInputStream(server.getInputStream());){
			out = new ObjectOutputStream(server.getOutputStream());
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
								out.writeObject(new Payload(PayloadType.MESSAGE, line));
							}
							else {
								System.out.println("Stopping input thread");
								//we're quitting so tell server we disconnected so it can broadcast
								out.writeObject(new Payload(PayloadType.DISCONNECT, null));
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
						Payload fromServer;
						//while we're connected, listed for payloads from server
						while(!server.isClosed() && (fromServer = (Payload)in.readObject()) != null) {
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
	/**
	 * Handle our different payload types.
	 * You may create functions for each case to help organize code or keep it cleaner
	 * @param p
	 */
	void processPayload(Payload p) {
		switch(p.payloadType) {
			case CONNECT:
				System.out.println("A client connected");
				messages.add("A client connected");
				break;
			case DISCONNECT:
				System.out.println("A client disconnected");
				messages.add("A client disconnected");
				break;
			case MESSAGE:
				System.out.println("Replay from server: " + p.message);
				messages.add(p.message);
				break;
			case CHOICE:
				messages.add(p.message);
				break;
			default:
				System.out.println("We aren't handling payloadType " + p.payloadType.toString());
				break;
		}
	}
	private void close() {
		if(server != null && !server.isClosed()) {
			try {
				server.close();
				System.out.println("Closed socket");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		SampleSocketClient client = new SampleSocketClient();
		//grab host:port from commandline
		//TODO this was reworked, please take note
		String host = null;
		int port = -1;
		try{
			//not safe but try-catch will get it
			if(args[0].indexOf(":") > -1) {
				String[] target = args[0].split(":");
				host = target[0].trim();
				port = Integer.parseInt(target[1].trim());
			}
			else {
				System.out.println("Important!: Please pass the argument as hostname:port or ipaddress:port");
			}
		}
		catch(Exception e){
			System.out.println("Error parsing host:port argument[0]");
		}
		if(port == -1 || host == null){
			return;
		}
		client.connect(host, port);
		try {
			//if start is private, it's valid here since this main is part of the class
			client.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class NetworkClient {
	Socket server;
	boolean isRunning = false;
	Queue<Payload> outMessages = new LinkedList<Payload>();
	Queue<Payload> inMessages = new LinkedList<Payload>();
	public boolean connect(String address, int port) throws UnknownHostException, IOException {
		server = new Socket(address, port);
		System.out.println("Client connected");
		NetworkClient self = this;
		Thread nc = new Thread() {
			@Override
			public void run() {
				try {
					self.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		nc.start();
		return true;
	}
	public String getAddress() {
		return server.getLocalAddress().getHostAddress();
	}
	public void send(int id, PayloadType type) {
		send(id, type, 0,0, null);
	}
	public void send(int id, PayloadType type, String extra) {
		send(id, type, 0,0,extra);
	}
	public void send(int id, PayloadType type, int x, int y) {
		send(id, type, x, y, null);
	}
	public void send(int id, PayloadType type, int x, int y, String extra) {
		if(server != null && !server.isClosed()) {
			System.out.println("Sending " + ((PayloadType)type).toString());
			outMessages.add(new Payload(id, type, x, y, extra));
		}
	}
	public void handleQueuedMessages(Consumer<Payload> processFromServer, int messagesToHandle) {
		Payload p = null;
		int processed = 0;
		while((p = this.getMessage()) != null) {
			//call the processFromServer callback with the payload as a parameter
			processFromServer.accept(p);
			//process up to [messagesToHandle] messages per "tick"
			processed++;
			if(processed >= messagesToHandle) {
				break;
			}
		}
	}
	void pollMessagesToSend(ObjectOutputStream out) {
		Thread inputThread = new Thread() {
			@Override
			public void run() {
				try {
					while(!server.isClosed() && isRunning) {
						Payload payload = outMessages.poll();
						if(payload != null) {
							out.writeObject(payload);//send to server
							if(payload.payloadType == PayloadType.DISCONNECT) {
								System.out.println("Stopping input thread");
								break;
							}
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
	}
	void listenForServer(ObjectInputStream in) {
		//Thread to listen for responses from server so it doesn't block main thread
		Thread fromServerThread = new Thread() {
			@Override
			public void run() {
				try {
					while(!server.isClosed() && isRunning) {
						Payload p = (Payload)in.readObject();
						inMessages.add(p);
						if(p.payloadType != PayloadType.MOVE_SYNC)
						System.out.println("Replay from server: " + p.toString());
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
	}
	public void start() throws IOException {
		if(server == null) {
			return;
		}
		System.out.println("Client Started");
		isRunning = true;
		try(
				ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(server.getInputStream());){
			
			pollMessagesToSend(out);
			listenForServer(in);
			
			//Keep main thread alive until the socket is closed
			while(!server.isClosed() && isRunning) {
				Thread.sleep(50);
			}
			System.out.println("Exited loop");
			System.exit(0);//force close
			//TODO implement cleaner closure when server stops before client
			//currently hangs/waits on the console/scanner input
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			close();
		}
	}
	public Payload getMessage() {
		return inMessages.poll();
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
	public void terminate() {
		isRunning = false;
		System.exit(0);
	}
	//helpers
	public void disconnect(int id) {
		send(id, PayloadType.DISCONNECT);
	}
}

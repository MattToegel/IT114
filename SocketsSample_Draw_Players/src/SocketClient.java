import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;


public class SocketClient {
	private Socket server;
	private OnReceive onReceiveListener;
	public void registerListeners(OnReceive listener) {
		this.onReceiveListener = listener;
	}
	private Queue<Payload> toServer = new LinkedList<Payload>();
	private Queue<Payload> fromServer = new LinkedList<Payload>();
	
	public static SocketClient connect(String address, int port) {
		SocketClient client = new SocketClient();
		client._connect(address, port);
		Thread clientThread =  new Thread() {
			@Override
			public void run() {
				client.start();
			}
		};
		clientThread.start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return client;
	}
	private void _connect(String address, int port) {
		try {
			server = new Socket(address, port);
			System.out.println("Client connected");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/***
	 * This blocks whatever thread it's called on, don't call it on main thread
	 */
	private void start() {
		if(server == null) {
			return;
		}
		System.out.println("Client Started");
		//listen to console, server in, and write to server out
		try(	ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(server.getInputStream());){
			Thread inputThread = new Thread() {
				@Override
				public void run() {
					try {
						while(!server.isClosed()) {
							Payload p = toServer.poll();
							if(p != null) {
								out.writeObject(p);
							}
							else {
								try {
									Thread.sleep(8);
								}
								catch (Exception e) {
									e.printStackTrace();
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
			
			//Thread to listen for responses from server so it doesn't block main thread
			Thread fromServerThread = new Thread() {
				@Override
				public void run() {
					try {
						Payload p;
						//while we're connected, listen for payloads from server
						while(!server.isClosed() && (p = (Payload)in.readObject()) != null) {
							//System.out.println(fromServer);
							//processPayload(fromServer);
							fromServer.add(p);
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
			
			
			Thread payloadProcessor = new Thread(){
				@Override
				public void run() {
					while(!server.isClosed()) {
						Payload p = fromServer.poll();
						if(p != null) {
							processPayload(p);
						}
						else {
							try {
								Thread.sleep(8);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			};
			payloadProcessor.start();
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
	public void postConnectionData(String clientName) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.CONNECT);
		payload.setMessage(clientName);
		toServer.add(payload);
	}
	public void sendMessage(String message) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.MESSAGE);
		payload.setMessage(message);
		toServer.add(payload);
	}
	public void synDirection(Point p, int playerId) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.DIRECTION);
		payload.setX(p.x);
		payload.setY(p.y);
		System.out.println(payload);
		payload.setId(playerId);
		toServer.add(payload);
	}
	private void processPayload(Payload payload) {
		//commented out because it gets too spammy
		//System.out.println(payload);
		String msg = "";
		switch(payload.getPayloadType()) {
		case SELF:
			System.out.println("Set self");
			if(onReceiveListener != null) {
				onReceiveListener.onPlayerConnected(
						payload.getId(), 
						payload.getX(),
						payload.getY(),
						payload.getMessage());
			}
			break;
		case CONNECT:
			msg = String.format("Client \"%s\" connected", payload.getMessage());
			System.out.println(msg);
			if(onReceiveListener != null) {
				onReceiveListener.onPlayerConnected(
						payload.getId(), 
						payload.getX(),
						payload.getY(),
						payload.getMessage());
			}
			break;
		case DISCONNECT:
			msg = String.format("Client \"%s\" disconnected", payload.getMessage());
			System.out.println(msg);
			if(onReceiveListener != null) {
				onReceiveListener.onReceivedMessage(msg);
				onReceiveListener.onPlayerDisconnected(payload.getId());
			}
			
			break;
		case MESSAGE:
			System.out.println(
					String.format("%s", payload.getMessage())
			);
			if(onReceiveListener != null) {
				onReceiveListener.onReceivedMessage(msg);
			}
			break;
		case DIRECTION:
		case MOVE_SYNC:
			if (onReceiveListener != null) {
				onReceiveListener.onReceivedTransform(
						payload.getId(),
						payload.getPayloadType(),
						payload.getX(),
						payload.getY()
						);
			}
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
}

interface OnReceive{
	void onReceivedTransform(int playerId, PayloadType type, int x, int y);
	void onReceivedMessage(String msg);
	void onPlayerConnected(int id, int x, int y, String name);
	void onPlayerDisconnected(int id);
}
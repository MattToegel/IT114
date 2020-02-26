import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.google.gson.Gson;


public class SampleSocketServerPart5 {
	int port = 3002;
	public static boolean isRunning = true;
	private List<ServerThreadPart5> clients = new ArrayList<ServerThreadPart5>();
	//We'll use a queue and a thread to separate our chat history
	Queue<String> messages = new LinkedList<String>();
	private void start(int port) {
		this.port = port;
		startQueueReader();
		//sample score save, why not here?
		loadScore();
		saveScore(1000);
		System.out.println("Waiting for client");
		try (ServerSocket serverSocket = new ServerSocket(port);) {
			while(SampleSocketServerPart5.isRunning) {
				try {
					Socket client = serverSocket.accept();
					System.out.println("Client connecting...");
					//Server thread is the server's representation of the client
					ServerThreadPart5 thread = new ServerThreadPart5(client, this);
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
	void loadScore() {
		try {
			Gson gson = new Gson();
			ScoreState ss = gson.fromJson(new FileReader("score.json"), ScoreState.class);
			long s = (long) ss.scores.get(0).score;
			System.out.println("Loaded score: " + s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void saveScore(int score){
		//false because we don't want to append
		//we want to update/replace the object
		//sample
		ScoreState ss = new ScoreState();
		ss.scores.add(new Score("Bob", 1000));
		ss.scores.add(new Score("Joe", 500));
		System.out.println(ss.toString());
		try(FileWriter writer = new FileWriter("score.json",false)){
			//TODO get ScoreState to convert to JSON
			Gson gson = new Gson();
			writer.write(gson.toJson(ss));
			writer.flush();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	void startQueueReader() {
		System.out.println("Preparing Queue Reader");
		Thread queueReader = new Thread() {
			@Override
			public void run() {
				String message = "";
				try(FileWriter write = new FileWriter("chathistory.txt", true)){
					while(isRunning) {
						message = messages.poll();
						if(message != null) {
							message = messages.poll();
							write.append(message);
							write.write(System.lineSeparator());
							write.flush();
						}
						//sleep for a bit to let OS multi-task
						//since it's FIFO we don't need immediate polling
						sleep(50);
					}
				}
				catch(IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		queueReader.start();
		System.out.println("Started Queue Reader");
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
	public synchronized void broadcast(PayloadPart5 payload, String name) {
		String msg = payload.getMessage();
		payload.setMessage(
				//prepending client name to front of message
				(name!=null?name:"[Name Error]") 
				//including original message if not null (with a prepended colon)
				+ (msg != null?": "+ msg:"")
		);
		broadcast(payload);
	}
	public synchronized void broadcast(PayloadPart5 payload) {
		System.out.println("Sending message to " + clients.size() + " clients");
		//TODO record message
		storeInFile(payload.getMessage());
		Iterator<ServerThreadPart5> iter = clients.iterator();
		while(iter.hasNext()) {
			ServerThreadPart5 client = iter.next();
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
	public synchronized void broadcast(PayloadPart5 payload, long id) {
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
		PayloadPart5 payload = new PayloadPart5();
		payload.setPayloadType(PayloadTypePart5.MESSAGE);
		payload.setMessage(message);
		broadcast(payload, id);
	}
	void storeInFile(String message) {
		//add our message to our queue
		messages.add(message);
		//we'll have a separate thread do the actual saving for now
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
		SampleSocketServerPart5 server = new SampleSocketServerPart5();
		System.out.println("Listening on port " + port);
		server.start(port);
		System.out.println("Server Stopped");
	}
}
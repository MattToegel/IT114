package Module5.Part4;

import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private String name;//unique name of the Room
    private volatile boolean isRunning = false;
    private ConcurrentHashMap<Long, ServerThread> clientsInRoom = new ConcurrentHashMap<Long, ServerThread>();
    
    //constants (used to reduce potential types when using them in code)
    private final String COMMAND_CHARACTER =  "/";
    private final String CREATE_ROOM = "createroom";
    private final String JOIN_ROOM = "joinroom";
    private final String DISCONNECT = "disconnect";
    private final String LOGOFF = "logoff";
    private final String LOGOUT = "logout";
    private final String SINGLE_SPACE = " ";

    public final static String LOBBY = "lobby";

    private void info(String message) {
		System.out.println(String.format("Room[%s]: %s", name, message));
	}
    public Room(String name){
        this.name = name;
        isRunning = true;
        System.out.println(String.format("Room[%s] created", this.name));
    }
    public String getName(){
        return this.name;
    }
    protected synchronized void addClient(ServerThread client){
        if(!isRunning){ //block action if Room isn't running
            return;
        }
        if(clientsInRoom.containsKey(client.getClientId())){
            info("Attempting to add a client that already exists in the room");
            return;
        }
        clientsInRoom.put(client.getClientId(), client);
        client.setCurrentRoom(this);
        //notify clients of someone joining
        sendMessage(null, String.format("User[%s] joined the Room[%s]", client.getClientId(), getName()));
    
    }
    
    protected synchronized void removedClient(ServerThread client){
        if(!isRunning){ //block action if Room isn't running
            return;
        }
        clientsInRoom.remove(client.getClientId());
        if(!clientsInRoom.isEmpty()){
            //notify remaining clients of someone leaving
            sendMessage(null, String.format("User[%s] left the room", client.getClientId(), getName()));
        }
        autoCleanup();
        
    }

    /***
	 * Helper function to process messages to trigger different functionality.
	 * 
	 * @param sender  The sender of the message (since they'll be the ones
	 *                triggering the actions)
     * @param message The original message being sent
	 */
	private boolean processCommand(ServerThread sender, String message) {
		boolean wasCommand = false;
		try {
			if (message.startsWith(COMMAND_CHARACTER)) {
                //send the original message back to the sender so it's clear what they entered
                //afterwards, below processing will only share the result
                sender.send(message);
                //extract command and potential value from the message
				String fullCommand = message.replace(COMMAND_CHARACTER, "");
				String part1 = fullCommand;
				String[] commandParts = part1.split(SINGLE_SPACE, 2);// using limit so spaces in the command value aren't split
				final String command = commandParts[0];
				final String commandValue = commandParts.length>=2?commandParts[1]:"";
				switch (command) {
					case CREATE_ROOM:
						if (Server.INSTANCE.createRoom(commandValue)) {
                            Server.INSTANCE.joinRoom(commandValue, sender);
						} else {
							sender.send(String.format("Room %s already exists", commandValue));
						}
						wasCommand = true;
						break;
					case JOIN_ROOM:
						if (!Server.INSTANCE.joinRoom(commandValue, sender)) {
							sender.send(String.format("Room %s doesn't exist", commandValue));
						}
						wasCommand = true;
						break;
                    // Note: these are to disconnect, they're not for changing rooms
					case DISCONNECT:
					case LOGOFF:
					case LOGOUT:
						disconnect(sender);
						wasCommand = true;
						break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wasCommand;
	}
    /**
     * Sends a basic String message from the sender to all connectedClients
     * Internally calls processCommand and evaluates as necessary.
     * Note: Clients that fail to receive a message get removed from
     * connectedClients.
     * Adding the synchronized keyword ensures that only one thread can execute
     * these methods at a time,
     * preventing concurrent modification issues and ensuring thread safety
     * 
     * @param message
     * @param sender ServerThread (client) sending the message or null if it's a server-generated message
     */
    protected synchronized void sendMessage(ServerThread sender, String message) {
        if(!isRunning){ //block action if Room isn't running
            return;
        }
        
        if (sender != null && processCommand(sender, message)) {
            return;
        }
        // let's temporarily use the thread id as the client identifier to
        // show in all client's chat. This isn't good practice since it's subject to
        // change as clients connect/disconnect
        // Note: any desired changes to the message must be done before this line
        String senderString = sender == null ? "Room" : String.format("User[%s]", sender.getClientId());
        final String formattedMessage = String.format("%s: %s", senderString, message);
        // end temp identifier

        // loop over clients and send out the message; remove client if message failed
        // to be sent
        // Note: this uses a lambda expression for each item in the values() collection,
        // it's one way we can safely remove items during iteration
        info(String.format("sending message to %s recipients: %s", clientsInRoom.size(), formattedMessage));
        clientsInRoom.values().removeIf(client -> {
            boolean failedToSend = !client.send(formattedMessage);
            if (failedToSend) {
               info(String.format("Removing disconnected client[%s] from list", client.getClientId()));
                disconnect(client);
            }
            return failedToSend;
        });
    }
    /**
     * Takes a ServerThread and removes them from the Server
     * Adding the synchronized keyword ensures that only one thread can execute
     * these methods at a time,
     * preventing concurrent modification issues and ensuring thread safety
     * 
     * @param client
     */
    protected synchronized void disconnect(ServerThread client) {
        if(!isRunning){ //block action if Room isn't running
            return;
        }
        long id = client.getClientId();
        client.disconnect();
        removedClient(client);
        // Improved logging with user ID
        sendMessage(null, "User[" + id + "] disconnected");
    }
    protected synchronized void disconnectAll(){
        info("Disconnect All triggered");
        if(!isRunning){
            return;
        }
        clientsInRoom.values().removeIf(client->{
            disconnect(client);
            return true;
        });
        info("Disconnect All finished");
    }
    
    /**
     * Attempts to close the room to free up resources if it's empty
     */
    private void autoCleanup(){
        if(!Room.LOBBY.equalsIgnoreCase(name) && clientsInRoom.isEmpty()){
            close();
        }
    }

    public void close(){
        //attempt to gracefully close and migrate clients
        if(!clientsInRoom.isEmpty()){
            sendMessage(null, "Room is shutting down, migrating to lobby");
            info(String.format("migrating %s clients", name, clientsInRoom.size()));
            clientsInRoom.values().removeIf(client->{
                Server.INSTANCE.joinRoom(Room.LOBBY, client);
                return true;
            });
        }
        Server.INSTANCE.removeRoom(this);
        isRunning = false;
        clientsInRoom.clear();
        info(String.format("closed", name));
    }
}

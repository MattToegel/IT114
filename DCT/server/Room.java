package DCT.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.logging.Level;



import DCT.common.Constants;

public class Room implements AutoCloseable {
    // protected static Server server;// used to refer to accessible server
    // functions
    private String name;
    protected List<ServerThread> clients = new ArrayList<ServerThread>();
    private boolean isRunning = false;
    // Commands
    private final static String COMMAND_TRIGGER = "/";
    private final static String CREATE_ROOM = "createroom";
    private final static String JOIN_ROOM = "joinroom";
    private final static String DISCONNECT = "disconnect";
    private final static String LOGOUT = "logout";
    private final static String LOGOFF = "logoff";
    private final static String FLIP = "flip";
    private final static String ROLL = "roll";
    private final static String MUTE = "mute";
    private final static String UNMUTE = "unmute";
    private final static String PM = "@";
    private static Logger logger = Logger.getLogger(Room.class.getName());

    public Room(String name) {
        this.name = name;
        isRunning = true;
    }

    public String getName() {
        return name;
    }

    public boolean isRunning() {
        return isRunning;
    }

    protected synchronized void addClient(ServerThread client) {
        if (!isRunning) {
            return;
        }
        client.setCurrentRoom(this);
        if (clients.indexOf(client) > -1) {
            logger.warning("Attempting to add client that already exists in room");
        } else {
            clients.add(client);
            client.sendResetUserList();
            syncCurrentUsers(client);
            sendConnectionStatus(client, true);
        }
    }

    protected synchronized void removeClient(ServerThread client) {
        if (!isRunning) {
            return;
        }
        // attempt to remove client from room
        try {
            clients.remove(client);
        } catch (Exception e) {
            logger.severe(String.format("Error removing client from room %s", e.getMessage()));
            e.printStackTrace();
        }
        // if there are still clients tell them this person left
        if (clients.size() > 0) {
            sendConnectionStatus(client, false);
        }
        checkClients();
    }

    private void syncCurrentUsers(ServerThread client) {
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread existingClient = iter.next();
            if (existingClient.getClientId() == client.getClientId()) {
                continue;// don't sync ourselves
            }
            boolean messageSent = client.sendExistingClient(existingClient.getClientId(),
                    existingClient.getClientName());
            if (!messageSent) {
                handleDisconnect(iter, existingClient);
                break;// since it's only 1 client receiving all the data, break if any 1 send fails
            }
        }
    }

    /***
     * Checks the number of clients.
     * If zero, begins the cleanup process to dispose of the room
     */
    private void checkClients() {
        // Cleanup if room is empty and not lobby
        if (!name.equalsIgnoreCase(Constants.LOBBY) && (clients == null || clients.size() == 0)) {
            close();
        }
    }

    Random flipRoll = new Random();
    
     
    protected synchronized void flip(ServerThread client) {
    	int result = flipRoll.nextInt(2);
    	String message;
    	if(result == 0)
    		message = " flipped heads";
    	else 
    		message = " flipped tails";
    	sendMessage(client, "<b><i><font color=gray>" + message + "</font></i></b>");
    }

    

    /***
     * Helper function to process messages to trigger different functionality.
     * 
     * @param message The original message being sent
     * @param client  The sender of the message (since they'll be the ones
     *                triggering the actions)
     */
    @Deprecated // not used in my project as of this lesson, keeping it here in case things
                // change
    private boolean processCommands(String message, ServerThread client) {
        boolean wasCommand = false;
        String response;
        try {
            if (message.startsWith(COMMAND_TRIGGER))  {
                String[] comm = message.split(COMMAND_TRIGGER);
                String part1 = comm[1];
                String[] comm2 = part1.split(" ");
                String command = comm2[0];
                String roomName;
                wasCommand = true;
                switch (command) {
                    case CREATE_ROOM:
                        roomName = comm2[1];
                        Room.createRoom(roomName, client);
                        break;
                    case JOIN_ROOM:
                        roomName = comm2[1];
                        Room.joinRoom(roomName, client);
                        break;
                    case DISCONNECT:
                    case LOGOUT:
                    case LOGOFF:
                        Room.disconnectClient(client, this);
                        break;
                    default:
                        wasCommand = false;
                        break;
                    //added "flip" and "roll" as potential cases
		case FLIP:
        flip(client);
        wasCommand = true;
        break;
    case ROLL:
    String[] diceParts = comm2[1].split("d"); //splits the d for die rolled times and sides on die
    if (diceParts.length != 2) {
        System.out.print("Invalid format. Use 'roll #d#'.");
    }

    int numDice = Integer.parseInt(diceParts[0]);
    int numSides = Integer.parseInt(diceParts[1]);

    if (numDice <= 0 || numSides <= 0) {
        System.out.print("Invalid dice parameters. Both numbers must be positive.");
    }

    // Simulate the dice roll!
    int total = 0;
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < numDice; i++) {
        int roll = new Random().nextInt(numSides) + 1;
        total += roll;
     result.append(roll);
        if (i < numDice - 1) {
         result.append(", ");
     }
    }
    response = "You got:" + String.format("rolled %dd%d and got %s (Total: %d)", numDice, numSides, result.toString(), total);
    sendMessage(client, "<b><i><font color=purple>" + response + "</font></i></b>");
    wasCommand = true;
    break;
      //added "mute" and "unmute" and potential cases
    case MUTE: 
        String[] muted = comm2[1].split(", ");
	    	List<String> muteList = new ArrayList<String>();
	    	// can mute multiple clients separated by comma jad237 1214
	    	for (String user : muted) {
	    		if (!client.isMuted(user)) {
		    		client.mute(user);
		    		muteList.add(user);
		    	}
	    	}
	    	sendPrivateMessage(client, " muted you", muteList);
	   
	    	wasCommand = true;
	    	break;
    case UNMUTE:
        String[] unmuted = comm2[1].split(", ");
        List<String> unmuteList = new ArrayList<String>();
        // can unmute multiple clients separated by comma
        for (String user : unmuted) {
            if (client.isMuted(user)) {
                client.unmute(user);
                unmuteList.add(user);
            }
        }
        sendPrivateMessage(client, " unmuted you", unmuteList);
        
        wasCommand = true;
        break;
    }
    } 
    // private message functionality
    // message will be sent to user specified with an "@" 
    else if (message.indexOf("@") > -1) {
        String command = "";
        String[] comm = message.split("@", 2);

        String part1 = comm[1];
        String[] comm2 = part1.split(" @");
        List<String> users = new ArrayList<String>();
            // get list of intended users 
        for (String user : comm2) {
            if(!user.equals(comm2[comm2.length-1])) {
                users.add(user.toLowerCase());
            }
            else {	// get message
                String[] pm = user.split(" ", 2);
                String last = pm[0];
                users.add(last.toLowerCase());
                command = pm[1];
            }
        }
        users.add(client.getClientName());
        sendPrivateMessage(client, "<b> /pm </b> " + command, users);

        wasCommand = true;
    }
    // change text functionality
    else {
        String command = message;
        //BOLD
        //makes sure there is at least one pair of "bold" symbols i.e *bold*
        if (command.matches("(.*)\\*(.+)\\*(.*)")) {
            int count = 0;
            String changeText = "";
        ArrayList<String> tags = new ArrayList<String>();
        for (int i = 0; i < command.length(); i++) {
            if (Character.toString(command.charAt(i)).equals("*")) {
                count++;
                if (count %2 != 0) {
                    tags.add("<b>");
                }
                else {
                    tags.add("</b>");
                }
                
            }
        }
        String [] bold = command.split("\\*");
        
        //accounts for "***" as a potential text option
        if (bold.length == 0) {
            changeText += "<b>*</b>";
                    if (command.length() > 3)
                changeText += command.substring(3);
            
        }
        for (int i = 0; i < bold.length; i++) {
            
            // accounts for odd number of "*"
            //  and also two "*" in a row
            if (tags.size() == 1 && tags.get(tags.size()-1).contains("<b>") || (bold[i].equals("") && (bold[i+1].equals("")))) {
                changeText += bold[i] + "*";
                tags.remove(0);
            }
            // convet "**" pairs to "<b></b>"
            else if (tags.size() > 1 || (tags.size() == 1 && tags.get(tags.size()-1).contains("</b>")) ){
                changeText += bold[i] + tags.get(0);
                tags.remove(0);
            }
            else changeText += bold[i];
            }
        wasCommand = true;
        
        // makes it so that conditions stack; 
        // can have more than one type of function applied on the same line
        if (changeText != "") {
            command = changeText;
        }
    }
        // ITALICS
        // same logic as bold
        if (command.matches("(.*)_(.+)_(.*)")) {
            int count = 0;
            String changeText = "";
            ArrayList<String> tags = new ArrayList<String>();
            for (int i = 0; i < command.length(); i++) {
                if (Character.toString(command.charAt(i)).equals("_")) {
                    count++;
                    if (count %2 != 0) {
                        tags.add("<i>");
                    }
                    else {
                        tags.add("</i>");
                    }
                }
            }
            String [] italics = command.split("_");
            if (italics.length == 0) {
                changeText += "<i>_</i>";
                        if (command.length() > 3)
                    changeText += command.substring(3);
                
            }
            for (int i = 0; i < italics.length; i++) {
                if (tags.size() == 1 && tags.get(tags.size()-1).contains("<i>") || (italics[i].equals("") && (italics[i+1].equals("")))) {
                    changeText += italics[i] + "_";
                    tags.remove(0);
                }
                else if (tags.size() > 1 || (tags.size() == 1 && tags.get(tags.size()-1).contains("</i>")) ){ //if (i % 2 == 0) {
                    changeText += italics[i] + tags.get(0);
                    tags.remove(0);
                }
                else changeText += italics[i];
                }
            wasCommand = true;
            if (changeText != "") {
                command = changeText;
            }
    }
        // UNDERLINE
        // same logic as bold
        if (command.matches("(.*)~(.+)~(.*)")) {
            int count = 0;
            String changeText = "";
            ArrayList<String> tags = new ArrayList<String>();
            for (int i = 0; i < command.length(); i++) {
                if (Character.toString(command.charAt(i)).equals("~")) {
                    count++;
                    if (count %2 != 0) {
                        tags.add("<u>");
                    }
                    else {
                        tags.add("</u>");
                    }
                    
                }
            }
            String [] underline = command.split("~");
            if (underline.length == 0) {
                changeText += "<b>~</b>";
                        if (command.length() > 3)
                    changeText += command.substring(3);
                
            }			
            for (int i = 0; i < underline.length; i++) {
                if (tags.size() == 1 && tags.get(tags.size()-1).contains("<u>") || (underline[i].equals("") && (underline[i+1].equals("")))) {
                    changeText += underline[i] + "~";
                    tags.remove(0);
                }
                else if (tags.size() > 1 || (tags.size() == 1 && tags.get(tags.size()-1).contains("</i>")) ){ //if (i % 2 == 0) {
                    changeText += underline[i] + tags.get(0);
                    tags.remove(0);
                }
                else changeText += underline[i];
                }
            wasCommand = true;
            if (changeText != "") {
                command = changeText;
            }
    }

     // COLOR
     // change color by declaring a color between two pound signs
        // e.x #red# this text would be red
        // to change back to black/default type '##'
        // color will stay black if declared color does not exist 
            //e.x #null# this will stay black
        if (command.matches("(.*)#(.+)#(.*)")) {
            String changeText = "";
            String colorString = "black";;
            String [] color = command.split("#", -1);
            System.out.println(Arrays.toString(color));
            if (color.length == 0) {
                changeText += command;	
            }	
                for (int i = 0; i < color.length; i++) {
                    
                    if (i % 2 != 0 && (!color[i].contains(" "))){
                        // accounts for odd number of #
                        if (i == color.length-1) {
                            changeText += "#" + color[i];
                        }
                        else { 
                            //if text is between two pound signs declare that as color variable
                            colorString = color[i];
                            if (colorString.equals("")) { colorString = "black"; }
                        }
                    }
                    // will not work if whitespace between pound signs i.e. # #
                    else if (i % 2 != 0 && (color[i].contains(" "))){
                        changeText += "#" + color[i] + "#";
                    }
                    // append message
                    else { 
                        changeText += "<font color="+colorString+">" + color[i] + "</font>";
                        colorString="black";
                    }
                }
            wasCommand = true;
            if (changeText != "") {
                command = changeText;
            }
            
        }
   
        if (wasCommand == true) {
            sendMessage(client, command);
    }

    }
}
//catch (EOFException e) {
       // ... this is fine
//	}
catch (Exception e) {
    e.printStackTrace();
}
return wasCommand;
}

protected void sendPrivateMessage(ServerThread sender, String message, List<String> users) {
        logger.log(Level.INFO, getName() + ": Sending message to " + users.size() + " clients");
        if (processCommands(message, sender)) {
            // it was a command, don't broadcast
            return;
        }
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread client = iter.next();
                // send message if sender not muted
            if(users.contains(client.getClientName().toLowerCase())) {
                if (!client.isMuted(sender.getClientName())){
                    boolean messageSent = client.send(sender.getClientName(), message);
                    if (!messageSent) {
                        iter.remove();
                    }
                }
            }
        }
        }




    // Command helper methods
    protected static void getRooms(String query, ServerThread client) {
        String[] rooms = Server.INSTANCE.getRooms(query).toArray(new String[0]);
        client.sendRoomsList(rooms,
                (rooms != null && rooms.length == 0) ? "No rooms found containing your query string" : null);
    }

    protected static void createRoom(String roomName, ServerThread client) {
        if (Server.INSTANCE.createNewRoom(roomName)) {
            Room.joinRoom(roomName, client);
        } else {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format("Room %s already exists", roomName));
        }
    }

    /**
     * Will cause the client to leave the current room and be moved to the new room
     * if applicable
     * 
     * @param roomName
     * @param client
     */
    protected static void joinRoom(String roomName, ServerThread client) {
        if (!Server.INSTANCE.joinRoom(roomName, client)) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format("Room %s doesn't exist", roomName));
        }
    }

    protected static void disconnectClient(ServerThread client, Room room) {
        client.disconnect();
        room.removeClient(client);
    }
    // end command helper methods

    /***
     * Takes a sender and a message and broadcasts the message to all clients in
     * this room. Client is mostly passed for command purposes but we can also use
     * it to extract other client info.
     * 
     * @param sender  The client sending the message
     * @param message The message to broadcast inside the room
     */
    protected synchronized void sendMessage(ServerThread sender, String message) {
        if (!isRunning) {
            return;
        }
        logger.info(String.format("Sending message to %s clients", clients.size()));
        if (sender != null && processCommands(message, sender)) {
            // it was a command, don't broadcast
            return;
        }
        long from = sender == null ? Constants.DEFAULT_CLIENT_ID : sender.getClientId();
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread client = iter.next();
            boolean messageSent = client.sendMessage(from, message);
            if (!messageSent) {
                handleDisconnect(iter, client);
            }
        }
    }

    protected synchronized void sendConnectionStatus(ServerThread sender, boolean isConnected) {
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread receivingClient = iter.next();
            boolean messageSent = receivingClient.sendConnectionStatus(
                    sender.getClientId(),
                    sender.getClientName(),
                    isConnected);
            if (!messageSent) {
                handleDisconnect(iter, receivingClient);
            }
        }
    }

    protected void handleDisconnect(Iterator<ServerThread> iter, ServerThread client) {
        iter.remove();
        logger.info(String.format("Removed client %s", client.getClientName()));
        sendMessage(null, client.getClientName() + " disconnected");
        checkClients();
    }

    public void close() {
        Server.INSTANCE.removeRoom(this);
        isRunning = false;
        clients.clear();
    }
}

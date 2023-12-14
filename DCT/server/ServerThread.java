package DCT.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import DCT.common.CellData;
import DCT.common.CellPayload;
import DCT.common.Character;
import DCT.common.CharacterPayload;
import DCT.common.Constants;
import DCT.common.Payload;
import DCT.common.PayloadType;
import DCT.common.Phase;
import DCT.common.PositionPayload;
import DCT.common.RoomResultPayload;

/**
 * A server-side representation of a single client
 */
public class ServerThread extends Thread {
    private Socket client;
    private String clientName;
    private boolean isRunning = false;
    private ObjectOutputStream out;// exposed here for send()
    // private Server server;// ref to our server so we can call methods on it
    // more easily jad237 1214
    private Room currentRoom;
    private static Logger logger = Logger.getLogger(ServerThread.class.getName());
    private long myClientId;
    List<String> mutedClients = new ArrayList<String>();

    public void mute(String name) {
     	name = name.trim().toLowerCase();
     	if (!isMuted(name)) {
     		mutedClients.add(name);
     		saveMuteList();
     		syncIsMuted(name, true);
     	}
     }
     
     //unmutes clients on call
  	public void unmute(String name) {
  		name = name.trim().toLowerCase();
     	if (isMuted(name)) {
     		System.out.println("ok");
     		mutedClients.remove(name);
     		System.out.println("yessir");
     		saveMuteList();
     		syncIsMuted(name, false);
     	}
  
     }
  	
  	//checks to see if client is muted
    public boolean isMuted(String name) {
     	name = name.trim().toLowerCase();
     	return mutedClients.contains(name);
   	} 
     
    // overwrites client's mutedClients list to a file
    void saveMuteList() {
    	 String data = clientName + ": " + String.join(", ", mutedClients);
    	 try {
 	 		FileWriter export = new FileWriter(clientName + ".txt");
 	 		BufferedWriter bw = new BufferedWriter(export);
 			bw.write("" + data); // convert StringBuilder to string
 			bw.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
    }

    // loads client's mutedClients list on reconnect jad237 1214
   void loadMuteList() {
	   	 File file = new File(clientName + ".txt");
	   	 if (file.exists()) {
		   	 try (Scanner reader = new Scanner(file)) {
		   		String dataFromFile = "";
		   		while (reader.hasNextLine()) {
			   		 String text = reader.nextLine();
			   		 dataFromFile += text;
		   		}
		   		dataFromFile = dataFromFile.substring(dataFromFile.indexOf(" ")+1);;
		   		if (!dataFromFile.strip().equals("") && !dataFromFile.isEmpty()) {
		   			List<String> getClients = Arrays.asList(dataFromFile.split(", "));
		   	    	for (String client : getClients) {
		   	    	    mute(client);
		   	    	    System.out.println("sync");
		   	    	}
		   		}
		   	 }
		   	catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
	   	 }
	   	 System.out.println(mutedClients.toString());
    }
	   	 
   // sends client mute or unmute to clientside through payload jad237 1214
   	protected boolean syncIsMuted(String clientName, boolean isMuted) {
		Payload p = new Payload();
		p.setPayloadType(PayloadType.MUTE);
		p.setClientName(clientName);
		p.setFlag(isMuted);
		return sendPayload(p);
    }
  
    public void setClientId(long id) {
        myClientId = id;
    }

    public long getClientId() {
        return myClientId;
    }

    public boolean isRunning() {
        return isRunning;
    }
    protected boolean send(String message) {
        // added a boolean so we can see if the send was successful
        try {
            out.writeObject(message);
            return true;
        }
        catch (IOException e) {
            logger.log(Level.INFO, "Error sending message to client (most likely disconnected)");
            e.printStackTrace();
            cleanup();
            return false;
        }
       }
   
        /***
         * Replacement for send(message) that takes the client name and message and
         * converts it into a payload
         * 
         * @param clientName
         * @param message
         * @return
         */
       protected boolean send(String clientName, String message) {
        Payload payload = new Payload();
        payload.setPayloadType(PayloadType.MESSAGE);
        payload.setClientName(clientName);
        payload.setMessage(message);
   
        return sendPayload(payload);
       }

    public ServerThread(Socket myClient, Room room) {
        logger.info("ServerThread created");
        // get communication channels to single client
        this.client = myClient;
        this.currentRoom = room;

    }
    private boolean sendPayload(Payload p) {
        try {
            out.writeObject(p);
            return true;
        }
        catch (IOException e) {
            logger.log(Level.INFO, "Error sending message to client (most likely disconnected)");
            e.printStackTrace();
            cleanup();
            return false;
        }
       }

    protected void setClientName(String name) {
        if (name == null || name.isBlank()) {
            logger.warning("Invalid name being set");
            return;
        }
        clientName = name;
    }

    public String getClientName() {
        return clientName;
    }

    protected synchronized Room getCurrentRoom() {
        return currentRoom;
    }

    protected synchronized void setCurrentRoom(Room room) {
        if (room != null) {
            currentRoom = room;
        } else {
            logger.info("Passed in room was null, this shouldn't happen");
        }
    }

    public void disconnect() {
        sendConnectionStatus(myClientId, getClientName(), false);
        logger.info("Thread being disconnected by server");
        isRunning = false;
        cleanup();
    }

    // send methods
    public boolean sendGridReset(){
        Payload p = new Payload();
        p.setPayloadType(PayloadType.GRID_RESET);
        return send(p);
    }
    public boolean sendCells(List<CellData> cells){
        CellPayload cp = new CellPayload();
        cp.setCellData(cells);
        return send(cp);
    }

    public boolean sendGridDimensions(int x, int y){
        PositionPayload pp = new PositionPayload();
        pp.setCoord(x, y);
        pp.setPayloadType(PayloadType.GRID); //override default payload type
        return send(pp);
    }
    public boolean sendCurrentTurn(long clientId) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.TURN);
        p.setClientId(clientId);
        return send(p);
    }

    public boolean sendCharacter(long clientId, Character character) {
        CharacterPayload cp = new CharacterPayload();
        cp.setCharacter(character);
        cp.setClientId(clientId);
        return send(cp);
    }

    public boolean sendPhaseSync(Phase phase) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.PHASE);
        p.setMessage(phase.name());
        return send(p);
    }

    public boolean sendReadyStatus(long clientId) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.READY);
        p.setClientId(clientId);
        return send(p);
    }

    public boolean sendRoomName(String name) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.JOIN_ROOM);
        p.setMessage(name);
        return send(p);
    }

    public boolean sendRoomsList(String[] rooms, String message) {
        RoomResultPayload payload = new RoomResultPayload();
        payload.setRooms(rooms);
        if (message != null) {
            payload.setMessage(message);
        }
        return send(payload);
    }

    public boolean sendExistingClient(long clientId, String clientName) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.SYNC_CLIENT);
        p.setClientId(clientId);
        p.setClientName(clientName);
        return send(p);
    }

    public boolean sendResetUserList() {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.RESET_USER_LIST);
        return send(p);
    }

    public boolean sendClientId(long id) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CLIENT_ID);
        p.setClientId(id);
        return send(p);
    }

    public boolean sendMessage(long clientId, String message) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setClientId(clientId);
        p.setMessage(message);
        return send(p);
    }

    public boolean sendConnectionStatus(long clientId, String who, boolean isConnected) {
        Payload p = new Payload();
        p.setPayloadType(isConnected ? PayloadType.CONNECT : PayloadType.DISCONNECT);
        p.setClientId(clientId);
        p.setClientName(who);
        // p.setMessage(isConnected ? "connected" : "disconnected");
        p.setMessage(String.format("%s the room %s", (isConnected ? "Joined" : "Left"), currentRoom.getName()));
        return send(p);
    }

    private boolean send(Payload payload) {
        try {
            logger.log(Level.FINE, "Outgoing payload: " + payload);
            out.writeObject(payload);
            logger.log(Level.INFO, "Sent payload: " + payload);
            return true;
        } catch (IOException e) {
            logger.info("Error sending message to client (most likely disconnected)");
            // uncomment this to inspect the stack trace
            // e.printStackTrace();
            cleanup();
            return false;
        } catch (NullPointerException ne) {
            logger.info("Message was attempted to be sent before outbound stream was opened: " + payload);
            // uncomment this to inspect the stack trace
            // e.printStackTrace();
            return true;// true since it's likely pending being opened
        }
    }

    // end send methods
    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());) {
            this.out = out;
            isRunning = true;
            Payload fromClient;
            while (isRunning && // flag to let us easily control the loop
                    (fromClient = (Payload) in.readObject()) != null // reads an object from inputStream (null would
                                                                     // likely mean a disconnect)
            ) {

                logger.info("Received from client: " + fromClient);
                processPayload(fromClient);

            } // close while loop
        } catch (Exception e) {
            // happens when client disconnects
            e.printStackTrace();
            logger.info("Client disconnected");
        } finally {
            isRunning = false;
            logger.info("Exited thread loop. Cleaning up connection");
            cleanup();
        }
    }

    void processPayload(Payload p) {
        switch (p.getPayloadType()) {
            case CONNECT:
                setClientName(p.getClientName());
                break;
            case DISCONNECT:
                Room.disconnectClient(this, getCurrentRoom());
                break;
            case MESSAGE:
                if (currentRoom != null) {
                    currentRoom.sendMessage(this, p.getMessage());
                } else {
                    // TODO migrate to lobby
                    logger.log(Level.INFO, "Migrating to lobby on message with null room");
                    Room.joinRoom(Constants.LOBBY, this);
                }
                break;
            case GET_ROOMS:
                Room.getRooms(p.getMessage().trim(), this);
                break;
            case CREATE_ROOM:
                Room.createRoom(p.getMessage().trim(), this);
                break;
            case JOIN_ROOM:
                Room.joinRoom(p.getMessage().trim(), this);
                break;
            case READY:
                try {
                    ((GameRoom) currentRoom).setReady(this);
                } catch (Exception e) {
                    logger.severe(String.format("There was a problem during readyCheck %s", e.getMessage()));
                    e.printStackTrace();
                }
                break;
            case CHARACTER:
                try {
                    CharacterPayload cp = (CharacterPayload) p;
                    // Here I'm making the assumption if the passed Character is null, it's likely a
                    // create request,
                    // if the passed character is not null, then some of the properties will be used
                    // for loading
                    if (cp.getCharacter() == null) {
                        ((GameRoom) currentRoom).createCharacter(this, cp.getCharacterType());
                    } else {
                        ((GameRoom) currentRoom).loadCharacter(this, cp.getCharacter());
                    }
                } catch (Exception e) {
                    logger.severe(String.format("There was a problem during character handling %s", e.getMessage()));
                    e.printStackTrace();
                }
                break;
            case MOVE:
                try {
                    PositionPayload pp = (PositionPayload) p;
                    ((GameRoom) currentRoom).handleMove(pp.getX(), pp.getY(), this);
                } catch (Exception e) {
                    logger.severe(String.format("There was a problem during position handling %s", e.getMessage()));
                    e.printStackTrace();
                }
                break;
            default:
                break;

        }

    }

    private void cleanup() {
        logger.info("Thread cleanup() start");
        try {
            client.close();
        } catch (IOException e) {
            logger.info("Client already closed");
        }
        logger.info("Thread cleanup() complete");
    }
}

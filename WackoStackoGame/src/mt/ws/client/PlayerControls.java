package mt.ws.client;

import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import mt.ws.dataobject.Player;
import mt.ws.network.client.SocketClient;


class PlayerControls{
	public static boolean LEFT_DOWN = false, RIGHT_DOWN = false, UP_DOWN = false, DOWN_DOWN = false, SPACE_DOWN = false;
	private static Point pdd = new Point(-2,-2);
	public Point getDirection() {
		return pdd;
	}
	private static void handleControls() {
		if(!UP_DOWN && !DOWN_DOWN) {
			pdd.y = 0;
		}
		if(!LEFT_DOWN && !RIGHT_DOWN) {
			pdd.x = 0;
		}
		if(LEFT_DOWN) {
			pdd.x = -1;
		}
		if(RIGHT_DOWN) {
			pdd.x = 1;
		}
		if(UP_DOWN) {
			pdd.y = -1;
		}
		if(DOWN_DOWN) {
			pdd.y = 1;
		}
	}
	public static void setKeyBindings(InputMap im, ActionMap am) {
		
		//bind key actions to action map
		im.put(KeyStroke.getKeyStroke("pressed UP"), "UAD");
		im.put(KeyStroke.getKeyStroke("pressed DOWN"), "DAD");
		im.put(KeyStroke.getKeyStroke("pressed LEFT"), "LAD");
		im.put(KeyStroke.getKeyStroke("pressed RIGHT"), "RAD");
		
		im.put(KeyStroke.getKeyStroke("released UP"), "UAU");
		im.put(KeyStroke.getKeyStroke("released DOWN"), "DAU");
		im.put(KeyStroke.getKeyStroke("released LEFT"), "LAU");
		im.put(KeyStroke.getKeyStroke("released RIGHT"), "RAU");
		
		im.put(KeyStroke.getKeyStroke("pressed SPACE"), "SPACE");
		
		//bind Action to Action map
		am.put("UAD", new MoveAction(true, 0, -1));
		am.put("DAD", new MoveAction(true, 0, 1));
		am.put("LAD", new MoveAction(true, -1,0));
		am.put("RAD", new MoveAction(true, 1, 0));
		
		am.put("UAU", new MoveAction(false, 0, -1));
		am.put("DAU", new MoveAction(false, 0, 1));
		am.put("LAU", new MoveAction(false, -1,0));
		am.put("RAU", new MoveAction(false, 1, 0));
		
		am.put("SPACE", new TagAction());
	}
	static void applyControls(Player myPlayer, SocketClient client) {
		
		if(myPlayer != null) {
			handleControls();
			//apply direction and see if it changed
			if(myPlayer.setDirection(pdd)) {
				//send to server
				Point mp = myPlayer.getDirection();
				//System.out.println("Direction: " + mp.toString());
				//TODO Send new Direction over Network
				//client.send(id, PayloadType.CHANGE_DIRECTION, mp.x, mp.y);
				client.SyncDirection(mp);
			}
			if(PlayerControls.SPACE_DOWN) {
				PlayerControls.SPACE_DOWN = false;
				if(myPlayer.tryJump()) {
					
					//TODO send to network
				}
				/*if(myPlayer.tryToTag()) {
					//TODO send Tag action to server
					client.send(id, PayloadType.TRIGGER_TAG);
				}*/
			}
		}
	}
}
class TagAction extends AbstractAction{
	private static final long serialVersionUID = 397012257495431091L;

	@Override
	public void actionPerformed(ActionEvent e) {
		PlayerControls.SPACE_DOWN = true;
	}
	
}
class MoveAction extends AbstractAction{
	private static final long serialVersionUID = 5137817329873449021L;
	int x,y;
	boolean pressed = false;
	MoveAction(boolean pressed, int x, int y){
		this.x = x;
		this.y = y;
		this.pressed = pressed;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if (x == -1) {
			PlayerControls.LEFT_DOWN = pressed;
		}
		if (x == 1) {
			PlayerControls.RIGHT_DOWN = pressed;
		}
		if (y == -1) {
			PlayerControls.UP_DOWN = pressed;
		}
		if (y == 1) {
			PlayerControls.DOWN_DOWN = pressed;
		}
	}
	
}
package mt.ws.client;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import org.dyn4j.geometry.Vector2;

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
	/**
	 * Used for JComponents since they have InputMap/ActionMap
	 * @param im
	 * @param am
	 */
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
				Vector2 mp = myPlayer.getDirection();
				//Point mp = new Point((int)myPlayer.getDirection().x, (int)myPlayer.getDirection().y);
				System.out.println("Sending dir change to cient");
				client.SyncDirection(mp, myPlayer.getID());
			}
			if(PlayerControls.SPACE_DOWN) {
				PlayerControls.SPACE_DOWN = false;
				if(myPlayer.tryJump()) {
					
					//TODO send to network
				}
			}
		}
	}
	/**
	 * Used for Canvas since it doesn't have an InputMap/ActionMap
	 * @author MattT
	 *
	 */
	public static class CustomKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
					PlayerControls.LEFT_DOWN = true;
					break;
				case KeyEvent.VK_RIGHT:
					PlayerControls.RIGHT_DOWN = true;
					break;
				case KeyEvent.VK_UP:
					PlayerControls.UP_DOWN = true;
					break;
				case KeyEvent.VK_DOWN:
					PlayerControls.DOWN_DOWN = true;
				case KeyEvent.VK_SPACE:
					PlayerControls.SPACE_DOWN = true;
					break;
			}
			
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				PlayerControls.LEFT_DOWN = false;
				break;
			case KeyEvent.VK_RIGHT:
				PlayerControls.RIGHT_DOWN = false;
				break;
			case KeyEvent.VK_UP:
				PlayerControls.UP_DOWN = false;
				break;
			case KeyEvent.VK_DOWN:
				PlayerControls.DOWN_DOWN = false;
			case KeyEvent.VK_SPACE:
				PlayerControls.SPACE_DOWN = false;
				break;
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
		System.out.println("Key pressed");
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
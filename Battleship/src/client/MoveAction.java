package client;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

public class MoveAction extends AbstractAction {
    private static final long serialVersionUID = 5137817329873449021L;
    int key;
    boolean pressed = false;

    MoveAction(int k, boolean pressed) {
	key = k;
	this.pressed = pressed;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	switch (key) {
	case KeyEvent.VK_W:
	    KeyStates.W = pressed;
	    break;
	case KeyEvent.VK_S:
	    KeyStates.S = pressed;
	    break;
	case KeyEvent.VK_A:
	    KeyStates.A = pressed;
	    break;
	case KeyEvent.VK_D:
	    KeyStates.D = pressed;
	    break;
	}
    }
}
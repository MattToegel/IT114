package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import core.Countdown;

public class TicketCollector extends Player {
    Point chatOffset = new Point(40, -20);
    String chat = "Tickets Please!";
    boolean showChat = false;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public void setChatSide(int dir) {
	chatOffset.x *= dir;
    }

    public void showChat(boolean b, String message) {
	if (message != null) {
	    chat = message;
	}
	showChat = b;
	new Countdown("", 2, (x) -> {
	    showChat = false;
	});
    }

    @Override
    public boolean draw(Graphics g) {
	if (super.draw(g)) {
	    if (showChat) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Monospaced", Font.PLAIN, 12));
		g.drawString(chat, position.x + chatOffset.x, position.y + chatOffset.y);
	    }
	    return true;
	}
	return false;
    }
}
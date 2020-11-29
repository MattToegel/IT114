package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import core.Countdown;
import core.Helpers;

public class TicketCollector extends Player {
    private Point chatOffset = new Point(40, -20);
    private String chat = "Tickets Please!";
    private boolean showChat = false;
    private int sum = 0;
    private int hiddenValue = 0;
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

    public void loadTickets(List<Ticket> tickets) {
	sum = 0;
	for (Ticket t : tickets) {
	    sum += t.getValue();
	}
	System.out.println("Ticket sum: " + sum);
	hiddenValue = Helpers.getNumberBetween(1, sum);
    }

    public boolean isTicketValid(Ticket t) {
	hiddenValue -= t.getValue();
	if (hiddenValue <= 0) {
	    hiddenValue = Helpers.getNumberBetween(1, sum);
	    return false;
	}
	return true;
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
package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.io.Serializable;

import core.GameObject;

public class Ticket extends GameObject implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6088251166673414031L;
    private Color color = Color.DARK_GRAY;
    private Point nameOffset = new Point(0, -5);
    Player holder = null;
    private int value = 0;

    public Ticket(String name) {
	setName(name);
    }

    public void setValue(int v) {
	value = v;
    }

    public int getValue() {
	return value;
    }

    public boolean isAvailable() {
	return holder == null;
    }

    public void setPlayer(Player p) {
	holder = p;
    }

    public Player getHolder() {
	return holder;
    }

    public String getHolderName() {
	if (holder == null) {
	    return null;
	}
	return holder.getName();
    }

    @Override
    public void setSize(int x, int y) {
	super.setSize(x, y);
	// math used are just magic numbers, played with it until it looked ok
	nameOffset.x = (int) (x * .1);
	nameOffset.y = (int) (y / 1.5f);
    }

    /**
     * Gets called by the game engine to draw the current location/size
     */
    @Override
    public boolean draw(Graphics g) {
	// using a boolean here so we can block drawing if isActive is false via call to
	// super
	if (super.draw(g)) {
	    g.setColor(color);
	    if (holder != null) {
		// TODO this fixes it!
		// position = holder.getPosition();
		position.x = holder.getPosition().x;
		position.y = holder.getPosition().y;
	    }
	    g.fillRect(position.x, position.y, size.width, size.height);
	    g.setColor(Color.WHITE);
	    g.setFont(new Font("Monospaced", Font.PLAIN, 12));
	    g.drawString(getName(), position.x + nameOffset.x, position.y + nameOffset.y);
	}
	return true;
    }

    @Override
    public String toString() {
	return String.format("Name: %s, p: (%d,%d), s: (%d, %d), d: (%d, %d), isAcitve: %s", name, position.x,
		position.y, speed.x, speed.y, direction.x, direction.y, isActive);
    }
}
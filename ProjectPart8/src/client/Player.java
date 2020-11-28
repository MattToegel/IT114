package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.io.Serializable;

import core.GameObject;

public class Player extends GameObject implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6088251166673414031L;
    Color color = Color.RED;
    Point nameOffset = new Point(0, 0);
    Ticket ticket = null;
    Chair chair = null;
    boolean isReady = false;
    long lastAction = -1L;
    boolean isLocked = false;
    boolean isKicked = false;

    /**
     * Override for name so we can calculate a better offset
     */
    @Override
    public void setName(String name) {
	super.setName(name);
	if (name != null) {
	    nameOffset.y = -(int) (size.width * .9);
	    nameOffset.x = -(name.length() * 3);
	}
    }

    public void setLastAction(Long l) {
	lastAction = l;
    }

    public long getTimeBetweenLastAction(Long compare) {
	return compare - lastAction;
    }

    public long getLastAction() {
	return lastAction;
    }

    public void setLocked(boolean l) {
	isLocked = l;
    }

    public boolean isLocked() {
	return isLocked;
    }

    public void setReady(boolean r) {
	isReady = r;
    }

    public boolean isReady() {
	return isReady;
    }

    public boolean hasTicket() {
	return ticket != null;
    }

    public void setTicket(Ticket n) {
	ticket = n;
    }

    public Ticket takeTicket() {
	if (ticket == null) {
	    return null;
	}
	Ticket t = ticket;
	ticket = null;
	return t;
    }

    public void setChair(Chair c) {
	chair = c;
    }

    public boolean isSitting() {
	return chair != null;
    }

    public void unsit() {
	chair = null;
    }

    @Override
    public void move() {
	if (!isActive) {
	    return;
	}
	if (isLocked && !isKicked) {
	    return;
	}
	int sx = speed.x;
	int sy = speed.y;
	if (isKicked) {
	    sx *= 10;
	    sy *= 10;
	}
	previousPosition.x = position.x;
	previousPosition.y = position.y;
	position.x += (sx * direction.x);
	position.y += (sy * direction.y);
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
	    g.fillOval(position.x, position.y, size.width, size.height);
	    g.setColor(Color.WHITE);
	    g.setFont(new Font("Monospaced", Font.PLAIN, 12));
	    // updated to be drawn at an offset from "center"
	    // not using getCenter() as to reduce garbage Point() objects being created each
	    // frame
	    g.drawString(name, (int) (position.x + (size.width * .5)) + nameOffset.x,
		    (int) (position.y + (size.height * .5)) + nameOffset.y);
	    return true;
	}
	return false;
    }

    @Override
    public String toString() {
	return String.format("Name: %s, p: (%d,%d), s: (%d, %d), d: (%d, %d), isAcitve: %s", name, position.x,
		position.y, speed.x, speed.y, direction.x, direction.y, isActive);
    }
}
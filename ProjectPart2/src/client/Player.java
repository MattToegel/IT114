package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import core.GameObject;
import core.Helpers;

public class Player extends GameObject {
    Color color = Color.YELLOW;
    Point nameOffset = new Point(0, 5);

    @Override
    public boolean draw(Graphics g) {
	// using a boolean here so we can block drawing if isActive is false via call to
	// super
	if (super.draw(g)) {
	    g.setColor(color);
	    g.fillOval(position.x, position.y, size.width, size.height);
	    g.setColor(Color.WHITE);
	    g.setFont(new Font("Monospaced", Font.PLAIN, 12));
	    g.drawString("Name: " + name, position.x + nameOffset.x, position.y + nameOffset.y);
	}
	return true;
    }

    /***
     * Sets the direction of this object. Use the return value to determine if a
     * network request should sync
     * 
     * @param x
     * @param y
     * @return returns true if changed, false if it's the same.
     */
    public boolean setDirection(int x, int y) {
	x = Helpers.clamp(x, -1, 1);
	y = Helpers.clamp(y, -1, 1);
	boolean changed = false;
	if (direction.x != x) {
	    direction.x = x;
	    changed = true;
	}
	if (direction.y != y) {
	    direction.y = y;
	    changed = true;
	}
	return changed;
    }

    public Point getDirection() {
	return direction;
    }

    @Override
    public String toString() {
	return String.format("Name: %s, p: (%d,%d), s: (%d, %d), isAcitve: %s", name, position.x, position.y, speed.x,
		speed.y, isActive);
    }
}
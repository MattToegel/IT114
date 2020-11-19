package core;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.io.Serializable;

public abstract class GameObject implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -9145932773417678588L;
    protected Point position = new Point(0, 0);
    protected Point previousPosition = new Point(0, 0);
    protected Point speed = new Point(2, 2);
    protected Point direction = new Point(0, 0);
    protected Dimension size = new Dimension(25, 25);
    protected String name = "";
    protected boolean isActive = true;

    /**
     * Set the x,y speed of the object, values can only be positive. Set -1 to
     * ignore speed change for that dimension. A value of 0 would stop this object
     * from moving on that dimension Use setDirection for changes in direction
     * 
     * @param x
     * @param y
     */
    public void setSpeed(int x, int y) {
	// not using Math.max here since we want to be able to ignore a speed dimension
	// Math.max would set it to a value
	if (x > -1) {
	    speed.x = x;
	}
	if (y > -1) {
	    speed.y = y;
	}
    }

    /**
     * Sets the dimensions of the object
     * 
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
	size.width = Math.max(0, width);
	size.height = Math.max(0, height);
    }

    public Dimension getSize() {
	return size;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return this.name;
    }

    /**
     * Enable or disable object
     * 
     * @param isActive
     */
    public void setActive(boolean isActive) {
	this.isActive = isActive;
    }

    public boolean isActive() {
	return this.isActive;
    }

    /**
     * Call to apply speed/direction to position
     */
    public void move() {
	if (!isActive) {
	    return;
	}
	previousPosition.x = position.x;
	previousPosition.y = position.y;
	position.x += (speed.x * direction.x);
	position.y += (speed.y * direction.y);
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

    /**
     * Instantly sets a position
     * 
     * @param position
     */
    public void setPosition(Point position) {
	previousPosition.x = position.x;
	previousPosition.y = position.y;
	this.position.x = position.x;
	this.position.y = position.y;
    }

    public Point getPosition() {
	return position;
    }

    /***
     * Generates a new point representing the center of the object. Safe to edit.
     * 
     * @return
     */
    public Point getCenter() {
	Point p = (Point) position.clone();
	p.x += size.width * .5;
	p.y += size.height * .5;
	return p;
    }

    /**
     * Checks if previous position differs from current position
     * 
     * @return
     */
    public boolean changedPosition() {
	return (previousPosition.x != position.x || previousPosition.y != position.y);
    }

    /**
     * use to determine if subclass should draw due to active status
     * 
     * @param g
     * @return
     */
    public boolean draw(Graphics g) {
	if (!isActive) {
	    return false;
	}
	return true;
    }
}

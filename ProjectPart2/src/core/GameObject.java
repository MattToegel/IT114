package core;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

public abstract class GameObject {
    protected Point position = new Point(0, 0);
    protected Point speed = new Point(2, 2);
    protected Point direction = new Point(0, 0);
    protected Dimension size = new Dimension(100, 100);
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

    public void setSize(int width, int height) {
	size.width = Math.max(0, width);
	size.height = Math.max(0, height);
    }

    public void setName(String name) {
	this.name = name;
    }

    public void setActive(boolean isActive) {
	this.isActive = isActive;
    }

    public boolean isActive() {
	return this.isActive;
    }

    public void move() {
	if (!isActive) {
	    return;
	}
	position.x += speed.x * direction.x;
	position.y += speed.y * direction.y;
    }

    public boolean draw(Graphics g) {
	if (!isActive) {
	    return false;
	}
	return true;
    }
}
package mt.ws.dataobject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import mt.ws.client.GameEngine;

public class Player{
	public Point position = new Point(300,300);
	public Point speed = new Point(0,0);
	public Point direction = new Point(0,0);
	Point lastDirection = new Point(0,0);
	public Dimension size = new Dimension(50,50);
	public Color myColor;
	public String name = "Sample name";
	private Point center = new Point();
	public boolean changedDirection = false;
	public Player() {
		Random random = new Random();
		myColor = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
	}
	public Point getLastDirection() {
		return lastDirection;
	}
	public void draw(Graphics2D g2d) {
		g2d.setColor(myColor);
		g2d.fillRect(
				position.x - (size.width/2),
				position.y - (size.height/2),
				size.width,
				size.height);
		g2d.drawString(name, position.x - (size.width/2),
				position.y + (size.height*.9f));
	}
	boolean _isOutofBounds(int halfWidth, int halfHeight, Dimension bounds) {
		return(position.x - halfWidth <= 0
				|| position.x + halfWidth >= bounds.width
				|| position.y - halfHeight <= 0
				|| position.y + halfHeight >= bounds.height);
	}
	boolean _isOutOfBoundsY(int halfHeight, int height) {
		return(position.y - halfHeight <= 0
				|| position.y + halfHeight >= height);
	}
	boolean _isOutOfBoundsX(int halfWidth, int width) {
		return(position.x - halfWidth <= 0
				|| position.x + halfWidth >= width);
	}
	boolean _canMoveVert(int top, int bottom, int height) {
		if(direction.y < 0) {
			isGrounded = false;
			//up
			if(top > 0) {
				//ok
				return true;
			}
			
		}
		else if(direction.y > 0) {
			//down
			if(bottom < height) {
				//ok
				isGrounded = false;
				return true;
			}
			else {
				isGrounded = true;
			}
		}
		return false;
	}
	boolean _canMoveHorz(int left, int right, int width) {
		if (direction.x < 0) {
			//left
			if(left > 0) {
				//ok
				return true;
			}
		}
		else if (direction.x > 0) {
			//right
			if(right < width) {
				//ok
				return true;
			}
		}
		return false;
	}
	public void move(Dimension bounds) {
		int halfWidth = size.width /2;
		int halfHeight = size.height/2;
		int l = position.x - halfWidth;
		int r = position.x + halfWidth;
		int t = position.y - halfHeight;
		int b = position.y + halfHeight;
	
		//only move x if we're not currently out of bounds 
		//in the direction of travel
		if(_canMoveHorz(l, r, bounds.width)) {
			position.x += (direction.x * speed.x);
		}
		//only move y if we're not currently out of bounds 
		//in the direction of travel
		if(_canMoveVert(t, b, bounds.height)) {
			position.y += (direction.y * speed.y);
		}
		
		/*if((position.x - halfWidth <= 0) 
				|| (position.x + halfWidth >= bounds.width)) {
			direction.x *= -1;
		}
		//hit top/bottom reflect direction y
		if((position.y - halfHeight <= 0)
				|| (position.y+halfHeight >= bounds.height)) {
			direction.y *= -1;
		}*/
		//used to determine intent of change to send across network
		if(lastDirection != direction) {
			changedDirection = true;
			lastDirection = direction;
		}
	}
	public void setPosition(int x, int y) {
		//check distance for snapping or lerping
		double dist = GameEngine.distance(x, y, center.x, center.y);
		if(dist < 50) {
			float f = 1 - (float)dist/50f;//invert the %
			//apply lerp to easy the x,y to the new coordinate to reduce jitty
			center.x = (int)GameEngine.lerp(x, center.x, f);
			center.y = (int)GameEngine.lerp(y, center.y, f);
		}
		else {
			center.x = x;
			center.y = y;
		}
	}
	/***
	 * Set x, y direction
	 * To ignore pass -2 for a value
	 * Ran on the client side checking if direction was actually changed
	 * @param p
	 * p.x can be 1, 0, -1 [-2 to ignore]
	 * p.y can be 1, 0, -1 [-2 to ignore]
	 */
	public boolean setDirection(Point p) {
		boolean changed = false;
		if(isValidDirection(p.x, direction.x)) {
			direction.x = p.x;
			changed = true;
		}
		if(isValidDirection(p.y, direction.y)) {
			direction.y = p.y;
			changed = true;
		}
		return changed;
	}
	/**
	 * Forced updates used particular for updating from server responses
	 * @param x
	 * @param y
	 */
	public void setDirection(int x, int y) {
		direction.x = x;
		direction.y = y;
	}
	public Point getDirection() {
		return this.direction;
	}
	/***
	 * 
	 * @param d
	 * @param current
	 * @return
	 * false if value equals the ignore value
	 * true if value is -1, 0, 1
	 * false if value didn't change
	 */
	static boolean isValidDirection(int d, int current) {
		if(d == -2) {
			return false;
		}
		if(d >= -1 && d <= 1) {
			if(d != current) {
				return true;
			}
		}
		
		return false;
	}
	/**
	 * Triggered from player action to attempt a tag
	 * @return
	 */
	//check against this
	boolean isJumping = false;
	boolean canJump = true;
	boolean isGrounded = false;
	private ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(3);
	public boolean isJumping() {
		return isJumping;
	}
	int tempSpeed = 0;
	public boolean tryJump() {
		if(!isJumping && canJump && isGrounded) {
			isJumping = true;
			tempSpeed = speed.y;
            speed.y *= 2;
			//try to tag for 250 ms
			//when this expires, tagging action will stop
			exec.schedule(()->{
		              isJumping = false;
		              canJump = false;
		              speed.y = tempSpeed;
		              //delay when we can tag again
		              //after 2 seconds we can try to tag again
		              exec.schedule(()->{
		            	  canJump = true;
		            	  
		              }, 250, TimeUnit.MILLISECONDS);
		     }, 500, TimeUnit.MILLISECONDS);
		}
		return isJumping;
	}
	
}
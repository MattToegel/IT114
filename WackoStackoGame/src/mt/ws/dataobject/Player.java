package mt.ws.dataobject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dyn4j.geometry.Vector2;

import mt.ws.client.GameEngine;

public class Player extends GameObject{
	private int id;
	private Vector2 position = new Vector2(300,300);
	private Vector2 speed = new Vector2(0,0);
	private Vector2 direction = new Vector2(0,0);
	private Vector2 lastDirection = new Vector2(0,0);
	private Color myColor;
	private String name = "Sample name";
	private Point center = new Point();
	private boolean changedDirection = false;
	private int gravity = 1;
	public void overrideGravity(int g) {
		gravity = g;
	}
	public void setID(int id) {
		this.id = id;
	}
	public int getID() {
		return id;
	}
	public Vector2 getPosition() {
		return position;
	}
	public Vector2 getSpeed() {
		return speed;
	}
	public Vector2 getDirection() {
		return direction;
	}
	public Vector2 getLastDirection() {
		return lastDirection;
	}
	public boolean didChangeDirection() {
		return changedDirection;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setRandomSpeed() {
		Random random = new Random();
		speed.x = random.nextInt(3)+1;
		speed.y = random.nextInt(3)+1;
	}
	public Player() {
		Random random = new Random();
		myColor = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
		speed = new Vector2(5,5);
	}
	void _physicalMove() {
		Vector2 r = new Vector2();
		r.x = direction.x * speed.x;
		r.y = direction.y * speed.y;
		this.applyForce(r);
	}
	public void move() {
		_physicalMove();
	}
	public void overrideDirectionY(int y) {
		this.direction.y = y;
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
		if(isValidDirection((int)p.x, (int)direction.x)) {
			direction.x = p.x;
			changed = true;
		}
		if(isValidDirection((int)p.y, (int)direction.y)) {
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
		//honestly I don't recall why there's a hardcoded check for -2
		//since this was pulled from an older project
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
	
	private ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(3);
	public boolean isJumping() {
		return isJumping;
	}
	int tempSpeed = 0;
	public boolean tryJump() {
		System.out.println("Trying jump");
		System.out.println("isJumping: " + isJumping);
		System.out.println("canJump: " + canJump);
		System.out.println("isGrounded: " + isGrounded);
		System.out.println("Expected False, True, True");
		if(!isJumping && canJump && isGrounded) {
			isJumping = true;
			System.out.println("Jumped");
			//tempSpeed = (int)speed.y;
            //speed.y *= 2;
			this.applyImpulse(new Vector2(0,5));
			//try to tag for 250 ms
			//when this expires, tagging action will stop
			exec.schedule(()->{
		              isJumping = false;
		              canJump = false;
		              //speed.y = tempSpeed;
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
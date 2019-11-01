

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Player {
	private Color color = Color.GRAY;
	private Color previousColor = color;
	private Point center = new Point();
	private Point direction = new Point();
	private int radius = 20;
	private int diameter = 2*radius;
	private int speed = 1;
	private int baseSpeed = 1;//TODO server should own this
	private String name = "";
	private boolean isIt = false;
	private Dimension nameSize = new Dimension(0,0);
	private boolean isTryingToTag = false;
	private boolean canTagAgain = false;
	private ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(3);
	private boolean isBoosted = false;
	private int numOfTags = 0;
	private int numTagged = 0;
	public boolean blacklist = false;
	private int id = -1;
	public Player(String name) {
		this.name = name;
		if(name.toLowerCase().contains("comedian")) {
			blacklist = true;
		}
		else {
			blacklist = false;
		}
		speed = baseSpeed;
		diameter = radius * 2;
		center.x = 100;
		center.y = 100;
		direction.x = 1;
		direction.y = 1;
	}
	public void syncStats(int tags, int tagged) {
		numOfTags = tags;
		numTagged = tagged;
	}
	public void incrementTagged() {
		numTagged++;
	}
	public int getNumberTagged() {
		return numTagged;
	}
	public void incrementTags() {
		numOfTags++;
	}
	public int getNumberOfTags() {
		return numOfTags;
	}
	/***
	 * Overrides name from constructor
	 * @param n
	 */
	public void setName(String n) {
		this.name = n;
		if(name.toLowerCase().contains("comedian")) {
			blacklist = true;
		}
		else {
			blacklist = false;
		}
	}
	public String getName() {
		return this.name;
	}
	public void setID(int id) {
		this.id = id;
	}
	public int getID() {
		return this.id;
	}
	public int getRadius() {
		return radius;
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
	public Point getPosition() {
		return center;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	public void setIsIt(boolean isIt) {
		color = (isIt?Color.BLACK:Color.GRAY);
		if(isIt) {
			canTagAgain = true;
		}
		this.isIt = isIt;
	}
	public boolean isIt() {
		return isIt;
	}
	int xi, yi, xMin, xMax, yMin, yMax;
	int lastX = 10000, lastY = 10000;
	/**
	 * Does all the movement calculations for the player including bounds check
	 * @return
	 */
	public Point move() {
		/***
		 * Get center coordinates
		 * Check if Direction is set, then apply movement
		 */
		xi = center.x;
		if(direction.x != 0) {
			xi += (direction.x * speed);
		}
		yi = center.y;
		if(direction.y != 0) {
			yi += (direction.y * speed);
		}
		/***
		 * Adjust for radius
		 */
		xMin = xi - radius;
		xMax = xi + radius;
		yMin = yi - radius;
		yMax = yi + radius;
		/***
		 * Check if bounds are within play area
		 * Set new position if within play area
		 */
		//System.out.println(GameEngine.GetPlayArea());
		if(xMin >= 0 && xMax <= GameEngine.GetPlayArea().width) {
			center.x = xi;
		}
		if(yMin >= 0 && yMax <= GameEngine.GetPlayArea().height) {
			center.y = yi;
		}
		lastX = center.x;
		lastY = center.y;
		return center;
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
	public boolean tryToTag() {
		if(isIt && !isTryingToTag && canTagAgain) {
			isTryingToTag = true;
			//try to tag for 250 ms
			//when this expires, tagging action will stop
			exec.schedule(()->{
		              isTryingToTag = false;
		              canTagAgain = false;
		              //delay when we can tag again
		              //after 2 seconds we can try to tag again
		              exec.schedule(()->{
		            	  canTagAgain = true;
		              }, 2000, TimeUnit.MILLISECONDS);
		     }, 250, TimeUnit.MILLISECONDS);
		}
		return isTryingToTag;
	}
	/***
	 * Temporarily increases movement speed
	 */
	public void applyBoost(int targetSpeed) {
		if(!isBoosted) {
			isBoosted = true;
			speed = targetSpeed;
			previousColor = color;
			color = Color.BLUE;
			System.out.println("Is boosted");
			exec.schedule(new Runnable() {
		          public void run() {
		              isBoosted = false;
		              speed = baseSpeed;
		              color = previousColor;
		              System.out.println("Is not boosted");
		          }
		     }, 3, TimeUnit.SECONDS);
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
		return direction;
	}
	
	/***
	 * Draws a string that can handle new line characters
	 * @param g
	 * @param text
	 * @param x
	 * @param y
	 */
	private void drawString(Graphics2D g, String text, int x, int y) {
		int i = 0;
        for (String line : text.split("\n")) {
            g.drawString(line, x, y += i * g.getFontMetrics().getHeight());
        	i++;
		}
    }
	/***
	 * Draws our entire player including name/tag
	 * @param g2d
	 */
	protected void paint(Graphics2D g2d) {
        if (center != null && g2d != null) {
        	if(isTryingToTag) {
        		g2d.setColor(Color.CYAN);
        		g2d.fillOval(center.x - (radius+1), center.y - (radius+1), (diameter+2), (diameter+2));
        	}
            g2d.setColor(color);
            if(blacklist) {
            	g2d.fillRect(center.x - radius, center.y - radius, diameter, diameter);
            }
            else {
            	g2d.fillOval(center.x - radius, center.y - radius, diameter, diameter);
            }
            
            g2d.setColor(Color.WHITE);
            if(nameSize.width == 0) {
            	FontMetrics fm = g2d.getFontMetrics();
                nameSize.width = (int) fm.getStringBounds(name, g2d).getWidth();
                nameSize.height = fm.getMaxAscent();
            }
            g2d.setColor(Color.WHITE);
            drawString(g2d, name + "\n(" + id +")", (int) (center.x - (nameSize.width * .51)),
                (int) (center.y + (nameSize.height * .01f)));
            
        }
    }
}

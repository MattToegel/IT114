

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
	private int radius = 15;
	private int diameter = 2*radius;
	private int speed = 1;
	private int baseSpeed = 1;//TODO server should own this
	private String name = "";
	private boolean isIt = false;
	private Dimension nameSize = new Dimension(0,0);
	private Dimension playArea = new Dimension(0,0);
	private boolean isTryingToTag = false;
	private boolean canTagAgain = false;
	private ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(3);
	private boolean isBoosted = false;
	private int numOfTags = 0;
	private int numTagged = 0;
	public boolean blacklist = false;
	public Player(String name, Dimension playArea) {
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
		this.playArea = playArea;
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
	public int getRadius() {
		return radius;
	}
	public void setPosition(int x, int y) {
		double dist = GameEngine.distance(x, y, center.x, center.y);
		if(dist < 50) {
			float f = 1 - (float)dist/50f;
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
		if(xMin >= 0 && xMax <= playArea.width) {
			center.x = xi;
		}
		if(yMin >= 0 && yMax <= playArea.height) {
			center.y = yi;
		}
		//direction.x = 0;
		//.y = 0;
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
	
	
	public boolean tryToTag() {
		if(isIt && !isTryingToTag && canTagAgain) {
			isTryingToTag = true;
			//speed = 2;
			System.out.println("Is isTryingToTag");
			
			exec.schedule(()->{
		              isTryingToTag = false;
		              canTagAgain = false;
		              exec.schedule(()->{canTagAgain = true;}, 2000, TimeUnit.MILLISECONDS);
		              //speed = baseSpeed;
		              System.out.println("Is not isTryingToTag");
		     }, 250, TimeUnit.MILLISECONDS);
		}
		return isTryingToTag;
	}
	public void applyBoost() {
		if(!isBoosted) {
			isBoosted = true;
			speed = 2;
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
	public void setDirectionX(int x) {
		if(isValidDirection(x, direction.x)) {
			direction.x = x;
		}
	}
	public Point getDirection() {
		return direction;
	}
	public void setDirectionY(int y) {
		if(isValidDirection(y, direction.y)) {
			direction.y = y;
		}
	}
	public void setDirection(int x, int y) {
		direction.x = x;
		direction.y = y;
	}
	public void changeDirection(boolean cx, boolean cy) {
		if(cx) {
			direction.x *= -1;
		}
		if(cy) {
			direction.y *= -1;
		}
	}
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
            g2d.drawString(name, (int) (center.x - (nameSize.width * .51)),
                (int) (center.y + (nameSize.height * .49)));
            
        }
    }
}

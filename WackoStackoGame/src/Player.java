import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;

class Player{
	public Point position = new Point(300,300);
	public Point speed = new Point(0,0);
	public Point direction = new Point(0,0);
	public Dimension size = new Dimension(50,50);
	public Color myColor;
	public Player() {
		Random random = new Random();
		myColor = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
	}
	public void draw(Graphics2D g2d) {
		g2d.setColor(myColor);
		g2d.fillRect(
				position.x - (size.width/2),
				position.y - (size.height/2),
				size.width,
				size.height);
	}
	public void move(Rectangle bounds) {
		position.x += (direction.x * speed.x);
		position.y += (direction.y * speed.y);
		int halfWidth = size.width /2;
		int halfHeight = size.height/2;
		if((position.x - halfWidth <= bounds.getMinX()) 
				|| (position.x + halfWidth >= bounds.getMaxX())) {
			direction.x *= -1;
		}
		//hit top/bottom reflect direction y
		if((position.y - halfHeight <= bounds.getMinY())
				|| (position.y+halfHeight >= bounds.getMaxY())) {
			direction.y *= -1;
		}
	}
}
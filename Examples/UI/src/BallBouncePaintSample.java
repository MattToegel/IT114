
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class BallBouncePaintSample extends JPanel{
	public static boolean isRunning = true;
	Point ball = new Point(200,200);
	Point dir = new Point(0,0);
	int dx = 2;
	int dy = 2;
	int radius = 15;
	public static void main (String[] args) {
		JFrame frame = new JFrame("Ball Bounce");
		JPanel panel = new BallBouncePaintSample();
		panel.setBorder(BorderFactory.createLineBorder(Color.black));
		panel.setVisible(true);
		panel.setPreferredSize(new Dimension(400,400));
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		((BallBouncePaintSample)panel).run();
		
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		//top left corner default
		//g2d.drawOval(ball.x, ball.y, 15, 15);
		//fix offset to center
		g2d.drawOval(ball.x - radius, ball.y - radius, radius * 2, radius* 2);
	}
	public void run() {
		Random random = new Random();
		//give a random direction
		//based on 0 or 1
		dir.x = random.nextInt(2) == 0?-1:1;
		dir.y = random.nextInt(2) == 0?-1:1;
		
		//give a random speed between 1 and 3
		dx = random.nextInt(3)+1;
		dy = random.nextInt(3)+1;
		while(isRunning) {
			//apply direction and speed
			ball.x += (dir.x * dx);
			ball.y += (dir.y * dy);
			//hit side, reflect direction x
			if((ball.x - radius) <= 0 || (ball.x+(radius)) >= 400) {
				dir.x *= -1;
			}
			//hit top/bottom reflect direction y
			if((ball.y - radius) <= 0 || (ball.y+(radius)) >= 400) {
				dir.y *= -1;
			}
			repaint();
			try {
				Thread.sleep(16);//16 ms is ~60 fps
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

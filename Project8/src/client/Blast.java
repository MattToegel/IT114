package client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import core.GameObject;

public class Blast extends GameObject {
	private Color color = Color.darkGray;
	private boolean isAnimating = false;
	private Dimension originalSize = new Dimension();
	
	public Blast(int x, int y, int radius) {
		set(x, y, radius);
	}
	public void set(int x, int y, int radius) {
		///int start = (int) (radius - (5*(radius *.2)));
		int start = 0;
		for(int i = 0; i < 5; i++) {
			radius *= .8;
		}
		start = radius;
		this.size.width = start;
		this.size.height = start;
		originalSize.width = start;
		originalSize.height = start;
		isActive = true;
		click(x, y);
	}

	ExecutorService executor = Executors.newFixedThreadPool(1);

	public void click(int x, int y) {
		this.isActive = true;
		this.position.x = x;
		this.position.y = y;
		if (isAnimating) {
			return;
		}
		executor.execute(() -> {
			try {
				this.size.width = originalSize.width;
				this.size.height = originalSize.height;
				Thread.sleep(100);
				for (int i = 0; i < 5; i++) {
					Thread.sleep(100);
					this.size.width += this.size.width * .2;
					this.size.height +=  this.size.height * .2;
				}
				Thread.sleep(100);
				isAnimating = false;
				this.isActive = false;
			} catch (Exception e) {

			}
		});
	}

	@Override
	public boolean draw(Graphics g) {
		// using a boolean here so we can block drawing if isActive is false via call to
		// super
		if (super.draw(g)) {
			g.setColor(color);
			int hw = (int) (size.width * .5);
			int hh = (int) (size.height * .5);
			g.fillOval(position.x - hw, position.y - hh, size.width, size.height);

			// g.setColor(Color.WHITE);
			// g.setFont(new Font("Monospaced", Font.PLAIN, 12));
			// g.drawString("Name: " + name, position.x + nameOffset.x, position.y +
			// nameOffset.y);
		}
		return true;
	}
}
package client;

import java.awt.Color;
import java.awt.Graphics;

import core.GameObject;

public class Marker extends GameObject {
	Color color = Color.RED;

	public Marker(MarkerType t) {
		if (t == MarkerType.HIT) {
			color = Color.GREEN;
		} else {
			color = Color.RED;
		}
	}

	public boolean draw(Graphics g) {
		// using a boolean here so we can block drawing if isActive is false via call to
		// super
		if (super.draw(g)) {
			g.setColor(color);

			int hw = (int) (size.width * .5);
			int hh = (int) (size.height * .5);
			g.fillOval(position.x - hw, position.y - hh, size.width, size.height);

		}
		return true;
	}
}
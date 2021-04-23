package common;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import core.GameObject;
import server.ClientPlayer;

public class Ship extends GameObject {
	Color color = Color.BLUE;
	private int id;
	private int health = 3;
	private int maxHealth = 3;
	private ClientPlayer owner;//<-- only used server side
	
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	
	public void setOwner(ClientPlayer cp) {
		owner = cp;
	}
	public ClientPlayer getOwner() {
		return owner;
	}
	public int getHealth() {
		return health;
	}
	public int getMaxHealth() {
		return maxHealth;
	}
	
	public void setHealth(int life) {
		this.health = life;
		if(this.health <= 0) {
			this.isActive = false;
		}
	}

	public boolean hit() {
		return hit(1);
	}

	public boolean hit(int damage) {
		health -= damage;
		if (health <= 0) {
			isActive = false;
		}
		return health <= 0;
	}

	public boolean draw(Graphics g) {
		// using a boolean here so we can block drawing if isActive is false via call to
		// super
		if (super.draw(g)) {
			g.setColor(color);

			int hw = (int) (size.width * .5);
			int hh = (int) (size.height * .5);
			g.fillOval(position.x - hw, position.y - hh, size.width, size.height);
			String display = name + " (" + health + "/" + maxHealth + ")";
			g.setColor(Color.WHITE);
			g.setFont(new Font("Monospaced", Font.CENTER_BASELINE, 12));
			FontMetrics fm = g.getFontMetrics();
			int x = ((size.width - fm.stringWidth(display)) / 2);
			int y = ((size.height - fm.getHeight()) / 2) + fm.getAscent();
			g.drawString(display, position.x + x - hw, position.y + y - (int) (size.height * 1.5));

		}
		return true;
	}
}
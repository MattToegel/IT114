package mt.ws.dataobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;

import ws.dyn4j.framework.Graphics2DRenderer;

public class GameObject extends Body{
	private static double SCALE;
	private Color color = Color.DARK_GRAY;
	public static void setScale(double scale) {
		SCALE = scale;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public void render(Graphics2D g) {
		// save the original transform
		AffineTransform ot = g.getTransform();
		
		// transform the coordinate system from world coordinates to local coordinates
		AffineTransform lt = new AffineTransform();
		lt.translate(this.transform.getTranslationX() * SCALE, this.transform.getTranslationY() * SCALE);
		lt.rotate(this.transform.getRotationAngle());
		
		// apply the transform
		g.transform(lt);
		
		// loop over all the body fixtures for this body
		for (BodyFixture fixture : this.fixtures) {
			// get the shape on the fixture
			Convex convex = fixture.getShape();
			Graphics2DRenderer.render(g, convex, SCALE, color);
		}
		
		// set the original transform
		g.setTransform(ot);
	}
}

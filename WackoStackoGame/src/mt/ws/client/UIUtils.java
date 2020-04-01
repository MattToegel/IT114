package mt.ws.client;
import java.awt.Color;
import java.awt.Graphics2D;

class UIUtils{
	double FPSOldTime = 0;
	Color myColor = Color.BLACK;
	double getFPS(double oldTime) {
	    double newTime = System.nanoTime();
	    double delta = newTime - oldTime;

	    //double FPS = 1d / (delta * 1000);
	    double FPS = 1000000000.0 / delta;
	    FPSOldTime = newTime;

	    return FPS;
	}
	public void showFPS(Graphics2D g2d) {
		g2d.setColor(myColor);
		boolean doesUserWantFPS = true;
		if(doesUserWantFPS) {
			String str = String.format("FPS Counter: %.2f",getFPS(FPSOldTime));
			g2d.drawString(str, 50f, 50f);
		}
	}
}
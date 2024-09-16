package UISamples;

import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/*
 * Standalone example, unrelated to my project
 */
public class DrawSample extends JPanel {
    private int circleX = 0;
    private int circleY = 0;
    private int dirX = 1;
    private int dirY = 1;

    public void setCircleX(int x) {
        this.circleX = x;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw a circle at the current x-coordinate
        // circleY = getHeight() / 2;
        g.fillOval(circleX, circleY, 50, 50);
    }

    public DrawSample() {

        // Create a new Thread
        Thread drawingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Let's say we want to move a circle across the panel
                circleX = 0;
                while (true) {
                    // Increase the x-coordinate for the next frame
                    circleX += 5 * dirX;
                    circleY += 5 * dirY;

                    // If the circle has moved off the edge of the panel, reset the position

                    if (circleX + 50 > getWidth() || circleX < 0) {
                        dirX *= -1;
                    }
                    if (circleY + 50 > getHeight() || circleY < 0) {
                        dirY *= -1;
                    }

                    /*
                     * if (circleX > getWidth()) {
                     * circleX = 0;
                     * }
                     */

                    // Use SwingUtilities.invokeLater to ensure thread safety when updating Swing
                    // components
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // Update the position of the circle
                            // Here, you would update the state of your object, e.g., the position of the
                            // circle
                            // For example, you might have a method in your panel like setCircleX(x);
                            // Repaint the panel
                            // This will cause the panel's paintComponent method to be called again the next
                            // time the GUI is updated
                            repaint();
                        }
                    });

                    // Wait for a while before the next frame
                    try {
                        Thread.sleep(8);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Start the thread
        drawingThread.start();
        setSize(400, 400);
        setVisible(true);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Draw Sample");
        DrawSample ds = new DrawSample();
        frame.add(ds);
        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}

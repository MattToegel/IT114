package UISamples;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class DrawingPanel extends JPanel {
    private int numCharacters = 0;

    public static void main(String[] args) {
        JFrame test = new JFrame();
        test.setVisible(true);
        DrawingPanel dp = new DrawingPanel();
        test.add(dp);
        test.setSize(400, 400);
        dp.setSize(400, 400);
        test.pack();
        test.repaint();

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cx = (int) (getWidth() * .5f);
        int cy = (int) (getHeight() * .5f);
        int numCharacters = 3;
        if (numCharacters > 0) {
            for (int i = 0; i < numCharacters; i++) {
                int x = 50 + (i * 50);
                drawStickFigure(g, x, cy);
            }
        }
        // drawWall(g, cx, cy, 100, 100);
        drawArchedDoor(g, cx, cy, 100, 100, Color.GREEN);
        /*
         * int cx = (int) (getWidth() * .5f);
         * int cy = (int) (getHeight() * .5f);
         * if (numCharacters > 0) {
         * for (int i = 0; i < numCharacters; i++) {
         * int x = 50 + (i * 50);
         * drawStickFigure(g, x, cy);
         * }
         * }
         * if (ct == CellType.START_DOOR || ct == CellType.END_DOOR) {
         * drawArchedDoor(g, cx, cy, 100, 100, ct == CellType.START_DOOR ? Color.GREEN :
         * Color.BLUE);
         * } else if (ct == CellType.WALL) {
         * drawWall(g, cx, cy, 100, 100);
         * }
         */
    }

    private void drawStickFigure(Graphics g, int x, int y) {
        // Set the color for the stick figure
        g.setColor(Color.BLACK);

        // In Java's coordinate system, the origin (0,0) is at the top left corner of
        // the component (like a JPanel or JFrame).
        // The x-coordinates increase as you move to the right.
        // The y-coordinates increase as you move down.

        // Draw the head of the stick figure
        // fillOval(int x, int y, int width, int height) draws a filled oval bounded by
        // the specified rectangle
        // x and y here are the coordinates of the top-left corner of the oval (head)
        g.fillOval(x - 15, y - 15, 30, 30); // Head

        // Draw the body of the stick figure
        // drawLine(int x1, int y1, int x2, int y2) draws a line, using the current
        // color, between the points (x1, y1) and (x2, y2) in this graphics context's
        // coordinate system
        // The body starts from the bottom of the head (y + 15) and extends downwards (y
        // + 55)
        g.drawLine(x, y + 15, x, y + 55); // Body

        // Draw the arms of the stick figure
        // The arms start a bit below the head (y + 25) and extend to the left and right
        // of the body
        g.drawLine(x, y + 25, x - 20, y + 35); // Left arm
        g.drawLine(x, y + 25, x + 20, y + 35); // Right arm

        // Draw the legs of the stick figure
        // The legs start at the bottom of the body (y + 55) and extend downwards
        g.drawLine(x, y + 55, x - 20, y + 85); // Left leg
        g.drawLine(x, y + 55, x + 20, y + 85); // Right leg
    }

    private void drawArchedDoor(Graphics g, int x, int y, int width, int height, Color doorColor) {
        // Calculate the top left corner of the door
        // Since x and y represent the center of the door, we subtract half the width
        // and height.
        int topLeftX = x - (int) (width * 0.5);
        int topLeftY = y - (int) (height * 0.5);

        // Set the color for the door frame
        g.setColor(Color.GRAY);

        // Draw the door frame
        // fillRect(int x, int y, int width, int height) draws a filled rectangle with
        // the current color
        // topLeftX and topLeftY here are the coordinates of the top-left corner of the
        // rectangle (door frame)
        g.fillRect(topLeftX, topLeftY, width, height);

        // Calculate the control points for the arch
        // These points are used to control the shape of the arch
        // They are located at 1/4 and 3/4 of the width of the door, and at half the
        // height of the door
        int controlPointX1 = topLeftX + (int) (width * 0.25);
        int controlPointY1 = topLeftY + (int) (height * 0.5);
        int controlPointX2 = topLeftX + (int) (width * 0.75);
        int controlPointY2 = topLeftY + (int) (height * 0.5);

        // Set the color for the arch
        g.setColor(doorColor);

        // Draw the arch
        // fillArc(int x, int y, int width, int height, int startAngle, int arcAngle)
        // draws a filled arc with the current color
        // The arc is part of the oval that is defined by the rectangle (topLeftX,
        // topLeftY, width, height)
        // The arc is drawn from startAngle to startAngle + arcAngle. The angles are
        // expressed in degrees
        g.fillArc(topLeftX, topLeftY, width, height, 0, 180);

        // Draw the control points (optional, for visualization)
        // These points help to visualize where the control points for the arch are
        // located
        g.setColor(Color.RED);
        g.fillOval(controlPointX1 - 2, controlPointY1 - 2, 4, 4);
        g.fillOval(controlPointX2 - 2, controlPointY2 - 2, 4, 4);
    }

    private void drawWall(Graphics g, int x, int y, int width, int height) {
        // Set the color for the wall
        g.setColor(new Color(139, 69, 19)); // Brown color

        // Calculate the size of each brick
        // We want 10 bricks across the width and 20 bricks down the height,
        // so each brick is 10% of the total width and 5% of the total height.
        int brickWidth = (int) (width * 0.1); // 10% of the total width
        int brickHeight = (int) (height * 0.05); // 5% of the total height

        // Calculate the top left corner of the wall
        // Since x and y represent the center of the wall, we subtract half the width
        // and height.
        int topLeftX = x - (int) (width * 0.5); // 50% of the total width
        int topLeftY = y - (int) (height * 0.5); // 50% of the total height

        // Draw the bricks
        // We have 20 rows of bricks and 10 bricks per row.
        for (int i = 0; i < 20; i++) { // 20 rows of bricks
            for (int j = 0; j < 10; j++) { // 10 bricks per row
                // Calculate the position of the brick
                // Each brick is offset from the top left corner by its width and height times
                // its row and column number.
                int brickX = topLeftX + j * brickWidth;
                int brickY = topLeftY + i * brickHeight;

                // Draw the brick
                // We use fillRect to draw a solid rectangle for the brick.
                g.fillRect(brickX, brickY, brickWidth, brickHeight);

                // Draw a black line around the brick to represent the mortar
                // We use setColor to change the color to black, and drawRect to draw the
                // outline of the brick.
                g.setColor(Color.BLACK);
                g.drawRect(brickX, brickY, brickWidth, brickHeight);

                // Set the color back to brown for the next brick
                // We need to change the color back to brown before drawing the next brick.
                g.setColor(new Color(139, 69, 19)); // Brown color
            }
        }
    }
}
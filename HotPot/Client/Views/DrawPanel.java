package HotPot.Client.Views;

import javax.swing.*;
import java.awt.*;

// Note: This file was generated via GPT for quick approximate poses
public class DrawPanel extends JPanel {
    private int posePercentage = 0; // 0-100 range to determine the pose

    public DrawPanel() {
        this.setPreferredSize(new Dimension(200, 300)); // Set the default size of the panel
        this.setBackground(Color.WHITE); // Set the background color
    }

    /**
     * Set the pose percentage (0-100) to update the stick figure's pose.
     *
     * @param percentage The pose percentage (0-100).
     */
    public void setPosePercentage(int percentage) {
        this.posePercentage = Math.min(100, Math.max(0, percentage)); // Clamp the value between 0 and 100
        repaint(); // Trigger a repaint to update the drawing
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Enable anti-aliasing for smoother lines
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Center the stick figure
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Draw the head
        int headRadius = 20;
        g2.setColor(Color.BLACK);
        g2.drawOval(centerX - headRadius, centerY - 120, headRadius * 2, headRadius * 2);

        // Determine pose based on percentage
        if (posePercentage <= 20) {
            poseOne(g2, centerX, centerY);
        } else if (posePercentage <= 40) {
            poseTwo(g2, centerX, centerY);
        } else if (posePercentage <= 60) {
            poseThree(g2, centerX, centerY);
        } else if (posePercentage <= 80) {
            poseFour(g2, centerX, centerY);
        } else {
            poseFive(g2, centerX, centerY);
        }
    }

    /**
     * Pose One: Normal standing pose.
     */
    private void poseOne(Graphics2D g2, int centerX, int centerY) {
        // Body
        g2.drawLine(centerX, centerY - 80, centerX, centerY);

        // Arms hanging down
        g2.drawLine(centerX, centerY - 60, centerX - 30, centerY - 40);
        g2.drawLine(centerX, centerY - 60, centerX + 30, centerY - 40);

        // Legs standing straight
        g2.drawLine(centerX, centerY, centerX - 20, centerY + 80);
        g2.drawLine(centerX, centerY, centerX + 20, centerY + 80);
    }

    /**
     * Pose Two: Curious pose with arms slightly raised.
     */
    private void poseTwo(Graphics2D g2, int centerX, int centerY) {
        // Body
        g2.drawLine(centerX, centerY - 80, centerX, centerY);

        // Arms slightly raised with elbows bent
        g2.drawLine(centerX, centerY - 60, centerX - 20, centerY - 80); // Upper left arm
        g2.drawLine(centerX - 20, centerY - 80, centerX - 40, centerY - 60); // Left forearm
        g2.drawLine(centerX, centerY - 60, centerX + 20, centerY - 80); // Upper right arm
        g2.drawLine(centerX + 20, centerY - 80, centerX + 40, centerY - 60); // Right forearm

        // Legs slightly apart
        g2.drawLine(centerX, centerY, centerX - 20, centerY + 80);
        g2.drawLine(centerX, centerY, centerX + 20, centerY + 80);
    }

    /**
     * Pose Three: Hands near shoulders, elbows pointing downward.
     */
    private void poseThree(Graphics2D g2, int centerX, int centerY) {
        // Body
        g2.drawLine(centerX, centerY - 80, centerX, centerY);

        // Arms closer to the shoulders with elbows pointing downward
        g2.drawLine(centerX, centerY - 60, centerX - 20, centerY - 50); // Upper left arm
        g2.drawLine(centerX - 20, centerY - 50, centerX - 30, centerY - 70); // Left forearm
        g2.drawLine(centerX, centerY - 60, centerX + 20, centerY - 50); // Upper right arm
        g2.drawLine(centerX + 20, centerY - 50, centerX + 30, centerY - 70); // Right forearm

        // Legs slightly closer
        g2.drawLine(centerX, centerY, centerX - 15, centerY + 80);
        g2.drawLine(centerX, centerY, centerX + 15, centerY + 80);
    }

    /**
     * Pose Four: Hands higher, elbows still bent, transitioning to hands-on-head.
     */
    private void poseFour(Graphics2D g2, int centerX, int centerY) {
        // Body
        g2.drawLine(centerX, centerY - 80, centerX, centerY);

        // Arms raised higher, bent at elbows, transitioning to head
        g2.drawLine(centerX, centerY - 60, centerX - 20, centerY - 90); // Upper left arm
        g2.drawLine(centerX - 20, centerY - 90, centerX - 10, centerY - 110); // Left forearm
        g2.drawLine(centerX, centerY - 60, centerX + 20, centerY - 90); // Upper right arm
        g2.drawLine(centerX + 20, centerY - 90, centerX + 10, centerY - 110); // Right forearm

        // Legs slightly apart, leaning outward
        g2.drawLine(centerX, centerY, centerX - 20, centerY + 80);
        g2.drawLine(centerX, centerY, centerX + 20, centerY + 80);
    }

    /**
     * Pose Five: Final pose, hands on head in worry.
     */
    private void poseFive(Graphics2D g2, int centerX, int centerY) {
        // Body
        g2.drawLine(centerX, centerY - 80, centerX, centerY);

        // Arms bent with hands on the head
        g2.drawLine(centerX, centerY - 60, centerX - 20, centerY - 100); // Upper left arm
        g2.drawLine(centerX - 20, centerY - 100, centerX - 10, centerY - 120); // Left forearm
        g2.drawLine(centerX, centerY - 60, centerX + 20, centerY - 100); // Upper right arm
        g2.drawLine(centerX + 20, centerY - 100, centerX + 10, centerY - 120); // Right forearm

        // Legs in a worried stance (closer together)
        g2.drawLine(centerX, centerY, centerX - 10, centerY + 80);
        g2.drawLine(centerX, centerY, centerX + 10, centerY + 80);
    }

    /**
     * Standalone Test Method
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Stick Figure Pose Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DrawPanel drawPanel = new DrawPanel();
        frame.add(drawPanel, BorderLayout.CENTER);

        JSlider poseSlider = new JSlider(0, 100, 0);
        poseSlider.setMajorTickSpacing(20);
        poseSlider.setMinorTickSpacing(5);
        poseSlider.setPaintTicks(true);
        poseSlider.setPaintLabels(true);

        poseSlider.addChangeListener(e -> drawPanel.setPosePercentage(poseSlider.getValue()));

        frame.add(poseSlider, BorderLayout.SOUTH);
        frame.setSize(400, 500);
        frame.setVisible(true);
    }
}

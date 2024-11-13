package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * UserListItem represents a user entry in the user list.
 */
public class UserListItem extends JPanel {
    private JEditorPane textContainer;
    private JPanel turnIndicator = new JPanel();
    private JEditorPane pointsPanel = new JEditorPane("text/plain","");

    /**
     * Constructor to create a UserListItem.
     *
     * @param clientId   The ID of the client.
     * @param clientName The name of the client.
     * @param parent     The parent container to calculate available width.
     */
    public UserListItem(long clientId, String clientName, JPanel parent) {
        textContainer = new JEditorPane("text/plain", clientName);
        textContainer.setName(Long.toString(clientId));
        textContainer.setEditable(false);
        textContainer.setBorder(new EmptyBorder(0, 0, 0, 0)); // Add padding

        // Clear background and border
        textContainer.setOpaque(false);
        textContainer.setBorder(BorderFactory.createEmptyBorder());
        textContainer.setBackground(new Color(0, 0, 0, 0));

        this.setLayout(new BorderLayout());
        // locking the turnIndicator size
        turnIndicator.setPreferredSize(new Dimension(10, 10));
        turnIndicator.setMinimumSize(turnIndicator.getPreferredSize());
        turnIndicator.setMaximumSize(turnIndicator.getPreferredSize());
        this.add(turnIndicator, BorderLayout.WEST);
        JPanel mid = new JPanel(new BorderLayout());
        mid.add(textContainer, BorderLayout.NORTH);
        pointsPanel.setEditable(false);
        pointsPanel.setBorder(new EmptyBorder(0, 0, 0, 0)); // Add padding
        pointsPanel.setOpaque(false);
        pointsPanel.setBorder(BorderFactory.createEmptyBorder());
        pointsPanel.setBackground(new Color(0, 0, 0, 0));
        mid.add(pointsPanel, BorderLayout.SOUTH);
        this.add(mid, BorderLayout.CENTER);
        setPoints(-1);
        // setPreferredSize(new Dimension(0,0));
    }

    public String getClientName() {
        return textContainer.getText();
    }
    /**
     * Mostly used to trigger a reset, but if used for a true value, it'll apply Color.GREEN
     * @param didTakeTurn
     */
    public void setTurn(boolean didTakeTurn){
        setTurn(didTakeTurn, Color.GREEN);
    }
    
    /**
     * Sets the indicator and color based on turn status
     * @param didTaketurn if true, applies trueColor; otherwise applies transparent
     * @param trueColor Color to apply when true
     */
    public void setTurn(boolean didTaketurn, Color trueColor) {
        turnIndicator.setBackground(didTaketurn ? trueColor : new Color(0, 0, 0, 0));
        repaint();
    }

    public void setPoints(int points) {
        if (points < 0) {
            pointsPanel.setText("0");
            pointsPanel.setVisible(false);
        } else {
            pointsPanel.setText(points + "");
            if(!pointsPanel.isVisible()){
                pointsPanel.setVisible(true);
                invalidate();
            }
            
        }
        repaint();
    }
}
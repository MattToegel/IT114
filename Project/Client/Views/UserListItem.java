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
    private JEditorPane energyPanel = new JEditorPane();

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
        turnIndicator.setPreferredSize(new Dimension(20, 20));
        this.add(turnIndicator, BorderLayout.WEST);
        JPanel mid = new JPanel(new BorderLayout());
        mid.add(textContainer, BorderLayout.NORTH);
        mid.add(energyPanel, BorderLayout.SOUTH);
        this.add(mid, BorderLayout.CENTER);
        setEnergy(-1);
        // setPreferredSize(new Dimension(0,0));
    }

    public String getClientName() {
        return textContainer.getText();
    }

    public void setCurrentTurn(boolean isMyTurn) {
        turnIndicator.setBackground(isMyTurn ? Color.GREEN : new Color(0, 0, 0, 0));
        repaint();
    }

    public void setEnergy(int energy) {
        if (energy < 0) {
            energyPanel.setText("0");
            energyPanel.setVisible(false);
        } else {
            energyPanel.setText(energy + "");
            energyPanel.setVisible(true);
        }
        repaint();
    }
}

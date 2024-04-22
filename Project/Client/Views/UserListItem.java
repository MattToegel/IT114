package Project.Client.Views;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import Project.Client.ClientUtils;

public class UserListItem extends JPanel {
    private JEditorPane usernameContainer;
    private JEditorPane pointsField;
    private int points = -1;
    private JButton turnIndicator;

    public UserListItem(long clientId, String clientName) {
        // Set a layout that will organize the children vertically
        this.setLayout(new GridLayout(1, 3));
        this.setName(Long.toString(clientId));
        JButton turnIndicator = new JButton();
        this.turnIndicator = turnIndicator;
        this.turnIndicator.setVisible(false);
        this.add(turnIndicator);
        JEditorPane textContainer = new JEditorPane("text/plain", clientName);
        /*
         * String display = String.format("%s [%s]", clientName, points);
         * textContainer.setText(display);
         */
        this.usernameContainer = textContainer;
        textContainer.setEditable(false);
        textContainer.setName(Long.toString(clientId));

        ClientUtils.clearBackground(textContainer);
        this.add(textContainer);

        pointsField = new JEditorPane();
        pointsField.setText(Integer.toString(points));
        pointsField.setEditable(false);
        pointsField.setVisible(false);

        ClientUtils.clearBackground(pointsField);
        this.add(pointsField);
    }

    public void setPoints(int points) {
        pointsField.setText(Integer.toString(points));
        pointsField.setVisible(points >= 0);
        this.revalidate();
        this.repaint();
    }

    // Can be used for sorting purposes later
    public int getPoints() {
        return points;
    }

    public void setCurrentTurn(long clientId) {
        if (getName().equals(Long.toString(clientId))) {
            turnIndicator.setVisible(true);
            turnIndicator.setBackground(Color.GREEN);
        } else {
            turnIndicator.setVisible(false);
        }
    }
}

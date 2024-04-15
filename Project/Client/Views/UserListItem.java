package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.logging.Level;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import Project.Client.ClientUtils;

public class UserListItem extends JPanel {
    private JEditorPane usernameContainer;
    private JTextField pointsField;

    public UserListItem(JPanel content, long clientId, String clientName) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JEditorPane textContainer = new JEditorPane("text/plain", clientName);
        this.usernameContainer = textContainer;
        // textContainer.setName(clientId + "");
        this.setName(clientId + "");// <-- don't forget JPanel is the parent now
        // sizes the panel to attempt to take up the width of the container
        // and expand in height based on word wrapping
        // textContainer.setLayout(null);
        /*
         * textContainer.setPreferredSize(
         * new Dimension(content.getWidth(),
         * ClientUtils.calcHeightForText(content, clientName, content.getWidth())));
         */
        /*
         * this.setPreferredSize(new Dimension(content.getWidth(),
         * ClientUtils.calcHeightForText(content, clientName, content.getWidth())));
         */
        // textContainer.setMaximumSize(textContainer.getPreferredSize());
        textContainer.setEditable(false);
        // remove background and border (comment these out to see what it looks like
        // otherwise)
        ClientUtils.clearBackground(textContainer);
        this.add(textContainer);
        pointsField = new JTextField("0");
        pointsField.setEditable(false);
        ClientUtils.clearBackground(pointsField);
        this.add(pointsField);
    }

    public void setPoints(int points) {
        pointsField.setText(points + "");
        this.revalidate();
        this.repaint();
    }
}

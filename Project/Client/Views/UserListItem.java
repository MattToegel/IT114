package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * UserListItem represents a user entry in the user list.
 */
public class UserListItem extends JPanel {
    private JEditorPane textContainer;

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

        // Account for the width of the vertical scrollbar
        JScrollPane parentScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, parent);
        int scrollBarWidth = parentScrollPane.getVerticalScrollBar().getPreferredSize().width;

        // Adjust the width of the text container
        int availableWidth = parent.getWidth() - scrollBarWidth - 10; // Subtract an additional padding
        textContainer.setSize(new Dimension(availableWidth, Integer.MAX_VALUE));
        Dimension d = textContainer.getPreferredSize();
        textContainer.setPreferredSize(new Dimension(availableWidth, d.height));

        // Clear background and border
        textContainer.setOpaque(false);
        textContainer.setBorder(BorderFactory.createEmptyBorder());
        textContainer.setBackground(new Color(0, 0, 0, 0));

        this.setLayout(new BorderLayout());
        this.add(textContainer, BorderLayout.CENTER);
    }

    public String getClientName() {
        return textContainer.getText();
    }
}

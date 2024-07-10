package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import Project.Common.LoggerUtil;

/**
 * UserListPanel represents a UI component that displays a list of users.
 */
public class UserListPanel extends JPanel {
    private JPanel userListArea;
    private GridBagConstraints lastConstraints; // Keep track of the last constraints for the glue
    private HashMap<Long, UserListItem> userItemsMap; // Maintain a map of client IDs to UserListItems

    /**
     * Constructor to create the UserListPanel UI.
     */
    public UserListPanel() {
        super(new BorderLayout(10, 10));
        userItemsMap = new HashMap<>(); // Initialize the map

        JPanel content = new JPanel(new GridBagLayout());
        userListArea = content;

        // Wraps a viewport to provide scroll capabilities
        JScrollPane scroll = new JScrollPane(userListArea);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0)); // Remove border

        this.add(scroll, BorderLayout.CENTER);

        userListArea.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                if (userListArea.isVisible()) {
                    SwingUtilities.invokeLater(() -> {
                        userListArea.revalidate();
                        userListArea.repaint();
                    });
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (userListArea.isVisible()) {
                    SwingUtilities.invokeLater(() -> {
                        userListArea.revalidate();
                        userListArea.repaint();
                    });
                }
            }
        });

        // Add vertical glue to push items to the top
        lastConstraints = new GridBagConstraints();
        lastConstraints.gridx = 0;
        lastConstraints.gridy = GridBagConstraints.RELATIVE;
        lastConstraints.weighty = 1.0;
        lastConstraints.fill = GridBagConstraints.VERTICAL;
        userListArea.add(Box.createVerticalGlue(), lastConstraints);

        // Listen for resize events to adjust user list items accordingly
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> adjustUserListItemsWidth());
            }
        });
    }

    /**
     * Adds a user to the list.
     *
     * @param clientId   The ID of the client.
     * @param clientName The name of the client.
     */
    protected void addUserListItem(long clientId, String clientName) {
        SwingUtilities.invokeLater(() -> {
            if (userItemsMap.containsKey(clientId)) {
                LoggerUtil.INSTANCE.warning("User already in the list: " + clientName);
                return; // User already in the list
            }

            LoggerUtil.INSTANCE.info("Adding user to list: " + clientName);

            UserListItem userItem = new UserListItem(clientId, clientName, userListArea);

            // GridBagConstraints settings for each user
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; // Column index 0
            gbc.gridy = userListArea.getComponentCount() - 1; // Place before the glue
            gbc.weightx = 1; // Let the component grow horizontally to fill the space
            gbc.anchor = GridBagConstraints.NORTH; // Anchor to the top
            gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally
            gbc.insets = new Insets(0, 0, 5, 0); // Add spacing between users

            // Remove the last glue component if it exists
            if (lastConstraints != null) {
                int index = userListArea.getComponentCount() - 1;
                if (index > -1) {
                    userListArea.remove(index);
                }
            }
            // Add user item
            userListArea.add(userItem, gbc);

            // Add vertical glue to push items to the top
            userListArea.add(Box.createVerticalGlue(), lastConstraints);

            userItemsMap.put(clientId, userItem); // Add to the map

            userListArea.revalidate();
            userListArea.repaint();
        });
    }

    /**
     * Adjusts the width of all user list items.
     */
    private void adjustUserListItemsWidth() {
        SwingUtilities.invokeLater(() -> {
            for (UserListItem item : userItemsMap.values()) {
                item.setPreferredSize(
                        new Dimension(userListArea.getWidth() - 20, item.getPreferredSize().height));
            }
            userListArea.revalidate();
            userListArea.repaint();
        });
    }

    /**
     * Removes a user from the list.
     *
     * @param clientId The ID of the client to be removed.
     */
    protected void removeUserListItem(long clientId) {
        SwingUtilities.invokeLater(() -> {
            LoggerUtil.INSTANCE.info("Removing user list item for id " + clientId);
            UserListItem item = userItemsMap.remove(clientId); // Remove from the map
            if (item != null) {
                userListArea.remove(item);
                userListArea.revalidate();
                userListArea.repaint();
            }
        });
    }

    /**
     * Clears the user list.
     */
    protected void clearUserList() {
        SwingUtilities.invokeLater(() -> {
            LoggerUtil.INSTANCE.info("Clearing user list");
            userItemsMap.clear(); // Clear the map
            userListArea.removeAll();
            userListArea.revalidate();
            userListArea.repaint();
        });
    }
}

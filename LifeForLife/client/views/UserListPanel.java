package LifeForLife.client.views;

import java.awt.BorderLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import LifeForLife.client.ClientUtils;
import LifeForLife.client.ICardControls;
import LifeForLife.common.MyLogger;

public class UserListPanel extends JPanel {
    JPanel userListArea;
    JPanel wrapper;
    private static MyLogger logger = MyLogger.getLogger(UserListPanel.class.getName());

    public UserListPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        // wraps a viewport to provide scroll capabilities
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        // scroll.setBorder(BorderFactory.createEmptyBorder());
        // no need to add content specifically because scroll wraps it

        userListArea = content;
        this.wrapper = wrapper;
        wrapper.add(scroll);
        this.add(wrapper, BorderLayout.CENTER);

        userListArea.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {
                if (userListArea.isVisible()) {
                    logger.info("Added visible item");
                    userListArea.revalidate();
                    userListArea.repaint();
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (userListArea.isVisible()) {
                    userListArea.revalidate();
                    userListArea.repaint();
                }
            }

        });
    }

    protected void resizeUserListItems() {
        for (Component p : userListArea.getComponents()) {
            if (p.isVisible()) {
                //tooltip is storing the original unformated clientName
                p.setMinimumSize(
                        new Dimension(wrapper.getWidth(), ClientUtils.calcHeightForText(this,
                                ((JEditorPane) p).getToolTipText(), wrapper.getWidth())));
                p.setMaximumSize(p.getMinimumSize());
            }
        }
        userListArea.revalidate();
        userListArea.repaint();
    }
    /**
     * Adds user info to the user list panel
     * @param clientId - unique identifier
     * @param clientName - used to calculate proper sizing
     * @param formattedName - used to display the actual value
     */
    protected void addUserListItem(long clientId, String clientName, String formattedName) {
        logger.info("Adding user to list: " + clientName);
        JPanel content = userListArea;
        logger.info("Userlist: " + wrapper.getSize());
        JEditorPane textContainer = new JEditorPane("text/html", formattedName);
        textContainer.setName(clientId + "");
        textContainer.setToolTipText(clientName);//store original unformatted clientNAme
        // sizes the panel to attempt to take up the width of the container
        // and expand in height based on word wrapping
        textContainer.setAlignmentX(JEditorPane.LEFT_ALIGNMENT);
        //textContainer.setLayout(null);
        textContainer.setMinimumSize(
                new Dimension(wrapper.getWidth(), ClientUtils.calcHeightForText(this, clientName, wrapper.getWidth())));
        textContainer.setMaximumSize(textContainer.getMinimumSize());
        logger.info("User List Item: " + textContainer.getMinimumSize());
        textContainer.setEditable(false);
        // remove background and border (comment these out to see what it looks like
        // otherwise)
        ClientUtils.clearBackground(textContainer);
        // add to container
        content.add(textContainer);
    }

    protected void removeUserListItem(long clientId) {
        logger.info("removing user list item for id " + clientId);
        Component[] cs = userListArea.getComponents();
        for (Component c : cs) {
            if (c.getName().equals(clientId + "")) {
                userListArea.remove(c);
                break;
            }
        }
    }

    protected void clearUserList() {
        Component[] cs = userListArea.getComponents();
        for (Component c : cs) {
            userListArea.remove(c);
        }
    }
}

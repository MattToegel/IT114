package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import Project.Client.CardView;
import Project.Client.Client;
import Project.Client.Interfaces.ICardControls;
import Project.Common.LoggerUtil;

/**
 * RoomsPanel class represents the UI for managing chat rooms.
 */
public class RoomsPanel extends JPanel {
    private final JPanel container;
    private final List<RoomListItem> rooms = new ArrayList<>();
    private final JLabel message;

    /**
     * Constructor to create the RoomsPanel UI.
     * 
     * @param controls The card controls interface to handle navigation.
     */
    public RoomsPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.RED, 0),
                new EmptyBorder(10, 10, 0, 10))); // Add padding and colored border

        JScrollPane scroll = new JScrollPane(container, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JButton back = new JButton("Go Back");
        back.addActionListener(event -> controls.previous());

        JPanel search = new JPanel();
        search.setLayout(new BoxLayout(search, BoxLayout.Y_AXIS));
        search.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel searchContent = new JPanel();
        searchContent.setLayout(new BoxLayout(searchContent, BoxLayout.X_AXIS));
        JLabel searchLabel = new JLabel("Room Name");
        JTextField searchValue = new JTextField();
        JButton searchButton = new JButton("Search");
        message = new JLabel("", 0);
        JPanel messageContainer = new JPanel(); // wrapper to help fix alignment
        messageContainer.setBorder(new EmptyBorder(5, 0, 0, 0)); // Add padding

        // Search button action
        searchButton.addActionListener(event -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    String query = searchValue.getText().trim();
                    if (!query.isEmpty()) {
                        removeAllRooms();
                        Client.INSTANCE.sendListRooms(query);
                        message.setText("Sent query");
                    } else {
                        message.setText("Can't search with an empty query");
                    }
                } catch (IOException e) {
                    LoggerUtil.INSTANCE.warning("Error sending request: " + e.getMessage(), e);
                    message.setText("Error sending request: " + e.getMessage());
                }
            });
        });

        JButton createButton = new JButton("Create");
        createButton.addActionListener(event -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    String query = searchValue.getText().trim();
                    if (!query.isEmpty()) {
                        Client.INSTANCE.sendCreateRoom(query);
                        message.setText("Created room");
                    } else {
                        message.setText("Can't create a room without a name");
                    }
                } catch (IOException e) {
                    LoggerUtil.INSTANCE.warning("Error sending request: " + e.getMessage(), e);
                    message.setText("Error sending request: " + e.getMessage());
                }
            });
        });

        JButton joinButton = new JButton("Join");
        joinButton.addActionListener(event -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    String query = searchValue.getText().trim();
                    if (!query.isEmpty()) {
                        Client.INSTANCE.sendJoinRoom(query);
                        message.setText("Joined room");
                    } else {
                        message.setText("Can't join a room without a name");
                    }
                } catch (NullPointerException ne) {
                    message.setText("Not connected");
                } catch (IOException e) {
                    LoggerUtil.INSTANCE.warning("Not connected", e);
                    message.setText("Error sending request: " + e.getMessage());
                }
            });
        });

        searchContent.add(searchLabel);
        searchContent.add(searchValue);
        searchContent.add(searchButton);
        searchContent.add(createButton);
        searchContent.add(joinButton);
        search.add(searchContent);
        messageContainer.add(message);
        search.add(messageContainer);

        this.add(search, BorderLayout.NORTH);
        this.add(back, BorderLayout.SOUTH);
        this.add(scroll, BorderLayout.CENTER);

        container.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                if (container.isVisible()) {
                    revalidate();
                    repaint();
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (container.isVisible()) {
                    revalidate();
                    repaint();
                }
            }
        });

        this.setName(CardView.ROOMS.name());
        controls.addPanel(CardView.ROOMS.name(), this);
    }

    /**
     * Sets the message text displayed in the panel.
     * 
     * @param message The message text to set.
     */
    public void setMessage(String message) {
        this.message.setText(message);
    }

    /**
     * Adds a room to the rooms list.
     * 
     * @param room The name of the room to add.
     */
    public void addRoom(String room) {
        if (room != null) {
            LoggerUtil.INSTANCE.info("Adding: " + room);
            RoomListItem roomListItem = new RoomListItem(room, this::handleSelection);
            Dimension size = new Dimension(this.getSize().width, 40);
            roomListItem.setPreferredSize(size);
            roomListItem.setMaximumSize(size);
            roomListItem.setMinimumSize(size);
            container.add(roomListItem);
            rooms.add(roomListItem);
            revalidate();
            repaint();
        }
    }

    /**
     * Removes a room from the rooms list.
     * 
     * @param room The name of the room to remove.
     */
    public void removeRoom(String room) {
        rooms.removeIf(r -> {
            if (r.getRoomName().equalsIgnoreCase(room)) {
                r.removeAll();
                container.remove(r);
                revalidate();
                repaint();
                return true;
            }
            return false;
        });
    }

    /**
     * Removes all rooms from the rooms list.
     */
    public void removeAllRooms() {
        LoggerUtil.INSTANCE.info("Clearing rooms");
        for (RoomListItem roomListItem : rooms) {
            LoggerUtil.INSTANCE.info("Removing " + roomListItem.getRoomName());
            container.remove(roomListItem);
        }
        rooms.clear();
        revalidate();
        repaint();
    }

    /**
     * Handles the selection of a room by sending a join request.
     * 
     * @param room The name of the room to join.
     */
    public void handleSelection(String room) {
        SwingUtilities.invokeLater(() -> {
            try {
                Client.INSTANCE.sendJoinRoom(room);
            } catch (IOException e) {
                LoggerUtil.INSTANCE.severe("Error joining room: " + e.getMessage(), e);
            }
        });
    }
}

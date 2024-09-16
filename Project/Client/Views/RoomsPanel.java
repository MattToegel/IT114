package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import Project.Client.CardView;
import Project.Client.Client;
import Project.Client.ICardControls;

public class RoomsPanel extends JPanel {
    private JPanel container;
    private List<RoomListItem> rooms = new ArrayList<RoomListItem>();
    private JLabel message;
    private static Logger logger = Logger.getLogger(RoomsPanel.class.getName());

    public RoomsPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));
        container = new JPanel(
                new BoxLayout(this, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(container);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentY(TOP_ALIGNMENT);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JButton back = new JButton("Go Back");
        back.addActionListener((event) -> {
            controls.previous();
        });
        JPanel search = new JPanel();
        search.setLayout(new BoxLayout(search, BoxLayout.Y_AXIS));

        search.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel searchContent = new JPanel();
        searchContent.setLayout(new BoxLayout(searchContent, BoxLayout.X_AXIS));
        JLabel searchLabel = new JLabel("Room Name");
        JTextField searchValue = new JTextField();
        JButton searchButton = new JButton("Search");
        message = new JLabel("", 0);
        JPanel messageContainer = new JPanel();// wrapper to help fix alignment
        searchButton.addActionListener((event) -> {
            try {
                String query = searchValue.getText().trim();
                if (query.length() > 0) {
                    removeAllRooms();
                    Client.INSTANCE.sendListRooms(query);
                    message.setText("Sent query");
                } else {
                    message.setText("Can't search with an empty query");
                }
            } catch (IOException e) {
                e.printStackTrace();
                message.setText("Error sending request: " + e.getMessage());
            }
        });
        JButton createButton = new JButton("Create");
        createButton.addActionListener((event) -> {
            try {
                String query = searchValue.getText().trim();
                if (query.length() > 0) {
                    Client.INSTANCE.sendCreateRoom(query);
                    message.setText("Created room");
                } else {
                    message.setText("Can't create a room without a name");
                }
            } catch (IOException e) {
                e.printStackTrace();
                message.setText("Error sending request: " + e.getMessage());
            }
        });
        JButton joinButton = new JButton("Join");
        joinButton.addActionListener((event) -> {
            try {
                String query = searchValue.getText().trim();
                if (query.length() > 0) {
                    Client.INSTANCE.sendJoinRoom(query);
                    message.setText("Joined room");
                } else {
                    message.setText("Can't join a room without a name");
                }
            } catch (NullPointerException ne) {
                message.setText("Not connected");
            } catch (IOException e) {
                logger.log(Level.WARNING, "Not connected");
                e.printStackTrace();
                message.setText("Error sending request: " + e.getMessage());
            }
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
        this.add(container, BorderLayout.CENTER);
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

    public void setMessage(String message) {
        this.message.setText(message);
    }

    public void addRoom(String room) {
        if (room != null) {
            System.out.println("Adding: " + room);
            RoomListItem r = new RoomListItem(room, this::handleSelection);
            Dimension size = new Dimension(this.getSize().width, 40);
            r.setPreferredSize(size);
            r.setMaximumSize(size);
            r.setMinimumSize(size);
            container.add(r);
            rooms.add(r);
        }
    }

    public void removeRoom(String room) {
        Iterator<RoomListItem> iter = rooms.iterator();
        while (iter.hasNext()) {
            RoomListItem r = iter.next();
            if (r.getRoomName().equalsIgnoreCase(room)) {
                r.removeAll();
                container.remove(r);
                iter.remove();
                break;
            }
        }
    }

    public void removeAllRooms() {
        System.out.println("Clearing rooms");
        Iterator<RoomListItem> iter = rooms.iterator();
        while (iter.hasNext()) {
            RoomListItem r = iter.next();
            System.out.println("Removing " + r.getRoomName());
            container.remove(r);

            iter.remove();
        }
    }

    public void handleSelection(String room) {
        try {
            Client.INSTANCE.sendJoinRoom(room);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

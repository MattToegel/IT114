package Module6.Part8.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import Module6.Part8.client.views.RoomsPanel;
import Module6.Part8.common.Constants;

public class ClientUI extends JFrame implements IClientEvents {
    CardLayout card = null;// accessible so we can call next() and previous()
    Container container;// accessible to be passed to card methods
    String originalTitle = null;
    private static Logger logger = Logger.getLogger(ClientUI.class.getName());
    private JPanel currentCardPanel = null;
    private JPanel chatArea = null;
    private JPanel userListArea = null;
    private Card currentCard = Card.CONNECT;

    private String host;
    private int port;
    private String username;

    private enum Card {
        CONNECT, USER_INFO, CHAT, ROOMS
    }

    private Hashtable<Long, String> userList = new Hashtable<Long, String>();

    private long myId = Constants.DEFAULT_CLIENT_ID;
    private JMenuBar menu;
    private RoomsPanel roomsPanel;

    public ClientUI(String title) {
        super(title);// call the parent's constructor
        originalTitle = title;
        container = getContentPane();
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // System.out.println("Resized to " + e.getComponent().getSize());
                // rough concepts for handling resize
                container.setPreferredSize(e.getComponent().getSize());
                container.revalidate();
                container.repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                // System.out.println("Moved to " + e.getComponent().getLocation());
            }
        });
        // this tells the x button what to do
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(400, 400));
        // centers window
        setLocationRelativeTo(null);
        card = new CardLayout();
        setLayout(card);
        createMenu();
        // separate views
        createConnectionScreen();
        ceateUserInputScreen();
        createChatScreen();
        roomsPanel = new RoomsPanel(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                previous();
                return null;
            }
        });
        roomsPanel.setName(Card.ROOMS.name());
        add(roomsPanel, Card.ROOMS.name());
        // lastly
        pack();// tells the window to resize itself and do the layout management
        setVisible(true);
    }

    private void createMenu() {
        menu = new JMenuBar();
        JMenu roomsMenu = new JMenu("Rooms");
        JMenuItem roomsSearch = new JMenuItem("Search");
        roomsSearch.addActionListener((event) -> {
            show(Card.ROOMS.name());
        });
        roomsMenu.add(roomsSearch);
        menu.add(roomsMenu);
        this.setJMenuBar(menu);
    }

    private void createConnectionScreen() {
        JPanel parent = new JPanel(
                new BorderLayout(10, 10));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        // add host info
        JLabel hostLabel = new JLabel("Host:");
        JTextField hostValue = new JTextField("127.0.0.1");
        JLabel hostError = new JLabel();
        content.add(hostLabel);
        content.add(hostValue);
        content.add(hostError);
        // add port info
        JLabel portLabel = new JLabel("Port:");
        JTextField portValue = new JTextField("3000");
        JLabel portError = new JLabel();
        content.add(portLabel);
        content.add(portValue);
        content.add(portError);
        // add button
        JButton button = new JButton("Next");
        // add listener
        button.addActionListener((event) -> {
            boolean isValid = true;
            try {
                port = Integer.parseInt(portValue.getText());
                portError.setVisible(false);
                // if valid, next card

            } catch (NumberFormatException e) {
                portError.setText("Invalid port value, must be a number");
                portError.setVisible(true);
                isValid = false;
            }
            if (isValid) {
                host = hostValue.getText();
                next();
            }
        });
        content.add(button);
        // filling the other slots for spacing
        parent.add(new JPanel(), BorderLayout.WEST);
        parent.add(new JPanel(), BorderLayout.EAST);
        parent.add(new JPanel(), BorderLayout.NORTH);
        parent.add(new JPanel(), BorderLayout.SOUTH);
        // add the content to the center slot
        parent.add(content, BorderLayout.CENTER);
        parent.setName(Card.CONNECT.name());
        this.add(Card.CONNECT.name(), parent);// this is the JFrame

    }

    private void ceateUserInputScreen() {
        JPanel parent = new JPanel(
                new BorderLayout(10, 10));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel userLabel = new JLabel("Username: ");
        JTextField userValue = new JTextField();
        JLabel userError = new JLabel();
        content.add(userLabel);
        content.add(userValue);
        content.add(userError);
        content.add(Box.createRigidArea(new Dimension(0, 200)));

        JButton pButton = new JButton("Previous");
        pButton.addActionListener((event) -> {
            previous();
        });
        JButton nButton = new JButton("Connect");
        nButton.addActionListener((event) -> {

            boolean isValid = true;

            try {
                username = userValue.getText();
                if (username.trim().length() == 0) {
                    userError.setText("Username must be provided");
                    userError.setVisible(true);
                    isValid = false;
                }
            } catch (NullPointerException e) {
                userError.setText("Username must be provided");
                userError.setVisible(true);
                isValid = false;
            }
            if (isValid) {
                // System.out.println("Chosen username: " + username);
                logger.log(Level.INFO, "Chosen username: " + username);
                userError.setVisible(false);
                setTitle(originalTitle + " - " + username);
                Client.INSTANCE.connect(host, port, username, this);
                next();
                // System.out.println("Connection process would be triggered");
            }
        });
        // button holder
        JPanel buttons = new JPanel();
        buttons.add(pButton);
        buttons.add(nButton);

        content.add(buttons);
        parent.add(new JPanel(), BorderLayout.WEST);
        parent.add(new JPanel(), BorderLayout.EAST);
        parent.add(new JPanel(), BorderLayout.NORTH);
        parent.add(new JPanel(), BorderLayout.SOUTH);
        parent.add(content, BorderLayout.CENTER);
        parent.setName(Card.USER_INFO.name());
        this.add(Card.USER_INFO.name(), parent);// JFrame

    }

    private JPanel createUserListPanel() {
        JPanel parent = new JPanel(
                new BorderLayout(10, 10));
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

        wrapper.add(scroll);
        parent.add(wrapper, BorderLayout.CENTER);
        // set the dimensions based on the frame size
        int w = (int) Math.ceil(this.getWidth() * .3f);
        parent.setPreferredSize(new Dimension(w, this.getHeight()));
        userListArea.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {
                if (userListArea.isVisible()) {
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
        return parent;
    }

    private void createChatScreen() {
        JPanel parent = new JPanel(
                new BorderLayout(10, 10));
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        // wraps a viewport to provide scroll capabilities
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        // no need to add content specifically because scroll wraps it
        wrapper.add(scroll);
        parent.add(wrapper, BorderLayout.CENTER);

        JPanel input = new JPanel();
        input.setLayout(new BoxLayout(input, BoxLayout.X_AXIS));
        JTextField textValue = new JTextField();
        input.add(textValue);
        JButton button = new JButton("Send");
        // lets us submit with the enter key instead of just the button click
        textValue.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    button.doClick();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }

        });
        button.addActionListener((event) -> {
            try {
                String text = textValue.getText().trim();
                if (text.length() > 0) {
                    Client.INSTANCE.sendMessage(text);
                    textValue.setText("");// clear the original text

                    // debugging
                    logger.log(Level.FINEST, "Content: " + content.getSize());
                    logger.log(Level.FINEST, "Parent: " + parent.getSize());

                }
            } catch (NullPointerException e) {
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        chatArea = content;
        input.add(button);
        parent.add(createUserListPanel(), BorderLayout.EAST);
        parent.add(input, BorderLayout.SOUTH);
        parent.setName(Card.CHAT.name());
        this.add(Card.CHAT.name(), parent);
        chatArea.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {
                if (chatArea.isVisible()) {
                    chatArea.revalidate();
                    chatArea.repaint();
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (chatArea.isVisible()) {
                    chatArea.revalidate();
                    chatArea.repaint();
                }
            }

        });
    }

    private void addUserListItem(long clientId, String clientName) {
        logger.log(Level.INFO, "Adding user to list: " + clientName);
        JPanel content = userListArea;
        logger.log(Level.INFO, "Userlist: " + content.getSize());
        JEditorPane textContainer = new JEditorPane("text/plain", clientName);
        textContainer.setName(clientId + "");
        // sizes the panel to attempt to take up the width of the container
        // and expand in height based on word wrapping
        textContainer.setLayout(null);
        textContainer.setPreferredSize(
                new Dimension(content.getWidth(), calcHeightForText(clientName, content.getWidth())));
        textContainer.setMaximumSize(textContainer.getPreferredSize());
        textContainer.setEditable(false);
        // remove background and border (comment these out to see what it looks like
        // otherwise)
        textContainer.setOpaque(false);
        textContainer.setBorder(BorderFactory.createEmptyBorder());
        textContainer.setBackground(new Color(0, 0, 0, 0));
        // add to container
        content.add(textContainer);
    }

    private void removeUserListItem(long clientId) {
        logger.log(Level.INFO, "removing user list item for id " + clientId);
        Component[] cs = userListArea.getComponents();
        for (Component c : cs) {
            if (c.getName().equals(clientId + "")) {
                userListArea.remove(c);
                break;
            }
        }
    }

    private void clearUserList() {
        Component[] cs = userListArea.getComponents();
        for (Component c : cs) {
            userListArea.remove(c);
        }
    }

    private void addText(String text) {
        JPanel content = chatArea;
        // add message
        JEditorPane textContainer = new JEditorPane("text/plain", text);

        // sizes the panel to attempt to take up the width of the container
        // and expand in height based on word wrapping
        textContainer.setLayout(null);
        textContainer.setPreferredSize(
                new Dimension(content.getWidth(), calcHeightForText(text, content.getWidth())));
        textContainer.setMaximumSize(textContainer.getPreferredSize());
        textContainer.setEditable(false);
        // remove background and border (comment these out to see what it looks like
        // otherwise)
        textContainer.setOpaque(false);
        textContainer.setBorder(BorderFactory.createEmptyBorder());
        textContainer.setBackground(new Color(0, 0, 0, 0));
        // add to container and tell the layout to revalidate
        content.add(textContainer);
        // scroll down on new message
        JScrollBar vertical = ((JScrollPane) chatArea.getParent().getParent()).getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    void next() {
        card.next(container);
        for (Component c : container.getComponents()) {
            if (c.isVisible()) {
                currentCardPanel = (JPanel) c;
                currentCard = Enum.valueOf(Card.class, currentCardPanel.getName());
                break;
            }
        }
        System.out.println(currentCardPanel.getName());
    }

    void previous() {
        card.previous(container);
        for (Component c : container.getComponents()) {
            if (c.isVisible()) {
                currentCardPanel = (JPanel) c;
                currentCard = Enum.valueOf(Card.class, currentCardPanel.getName());
                break;
            }
        }
        System.out.println(currentCardPanel.getName());
    }

    void show(String cardName) {
        card.show(container, cardName);
        for (Component c : container.getComponents()) {
            if (c.isVisible()) {
                currentCardPanel = (JPanel) c;
                currentCard = Enum.valueOf(Card.class, currentCardPanel.getName());
                break;
            }
        }
        System.out.println(currentCardPanel.getName());
    }

    /***
     * Attempts to calculate the necessary dimensions for a potentially wrapped
     * string of text. This isn't perfect and some extra whitespace above or below
     * the text may occur
     * 
     * @param str
     * @return
     */
    private int calcHeightForText(String str, int width) {
        FontMetrics metrics = container.getGraphics().getFontMetrics(container.getFont());
        int hgt = metrics.getHeight();
        logger.log(Level.FINEST, "Font height: " + hgt);
        int adv = metrics.stringWidth(str);
        final int PIXEL_PADDING = 6;
        Dimension size = new Dimension(adv, hgt + PIXEL_PADDING);
        final float PADDING_PERCENT = 1.1f;
        // calculate modifier to line wrapping so we can display the wrapped message
        int mult = (int) Math.round(size.width / (width * PADDING_PERCENT));
        // System.out.println(mult);
        mult++;
        return size.height * mult;
    }

    public static void main(String[] args) {
        new ClientUI("Client");
    }

    private String mapClientId(long clientId) {
        String clientName = userList.get(clientId);
        if (clientName == null) {
            clientName = "Server";
        }
        return clientName;
    }

    /**
     * Used to handle new client connects/disconnects or existing client lists (one
     * by one)
     * 
     * @param clientId
     * @param clientName
     * @param isConnect
     */
    private synchronized void processClientConnectionStatus(long clientId, String clientName, boolean isConnect) {
        if (isConnect) {
            if (!userList.containsKey(clientId)) {
                logger.log(Level.INFO, String.format("Adding %s[%s]", clientName, clientId));
                userList.put(clientId, clientName);
                addUserListItem(clientId, String.format("%s (%s)", clientName, clientId));
            }
        } else {
            if (userList.containsKey(clientId)) {
                logger.log(Level.INFO, String.format("Removing %s[%s]", clientName, clientId));
                userList.remove(clientId);
                removeUserListItem(clientId);
            }
            if (clientId == myId) {
                logger.log(Level.INFO, "I disconnected");
                myId = Constants.DEFAULT_CLIENT_ID;
                previous();
            }
        }
    }

    @Override
    public void onClientConnect(long clientId, String clientName, String message) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            processClientConnectionStatus(clientId, clientName, true);
            addText(String.format("*%s %s*", clientName, message));
        }
    }

    @Override
    public void onClientDisconnect(long clientId, String clientName, String message) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            processClientConnectionStatus(clientId, clientName, false);
            addText(String.format("*%s %s*", clientName, message));
        }
    }

    @Override
    public void onMessageReceive(long clientId, String message) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            String clientName = mapClientId(clientId);
            addText(String.format("%s: %s", clientName, message));
        }
    }

    @Override
    public void onReceiveClientId(long id) {
        if (myId == Constants.DEFAULT_CLIENT_ID) {
            myId = id;
        } else {
            logger.log(Level.WARNING, "Received client id after already being set, this shouldn't happen");
        }
    }

    @Override
    public void onResetUserList() {
        userList.clear();
        clearUserList();
    }

    @Override
    public void onSyncClient(long clientId, String clientName) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            processClientConnectionStatus(clientId, clientName, true);
        }
    }

    @Override
    public void onReceiveRoomList(String[] rooms, String message) {
        roomsPanel.removeAllRooms();
        if (message != null && message.length() > 0) {
            roomsPanel.setMessage(message);
        }
        if (rooms != null) {
            for (String room : rooms) {
                roomsPanel.addRoom(room);
            }
        }
    }

    @Override
    public void onRoomJoin(String roomName) {
        
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            addText("Joined room " + roomName);
        }
    }
}

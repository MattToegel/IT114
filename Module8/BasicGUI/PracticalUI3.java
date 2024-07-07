package Module8.BasicGUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * PracticalUI3 demonstrates a basic multi-screen GUI application using Swing.
 * It consists of connection, user input, and chat screens.
 */
public class PracticalUI3 extends JFrame {
    private CardLayout card = null; // Layout manager to switch between different screens
    private Container container; // Container to hold different panels
    private String originalTitle = null;
    private String username = "";

    /**
     * Constructor to create the main application window.
     * 
     * @param title The title of the window.
     */
    public PracticalUI3(String title) {
        super(title); // Call the parent's constructor to set the frame title
        originalTitle = title;
        container = getContentPane();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace(); // Consider using a logging framework for production code
        }
        // Add a listener to handle window resize and move events
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                System.out.println("Resized to " + e.getComponent().getSize());
                // Adjust container size on resize
                container.setPreferredSize(e.getComponent().getSize());
                container.invalidate();
                container.repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                System.out.println("Moved to " + e.getComponent().getLocation());
            }
        });

        // This tells the X button what to do (exit the application)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(400, 400));
        // Center the window
        setLocationRelativeTo(null);
        card = new CardLayout();
        setLayout(card);

        // Create and add different screens
        createConnectionScreen();
        createUserInputScreen();
        createChatScreen();

        // Pack the components within the window
        pack();
        setVisible(true);
    }

    /**
     * Creates the connection screen with fields for host and port.
     */
    private void createConnectionScreen() {
        JPanel parent = new JPanel(new BorderLayout(10, 10));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.RED, 1),
                new EmptyBorder(10, 10, 10, 10))); // Add padding and colored border

        // Add host input field
        JLabel hostLabel = new JLabel("Host:");
        JTextField hostValue = new JTextField("127.0.0.1");
        JLabel hostError = new JLabel();
        hostError.setVisible(false);
        content.add(hostLabel);
        content.add(hostValue);
        content.add(hostError);

        // Add port input field
        JLabel portLabel = new JLabel("Port:");
        JTextField portValue = new JTextField("3000");
        JLabel portError = new JLabel();
        portError.setVisible(false);
        content.add(portLabel);
        content.add(portValue);
        content.add(portError);

        // Add Next button
        JButton button = new JButton("Next");
        button.setAlignmentX(JButton.CENTER_ALIGNMENT); // Center the button
        button.addActionListener((event) -> {
            boolean isValid = true;
            try {
                Integer.parseInt(portValue.getText());
                portError.setVisible(false);
            } catch (NumberFormatException e) {
                portError.setText("Invalid port value, must be a number");
                portError.setVisible(true);
                isValid = false;
            }
            if (isValid) {
                card.next(container); // Go to the next screen
            }
        });
        content.add(Box.createVerticalStrut(10)); // Add spacing
        content.add(button);

        parent.add(content, BorderLayout.CENTER);
        this.add(parent); // This is the JFrame
    }

    /**
     * Creates the user input screen for entering the username.
     */
    private void createUserInputScreen() {
        JPanel parent = new JPanel(new BorderLayout(10, 10));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding

        // Add username input field
        JLabel userLabel = new JLabel("Username: ");
        JTextField userValue = new JTextField();
        JLabel userError = new JLabel();
        userError.setVisible(false);
        content.add(userLabel);
        content.add(userValue);
        content.add(userError);
        content.add(Box.createRigidArea(new Dimension(0, 200))); // Add vertical space

        // Add Previous and Connect buttons
        JButton pButton = new JButton("Previous");
        pButton.addActionListener((event) -> card.previous(container));
        JButton nButton = new JButton("Connect");
        nButton.addActionListener((event) -> {
            boolean isValid = true;
            String _username = userValue.getText();
            if (_username.trim().isEmpty()) {
                userError.setText("Username must be provided");
                userError.setVisible(true);
                isValid = false;
            }
            if (isValid) {
                System.out.println("Chosen username: " + _username);
                userError.setVisible(false);
                setTitle(originalTitle + " - " + _username);
                username = _username;
                card.next(container); // Go to the next screen
                System.out.println("Connection process would be triggered");
            }
        });

        JPanel buttons = new JPanel();
        buttons.add(pButton);
        buttons.add(nButton);

        content.add(buttons);
        parent.add(content, BorderLayout.CENTER);
        this.add(parent); // JFrame
    }

    /**
     * Creates the chat screen where messages can be sent and displayed.
     */
    private void createChatScreen() {
        JPanel parent = new JPanel(new BorderLayout(10, 10));
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.RED, 0),
                new EmptyBorder(10, 10, 0, 10))); // Add padding and colored border

        JPanel content = new JPanel(new GridBagLayout());
        content.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        // Wraps a viewport to provide scroll capabilities
        JScrollPane scroll = new JScrollPane(content, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        wrapper.add(scroll);
        parent.add(wrapper, BorderLayout.CENTER);

        // Create the input area with a text field and a button
        JPanel input = new JPanel();
        input.setLayout(new BoxLayout(input, BoxLayout.X_AXIS));
        input.setBorder(new EmptyBorder(0, 5, 5, 5)); // Add padding

        JTextField textValue = new JTextField();
        input.add(textValue);

        JButton button = new JButton("Send");
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
            String text = textValue.getText().trim();
            if (!text.isEmpty()) {
                text = String.format("%s: %s", username, text);
                addText(text, content); // Add the message to the chat area
                textValue.setText(""); // Clear the text field
                // Scroll down to the newest message
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vertical = scroll.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                });
            }
        });

        input.add(button);
        parent.add(input, BorderLayout.SOUTH);
        this.add(parent); // JFrame

        // Add vertical glue to push messages to the bottom
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; // Column index 0
        gbc.gridy = GridBagConstraints.RELATIVE; // Automatically move to the next row
        gbc.weighty = 1.0; // Give extra space vertically to this component
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        content.add(Box.createVerticalGlue(), gbc);
    }

    /**
     * Adds a message to the chat area.
     * 
     * @param text    The text of the message.
     * @param content The JPanel to add the message to.
     */
    private void addText(String text, JPanel content) {
        JEditorPane textContainer = new JEditorPane("text/plain", text);
        textContainer.setEditable(false);
        textContainer.setBorder(BorderFactory.createEmptyBorder());

        // Account for the width of the vertical scrollbar
        JScrollPane parentScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, content);
        int scrollBarWidth = parentScrollPane.getVerticalScrollBar().getPreferredSize().width;

        // Adjust the width of the text container
        int availableWidth = content.getWidth() - scrollBarWidth - 10; // Subtract an additional padding
        textContainer.setSize(new Dimension(availableWidth, Integer.MAX_VALUE));
        Dimension d = textContainer.getPreferredSize();
        textContainer.setPreferredSize(new Dimension(availableWidth, d.height));
        // Remove background and border
        textContainer.setOpaque(false);
        textContainer.setBorder(BorderFactory.createEmptyBorder());
        textContainer.setBackground(new Color(0, 0, 0, 0));

        // GridBagConstraints settings for each message
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; // Column index 0
        gbc.gridy = GridBagConstraints.RELATIVE; // Automatically move to the next row
        gbc.weightx = 1; // Let the component grow horizontally to fill the space
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally
        gbc.insets = new Insets(0, 0, 5, 0); // Add spacing between messages

        content.add(textContainer, gbc);
        content.revalidate();
        content.repaint();
    }

    /**
     * The main method to run the PracticalUI3 application.
     * 
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new PracticalUI3("Practical UI 3"));
    }
}

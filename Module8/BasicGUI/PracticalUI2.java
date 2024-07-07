package Module8.BasicGUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
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
import javax.swing.border.EmptyBorder;

/**
 * PracticalUI2 demonstrates a basic chatroom interface using Java Swing.
 */
public class PracticalUI2 extends JFrame {
    private CardLayout card = null; // Layout manager for switching between views
    private Container container; // Main container to hold different views
    private String originalTitle = null; // Store the original window title

    /**
     * Constructs the main frame for PracticalUI2.
     *
     * @param title the title of the window
     */
    public PracticalUI2(String title) {
        super(title); // Call the parent's constructor
        originalTitle = title;
        container = getContentPane();
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                System.out.println("Resized to " + e.getComponent().getSize());
                // Handle resize
                container.setPreferredSize(e.getComponent().getSize());
                container.invalidate();
                container.repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                System.out.println("Moved to " + e.getComponent().getLocation());
            }
        });
        // Handle the close operation
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(400, 400));
        // Center the window
        setLocationRelativeTo(null);
        card = new CardLayout();
        setLayout(card);
        // Create different views
        createConnectionScreen();
        createUserInputScreen();
        createChatScreen();
        // Final setup
        pack(); // Adjust the window size
        setVisible(true);
    }

    /**
     * Creates the connection screen for entering host and port.
     */
    private void createConnectionScreen() {
        JPanel parent = new JPanel(new BorderLayout(10, 10));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding

        // Add host info
        JLabel hostLabel = new JLabel("Host:");
        JTextField hostValue = new JTextField("127.0.0.1");
        JLabel hostError = new JLabel();
        hostError.setVisible(false);
        content.add(hostLabel);
        content.add(hostValue);
        content.add(hostError);

        // Add port info
        JLabel portLabel = new JLabel("Port:");
        JTextField portValue = new JTextField("3000");
        JLabel portError = new JLabel();
        portError.setVisible(false);
        content.add(portLabel);
        content.add(portValue);
        content.add(portError);

        // Add button
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
                card.next(container);
            }
        });
        content.add(Box.createVerticalStrut(10)); // Add spacing
        content.add(button);

        parent.add(content, BorderLayout.CENTER);
        this.add(parent); // This is the JFrame
    }

    /**
     * Creates the user input screen for entering a username.
     */
    private void createUserInputScreen() {
        JPanel parent = new JPanel(new BorderLayout(10, 10));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding

        JLabel userLabel = new JLabel("Username: ");
        JTextField userValue = new JTextField();
        JLabel userError = new JLabel();
        userError.setVisible(false);
        content.add(userLabel);
        content.add(userValue);
        content.add(userError);
        content.add(Box.createRigidArea(new Dimension(0, 200))); // Add vertical space

        JButton pButton = new JButton("Previous");
        pButton.addActionListener((event) -> card.previous(container));
        JButton nButton = new JButton("Connect");
        nButton.addActionListener((event) -> {
            boolean isValid = true;
            String username = userValue.getText();
            if (username.trim().isEmpty()) {
                userError.setText("Username must be provided");
                userError.setVisible(true);
                isValid = false;
            }
            if (isValid) {
                System.out.println("Chosen username: " + username);
                userError.setVisible(false);
                setTitle(originalTitle + " - " + username);
                card.next(container);
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
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        // Wraps a viewport to provide scroll capabilities
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        wrapper.add(scroll);
        parent.add(wrapper, BorderLayout.CENTER);

        JPanel input = new JPanel();
        input.setLayout(new BoxLayout(input, BoxLayout.X_AXIS));
        input.setBorder(new EmptyBorder(5, 5, 5, 5)); // Add padding

        JTextField textValue = new JTextField();
        input.add(textValue);

        JButton button = new JButton("Send");
        textValue.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    button.doClick();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        button.addActionListener((event) -> {
            String text = textValue.getText().trim();
            if (!text.isEmpty()) {
                addText(text, content);
                textValue.setText(""); // Clear the original text
                // Scroll down on new message
                JScrollBar vertical = scroll.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
                // Debugging
                System.out.println("Content: " + content.getSize());
                System.out.println("Parent: " + parent.getSize());
            }
        });

        input.add(button);
        parent.add(input, BorderLayout.SOUTH);
        this.add(parent); // JFrame
    }

    /**
     * Adds a new message to the chat content.
     *
     * @param text the message text
     * @param content the panel to add the message to
     */
    private void addText(String text, JPanel content) {
        JEditorPane textContainer = new JEditorPane("text/plain", text);
        textContainer.setLayout(null);
        textContainer.setPreferredSize(
                new Dimension(content.getWidth(), calcHeightForText(text, content.getWidth())));
        textContainer.setMaximumSize(textContainer.getPreferredSize());
        textContainer.setEditable(false);
        // Remove background and border
        textContainer.setOpaque(false);
        textContainer.setBorder(BorderFactory.createEmptyBorder());
        textContainer.setBackground(new Color(0, 0, 0, 0));

        content.add(textContainer);
        content.revalidate();
    }

    /**
     * Calculates the height needed to display a wrapped text.
     *
     * @param str the text to calculate height for
     * @param width the width available for the text
     * @return the calculated height
     */
    private int calcHeightForText(String str, int width) {
        FontMetrics metrics = container.getGraphics().getFontMetrics(container.getFont());
        int hgt = metrics.getHeight();
        int adv = metrics.stringWidth(str);
        final int PIXEL_PADDING = 6;
        Dimension size = new Dimension(adv, hgt + PIXEL_PADDING);
        final float PADDING_PERCENT = 1.1f;
        int mult = (int) Math.ceil((double) size.width / (width * PADDING_PERCENT));
        mult++;
        return size.height * mult;
    }

    /**
     * The main method to launch the PracticalUI2 application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new PracticalUI2("Practical UI 2"));
    }
}

package Module8.BasicGUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class PracticalUI extends JFrame {
    private CardLayout card = null; // Accessible so we can call next() and previous()
    private Container container; // Accessible to be passed to card methods

    public PracticalUI(String title) {
        super(title); // Call the parent's constructor
        container = getContentPane();
        // This tells the X button what to do
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(400, 400));
        // Centers window
        setLocationRelativeTo(null);
        card = new CardLayout();
        setLayout(card);
        // Separate views
        createConnectionScreen();
        createUserInputScreen();
        // Lastly
        pack(); // Tells the window to resize itself and do the layout management
        setVisible(true);
    }

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
                // If valid, next card
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

    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new PracticalUI("Practical UI"));
    }
}


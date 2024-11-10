package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import Project.Client.CardView;
import Project.Client.Interfaces.ICardControls;
import Project.Common.LoggerUtil;

/**
 * UserDetailsPanel represents the UI for entering user details like username.
 */
public class UserDetailsPanel extends JPanel {
    private String username;

    /**
     * Constructor to create the UserDetailsPanel UI.
     * 
     * @param controls The card controls interface to handle navigation.
     */
    public UserDetailsPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));

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
        JButton previousButton = new JButton("Previous");
        previousButton.addActionListener(event -> controls.previous());
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(event -> {
            String incomingUsername = userValue.getText().trim();
            if (incomingUsername.isEmpty()) {
                userError.setText("Username must be provided");
                userError.setVisible(true);
            } else {
                username = incomingUsername;
                LoggerUtil.INSTANCE.info("Chosen username: " + username);
                userError.setVisible(false);
                controls.connect();
            }
        });

        JPanel buttons = new JPanel();
        buttons.add(previousButton);
        buttons.add(connectButton);

        content.add(Box.createVerticalGlue()); // Push buttons to the bottom
        content.add(buttons);

        // Add the content panel to the center of the BorderLayout
        this.add(content, BorderLayout.CENTER);

        // Add empty borders to the sides for spacing
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        this.setName(CardView.USER_INFO.name());
        controls.addPanel(CardView.USER_INFO.name(), this);
    }

    /**
     * Gets the username entered by the user.
     * 
     * @return The username.
     */
    public String getUsername() {
        return username;
    }
}

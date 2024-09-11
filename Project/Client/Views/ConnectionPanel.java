package Project.Client.Views;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Project.Client.CardView;
import Project.Client.ICardControls;

public class ConnectionPanel extends JPanel {
    private String host;
    private int port;

    public ConnectionPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));

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
                controls.next();
            }
        });
        content.add(button);
        // filling the other slots for spacing
        this.add(new JPanel(), BorderLayout.WEST);
        this.add(new JPanel(), BorderLayout.EAST);
        this.add(new JPanel(), BorderLayout.NORTH);
        this.add(new JPanel(), BorderLayout.SOUTH);
        // add the content to the center slot
        this.add(content, BorderLayout.CENTER);
        this.setName(CardView.CONNECT.name());
        controls.addPanel(CardView.CONNECT.name(), this);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}

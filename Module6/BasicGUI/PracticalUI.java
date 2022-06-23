package Module6.BasicGUI;

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

public class PracticalUI extends JFrame {
    CardLayout card = null;//accessible so we can call next() and previous()
    Container container;//accessible to be passed to card methods

    public PracticalUI(String title) {
        super(title);// call the parent's constructor
        container = getContentPane();
        // this tells the x button what to do
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(400, 400));
        // centers window
        setLocationRelativeTo(null);
        card = new CardLayout();
        setLayout(card);
        // separate views
        createConnectionScreen();
        ceateUserInputScreen();
        // lastly
        pack();// tells the window to resize itself and do the layout management
        setVisible(true);
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
                Integer.parseInt(portValue.getText());
                portError.setVisible(false);
                // if valid, next card

            } catch (NumberFormatException e) {
                portError.setText("Invalid port value, must be a number");
                portError.setVisible(true);
                isValid = false;
            }
            if (isValid) {
                card.next(container);
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
        this.add(parent);// this is the JFrame

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
        content.add(Box.createRigidArea(new Dimension(0,200)));

        JButton pButton = new JButton("Previous");
        pButton.addActionListener((event) -> {
            card.previous(container);
        });
        JButton nButton = new JButton("Connect");
        nButton.addActionListener((event)->{
            
            boolean isValid = true;
            String username = "";
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
                System.out.println("Chosen username: " + username);
                userError.setVisible(false);
                card.next(container);
                System.out.println("Connection process would be triggered");
            }
        });
        //button holder
        JPanel buttons = new JPanel();
        buttons.add(pButton);
        buttons.add(nButton);
        
        content.add(buttons);
        parent.add(new JPanel(), BorderLayout.WEST);
        parent.add(new JPanel(), BorderLayout.EAST);
        parent.add(new JPanel(), BorderLayout.NORTH);
        parent.add(new JPanel(), BorderLayout.SOUTH);
        parent.add(content, BorderLayout.CENTER);
        this.add(parent);//JFrame

    }

    public static void main(String[] args) {
        new PracticalUI("Practical UI");
    }
}

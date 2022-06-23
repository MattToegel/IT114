package Module6.BasicGUI;

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

public class PracticalUI2 extends JFrame {
    CardLayout card = null;// accessible so we can call next() and previous()
    Container container;// accessible to be passed to card methods
    String originalTitle = null;

    public PracticalUI2(String title) {
        super(title);// call the parent's constructor
        originalTitle = title;
        container = getContentPane();
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                System.out.println("Resized to " + e.getComponent().getSize());
                // rough concepts for handling resize
                container.setPreferredSize(e.getComponent().getSize());
                container.invalidate();
                container.repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                System.out.println("Moved to " + e.getComponent().getLocation());
            }
        });
        // this tells the x button what to do
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(400, 400));
        // centers window
        setLocationRelativeTo(null);
        card = new CardLayout();
        setLayout(card);
        // separate views
        createConnectionScreen();
        ceateUserInputScreen();
        createChatScreen();
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
        content.add(Box.createRigidArea(new Dimension(0, 200)));

        JButton pButton = new JButton("Previous");
        pButton.addActionListener((event) -> {
            card.previous(container);
        });
        JButton nButton = new JButton("Connect");
        nButton.addActionListener((event) -> {

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
                setTitle(originalTitle + " - " + username);
                card.next(container);
                System.out.println("Connection process would be triggered");
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
        this.add(parent);// JFrame

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
                    addText(text, content);
                    textValue.setText("");// clear the original text
                    // scroll down on new message
                    JScrollBar vertical = scroll.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                    // debugging
                    System.out.println("Content: " + content.getSize());
                    System.out.println("Parent: " + parent.getSize());

                }
            } catch (NullPointerException e) {
            }
        });

        input.add(button);
        parent.add(input, BorderLayout.SOUTH);
        this.add(parent);
    }

    private void addText(String text, JPanel content) {
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
        content.revalidate();

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
        System.out.println("Font height: " + hgt);
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
        new PracticalUI2("Practical UI 2");
    }
}

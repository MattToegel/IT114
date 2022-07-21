package LifeForLife.client.views;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import LifeForLife.client.Card;
import LifeForLife.client.Client;
import LifeForLife.client.ClientUtils;
import LifeForLife.client.ICardControls;
import LifeForLife.common.MyLogger;

public class ChatPanel extends JPanel {
    private static MyLogger logger = MyLogger.getLogger(ChatPanel.class.getName());
    private JPanel chatArea = null;
    private JPanel wrapper = null;
    private UserListPanel userListPanel;
    private Dimension lastSize = new Dimension();

    public ChatPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));
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
        this.add(wrapper, BorderLayout.CENTER);

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
                    logger.fine("Content: " + content.getSize());
                    logger.fine("Parent: " + this.getSize());

                }
            } catch (NullPointerException e) {
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        chatArea = content;
        this.wrapper = wrapper;
        input.add(button);
        userListPanel = new UserListPanel(controls);
        this.add(userListPanel, BorderLayout.EAST);
        this.add(input, BorderLayout.SOUTH);
        this.setName(Card.CHAT.name());
        controls.addPanel(Card.CHAT.name(), this);
        chatArea.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {
                if (chatArea.isVisible()) {
                    // scroll down on new message
                    
                    chatArea.revalidate();
                    chatArea.repaint();
                    /**
                     * Note: with the setValue(maxValue) it seemed to have a gap.
                     * The gap would cut off the last message.
                     * The updated logic below from https://stackoverflow.com/a/34086741
                     * solves this.
                     */
                    JScrollBar vertical = ((JScrollPane) chatArea.getParent().getParent()).getVerticalScrollBar();
                    AdjustmentListener scroller = new AdjustmentListener() {
                        @Override
                        public void adjustmentValueChanged(AdjustmentEvent e) {
                            Adjustable adjustable = e.getAdjustable();
                            adjustable.setValue(vertical.getMaximum());
                            // We have to remove the listener, otherwise the
                            // user would be unable to scroll afterwards
                            vertical.removeAdjustmentListener(this);
                        }

                    };
                    vertical.addAdjustmentListener(scroller);
                    
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
        wrapper.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {

                super.componentShown(e);
                logger.info("Component shown");

                doResize();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                // System.out.println("Resized to " + e.getComponent().getSize());
                // rough concepts for handling resize
                // set the dimensions based on the frame size
                doResize();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                // System.out.println("Moved to " + e.getComponent().getLocation());
            }
        });
    }

    private void doResize() {
        if (!this.isVisible()) {
            return;
        }
        Dimension frameSize = wrapper.getSize();
        int deltaX = Math.abs(frameSize.width - lastSize.width);
        int deltaY = Math.abs(frameSize.height - lastSize.height);
        if (deltaX >= 5 || deltaY >= 5) {
            lastSize = frameSize;

            logger.info("Wrapper size: " + frameSize);
            int w = (int) Math.ceil(frameSize.getWidth() * .3f);

            userListPanel.setPreferredSize(new Dimension(w, (int) frameSize.getHeight()));
            userListPanel.revalidate();
            userListPanel.repaint();
            w = (int) Math.ceil(frameSize.getWidth() * .7f);
            //preferred size was preventing it from growing with its children
            //chatArea.setPreferredSize(new Dimension(w, (int) Short.MAX_VALUE));
            chatArea.setMinimumSize(new Dimension(w, (int) frameSize.getHeight()));
            userListPanel.resizeUserListItems();
            resizeMessages();
            // scroll down on new message
            JScrollBar vertical = ((JScrollPane) chatArea.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        }
    }

    private void resizeMessages() {
        for (Component p : chatArea.getComponents()) {
            if (p.isVisible()) {
                p.setPreferredSize(
                        new Dimension(wrapper.getWidth(), ClientUtils.calcHeightForText(this,
                                ((JEditorPane) p).getText(), wrapper.getWidth())));
                p.setMaximumSize(p.getPreferredSize());

            }
        }
        chatArea.revalidate();
        chatArea.repaint();
    }

    public void addUserListItem(long clientId, String clientName) {
        userListPanel.addUserListItem(clientId, clientName);
    }

    public void removeUserListItem(long clientId) {
        userListPanel.removeUserListItem(clientId);
    }

    public void clearUserList() {
        userListPanel.clearUserList();
    }

    public void addText(String text) {
        JPanel content = chatArea;
        // add message
        JEditorPane textContainer = new JEditorPane("text/html", text);

        // sizes the panel to attempt to take up the width of the container
        // and expand in height based on word wrapping
        textContainer.setLayout(null);
        textContainer.setPreferredSize(
                new Dimension(content.getWidth(), ClientUtils.calcHeightForText(this, text, content.getWidth())));
        textContainer.setMaximumSize(textContainer.getPreferredSize());
        textContainer.setEditable(false);
        ClientUtils.clearBackground(textContainer);
        // add to container and tell the layout to revalidate
        content.add(textContainer);
        
    }
}
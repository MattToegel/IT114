package Project.Client.Views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import Project.Client.CardView;
import Project.Client.Interfaces.ICardControls;

public class ChatGamePanel extends JPanel {
    private ChatPanel chatPanel;
    private GamePanel gamePanel;
    Dimension zero = new Dimension(0, 0);

    public ChatGamePanel(ICardControls controls) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        chatPanel = new ChatPanel(controls);
        gamePanel = new GamePanel(controls);
        gamePanel.setVisible(false);
        gamePanel.setBackground(Color.BLUE);
        chatPanel.setBackground(Color.GRAY);

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gamePanel, chatPanel);
        splitPane.setResizeWeight(.6);

        splitPane.setOneTouchExpandable(false); // This disables the one-touch expandable buttons
        splitPane.setEnabled(false); // This makes the divider non-movable
        gamePanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // Recalculate the divider location when the left panel becomes visible
                splitPane.setDividerLocation(0.6);
            }
        });
        add(splitPane);
        this.setName(CardView.CHAT_GAME_SCREEN.name());
        controls.addPanel(CardView.CHAT_GAME_SCREEN.name(), this);
        chatPanel.setVisible(true);

    }

    public ChatPanel getChatPanel() {
        return chatPanel;
    }

}
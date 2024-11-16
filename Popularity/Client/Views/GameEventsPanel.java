package Popularity.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import Popularity.Client.Client;
import Popularity.Client.Interfaces.IMessageEvents;
import Popularity.Client.Interfaces.IPhaseEvent;
import Popularity.Client.Interfaces.IReadyEvent;
import Popularity.Client.Interfaces.ITimeEvents;
import Popularity.Common.Constants;
import Popularity.Common.Phase;
import Popularity.Common.TimerType;

public class GameEventsPanel extends JPanel implements IPhaseEvent, IReadyEvent, IMessageEvents, ITimeEvents {
    private JPanel content;
    private boolean debugMode = true; // Set this to false to disable debugging styling
    private JLabel timerText;

    // GridBagConstraints for the vertical glue
    private GridBagConstraints gbcGlue = new GridBagConstraints();

    public GameEventsPanel() {
        super(new BorderLayout(10, 10));
        content = new JPanel(new GridBagLayout());

        if (debugMode) {
            content.setBorder(BorderFactory.createLineBorder(Color.RED)); // Red border for debugging
            content.setBackground(new Color(240, 240, 240)); // Light grey background for debugging
        }

        // Wraps a viewport to provide scroll capabilities
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        if (debugMode) {
            scroll.setBorder(BorderFactory.createLineBorder(Color.GREEN)); // Green border for debugging
        } else {
            scroll.setBorder(BorderFactory.createEmptyBorder());
        }

        this.add(scroll, BorderLayout.CENTER);

        // Add vertical glue to push messages to the top
        gbcGlue.gridx = 0;
        gbcGlue.gridy = GridBagConstraints.RELATIVE;
        gbcGlue.weighty = 1.0; // Give vertical glue a weight
        gbcGlue.fill = GridBagConstraints.BOTH;
        content.add(Box.createVerticalGlue(), gbcGlue);

        timerText = new JLabel();
        this.add(timerText, BorderLayout.NORTH);
        timerText.setVisible(false);
        Client.INSTANCE.addCallback(this);
    }

    public void addText(String text) {
        SwingUtilities.invokeLater(() -> {
            JEditorPane textContainer = new JEditorPane("text/plain", text);
            textContainer.setEditable(false);

            if (debugMode) {
                textContainer.setBorder(BorderFactory.createLineBorder(Color.BLUE)); // Blue border for debugging
                textContainer.setBackground(new Color(255, 255, 200)); // Light yellow background for debugging
            } else {
                textContainer.setBorder(BorderFactory.createEmptyBorder());
                textContainer.setBackground(new Color(0, 0, 0, 0));
            }

            // Set the text and then recalculate the preferred size
            textContainer.setText(text);
            Dimension preferredSize = textContainer.getPreferredSize();
            textContainer.setPreferredSize(new Dimension(content.getWidth(), preferredSize.height));

            // GridBagConstraints settings for each message
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; // Column index 0
            gbc.gridy = content.getComponentCount() - 1; // Place before the glue
            gbc.weightx = 1; // Let the component grow horizontally to fill the space
            gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally
            gbc.insets = new Insets(0, 0, 5, 0); // Add spacing between messages for debugging

            // Remove the glue, add the text, then re-add the glue
            content.remove(content.getComponentCount() - 1);
            content.add(textContainer, gbc);
            content.add(Box.createVerticalGlue(), gbcGlue);

            content.revalidate();
            content.repaint();

            // Scroll down on new message
            JScrollPane parentScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, content);
            if (parentScrollPane != null) {
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vertical = parentScrollPane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                });
            }
        });
    }

    @Override
    public void onReceivePhase(Phase phase) {
        addText(String.format("The current phase is %s", phase));
    }

    @Override
    public void onReceiveReady(long clientId, boolean isReady, boolean isQuiet) {
        if (isQuiet) {
            return;
        }
        String clientName = Client.INSTANCE.getClientNameFromId(clientId);
        addText(String.format("%s[%s] is %s", clientName, clientId, isReady ? "ready" : "not ready"));
    }

    @Override
    public void onMessageReceive(long id, String message) {
        if (id == Constants.GAME_EVENT_CHANNEL) { // using -2 as an internal channel for GameEvents
            addText(message);
        }
    }

    @Override
    public void onTimerUpdate(TimerType timerType, int time) {
        timerText.setText(String.format("%s timer: %s", timerType.name(), time));
        timerText.setVisible(time > 0);
    }
}
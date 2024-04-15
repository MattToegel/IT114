package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import Project.Client.Client;
import Project.Client.ClientUtils;
import Project.Client.IGameEvents;
import Project.Common.CellData;
import Project.Common.Phase;

public class GameEventsPanel extends JPanel implements IGameEvents {
    private JPanel content;

    public GameEventsPanel() {
        super(new BorderLayout(10, 10));
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JPanel content = new JPanel();
        this.content = content;
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        // wraps a viewport to provide scroll capabilities
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        // no need to add content specifically because scroll wraps it
        wrapper.add(scroll, BorderLayout.CENTER);
        content.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {

                if (content.isVisible()) {
                    // scroll down on new message
                    JScrollBar vertical = ((JScrollPane) content.getParent().getParent()).getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                    content.revalidate();
                    content.repaint();
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (content.isVisible()) {
                    // scroll down on new message
                    JScrollBar vertical = ((JScrollPane) content.getParent().getParent()).getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                    content.revalidate();
                    content.repaint();
                }
            }

        });
        Client.INSTANCE.addCallback(this);
        this.add(wrapper);
    }

    private void addText(String text) {
        // add message
        JEditorPane textContainer = new JEditorPane("text/plain", text);

        // sizes the panel to attempt to take up the width of the container
        // and expand in height based on word wrapping
        /*
         * textContainer.setLayout(null);
         * textContainer.setPreferredSize(
         * new Dimension(content.getWidth(), ClientUtils.calcHeightForText(this, text,
         * content.getWidth())));
         * textContainer.setMaximumSize(textContainer.getPreferredSize());
         */
        textContainer.setEditable(false);
        ClientUtils.clearBackground(textContainer);
        // add to container and tell the layout to revalidate
        content.add(textContainer);

    }

    @Override
    public void onClientConnect(long id, String clientName, String message) {

    }

    @Override
    public void onClientDisconnect(long id, String clientName, String message) {

    }

    @Override
    public void onMessageReceive(long id, String message) {

    }

    @Override
    public void onReceiveClientId(long id) {

    }

    @Override
    public void onSyncClient(long id, String clientName) {

    }

    @Override
    public void onResetUserList() {

    }

    @Override
    public void onReceiveRoomList(List<String> rooms, String message) {

    }

    @Override
    public void onRoomJoin(String roomName) {

    }

    @Override
    public void onReceiveReady(long clientId, boolean isReady) {
        if (!isReady) {
            return;
        }
        String message = String.format("%s is ready", Client.INSTANCE.getClientNameFromId(clientId));
        addText(message);
    }

    @Override
    public void onReceivePhase(Phase phase) {
        String message = String.format("The current phase is %s", phase.name());
        addText(message);
    }

    @Override
    public void onReceiveGrid(int rows, int columns) {

    }

    @Override
    public void onReceiveCell(List<CellData> cells) {

    }

    @Override
    public void onReceiveRoll(long clientId, int roll) {
        String message = String.format("%s rolled a %s", Client.INSTANCE.getClientNameFromId(clientId), roll);
        addText(message);
    }

    @Override
    public void onReceivePoints(long clientId, int changedPoints, int currentPoints) {
        String message = String.format("%s %s %s treasure and now has %s",
                Client.INSTANCE.getClientNameFromId(clientId), changedPoints > 0 ? "gained" : "lost", changedPoints,
                currentPoints);
        addText(message);
    }
}

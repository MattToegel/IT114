package Project.Client.Views;

import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * RoomListItem represents an item in the list of rooms with a join button.
 */
public class RoomListItem extends JPanel {
    private JTextField roomName;
    private JButton joinButton;

    /**
     * Constructs a RoomListItem with the specified room name and callback.
     * 
     * @param room   - Name of room to show on the UI.
     * @param onJoin - Callback to trigger when the button is clicked.
     */
    public RoomListItem(String room, Consumer<String> onJoin) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        roomName = new JTextField(room);
        roomName.setEditable(false);
        roomName.setToolTipText("Room name");

        joinButton = new JButton("Join");
        joinButton.setToolTipText("Join this room");

        joinButton.addActionListener((event) -> {
            SwingUtilities.invokeLater(() -> {
                onJoin.accept(roomName.getText());
            });
        });

        this.add(roomName);
        this.add(joinButton);
    }

    /**
     * Gets the room name displayed in this item.
     * 
     * @return the room name.
     */
    public String getRoomName() {
        return roomName.getText();
    }
}

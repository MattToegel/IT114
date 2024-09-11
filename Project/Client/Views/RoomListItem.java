package Project.Client.Views;

import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RoomListItem extends JPanel {
    private JTextField roomName;
    private JButton joinButton;

    /**
     * We pass the room name, and a callback for when the room is selected
     * 
     * @param room   - Name of room to show on the UI
     * @param onJoin - Callback to trigger when button is clicked
     */
    public RoomListItem(String room, Consumer<String> onJoin) {
        // TODO see below links regarding Consumer
        // https://medium.com/swlh/understanding-java-8s-consumer-supplier-predicate-and-function-c1889b9423d
        // https://mkyong.com/java8/java-8-consumer-examples/
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        roomName = new JTextField(room);
        roomName.setEditable(false);
        joinButton = new JButton("Join");
        joinButton.addActionListener((event) -> {
            onJoin.accept(roomName.getText());
        });
        this.add(roomName);
        this.add(joinButton);
    }

    public String getRoomName() {
        return roomName.getText();
    }
}

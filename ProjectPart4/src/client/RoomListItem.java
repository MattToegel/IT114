package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RoomListItem extends JPanel implements AutoCloseable {
    /**
     * 
     */
    private static final long serialVersionUID = -8340982098822138336L;
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
	joinButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		// invokes the callback
		onJoin.accept(roomName.getText());
	    }
	});
	this.add(roomName);
	this.add(joinButton);
    }

    @Override
    public void close() {
	for (ActionListener al : joinButton.getActionListeners()) {
	    joinButton.removeActionListener(al);
	}
	this.removeAll();
    }

    public String getRoomName() {
	return roomName.getText();
    }
}
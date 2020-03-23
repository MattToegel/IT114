

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class SimpleChatUI {
	public static void main(String[] args) {
		//create frame
		JFrame frame = new JFrame("Simple Chat Mockup");
		frame.setLayout(new BorderLayout());
		//create panel
		JPanel simpleChat = new JPanel();
		simpleChat.setPreferredSize(new Dimension(400,400));
		simpleChat.setLayout(new BorderLayout());
		//create text area for messages
		JTextArea textArea = new JTextArea();
		//don't let the user edit this directly
		textArea.setEditable(false);
		textArea.setText("");
		//create panel to hold multiple controls
		JPanel chatArea = new JPanel();
		chatArea.setLayout(new BorderLayout());
		//add text area to chat area
		chatArea.add(textArea, BorderLayout.CENTER);
		chatArea.setBorder(BorderFactory.createLineBorder(Color.black));
		//add chat area to panel
		simpleChat.add(chatArea, BorderLayout.CENTER);
		//create panel to hold multiple controls
		JPanel userInput = new JPanel();
		//setup textfield
		JTextField textField = new JTextField();
		textField.setPreferredSize(new Dimension(100,30));
		//setup submit button
		JButton b = new JButton();
		b.setPreferredSize(new Dimension(100,30));
		b.setText("Send");
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String message = textField.getText();
				if(message.length() > 0) {
					//append a newline and the text from the textfield
					//to that textarea (simulate simple chatroom)
					textArea.append("\n" + textField.getText());
					textField.setText("");
				}
			}
			
		});
		//add textfield and button to panel
		userInput.add(textField);
		userInput.add(b);
		//add panel to simpleChat panel
		simpleChat.add(userInput, BorderLayout.SOUTH);
		//add simpleChat panel to frame
		frame.add(simpleChat, BorderLayout.CENTER);
		
		frame.pack();
		frame.setVisible(true);
	}
}

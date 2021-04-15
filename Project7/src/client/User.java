package client;

import java.awt.BorderLayout;


import javax.swing.JEditorPane;
import javax.swing.JPanel;

public class User extends JPanel {
	private String name;
	private JEditorPane nameField;

	public User(String name, String styledName) {
		this.name = name;
	
		nameField = new JEditorPane();
		nameField.setContentType("text/html");
		nameField.setText(styledName);
		
		nameField.setEditable(false);
		this.setLayout(new BorderLayout());
		this.add(nameField);
	}

	public String getName() {
		return name;
	}
}
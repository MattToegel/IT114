package client;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class User extends JPanel {
    private String name;
    private JEditorPane nameField;
    private JTextField scoreField;

    public User(String name, int score, String wrapper) {
	this.name = name;
	nameField = new JEditorPane();
	nameField.setContentType("text/html");
	nameField.setText(String.format(wrapper, name));
	nameField.setEditable(false);
	scoreField = new JTextField();
	scoreField.setText("" + score);
	scoreField.setEditable(false);
	scoreField.setPreferredSize(new Dimension(30, this.getSize().height));
	this.setLayout(new BorderLayout());
	this.add(nameField, BorderLayout.CENTER);
	this.add(scoreField, BorderLayout.EAST);
    }

    public String getName() {
	return name;
    }
}
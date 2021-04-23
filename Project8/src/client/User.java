package client;

import java.awt.BorderLayout;


import javax.swing.JEditorPane;
import javax.swing.JPanel;

public class User extends JPanel {
	private String name;
	private JEditorPane nameField;
	private static String bg = "<font style='background-color: #ff0000'>%s</font>";
	private boolean isMyTurn = false;
	private String nameStyle = "<font>%s</font>";
	private String shipsStyle = ": <font>%s</font>";
	private int ships = 0;
	
	public User(String name, String styledName) {
		this.name = name;
	
		nameField = new JEditorPane();
		nameField.setContentType("text/html");
		nameField.setText(styledName);
		
		nameField.setEditable(false);
		this.setLayout(new BorderLayout());
		this.add(nameField);
	}
	public void isMyTurn(boolean isMyTurn) {
		this.isMyTurn = isMyTurn;
		updateDisplay();
	}
	public void setShips(int ships) {
		this.ships = ships;
		updateDisplay();
	}
	@Deprecated
	public void updateNameDisplay(String styledName) {
		nameField.setText(styledName);
	}
	public String getName() {
		return name;
	}
	private void updateDisplay() {
		// <bg>Name - Ships  </bg>
		String display = "";
		if(isMyTurn) {
			display = String.format(bg, String.format(nameStyle, name) + String.format(shipsStyle, ships));
		}
		else {
			display = String.format(nameStyle, name) + String.format(shipsStyle, ships);
		}
		nameField.setText(display);
	}
}
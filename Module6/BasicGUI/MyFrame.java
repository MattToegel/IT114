package Module6.BasicGUI;

import javax.swing.JFrame;
import javax.swing.JTextField;

public class MyFrame extends JFrame {
	//constructor based design
	public MyFrame(String myTitle) {
		this.setTitle(myTitle);
		JTextField textField = new JTextField("Hello World");
		textField.setSize(100, 100);//define the size in pixels
		add(textField);//add the JTextField to the hierarchy and make it visible to the user
		setLayout(null);//no layout manager for now
		setSize(200, 200);//define the size in pixels
		setVisible(true);//required to show the window; otherwise program terminates
	}

	public static void main(String[] args) {
		new MyFrame("My First Frame");
		//Note: Don't forget to ctrl+c to terminate or stop the app
		//the x (close) button is not configured by default
	}
}
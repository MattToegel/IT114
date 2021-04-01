
import javax.swing.JFrame;
import javax.swing.JTextField;

public class MyFrame extends JFrame {
	public MyFrame(String myTitle) {
		this.setTitle(myTitle);
		JTextField textField = new JTextField("Hello World");
		textField.setSize(100, 100);
		add(textField);
		setLayout(null);
		setSize(200, 200);
		setVisible(true);
	}

	public static void main(String[] args) {
		MyFrame frame = new MyFrame("My First Frame");
	}
}
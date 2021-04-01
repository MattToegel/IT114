
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PracticalUI extends JFrame {
	CardLayout card;
	PracticalUI self;

	public PracticalUI(String title) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(400, 400));
		setLocationRelativeTo(null);
		self = this;
		setTitle(title);
		card = new CardLayout();
		setLayout(card);
		createConnectionScreen();
		createUserInputScreen();
		showUI();
	}

	void createConnectionScreen() {
		JPanel panel = new JPanel();
		// yes it's a bit weird setting layout and passing in the reference as to the
		// LayoutManager.
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel hostLabel = new JLabel("Host:");
		JTextField host = new JTextField("127.0.0.1");
		panel.add(hostLabel);
		panel.add(host);
		JLabel portLabel = new JLabel("Port:");
		JTextField port = new JTextField("3000");
		panel.add(portLabel);
		panel.add(port);
		JButton button = new JButton("Next");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO trigger connection; let a callback change our page
				self.next();
			}

		});
		panel.add(button);
		this.add(panel);
	}

	void createUserInputScreen() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel userLabel = new JLabel("Username:");
		JTextField username = new JTextField();
		panel.add(userLabel);
		panel.add(username);
		JButton button = new JButton("Previous");// TODO rename to something like Start or Login
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO trigger username payload
				self.previous();
			}

		});
		panel.add(button);
		this.add(panel);
	}

	void next() {
		card.next(this.getContentPane());
	}

	void previous() {
		card.previous(this.getContentPane());
	}

	void showUI() {
		pack();
		setVisible(true);
	}

	public static void main(String[] args) {
		PracticalUI ui = new PracticalUI("My UI");
	}
}
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class UISample extends JFrame implements OnReceiveMessage{
	static SocketClient client;
	static JButton toggle;
	static JButton clickit;
	public UISample() {
		super("Callable SocketClient");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// add a window listener
		this.addWindowListener(new WindowAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				// before we stop the JVM stop the example
				//client.isRunning = false;
				super.windowClosing(e);
			}
		});
	}
	public static boolean toggleButton(boolean isOn) {
		String t = UISample.toggle.getText();
		if(isOn) {
			UISample.toggle.setText("ON");
			UISample.toggle.setBackground(Color.GREEN);
			UISample.toggle.setForeground(Color.GREEN);
			clickit.setText("Click to Turn Off");
			return true;
		}
		else {
			UISample.toggle.setText("OFF");
			UISample.toggle.setBackground(Color.RED);
			UISample.toggle.setForeground(Color.RED);
			clickit.setText("Click to Turn On");
			return false;
		}
	}
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException ex) {
		} catch (InstantiationException ex) {
		} catch (IllegalAccessException ex) {
		} catch (UnsupportedLookAndFeelException ex) {
		}
		UISample window = new UISample();
		window.setLayout(new BorderLayout());
		JPanel connectionDetails = new JPanel();
		JTextField host = new JTextField();
		host.setText("127.0.0.1");
		JTextField port = new JTextField();
		port.setText("3001");
		JButton connect = new JButton();
		
		connect.setText("Connect");
		connectionDetails.add(host);
		connectionDetails.add(port);
		connectionDetails.add(connect);
		window.add(connectionDetails, BorderLayout.NORTH);
		JPanel area = new JPanel();
		area.setLayout(new BorderLayout());
		window.add(area, BorderLayout.CENTER);
		JButton toggle = new JButton();/* {
			@Override
			public void paintComponent(Graphics g) {
				//TODO fix
				super.paintComponent(g);
				System.out.println("Painting");
				g.setColor(Color.black);
			    g.fillRect(0, 0, 400, 200);
			}
		};*/
		toggle.setText("OFF");
		//Cache it statically (not great but it's a sample)
		UISample.toggle = toggle;
		BoxIcon icon = new BoxIcon(Color.GREEN,400,200, 2);
		icon.setText("This is a test");
		JButton click = new JButton("Click to Turn On", 
				icon);
		icon.setParent(click);
		clickit = click;
		click.setPreferredSize(new Dimension(400,200));
		click.setText("Click to Turn On");
		click.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	String t = toggle.getText();
		    	//boolean isOn = UISample.toggleButton();
		    	boolean turnOn = toggle.getText().contains("OFF");
		    	//TODO send to server
		    	client.doClick(turnOn);
		    }
		});
		click.setEnabled(false);
		
		connect.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	client = new SocketClient();
		    	int _port = -1;
		    	try {
		    		_port = Integer.parseInt(port.getText());
		    	}
		    	catch(Exception num) {
		    		System.out.println("Port not a number");
		    	}
		    	if(_port > -1) {
			    	client = SocketClient.connect(host.getText(), _port);
			    	
			    	//METHOD 1 Using the interface
			    	client.registerListener(window);
			    	//METHOD 2 Lamba Expression (unnamed function to handle callback)
			    	/*client.registerListener(()->{	
			    		if(UISample.toggle != null) {
			    			UISample.toggle.setText("OFF");
			    			UISample.toggle.setBackground(Color.RED);
			    		}
			    	});*/
			    	
			    	
			    	//trigger any one-time data after client connects
			    	client.postConnectionData();
			    	connect.setEnabled(false);
			    	click.setEnabled(true);
		    	}
		    }
		});
		
		area.add(toggle, BorderLayout.CENTER);
		area.add(click, BorderLayout.SOUTH);
		
		window.setPreferredSize(new Dimension(400,600));
		window.pack();
		window.setVisible(true);
	}
	@Override
	public void onReceived(boolean isOn) {
		// TODO Auto-generated method stub
		if(UISample.toggle != null) {
			UISample.toggleButton(isOn);
		}
	}
}

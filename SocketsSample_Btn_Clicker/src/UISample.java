import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;

public class UISample extends JFrame implements OnReceive{
	static SocketClient client;
	static JButton toggle;
	static JButton clickit;
	static JTextArea history;
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
			clickit.setText("Click to Turn Off");
			return true;
		}
		else {
			UISample.toggle.setText("OFF");
			UISample.toggle.setBackground(Color.RED);
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
		JButton toggle = new JButton(){
			//TODO showing how we can override the painting of a component
			//for "custom" ui
			@Override
			public void paintComponent(Graphics g) {
				//TODO fix
				super.paintComponent(g);
				//use the assigned background color
				g.setColor(this.getBackground());
				//grab the current dimensions
				Dimension d = this.getSize();
			    g.fillRect(0, 0, d.width, d.height);
			    g.setColor(Color.black);
			    //draw the current text
			    //Note: the other text still draws but we paint on top of it
			    //we should look into this and see if it can be more easily repurposed
			    g.drawString(this.getText(), d.width/2,d.height/2);
			}
		};
		toggle.setBackground(Color.red);
		toggle.setText("OFF");
		//Cache it statically (not great but it's a sample)
		UISample.toggle = toggle;
		JButton click = new JButton("Click to Turn On");
		//icon.setParent(click);
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
			    	client.registerSwitchListener(window);
			    	//METHOD 2 Lamba Expression (unnamed function to handle callback)
			    	/*client.registerListener(()->{	
			    		if(UISample.toggle != null) {
			    			UISample.toggle.setText("OFF");
			    			UISample.toggle.setBackground(Color.RED);
			    		}
			    	});*/
			    	
			    	
			    	//trigger any one-time data after client connects
			    	
			    	//register our history/message listener
			    	client.registerMessageListener(window);
			    	client.postConnectionData();
			    	connect.setEnabled(false);
			    	click.setEnabled(true);
		    	}
		    }
		});
		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		JTextArea ta = new JTextArea();
		ta.setEditable(false);
		history = ta;
		history.setWrapStyleWord(true);
		history.setAutoscrolls(true);
		history.setLineWrap(true);
		JScrollPane scroll = new JScrollPane(history);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		DefaultCaret caret = (DefaultCaret)history.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		container.add(scroll, BorderLayout.CENTER);
		
		JPanel spacer = new JPanel();
		Dimension panelSize = new Dimension(125, 200);
		container.setPreferredSize(panelSize);
		spacer.setPreferredSize(panelSize);
		area.add(toggle, BorderLayout.CENTER);
		area.add(click, BorderLayout.SOUTH);
		area.add(container, BorderLayout.WEST);
		area.add(spacer, BorderLayout.EAST);
		
		
		window.setPreferredSize(new Dimension(400,600));
		window.pack();
		window.setVisible(true);
	}
	@Override
	public void onReceivedSwitch(boolean isOn) {
		if(UISample.toggle != null) {
			UISample.toggleButton(isOn);
		}
	}
	@Override
	public void onReceivedMessage(String msg) {
		if(history != null) {
			history.append(msg);
			history.append(System.lineSeparator());
		}
	}
}

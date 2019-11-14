import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class MyUI {
	public static boolean isRunning = true;
	public static void main(String[] args) {
		//create frame
		JFrame frame = new JFrame("Rock-Paper-Scissors");
		frame.setLayout(new BorderLayout());
		//create panel
		JPanel rps = new JPanel();
		rps.setPreferredSize(new Dimension(400,400));
		rps.setLayout(new BorderLayout());
		//create text area for messages
		JTextArea textArea = new JTextArea();
		//don't let the user edit this directly
		textArea.setEditable(false);
		textArea.setText("");
		//create panel to hold multiple controls
		JPanel attemptsArea = new JPanel();
		attemptsArea.setLayout(new BorderLayout());
		//add text area to history/attempts
		attemptsArea.add(textArea, BorderLayout.CENTER);
		attemptsArea.setBorder(BorderFactory.createLineBorder(Color.black));
		//add history/attempts to panel
		rps.add(attemptsArea, BorderLayout.CENTER);
		//create panel to hold multiple controls
		JPanel userInput = new JPanel();
		//Interaction will be our instance to interact with
		//socket client
		Interaction interaction = new Interaction();
		//create rock button
		JButton rock = new JButton();
		rock.setText("Rock");
		rock.setPreferredSize(new Dimension(100,30));
		rock.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				interaction.sendChoice("rock");
			}
		});
		//create paper button
		JButton paper = new JButton();
		paper.setText("Paper");
		paper.setPreferredSize(new Dimension(100,30));
		paper.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				interaction.sendChoice("paper");
			}
		});
		//create scissors button
		JButton scissors = new JButton();
		scissors.setText("Scissors");
		scissors.setPreferredSize(new Dimension(100,30));
		scissors.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				interaction.sendChoice("scissors");
			}
		});
		
		userInput.add(rock);
		userInput.add(paper);
		userInput.add(scissors);
		//add panel to rps panel
		rps.add(userInput, BorderLayout.SOUTH);
		//add rps panel to frame
		frame.add(rps, BorderLayout.CENTER);
		
		frame.pack();
		frame.setVisible(true);
		Thread test = new Thread() {
			@Override
			public void run() {
				String m = interaction.getMessage();
				if(m != null) {
				textArea.append(m);
				}
				while(isRunning) {
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		};
		test.start();
	}
	
}
class Interaction {
	SampleSocketClient client;
	public Interaction() {
		client = new SampleSocketClient();
		client.connect("127.0.0.1", 3000);
		try {
			client.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void sendChoice(String choice) {
		client.sendChoice(choice);
	}
	public String getMessage() {
		return client.messages.poll();
	}
}
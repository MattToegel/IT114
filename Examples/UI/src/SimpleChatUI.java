package com.examples.drawing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class SimpleChat {
	public static void main(String[] args) {
		
		JFrame frame = new JFrame("Havin' Fun?");
		frame.setLayout(new BorderLayout());
		//start simple chat
		JPanel simpleChat = new JPanel();
		simpleChat.setPreferredSize(new Dimension(400,400));
		simpleChat.setLayout(new BorderLayout());
		JTextArea ta = new JTextArea();
		ta.setText("");
		JPanel chatArea = new JPanel();
		chatArea.setLayout(new BorderLayout());
		chatArea.add(ta, BorderLayout.CENTER);
		chatArea.setBorder(BorderFactory.createLineBorder(Color.black));
		simpleChat.add(chatArea, BorderLayout.CENTER);
		
		JPanel userInput = new JPanel();
		JTextField tf = new JTextField();
		tf.setPreferredSize(new Dimension(100,30));
		JButton b = new JButton();
		b.setPreferredSize(new Dimension(100,30));
		b.setText("Send");
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String messages = ta.getText();
				messages += "\n" + tf.getText();
				ta.setText(messages);
				tf.setText("");
			}
			
		});
		userInput.add(tf);
		userInput.add(b);
		simpleChat.add(userInput, BorderLayout.SOUTH);
		//end simple chat
		frame.add(simpleChat, BorderLayout.CENTER);
		
		frame.pack();
		frame.setVisible(true);
	}
}

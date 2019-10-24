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

public class BasicGrid {
	public static Point testPoint = new Point(0,0);
	public static HashMap<Point, JButton> lazyGrid;
	public static void main(String[] args) {
		
		JFrame frame = new JFrame("Havin' Fun?");
		frame.setLayout(new BorderLayout());
		
		//start grid
		JPanel grid = new JPanel();
		int rows = 16;
		int cols = 16;
		grid.setLayout(new GridLayout(rows, cols));
		grid.setSize(new Dimension(400,400));
		JTextField t = new JTextField();
		//grid layout creation (full layout control)
		/*for(int i = 0; i < (rows*cols); i++) {
			JButton button = new JButton();
			button.setBackground(Color.white);
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					t.setText(((JButton)e.getSource()).getText());
				}
				
			});
			button.setText(""+i);
			grid.add(button);
		}*/
		//Manual grid creation (To Be Updated for true manual override)
		//(sample doesn't work here, but would work with free placement/painting)
		lazyGrid = new HashMap<Point,JButton>();
		Random random = new Random();
		for(int x = 0; x < cols; x++) {
			for(int y = 0; y < rows; y++) {
				JButton bt = new JButton();
				bt.setText(x+","+y);
				bt.setLocation(x, y);
				Point p = new Point(x, y);
				lazyGrid.put(p, bt);
				if(p == testPoint) {
					bt.setBackground(Color.red);
				}
				else {
					bt.setBackground(Color.white);
				}
				//uncomment if you want random colors per button
				//bt.setBackground(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat()));
				
				
				bt.setSize(new Dimension(10,10));
				bt.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						//set the textfield value to the text value of the button to show clicked coordinate
						t.setText(((JButton)e.getSource()).getText());
						//set clicked button to red
						((JButton)e.getSource()).setBackground(Color.red);
					}
					
				});
				grid.add(bt);
			}
		}
		
		frame.add(grid, BorderLayout.CENTER);
		frame.add(t, BorderLayout.SOUTH);
		//end grid
		frame.pack();
		frame.setVisible(true);
		setKeyBindings(grid.getInputMap(), grid.getActionMap());
	}
	//keybindings sample to show grid movement
	public static void setKeyBindings(InputMap im, ActionMap am) {
		
		//bind key actions to action map
		im.put(KeyStroke.getKeyStroke("pressed UP"), "UAD");
		im.put(KeyStroke.getKeyStroke("pressed DOWN"), "DAD");
		im.put(KeyStroke.getKeyStroke("pressed LEFT"), "LAD");
		im.put(KeyStroke.getKeyStroke("pressed RIGHT"), "RAD");
		
		im.put(KeyStroke.getKeyStroke("released UP"), "UAU");
		im.put(KeyStroke.getKeyStroke("released DOWN"), "DAU");
		im.put(KeyStroke.getKeyStroke("released LEFT"), "LAU");
		im.put(KeyStroke.getKeyStroke("released RIGHT"), "RAU");
		
		im.put(KeyStroke.getKeyStroke("pressed SPACE"), "SPACE");
		
		//bind Action to Action map
		am.put("UAD", new MoveAction(true, 0, -1));
		am.put("DAD", new MoveAction(true, 0, 1));
		am.put("LAD", new MoveAction(true, -1,0));
		am.put("RAD", new MoveAction(true, 1, 0));
		
		am.put("UAU", new MoveAction(false, 0, -1));
		am.put("DAU", new MoveAction(false, 0, 1));
		am.put("LAU", new MoveAction(false, -1,0));
		am.put("RAU", new MoveAction(false, 1, 0));
	}
}
//Move action to alter per your needs
class MoveAction extends AbstractAction{
	private static final long serialVersionUID = 5137817329873449021L;
	int x,y;
	boolean pressed = false;
	MoveAction(boolean pressed, int x, int y){
		this.x = x;
		this.y = y;
		this.pressed = pressed;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		//reset all buttons to white background
		SomeUI.lazyGrid.forEach((point, button)->{
			button.setBackground(Color.white);
		});
		System.out.println("Moved");
		//x, y might be swapped in UI vs the paint example this was fulled from
		if(y != 0)
			SomeUI.testPoint.x += y;
		if(x != 0)
			SomeUI.testPoint.y += x;
		//check if point exists in our grid mapping, if so update the position's color
		if(SomeUI.lazyGrid.containsKey(SomeUI.testPoint)) {
			SomeUI.lazyGrid.get(SomeUI.testPoint).setBackground(Color.red);
		}
		System.out.println(SomeUI.testPoint);
	}
}

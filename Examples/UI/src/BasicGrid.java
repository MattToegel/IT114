

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
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class BasicGrid {
	public static Point testPoint = new Point(0,0);
	public static HashMap<Point, JButton> lazyGrid;
	public static void main(String[] args) {
		
		JFrame frame = new JFrame("Dual Grid Comparison Sample");
		frame.setLayout(new BorderLayout());
		frame.setSize(new Dimension(600,600));
		frame.setMinimumSize(new Dimension(600,600));
		//empty panel we'll use as a spacer for now
		JPanel empty = new JPanel();
		
		int rows = 3;
		int cols = 3;
		Dimension gridDimensions = new Dimension(400,400);
		//Create two sample grids to compare adding elements
		JPanel grid1 = new JPanel();
		//set gridlayout pass in rows and cols
		grid1.setLayout(new GridLayout(rows,cols));
		grid1.setSize(gridDimensions);
		
		JPanel grid2 = new JPanel();
		//set gridlayout pass in rows and cols
		grid2.setLayout(new GridLayout(rows, cols));
		grid2.setSize(gridDimensions);
		JTextField textField = new JTextField();
		//grid layout creation (full layout control)
		for(int i = 0; i < (rows*cols); i++) {
			JButton button = new JButton();
			//convert to x coordinate
			int x = i % rows;
			//convert to y coordinate
			int y = i/cols;
			//%1 first param, %2 second param, etc
			String buttonText = String.format("Index: %1$s Coord: (%2$s, %3$s)", i, x, y);
			//show index and coordinate details on button
			button.setText(buttonText);
			
			button.setBackground(Color.white);
			//create an action to perform when button is clicked
			//override the default actionPerformed method to tell the code how to handle it
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					textField.setText(((JButton)e.getSource()).getText());
					
					//give focus back to grid2 for navigation sample
					grid2.grabFocus();
				}
				
			});
			grid1.add(button);
		}
		//can omit if not doing navigation sample
		lazyGrid = new HashMap<Point,JButton>();
		//keep if using Random, otherwise can omit
		Random random = new Random();
		int i = 0;
		Dimension buttonSize = new Dimension(10,10);
		for(int y = 0; y < rows; y++) {
			for(int x = 0; x < cols; x++) {
				JButton bt = new JButton();
				//%1 first param, %2 second param, etc
				String buttonText = String.format("Index: %1$s Coord: (%2$s, %3$s)", i, x, y);
				bt.setText(buttonText);
				bt.setLocation(x, y);
				//point to button map for easy button reference in navigation sample
				//can omit these related lines if it's not relevant to you
					Point p = new Point(x, y);
					lazyGrid.put(p, bt);
					//set background color based on this point matching our testPoint
					bt.setBackground((p == testPoint)?Color.red:Color.white);
					//uncomment if you want random colors per button
					//bt.setBackground(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat()));
				//end potential omit section
				bt.setSize(buttonSize);
				//create an action to perform when button is clicked
				//override the default actionPerformed method to tell the code how to handle it
				bt.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						//set the textfield value to the text value of the button to show clicked coordinate
						textField.setText(((JButton)e.getSource()).getText());
						//set clicked button to red
						((JButton)e.getSource()).setBackground(Color.red);
						
						//give focus back to grid2 for navigation sample
						grid2.grabFocus();
					}
					
				});
				//add the button to grid2
				grid2.add(bt);
				//increment our index to demo the order the buttons are added
				i++;
			}
		}
		//add grid1 sample to left
		frame.add(grid1, BorderLayout.WEST);
		//add empty space to prevent the grids from visually merging initially
		frame.add(empty, BorderLayout.CENTER);
		//add grid2 sample to right
		frame.add(grid2, BorderLayout.EAST);
		//add output field to bottom
		frame.add(textField, BorderLayout.SOUTH);
		//resize based on elements applied to layout
		frame.pack();
		frame.setVisible(true);
		
		//set keybindings for navigation sample, may omit this and related method/function
		//if not useful
		setKeyBindings(grid2.getInputMap(), grid2.getActionMap());
		grid2.setFocusable(true);
		grid2.grabFocus();
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
		//technically we don't need this, we're just listening for keydown
		//but include for a complete example
		am.put("UAU", new MoveAction(false, 0, -1));
		am.put("DAU", new MoveAction(false, 0, 1));
		am.put("LAU", new MoveAction(false, -1,0));
		am.put("RAU", new MoveAction(false, 1, 0));
	}
}
//Create a move action we can trigger on key press
class MoveAction extends AbstractAction{
	private static final long serialVersionUID = 5137817329873449021L;
	//passed in direction we want to move
	int x,y;
	boolean pressed = false;
	MoveAction(boolean pressed, int x, int y){
		this.x = x;
		this.y = y;
		this.pressed = pressed;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(!pressed) {
			//in this example we only care about pressed = true
			//so we return here if it's false (when the key is up)
			return;
		}
		//reset all buttons to white background
		BasicGrid.lazyGrid.forEach((point, button)->{
			button.setBackground(Color.white);
		});
		
		//This line passes reference to testPoint, so it doesn't revert correctly
		//when it moves outside of the grid
		//uncomment the below line and comment out line 175 to see
		//Point previous = BasicGrid.testPoint;
		//Point next = previous;
		
		//This creates a new point so we don't affect the original until we want to
		Point previous = new Point(BasicGrid.testPoint.x, BasicGrid.testPoint.y);
		Point next = new Point(previous.x, previous.y);
		if(x != 0) {
			next.x += x;
		}
		if(y != 0) {
			next.y += y;
		}
		System.out.println("Next Coord: " + next);
		//check if point exists in our grid mapping, if so update the position's color
		if(BasicGrid.lazyGrid.containsKey(next)) {
			BasicGrid.lazyGrid.get(next).setBackground(Color.red);
			BasicGrid.testPoint = next;
		}
		else {
			//reset color for previous point
			BasicGrid.lazyGrid.get(previous).setBackground(Color.red);
		}
		System.out.println("TestPoint Coord: " + BasicGrid.testPoint);
	}
}

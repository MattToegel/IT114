import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class LayoutsAndPosition {
	public static void main(String[] args) {
		JFrame f = new JFrame("Test");
		f.setLayout(new BorderLayout());
		
		//create main panel to hold our elements
		JPanel main = new JPanel();
		main.setLayout(new BorderLayout());//new FlowLayout());
		JTextField textField = new JTextField("Hello");
		textField.setPreferredSize(new Dimension(100,30));
		
		//create slider panel to hold our label and slider
		JPanel sliderPanel = new JPanel();
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 3, 0);
		JLabel sliderLabel = new JLabel("Frames Per Second", JLabel.CENTER);
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
		sliderPanel.add(sliderLabel);
		sliderPanel.add(slider);
		
		//Add items to the flow panel
		main.add(textField, BorderLayout.WEST);
		main.add(sliderPanel, BorderLayout.EAST);
		
		/* Above is vaguely similar to the below html
		 <div id="main">
		 	<div id="textField"><input type="text"/></div>
			<div id="sliderPanel">
				<label>Slider</label>
				<Slider></slider>
			<div>
		</div>
		*/
		
		
		
		JPanel area = new JPanel();
		area.setLayout(new BorderLayout());
		JTextArea textArea = new JTextArea();
		area.add(textArea, BorderLayout.CENTER);
		JButton testWestAnchor = new JButton();
		testWestAnchor.setLayout(new FlowLayout());
		JButton testEastAnchor = new JButton();
		testEastAnchor.setLayout(new FlowLayout());
		JButton testNorthAnchor = new JButton();
		testNorthAnchor.setLayout(new FlowLayout());
		
		//add elements to the frame
		f.add(testWestAnchor, BorderLayout.WEST);
		f.add(testEastAnchor, BorderLayout.EAST);
		f.add(testNorthAnchor, BorderLayout.NORTH);
		f.add(area, BorderLayout.CENTER);
		f.add(main, BorderLayout.SOUTH);
		
		//don't forget to set size
		f.setPreferredSize(new Dimension(400,400));
		//pack to apply the layout rules
		f.pack();
		f.setVisible(true);
	}
}

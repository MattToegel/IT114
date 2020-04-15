import java.awt.event.*; 
import java.awt.*; 
import javax.swing.*;    
public class BasicRPS {  
    public static void main(String[] args) {  
        JFrame f = new JFrame("RPS Starter");  
        f.setLayout(new BorderLayout());
        JButton sub = new JButton("Submit");
        sub.setPreferredSize(new Dimension(100,70));
        sub.setMaximumSize(new Dimension(100,70));
        JRadioButton checkBox1 = new JRadioButton("Rock");
        JRadioButton checkBox2 = new JRadioButton("Paper");
        JRadioButton checkBox3 = new JRadioButton("Rock");
        JPanel options = new JPanel();
        options.setLayout(new FlowLayout());
        options.add(checkBox1);
        options.add(checkBox2);
        options.add(checkBox3);
        f.add(sub, BorderLayout.SOUTH);
        f.add(options, BorderLayout.CENTER);
        f.setPreferredSize(new Dimension(400,400));  
        f.pack();
        f.setVisible(true);
    }
}
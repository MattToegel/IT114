package Module8.BasicGUI;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class MyFrame extends JFrame {
    
    // Constructor based design
    public MyFrame(String myTitle) {
        this.setTitle(myTitle);
        initializeComponents();
        this.setSize(200, 200); // Define the size in pixels
        this.setVisible(true); // Required to show the window; otherwise, the program terminates
    }

    // Method to initialize GUI components
    private void initializeComponents() {
        JTextField textField = new JTextField("Hello World");
        textField.setBounds(50, 50, 100, 30); // Set position and size
        this.setLayout(null); // No layout manager for now
        this.add(textField); // Add the JTextField to the hierarchy
    }

    // Main method to launch the application
    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new MyFrame("My First Frame"));
    }
}


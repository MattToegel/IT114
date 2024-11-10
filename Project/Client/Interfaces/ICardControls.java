package Project.Client.Interfaces;

import javax.swing.JPanel;

public interface ICardControls {

    /**
     * Used to trigger CardLayout next()
     */
    void next();

    /**
     * Used to trigger CardLayout previous()
     */
    void previous();

    /**
     * Used to trigger CardLayout show()
     * 
     * @param cardName The specific card name to show
     */
    void show(String cardName);

    /**
     * Used to have child panels register themselves with ClientUI
     * 
     * @param name
     * @param panel
     */
    void addPanel(String name, JPanel panel);

    /**
     * Used to invoke ClientUI's connect logic (once host, port, and username are
     * set)
     */
    void connect();
}
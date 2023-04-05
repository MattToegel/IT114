package HNS.client;

import javax.swing.JPanel;

public interface ICardControls {
    void next();

    void previous();

    void show(String cardName);

    void addPanel(String name, JPanel panel);

    void connect();
}

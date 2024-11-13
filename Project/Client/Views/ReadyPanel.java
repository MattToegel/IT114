package Project.Client.Views;

import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;

import Project.Client.Client;

public class ReadyPanel extends JPanel {
    public ReadyPanel() {
        JButton readyButton = new JButton();
        readyButton.setText("Ready");
        readyButton.addActionListener(l -> {
            try {
                Client.INSTANCE.sendReady();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        this.add(readyButton);
    }
}
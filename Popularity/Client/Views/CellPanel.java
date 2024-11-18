package Popularity.Client.Views;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Popularity.Client.Client;

public class CellPanel extends JPanel {
    private JLabel label;
    private int x, y;
    private JPanel self;
    public CellPanel(int _x, int _y) {
        this.x = _x;
        this.y = _y;
        label = new JLabel("0");
        label.setLabelFor(this);
        this.add(label);
        self=this;
        setCount(0);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                
                try {
                    Client.INSTANCE.sendTurnAction(x,y);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    public void highlight(){
        self.setBackground(Color.BLUE);
        repaint();
    }
    public void unhighlight(){
        self.setBackground(Color.GRAY);
        repaint();
    }

    public void setCount(int count) {
        label.setText(count + "");
        self.setBackground(count > 0 ? Color.WHITE : Color.GRAY);
    }
}

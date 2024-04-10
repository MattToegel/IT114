package Project.Client.Views;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Project.Common.CellType;

public class CellPanel extends JPanel {
    private static final List<CellPanel> allCells = new ArrayList<>();
    private boolean selected = false;
    private int x, y;
    private JTextField jtf;

    public CellPanel() {
        allCells.add(this); // Add this instance to the list of all cells
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selected = !selected;
                // setBackground(selected ? Color.GREEN : Color.WHITE);
                updateAllBorders(); // Update the borders of all cells
                setBorder(BorderFactory.createLineBorder(selected ? Color.CYAN : Color.BLACK));
            }
        });
        jtf = new JTextField("");
        jtf.setEditable(false);
        this.add(jtf);
    }

    // Method to update the borders of all cells
    private void updateAllBorders() {
        for (CellPanel cell : allCells) {
            if (cell != this) { // Don't update the border of the cell that was clicked
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cell.selected = false;
            }
        }
    }

    public void setOccupiedCount(int c) {
        jtf.setVisible(c > 0);
        jtf.setText(c + "");
        jtf.repaint();
    }

    public void setType(CellType type, int x, int y) {
        this.x = x;
        this.y = y;
        switch (type) {
            case START:
                setBackground(Color.BLUE);
                break;
            case DRAGON:
                setBackground(Color.ORANGE);
                break;
            case WALKABLE:
                setBackground(Color.WHITE);
                break;
            case UNWALKABLE:
                setBackground(new Color(102, 51, 0));
                break;
            default:
                setBackground(Color.BLACK);
                break;

        }
    }

    public static void reset() {
        allCells.clear();
    }
}

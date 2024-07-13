package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Project.Common.LoggerUtil;
import Project.Common.Tower;

public class CellPanel extends JPanel {
    private static final List<CellPanel> allCells = new ArrayList<>();
    private boolean selected = false;
    private int cx, cy;
    private JLabel label;
    private Tower tower;

    public CellPanel(int x, int y, Consumer<CellPanel> selectCallback) {
        this.cx = x;
        this.cy = y;
        allCells.add(this); // Add this instance to the list of all cells
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new BorderLayout());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LoggerUtil.INSTANCE.info(String.format("Clicked CellPanel %s,%s", getCellX(), getCellY()));
                setSelected(!selected);
                if (selectCallback != null) {
                    selectCallback.accept(CellPanel.this);
                }
            }
        });

        label = new JLabel("");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        add(label, BorderLayout.CENTER);
    }

    public void setSelected(boolean isSelected) {
        selected = isSelected;
        updateAllBorders(); // Update the borders of all cells
        setBorder(BorderFactory.createLineBorder(selected ? Color.CYAN : Color.BLACK));
    }

    /**
     * Uses to get the x coordinate (note: getX() is a built in method for screen
     * info)
     * 
     * @return
     */
    public int getCellX() {
        return cx;
    }

    /**
     * Uses to get the y coordinate (note: getY() is a built in method for screen
     * info)
     * 
     * @return
     */
    public int getCellY() {
        return cy;
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

    public static void reset() {
        allCells.forEach(cell -> cell.cleanup());
        allCells.clear();
    }

    public Tower getTower() {
        return tower;
    }

    public void setTower(Tower tower) {
        this.tower = tower;
        if (tower != null) {
            String text = String.format(
                    "<html>Id: %d<br>Health: %s/5<br>Energy: %s<br>Allocated: %s<br>Attacked: %s</html>",
                    tower.getId(), tower.getHealth(), tower.getAllocatedEnergy(), tower.didAllocate() ? "Y" : "N",
                    tower.didAttack() ? "Y" : "N");
            label.setText(text);
        } else {
            label.setText("");
        }
    }

    public void cleanup() {
        // Cleanup resources and dereference the callback
        setTower(null);
    }
}

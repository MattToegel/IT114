package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Project.Client.Client;
import Project.Common.Cell.Terrain;
import Project.Common.LoggerUtil;
import Project.Common.Tower;

public class CellPanel extends JPanel {
    private static final List<CellPanel> allCells = new ArrayList<>();
    private boolean selected = false;

    private int column, row; // column = x, row = y
    private JLabel label;
    private Tower tower;
    private Color color = Color.WHITE;
    private int cost = 1;
    private Terrain terrain;

    public int getCost() {
        return cost;
    }

    /**
     * Constructs a CellPanel with specified coordinates, cost, terrain, and a callback for selection.
     * 
     * @param x Horizontal (column) coordinate.
     * @param y Vertical (row) coordinate.
     * @param cost Cost associated with this cell.
     * @param terrain Terrain type of the cell.
     * @param selectCallback Callback function to handle selection.
     */
    public CellPanel(int x, int y, int cost, Terrain terrain, Consumer<CellPanel> selectCallback) {
        this.column = x; // Column corresponds to x
        this.row = y;    // Row corresponds to y
        allCells.add(this); // Add this instance to the list of all cells

        this.terrain = terrain;
        switch (terrain) {
            case ATTACK:
                color = new Color(210, 180, 140);
                break;
            case DEFENSE:
                color = new Color(139, 69, 19);
                break;
            case ENERGY:
                color = new Color(0, 100, 0);
                break;
            case HEALING:
                color = new Color(0, 191, 255);
                break;
            case RANGE:
                color = new Color(144, 238, 144);
                break;
            case CARDS:
                color = new Color(169, 169, 169);
                break;
            case NONE:
            default:
                break;
        }
        this.cost = cost;
        setBackground(this.color);
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
        setTower(null);
    }

    public void setSelected(boolean isSelected) {
        selected = isSelected;
        updateAllBorders(); // Update the borders of all cells
        setBorder(BorderFactory.createLineBorder(selected ? Color.CYAN : Color.BLACK));
    }

    /**
     * Returns the column (x) coordinate of the cell.
     * 
     * @return Column coordinate.
     */
    public int getCellX() {
        return column;
    }

    /**
     * Returns the row (y) coordinate of the cell.
     * 
     * @return Row coordinate.
     */
    public int getCellY() {
        return row;
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
                    "<html>Id: %d<br>Health: %s/5<br>Attack: %s<br>Defense: %s<br>Range: %s<br>Energy: %s<br>Allocated: %s<br>Attacked: %s</html>",
                    tower.getId(), tower.getHealth(), tower.getAttack(), tower.getDefense(), tower.getRange(),
                    tower.getAllocatedEnergy(), tower.didAllocate() ? "Y" : "N",
                    tower.didAttack() ? "Y" : "N");
            Font font = label.getFont().deriveFont(8f); // be careful of int vs float, two different methods
            label.setFont(font);
            label.setText(text);
            label.setForeground(tower.getClientId() == Client.INSTANCE.getMyClientId() ? Color.BLUE : Color.RED);
        } else {
            label.setText(String.format("<html>(%s,%s)Cost: %s<br>Terrain: %s</html>",getCellX(), getCellY(), cost, terrain.name()));
        }
    }

    public void cleanup() {
        // Cleanup resources and dereference the callback
        setTower(null);
    }
}

package DCT.client.views;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import DCT.client.Client;
import DCT.client.IGameControls;
import DCT.common.CellType;

public class CellPanel extends JPanel {
    private static final List<CellPanel> allCells = new ArrayList<>();
    private boolean selected = false;
    private int x, y;
    private JTextField jtf;
    private IGameControls callback;
    private CellType cellType;

    public CellType getCellType() {
        return cellType;
    }

    public CellPanel(IGameControls callback) {
        this.callback = callback;
        allCells.add(this); // Add this instance to the list of all cells
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) { // Check if it's a right-click event
                    promptAction();
                }

                selected = !selected;
                // setBackground(selected ? Color.GREEN : Color.WHITE);
                updateAllBorders(); // Update the borders of all cells
                setBorder(BorderFactory.createLineBorder(selected ? Color.CYAN : Color.BLACK));
                if (callback != null) {
                    callback.onClickCell(x, y);
                }
            }
        });
        jtf = new JTextField("");
        jtf.setEditable(false);
        jtf.setEnabled(false); // Disable the JTextField to make it not clickable
        jtf.setOpaque(false); // Make the JTextField non-opaque
        jtf.setBorder(null); // Remove the border
        jtf.setHorizontalAlignment(JTextField.CENTER); // Center the text
        this.add(jtf);
    }

    private void promptAction() {
        // Show JOptionPane with options
        Object[] options = { "Move", "Attack", "Heal", "End Turn" };
        int result = JOptionPane.showOptionDialog(null, "Choose an action", "Action",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);
        // Handle the result
        try {
            switch (result) {
                case 0: // Move
                    // Handle move action
                    Client.INSTANCE.sendMove(x, y);
                    break;
                case 1: // Attack
                    // Handle attack action
                    Client.INSTANCE.sendAttack(x, y);
                    break;
                case 2: // Heal
                    Client.INSTANCE.sendHeal(x, y);
                    break;
                case 3: // end turn
                    Client.INSTANCE.sendEndTurn();
                    break;
                default:
                    // Handle case where user closed the dialog without choosing an option
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        jtf.setText(c + "");
    }

    public int getOccupiedCount() {
        try {
            return Integer.parseInt(jtf.getText());
        } catch (Exception e) {
            return 0;
        }
    }

    public void setType(CellType type, int x, int y, boolean isBlocked) {
        this.x = x;
        this.y = y;
        this.cellType = type;
        switch (type) {
            case END_DOOR:
                setBackground(Color.BLUE);
                break;
            case START_DOOR:
                setBackground(Color.GREEN);
                break;
            case TILE:
                setBackground(isBlocked ? Color.GRAY : Color.WHITE);
                break;
            case WALL:
                setBackground(new Color(102, 51, 0));
                break;
            default:
                setBackground(Color.BLACK);
                break;

        }
    }

    public static void reset() {
        allCells.forEach(c -> {
            c.callback = null;
        });
        allCells.clear();
    }
}
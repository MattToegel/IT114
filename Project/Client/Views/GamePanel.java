package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import Project.Client.CardView;
import Project.Client.Client;
import Project.Client.Interfaces.*;
import Project.Common.Constants;
import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.Tower;

public class GamePanel extends JPanel implements ITowerEvents, IRoomEvents, IPhaseEvent, IGridEvents, IReadyEvent {
    private CellPanel[][] cells;
    private JPanel gridPanel;
    private CardLayout cardLayout;
    private static final String READY_PANEL = "READY";
    private static final String GRID_PANEL = "GRID";
    private TurnAction currentAction = TurnAction.NONE;
    private Tower selectedTower = null;
    private CellPanel selectedCell = null;
    private List<Tower> defenderTowers = new ArrayList<>();
    JPanel buttonPanel = new JPanel();

    public enum TurnAction {
        NONE,
        PLACE,
        ALLOCATE,
        ATTACK
    }

    public GamePanel(ICardControls controls) {
        super(new BorderLayout());

        // Create the buttons and add them to a panel
        JButton placeButton = createActionButton("Place", TurnAction.PLACE);
        JButton allocateButton = createActionButton("Allocate", TurnAction.ALLOCATE);
        JButton attackButton = createActionButton("Attack", TurnAction.ATTACK);
        JButton endButton = createActionButton("End", TurnAction.NONE);

        buttonPanel.add(placeButton);
        buttonPanel.add(allocateButton);
        buttonPanel.add(attackButton);
        buttonPanel.add(endButton);
        buttonPanel.setVisible(false);

        // Add the button panel to the top of the GamePanel
        this.add(buttonPanel, BorderLayout.NORTH);

        JPanel gameContainer = new JPanel(new CardLayout());
        cardLayout = (CardLayout) gameContainer.getLayout();
        this.setName(CardView.GAME_SCREEN.name());
        Client.INSTANCE.addCallback(this);

        ReadyPanel readyPanel = new ReadyPanel();
        readyPanel.setName(READY_PANEL);
        gameContainer.add(READY_PANEL, readyPanel);

        gridPanel = new JPanel();
        gridPanel.setName(GRID_PANEL);
        gameContainer.add(GRID_PANEL, gridPanel);

        GameEventsPanel gameEventsPanel = new GameEventsPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gameContainer, gameEventsPanel);
        splitPane.setResizeWeight(0.7);

        gridPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                splitPane.setDividerLocation(0.7);
            }
        });

        gridPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                gridPanel.revalidate();
                gridPanel.repaint();
            }
        });

        this.add(splitPane, BorderLayout.CENTER);
        setVisible(false);
    }

    private JButton createActionButton(String text, TurnAction action) {
        JButton button = new JButton(text);
        button.addActionListener((event) -> {
            currentAction = action;
            LoggerUtil.INSTANCE.fine("Selected " + currentAction);
            selectedTower = null;
            defenderTowers.clear();
            if (action == TurnAction.NONE) {
                try {
                    Client.INSTANCE.sendEndTurn();
                } catch (IOException e) {
                    Client.INSTANCE.clientSideGameEvent("Connection issue during End action");
                }
            }
        });
        return button;
    }

    private void resetView() {
        if (gridPanel != null) {
            LoggerUtil.INSTANCE.fine("ResetView()");
            gridPanel.setLayout(null);
            CellPanel.reset();
            cells = null;
            gridPanel.removeAll();
            gridPanel.revalidate();
            gridPanel.repaint();
        }
    }

    private void makeGrid(int rows, int columns) {
        SwingUtilities.invokeLater(() -> {
            resetView();
            LoggerUtil.INSTANCE.fine("Create CellPanels Grid");
            cells = new CellPanel[rows][columns];
            gridPanel.setLayout(new GridLayout(rows, columns));
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    cells[i][j] = new CellPanel(i, j, this::handleSelection);
                    gridPanel.add(cells[i][j]);
                }
            }
            gridPanel.revalidate();
            gridPanel.repaint();
        });
    }

    private void handleSelection(CellPanel cellPanel) {
        Tower tower = cellPanel.getTower();
        LoggerUtil.INSTANCE.fine("Selected a panel with action: " + currentAction.name());
        showDialog(cellPanel, tower);
    }

    private void showDialog(CellPanel cellPanel, Tower tower) {
        List<String> options = new ArrayList<>();

        switch (currentAction) {
            case PLACE:
                options.add("Place");
                break;
            case ALLOCATE:
                if (tower != null && tower.getClientId() == Client.INSTANCE.getMyClientId()) {
                    options.add("Allocate");
                }
                break;
            case ATTACK:
                if (selectedTower == null && tower != null && tower.getClientId() == Client.INSTANCE.getMyClientId()) {
                    options.add("Select Attacker");
                } else if (selectedTower != null && tower != null
                        && tower.getClientId() != Client.INSTANCE.getMyClientId()) {
                    options.add("Select Defender");
                }
                break;
            default:
                if (tower != null) {
                    options.add("Inspect");
                }
                break;
        }

        if (!options.isEmpty()) {
            String[] optionArray = options.toArray(new String[0]);
            String selectedOption = (String) JOptionPane.showInputDialog(
                    this,
                    "Choose an action:",
                    "Action Menu",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    optionArray,
                    optionArray[0]);

            if (selectedOption != null) {
                handleAction(selectedOption, cellPanel, tower);
            }
        }
    }

    private void handleAction(String action, CellPanel cellPanel, Tower tower) {
        switch (action) {
            case "Place":
                handlePlaceAction(cellPanel);
                break;
            case "Allocate":
                handleAllocateAction(cellPanel, tower);
                break;
            case "Select Attacker":
                selectedTower = tower;
                selectedCell = cellPanel;
                System.out.println("Attacker tower selected");
                break;
            case "Select Defender":
                handleAttackAction(cellPanel, tower);
                break;
            case "Inspect":
                inspectTower(tower);
                break;
        }
    }

    private void inspectTower(Tower tower) {
        if (tower != null) {
            JOptionPane.showMessageDialog(this, tower.toString(), "Tower Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handlePlaceAction(CellPanel cellPanel) {
        try {
            Client.INSTANCE.sendPlace(cellPanel.getCellX(), cellPanel.getCellY());
            cellPanel.setSelected(false);
        } catch (IOException e) {
            Client.INSTANCE.clientSideGameEvent("Connection issue during Place action");
        } catch (Exception e) {
            Client.INSTANCE.clientSideGameEvent(e.getMessage());
        }
    }

    private void handleAllocateAction(CellPanel cellPanel, Tower tower) {
        if (tower != null && tower.getClientId() == Client.INSTANCE.getMyClientId()) {
            String input = JOptionPane.showInputDialog(this,
                    "Enter energy to allocate (negative to deallocate):", tower.getAllocatedEnergy());
            if (input != null) {
                try {
                    int energy = Integer.parseInt(input);
                    Client.INSTANCE.sendAllocate(cellPanel.getCellX(), cellPanel.getCellY(), energy);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } catch (IOException e) {
                    Client.INSTANCE.clientSideGameEvent("Connection issue during Allocation action");
                }
            }
        }
    }

    private void handleAttackAction(CellPanel cellPanel, Tower tower) {
        if (selectedTower != null && tower != null && tower.getClientId() != Client.INSTANCE.getMyClientId()) {
            int distance = Math.abs(cellPanel.getCellX() - selectedCell.getCellX())
                    + Math.abs(cellPanel.getCellY() - selectedCell.getCellY());
            if (distance <= selectedTower.getRange()) {
                defenderTowers.add(tower);
                System.out.println("Defender tower added");
                try {
                    Client.INSTANCE.sendAttack(selectedCell.getCellX(), selectedCell.getCellY(),
                            defenderTowers.stream().map(Tower::getId).collect(Collectors.toList()));
                } catch (IOException e) {
                    Client.INSTANCE.clientSideGameEvent("Connection issue during Attack action");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Tower is out of range.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onRoomAction(long clientId, String clientName, String roomName, boolean isJoin) {
        if (Constants.LOBBY.equals(roomName) && isJoin) {
            setVisible(false);
            revalidate();
            repaint();
        }
    }

    @Override
    public void onReceivePhase(Phase phase) {
        System.out.println("Received phase: " + phase.name());
        if (!isVisible()) {
            setVisible(true);
            getParent().revalidate();
            getParent().repaint();
            System.out.println("GamePanel visible");
        }
        if (phase == Phase.READY) {
            cardLayout.show(gridPanel.getParent(), READY_PANEL);
            buttonPanel.setVisible(false);
        } else if (phase == Phase.TURN) {
            cardLayout.show(gridPanel.getParent(), GRID_PANEL);
            buttonPanel.setVisible(true);
        }
    }

    @Override
    public void onReceiveReady(long clientId, boolean isReady) {
        // Implement functionality if needed
    }

    @Override
    public void onReceiveGrid(int rows, int columns) {
        if (rows > 0 && columns > 0) {
            makeGrid(rows, columns);
        } else {
            resetView();
        }
    }

    @Override
    public void onReceiveTowerStatus(int x, int y, Tower tower) {
        if (cells != null && x >= 0 && x < cells.length && y >= 0 && y < cells[0].length) {
            CellPanel target = cells[x][y];
            target.setTower(tower);
        } else {
            System.err.println("Invalid cell coordinates: (" + x + ", " + y + ")");
        }
    }

    @Override
    public void onReceiveRoomList(List<String> rooms, String message) {
        // Not used here, but needs to be defined due to interface
    }
}

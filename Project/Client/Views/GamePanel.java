package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Frame;
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
import Project.Client.Interfaces.ICardControls;
import Project.Client.Interfaces.ICardEvents;
import Project.Client.Interfaces.IGridEvents;
import Project.Client.Interfaces.IPhaseEvent;
import Project.Client.Interfaces.IReadyEvent;
import Project.Client.Interfaces.IRoomEvents;
import Project.Client.Interfaces.ITowerEvents;
import Project.Common.Card;
import Project.Common.Constants;
import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.Tower;

public class GamePanel extends JPanel
        implements ITowerEvents, IRoomEvents, IPhaseEvent, IGridEvents, IReadyEvent, ICardEvents {
    private CellPanel[][] cells;
    private JPanel gridPanel;
    private CardLayout cardLayout;
    private static final String READY_PANEL = "READY";
    private static final String GRID_PANEL = "GRID";
    private TurnAction currentAction = TurnAction.NONE;
    private Tower selectedTower = null;
    private CellPanel selectedCell = null;
    private List<Tower> defenderTowers = new ArrayList<>();
    private List<Card> cards = new ArrayList<>();
    private Card selectedCard = null;
    private JPanel buttonPanel = new JPanel();
    private JButton cardSelectButton;
    private JButton cancelButton;

    public enum TurnAction {
        NONE,
        PLACE,
        ALLOCATE,
        ATTACK,
        SELECT,
        INSPECT
    }

    public GamePanel(ICardControls controls) {
        super(new BorderLayout());

        // Create the buttons and add them to a panel
        JButton placeButton = createActionButton("Place", TurnAction.PLACE);
        JButton allocateButton = createActionButton("Allocate", TurnAction.ALLOCATE);
        JButton attackButton = createActionButton("Attack", TurnAction.ATTACK);
        cancelButton = new JButton("Cancel");
        cancelButton.setVisible(false);
        cancelButton.addActionListener((event) -> {
            enableButtons();
            selectedCard = null;
        });
        JButton endButton = createActionButton("End", TurnAction.NONE);
        cardSelectButton = createCardSelectButton();

        buttonPanel.add(placeButton);
        buttonPanel.add(allocateButton);
        buttonPanel.add(attackButton);
        buttonPanel.add(cardSelectButton);
        buttonPanel.add(cancelButton);
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
        controls.addPanel(CardView.CHAT_GAME_SCREEN.name(), this);
        setVisible(false);
    }

    private void useCard(Card card, int x, int y) {
        if (card.getEnergy() > Client.INSTANCE.getMyEnergy()) {
            Client.INSTANCE.clientSideGameEvent("You can't afford to use this card");
            return;
        }
        try {
            Client.INSTANCE.sendUseCard(card, x, y);
        } catch (IOException e) {
            Client.INSTANCE.clientSideGameEvent("There was a network error during use card");
        }
    }

    private void handleTargetSelection(int x, int y) {
        if (selectedCard != null) {
            useCard(selectedCard, x, y);
            selectedCard = null;
            currentAction = TurnAction.NONE;
            enableButtons();
        }
    }

    private void enableButtons() {
        for (Component component : buttonPanel.getComponents()) {
            component.setEnabled(true);
        }
        cancelButton.setVisible(false);
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    private void disableButtonsExceptCancel() {
        for (Component component : buttonPanel.getComponents()) {
            if (component instanceof JButton && !((JButton) component).equals(cancelButton)) {
                component.setEnabled(false);
            }
        }
        cancelButton.setVisible(true);
        buttonPanel.revalidate();
        buttonPanel.repaint();
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

    private JButton createCardSelectButton() {
        JButton button = new JButton("Select Card");
        button.addActionListener((event) -> {
            if (cards.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No cards available", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            CardSelectionDialog dialog = new CardSelectionDialog(parentFrame, cards);
            dialog.setVisible(true);
            selectedCard = dialog.getSelectedCard();
            if (selectedCard != null) {
                if (dialog.isDiscarded()) {
                    // Handle discard card
                    System.out.println("Discarded card: " + selectedCard);
                    try {
                        Client.INSTANCE.sendDiscardCard(selectedCard);
                    } catch (IOException e) {
                        Client.INSTANCE.clientSideGameEvent("There was a network error during discard");
                    }
                } else {
                    // Handle use card
                    System.out.println("Used card: " + selectedCard);
                    // Check if the card requires a target
                    if (selectedCard.requiresTarget()) {
                        disableButtonsExceptCancel();
                        currentAction = TurnAction.SELECT;
                    } else {
                        useCard(selectedCard, -1, -1);
                    }
                }
            } else {
                System.out.println("No card selected");
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
        LoggerUtil.INSTANCE.info(String.format("Selected Tower: %s, Tower: %s, My ID %s",
        selectedTower==null?"null":selectedTower, tower==null?"null":tower, Client.INSTANCE.getMyClientId()));
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
            case SELECT:
                options.add("Select");
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
                try {
                    selectedOption = selectedOption.toLowerCase();
                    if(selectedOption.contains("attacker") || selectedOption.contains("defender")){
                        selectedOption = TurnAction.ATTACK.name();
                    }
                    TurnAction selectedAction = TurnAction.valueOf(selectedOption.toUpperCase());
                    handleAction(selectedAction, cellPanel, tower);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid action selected");
                }
            }
        }
    }

    private void handleAction(TurnAction action, CellPanel cellPanel, Tower tower) {
        switch (action) {
            case PLACE:
                handlePlaceAction(cellPanel);
                break;
            case ALLOCATE:
                handleAllocateAction(cellPanel, tower);
                break;
            case ATTACK:
                if (selectedTower == null) {
                    selectedTower = tower;
                    selectedCell = cellPanel;
                    System.out.println("Attacker tower selected");
                } else {
                    handleAttackAction(cellPanel, tower);
                }
                break;
            case SELECT:
                handleTargetSelection(cellPanel.getCellX(), cellPanel.getCellY());
                break;
            case INSPECT:
            case NONE:
                inspectTower(tower);
                break;
            default:
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
            buttonPanel.revalidate();
            buttonPanel.repaint();
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
            Client.INSTANCE.clientSideGameEvent("Invalid cell coordinates: (\" + x + \", \" + y + \")");
        }
    }

    @Override
    public void onReceiveRoomList(List<String> rooms, String message) {
        // Not used here, but needs to be defined due to interface
    }

    public void clearCards() {
        cards.clear();
    }

    @Override
    public void onAddCard(Card card) {
        LoggerUtil.INSTANCE.fine(String.format("Added card: %s", card));
        cards.add(card);
        cardSelectButton.setText(String.format("Hand: %s", this.cards.size()));
    }

    @Override
    public void onRemoveCard(Card card) {
        LoggerUtil.INSTANCE.fine(String.format("Removed card: %s", card));
        cards.removeIf(c -> c.getId() == card.getId());
        cardSelectButton.setText(String.format("Hand: %s", this.cards.size()));
    }

    @Override
    public void onSetCards(List<Card> cards) {
        LoggerUtil.INSTANCE.fine(String.format("Set Cards: %s", cards == null ? "null" : cards.size()));
        if (cards == null) {
            clearCards();
        } else {
            // make a copy of the passed in list to avoid reference issues
            this.cards = cards.stream().map(c -> c).collect(Collectors.toList());
        }
        cardSelectButton.setText(String.format("Hand: %s", this.cards == null ? 0 : this.cards.size()));
    }
}

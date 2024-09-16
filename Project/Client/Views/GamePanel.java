package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import Project.Client.CardView;
import Project.Client.Client;
import Project.Client.ICardControls;
import Project.Client.IGameEvents;

import Project.Common.CellData;
import Project.Common.CellType;
import Project.Common.Constants;
import Project.Common.Phase;

public class GamePanel extends JPanel implements IGameEvents {
    private CellPanel[][] cells;
    private JPanel gridPanel;
    private CardLayout cardLayout;
    private final static String READY_PANEL = "READY";
    private final static String GRID_PANEL = "GRID";
    private ICardControls controls;
    private JPanel gameContainerCardLayout;
    public GamePanel(ICardControls controls) {
        // super(new CardLayout());
        super(new BorderLayout());
        this.controls = controls;
        gameContainerCardLayout = new JPanel();
        gameContainerCardLayout.setLayout(new CardLayout());
        cardLayout = (CardLayout) gameContainerCardLayout.getLayout();
        this.setName(CardView.GAME_SCREEN.name());
        Client.INSTANCE.addCallback(this);
        // ready panel
        ReadyPanel rp = new ReadyPanel();
        rp.setName(READY_PANEL);
        gameContainerCardLayout.add(READY_PANEL, rp);

        JPanel gridAreaContainer = new JPanel();
        gridAreaContainer.setLayout(new BorderLayout());
        // grid
        gridPanel = new JPanel();
        gridPanel.setName(GRID_PANEL);
        gridAreaContainer.add(gridPanel, BorderLayout.CENTER);
        JButton roll = new JButton("Roll");
        roll.setBackground(Color.BLACK);
        roll.setForeground(Color.WHITE);
        roll.addActionListener((action) -> {
            try {
                Client.INSTANCE.sendRoll();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        gridAreaContainer.add(roll, BorderLayout.SOUTH);
        gameContainerCardLayout.add(GRID_PANEL, gridAreaContainer);
        // game events
        GameEventsPanel gep = new GameEventsPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gameContainerCardLayout, gep);
        splitPane.setResizeWeight(.7);

        gridPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // Recalculate the divider location when the left panel becomes visible
                splitPane.setDividerLocation(0.7);
            }
        });
        this.add(splitPane, BorderLayout.CENTER);
        // this.add(gep, BorderLayout.SOUTH);
        setVisible(false);
        // don't need to add this to ClientUI as this isn't a primary panel(it's nested
        // in ChatGamePanel)
        // controls.addPanel(Card.GAME_SCREEN.name(), this);
    }

    private void resetView() {
        if (gridPanel == null) {
            return;
        }
        if (gridPanel.getLayout() != null) {
            gridPanel.setLayout(null);
        }
        cells = null;
        gridPanel.removeAll();
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void makeGrid(int rows, int columns) {
        resetView();
        cells = new CellPanel[rows][columns];
        gridPanel.setLayout(new GridLayout(rows, columns));
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                cells[i][j] = new CellPanel();
                cells[i][j].setType(CellType.NONE, i, j);
                gridPanel.add(cells[i][j]);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    @Override
    public void onClientConnect(long id, String clientName, String message) {
    }

    @Override
    public void onClientDisconnect(long id, String clientName, String message) {
    }

    @Override
    public void onMessageReceive(long id, String message) {
    }

    @Override
    public void onReceiveClientId(long id) {
    }

    @Override
    public void onSyncClient(long id, String clientName) {
    }

    @Override
    public void onResetUserList() {
    }

    @Override
    public void onRoomJoin(String roomName) {
        if (Constants.LOBBY.equals(roomName)) {
            setVisible(false);// TODO along the way to hide game view when you leave
            this.revalidate();
            this.repaint();
        }
    }

    @Override
    public void onReceiveRoomList(List<String> rooms, String message) {

    }

    @Override
    public void onReceivePhase(Phase phase) {
        // I'll temporarily do next(), but there may be scenarios where the screen can
        // be inaccurate
        System.out.println("Received phase: " + phase.name());
        if (!isVisible()) {
            setVisible(true);
            this.getParent().revalidate();
            this.getParent().repaint();
            System.out.println("GamePanel visible");
        }
        if (phase == Phase.READY) {
            cardLayout.show(gameContainerCardLayout, READY_PANEL);
        } else if (phase == Phase.TURN) {
            cardLayout.show(gameContainerCardLayout, GRID_PANEL);
        }
    }

    @Override
    public void onReceiveReady(long clientId, boolean isReady) {
    }

    @Override
    public void onReceiveCell(List<CellData> cells) {
        for (CellData c : cells) {
            CellPanel target = this.cells[c.getX()][c.getY()];
            target.setType(c.getCellType(), c.getX(), c.getY());
            target.setOccupiedCount(c.getNumInCell());
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    @Override
    public void onReceiveGrid(int rows, int columns) {
        resetView();
        if (rows > 0 && columns > 0) {
            makeGrid(rows, columns);
        }
    }

    @Override
    public void onReceiveRoll(long clientId, int roll) {

    }

    @Override
    public void onReceivePoints(long clientId, int changedPoints, int currentPoints) {
        controls.updateClientPoints(clientId, currentPoints);
    }

    @Override
    public void onReceiveCurrentTurn(long clientId) {
        controls.updateCurrentTurn(clientId);
    }

    @Override
    public void onReceiveGameEvent(String message) {

    }

}

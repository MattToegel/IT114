package Popularity.Client.Views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import Popularity.Client.CardView;
import Popularity.Client.Client;
import Popularity.Client.Interfaces.ICardControls;
import Popularity.Client.Interfaces.IGridEvents;
import Popularity.Client.Interfaces.IPhaseEvent;
import Popularity.Client.Interfaces.IRoomEvents;
import Popularity.Common.Constants;
import Popularity.Common.Phase;

public class GamePanel extends JPanel implements IRoomEvents, IPhaseEvent, IGridEvents {

    private JPanel playPanel;
    private JPanel scorePanel;
    private JTable scoreTable;
    private CardLayout cardLayout;
    private static final String READY_PANEL = "READY";
    private static final String PLAY_PANEL = "PLAY";// example panel for this lesson
    private static final String SCORE_PANEL = "SCORE";

    public GamePanel(ICardControls controls) {
        super(new BorderLayout());

        JPanel gameContainer = new JPanel(new CardLayout());
        cardLayout = (CardLayout) gameContainer.getLayout();
        this.setName(CardView.GAME_SCREEN.name());
        Client.INSTANCE.addCallback(this);

        ReadyPanel readyPanel = new ReadyPanel();
        readyPanel.setName(READY_PANEL);
        gameContainer.add(READY_PANEL, readyPanel);

        playPanel = new JPanel();
        playPanel.setName(PLAY_PANEL);
        gameContainer.add(PLAY_PANEL, playPanel);

        scorePanel = new JPanel(new BorderLayout());
        scorePanel.setName(SCORE_PANEL);
        scoreTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        scorePanel.add(scrollPane, BorderLayout.CENTER);
        gameContainer.add(SCORE_PANEL, scorePanel);

        GameEventsPanel gameEventsPanel = new GameEventsPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gameContainer, gameEventsPanel);
        splitPane.setResizeWeight(0.7);

        playPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                splitPane.setDividerLocation(0.7);
            }
        });

        playPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                playPanel.revalidate();
                playPanel.repaint();
            }
        });

        this.add(splitPane, BorderLayout.CENTER);
        controls.addPanel(CardView.CHAT_GAME_SCREEN.name(), this);
        setVisible(false);
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
            cardLayout.show(playPanel.getParent(), READY_PANEL);

        } else if (phase == Phase.IN_PROGRESS) {
            cardLayout.show(playPanel.getParent(), PLAY_PANEL);
            for (Component cp : playPanel.getComponents()) {
                ((CellPanel) cp).setCount(0);
            }
        } else if (phase == Phase.SCORING) {
            DefaultTableModel dfm = new DefaultTableModel(new String[] { "Rank", "Player", "Score" }, 0);
            scoreTable.setModel(dfm);
            List<Object[]> scores = Client.INSTANCE.getScores();
            for (Object[] score : scores) {
                dfm.addRow(score);
            }
            cardLayout.show(scorePanel.getParent(), SCORE_PANEL);

        }
    }

    @Override
    public void onReceiveRoomList(List<String> rooms, String message) {
        // Not used here, but needs to be defined due to interface
    }

    @Override
    public void onUpdateOccupied(int x, int y, int count) {
        try {
            // convert 2d coordinate to 1d coordinate
            GridLayout layout = (GridLayout) playPanel.getLayout();
            int columns = layout.getColumns(); // Number of columns in the
            if (columns == 0) {// in case columns isn't set
                columns = playPanel.getComponentCount() / layout.getRows();
            }
            int index = x * columns + y;
            Component cellPanel = playPanel.getComponent(index);
            ((CellPanel) cellPanel).setCount(count);
        } catch (Exception e) {

        }
    }

    @Override
    public void onGridDimensions(int w, int h) {
        if (w <= 0 || h <= 0) {
            playPanel.removeAll();
            playPanel.setLayout(null);
            return;
        }
        if (playPanel.getComponentCount() > 0) {
            playPanel.removeAll();
        }
        playPanel.setLayout(new GridLayout(w, h));
        for (int row = 0; row < w; row++) {
            for (int col = 0; col < h; col++) {
                CellPanel cp = new CellPanel(row, col);
                playPanel.add(cp);
            }
        }
    }

    @Override
    public void onResetCells() {
        for (Component cp : playPanel.getComponents()) {
            ((CellPanel) cp).setCount(0);
        }
    }

    @Override
    public void onConfirmMove(int x, int y) {
        GridLayout layout = (GridLayout) playPanel.getLayout();
        int columns = layout.getColumns(); // Number of columns in the
        if (columns == 0) {// in case columns isn't set
            columns = playPanel.getComponentCount() / layout.getRows();
        }
        // clear existing
        for (Component cp : playPanel.getComponents()) {
            ((CellPanel) cp).unhighlight();
        }
        // highlight new
        int index = x * columns + y;
        Component cellPanel = playPanel.getComponent(index);
        ((CellPanel) cellPanel).highlight();
    }
}
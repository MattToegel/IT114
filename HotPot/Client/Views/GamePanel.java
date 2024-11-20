package HotPot.Client.Views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import HotPot.Client.CardView;
import HotPot.Client.Client;
import HotPot.Client.Interfaces.ICardControls;
import HotPot.Client.Interfaces.ICardGameEvents;
import HotPot.Client.Interfaces.IPhaseEvent;
import HotPot.Client.Interfaces.IRoomEvents;
import HotPot.Common.Card;
import HotPot.Common.Constants;
import HotPot.Common.Phase;

public class GamePanel extends JPanel implements IRoomEvents, IPhaseEvent, ICardGameEvents {

    private JPanel playPanel;
    private JPanel handPanel;
    private DrawPanel drawPanel;
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
        drawPanel = new DrawPanel();
        drawPanel.setMinimumSize(new Dimension(200, 200)); // Prevent collapse
        drawPanel.setPreferredSize(drawPanel.getMinimumSize());
        drawPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        handPanel = new JPanel();
        handPanel.setBorder(BorderFactory.createLineBorder(Color.CYAN));
        handPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Horizontal and vertical gaps
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, drawPanel, handPanel);
        split.setResizeWeight(.7);
        split.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        playPanel.add(split);
        gameContainer.add(PLAY_PANEL, playPanel);

        scorePanel = new JPanel(new BorderLayout());
        scorePanel.setName(SCORE_PANEL);
        scoreTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        scorePanel.add(scrollPane, BorderLayout.CENTER);
        gameContainer.add(SCORE_PANEL, scorePanel);

        GameEventsPanel gameEventsPanel = new GameEventsPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gameContainer, gameEventsPanel);
        splitPane.setResizeWeight(0.6);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                splitPane.setDividerLocation(0.6);
                split.setDividerLocation(.7);
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
        }
        else if (phase == Phase.SCORING) {
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
    public void onHandChange(List<Card> cards) {
        // Not the best approach but doing quick solutions for this lesson
        // It's best to recycle components instead of destroy/create
        handPanel.removeAll();
        for (Card card : cards) {
            CardPanel cp = new CardPanel(card);
            handPanel.add(cp);
        }
        this.revalidate();
        this.repaint();
    }

    @Override
    public void onReceivePercentage(int percentage) {
        drawPanel.setPosePercentage(percentage);
    }
}
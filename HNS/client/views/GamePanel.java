package HNS.client.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import HNS.client.Client;
import HNS.client.IClientEvents;
import HNS.common.Constants;
import HNS.common.GameOptions;
import HNS.common.Grid;
import HNS.common.Phase;
import HNS.common.TimedEvent;
import HNS.common.Cell;

public class GamePanel extends JPanel implements IClientEvents {

    int numReady = 0;

    private static Logger logger = Logger.getLogger(GamePanel.class.getName());
    GamePanel self;
    JPanel gridLayout;
    JPanel readyCheck;
    UserListPanel ulp;
    TimedEvent currentTimer;
    JLabel timeLabel = new JLabel("");
    Phase currentPhase;
    OptionsPanel optionsPanel;

    public GamePanel() {
        gridLayout = new JPanel();
        buildReadyCheck();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        timeLabel.setName("time");
        Dimension td = new Dimension(this.getWidth(), 30);
        timeLabel.setMaximumSize(td);
        timeLabel.setPreferredSize(td);
        this.add(timeLabel);
        this.add(gridLayout);
        this.add(readyCheck);
        self = this;
        Client.INSTANCE.addListener(this);
        this.setFocusable(true);
        this.setRequestFocusEnabled(true);

    }

    public void setUserListPanel(UserListPanel ulp) {
        this.ulp = ulp;
    }

    private void buildReadyCheck() {
        if (readyCheck == null) {
            readyCheck = new JPanel();
            readyCheck.setLayout(new BorderLayout());
            optionsPanel = new OptionsPanel(Client.INSTANCE.isHost());

            optionsPanel.setCallback((go) -> {
                // generate payload if I'm the host
                if (Client.INSTANCE.isHost()) {
                    try {
                        logger.info("Sending game options");
                        Client.INSTANCE.sendGameOptions(go);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });

            readyCheck.add(optionsPanel, BorderLayout.NORTH);

            JTextField tf = new JTextField(String.format("%s/%s", 0, Constants.MINIMUM_PLAYERS));
            tf.setName("readyText");
            tf.setMaximumSize(new Dimension(readyCheck.getWidth(), 30));
            tf.setPreferredSize(tf.getMaximumSize());
            readyCheck.add(tf, BorderLayout.CENTER);
            JButton jb = new JButton("Ready");
            jb.addActionListener((event) -> {
                if (!Client.INSTANCE.isCurrentPhase(Phase.READY)) {
                    return;
                }
                try {
                    Client.INSTANCE.sendReadyStatus();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readyCheck.add(jb, BorderLayout.SOUTH);
        }
    }

    private void drawBoard() {
        gridLayout.setVisible(true);
        gridLayout.repaint();
    }

    // Although we must implement all of these methods, not all of them may be
    // applicable to this panel
    @Override
    public void onClientConnect(long id, String clientName, String message) {

    }

    @Override
    public void onClientDisconnect(long id, String clientName, String message) {

    }

    @Override
    public void onMessageReceive(long id, String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReceiveClientId(long id) {

    }

    @Override
    public void onSyncClient(long id, String clientName) {
    }

    @Override
    public void onResetUserList() {
        // players.clear();
    }

    @Override
    public void onReceiveRoomList(String[] rooms, String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomJoin(String roomName) {
        logger.info(
                Constants.ANSI_BRIGHT_BLUE + String.format("Received room name %s", roomName) + Constants.ANSI_RESET);

        if (roomName.equalsIgnoreCase("lobby")) {
            setVisible(false);
        } else {
            setVisible(true);
        }

    }

    @Override
    public void onReceiveReady(long clientId) {
        if (currentTimer == null) {
            currentTimer = new TimedEvent(30, () -> {
                currentTimer = null;
            });
            currentTimer.setTickCallback((time) -> {
                timeLabel.setText("Remaining: " + time);
            });
        }
    }

    @Override
    public void onReceiveReadyCount(long count) {
        logger.info(
                Constants.ANSI_BRIGHT_BLUE + String.format("Received ready count %s", count) + Constants.ANSI_RESET);
        if (currentTimer != null && count == 0) {
            currentTimer.cancel();
            currentTimer = null;
        }
        if (readyCheck != null) {
            for (Component c : readyCheck.getComponents()) {
                if (c.getName().equalsIgnoreCase("readyText")) {
                    ((JTextField) c).setText(String.format("%s/%s", count, Constants.MINIMUM_PLAYERS));
                    break;
                }
            }
        }
        this.validate();
        this.repaint();
    }

    @Override
    public void onReceivePhase(Phase phase) {
        logger.info(Constants.ANSI_BRIGHT_BLUE + String.format("Received phase %s", phase) + Constants.ANSI_RESET);
        currentPhase = phase;
        Dimension td = new Dimension(this.getWidth(), 30);
        timeLabel.setMaximumSize(td);
        timeLabel.setPreferredSize(td);
        if (phase == Phase.READY) {
            readyCheck.setVisible(true);
            gridLayout.setVisible(false);
        } else if (phase == Phase.HIDE) {
            readyCheck.setVisible(false);
            gridLayout.setVisible(true);
        }

        // if (phase != Phase.READY) {
        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer = null;
        }
        // }
        if (phase != Phase.READY) {
            currentTimer = new TimedEvent(30, () -> {
                currentTimer = null;
            });
            currentTimer.setTickCallback((time) -> {
                timeLabel.setText("Remaining: " + time);
            });
        }
        this.validate();
        this.repaint();
        logger.info(
                Constants.ANSI_BRIGHT_MAGENTA + String.format("Dimension %s", this.getSize()) + Constants.ANSI_RESET);
    }

    @Override
    public void onReceiveSeeker(long clientId) {
        ulp.setSeeker(clientId);
    }

    @Override
    public void onReceiveHide(int x, int y, long clientId) {
        Grid grid = Client.INSTANCE.getGrid();
        int indexFrom2D = (x * grid.getColumnCount()) + y;
        JButton jb = (JButton) gridLayout.getComponent(indexFrom2D);
        if (jb != null) {
            jb.setText("" + grid.getCell(x, y).getPlayersInCell().size());
        }
        // recalculate hiders in cells
        for (Component c : gridLayout.getComponents()) {
            if (c instanceof JButton) {
                try {
                    String n = c.getName();
                    String[] nums = n.split(",");
                    int cx = Integer.parseInt(nums[0]);
                    int cy = Integer.parseInt(nums[1]);
                    int hiders = grid.getCell(cx, cy).getPlayersInCell().size();
                    if (hiders == 0) {
                        ((JButton) c).setText("");
                    } else {
                        ((JButton) c).setText("" + hiders);
                    }
                } catch (Exception e) {
                }
            }
        }
        drawBoard();
    }

    @Override
    public void onReceiveOut(long clientId) {
        ulp.setOut(clientId);
    }

    @Override
    public void onReceiveGrid(Grid grid) {
        int rows = grid.getRowCount();
        int columns = grid.getColumnCount();
        this.gridLayout.setLayout(new GridLayout(rows, columns));
        this.gridLayout.removeAll();
        for (int i = 0; i < (rows * columns); i++) {
            JButton button = new JButton();
            button.setSize(new Dimension(2, 2));
            // convert to x coordinate
            int x = i / rows;
            // convert to y coordinate
            int y = i % columns;
            // %1 first param, %2 second param, etc
            String buttonText = "";// String.format("%1$s:(%2$s, %3$s)", i, x, y);
            button.setText(buttonText);
            button.setBackground(Color.white);
            button.setName(x + "," + y);
            // check blocked
            try {
                Cell cell = grid.getCell(x, y);
                if (cell.isBlocked()) {
                    button.setEnabled(false);
                    button.setText("X");
                    button.setBackground(Color.gray);
                }
            } catch (Exception e) {

            }
            // show index and coordinate details on button

            // create an action to perform when button is clicked
            // override the default actionPerformed method to tell the code how to handle it
            button.addActionListener((event) -> {
                // TODO
                final int mx = x;
                final int my = y;
                try {
                    if (Client.INSTANCE.isSeeker()) {
                        Client.INSTANCE.sendSeekPosition(mx, my);
                    } else {
                        Client.INSTANCE.sendHidePosition(mx, my);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO handle
                }
            });

            this.gridLayout.add(button);
        }
    }

    @Override
    public void onReceivePoints(long clientId, int points) {
        if (ulp != null) {
            ulp.setPointsForPlayer(clientId, points);
        }
    }

    @Override
    public void onReceiveHost(long clientId) {
        if (optionsPanel != null) {
            optionsPanel.setIsHost(Client.INSTANCE.isHost());
        }
        ulp.setHost(clientId);
    }

    @Override
    public void onReceiveGameOptions(GameOptions options) {
        if (optionsPanel != null) {
            optionsPanel.setOptions(options);
        }
    }

}

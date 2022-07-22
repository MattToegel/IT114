package AnteMatter.client.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import AnteMatter.client.Client;
import AnteMatter.client.ClientUtils;
import AnteMatter.client.IClientEvents;
import AnteMatter.common.Constants;
import AnteMatter.common.Countdown;
import AnteMatter.common.GeneralUtils;
import AnteMatter.common.MyLogger;
import AnteMatter.common.Phase;
import AnteMatter.common.Player;

public class GamePanel extends JPanel implements IClientEvents {

    private boolean isReady = false;
    private Phase currentPhase = Phase.READY_CHECK;
    private Hashtable<Long, Player> players = new Hashtable<Long, Player>();
    private long myId = Constants.DEFAULT_CLIENT_ID;
    int numReady = 0;

    private Rectangle readyButton = new Rectangle();
    private Rectangle betArea = new Rectangle();
    private Rectangle confirmButton = new Rectangle();

    private static MyLogger logger = MyLogger.getLogger(GamePanel.class.getName());
    GamePanel self;
    private boolean isMyTurn = false;
    private long maxGuess = 0;
    private long maxBet = 0;
    private State currentState = State.IDLE;
    private int currentBet = 0;
    private long currentGuess = 0;
    private long lastWinner = Constants.DEFAULT_CLIENT_ID;
    private int countdown = 30;
    private Countdown turnTimer;

    private enum State {
        PENDING_RESTART, IDLE, BET, GUESS
    }

    public GamePanel() {
        self = this;
        Client.INSTANCE.addCallback(this);
        this.setFocusable(true);
        this.setRequestFocusEnabled(true);
        this.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                // check if we're in the proper state
                if (currentState.ordinal() > State.IDLE.ordinal()) {
                    handleValueSelect(e.getPoint());
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // TODO Auto-generated method stub

            }

        });
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                self.grabFocus();

                logger.info(
                        String.format("Mouse info LOC %s Point %s", e.getLocationOnScreen(), e.getPoint()));
                if (currentPhase == Phase.READY_CHECK) {
                    // get point is relative to source
                    handleReadyCheck(e.getPoint());
                } else if (currentPhase == Phase.ANTE) {
                    handleTurnConfirm(e.getPoint());
                } else if (currentPhase == Phase.REVEAL) {
                    handleEndGame(e.getPoint());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }

        });

    }

    private void handleReadyCheck(Point mp) {
        if (currentPhase == Phase.READY_CHECK) {
            // get point is relative to source
            if (!isReady && readyButton.contains(mp)) {
                // isReady = true;
                try {
                    Client.INSTANCE.sendReady();
                } catch (NullPointerException | IOException e1) {
                    e1.printStackTrace();
                }
                self.repaint();
            }
        }
    }

    private void handleTurnConfirm(Point mp) {
        if (currentPhase == Phase.ANTE) {
            if (confirmButton.contains(mp)) {
                if (currentState == State.BET && currentBet > 0) {
                    currentState = State.GUESS;
                } else if (currentState == State.GUESS && currentGuess > 0) {
                    // TODO send bet/guess
                    logger.info("Sending bet and guess");
                    try {
                        Client.INSTANCE.sendBetAndGuess(currentBet, currentGuess);
                        currentState = State.IDLE;
                    } catch (NullPointerException | IOException e1) {
                        e1.printStackTrace();
                    }
                }
                self.repaint();
            }
        }
    }

    private void handleEndGame(Point mp) {
        if (confirmButton.contains(mp)) {
            if (mp.getX() <= confirmButton.getCenterX()) {
                // selected yes; trigger restart cycle

                try {
                    Client.INSTANCE.sendRestartRequest();
                } catch (NullPointerException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                // selected no; move player to lobby
                try {
                    Client.INSTANCE.sendJoinRoom(Constants.LOBBY);
                } catch (NullPointerException | IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void handleValueSelect(Point mp) {
        if (currentState.ordinal() > State.IDLE.ordinal()) {
            // check if we're interacting with the proper rectangle
            if (betArea.contains(mp)) {
                logger.info(mp + " " + betArea.getLocation() + " " + betArea.getMinX()
                        + " " + betArea.getMaxX());
                // calculate position to generate a percentage
                double x = mp.x - betArea.getMinX();
                double max = betArea.getMaxX() - betArea.getMinX();
                double p = x / max;
                long lastVal;
                if (currentState == State.BET) {
                    lastVal = currentBet;
                    currentBet = (int) Math.ceil(p * maxBet);
                    currentBet = GeneralUtils.clamp(currentBet, 1, (int) maxBet);
                    if (lastVal != currentBet) {
                        self.repaint();
                    }

                } else if (currentState == State.GUESS) {
                    lastVal = currentGuess;
                    currentGuess = (int) Math.round(p * maxGuess);
                    currentGuess = GeneralUtils.clamp(currentGuess, 1, maxGuess);
                    if (lastVal != currentGuess) {
                        self.repaint();
                    }
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        // Let UI Delegate paint first, which
        // includes background filling since
        // this component is opaque.
        super.paintComponent(g); // paint parent's background
        setBackground(Color.BLACK); // set background color for this JPanel
        // https://www3.ntu.edu.sg/home/ehchua/programming/java/J4b_CustomGraphics.html
        // https://books.trinket.io/thinkjava/appendix-b.html
        // http://www.edu4java.com/en/game/game2.html
        Graphics2D g2 = ((Graphics2D) g);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (currentPhase) {
            case READY_CHECK:
                drawReadyCheck(g2);
                break;
            case ANTE:
                drawScoreboard(g2);
                if (currentState.ordinal() > State.IDLE.ordinal()) {
                    drawControls(g2);
                }
                break;
            case REVEAL:
                drawEndGame(g2);
                break;
            default:
                break;
        }

    }

    private void drawReadyCheck(Graphics2D g) {
        Dimension s = self.getSize();
        // logger.info( "Panel size: " + s);
        readyButton.setRect(s.getWidth() * .1f, s.getHeight() * .5f, (s.getWidth() * .9f) - (s.getWidth() * .1f),
                s.getHeight() * .2f);
        g.setColor(Color.WHITE);
        g.draw(readyButton);
        g.setFont(new Font("Monospaced", Font.PLAIN, 32));
        int totalPlayers = players.size();
        if (totalPlayers < Constants.MINIMUM_PLAYERS) {
            totalPlayers = Constants.MINIMUM_PLAYERS;
        }
        ClientUtils.drawCenteredString(String.format("Ready %s/%s", numReady, totalPlayers), 0,
                -(int) (s.getHeight() * .2f), s.width, s.height, g);
        ClientUtils.drawCenteredString(isReady ? "READY!" : "Click when ready",
                readyButton.x,
                readyButton.y,
                readyButton.width,
                readyButton.height,
                g);
    }

    private void drawScoreboard(Graphics2D g) {
        Dimension s = self.getSize();
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        int i = 1;
        g.setColor(Color.WHITE);
        synchronized (players) {
            // sort players by matter high to low
            List<Player> _players = new ArrayList<Player>(players.values());
            _players.sort((a, b) -> {
                if (a.getMatter() == b.getMatter()) {
                    return 0;
                } else if (a.getMatter() < b.getMatter()) {
                    return 1;
                }
                return -1;
            });
            ClientUtils.drawCenteredString("The Matter Ladder", 0, (int) (s.getHeight() * .025f),
                    (int) s.getWidth(), 0, g);
            for (Player p : _players) {
                int offset = (int) ((s.getHeight() * .025f) * (i + 1));
                logger.info("Offset: " + offset + " size: " + s);
                String toShow = String.format("%s has %s matter", p.getClientName(), p.getMatter());
                ClientUtils.drawCenteredString(toShow, 0, offset,
                        (int) s.getWidth(), 0, g);
                i++;
            }
        }
    }

    private void drawControls(Graphics2D g) {
        Dimension s = self.getSize();
        g.setColor(Color.WHITE);
        // draggable area for betting/guessing
        betArea.setRect(s.getWidth() * .1f, s.getHeight() * .7f, (s.getWidth() * .9f) - (s.getWidth() * .1f),
                s.getHeight() * .05f);
        confirmButton.setRect(s.getWidth() * .2f, s.getHeight() * .8f, (s.getWidth() * .8f) - (s.getWidth() * .2f),
                s.getHeight() * .1f);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ClientUtils.drawCenteredString("Drag mouse here \nto choose value",
                betArea.x, betArea.y, betArea.width, betArea.height, g);
        ClientUtils.drawCenteredString(
                String.format("Confirm your Ante and Guess within %s seconds.", countdown),
                confirmButton.x,
                (int) (s.getHeight() * .3), confirmButton.width, confirmButton.height, g);

        // draw bet area
        g.draw(betArea);
        g.setColor(Color.GREEN);
        g.setFont(new Font("Monospaced", Font.PLAIN, 32));
        ClientUtils.drawCenteredString("Confirm",
                confirmButton.x, confirmButton.y, confirmButton.width, confirmButton.height, g);
        // drawn button for confirming choices
        g.draw(confirmButton);
        // display to the user what they should do
        g.setColor(Color.WHITE);
        if (currentState == State.BET) {
            ClientUtils.drawCenteredString(String.format("Bet(1-%s): %s", maxBet, currentBet), confirmButton.x,
                    (int) (s.getHeight() * .55), confirmButton.width, confirmButton.height, g);
        } else if (currentState == State.GUESS) {
            ClientUtils.drawCenteredString(String.format("Guess(1-%s): %s", maxGuess, currentGuess), confirmButton.x,
                    (int) (s.getHeight() * .55), confirmButton.width, confirmButton.height, g);
        }
    }

    private void drawEndGame(Graphics2D g) {
        Player winner = players.get(lastWinner);
        Dimension s = self.getSize();
        g.setColor(Color.WHITE);

        // define button for confirming choices
        confirmButton.setRect(s.getWidth() * .2f, s.getHeight() * .8f, (s.getWidth() * .8f) - (s.getWidth() * .2f),
                s.getHeight() * .1f);
        // show restart warning
        if (currentState == State.PENDING_RESTART) {
            ClientUtils.drawCenteredString(
                    String.format("Game will be restarting in %s seconds.", countdown),
                    confirmButton.x,
                    (int) (s.getHeight() * .3), confirmButton.width, confirmButton.height, g);
        }
        // show winner
        ClientUtils.drawCenteredString(
                String.format("%s is the winner with %s matter!", winner.getClientName(), winner.getMatter()),
                confirmButton.x,
                (int) (s.getHeight() * .4), confirmButton.width, confirmButton.height, g);
        // TODO: should probably have a way to hide it if this player chose "Yes"
        // show player action choice
        ClientUtils.drawCenteredString(String.format("Would you like to play again?"), confirmButton.x,
                (int) (s.getHeight() * .55), confirmButton.width, confirmButton.height, g);
        g.setColor(Color.GREEN);
        // lazy split of 1 button into two :)
        g.drawLine((int) confirmButton.getCenterX(), (int) confirmButton.getMinY(), (int) confirmButton.getCenterX(),
                (int) confirmButton.getMaxY());
        g.setFont(new Font("Monospaced", Font.PLAIN, 32));
        ClientUtils.drawCenteredString("<- Yes / No ->",
                confirmButton.x, confirmButton.y, confirmButton.width, confirmButton.height, g);
        g.draw(confirmButton);
    }

    private synchronized void processClientConnectionStatus(long clientId, String clientName, boolean isConnect) {
        if (isConnect) {
            if (!players.containsKey(clientId)) {
                logger.info(String.format("Adding %s[%s]", clientName, clientId));
                players.put(clientId, new Player(clientId, clientName));
            }
        } else {
            if (players.containsKey(clientId)) {
                logger.info(String.format("Removing %s[%s]", clientName, clientId));
                players.remove(clientId);
            }
            if (clientId == myId) {
                logger.info("I disconnected");
                myId = Constants.DEFAULT_CLIENT_ID;
            }
        }
        if(currentPhase == Phase.READY_CHECK){
            readyCheck();
        }
        self.repaint();
        logger.info("Clients in room: " + players.size());
    }

    private void readyCheck(){
        int numReady = 0;
        synchronized (players) {
            Iterator<Player> iter = players.values().iterator();
            while (iter.hasNext()) {
                Player p = iter.next();
                if (p != null && p.isReady()) {
                    numReady++;
                }
            }
        }
        this.numReady = numReady;
    }
    // Although we must implement all of these methods, not all of them may be
    // applicable to this panel
    @Override
    public void onClientConnect(long id, String clientName, String formattedName, String message) {
        processClientConnectionStatus(id, clientName, true);

    }

    @Override
    public void onClientDisconnect(long id, String clientName, String message) {
        processClientConnectionStatus(id, clientName, false);

    }

    @Override
    public void onMessageReceive(long id, String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReceiveClientId(long id) {
        if (myId == Constants.DEFAULT_CLIENT_ID) {
            myId = id;
        } else {
            logger.warning("Received client id after already being set, this shouldn't happen");
        }

    }

    @Override
    public void onSyncClient(long id, String clientName, String formattedName) {
        processClientConnectionStatus(id, clientName, true);
    }

    @Override
    public void onResetUserList() {
        players.clear();
    }

    @Override
    public void onReceiveRoomList(String[] rooms, String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomJoin(String roomName) {
        if (roomName.equalsIgnoreCase(Constants.LOBBY)) {
            setVisible(false);
        } else {
            setVisible(true);
            //added during video
            isReady = false;
            numReady = 0;
            for(Player p : players.values()){
                p.setIsReady(false);
            }
        }

    }

    @Override
    public void onReceiveReady(long clientId) {
        Player p = players.get(clientId);
        if (p != null) {
            if (!p.isReady()) {
                p.setIsReady(true);
                if (clientId == myId) {
                    isReady = true;
                }
            }
        }
        readyCheck();
        self.repaint();
    }

    @Override
    public void onReceiveMatterUpdate(long clientId, long currentMatter) {
        // server trigger to change phase since this project doesn't have an official
        // "START" payload
        if (currentPhase == Phase.READY_CHECK) {
            currentPhase = Phase.ANTE;
        }
        if (myId == clientId) {
            // prevents the player from betting more than they have
            maxBet = Math.min(Constants.STARTING_MATTER, currentMatter);
        }
        Player p = players.get(clientId);
        if (p != null) {
            p.setMatter(currentMatter);
            self.repaint();
        }
    }

    @Override
    public void onReceiveTurn(long clientId, long maxGuess) {
        isMyTurn = clientId == myId;
        if (turnTimer != null) {
            turnTimer.cancel();
        }
        if (isMyTurn) {
            currentBet = 0;
            currentGuess = 0;
            currentState = State.BET;
            turnTimer = new Countdown("Turn", 30);
            turnTimer.setTickCallback((time) -> {
                countdown = time;
                self.repaint();
            });
            turnTimer.setExpireCallback(() -> {
                isMyTurn = false;
                self.repaint();
            });
        }
        this.maxGuess = maxGuess;
        self.repaint();
    }

    @Override
    public void onReceiveWinner(long clientId) {
        lastWinner = clientId;
        currentPhase = Phase.REVEAL;
        self.repaint();
    }

    @Override
    public void onReceiveRestart() {
        if (currentState != State.PENDING_RESTART) {
            currentState = State.PENDING_RESTART;
            countdown = 30;
            Countdown c = new Countdown("", countdown, () -> {
                isReady = false;
                numReady = 0;
                for(Player p : players.values()){
                    p.setIsReady(false);
                }
                currentPhase = Phase.READY_CHECK;
                self.repaint();
            });
            c.setTickCallback((time) -> {
                countdown = time;
                self.repaint();
            });
        }

    }

    @Override
    public void onReceiveCurrentPhase(Phase phase) {
        currentPhase = phase;
        self.repaint();
        
    }

}

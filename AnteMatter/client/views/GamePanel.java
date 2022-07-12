package AnteMatter.client.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JPanel;

import AnteMatter.client.Client;
import AnteMatter.client.ClientUtils;
import AnteMatter.client.IClientEvents;
import AnteMatter.common.Constants;
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

    private enum State {
        IDLE, BET, GUESS
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
                    // check if we're interacting with the proper rectangle
                    if (betArea.contains(e.getPoint())) {
                        logger.info(e.getPoint() + " " + betArea.getLocation() + " " + betArea.getMinX()
                                + " " + betArea.getMaxX());
                        // calculate position to generate a percentage
                        double x = e.getPoint().x - betArea.getMinX();
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
                    if (!isReady && readyButton.contains(e.getPoint())) {
                        // isReady = true;
                        try {
                            Client.INSTANCE.sendReady();
                        } catch (NullPointerException | IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        self.repaint();
                    }
                } else if (currentPhase == Phase.ANTE) {
                    if (confirmButton.contains(e.getPoint())) {
                        if (currentState == State.BET && currentBet > 0) {
                            currentState = State.GUESS;
                        } else if (currentState == State.GUESS && currentGuess > 0) {
                            // TODO send bet/guess
                            logger.info("Sending bet and guess");
                            try {
                                Client.INSTANCE.sendBetAndGuess(currentBet, currentGuess);
                                currentState = State.IDLE;
                            } catch (NullPointerException | IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        }
                        self.repaint();
                    }
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
                break;
            default:
                break;
        }

        /*
         * g.setColor(Color.YELLOW); // set the drawing color
         * g.drawLine(30, 40, 100, 200);
         * g.drawOval(150, 180, 10, 10);
         * g.drawRect(200, 210, 20, 30);
         * g.setColor(Color.RED); // change the drawing color
         * g.fillOval(300, 310, 30, 50);
         * g.fillRect((int)test.x, (int)test.y, 60, 50);
         * // Printing texts
         * g.setColor(Color.WHITE);
         * g.setFont(new Font("Monospaced", Font.PLAIN, 12));
         * g.drawString("Testing custom drawing ...", 10, 20);
         */

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
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ClientUtils.drawCenteredString("Drag mouse here \nto choose value",
                betArea.x, betArea.y, betArea.width, betArea.height, g);
        // drawn button for confirming choices
        confirmButton.setRect(s.getWidth() * .2f, s.getHeight() * .8f, (s.getWidth() * .8f) - (s.getWidth() * .2f),
                s.getHeight() * .1f);
        g.draw(betArea);
        g.setColor(Color.GREEN);
        g.setFont(new Font("Monospaced", Font.PLAIN, 32));
        ClientUtils.drawCenteredString("Confirm",
                confirmButton.x, confirmButton.y, confirmButton.width, confirmButton.height, g);
        g.draw(confirmButton);
        // display to the user what they should do
        g.setColor(Color.WHITE);
        if (currentState == State.BET) {
            ClientUtils.drawCenteredString(String.format("Bet(1-%s): %s", maxBet, currentBet), confirmButton.x,
                    (int) (s.getHeight() * .55), confirmButton.width, confirmButton.height, g);
        } else if (currentState == State.GUESS) {
            ClientUtils.drawCenteredString(String.format("Guess(1-%s): %s", maxGuess, currentGuess), confirmButton.x,
                    (int) (s.getHeight() * .4), confirmButton.width, confirmButton.height, g);
        }
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
        logger.info("Clients in room: " + players.size());
    }

    // Although we must implement all of these methods, not all of them may be
    // applicable to this panel
    @Override
    public void onClientConnect(long id, String clientName, String message) {
        processClientConnectionStatus(id, clientName, true);

    }

    @Override
    public void onClientDisconnect(long id, String clientName, String message) {
        processClientConnectionStatus(id, clientName, true);

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
    public void onSyncClient(long id, String clientName) {
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
        if (roomName.equalsIgnoreCase("lobby")) {
            setVisible(false);
        } else {
            setVisible(true);
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
                numReady++;
                self.repaint();
            }
        }
        // TODO in the future adjust when game starts
        // this is just for example sake
        if (numReady >= players.size()) {
            currentPhase = Phase.ANTE;
            self.repaint();
        }
    }

    @Override
    public void onReceiveMatterUpdate(long clientId, long currentMatter) {
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
        if (isMyTurn) {
            currentBet = 0;
            currentGuess = 0;
            currentState = State.BET;
        }
        this.maxGuess = maxGuess;
        self.repaint();
    }

}

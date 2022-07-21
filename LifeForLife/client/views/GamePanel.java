package LifeForLife.client.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import LifeForLife.client.Client;
import LifeForLife.client.ClientUtils;
import LifeForLife.client.IClientEvents;
import LifeForLife.common.Constants;
import LifeForLife.common.MyLogger;
import LifeForLife.common.Phase;
import LifeForLife.common.Player;
import LifeForLife.common.ProjectilePool;
import LifeForLife.common.Throttle;
import LifeForLife.common.Vector2;

public class GamePanel extends JPanel implements IClientEvents {

    private boolean isReady = false;
    private Phase currentPhase = Phase.READY_CHECK;
    private Hashtable<Long, Player> players = new Hashtable<Long, Player>();
    private long myId = Constants.DEFAULT_CLIENT_ID;
    int numReady = 0;
    private Player myPlayer = null;

    private Rectangle readyButton = new Rectangle();

    // private static Logger logger = Logger.getLogger(GamePanel.class.getName());
    private static MyLogger logger = MyLogger.getLogger(GamePanel.class.getName());
    GamePanel self;
    Thread drawLoop = null;
    Thread inputThread = null;
    Throttle clientSendThrottle = new Throttle(8);
    Point mp = new Point();
    private boolean isRunning = false;
    private ProjectilePool projectilePool = new ProjectilePool();

    // inner class keystates
    abstract class KeyStates {
        public static boolean W = false;
        public static boolean S = false;
        public static boolean A = false;
        public static boolean D = false;
    }

    // inner class keyboard action
    private final class KeyboardAction extends AbstractAction {
        int key = -1;
        boolean pressed = false;

        public KeyboardAction(int key, boolean pressed) {
            this.key = key;
            this.pressed = pressed;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // logger.info("Key pressed: " + key);
            switch (key) {
                case KeyEvent.VK_W:
                    KeyStates.W = pressed;
                    break;
                case KeyEvent.VK_S:
                    KeyStates.S = pressed;
                    break;
                case KeyEvent.VK_A:
                    KeyStates.A = pressed;
                    break;
                case KeyEvent.VK_D:
                    KeyStates.D = pressed;
                    break;
            }

        }

    }

    public GamePanel() {
        self = this;
        Client.INSTANCE.addCallback(this);
        this.setFocusable(true);
        this.setRequestFocusEnabled(true);
        this.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentPhase == Phase.BATTLE) {
                    if (myPlayer != null) {
                        mp = e.getPoint();
                        myPlayer.lookAtPoint(mp.x, mp.y);
                        sendHeadingAndRotation();

                    }
                }
            }
        });
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                self.grabFocus();

                // logger.info(
                // String.format("Mouse info LOC %s Point %s", e.getLocationOnScreen(),
                // e.getPoint()));
                if (currentPhase == Phase.READY_CHECK) {
                    // get point is relative to source
                    if (!isReady && readyButton.contains(e.getPoint())) {
                        // isReady = true;
                        try {
                            Client.INSTANCE.sendReady();
                        } catch (NullPointerException | IOException e1) {
                            e1.printStackTrace();
                            logger.severe(e1.getMessage());
                        }
                        self.repaint();
                    }
                } 
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (currentPhase == Phase.BATTLE) {
                    if (isReady && myPlayer != null) {
                        boolean canShoot = myPlayer.canShoot();
                        if (canShoot) {
                            try {
                                Client.INSTANCE.sendShoot();
                            } catch (NullPointerException | IOException e1) {
                                e1.printStackTrace();
                                logger.severe(e1.getMessage());
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
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
            case BATTLE:
                drawField(g2);
                projectilePool.draw(g2);
                break;
            case END_GAME:
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

    private void drawField(Graphics2D g) {

        // debug draw border for panel size
        g.setColor(Color.YELLOW);
        g.drawRect(0, 0, getSize().width, getSize().height);
        // draw border for arena size
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, 800, 600);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));

        for (Player p : players.values()) {
            p.draw(g);
        }
    }

    private void sendHeadingAndRotation() {
        if (currentPhase == Phase.BATTLE && clientSendThrottle.ready() && myPlayer != null) {
            try {
                Client.INSTANCE.sendHeadingAndRotation(myPlayer.getHeading(), myPlayer.getRotation());
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
                logger.severe("Error sending transform data: " + e.getMessage());
                isRunning = false;
            }
        }
    }

    public void attachListeners() {
        isRunning = true;
        InputMap im = self.getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "up_pressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "up_released");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "down_pressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "down_released");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "left_pressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "left_released");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "right_pressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "right_released");

        ActionMap am = self.getActionMap();
        am.put("up_pressed", new KeyboardAction(KeyEvent.VK_W, true));
        am.put("up_released", new KeyboardAction(KeyEvent.VK_W, false));
        am.put("down_pressed", new KeyboardAction(KeyEvent.VK_S, true));
        am.put("down_released", new KeyboardAction(KeyEvent.VK_S, false));
        am.put("left_pressed", new KeyboardAction(KeyEvent.VK_A, true));
        am.put("left_released", new KeyboardAction(KeyEvent.VK_A, false));
        am.put("right_pressed", new KeyboardAction(KeyEvent.VK_D, true));
        am.put("right_released", new KeyboardAction(KeyEvent.VK_D, false));
        inputThread = new Thread() {
            @Override
            public void run() {
                logger.info("GamePanel thread started");
                Vector2 localHeading = new Vector2(0, 0);
                while (self.isEnabled() && isRunning) {
                    if (myPlayer != null) {
                        Vector2 ch = myPlayer.getHeading();
                        localHeading.x = 0;
                        localHeading.y = 0;
                        // movement
                        if (KeyStates.W) {
                            localHeading.y = -1;
                        } else if (KeyStates.S) {
                            localHeading.y = 1;
                        }
                        if (KeyStates.A) {
                            localHeading.x = -1;
                        } else if (KeyStates.D) {
                            localHeading.x = 1;
                        }
                        boolean changed = localHeading != ch;//
                        myPlayer.lookAtPoint(mp.x, mp.y);
                        // logger.info("Local heading: " + localHeading + " ch: " + ch);
                        if (changed) {
                            myPlayer.setHeading(localHeading);
                            // logger.info("Changed heading: " + myPlayer.getHeading());
                            sendHeadingAndRotation();
                        }
                    }
                    try {
                        Thread.sleep(16);// simulate 60 fps
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                logger.info("GamePanel thread exited");
            }
        };
        inputThread.start();
    }

    private synchronized void processClientConnectionStatus(long clientId, String clientName, String formattedName, boolean isConnect) {
        if (isConnect) {
            if (!players.containsKey(clientId)) {
                logger.info(String.format("Adding %s[%s]", clientName, clientId));
                players.put(clientId, new Player(clientId, clientName, formattedName));
                if (clientId == myId) {
                    myPlayer = players.get(clientId);
                }
            }
        } else {
            if (players.containsKey(clientId)) {
                logger.info(String.format("Removing %s[%s]", clientName, clientId));
                players.remove(clientId);
            }
            if (clientId == myId) {
                logger.info("I disconnected");
                myId = Constants.DEFAULT_CLIENT_ID;
                myPlayer = null;
            }
        }
        logger.info("Clients in room: " + players.size());
    }

    // Although we must implement all of these methods, not all of them may be
    // applicable to this panel
    @Override
    public void onClientConnect(long id, String clientName, String formattedName, String message) {
        processClientConnectionStatus(id, clientName, formattedName, true);

    }

    @Override
    public void onClientDisconnect(long id, String clientName, String message) {
        processClientConnectionStatus(id, clientName, null, false);

    }

    @Override
    public void onMessageReceive(long id, String message) {

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
        processClientConnectionStatus(id, clientName, formattedName, true);
    }

    @Override
    public void onResetUserList() {
        players.clear();
    }

    @Override
    public void onReceiveRoomList(String[] rooms, String message) {

    }

    @Override
    public void onRoomJoin(String roomName) {
        if (roomName.equalsIgnoreCase(Constants.LOBBY)) {
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
    }

    @Override
    public void onReceiveLifeUpdate(long clientId, long currentLife) {
        Player p = players.get(clientId);
        if (p != null) {
            p.setLife(currentLife);
            // self.repaint();//not needed anymore as of the addition of the drawing loop
        }
    }

    /**
     * Creates a separate thread for drawing if the thread doesn't exist
     */
    @Override
    public void onReceiveStart() {
        if (drawLoop == null) {
            drawLoop = new Thread() {
                @Override
                public void run() {
                    currentPhase = Phase.BATTLE;
                    attachListeners();
                    while (currentPhase == Phase.BATTLE && isRunning) {
                        self.repaint();
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            drawLoop.start();
        }
    }

    @Override
    public void onReceivePositionAndRotation(long clientId, Vector2 position, Vector2 heading, float rotation) {
        Player p = players.get(clientId);
        if (p != null) {
            p.setPosition(position);
            p.setRotation(rotation);
            p.setHeading(heading);
        }
    }

    @Override
    public void onReceiveProjectileSync(long clientId, long projectileId, Vector2 position, Vector2 heading, long life,
            int speed) {
        projectilePool.syncProjectile(clientId, projectileId, position, heading, life, speed);
    }

}

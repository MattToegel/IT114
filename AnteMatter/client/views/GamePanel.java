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
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import AnteMatter.client.Client;
import AnteMatter.client.ClientUtils;
import AnteMatter.client.IClientEvents;
import AnteMatter.common.Constants;
import AnteMatter.common.Phase;
import AnteMatter.common.Player;

public class GamePanel extends JPanel implements IClientEvents {

    private boolean isReady = false;
    private Phase currentPhase = Phase.READY_CHECK;
    private Hashtable<Long, Player> players = new Hashtable<Long, Player>();
    private long myId = Constants.DEFAULT_CLIENT_ID;
    int numReady = 0;

    private Rectangle readyButton = new Rectangle();

    private static Logger logger = Logger.getLogger(GamePanel.class.getName());
    GamePanel self;

    public GamePanel() {
        self = this;
        Client.INSTANCE.addCallback(this);
        this.setFocusable(true);
        this.setRequestFocusEnabled(true);
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                self.grabFocus();

                logger.log(Level.INFO,
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
        switch(currentPhase){
            case READY_CHECK:
                drawReadyCheck(g2);
                break;
            case ANTE:
                drawBoard(g2);
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
        // logger.log(Level.INFO, "Panel size: " + s);
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
    private void drawBoard(Graphics2D g){
        Dimension s = self.getSize();
        g.setFont(new Font("Monospaced", Font.PLAIN, 32));
        int i = 0;
        for(Player p : players.values()){
            int offset = (int)(s.getHeight() * .05f) * (i+1);
            ClientUtils.drawCenteredString(p.getClientName() + "(" + p.getMatter() + ")", 0, offset,
            (int)s.getWidth(), (int)s.getHeight(), g);
            i++;
        }
    }

    private synchronized void processClientConnectionStatus(long clientId, String clientName, boolean isConnect) {
        if (isConnect) {
            if (!players.containsKey(clientId)) {
                logger.log(Level.INFO, String.format("Adding %s[%s]", clientName, clientId));
                players.put(clientId, new Player(clientId, clientName));
            }
        } else {
            if (players.containsKey(clientId)) {
                logger.log(Level.INFO, String.format("Removing %s[%s]", clientName, clientId));
                players.remove(clientId);
            }
            if (clientId == myId) {
                logger.log(Level.INFO, "I disconnected");
                myId = Constants.DEFAULT_CLIENT_ID;
            }
        }
        logger.log(Level.INFO, "Clients in room: " + players.size());
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
            logger.log(Level.WARNING, "Received client id after already being set, this shouldn't happen");
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
        //TODO in the future adjust when game starts
        //this is just for example sake
        if(numReady >= players.size()){
            currentPhase = Phase.ANTE;
            self.repaint();
        }
    }

    @Override
    public void onReceiveMatterUpdate(long clientId, long currentMatter) {
        Player p = players.get(clientId);
        if (p != null) {
            p.setMatter(currentMatter);
            self.repaint();
        }
    }

}

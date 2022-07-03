package Module7.Part10.client.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import Module7.Part10.client.IClientEvents;

public class GamePanel extends JPanel implements IClientEvents {
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
            logger.log(Level.INFO, "Key pressed: " + key);
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

    Point test = new Point(0, 0);
    int speed = 5;
    private static Logger logger = Logger.getLogger(GamePanel.class.getName());
    GamePanel self;

    public GamePanel() {
        self = this;
        this.setFocusable(true);
        this.setRequestFocusEnabled(true);
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                self.grabFocus();
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
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Your custom painting codes. For example,
        // Drawing primitive shapes

        g.setColor(Color.YELLOW); // set the drawing color
        g.drawLine(30, 40, 100, 200);
        g.drawOval(150, 180, 10, 10);
        g.drawRect(200, 210, 20, 30);
        g.setColor(Color.RED); // change the drawing color
        g.fillOval(300, 310, 30, 50);
        g.fillRect((int)test.x, (int)test.y, 60, 50);
        // Printing texts
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString("Testing custom drawing ...", 10, 20);

    }

    public void attachListeners() {
        InputMap im = this.getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "up_pressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "up_released");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "down_pressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "down_released");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "left_pressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "left_released");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "right_pressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "right_released");

        ActionMap am = this.getActionMap();
        am.put("up_pressed", new KeyboardAction(KeyEvent.VK_W, true));
        am.put("up_released", new KeyboardAction(KeyEvent.VK_W, false));
        am.put("down_pressed", new KeyboardAction(KeyEvent.VK_S, true));
        am.put("down_released", new KeyboardAction(KeyEvent.VK_S, false));
        am.put("left_pressed", new KeyboardAction(KeyEvent.VK_A, true));
        am.put("left_released", new KeyboardAction(KeyEvent.VK_A, false));
        am.put("right_pressed", new KeyboardAction(KeyEvent.VK_D, true));
        am.put("right_released", new KeyboardAction(KeyEvent.VK_D, false));

        Thread t = new Thread() {
            @Override
            public void run() {
                logger.log(Level.INFO, "GamePanel thread started");
                
                while (self.isEnabled()) {
                   
                    // movement
                    if (KeyStates.W) {
                        test.y -= speed;
                    } else if (KeyStates.S) {
                        test.y += speed;
                    }
                    if (KeyStates.A) {
                        test.x -= speed;
                    } else if (KeyStates.D) {
                        test.x += speed;
                    }

                    self.repaint();
                    try {
                        Thread.sleep(16);// simulate 60 fps
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                logger.log(Level.INFO, "GamePanel thread exited");
            }
        };
        t.start();
    }

    // Although we must implement all of these methods, not all of them may be
    // applicable to this panel
    @Override
    public void onClientConnect(long id, String clientName, String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClientDisconnect(long id, String clientName, String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMessageReceive(long id, String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReceiveClientId(long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSyncClient(long id, String clientName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResetUserList() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReceiveRoomList(String[] rooms, String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomJoin(String roomName) {
        // TODO Auto-generated method stub

    }

}

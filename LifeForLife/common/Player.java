package LifeForLife.common;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import LifeForLife.client.ClientUtils;
import LifeForLife.server.ServerThread;

public class Player {
    private long life = 0;
    private int movement_speed = 5;
    private Vector2 heading = new Vector2(0, 0);
    private Vector2 pHeading = new Vector2(0, 0);// used to check if heading changed
    private Vector2 position = new Vector2(0, 0);
    private float rotation = 0;
    private float pRotation = 0;// used to check if rotation changed
    private boolean ready = false;
    private long clientId = Constants.DEFAULT_CLIENT_ID;
    private String clientName = "";
    private ServerThread serverThread;
    private Shape player;// drawable shape reference
    // private static Logger logger = Logger.getLogger(Player.class.getName());
    private static MyLogger logger = MyLogger.getLogger(Player.class.getName());

    /** Server-side constructor */
    public Player(ServerThread st) {
        this.serverThread = st;
        this.clientId = serverThread.getClientId();
        this.clientName = serverThread.getClientName();
    }

    public ServerThread getClient() {
        return serverThread;
    }

    /** client-side constructor */
    public Player(long clientId, String clientName) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.player = new Ellipse2D.Float(0, 0, Constants.PLAYER_SIZE, Constants.PLAYER_SIZE);

    }

    public long getClientId() {
        return clientId;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setIsReady(boolean isReady) {
        this.ready = isReady;
    }

    public boolean isReady() {
        return this.ready;
    }

    public void setLife(long life) {
        this.life = life;
    }

    public long getLife() {
        return life;
    }

    public void modifyLife(long change) {
        life += change;
    }

    public void draw(Graphics2D g) {
        // create a copy of Graphics2D for easier transformation
        Graphics2D g2d = (Graphics2D) g.create();
        AffineTransform at = AffineTransform.getTranslateInstance(position.x, position.y);
        at.rotate(Math.toRadians(rotation), player.getBounds2D().getCenterX(), player.getBounds2D().getCenterY());
        g2d.setTransform(at);
        // example bounding box (will be removed later likely)
        g2d.setColor(Color.BLUE);
        g2d.drawRect(
                (int) player.getBounds2D().getX(),
                (int) player.getBounds2D().getY(),
                (int) player.getBounds2D().getWidth(),
                (int) player.getBounds2D().getHeight());
        // player color (will be made into teams later)
        g2d.setColor(Color.RED);
        g2d.fill(player);
        // player outline (undecided what I'll do with this, maybe it'll become a team
        // id instead of fill)
        g2d.setColor(Color.GREEN);
        g2d.draw(player);
        // line showing direction of looking
        g2d.drawLine(
                (int) player.getBounds2D().getCenterX(),
                (int) player.getBounds2D().getCenterY(), (int) player.getBounds2D().getCenterX(),
                (int) (player.getBounds2D().getCenterY() + player.getBounds2D().getHeight() / 2));
        // cleanup copy of Graphics2D
        g2d.dispose();
        // draw name and life on original Graphics2D
        g.setColor(Color.WHITE);
        ClientUtils.drawCenteredString(getClientName(), position.x, position.y - Constants.PLAYER_SIZE,
                Constants.PLAYER_SIZE,
                Constants.PLAYER_SIZE, g);

        ClientUtils.drawCenteredString(getLife() + "", position.x, position.y, Constants.PLAYER_SIZE,
                Constants.PLAYER_SIZE, g);
    }

    /**
     * Rotates Player towards coordinate.
     * https://stackoverflow.com/questions/16133822/java-rotating-image-so-that-it-points-at-the-mouse-cursor
     * 
     * @param x
     * @param y
     * @return true or false depending if there was a chance since last rotation
     */
    public boolean lookAtPoint(int x, int y) {
        int deltaX = x - position.x;
        int deltaY = y - position.y;

        rotation = (float) -Math.atan2(deltaX, deltaY);

        rotation = (float) Math.toDegrees(rotation);// + 180;
        // https://mkyong.com/java/how-to-round-double-float-value-to-2-decimal-points-in-java/
        // rotation = (float)(Math.round(rotation * 1000.0) / 1000.0);//3 decimal
        // precision
        rotation = (float) (Math.round(rotation * 10.0) / 10.0);
        boolean changed = rotation != pRotation;
        if (changed) {
            pRotation = rotation;
        }
        return changed;
    }

    public void setPosition(Vector2 p) {
        this.position.x = p.x;
        this.position.y = p.y;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void move(Rectangle arenaSize) {
        this.position.x += heading.x * movement_speed;
        this.position.y += heading.y * movement_speed;
        logger.fine(String.format("%s new position %s,%s", getClientName(), position.x, position.y));
        this.position.x = GeneralUtils.clamp(this.position.x, (int) arenaSize.getMinX(),
                (int) arenaSize.getMaxX() - Constants.PLAYER_SIZE);
        this.position.y = GeneralUtils.clamp(this.position.y, (int) arenaSize.getMinY(), (int) arenaSize.getMaxY());
    }

    public void setRotation(float r) {
        this.rotation = r;
        // Note: This is only necessary if rotation dictated movement direction
        // https://www.gamedev.net/forums/topic/47069-how-to-convert-an-angle-to-a-2d-vector/1231692
        // this.heading.x = (int)Math.round(Math.sin(r));
        // this.heading.y = (int)Math.round(Math.cos(r));
    }

    public boolean setHeading(Vector2 heading) {
        this.heading.x = GeneralUtils.clamp(heading.x, -1, 1);
        this.heading.y = GeneralUtils.clamp(heading.y, -1, 1);
        boolean changed = false;
        if (this.heading.x != this.pHeading.x || this.heading.y != this.pHeading.y) {
            changed = true;
            pHeading.x = heading.x;
            pHeading.y = heading.y;
        }
        return changed;
    }

    public Vector2 getHeading() {
        return heading;
    }

    public float getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        return String.format("Player[%s], P[%s], H[%s], R[%s]", getClientName(), getPosition(), getHeading(),
                getRotation());
    }
}

package LifeForLife.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import LifeForLife.client.ClientUtils;
import LifeForLife.server.HitData;

public class Projectile {
    private int size = 10;
    private Vector2 position = new Vector2(0, 0);
    private Vector2 lastPosition = new Vector2(0, 0);
    private Vector2 direction = new Vector2(0, 0);
    private Color color = Color.WHITE;
    private long life = 1;
    private int speed = 10;
    private boolean didHit = true;
    private long relatedClientId = Constants.DEFAULT_CLIENT_ID;
    private Countdown lifetime = null;
    private long projectileId;
    private boolean hasPendingUpdate = true;
    private static MyLogger logger = MyLogger.getLogger(Projectile.class.getName());

    public Projectile(long projectileId) {
        this.projectileId = projectileId;
        disable();
    }
    public void setColor(Color color){
        this.color = color;
    }
    public boolean hasPendingUpdate(){
        return hasPendingUpdate;
    }
    public void resetPendingUpdate(){
        hasPendingUpdate = false;
    }
    public void setPendingUpdate(){
        hasPendingUpdate = true;
    }
    public long getClientId() {
        return relatedClientId;
    }

    public long getProjectileId() {
        return projectileId;
    }

    public int getSize() {
        return size;
    }
    public boolean isActive(){
        return position.x > -10_000;
    }
    public void disable() {
        clearTimer();
        color = Color.WHITE;
        position.x = -10_000;// off screen
        direction.x = 0;
        direction.y = 0;
        didHit = true;
        speed = 0;
        relatedClientId = Constants.DEFAULT_CLIENT_ID;
        setPendingUpdate();
    }

    private void clearTimer() {
        if (lifetime != null) {
            lifetime.cancel();
        }
    }

    private void startTimer() {
        clearTimer();
        // TODO: this will create a reasonable about of GC work
        // it's best to refactor Countdown.java to be able to reset the state
        lifetime = new Countdown(this + "_lifetime", 3, () -> {
            // when it expires, make projectile pickupable
            speed = 0;
            setPendingUpdate();
        });
    }

    public void setData(long clientId, Vector2 position, Vector2 heading, long life) {
        didHit = false;
        speed = 10;// default speed
        this.life = life;
        relatedClientId = clientId;
        this.position.x = position.x;
        this.position.y = position.y;
        this.direction.x = heading.x;
        this.direction.y = heading.y;
        startTimer();
    }

    public void syncData(Vector2 position, Vector2 heading, long life, int speed) {
        this.position.x = position.x;
        this.position.y = position.y;
        this.direction.x = heading.x;
        this.direction.y = heading.y;
        this.life = life;
        this.speed = speed;
        if (!isActive()) {
            disable();
        }
    }

    /**
     * Uses a mutex to only trigger a hit once in case it's checked multiple times
     * before it's disabled
     * 
     * @return returns the shooter id if it's a fresh hit, otherwise
     *         DEFAULT_CLIENT_ID to be ignored
     */
    public HitData hit() {
        if (!didHit) {
            HitData hd = new HitData();
            hd.sourceClientId = relatedClientId;
            hd.life = life;
            hd.didPickup = speed == 0;
            disable();
            return hd;
        }
        return null;
    }

    public boolean didHit() {
        return didHit;
    }

    public boolean move(Rectangle arenaSize) {
        if (didHit || speed <= 0) {
            return false;
        }
        lastPosition.x = position.x;
        lastPosition.y = position.y;
        position.x += direction.x * speed;
        position.y += direction.y * speed;
        
        //adjust x bounds
        if (this.position.x < arenaSize.getMinX()) {
            this.position.x = (float) arenaSize.getMinX();
            direction.x *= -1;// bounce horizontal
        } else if (this.position.x + size > arenaSize.getMaxX()) {
            this.position.x = (float) arenaSize.getMaxX() - size;
            direction.x *= -1;// bounce horizontal
        }
        // adjust y bounds
        if (this.position.y < arenaSize.getMinY()) {
            this.position.y = (float) arenaSize.getMinY();
            direction.y *= -1;// bounce vertical
        } else if (this.position.y + size > arenaSize.getMaxY()) {
            this.position.y = (float) arenaSize.getMaxY() - size;
            direction.y *= -1;// bounce vertical
        }
        if(lastPosition.x != position.x || lastPosition.y != position.y){
            setPendingUpdate();
            return true;
        }
        return false;
    }

    public void draw(Graphics2D g) {
        if (!isActive()) {
            return;
        }
        // create a copy of Graphics2D for easier transformation
        g.setColor(color);
        if (speed <= 0) {// pickup indicator
            g.setColor(Color.LIGHT_GRAY);
            g.fillOval((int)position.x-1, (int)position.y-1, size+1, size+1);
        }
        g.fillOval((int)position.x, (int)position.y, size, size);
        
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ClientUtils.drawCenteredString(life + "", (int)position.x, (int)position.y, size, size, g);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getHeading() {
        return direction;
    }

    public long getLife() {
        return life;
    }

    public int getSpeed() {
        return speed;
    }
    @Override
    public String toString(){
        return String.format("Projectile[%s] P[%s] D[%s] Speed[%s]", projectileId, position, direction, speed);
    }
}

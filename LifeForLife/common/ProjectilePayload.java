package LifeForLife.common;

public class ProjectilePayload extends Payload {
    private long projectileId;
    private Vector2 position = new Vector2(0, 0);
    private Vector2 heading = new Vector2(0, 0);
    private long life;
    private int speed;
    

    /**
     * @return the speed
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * @return the projectileId
     */
    public long getProjectileId() {
        return projectileId;
    }

    /**
     * @param projectileId the projectileId to set
     */
    public void setProjectileId(long projectileId) {
        this.projectileId = projectileId;
    }

    /**
     * @return the position
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(Vector2 position) {
        this.position.x = position.x;
        this.position.y = position.y;
    }

    /**
     * @return the heading
     */
    public Vector2 getHeading() {
        return heading;
    }

    /**
     * @param heading the heading to set
     */
    public void setHeading(Vector2 heading) {
        this.heading.x = heading.x;
        this.heading.y = heading.y;
    }

    /**
     * @return the life
     */
    public long getLife() {
        return life;
    }

    /**
     * @param life the life to set
     */
    public void setLife(long life) {
        this.life = life;
    }

    
}

package LifeForLife.common;

/**
 * Controls shooting mechanics
 */
public class Shooter {
    Throttle shot = new Throttle(500);
    ProjectilePool projectilePool;
    public void setProjectilePool(ProjectilePool pp){
        this.projectilePool = pp;
    }
    public Projectile shoot(long clientId, Vector2 position, Vector2 direction, long life) {

        if (canShoot() && projectilePool != null) {
            Projectile p = projectilePool.spawn();
            if (p != null) {
                p.setData(clientId, position, direction, life);
                return p;
            }
        }
        return null;
    }
    public boolean canShoot(){
        return shot.ready();
    }
}
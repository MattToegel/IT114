package LifeForLife.common;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import LifeForLife.server.HitData;

public class ProjectilePool {
    

    private List<Projectile> projectiles = new ArrayList<Projectile>();
    private int preload = 0;
    private long nextProjectileId = 0;
    //private Consumer<Projectile> syncCallback;
    private Consumer<HitData> collisionCallback;
    private static MyLogger logger = MyLogger.getLogger(ProjectilePool.class.getName());

    /*public void setSyncCallback(Consumer<Projectile> callback) {
        syncCallback = callback;
    }*/
    public ProjectilePool(){

    }
    public ProjectilePool(int preload){
        init(preload);
    }

    public void setCollisionCallback(Consumer<HitData> callback) {
        collisionCallback = callback;
    }

    public void init(int preload) {
        synchronized (this) {
            if (preload > 0) {
                this.preload = preload;
                for (int i = 0; i < preload; i++) {
                    addProjectile();
                }
            }
        }
    }

    private Projectile addProjectile() {
        nextProjectileId++;
        if (nextProjectileId < 0) {
            nextProjectileId = 1;
        }
        Projectile p = new Projectile(nextProjectileId);
        projectiles.add(p);
        return p;
    }

    private Projectile addProjectile(long pid) {
        Projectile p = new Projectile(pid);
        projectiles.add(p);
        return p;
    }

    /**
     * Server-side spawn
     * 
     * @return
     */
    public synchronized Projectile spawn() {
        synchronized (projectiles) {
            // find a projectile not in use to reuse
            Projectile p = projectiles.stream().filter((t) -> !t.isActive()).findFirst().orElse(null);
            if (p == null) {
                p = addProjectile();
            }
            p.setPendingUpdate();
            return p;
        }
    }

    // client-side spawn from server
    public synchronized Projectile spawn(long pid) {
        synchronized (projectiles) {
            Projectile p = projectiles.stream().filter((t) -> t.getProjectileId() == pid).findFirst().orElse(null);
            if (p == null) {
                p = addProjectile(pid);
            }
            return p;
        }
    }
    /**
     * Client-side sync
     * @param clientId
     * @param pid
     * @param position
     * @param heading
     * @param life
     * @param speed
     */
    public synchronized void syncProjectile(long clientId, long pid, Vector2 position, Vector2 heading, long life,
            int speed, Color color) {
        Projectile p = projectiles.stream().filter((t) -> t.getProjectileId() == pid).findFirst().orElse(null);
        if (p == null) {
            p = addProjectile(pid);
            p.setData(clientId, position, heading, life);
            // logger.info("Projectile add: " + p);
        } else {
            p.syncData(position, heading, life, speed);
            // logger.info("Projectile sync: " + p);
        }
        p.setColor(color);

    }

    public void reset() {
        if (projectiles.size() > preload) {
            synchronized (projectiles) {
                while (projectiles.size() > preload) {
                    int s = projectiles.size();
                    if (s - 1 >= 0) {
                        projectiles.remove(s - 1);
                    }
                }
            }
        }
        synchronized(projectiles){
            for(Projectile p : projectiles){
                p.disable();
            }
        }
    }

    public void move(Rectangle arena) {
        synchronized (projectiles) {
            List<Projectile> activeProjectiles = getActiveProjectiles();
            for (int i = 0, l = activeProjectiles.size(); i < l; i++) {
                Projectile p = activeProjectiles.get(i);
                if (p.isActive() && p.getSpeed() > 0) {
                    if (p.move(arena)) {
                        /*if (syncCallback != null) {
                            syncCallback.accept(p);
                        }*/
                    }
                }
            }
        }
    }

    public void checkCollision(Player player) {
        if (player.getLife() <= 0 || !player.isReady()) {
            return;
        }
        Vector2 playerPosition = new Vector2(player.getPosition());
        //convert to center
        playerPosition.x += Constants.PLAYER_SIZE*.5f;
        playerPosition.y += Constants.PLAYER_SIZE*.5f;

        Vector2 projectilePosition = new Vector2(0,0);
        synchronized (projectiles) {
            List<Projectile> activeProjectiles = getActiveProjectiles();
            for (int i = 0, l = activeProjectiles.size(); i < l; i++) {
                Projectile p = activeProjectiles.get(i);
                if (p.isActive() && !p.didHit()) {
                    if(p.getSpeed() > 0 && p.getClientId() == player.getClientId()){
                        //ignore moving projectiles against "owner"
                        continue;
                    }
                    //get center of projectile
                    projectilePosition.x = p.getPosition().x + (p.getSize()*.5f);
                    projectilePosition.y = p.getPosition().y + (p.getSize()*.5f);
                    
                    float distSq = (projectilePosition.x - playerPosition.x) * (projectilePosition.x - playerPosition.x) +
                            (projectilePosition.y - playerPosition.y) * (projectilePosition.y - playerPosition.y);
                    float d = (Constants.PLAYER_SIZE*.5f) + (p.getSize()*.5f);
                    float d2 = d * d;
                    if (distSq <= d2) {
                        HitData hd = p.hit();
                        if (hd != null) {
                            hd.targetClientId = player.getClientId();
                            /*if (syncCallback != null) {
                                syncCallback.accept(p);
                            }*/
                            if (collisionCallback != null) {
                                logger.fine("Collision data: " + hd);
                                collisionCallback.accept(hd);
                            }
                        }
                    }
                }
            }
        }
    }

    public void draw(Graphics2D g) {
        synchronized (projectiles) {
            for (int i = 0, l = projectiles.size(); i < l; i++) {
                Projectile p = projectiles.get(i);
                if (p.isActive()) {
                    p.draw(g);
                }
            }
        }
    }
    public List<Projectile> getPendingProjectiles(){
        return projectiles.stream().filter(p->p.hasPendingUpdate()).toList();
    }
    public List<Projectile> getActiveProjectiles(){
        return projectiles.stream().filter(p->p.isActive()).toList();
    }
}

package LifeForLife.server;

/**
 * Collision data to determine if it's a heal or hurt
 */
public class HitData {
    public long life;
    public long sourceClientId;
    public long targetClientId;
    public boolean didPickup;

    @Override
    public String toString() {
        return String.format("HitData client %s -> client %s type[%s] life[%s]",
                sourceClientId, targetClientId, (didPickup ? "pickup" : "hit"), life);
    }
}

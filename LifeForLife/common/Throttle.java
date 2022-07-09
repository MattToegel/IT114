package LifeForLife.common;

public class Throttle {
    private long lastTick;
    private long msDelay = 100;

    /**
     * Use to prevent an action from occuring more frequently than the delay in
     * milliseconds
     * 
     * @param msDelay
     */
    public Throttle(long msDelay) {
        if (msDelay > 0) {
            this.msDelay = msDelay;
        } else {
            this.msDelay = 10;
        }
    }

    public boolean ready() {
        long current = System.currentTimeMillis();
        if (current >= lastTick) {
            lastTick = current + msDelay;
            return true;
        }
        return false;
    }
}

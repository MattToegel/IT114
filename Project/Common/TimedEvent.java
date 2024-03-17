package Project.Common;

/* Originally based off of https://gist.github.com/MattToegel/c55747f26c5092d6362678d5b1729ec6 */

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * Simple countdown timer demo of java.util.Timer facility.
 * Formerly called Countdown
 */

public class TimedEvent {
    private int secondsRemaining;
    private Runnable expireCallback = null;
    private Consumer<Integer> tickCallback = null;
    final private Timer timer;

    /**
     * Create a TimedEvent to trigger the passed in callback after a set duration
     * 
     * @param durationInSeconds
     * @param callback
     */
    public TimedEvent(int durationInSeconds, Runnable callback) {
        this(durationInSeconds);
        this.expireCallback = callback;
    }

    /**
     * Create a TimedEvent to trigger after a set duration.
     * Note: Requires expireCallback and/or tickCallback to be set otherwise it'll
     * do nothing
     * 
     * @param durationInSeconds
     */
    public TimedEvent(int durationInSeconds) {
        timer = new Timer();
        secondsRemaining = durationInSeconds;
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                secondsRemaining--;
                if (tickCallback != null) {
                    tickCallback.accept(secondsRemaining);
                }
                if (secondsRemaining <= 0) {
                    timer.cancel();
                    secondsRemaining = 0;
                    if (expireCallback != null) {
                        expireCallback.run();
                    }
                }
            }
        }, 1000, 1000);
    }

    /**
     * Set a method to be called every timer tick; it'll receive the current time of
     * the timer.
     * 
     * @param callback
     */
    public void setTickCallback(Consumer<Integer> callback) {
        tickCallback = callback;
    }

    /**
     * Set a method to be called when the timer expires
     * 
     * @param callback
     */
    public void setExpireCallback(Runnable callback) {
        expireCallback = callback;
    }

    /**
     * Removes all callback references and cancels the timer
     */
    public void cancel() {
        expireCallback = null;
        tickCallback = null;
        timer.cancel();
    }

    /**
     * Used to override the remaining countdown durationInSeconds
     */
    public void setDurationInSeconds(int d) {
        secondsRemaining = d;
    }

    public int getRemainingTime() {
        return secondsRemaining;
    }

    /**
     * This is just for testing/demo
     * 
     * @param args
     */
    public static void main(String args[]) {
        TimedEvent cd = new TimedEvent(30, () -> {
            System.out.println("Time expired");
        });
        cd.setTickCallback((tick) -> {
            System.out.println("Tick: " + tick);
        });
    }
}

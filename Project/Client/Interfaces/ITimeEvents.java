package Project.Client.Interfaces;

import Project.Common.TimerType;

public interface ITimeEvents extends IClientEvents {
    /**
     * The current time of a timer
     * 
     * @param timerType The specifc timer
     * @param time      The time (use -1 to reset/cancel/stop)
     */
    void onTimerUpdate(TimerType timerType, int time);
}
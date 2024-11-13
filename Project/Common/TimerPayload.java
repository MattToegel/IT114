package Project.Common;

public class TimerPayload extends Payload {
    private int time;
    private TimerType timerType;

    public TimerPayload() {
        setPayloadType(PayloadType.TIME);
    }

    public int getTime() {
        return time;
    }

    public TimerType getTimerType() {
        return timerType;
    }

    public void setTimerType(TimerType timerType) {
        this.timerType = timerType;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
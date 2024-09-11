package Project.Common;

public class PointsPayload extends Payload {
    private int changedPoints, currentPoints;

    public int getChangedPoints() {
        return changedPoints;
    }

    public void setChangedPoints(int changedPoints) {
        this.changedPoints = changedPoints;
    }

    public int getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(int currentPoints) {
        this.currentPoints = currentPoints;
    }
}

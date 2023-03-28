package HNS.common;

public class Player {

    private boolean isReady = false;

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public boolean isReady() {
        return this.isReady;
    }

    private Cell currentCell = null;

    public void setCurrentCell(Cell c) {
        currentCell = c;
    }

    public Cell getCurrentCell() {
        return currentCell;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Is ready: %s", isReady));
        sb.append(String.format("Current Cell: %s", currentCell));
        return sb.toString();
    }
}

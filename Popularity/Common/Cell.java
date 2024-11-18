package Popularity.Common;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single cell in a grid.
 * The cell is defined by its row (x) and column (y) coordinates.
 */
public class Cell {
    private int x;
    private int y;
    private List<Long> occupiers = new ArrayList<Long>();

    /**
     * Constructs a Cell with specified coordinates.
     *
     * @param x the row index of the cell.
     * @param y the column index of the cell.
     */
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.occupiers.clear();
    }

    /**
     * Gets the row index of the cell.
     *
     * @return the row index.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the column index of the cell.
     *
     * @return the column index.
     */
    public int getY() {
        return y;
    }

    /**
     * Checks if the cell is occupied.
     *
     * @return true if the cell is occupied, false otherwise.
     */
    public boolean isOccupied() {
        return occupiers.size() > 0;
    }

    /**
     * Attempts to add clientId to internal list (acts as a set)
     * @param clientId
     * @return true if clientId was added
     */
    public boolean addPlayer(long clientId){
        if(!occupiers.contains(clientId)){
            occupiers.add(clientId);
            return true;
        }
        return false;
    }
    /**
     * Attempts to remove clientId from internal list
     * @param clientId
     * @return true if removal occurred
     */
    public boolean removePlayer(long clientId){
        return occupiers.remove(clientId);
    }

    public int getNumberInCell(){
        return occupiers.size();
    }
    public List<Long> getPlayersInCell(){
        return occupiers;
    }
    /**
     * Client-side setter for visualization purposes
     * @param count
     */
    public void setOccupiedStatus(int count){
        for(long i = 0; i < count; i++){
            occupiers.add(i);// arbitrary data
        }
    }

    /**
     * Resets the cell to its initial state.
     */
    public void reset() {
        this.occupiers.clear();
        // Reset other object references here if needed
    }

    /**
     * Returns a string representation of the cell.
     *
     * @return a string representation of the cell.
     */
    @Override
    public String toString() {
        return "Cell{" +
                "x=" + x +
                ", y=" + y +
                ", occupied=" + occupiers.size() +
                '}';
    }
}

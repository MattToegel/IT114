package Project.Common;

/**
 * Represents a single cell in a grid.
 * The cell is defined by its column (x) and row (y) coordinates.
 */
public class Cell {
    private int x; // column
    private int y; // row
    private Tower tower;

    // Terrain bonus start
    private int cost = 1;

    public int getCost() {
        return cost;
    }

    private double terrainBonus = 0;
    private TerrainBonusType bonusType = TerrainBonusType.FLAT;
    private Terrain terrainType = Terrain.NONE;

    public void setTerrainBonus(double terrainBonus) {
        this.terrainBonus = terrainBonus;
    }

    public double getTerrainBonus() {
        return terrainBonus;
    }

    public void setTerrainBonusType(TerrainBonusType terrainBonusType) {
        this.bonusType = terrainBonusType;
    }

    public TerrainBonusType getTerrainBonusType() {
        return bonusType;
    }

    public void setTerrainType(Terrain terrain) {
        this.terrainType = terrain;
        switch (terrainType) {
            case ATTACK:
                cost = 2;
                break;
            case CARDS:
                cost = 4;
                break;
            case DEFENSE:
                cost = 2;
                break;
            case ENERGY:
                cost = 3;
                break;
            case HEALING:
                cost = 2;
                break;
            case RANGE:
                cost = 2;
                break;
            case NONE:
            default:
                cost = 1;
                break;
        }
    }

    public Terrain getTerrainType() {
        return terrainType;
    }

    public enum TerrainBonusType {
        FLAT,
        PERCENT
    }

    public enum Terrain {
        NONE, // Plains
        ENERGY, // Forest
        ATTACK, // Desert
        DEFENSE, // Mountains
        RANGE, // Field
        HEALING, // Springs
        CARDS, // Factory
    }

    /**
     * Constructs a Cell with specified coordinates.
     *
     * @param x the column index of the cell.
     * @param y the row index of the cell.
     */
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.tower = null; // Initially, the cell has no tower.
    }

    /**
     * Gets the column index of the cell.
     *
     * @return the column index.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the row index of the cell.
     *
     * @return the row index.
     */
    public int getY() {
        return y;
    }

    /**
     * Checks if the cell is occupied.
     *
     * @return true if the cell has a tower, false otherwise.
     */
    public boolean isOccupied() {
        return tower != null;
    }

    /**
     * Gets the tower placed on the cell.
     *
     * @return the tower, or null if no tower is placed.
     */
    public Tower getTower() {
        return tower;
    }

    /**
     * Places a tower on the cell.
     *
     * @param tower the tower to be placed.
     */
    public void placeTower(Tower tower) {
        if (this.tower != null) {
            throw new IllegalStateException("Cell is already occupied by a tower.");
        }
        this.tower = tower;
        this.tower.setCell(this); // added for Terrain bonus feature
    }

    public void updateTower(Tower tower) {
        if (this.tower == null) {
            throw new IllegalStateException("No tower is assigned to this Cell.");
        }
        this.tower = tower;
        this.tower.setCell(this); // added for Terrain bonus feature
    }

    /**
     * Removes the tower from the cell.
     */
    public void removeTower() {
        this.tower = null;
    }

    /**
     * Resets the cell to its initial state.
     */
    public void reset() {
        this.tower = null; // Remove the tower reference if any
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
                ", Terrain=" + terrainType.name() +
                ", tower=" + tower +
                '}';
    }
}

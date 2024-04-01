package Project.Common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import Project.Common.TextFX.Color;

public class Grid {
    private Cell[][] grid;
    // TODO note to self, see if I swapped row/col logic
    private int rows, columns;

    // cached tiles
    private Cell dragonCell;
    private List<Cell> startCells = new ArrayList<Cell>();
    private Random random = new Random();

    public boolean isPopulated() {
        return dragonCell != null && !startCells.isEmpty();
    }
    public List<Cell> getStartCells() {
        return startCells;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < columns && y >= 0 && y < rows;
    }

    public void generate(int rows, int columns) {
        reset();
        this.rows = rows;
        this.columns = columns;
        grid = new Cell[rows][columns];
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                grid[x][y] = new Cell(x, y);
                // Note: using value as "tile type" so want to be careful
                // grid[x][y].setValue(String.format("(%s,%s) ", x, y));
            }
        }
    }

    public void reset() {
        System.out.println(TextFX.colorize("Resetting grid", Color.YELLOW));
        if (grid != null) {
            for (int x = 0; x < rows; x++) {
                for (int y = 0; y < columns; y++) {
                    grid[x][y].reset();
                }
            }
            grid = null;
        }
    }

    public void print() {
        if (grid == null) {
            System.out.println(TextFX.colorize("Grid isn't setup", Color.RED));
            return;
        }
        for (int x = 0; x < rows; x++) {
            // System.out.println("-".repeat(columns * 3));
            for (int y = 0; y < columns; y++) {
                System.out.print(String.format("%s", grid[x][y]));
            }
            System.out.println("");
        }
        // System.out.println("-".repeat(columns * 3));
    }

    /**
     * Adds a player reference to a cell
     * 
     * @param clientId   The reference/owner
     * @param clientName Example data (I'm using a String for now)
     * @param x          cell's x
     * @param y          cell's y
     * @return success of adding
     */
    public boolean addPlayer(long clientId, String clientName, int x, int y) {
        if (isValidCoordinate(x, y)) {
            return grid[x][y].addPlayer(clientId, clientName);
        }
        return false;
    }

    /**
     * Attempts to move a player reference to the destination coordinate. <br>
     * Will remove the player from their current cell if the destination is valid
     * and current isn't null. <br>
     * Adds the player to the destination cell if it's valid.
     * 
     * @param clientId
     * @param current  where the player currently is
     * @param dx       destination x
     * @param dy       destination y
     * @return a valid destination cell or null if invalid
     */
    public Cell movePlayer(long clientId, Cell current, int dx, int dy) {
        if (isValidCoordinate(dx, dy)) {
            if (current != null) {
                current.removePlayer(clientId);
            }
            Cell nextCell = getCell(dx, dy);
            nextCell.addPlayer(clientId, "");
            return nextCell;
        }
        return null;
    }

    /**
     * Returns a cell reference by coordinate (maybe we don't want to expose direct
     * access to cells)
     * 
     * @param x
     * @param y
     * @return
     */
    public Cell getCell(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return grid[x][y];
        }
        return null;
    }

    public boolean isGridFull() {
        int occupied = 0;
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                occupied += (grid[x][y].isOccupied() ? 1 : 0);
            }
        }
        return occupied == rows * columns;
    }

    private List<Cell> getNeighbors(int x, int y) {
        List<Cell> neighbors = new ArrayList<Cell>();
        int left = x - 1;
        int right = x + 1;
        int up = y - 1;
        int down = y + 1;
        if (isValidCoordinate(left, y)) {
            neighbors.add(grid[left][y]);
        }

        if (isValidCoordinate(right, y)) {
            neighbors.add(grid[right][y]);
        }

        if (isValidCoordinate(x, up)) {
            neighbors.add(grid[x][up]);
        }

        if (isValidCoordinate(x, down)) {
            neighbors.add(grid[x][down]);
        }
        return neighbors;
    }

    public int calculateManhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }

    private boolean isWalkable(Cell c) {
        if (c == null) {
            System.out.println(TextFX.colorize("Cell is null in isWalkable()", Color.RED));
            return false;
        }
        return Cell.WALKABLE.equals(c.getValue());
    }

    private boolean isUnwalkable(Cell c) {
        if (c == null) {
            System.out.println(TextFX.colorize("Cell is null in isUnwalkable()", Color.RED));
            return false;
        }
        return Cell.UNWALABLE.equals(c.getValue());
    }

    private boolean isCellSingleLine(int x, int y) {
        // check if the cell is surrounded by 3 or more other cells
        int left = x - 1;
        int right = x + 1;
        int up = y - 1;
        int down = y + 1;
        int connections = 0;
        if (isValidCoordinate(left, y) && isWalkable(grid[left][y])) {
            connections++;
        }
        if (isValidCoordinate(right, y) && isWalkable(grid[right][y])) {
            connections++;
        }
        if (isValidCoordinate(x, up) && isWalkable(grid[x][up])) {
            connections++;
        }
        if (isValidCoordinate(x, down) && isWalkable(grid[x][down])) {
            connections++;
        }
        return connections < 3;
    }

    /**
     * Generates a path from Coordinate 1 to Coordinate 2
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    private void generatePath(int x1, int y1, int x2, int y2) {
        if (!isValidCoordinate(x1, y2) || !isValidCoordinate(x2, y2)) {
            System.out.println("One of the cooridnates isn't valid");
            return;
        }
        Cell currentCell = grid[x1][y1];
        currentCell.setValue(Cell.WALKABLE);

        // walk algorithm
        while (currentCell.getX() != x2 || currentCell.getY() != y2) {
            int xDiff = x2 - currentCell.getX();
            int yDiff = y2 - currentCell.getY();

            List<Cell> potentialMoves = new ArrayList<Cell>();
            if (Math.abs(xDiff) > 0) {
                Cell horizontalMove = moveHorizontallyTowardsTarget(currentCell, xDiff);
                if (horizontalMove != currentCell
                        && isCellSingleLine(horizontalMove.getX(), horizontalMove.getY())) {
                    potentialMoves.add(horizontalMove);
                }
            }

            if (Math.abs(yDiff) > 0) {
                Cell verticalMove = moveVerticallyTowardsTarget(currentCell, yDiff);
                if (verticalMove != currentCell && isCellSingleLine(verticalMove.getX(), verticalMove.getY())) {
                    potentialMoves.add(verticalMove);
                }
            }
            if (!potentialMoves.isEmpty()) {
                Cell nextCell = potentialMoves.get(random.nextInt(potentialMoves.size()));
                nextCell.setValue(Cell.WALKABLE);
                currentCell = nextCell;
            } else {
                System.out.println("Stuck or needs to backtrack");
                break;
            }

        }
    }

    private Cell moveHorizontallyTowardsTarget(Cell current, int xDiff) {
        int cx = current.getX();
        int cy = current.getY();
        if (xDiff > 0 && isValidCoordinate(cx + 1, cy)) {
            return grid[cx + 1][cy];
        } else if (xDiff < 0 && isValidCoordinate(cx - 1, cy)) {
            return grid[cx - 1][cy];
        }
        return current;
    }

    private Cell moveVerticallyTowardsTarget(Cell current, int xDiff) {
        int cx = current.getX();
        int cy = current.getY();
        if (xDiff > 0 && isValidCoordinate(cx, cy + 1)) {
            return grid[cx][cy + 1];
        } else if (xDiff < 0 && isValidCoordinate(cx, cy - 1)) {
            return grid[cx][cy - 1];
        }
        return current;
    }

    public List<Cell> getCellsOfType(String t) {
        return Arrays.stream(grid).flatMap(Arrays::stream).filter(c -> c.getValue().equals(t))
                .collect(Collectors.toList());
    }

    public boolean isAtOrAdjacentToDragon(Cell current) {
        return Cell.DRAGON.equals(current.getValue()) || isAdjacentToDragon(current);
    }
    public boolean isAdjacentToDragon(Cell current) {
        /*
         * (first version)
         * List<Cell> n = getNeighbors(current.getX(), current.getY())
         * .stream()
         * .filter(c -> !isUnwalkable(c) && c.isConnected(current) &&
         * !c.equals(current)).toList();
         * return n.stream().anyMatch(c-> c.getValue().equals(Cell.DRAGON));
         */
        /*
         * (second during video)
         * return getNeighbors(current.getX(), current.getY())
         * .stream()
         * .filter(c -> !isUnwalkable(c) && c.isConnected(current) &&
         * !c.equals(current))
         * .anyMatch(c -> c.getValue().equals(Cell.DRAGON));
         */
        // (third during video)
        return getWalkableNeighbors(current).stream()
                .filter(c -> !isUnwalkable(c) && c.isConnected(current) && !c.equals(current))
                .anyMatch(c -> c.getValue().equals(Cell.DRAGON));
    }

    public List<Cell> getWalkableNeighbors(Cell current) {
        List<Cell> n = getNeighbors(current.getX(), current.getY())
                .stream()
                .filter(c -> !isUnwalkable(c) && c.isConnected(current) &&
                        !c.equals(current))
                .toList();
        System.out.println(String.format("Has %s neighbors", n.size()));
        return n;
    }

    public List<Cell> getValidMoves(Cell current, List<Cell> path) throws Exception {
        List<Cell> c = getWalkableNeighbors(current);
        /*
         * System.out.println(String.format("Checking path for %s,%s", current.getX(),
         * current.getY()));
         * path.forEach(p -> {
         * System.out.println(String.format("Path: %s,%s", p.getX(), p.getY()));
         * });
         */
        List<Cell> possible = c.stream().filter(nc -> !path.contains(nc)).collect(Collectors.toList());
        /*
         * System.out.println("Checking possible");
         * possible.forEach(p -> {
         * System.out.println(String.format("Possible: %s,%s", p.getX(), p.getY()));
         * });
         */
        if (possible.isEmpty()) {
            System.out.println(TextFX.colorize("Reached a dead end", Color.YELLOW));
            possible = c.stream().collect(Collectors.toList());
        }
        if (possible.isEmpty()) {
            throw new Exception("Invalid movement");
        }
        return possible;
    }

    private void associateNeighbors() {
        for (int x = 0; x < getRows(); x++) {
            for (int y = 0; y < getColumns(); y++) {
                Cell currentCell = getCell(x, y);
                currentCell.printNeighbors();
                List<Cell> n = getNeighbors(x, y).stream().filter(c -> !isUnwalkable(c)).toList();
                n.forEach(neighborCell -> {
                    currentCell.addAdjacent(neighborCell);
                });
            }
        }
    }

    // server-side
    public void populate(long seed) {
        random = new Random(seed);
        startCells.clear();
        int xMin = 0;
        int yMin = 0;
        int xMax = getRows() - 1;
        int yMax = getColumns() - 1;
        int centerX = getRows() / 2;
        int centerY = getColumns() / 2;
        // top left to dragon
        generatePath(xMin, yMin, centerX, centerY);
        // top right to dragon
        generatePath(xMin, yMax, centerX, centerY);
        // bottom left to dragon
        generatePath(xMax, yMin, centerX, centerY);
        // bottom right to dragon
        generatePath(xMax, yMax, centerX, centerY);

        // corners to corners
        // top left to top right
        generatePath(xMin, yMin, xMin, yMax);
        // top left to bottom left
        generatePath(xMin, yMin, xMax, yMin);
        // top right to bottom right
        generatePath(xMin, yMax, xMax, yMax);
        // bottom left to bottom right
        generatePath(xMax, yMin, xMax, yMax);

        // dragon
        dragonCell = getCell(centerX, centerY);
        dragonCell.setValue(Cell.DRAGON);

        Cell tr = getCell(xMin, yMax);
        tr.setValue(Cell.START);
        startCells.add(tr);

        Cell br = getCell(xMax, yMax);
        br.setValue(Cell.START);
        startCells.add(br);

        Cell tl = getCell(xMin, yMin);
        tl.setValue(Cell.START);
        startCells.add(tl);

        Cell bl = getCell(xMax, yMin);
        bl.setValue(Cell.START);
        startCells.add(bl);

        // connect everyone
        associateNeighbors();
        print();
    }

    public Cell handleTurn(long clientId, Cell current, List<Cell> path) throws Exception {

        List<Cell> validCells = getValidMoves(current, path);
        System.out.println(TextFX.colorize(validCells.size() + " valid cells", Color.YELLOW));
        if (validCells.isEmpty()) {
            System.out.println("No options");
            throw new Exception("No valid options");
        } else if (validCells.size() == 1) {
            Cell next = validCells.get(0);
            next = movePlayer(clientId, current, next.getX(), next.getY());
            if (next != null) {
                current = next;
                // path.add(current);
            }
        } else {
            // TODO offload choice to player
            // random direction
            Cell next = validCells.get(random.nextInt(validCells.size()));
            next = movePlayer(clientId, current, next.getX(), next.getY());
            if (next != null) {
                current = next;
                // path.add(current);
            }
        }
        return current;
    }
    public static void main(String[] args) {
        /*
         * Grid grid = new Grid();
         * grid.generate(8, 8);
         * grid.print();
         */
        Grid grid = new Grid();
        grid.generate(9, 9);
        grid.populate(5);

        Cell current = grid.getCell(0, 0);
        current.addPlayer(-1, "");
        Scanner s = new Scanner(System.in);

        while (s.hasNext()) {
            String line = s.nextLine();
            if (line.startsWith("/roll")) {
                try {
                    int roll = Integer.parseInt(line.replace("/roll", "").trim());
                    if (roll >= 1 && roll <= 6) {
                        List<Cell> path = new ArrayList<Cell>();
                        path.add(current);
                        for (int i = 0; i < roll; i++) {
                            System.out.println(TextFX.colorize("Doing step " + (i + 1), Color.CYAN));
                            System.out.println(TextFX.colorize(
                                    String.format("Current %s, %s", current.getX(), current.getY()), Color.YELLOW));

                            List<Cell> validCells = grid.getValidMoves(current, path);
                            if (validCells.isEmpty()) {
                                System.out.println("No options");
                            } else if (validCells.size() == 1) {
                                Cell next = validCells.get(0);
                                next = grid.movePlayer(-1, current, next.getX(), next.getY());
                                if (next != null) {
                                    // TODO associate with player (setCell)
                                    current = next;
                                    path.add(current);
                                }
                            } else {
                                System.out.println("Pick direction");
                                for (int k = 0; k < validCells.size(); k++) {
                                    System.out.println(String.format("%s - %s,%s", k, validCells.get(k).getX(),
                                            validCells.get(k).getY()));
                                }
                                try {
                                    int choice = Integer.parseInt(s.nextLine());
                                    Cell next = validCells.get(choice);
                                    next = grid.movePlayer(-1, current, next.getX(), next.getY());
                                    if (next != null) {
                                        // TODO associate with player (setCell)
                                        current = next;
                                        path.add(current);
                                    }
                                } catch (Exception e) {
                                }
                            }

                            System.out.println(TextFX.colorize(
                                    String.format("Reached %s,%s", current.getY(), current.getY()), Color.YELLOW));
                            grid.print(); // <-- show our movement
                            // check end of move
                            if (current.getValue().equals(Cell.DRAGON) || grid.isAdjacentToDragon(current)) {
                                System.out.println("Reached Dragon");
                                System.out.println(TextFX.colorize(
                                        String.format("Recevied %s treasure", grid.random.nextInt(4)), Color.YELLOW));

                                // pick new random start
                                List<Cell> starts = grid.getStartCells();
                                Cell next = starts.get(grid.random.nextInt(starts.size()));
                                next = grid.movePlayer(-1, current, next.getX(), next.getY());
                                if (next != null) {
                                    // TODO associate with player (setCell)
                                    current = next;
                                }
                                grid.print();
                                break; // even if we have moves left, stop this round
                            }
                        }
                        System.out.println("Out of steps");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        s.close();
        System.out.println("Example finished");

    }
}

package DCT.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class GridHelpers {
    // Helper method to find an adjacent neighbor in the main path
    public static Point findAdjacentNeighbor(Cell cell, List<Cell> mainPathCells, Cell[][] cells) {
        // Get the dimensions of the grid
        int rows = cells.length;
        int columns = cells[0].length;
        int x = cell.getX();
        int y = cell.getY();

        // Iterate over the neighboring cells
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;

                // Check if the neighbor is within the grid boundaries and not an edge cell
                if (nx > 0 && nx < rows - 1 && ny > 0 && ny < columns - 1) {
                    // Check if the neighbor is in the main path
                    Cell neighbor = cells[nx][ny];
                    if (mainPathCells.contains(neighbor)) {
                        return new Point(nx, ny);
                    }
                }
            }
        }

        return null;
    }

    // Generate a door on any edge of the grid
    public static DoorCell generateDoorOnEdge(int rows, int columns, int edge) {
        int x, y;

        // Generate door on the selected edge
        switch (edge) {
            case 0: // Top edge
                x = 0;
                y = (int) (Math.random() * (columns - 2)) + 1;
                break;
            case 1: // Right edge
                x = (int) (Math.random() * (rows - 2)) + 1;
                y = columns - 1;
                break;
            case 2: // Bottom edge
                x = rows - 1;
                y = (int) (Math.random() * (columns - 2)) + 1;
                break;
            case 3: // Left edge
                x = (int) (Math.random() * (rows - 2)) + 1;
                y = 0;
                break;
            default:
                throw new IllegalStateException("Invalid edge value");
        }

        DoorCell door = new DoorCell(x, y);
        return door;
    }

    // Helper method to create a path from one cell to another
    public static void createPath(Cell cell1, Cell cell2, Cell[][] cells) {
        // Get the coordinates of cell1
        int x1 = cell1.getX();
        int y1 = cell1.getY();

        // Get the coordinates of cell2
        int x2 = cell2.getX();
        int y2 = cell2.getY();

        // Start the path from the adjacent cell of cell1
        int startX = x1;
        int startY = y1;
        if (x1 < x2) {
            startX++; // Move to the right
        } else if (x1 > x2) {
            startX--; // Move to the left
        } else if (y1 < y2) {
            startY++; // Move downwards
        } else if (y1 > y2) {
            startY--; // Move upwards
        }

        // Create a path of cells from cell1 to cell2
        int currentX = startX;
        int currentY = startY;
        while (currentX != x2 || currentY != y2) {
            // Mark the current cell as a walkable cell by creating a new Cell object
            cells[currentX][currentY] = new Cell(currentX, currentY);

            // Move to the next cell based on the direction towards cell2
            if (currentX < x2) {
                currentX++; // Move to the right
            } else if (currentX > x2) {
                currentX--; // Move to the left
            } else if (currentY < y2) {
                currentY++; // Move downwards
            } else if (currentY > y2) {
                currentY--; // Move upwards
            }
        }
    }

    public static void addBranches(Cell[][] cells) {
        int rows = cells.length;
        int columns = cells[0].length;
        // Create a list to store the cells of the main path
        List<Cell> mainPathCells = new ArrayList<>();

        // Traverse the main path and store the cells in the list
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (cells[row][column] instanceof Cell) {
                    mainPathCells.add(cells[row][column]);
                }
            }
        }

        // Iterate over the inner cells to add branching paths
        for (int row = 1; row < rows - 1; row++) {
            for (int column = 1; column < columns - 1; column++) {
                if (cells[row][column] instanceof WallCell) {
                    // Randomly decide to add a branching path
                    double probability = Math.random();
                    if (probability < 0.05) {
                        // Find a random cell from the main path that is not an edge cell
                        Cell randomCell = getRandomCellExcludingEdges(mainPathCells, rows, columns);

                        // Find an adjacent neighbor in the main path
                        Point adjacentNeighbor = findAdjacentNeighbor(randomCell, mainPathCells, cells);

                        if (adjacentNeighbor != null) {
                            // Create a path from the current cell to the adjacent neighbor
                            createPath(cells[row][column], cells[adjacentNeighbor.x][adjacentNeighbor.y], cells);
                        }
                    }
                }
            }
        }
    }

    // Generate a random complete path from the start door to the end door
    public static void generatePath(DoorCell startDoor, DoorCell endDoor, Cell[][] cells) {
        int rows = cells.length;
        int columns = cells[0].length;

        // Start from the position of the start door
        int startX = startDoor.getX();
        int startY = startDoor.getY();

        // Set the current cell as the start door
        Cell currentCell = startDoor;

        // Create a set to keep track of visited cells
        Set<Point> visited = new HashSet<>();

        // Continue walking until reached the end door
        while (currentCell != endDoor) {
            // Get the neighbors of the current cell
            List<Point> neighbors = getNeighbors(new Point(startX, startY), rows, columns);
            List<Point> validNeighbors = new ArrayList<>();

            // Filter valid neighbors that are not already converted to Cell and not on the
            // edge
            for (Point neighbor : neighbors) {
                int nx = neighbor.x;
                int ny = neighbor.y;

                // Check if the neighbor is within the grid bounds and is a WallCell
                if (nx > 0 && ny > 0 && nx < rows - 1 && ny < columns - 1 && cells[nx][ny] instanceof WallCell) {
                    validNeighbors.add(neighbor);
                }
            }

            // If there are no valid neighbors, break the loop
            if (validNeighbors.isEmpty()) {
                break;
            }

            // Sort the valid neighbors by Manhattan distance to the end door
            validNeighbors.sort(
                    Comparator.comparingInt(p -> getManhattanDistance(p, new Point(endDoor.getX(), endDoor.getY()))));

            // Randomly select a neighbor with a lower Manhattan distance
            int minDistance = getManhattanDistance(validNeighbors.get(0), new Point(endDoor.getX(), endDoor.getY()));
            List<Point> candidateNeighbors = new ArrayList<>();
            for (Point neighbor : validNeighbors) {
                if (getManhattanDistance(neighbor, new Point(endDoor.getX(), endDoor.getY())) == minDistance) {
                    candidateNeighbors.add(neighbor);
                } else {
                    break;
                }
            }
            Point randomNeighbor = candidateNeighbors.get(new Random().nextInt(candidateNeighbors.size()));
            int nx = randomNeighbor.x;
            int ny = randomNeighbor.y;

            // Convert the selected neighbor to Cell
            cells[nx][ny] = new Cell(nx, ny);

            // Update the current position
            startX = nx;
            startY = ny;
            currentCell = cells[startX][startY];

            // Add the current position to the visited set
            visited.add(randomNeighbor);
        }
    }

    // Calculate the Manhattan distance between two points
    private static int getManhattanDistance(Point p1, Point p2) {
        // Manhattan distance is the total number of steps required to move from one
        // point to another
        // in a grid, considering only vertical and horizontal movements.
        // It is calculated by summing the absolute differences between the x and y
        // coordinates of the points.
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    // Helper method to get a random cell from the main path that is not on the
    // grid's edges
    private static Cell getRandomCellExcludingEdges(List<Cell> cells, int rows, int columns) {
        // Filter the cells from the main path that are not on the grid's edges
        List<Cell> eligibleCells = cells.stream()
                .filter(cell -> cell.getX() != 0 && cell.getX() != rows - 1 && cell.getY() != 0
                        && cell.getY() != columns - 1)
                .collect(Collectors.toList());

        // Generate a random index within the range of eligible cells
        int index = (int) (Math.random() * eligibleCells.size());

        // Return a random cell from the eligible cells
        return eligibleCells.get(index);
    }

    public static List<Cell> getNeighborCells(Cell cell, Cell[][] cells) {
        int rows = cells.length;
        int columns = cells[0].length;
        return getNeighbors(new Point(cell.getX(), cell.getY()), rows, columns).stream().map(p -> cells[p.x][p.y])
                .toList();
    }

    // Helper method to get the neighboring points of a given point within the grid
    private static List<Point> getNeighbors(Point current, int rows, int columns) {
        // Create a list to store the neighboring points
        List<Point> neighbors = new ArrayList<>();

        // Check if there is a point to the left of the current point
        if (current.x > 0) {
            // Add the left neighboring point to the list
            neighbors.add(new Point(current.x - 1, current.y));
        }
        // Check if there is a point above the current point
        if (current.y > 0) {
            // Add the upper neighboring point to the list
            neighbors.add(new Point(current.x, current.y - 1));
        }
        // Check if there is a point to the right of the current point
        if (current.x < rows - 1) {
            // Add the right neighboring point to the list
            neighbors.add(new Point(current.x + 1, current.y));
        }
        // Check if there is a point below the current point
        if (current.y < columns - 1) {
            // Add the lower neighboring point to the list
            neighbors.add(new Point(current.x, current.y + 1));
        }

        // Return the list of neighboring points
        return neighbors;
    }

    public static void printGrid(Cell[][] cells) {
        if (cells == null) {
            System.out.println("Grid is not built.");
            return;
        }

        int rows = cells.length;
        int columns = cells[0].length;

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Cell cell = cells[row][column];
                if (cell == null) {
                    System.out.print("-_-"); // hidden cell
                } else if (cell instanceof DoorCell) {
                    System.out.print("[D]"); // door cell
                } else if (cell instanceof WallCell) {
                    System.out.print("[W]"); // wall cell
                } else if (cell.isBlocked()) {
                    System.out.print("[x]"); // blocked path
                } else {
                    if (cell.getNumberOfCharactersInCell() > 0) {
                        System.out.print("[" + cell.getNumberOfCharactersInCell() + "]");
                    } else {
                        System.out.print("[ ]"); // walkable path
                    }

                }
            }
            System.out.println(); // Move to the next line after each row
        }
    }

    public static List<Cell> getCellsWithinRangeList(int x, int y, Cell[][] cells) {
        // Get the dimensions of the grid
        int rows = cells.length;
        int columns = cells[0].length;

        // Create a list to store the cells within the specified range
        List<Cell> cellsInRange = new ArrayList<>();

        // Iterate over the cells within the specified range
        for (int i = Math.max(0, x - 2); i <= Math.min(rows - 1, x + 2); i++) {
            for (int j = Math.max(0, y - 2); j <= Math.min(columns - 1, y + 2); j++) {
                // Check if the cell is not null and add it to the list
                if (cells[i][j] != null) {
                    cellsInRange.add(cells[i][j]);
                }
            }
        }

        // Return the list of cells within the specified range
        return cellsInRange;
    }

    public static Cell[][] getCellsWithinRange2D(int x, int y, Cell[][] cells) {
        // Get the dimensions of the grid
        int rows = cells.length;
        int columns = cells[0].length;

        // Create a 2D array to store the cells within the specified range
        Cell[][] cellsInRange = new Cell[rows][columns];

        // Iterate over the cells within the specified range
        for (int i = Math.max(0, x - 2); i <= Math.min(rows - 1, x + 2); i++) {
            for (int j = Math.max(0, y - 2); j <= Math.min(columns - 1, y + 2); j++) {
                // Check if the cell is not null and add it to the array
                if (cells[i][j] != null) {
                    cellsInRange[i][j] = cells[i][j];
                }
            }
        }

        // Return the 2D array of cells within the specified range
        return cellsInRange;
    }
}

import java.awt.Point;

/**
 * Starting point of the application to group up all the classes
 * @author MattT
 *
 */
public class GridSandbox {
	public static void main(String[] ar) {
		Client c = new Client();
		Server s = new Server();
	}
}
/**
 * Represents a single coordinate of a grid
 * Used a class so we can store more data
 * @author MattT
 *
 */
class Cell{
	public int player = -1;//-1 is untouched
	//We can add a point or x,y to have a local ref of our coordinate
	public Point mySpot = new Point(0,0);//init later
	public Cell(Point spotCache) {
		mySpot = spotCache;
	}
	public void setPlayerSelection(int playerIndex) {
		if(player < 0) {
			player = playerIndex;
		}
		else {
			System.out.println("Someone's already at this coordinate");
		}
	}
	public void reset() {
		player = -1;
	}
}
/***
 * Holds our multi-dimensional array Cell
 * @author MattT
 *
 */
class Grid{
	Cell[][] grid;
	public Grid(int cols, int rows) {
		grid = new Cell[cols][rows];
		for(int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				grid[x][y] = new Cell(new Point(x,y));
			}
		}
	}
	public Cell getCell(int x, int y) {
		return grid[x][y];
	}
	public Cell getCell(Point p) {
		return getCell(p.x, p.y);
	}
	public void updateCell(int x, int y, String something) {
		//TODO do something with cell via param(s)
		try {
			//for sake of example just trying to convert string to int since
			//we're passing player index/id as a string
			getCell(x, y).player = Integer.parseInt(something);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Cell " + x + ", " + y + ": Something happened: " + something);
	}
	public void resetGrid() {
		for(int x = 0; x < grid[0].length; x++) {
			for(int y = 0; y < grid[1].length; y++) {
				//reset each cell
				grid[x][y].reset();
			}
		}
	}
}

class Client{
	Grid grid;
	public Client() {
		//must match server
		grid = new Grid(3,3);
	}
	public void processPayload(Payload p) {
		//from server
		if(p.type == 1 /*Pick spot*/) {
			grid.updateCell(p.x, p.y, p.player+"");
		}
	}
}
class Server{
	Grid grid;
	public Server() {
		//must match client
		grid = new Grid(3,3);
	}
	public void broadcast(Payload p) {
		//Send to all
	}
	public void reply(int client, Payload p) {
		//send reply to one
	}
	public void processPayload(Payload p) {
		//from client
		if(p.type == 1 /*pick spot*/) {
			Cell cell = grid.getCell(p.x, p.y);
			if(cell.player < 0) {
				//OK, spot is vacant
				//update our local grid
				//broadcast move to players
				broadcast(p);
			}
			else {
				//Someone is here
				//can drop request or send a failure reply
				//back to only the player who made the request
				p.message = "Spot's taken, sorry";
				reply(p.clientId, p);
			}
		}
	}
}
/***
 * Very crude/generic Payload class (not recommended to use since it's not
 * as clear as the one outlined in class)
 * @author MattT
 *
 */
class Payload {
	//maybe not needed, but just here for example
	public int clientId;//assume it's a reference to who send this to server
	public int type;
	public int x;
	public int y;
	public int player;
	public String message;
}


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * The world in which this simulation exists. As a base
 * world, this produces a 10x10 room of tiles. In addition,
 * 20% of the room is covered with "walls" (tiles marked as IMPASSABLE).
 *
 * This object will allow the agent to explore the world and is how
 * the agent will retrieve information about the environment.
 * DO NOT MODIFY.
 * @author Adam Gaweda, Michael Wollowski
 */
public class Environment {
	private Tile[][] tiles;
	private int rows, cols;
	private LinkedList<Position> targets = new LinkedList<>();
	private ArrayList<Robot> robots;
	private String[][] policy;

	public Environment(LinkedList<String> map, ArrayList<Robot> robots) {
		this.cols = map.get(0).length();
		this.rows = map.size();
		this.tiles = new Tile[rows][cols];
		for (int row = 0; row < this.rows; row++) {
			for (int col = 0; col < this.cols; col++) {
				char tile = map.get(row).charAt(col);
				switch(tile) {
					case 'R': tiles[row][col] = new Tile(TileStatus.CLEAN); {
						robots.add(new Robot(this, row, col));
						break;
					}
					case 'D': tiles[row][col] = new Tile(TileStatus.DIRTY); break;
					case 'C': tiles[row][col] = new Tile(TileStatus.CLEAN); break;
					case 'W': tiles[row][col] = new Tile(TileStatus.IMPASSABLE); break;
					case 'T': tiles[row][col] = new Tile(TileStatus.TARGET); targets.add(new Position(row, col)); break;
				}
			}
		}
		this.robots = robots;

		//this is correct i believe, i just need to find where its accessing the actual policy to use the functions
		policy = this.retPolicy(this.computeValueMatrix());

		double[][] stuff = this.computeValueMatrix();
		for(int roah = 0; roah<this.rows; roah++) {
			for (int coal = 0; coal < this.cols; coal++) {
				System.out.print((double)Math.round(stuff[roah][coal]*100)/100+"    ");
			}
			System.out.println("");
		}

//		for(int roah = 0; roah<this.rows; roah++) {
//			for (int coal = 0; coal < this.cols; coal++) {
//				System.out.print(policy[roah][coal]+"    ");
//			}
//			System.out.println("");
//		}


	}

	/* Traditional Getters and Setters */
	public Tile[][] getTiles() { return tiles; }

	public int getRows() { return this.rows; }

	public int getCols() { return this.cols; }

	public LinkedList<Position> getTargets(){
		return (LinkedList<Position>) this.targets.clone();
	}

	public ArrayList<Robot> getRobots(){
		return (ArrayList<Robot>) this.robots.clone();
	}

	public String[][]getPolicy(){
		return policy;
	}

	/*
	 * Returns a the status of a tile at a given [row][col] coordinate
	 */
	public TileStatus getTileStatus(int row, int col) {
		if (row < 0 || row >= rows || col < 0 || col >= cols) return TileStatus.IMPASSABLE;
		else return tiles[row][col].getStatus();
	}

	/* Counts number of tiles that are not walls */
	public int getNumTiles() {
		int count = 0;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				if (this.tiles[row][col].getStatus() != TileStatus.IMPASSABLE)
					count++;
			}
		}
		return count;
	}

	/* Cleans the tile at coordinate [x][y] */
	public void cleanTile(int x, int y) {
		tiles[x][y].cleanTile();
	}

	/* Counts number of clean tiles */
	public int getNumCleanedTiles() {
		int count = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (this.tiles[i][j].getStatus() == TileStatus.CLEAN)
					count++;
			}
		}
		return count;
	}

	/* Counts number of dirty tiles */
	public int getNumDirtyTiles() {
		int count = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (this.tiles[i][j].getStatus() == TileStatus.DIRTY)
					count++;
			}
		}
		return count;
	}

	/* Determines if a particular [row][col] coordinate is within
	 * the boundaries of the environment. This is a rudimentary
	 * "collision detection" to ensure the agent does not walk
	 * outside the world (or through walls).
	 */
	public boolean validPos(int row, int col) {
		return row >= 0 && row < rows && col >= 0 && col < cols &&
				tiles[row][col].getStatus() != TileStatus.IMPASSABLE;
	}


	public String[][] retPolicy( double[][] valueMatrix){

		String[][] policy = new String[valueMatrix.length][valueMatrix[0].length];

		//iterating through all the blocks
		for(int row = 0; row<valueMatrix.length; row++){
			for(int col = 0; col<valueMatrix[0].length;col++){
				//all we are doing here is just instantiating the policy based upon the adjacent block with the maximum value

				//i copy pasted a large portion of what im showing below from the comptue value matrix function
				HashSet<direction> fourDirections = new HashSet<>();
				//do i ever check if its within bounds? yes

				//up
				fourDirections.add(new direction(row-1,col, "up"));
				//down
				fourDirections.add(new direction(row+1,col,"down"));
				//left
				fourDirections.add(new direction(row,col-1,"left"));
				//right
				fourDirections.add(new direction(row,col+1,"right"));


				double maxAdjacentValue = 0;
				//if this doesnt change position, then we are in a bad situation. this is really just the position of the adjacent tile that gives us the maximum value
				direction associatedDir = new direction(0,0, "");
				//for all four possible new locations to look into

				for(direction lookInto: fourDirections){
					//if the direction we are going is not a valid pos, then continue
					if(!validPos(lookInto.row,lookInto.col))
						continue;

					//otherwise, we need to first establish what has the max value, then we update based off that value .
					if(valueMatrix[lookInto.row][lookInto.col] > maxAdjacentValue){
						//if this tile is more than the max,
						maxAdjacentValue = valueMatrix[lookInto.row][lookInto.col];
						associatedDir = new direction(lookInto.row, lookInto.col,lookInto.dir);

					}

				}

				//okay, now we have the direction with the maximum value
				policy[row][col] = associatedDir.dir;


			}
		}

		return policy;
	}

	public double[][] computeValueMatrix(){

		int numRows = getRows();
		int numCols = getCols();

		double[][] values = new double[numRows][numCols];

		double[][] oldValues = deepCopy2d(values);
		double[][] updatedValues = new double[numRows][numCols];

		int numIterations = 10000;

		double delta = .79;

		for(int i = 0; i<numIterations; i++){

			double greatestChange = 0;
			//now, for a single iteration, we need to compute the value each place

			//we iterate through every tile
			for(int row = 0; row<numRows; row++){
				for(int col = 0; col<numCols; col++) {
					//first, we need to check if what we are updating is a valid pos
					if(!validPos(row,col) && !getTileStatus(row,col).equals(TileStatus.IMPASSABLE))
						continue;

					//if what we are looking at is valid position, then:
					//make 4 new positions
					HashSet<direction> fourDirections = new HashSet<>();
					//do i ever check if its within bounds? yes

					//if it is not a valid direction, then just use ourself
					//might need to not use impassable, not sure

					//up
					if(validPos(row-1,col))
						fourDirections.add(new direction(row-1,col, "up"));
					else{
						fourDirections.add(new direction(row,col, "up"));
					}
					//down
					if(validPos(row+1,col))
						fourDirections.add(new direction(row+1,col, "down"));
					else{
						fourDirections.add(new direction(row,col, "down"));
					}
					//left
					if(validPos(row,col-1))
						fourDirections.add(new direction(row,col-1, "left"));
					else{
						fourDirections.add(new direction(row,col, "left"));
					}
					//right
					if(validPos(row,col+1))
						fourDirections.add(new direction(row,col+1, "right"));
					else{
						fourDirections.add(new direction(row,col, "right"));
					}

					double maxUpdateValue = -1000;
					//if this doesnt change position, then we are in a bad situation. this is really just the position of the adjacent tile that gives us the maximum value

					//no longer need associated dir. doing all calculations within this loop, as we need a wholistic comparison of the sum of values, not just the value at that spot
					direction associatedDir = new direction(0,0, "");
					//for all four possible new locations to look into

					int countForDirection = 0;

					for(direction lookInto: fourDirections){
						//if the direction we are going was not valid, we just stayed put and did the calculation with respect to staying still

						Position use1 = new Position(row,col);
						Position use2 = new Position(row,col);
						switch(associatedDir.dir){
							case "left":
							case "right":
								if(validPos(row-1,col))
									use1 = new Position(row-1,col);
								if(validPos(row+1,col))
									use2 = new Position(row+1,col);
								break;
							case "up":
							case "down":
								if(validPos(row,col-1))
									use1 = new Position(row,col-1);
								if(validPos(row,col+1))
									use2 = new Position(row,col+1);
								break;

						}

						//alright, now we have the three positions we want to update with regards to.
						//now, do the updating!
						double reward = 0;

						switch(getTileStatus(row,col)){
							case DIRTY:
								reward = 1;
								break;
							case CLEAN:
								reward = -0.04;
								break;
						}
						double gamma = 0.9;

						if(reward + gamma*(0.8*oldValues[lookInto.row][lookInto.col] + 0.1*oldValues[use1.getRow()][use1.getCol()]+0.1*oldValues[use2.getRow()][use2.getCol()]) > maxUpdateValue)
							maxUpdateValue = reward + gamma*(0.8*oldValues[lookInto.row][lookInto.col] + 0.1*oldValues[use1.getRow()][use1.getCol()]+0.1*oldValues[use2.getRow()][use2.getCol()]);

					}

					updatedValues[row][col] = maxUpdateValue;

					if(Math.abs(updatedValues[row][col]-oldValues[row][col])>greatestChange)
						greatestChange = Math.abs(updatedValues[row][col]-oldValues[row][col]);
				}
			}
			//now, we have updated the entire matrix over a single iteration
			//now, we want to update the general values
			oldValues = deepCopy2d(updatedValues);

			if(greatestChange < .5*(1-delta)/delta)
				break;
		}


		return updatedValues;
	}

	class direction{
		private Position pos;
		private String dir;
		private int row;
		private int col;

		public direction(int ro, int co, String setDir){
			pos = new Position(ro,co);
			row = ro;
			col = co;
			dir = setDir;
		}

	}

	//just a simple deep copy function
	public double[][] deepCopy2d(double[][] from){
		//the two above should definitely be of the same dimension
		double[][] to = new double[from.length][from[0].length];

		for(int row = 0; row < from.length; row++)
			for(int col = 0; col< from[0].length; col++)
				to[row][col] = from[row][col];

		return to;
	}


}
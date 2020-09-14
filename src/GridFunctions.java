import java.util.ArrayList;
import java.util.TreeMap;

public class GridFunctions {

	//returns the id of the most common neighbor of the given cell at (r, c)
	//will crash if the r (the row) or c (the column) is out of bounds of the grid
	//if there are no neighbors of the cell, it will return -1
	//if there is a tie for the most common, one at random will be selected
	public static int mostCommonNeighbor(int r, int c, Grid grid) {
		
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
		
		for(int row = r-1; row <= r+1; row++) {
			for(int col = c-1; col <= c+1; col++) {
				if(r != row || c != col) {
					if(row >= 0 && col >= 0 && row < grid.getRows() && col < grid.getCols()) {
						if(grid.getCell(row, col) != -1) {
							int id = grid.getCell(row, col);
							if(map.containsKey(id))
								map.put(id, map.get(id)+1);
							else
								map.put(id, 1);
						}
					}
				}
			}
		}
		
		if(map.size() == 0)
			return -1;
		
		ArrayList<Integer> maxIDs = new ArrayList<Integer>();
		int maxValue = -1;
		
		for(int id : map.keySet()) {
			int value = map.get(id);
			
			if(value > maxValue) {
				maxValue = value;
				maxIDs.clear();
				maxIDs.add(id);
			}
			else if(value >= maxValue) {
				maxIDs.add(id);
			}
		}
		
		
		return maxIDs.get((int)(Math.random()*maxIDs.size()));
	}
	
	//returns the number of neighbors of the given cell at (r, c)
	//will crash if the r (the row) or c (the column) is out of bounds of the grid
	public static int getNeighbors(int r, int c, Grid grid) {
		int alive = 0;
		for(int row = r-1; row <= r+1; row++) {
			for(int col = c-1; col <= c+1; col++) {
				if(r != row || c != col) {
					if(row >= 0 && col >= 0 && row < grid.getRows() && col < grid.getCols()) {
						if(grid.getCell(row, col) != -1)
							alive++;
					}
				}
			}
		}
		return alive;
	}
}

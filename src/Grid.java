
public class Grid {
	
	private int[][] grid;
	
	public Grid(int[][] g) {
		grid = g;
	}
	
	public int getRows() {
		return grid.length;
	}
	
	public int getCols() {
		return grid[0].length;
	}
	
	public int getCell(int r, int c) {
		return grid[r][c];
	}
	
}

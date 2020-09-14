public class SpawnerAI extends CellAI {

	@Override
	public String getAIName() {
		// TODO Auto-generated method stub
		return "SpawnerAI";
	}

	@Override
	public Location select(Grid grid) {
		
		while(true) {
			int row = (int)(Math.random()*grid.getRows());
			int col = (int)(Math.random()*grid.getCols());
			
			if(grid.getCell(row, col) == -1)
				return new Location(row, col);
		}
	}
}
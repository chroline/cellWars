public class RandomAI extends CellAI {

	@Override
	public String getAIName() {
		// TODO Auto-generated method stub
		return "RandomAI";
	}

	@Override
	public Location select(Grid grid) {
		// TODO Auto-generated method stub
		return new Location((int)(Math.random()*grid.getRows()), (int)(Math.random()*grid.getCols()));
	}
}
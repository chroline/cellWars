
public abstract class CellAI {
		
	//the id that uniquely identifies the CellAI in the grid
	//id >= 0
	private int id;
	private static int nextID = 0;
	
	public CellAI() {
		id = nextID;
		nextID++;
	}
	
	public final int getID() {
		return id;
	}
	
	public abstract String getAIName();
	
	//if a position holds -1, then it is dead, >= 0 means alive, value is id of CelLAI
	public abstract Location select(Grid grid);
}

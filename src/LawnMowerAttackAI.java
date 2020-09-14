public class LawnMowerAttackAI extends CellAI {

    @Override
    public String getAIName() {
        // TODO Auto-generated method stub
        return "LawnMowerAttackAI";
    }

    @Override
    public Location select(Grid grid) {
        for(int r = 0; r < grid.getRows(); r++) {
            for(int c = 0; c < grid.getCols(); c++) {
                if(grid.getCell(r,c) != -1 && grid.getCell(r,c) != getID()) {
                    return new Location(r, c);
                }
            }
        }
        
        return new Location(0,0);
    }
}
import java.util.ArrayList;

/**
 * "the only real defense is active defense" - Mao Zedong
 *
 * Bacteria are "unicellular microorganisms" that are often fabricated to expand and reproduce. Clusters are groups of
 * the same form of bacteria whose goal is to expand and reproduce, as defined by that form of bacteria. Bacteriostatic
 * agents are formulated to inhibit the growth of bacteria, while bactericides are substances which kill bacteria
 * typically by means of infection or targeted attacks.
 *
 * BactericAIdal treats each cluster of cells in the Game of Life society as a cluster of bacterias, and its main goal
 * is to inhibit and then kill that bacteria. It accomplishes this goal by targeting growing "bacteria" and either
 * infects it or attacks it.
 *
 * Part 1: Targeting
 * The first step of the algorithm implemented by BactericAIdal is targeting a specific which cell to infect or attack.
 * A nested for-loop scans through each cell in the provided grid and selects a cell that matches the following
 * criteria:
 * - is dead
 * - is not going to die in the next round
 * - the most common neighbor is not BactericAIdal
 *
 * In order to determine which cells fit this criteria, the mostCommonNeighbor method on the GridFunctions class is
 * used. An additional method was created called cellWillDie that determines if the provided cell will die in the next
 * round, given its current state.
 *
 * Part 2: Elimination
 * - The cell must not have been subject to a previous attack. This check is done to confirm that the cell is not part
 * of a still life, in which case it is not suitable for a targeted attack but rather an infection. Jump to Part 2b:
 * Infection
 * - Otherwise, jump to Part 2a: Targeted Attack
 *
 * Part 2a: Targeted Attack
 * Once the chosen cell has been deemed suitable for a targeted attack, BactericAIdal will "select" this cell. This will
 * kill the bacterium in the cluster. The theory behind a targeted attack is that by eliminating one of the cells in the
 * cluster, a chain reaction will occur because the structure of the cluster has been dismantled.
 *
 * Part 2b: Infection
 * If the chosen cell has been deemed suitable for an infection, another process must occur before a cell is "selected".
 * The cell must not be part of a previous attack in order to prevent the repeated unsuccessful infection of a still
 * life. If it has been part of a previous unsuccessful attack, the column of the selected cell is moved to the right
 * until:
 * - it is empty
 * - it has not been part of a previous unsuccessful attack
 *
 * This will make sure that the selected cell that will perform the infection is directly to the right of the cluster.
 * If the selected cell can no longer move to the right due to it being on the edge, the row is moved upwards once so
 * that the selected cell is directly on top of the cluster.
 *
 * Part 3: Attack Monitoring
 * To make sure that an unsuccessful infection or attack is not repeated, BactericAIdal maintains a previousAttack
 * ArrayList that keeps track of what locations have been used in a previous attack. When that attack is determined to
 * be complete (i.e. it is no longer attacking the same cluster), the previousAttack list is reset.
 */
public class BactericAIdal extends CellAI {
    ArrayList<Location> previous = new ArrayList<>();

    @Override
    public String getAIName() {
        return "BactericAIdal";
    }

    @Override
    public Location select(Grid grid) {
        boolean resetPrevious = true;

        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                if (grid.getCell(r, c) != -1 && !cellWillDie(grid.getCell(r, c) != -1, GridFunctions.getNeighbors(r, c, grid)) &&
                        GridFunctions.mostCommonNeighbor(r, c, grid) != getID()) {
                    int col = c;
                    if (isInPrevious(r, col)) {
                        resetPrevious = false;
                        while (col < grid.getCols() - 1 && !(grid.getCell(r, col) == -1 || !isInPrevious(r, col)))
                            col++;
                        if (col == grid.getCols() - 1) r--;
                    }
                    Location target = new Location(r, col);
                    if (resetPrevious) previous = new ArrayList<>();
                    previous.add(target);
                    return target;
                }
            }
        }

        return new Location(0, 0);
    }

    private boolean cellWillDie(boolean isAlive, int neighbors) {
        if (isAlive) {
            if (neighbors <= 1) return true;
            if (neighbors >= 4) return true;
            return false;
        }

        if (neighbors == 3) return false;
        return true;
    }

    private boolean isInPrevious(int row, int col) {
        int index = -1;
        for (int i = 0; i < previous.size(); i++) {
            if (previous.get(i).getRow() == row && previous.get(i).getCol() == col) {
                index = i;
                break;
            }
        }
        return index > -1;
    }
}
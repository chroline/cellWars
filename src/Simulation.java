import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

public class Simulation {

    private int sizeMultiplier = 1000;
    private int aisPerRound = 4;
    private int games = 10;
    private int gameNum = 0;
    private ArrayList<Integer> sumOfFinishingPlaces = new ArrayList<Integer>();
    private int[][] headToHead;

    private boolean shouldDelay = true;

    private int[][] grid, tempGrid;

    private ArrayList<Class<?>> aiClasses;
    private ArrayList<CellAI> ais;
    private TreeMap<String, Integer> originalAliveCountByName;
    private TreeMap<String, Color> colorsOfAIs;

    private TreeMap<String, Integer> aliveCountByName;

    private JFrame displayWindow;
    private JTable displayTable;
    private JLabel iterLabel;
    private int iteration;
    private JSlider speed;
    private JButton startButt;

    private ArrayList<CellAI> currentAIs = new ArrayList<CellAI>();
    private ArrayList<CellAI> leftoverAIs = new ArrayList<CellAI>();
    private ArrayList<CellAI> advancingAIs = new ArrayList<CellAI>();

    public static void main(String[] args) {
        new Simulation();
    }

    public Simulation() {

        Color[] colors = {
                Color.BLUE,
                Color.PINK,
                Color.GREEN,
                Color.ORANGE,
                Color.BLACK,
                Color.MAGENTA,
                Color.CYAN
        };

        aiClasses = loadClasses();
        ais = new ArrayList<CellAI>();
        colorsOfAIs = new TreeMap<String, Color>();
        for (int i = 0; i < aiClasses.size(); i++) {
            try {
                ais.add((CellAI) aiClasses.get(i).newInstance());
                colorsOfAIs.put(ais.get(i).getAIName(), colors[i % colors.length]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        headToHead = new int[ais.size()][ais.size()];

        displayWindow = new JFrame("Simulation");

        displayTable = new JTable(new AbstractTableModel() {

            public int getRowCount() {
                if (originalAliveCountByName == null) return 0;
                return currentAIs.size();
            }

            public int getColumnCount() {
                return 3;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {

                try {
                    if (rowIndex >= currentAIs.size())
                        return "";

                    String name = currentAIs.get(rowIndex).getAIName();

                    if (columnIndex == 0)
                        return name;
                    else if (columnIndex == 1) {
                        if (aliveCountByName.containsKey(name))
                            return aliveCountByName.get(name);
                        else
                            return 0;
                    } else if (columnIndex == 2) {
                        return originalAliveCountByName.get(name);
                    }
                } catch (Exception e) {
                }
                return "";
            }

        }) {

            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component comp = super.prepareRenderer(renderer, row, col);
                Object value = getModel().getValueAt(row, col);

                try {
                    if (row < currentAIs.size()) {
                        if (col == 2) {
                            String name = currentAIs.get(row).getAIName();
                            comp.setBackground(colorsOfAIs.get(name));
                        } else {
                            comp.setBackground(Color.white);
                        }
                    }
                } catch (Exception e) {
                }
                return comp;
            }
        };

        JPanel center = new JPanel() {
            public void paint(Graphics g) {

                if (grid == null)
                    return;

                try {
                    int pixelSize = Math.min(getWidth() / grid[0].length, getHeight() / grid.length);
                    pixelSize = Math.max(pixelSize, 3);

                    for (int row = 0; row < grid.length; row++) {
                        for (int col = 0; col < grid[0].length; col++) {

                            if (grid[row][col] >= ais.size() || grid[row][col] < 0)
                                continue;

                            int id = grid[row][col];
                            if (id >= ais.size())
                                continue;

                            CellAI a = ais.get(id);
                            if (a != null) {
                                g.setColor(colorsOfAIs.get(a.getAIName()));

                                int x = ((getWidth() - pixelSize) * col) / grid[0].length;
                                int y = ((getHeight() - pixelSize) * row) / grid.length;

                                g.fillOval(x, y, pixelSize, pixelSize);
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        };

        center.setMinimumSize(new Dimension(600, 600));
        center.setPreferredSize(new Dimension(600, 600));

        displayWindow.getContentPane().setLayout(new BorderLayout());
        displayWindow.getContentPane().add(center, BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setMinimumSize(new Dimension(200, -1));
        right.setPreferredSize(new Dimension(200, -1));
        right.add(displayTable);
        displayWindow.getContentPane().add(right, BorderLayout.EAST);

        iterLabel = new JLabel();
        right.add(iterLabel);

        speed = new JSlider(1, 1000);
        speed.setValue(800);
        right.add(speed);

        startButt = new JButton("Start");
        startButt.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                sumOfFinishingPlaces = new ArrayList<Integer>();
                headToHead = new int[ais.size()][ais.size()];
                for (int i = 0; i < ais.size(); i++)
                    sumOfFinishingPlaces.add(0);
                gameNum = 0;

                startGame();

            }

        });
        right.add(startButt);

        displayWindow.setResizable(false);
        displayWindow.pack();
        displayWindow.setVisible(true);

        displayWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public void updateWinnerResults(CellAI winner, CellAI loser) {
        headToHead[winner.getID()][loser.getID()]++;
    }

    public void startGame() {
        if (!simulating) {

            gameNum++;


            System.out.println("\nPrinting the sum of finishing places (lower = better): ");
            for (int i = 0; i < ais.size(); i++) {
                System.out.println("\t" + ais.get(i).getAIName() + " " + sumOfFinishingPlaces.get(ais.get(i).getID()));
            }
            System.out.println();

            if (gameNum > games) {

                if (aisPerRound == 2) {
                    for (CellAI ai : ais) {
                        String name = ai.getAIName();
                        int id = ai.getID();
                        int wins = 0;
                        int losses = 0;
                        int ties = 0;

                        for (CellAI opp : ais) {
                            String oppName = opp.getAIName();
                            int oppID = opp.getID();

                            if (id != oppID) {

                                System.out.println(name + " vs " + oppName + " : " + headToHead[id][oppID] + "-" + headToHead[oppID][id]);

                                if (headToHead[id][oppID] > headToHead[oppID][id]) wins++;
                                else if (headToHead[id][oppID] > headToHead[oppID][id]) losses++;
                                else ties++;

                            }
                        }

                        System.out.println("Overall WTL: " + wins + "-" + ties + "-" + losses + "\n");
                    }
                }
                //print out results
                return;
            } else {
                System.out.println("Starting game " + gameNum);

            }

            Thread t = new Thread(new Runnable() {
                public void run() {

                    if (currentAIs.size() <= 1 && advancingAIs.size() == 0 && leftoverAIs.size() == 0) {

                        currentAIs.clear();

                        for (CellAI ai : ais) {
                            leftoverAIs.add(ai);
                        }

                        Collections.shuffle(leftoverAIs);

                        populateGrid();
                        runSimulation();
                    }
                }
            });
            t.start();
        }
    }

    private boolean simulating;

    private void runSimulation() {

        simulating = true;
        for (iteration = 0; iteration < 10000 && currentAIs.size() > 1; iteration++) {

            if (shouldDelay) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        iterLabel.setText("Iteration: " + (iteration) + " / " + 10000);
                        displayWindow.repaint();
                    }
                });
                try {
                    Thread.sleep(1001 - speed.getValue());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((AbstractTableModel) displayTable.getModel()).fireTableDataChanged();
            }
            step();
        }

        //cause slackers to die
        while (currentAIs.size() > 1) {

            int minAI = 0;
            int minAIScore = Integer.MAX_VALUE;

            for (int i = 0; i < currentAIs.size(); i++) {
                int alives = aliveCountByName.get(currentAIs.get(i).getAIName());
                if (alives < minAIScore) {
                    minAIScore = alives;
                    minAI = i;
                }
            }

            aiFinished(currentAIs.get(minAI));

            if (aisPerRound == 2)
                updateWinnerResults(currentAIs.get(1 - minAI), currentAIs.get(minAI));

            System.out.println(currentAIs.get(minAI).getAIName() + " just died!");
            currentAIs.remove(minAI);

        }

        simulating = false;
        if (populateGrid())
            runSimulation();
        else
            startGame();
    }

    private void updateAliveCount() {

        Set<String> prevAlive = aliveCountByName.keySet();

        aliveCountByName = new TreeMap<String, Integer>();

        for (int[] row : grid) {
            for (int id : row) {
                CellAI a = id == -1 ? null : ais.get(id);
                if (a != null) {
                    String name = a.getAIName();

                    if (!aliveCountByName.containsKey(name)) aliveCountByName.put(name, 1);
                    else aliveCountByName.put(name, aliveCountByName.get(name) + 1);
                }
            }
        }

        CellAI aliveCell = null;
        CellAI deadCell = null;

        for (String key : prevAlive) {
            if (aliveCountByName.get(key) == null) {
                for (int i = currentAIs.size() - 1; i >= 0; i--) {
                    if (currentAIs.get(i).getAIName().equals(key)) {
                        deadCell = currentAIs.get(i);
                        aiFinished(currentAIs.get(i));
                        System.out.println(key + " just died!");
                        currentAIs.remove(i);
                    } else {
                        aliveCell = currentAIs.get(i);
                    }
                }
            }
        }

        if (aisPerRound == 2 && aliveCell != null && deadCell != null)
            updateWinnerResults(aliveCell, deadCell);
    }

    private boolean populateGrid() {

        //only 1 player in this round - advance them!
        if (currentAIs.size() == 1) {

            if (shouldDelay) {
                displayWindow.repaint();
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
            }

            advancingAIs.add(currentAIs.remove(0));
        }

        //this round is over
        if (currentAIs.size() == 0) {

            //if we have at least 2 left to play, play them
            if (leftoverAIs.size() > 1) {
                for (int i = 0; i < aisPerRound && leftoverAIs.size() != 0; i++) {
                    currentAIs.add(leftoverAIs.remove(0));
                }
            } else { //we have 1 or fewer left to play
                if (leftoverAIs.size() == 1) { //1 left, play them
                    advancingAIs.add(leftoverAIs.remove(0));
                }

                //put the advancing ais into the next round
                while (advancingAIs.size() != 0) {
                    leftoverAIs.add(advancingAIs.remove(0));
                }

                //put some of them in the active set
                for (int i = 0; i < aisPerRound && leftoverAIs.size() != 0; i++) {
                    currentAIs.add(leftoverAIs.remove(0));
                }

                if (currentAIs.size() <= 1) {
                    if (currentAIs.size() == 1) {
                        aiFinished(currentAIs.get(0));
                        System.out.println("The winner is " + currentAIs.get(0).getAIName());
                    }

                    return false;
                }
            }
        } else { //in the middle of a competition
            return false;
        }

        originalAliveCountByName = new TreeMap<String, Integer>();
        aliveCountByName = new TreeMap<String, Integer>();

        //TODO: how big to make the size?
        int size = (int) (Math.sqrt(currentAIs.size() * sizeMultiplier));

        //start with everything dead
        grid = new int[size][size];
        tempGrid = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = -1;
            }
        }

        for (CellAI ai : currentAIs) {
            for (int i = 0; i < 100; i++) {
                int row = (int) (Math.random() * size);
                int col = (int) (Math.random() * size);

                if (grid[row][col] != -1) {
                    i--;
                } else {
                    grid[row][col] = ai.getID();
                }
            }

            originalAliveCountByName.put(ai.getAIName(), 100);

        }

        updateAliveCount();
        return true;
    }

    public void aiFinished(CellAI ai) {
        int place = currentAIs.size() + leftoverAIs.size() + advancingAIs.size();
        int value = sumOfFinishingPlaces.get(ai.getID()) + place;
        this.sumOfFinishingPlaces.set(ai.getID(), value);
    }

    private ArrayList<Class<?>> loadClasses() {
        ArrayList<Class<?>> list = new ArrayList<Class<?>>();

        list.add(RandomAI.class);
        //list.add(LawnMowerAttackAI.class);
        //list.add(SpawnerAI.class);
        list.add(BactericAIdal.class);

        return list;
    }

    private void step() {

        //TODO: step
        Grid g = new Grid(grid);

        CellAI current = currentAIs.remove(0);
        currentAIs.add(current);
        Location loc = current.select(g);

        if (loc.getRow() < 0 || loc.getCol() < 0 || loc.getRow() >= grid.length || loc.getCol() >= grid[0].length) {
            System.out.println(current.getAIName() + " tried to select an invalid row column!");
        } else if (grid[loc.getRow()][loc.getCol()] == -1) { // bring to life
            grid[loc.getRow()][loc.getCol()] = current.getID();
        } else { //kill selected spot
            grid[loc.getRow()][loc.getCol()] = -1;
        }

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                int count = GridFunctions.getNeighbors(i, j, g);

                //alive
                if (grid[i][j] != -1) {
                    //if > 3 or < 1 => die
                    //if 2 or 1 come to life (but as what color?)

                    if (count > 3 || count < 2) {
                        tempGrid[i][j] = -1;
                    } else {
                        tempGrid[i][j] = GridFunctions.mostCommonNeighbor(i, j, g);
                    }
                }
                //dead
                else {
                    //if exactly 3, come to life (but as what color?)
                    //otherwise stay dead

                    if (count != 3) {
                        tempGrid[i][j] = -1;
                    } else {
                        tempGrid[i][j] = GridFunctions.mostCommonNeighbor(i, j, g);
                    }
                }
            }
        }

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = tempGrid[i][j];
            }
        }

        updateAliveCount();
    }

}

class Coordinate implements Comparable<Coordinate> {

    private int row;
    private int col;

    public Coordinate(int r, int c) {
        row = r;
        col = c;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public int compareTo(Coordinate other) {
        // TODO Auto-generated method stub
        int rowDiff = row - other.row;
        if (rowDiff == 0)
            return col - other.col;
        return rowDiff;
    }

}

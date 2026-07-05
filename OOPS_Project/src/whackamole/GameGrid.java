package whackamole;

/**
 * Represents the grid of holes for the game.
 * Thread-safe: methods are synchronized for access from UI and GameEngine.
 */
public class GameGrid {

    private final HoleOccupant[][] grid;
    private final int rows;
    private final int cols;

    public GameGrid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new HoleOccupant[rows][cols];
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public synchronized HoleOccupant getOccupant(int row, int col) {
        return grid[row][col];
    }

    public synchronized void setOccupant(int row, int col, HoleOccupant occupant) {
        if (grid[row][col] != null && grid[row][col].isVisible() && occupant != null) {
            throw new InvalidGameStateException("Hole already occupied at (" + row + ", " + col + ")");
        }
        grid[row][col] = occupant;
    }

    /**
     * Called when user clicks a hole.
     *
     * @return score change from whacking this hole.
     */
    public synchronized int whack(int row, int col) {
        HoleOccupant occ = grid[row][col];
        if (occ == null) {
            return 0;
        }
        int delta = occ.whack();
        if (!occ.isVisible()) {
            grid[row][col] = null;
        }
        return delta;
    }



    /**
     * Ticks all active occupants each game tick.
     */
    public synchronized void tickAll() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                HoleOccupant occ = grid[r][c];
                if (occ != null) {
                    occ.tick();
                    if (!occ.isVisible()) {
                        grid[r][c] = null;
                    }
                }
            }
        }
    }

    /**
     * Clears all occupants from the grid.
     */
    public synchronized void clear() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = null;
            }
        }
    }
}

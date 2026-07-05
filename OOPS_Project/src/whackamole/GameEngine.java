package whackamole;

import javax.swing.SwingUtilities;
import java.util.Random;

/**
 * Runs the core game loop on a background thread.
 * Manages score, time remaining, and object spawning.
 */
public class GameEngine implements Runnable {

    private final GameGrid grid;
    private final GameUpdateListener listener;
    private final Random random = new Random();

    private volatile boolean running = true;
    private int score = 0;
    private int timeRemainingSeconds;

    public GameEngine(GameGrid grid, GameUpdateListener listener, int initialTimeSeconds) {
        this.grid = grid;
        this.listener = listener;
        this.timeRemainingSeconds = initialTimeSeconds;
    }

    public synchronized int getScore() {
        return score;
    }

    public synchronized int getTimeRemainingSeconds() {
        return timeRemainingSeconds;
    }

    /**
     * Called from the UI thread when a hole is clicked.
     */
    public void whackHole(int row, int col) {
        int delta;
        synchronized (grid) {
            delta = grid.whack(row, col);
        }
        synchronized (this) {
            score += delta;
        }
        requestUiUpdate();
    }

    private void requestUiUpdate() {
        if (listener != null) {
            // CRITICAL: ensure UI updates happen on EDT
            SwingUtilities.invokeLater(listener::onGameStateChanged);
        }
    }

    private void spawnRandomOccupant() {
        int row = random.nextInt(grid.getRows());
        int col = random.nextInt(grid.getCols());

        int type = random.nextInt(10);
        HoleOccupant occ;

        if (type < 6) {
            occ = new Mole(3);
        } else if (type < 8) {
            occ = new Bomb(3);
        } else {
            occ = new BonusMole(2);
        }

        synchronized (grid) {
            if (grid.getOccupant(row, col) == null) {
                grid.setOccupant(row, col, occ);
            }
        }
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            while (running && getTimeRemainingSeconds() > 0) {
                Thread.sleep(1000); // pacing

                synchronized (this) {
                    timeRemainingSeconds--;
                }

                synchronized (grid) {
                    grid.tickAll();
                }

                spawnRandomOccupant();
                requestUiUpdate();
            }
        } catch (InterruptedException e) {
            // graceful shutdown when main window calls interrupt()
            running = false;
        }
    }
}

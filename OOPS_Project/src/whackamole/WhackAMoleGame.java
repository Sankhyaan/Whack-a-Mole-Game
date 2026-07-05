package whackamole;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Swing GUI for the Whack-a-Mole game.
 */
public class WhackAMoleGame extends JFrame implements GameUpdateListener {

    // ====== CONFIGURABLE GRID SIZE ======
    private static final int GRID_ROWS = 5;
    private static final int GRID_COLS = 5;

    private static final int GAME_TIME_SECONDS = 30;

    // Top bar labels
    private final JLabel scoreLabel = new JLabel("Score: 0");
    private final JLabel highScoreLabel = new JLabel("High Score: 0");
    private final JLabel timeLabel = new JLabel("Time: " + GAME_TIME_SECONDS + "s");

    private final JButton[][] holeButtons = new JButton[GRID_ROWS][GRID_COLS];

    // Bottom control buttons
    private final JButton startButton = new JButton("Start Game");
    private final JButton exitButton = new JButton("Exit");

    private final GameGrid grid = new GameGrid(GRID_ROWS, GRID_COLS);
    private final HighScoreManager highScoreManager = new HighScoreManager();
    private List<PlayerScore> highScores = new ArrayList<>();

    private GameEngine engine;
    private Thread gameThread;

    // ====== ROUND BUTTON CLASS FOR HOLES ======
    private static class RoundButton extends JButton {
        public RoundButton() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // dark circle for the hole
            g2.setColor(new Color(45, 45, 60));
            int diameter = Math.min(getWidth(), getHeight()) - 6;
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;
            g2.fillOval(x, y, diameter, diameter);

            g2.dispose();

            // paint icon/text on top
            super.paintComponent(g);
        }

        @Override
        public boolean contains(int x, int y) {
            // make click area circular (optional)
            int diameter = Math.min(getWidth(), getHeight()) - 6;
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int rx = x - cx;
            int ry = y - cy;
            return rx * rx + ry * ry <= (diameter / 2) * (diameter / 2);
        }
    }

    // ====== CONSTRUCTOR ======
    public WhackAMoleGame() {
        super("Whack-a-Mole");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        loadExistingScores();
        initTopPanel();
        initGridPanel();
        initBottomPanel();

        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onWindowClosing();
            }
        });
    }

    // ====== TOP BAR ======
    private void initTopPanel() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        Color barDark = new Color(25, 28, 40);
        top.setBackground(barDark);

        JPanel center = new JPanel(new GridLayout(1, 3));
        center.setOpaque(false);

        scoreLabel.setHorizontalAlignment(SwingConstants.LEFT);
        highScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        Font barFont = scoreLabel.getFont().deriveFont(Font.BOLD, 14f);
        scoreLabel.setFont(barFont);
        highScoreLabel.setFont(barFont);
        timeLabel.setFont(barFont);

        scoreLabel.setForeground(Color.WHITE);
        highScoreLabel.setForeground(Color.WHITE);
        timeLabel.setForeground(Color.WHITE);

        center.add(scoreLabel);
        center.add(highScoreLabel);
        center.add(timeLabel);

        top.add(center, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
    }

    // ====== GRID (HOLE AREA) ======
    private void initGridPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(GRID_ROWS, GRID_COLS, 16, 16));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        gridPanel.setBackground(new Color(80, 180, 70));
        // dark board background

        Dimension cellSize = new Dimension(100, 100);

        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                final int row = r;
                final int col = c;

                JButton button = new RoundButton();
                button.setPreferredSize(cellSize);

                button.addActionListener(e -> {
                    if (engine != null) {
                        engine.whackHole(row, col);
                    }
                });

                holeButtons[r][c] = button;
                gridPanel.add(button);
            }
        }
        add(gridPanel, BorderLayout.CENTER);
    }

    // ====== BOTTOM BAR (START / EXIT) ======
    private void initBottomPanel() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        Color barDark = new Color(25, 28, 40);
        bottom.setBackground(barDark);

        startButton.setFocusPainted(false);
        exitButton.setFocusPainted(false);

        startButton.setPreferredSize(new Dimension(120, 32));
        exitButton.setPreferredSize(new Dimension(120, 32));

        startButton.addActionListener(e -> {
            if (gameThread != null && gameThread.isAlive()) {
                return; // ignore while running
            }
            startGame();
        });

        exitButton.addActionListener(e -> onWindowClosing());

        bottom.add(startButton);
        bottom.add(exitButton);

        add(bottom, BorderLayout.SOUTH);
    }

    // ====== GAME START / RESTART ======
    private void startGame() {
        // stop any previous engine/thread
        if (engine != null) {
            engine.stop();
        }
        if (gameThread != null && gameThread.isAlive()) {
            gameThread.interrupt();
        }

        grid.clear();
        refreshGridIcons();

        scoreLabel.setText("Score: 0");
        timeLabel.setText("Time: " + GAME_TIME_SECONDS + "s");

        engine = new GameEngine(grid, this, GAME_TIME_SECONDS);
        gameThread = new Thread(engine, "GameEngineThread");
        gameThread.start();

        startButton.setEnabled(false);
        startButton.setText("Game Running");
    }

    // ====== HIGH SCORES LOAD/SAVE ======
    private void loadExistingScores() {
        try {
            highScores = highScoreManager.loadScores();
        } catch (HighScoreException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not load previous scores. Starting fresh.",
                    "High Score Error", JOptionPane.WARNING_MESSAGE);
            highScores = new ArrayList<>();
        }
        updateHighScoreLabel();
    }

    private void saveScores() {
        try {
            highScoreManager.saveScores(highScores);
        } catch (HighScoreException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not save scores.",
                    "High Score Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onWindowClosing() {
        if (engine != null) {
            engine.stop();
        }
        if (gameThread != null && gameThread.isAlive()) {
            gameThread.interrupt();
        }
        saveScores();
        dispose();
        System.exit(0);
    }

    // ====== GAMEUPDATE LISTENER ======
    @Override
    public void onGameStateChanged() {
        int score = engine.getScore();
        int time = engine.getTimeRemainingSeconds();

        scoreLabel.setText("Score: " + score);
        timeLabel.setText("Time: " + time + "s");

        refreshGridIcons();

        if (time <= 0) {
            endGame();
        }
    }

    private void refreshGridIcons() {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                HoleOccupant occ = grid.getOccupant(r, c);
                Icon icon = (occ == null) ? null : occ.getIcon();
                holeButtons[r][c].setIcon(icon);
            }
        }
    }

    // ====== END GAME / HIGH SCORE DIALOG ======
    private void endGame() {
        startButton.setEnabled(true);
        startButton.setText("Play Again");

        String name = JOptionPane.showInputDialog(this,
                "Game over! Your score: " + engine.getScore() + "\nEnter your name:");
        if (name != null && !name.trim().isEmpty()) {
            highScores.add(new PlayerScore(name.trim(), engine.getScore()));
        }
        saveScores();
        updateHighScoreLabel();

        StringBuilder sb = new StringBuilder("High Scores:\n");
        for (PlayerScore ps : highScores) {
            sb.append(ps).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "High Scores",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateHighScoreLabel() {
        int best = 0;
        for (PlayerScore ps : highScores) {
            if (ps.getScore() > best) {
                best = ps.getScore();
            }
        }
        highScoreLabel.setText("High Score: " + best);
    }

    // ====== MAIN ======
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WhackAMoleGame game = new WhackAMoleGame();
            game.setVisible(true);
        });
    }
}


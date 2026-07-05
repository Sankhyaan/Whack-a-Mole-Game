package whackamole;

import java.io.Serializable;

/**
 * Serializable POJO representing a high score entry.
 */
public class PlayerScore implements Serializable, Comparable<PlayerScore> {

    private static final long serialVersionUID = 1L;

    private String playerName;
    private int score;

    public PlayerScore(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    @Override
    public int compareTo(PlayerScore other) {
        // Sort descending by score
        return Integer.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return playerName + " - " + score;
    }
}


package whackamole;

/**
 * Custom checked exception for high-score loading/saving problems.
 */
public class HighScoreException extends Exception {

    public HighScoreException(String message) {
        super(message);
    }

    public HighScoreException(String message, Throwable cause) {
        super(message, cause);
    }
}

package whackamole;

/**
 * Custom unchecked exception for impossible game state errors.
 */
public class InvalidGameStateException extends RuntimeException {

    public InvalidGameStateException(String message) {
        super(message);
    }
}

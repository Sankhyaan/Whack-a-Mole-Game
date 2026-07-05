package whackamole;

/**
 * Listener used by GameEngine to request safe UI updates.
 */
public interface GameUpdateListener {
    void onGameStateChanged();
}

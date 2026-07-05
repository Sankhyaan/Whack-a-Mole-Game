package whackamole;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Bomb: penalty when whacked.
 */
public class Bomb extends HoleOccupant {

    public Bomb(int ticksRemaining) {
        super(ticksRemaining);
    }

    @Override
    public int whack() {
        if (!visible) {
            return 0;
        }
        hide();
        // Could also trigger game-over logic in GameEngine if desired
        return -500;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("/whackamole/bomb.png"));
    }
}

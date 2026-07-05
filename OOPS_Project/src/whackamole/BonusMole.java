package whackamole;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * High-value or bonus mole.
 */
public class BonusMole extends HoleOccupant {

    public BonusMole(int ticksRemaining) {
        super(ticksRemaining);
    }

    @Override
    public int whack() {
        if (!visible) {
            return 0;
        }
        hide();
        return 1000; // +1000 points (or award bonus time in GameEngine)
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("/whackamole/bonus_mole.png"));
    }
}

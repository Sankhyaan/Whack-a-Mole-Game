package whackamole;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Standard mole: positive score when whacked.
 */
public class Mole extends HoleOccupant {

    public Mole(int ticksRemaining) {
        super(ticksRemaining);
    }

    @Override
    public int whack() {
        if (!visible) {
            return 0;
        }
        hide();
        return 100; // +100 points
    }

    @Override
    public Icon getIcon() {
        // Replace "mole.png" with your actual resource if you have one.
        return new ImageIcon(getClass().getResource("/whackamole/mole.png"));
    }
}

package whackamole;

import javax.swing.Icon;

/**
 * Abstract base class for any object that can appear in a hole.
 */
public abstract class HoleOccupant {
    protected boolean visible;
    protected int ticksRemaining;

    public HoleOccupant(int ticksRemaining) {
        this.visible = true;
        this.ticksRemaining = ticksRemaining;
    }

    /**
     * Called once per game tick by the GameEngine to update lifetime.
     */
    public void tick() {
        if (!visible) {
            return;
        }
        ticksRemaining--;
        if (ticksRemaining <= 0) {
            hide();
        }
    }

    public void hide() {
        this.visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    /**
     * Called when the user successfully whacks this occupant.
     *
     * @return change in score (positive, negative, or zero)
     */
    public abstract int whack();

    /**
     * Icon to display for this occupant in the GUI.
     */
    public abstract Icon getIcon();
}

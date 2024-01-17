package battlecode.common;

/**
 * Enumerates the possible types of global updates. More information about each
 * update
 * are available in the game specs.
 */

public enum GlobalUpgrade {

    /**
     * Attack upgrade increases the base attack by 75.
     */
    ATTACK(75, 0, 0, 0),

    /**
     * Healing increases base heal by 50.
     */
    HEALING(0, 50, 0, 0),

    /**
     * Capture upgrade increases the dropped flag delay from 4 rounds to 12 rounds.
     */
    CAPTURING(0, 0, 8, -8);

    /**
     * How much base attack changes
     */
    public final int baseAttackChange;

    /**
     * How much base heal changes
     */
    public final int baseHealChange;

    /**
     * How much dropped flag return delay changes
     */
    public final int flagReturnDelayChange;

    /**
     * How much the movement penalty for holding a flag changes.
     */
    public final int movementDelayChange;

    GlobalUpgrade(int baseAttackChange, int baseHealChange, int flagReturnDelayChange, int movementDelayChange) {
        this.baseAttackChange = baseAttackChange;
        this.baseHealChange = baseHealChange;
        this.flagReturnDelayChange = flagReturnDelayChange;
        this.movementDelayChange = movementDelayChange;
    }
}

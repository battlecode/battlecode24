package battlecode.common;

/**
 * Enumerates the possible types of global updates. More information about each update
 *  are available in the game specs.
 */

public enum GlobalUpgrade {

    /**
     * Action upgrade increases the amount cooldown drops per round by 6.
     * 
     * @battlecode.doc.globalupgrade
     */
    ACTION(4, 0, 0),

    /**
     * Healing increases base heal by 10.
     * 
     * @battlecode.doc.globalupgrade
     */
    HEALING(0, 10, 0),

    /**
     * Capture delays the return of a dropped flag by 8 rounds.
     * 
     * @battlecode.doc.globalupgrade
     */
    CAPTURING(0, 0, 4);

    /**
     * How much cooldown reduction changes
     */
    public final int cooldownReductionChange;

    /**
     * How much base heal changes
     */
    public final int baseHealChange;

    /**
     * how much dropped flag return delay changes
     */
    public final int flagReturnDelayChange;

    GlobalUpgrade(int cooldownReductionChange, int baseHealChange, int flagReturnDelayChange){
        this.cooldownReductionChange = cooldownReductionChange;
        this.baseHealChange = baseHealChange;
        this.flagReturnDelayChange = flagReturnDelayChange;
    }
}
package battlecode.world;

/**
 * Determines roughly by how much the winning team won.
 */
public enum DominationFactor {
    /**
     * Win by capturing all opponent flags.
     */
    CAPTURE,
    /**
     * Win by capturing more flags (tiebreak 1).
     */
    MORE_FLAG_CAPTURES, 
    /**
     * Win by having a greater sum of levels across all units (including robots in jail) (tiebreak 2).
     */
    SUM_LEVELS,
    /**
     * Win by killing more enemy units (tiebreak 3).
     */
    MORE_ENEMIES_KILLED,
    /**
     * Win by picking up more flags (even if not retrieved successfully) (tiebreak 3).
     */
    MORE_FLAGS_PICKED, 
    /**
     * Win by coinflip (tiebreak 4).
     */
    WON_BY_DUBIOUS_REASONS,
    /**
     * Win because the other team resigns.
     */
    RESIGNATION;
}

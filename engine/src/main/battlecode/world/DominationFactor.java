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
     * Win by having a higher cumulative robot level (tiebreak 2)
     */
    LEVEL_SUM,
    /**
     * Win by having more break (tiebreak 3)
     */
    MORE_BREAD,
    /**
     * Win by picking up more flags (even if not retrieved successfully) (tiebreak 4).
     */
    MORE_FLAGS_PICKED, 
    /**
     * Win by coinflip (tiebreak 5).
     */
    WON_BY_DUBIOUS_REASONS,
    /**
     * Win because the other team resigns.
     */
    RESIGNATION;
}

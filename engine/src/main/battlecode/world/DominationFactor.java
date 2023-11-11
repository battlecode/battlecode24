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
     * Win by having more tier three units (including robots in jail) (tiebreak 2).
     */
    TIER_THREE,
    /**
     * Win by having more tier two units (including robots in jail) (tiebreak 2).
     */
    TIER_TWO,
    /**
     * Win by having more break (tiebreak 3)
     */
    MORE_BREAD,
    /**
     * Win by picking up more flags (even if not retrieved successfully) (tiebreak 4).
     */
    MORE_FLAGS_PICKED, 
    /**
     * Win by completing more of a randomly chosen task (tiebreak 5).
     */
    RANDOM_TASK,
    /**
     * Win by coinflip (tiebreak 6).
     */
    WON_BY_DUBIOUS_REASONS,
    /**
     * Win because the other team resigns.
     */
    RESIGNATION;
}

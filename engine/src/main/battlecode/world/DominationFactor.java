package battlecode.world;

/**
 * Determines roughly by how much the winning team won.
 */
public enum DominationFactor {
    /**
     * Win by all enemy archons being destroyed (early end).
     */
    ANNIHILATED,
    /**
     * Win by having more Archons.
     */
    MORE_ARCHONS,
    /**
     * Win by more gold net worth (tiebreak 1).
     */
    MORE_GOLD_NET_WORTH,
    /**
     * Win by more lead net worth (tiebreak 2).
     */
    MORE_LEAD_NET_WORTH,
    /**
     * Win by coinflip (tiebreak 3).
     */
    WON_BY_DUBIOUS_REASONS,
}

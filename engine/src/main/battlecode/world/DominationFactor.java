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
     * Win by capturing 75+% of sky islands (early end).
     */
    CONQUEST,
    /**
     * Win by having more sky islands captured.
     */
    MORE_SKY_ISLANDS,
    /**
     * Win by having placed more reality anchors in total (tiebreak 1).
     */
    MORE_REALITY_ANCHORS, 
    /**
     * Win by more elixir net worth (tiebreak 2).
     */
    MORE_ELIXIR_NET_WORTH,
    /**
     * Win by more mana net worth (tiebreak 3).
     */
    MORE_MANA_NET_WORTH,
    /**
     * Win by more adamantium net worth (tiebreak 4).
     */
    MORE_ADAMANTIUM_NET_WORTH, 
    /**
     * Win by coinflip (tiebreak 5).
     */
    WON_BY_DUBIOUS_REASONS,
    /**
     * Win because the other team resigns.
     */
    RESIGNATION,
}

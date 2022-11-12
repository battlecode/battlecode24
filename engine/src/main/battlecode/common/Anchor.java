package battlecode.common;

/**
 * Anchor represents the types of anchors that exist in the world. 
 * Anchor provides information about what is needed to build an anchor
 * of that type and what that anchor does when it is planted on an island.
 */
public enum Anchor {
    
    // Turns Needed to Remove Anchor, Radius of Range of Units Affected, Acceleration Factor Change on Affected Units,
    // Cost in Mana, Cost in Adamantium, Cost in Elixir

    /**
     * Standard anchors are built out of standard type anchors and only take 15 turns
     * to remove. They also do not have any other affects.
     */
    STANDARD       (15,    0,   1.0f, 100, 100, 0),

    /**
     * Accelerating anchors are built out of elixir (the strongest element) and take 40 turns
     * to remove. They also power up nearby units of your team.
     */
    ACCELERATING      (40,    100,   1.15f, 0, 0, 300);

    /**
     * How long this type of anchor takes to remove.
     */
    public final int turnsToRemove;

    /**
     * The number of units of the island within which this anchor has an effect.
     */
    public final int unitsAffected;

    /**
     * The acceleration that this anchor causes on friendly nearby units within range.
     */
    public final float accelerationFactor;

    /**
     * The cost to build this anchor in mana.
     */
    public final int manaCost;

    /**
     * The cost to build this anchor in adamantium.
     */
    public final int adamantiumCost;

    /**
     * The cost to build this anchor in elixir.
     */
    public final int elixirCost;

    Anchor(int turnsToRemove, int unitsAffected, float accelerationFactor, int manaCost, int adamantiumCost, int elixirCost) {
        this.turnsToRemove     = turnsToRemove;
        this.unitsAffected       = unitsAffected;
        this.accelerationFactor    = accelerationFactor;
        this.manaCost = manaCost;
        this.adamantiumCost = adamantiumCost;
        this.elixirCost = elixirCost;
    }
}
    
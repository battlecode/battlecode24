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
     * Standard anchors are built out of standard type anchors and only have 250 health. They also do not have any other affects.
     */
    STANDARD       (250,    0,   0.0f, 100, 100, 0),

    /**
     * Accelerating anchors are built out of elixir (the strongest element) and have 750 health. They also power up nearby units of your team.
     */
    ACCELERATING      (750,    4,   -0.15f, 0, 0, 300);

    /**
     * How long this type of anchor takes to remove.
     */
    public final int totalHealth;

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

    public int getBuildCost(ResourceType rType) {
        switch (rType) {
            case ADAMANTIUM:
                return this.adamantiumCost;
            case MANA:
                return this.manaCost;
            case ELIXIR:
                return this.elixirCost;
            default:
                return 0;
        }
    }

    public int getAccelerationIndex() {
        return (this == STANDARD) ? 0 : 1;
    }

    Anchor(int totalHealth, int unitsAffected, float accelerationFactor, int manaCost, int adamantiumCost, int elixirCost) {
        this.totalHealth     = totalHealth;
        this.unitsAffected       = unitsAffected;
        this.accelerationFactor    = accelerationFactor;
        this.manaCost = manaCost;
        this.adamantiumCost = adamantiumCost;
        this.elixirCost = elixirCost;
    }
}
    
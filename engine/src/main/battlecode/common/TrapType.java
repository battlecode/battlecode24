package battlecode.common;

/**
 * Enumerates possible traps that can be built.
 */

public enum TrapType {

    // build cost, squared radius if entered, squared radius if dug, damange if entered, damage if dug, doesDig, actionCooldownIncrease, isInvisible

    /**
     * When an opponent enters, explosive traps deal 750 damage to all opponents within a sqrt 13 radius
     * If an opponent digs/breaks under the trap, it deals 500 damage to all opponnets in radius sqrt 9
     */
    EXPLOSIVE (250, 0, 13, 9, 750, 500, false, 5, true, 0),

    /**
     * When an opponent enters, water traps dig all unoccupied tiles within a radius of sqrt 9
     */
    WATER (100, 2, 9, 0, 0, 0, true, 5, true, 0),

    /**
     * When an opponent enters, all opponent robots movement and action cooldowns are set to 40.
     */
    STUN (100, 2, 13, 0, 0, 0, false, 5, true, 40),

    NONE (100, 0, 0, 0, 0, 0, false, 0, false, 0);
    
    /**
     * Crumbs cost of each trap
     */
    public final int buildCost;

    
     /**
     * The radius that the trap can be triggered from by building
     */
    public final int triggerRadius;

    /**
     * The radius of effect if trap is triggered by opponent entering a location
     */
    public final int enterRadius;


    /**
     * The radius of effect if trap is triggered by opponent building/breaking at a location
     */
    public final int interactRadius;

    /**
     * The damage done if trap triggered by opponent entering a location
     */
    public final int enterDamage;


    /**
     * The damagge done if trap triggered by opponent building/breaking at location
     */
    public final int interactDamage;


    /**
     * if the trap digs all nonoccupied locations within its radius
     */
    public final boolean doesDig;

    /**
     * What action cooldown is increased by within that radius
     */
    public final int actionCooldownIncrease;

    /**
     * If trap is invisible to opponents
     */
    public final boolean isInvisible;

    /**
     * Cooldown for opponents
     */
    public final int opponentCooldown;

    TrapType(int buildCost, int triggerRadius, int enterRadius, int interactRadius, int enterDamage, int interactDamage, boolean doesDig, int actionCooldownIncrease, boolean isInvisible, int opponentCooldown){
        this.buildCost = buildCost;
        this.triggerRadius = triggerRadius;
        this.enterRadius = enterRadius;
        this.interactRadius = interactRadius;
        this.enterDamage = enterDamage;
        this.interactDamage = interactDamage;
        this.doesDig = doesDig;
        this.actionCooldownIncrease = actionCooldownIncrease;
        this.isInvisible = isInvisible;
        this.opponentCooldown = opponentCooldown;
    }
}
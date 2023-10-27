package battlecode.common;

/**
 * Enumerates possible traps that can be built by aquaforming.
 */

public enum TrapType {

    // build cost, squared radius if entered, squared radius if dug, damange if entered, damage if dug, doesDig, actionCooldownIncrease, isInvisible

    /**
     * When an opponent enters, explosive traps deal 75 damage to all opponents within a sqrt 13 radius
     * If an opponent digs/breaks under the trap, it deals 50 damage to all opponnets in radius sqrt 9
     * 
     * @battlecode.doc.traptype
     */
    EXPLOSIVE (25, 13, 9, 75, 50, false, 0, true),

    /**
     * When an opponent enters, water traps dig all unoccupied tiles within a radius of sqrt 9
     * 
     * @battlecode.doc.traptype
     */
    WATER (10, 9, 0, 0, 0, true, 0, true),

    /**
     * When an opponent enters, all opponent robots movement and action cooldowns are set to 40.
     * 
     * @battlecode.doc.traptype
     */
    STUN (10, 13, 0, 0, 0, false, 40, true);

    /**
     * Bread cost of each trap
     */
    public final int buildCost;


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

    TrapType(int buildCost, int enterRadius, int interactRadius, int enterDamage, int interactDamage, boolean doesDig, int actionCooldownIncrease, boolean isInvisible){
        this.buildCost = buildCost;
        this.enterRadius = enterRadius;
        this.interactRadius = interactRadius;
        this.enterDamage = enterDamage;
        this.interactDamage = interactDamage;
        this.doesDig = doesDig;
        this.actionCooldownIncrease = actionCooldownIncrease;
        this.isInvisible = isInvisible;
    }
}
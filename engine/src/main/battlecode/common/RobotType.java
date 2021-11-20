package battlecode.common;

/**
* Contains the two currency types for supporting robot build costs.
*/
public enum CurrencyType {
    AU, PB
}

/**
 * Contains details on various attributes of the different robots. All of this information is in the specs in a more organized form.
 */

PLACEHOLDER_ACTION_RADIUS = 100;
PLACEHOLDER_VISION_RADIUS = 100;
PLACEHOLDER_BYTECODE_LIMIT = 7500;
NO_COOLDOWN_CLOCK = 10000000;

public enum RobotType {

    // Build Cost Lead, Build Cost Gold, Action Cooldown, Move Cooldown
    // DPS Lv1, HP Lv1, Action Radius (squared), Vision Radius (squared), Reclaim Cost Percentage, Bytecode Limit, RicochetCount

    /**
     * Archons are portable buildings that heal and generate robots.
     * Losing all archons means losing the game.
     *
     * @battlecode.doc.robottype
     */
    ARCHON          (0,   250, 10, NO_COOLDOWN_CLOCK, -5, 700, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0.2, 0, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL, BCG, AC                 MC DPS   HP                         AR                          VR  RCP RC,                         BL
    /**
     * A lead robot
     * Can mine gold or lead at their or an adjacent location.
     *
     * @battlecode.doc.robottype
     */
    MINER           ( 50,   0, 2,       10,   0,  40, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS,  0, 0, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL, BCG, AC       MC  DPS   HP                         AR                         VR RCP  RC                          BL
    /**
     * A lead robot
     * Can build, repair, and upgrade non-Archon buildings.
     *
     * @battlecode.doc.robottype
     */
    BUILDER         ( 40,  0,  10,       10, -10, 30, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0, 0, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL,BCG,  AC        MC  DPS  HP                         AR                        VR RCP  RC                          BL
    /**
     * Alchemist's laboratory
     * Converts lead into gold
     *
     * @battlecode.doc.robottype
     */
    LABORATORY      (800,   0, 10, NO_COOLDOWN_CLOCK, 0, 500, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0.2, 0, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL, BCG, AC                  MC DPS HP                         AR                        VR   RCP RC                          BL

    /**
     * Lead attacking robot, low-range, medium-damage.
    */
    GUARD           ( 75,   0,  10,       20, 35, 120, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS,   0, 0, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL, BCG,  AC        MC DPS   HP                         AR                          VR RCP RC                         BL
    
    /**
     * Lead attacking robot, high-range, low-damage.
    */
    ARCHER          ( 75,  0,  10,       15, 20,  80, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS,    0, 0, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL, BCG,  AC        MC  DPS  HP                         AR                          VR RCP  RC                          BL

    /**
     * Gold robot, medium-range, high-damage.
     */
    WIZARD          ( 0,   50,  20,       20, 75, 70, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS,  0,  0, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL, BCG,  AC        MC DPS   HP                        AR                          VR RCP RC                          BL

    /**
    * Guard turret 
    */
    // TODO These are placeholder values only
    GUARD_TURRET   ( 50,    0,  20,       20, 75, 70, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0.2,  0,  PLACEHOLDER_BYTECODE_LIMIT),
    //               BCA, BCG,  AC        MC DPS   HP                        AR                          VR RCP RC                           BL

    /**
    * Archer turret
    */
    // TODO These are placeholder values only
    ARCHER_TURRET   ( 50,   0,  20,       20, 75, 70, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0.2, 0, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL, BCG,  AC        MC DPS   HP                        AR                          VR RCP RC                          BL

    /**
    * Wizard turret
    * Has upgradeable ricochet count.
    */
    // TODO These are placeholder values only 
    WIZARD_TURRET   ( 50,   0,  20,       20, 75, 70, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0.2, 2, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL, BCG,  AC        MC DPS   HP                        AR                          VR RCP BL                          RC
    ;

    /**
     * Lead cost to build a given robot or building 
     */
    public final int buildCostLead;

    /**
     * Gold cost to build a given robot or building
     */
    public final int buildCostGold;

    /**
     * Action cooldown.
    */
    public final int actionCooldown;

    /**
     * Move cooldown.
     */
    public final int moveCooldown;

    /**
    * Damage per second for each robot in Level 1.
    */
    public final float DPSLv1;
    
    /**
    * Initial health per robot for Level 1.
    */
    public final float HPLv1;

    /**
     * Radius range of robots' abilities.
     */
    public final int actionRadiusSquared;

    /**
     * The radius range in which the robot can sense another
     * robot's information.
     */
    public final int visionRadiusSquared;

    /**
     * Base bytecode limit of this robot.
     */
    public final int bytecodeLimit;

    /**
     * Ricochet count.
     */
    public final int ricochetCount;

    /**
     * @return the squared action radius.
     */
    public int getActionRadiusSquared(int level) {
        return this.actionRadiusSquared;
    }

    /**
     * @return the squared vision radius.
     */
    public int getVisionRadiusSquared(int level) {
        return this.visionRadiusSquared;
    }


    /**
     * @return whether or not a given robot is a building.
    */
    public boolean isBuilding() {
        return (
            this == ARCHON
            || this == LABORATORY
            || this == GUARD_TURRET
            || this == ARCHER_TURRET
            || this == WIZARD_TURRET
        );
    }


    /**
     * Returns whether the type can build robots of the specified type.
     *
     * @param type the RobotType to be built
     * @return whether the type can build robots of the specified type
     */
    public boolean canBuild(RobotType type) {
        return this == type.spawnSource;
    }

    /**
     * Returns the max HP for a robot by level.
     * @param level of the robot
     * @return the max HP for a robot by level.
     */
    public int getMaxHealth(int level) {
        int health = type.HPLv1;
        if(level >= 2)
            health *= GameConstants.HP1_TO_2;
        if(level == 3)
            health *= GameConstants.HP_2_TO_3;
        
        return (int) health;

    }

    /**
     * Returns the starting HP for a robot by level.
     * @param level of the robot
     * @return the starting HP for a robot by level.
     */
    public int getStartingHealth(int level) {
        return (int) (type.HPLv1 * GameConstants.PROTOTYPE_STARTING_HEALTH_MULTIPLIER);
    }

    /**
     * @param level
     * @return the DPS for a robot by level.
     */
    public int getDamage(int level) {

        int dps = type.DPSLv1;
        if(level >= 2)
            dps *= GameConstants.DPS1_TO_2;
        if(level == 3)
            dps *= GameConstants.DPS_2_TO_3;

        return (int) dps;
    }

    /**
     * @param level
     * @return the ricochet count by level, controlled by a multiplier and rounded down.
     */
    public int getRicochetCount(int level) {
        return (int) (this.ricochetCount * Math.pow(GameConstants.RICOCHET_UPGRADE_MULTIPLIER, level - 1));
    }


    /**
     * @param level
     * @return the amount of lead dropped, based on a multiplier by level.
     */
    public int getLeadWorth(int level) {
        return this.getCurrencyWorth(this.buildCostLead, level);
    }

    /**
     * @param level
     * @return the amount of gold dropped, based on a multiplier by level.
     */
    public int getGoldWorth(int level) {
        return this.getCurrencyDropped(this.buildCostGold, level);
    }

    /**
     * Calculates the total cost of a building based on level/upgrade multiplier.
     * @param level 
     * @param baseWorth to build the building
     * @return worth of building in the currency
     */
    private int getCurrencyWorth(int baseWorth, int level) {
        return (int) (this.buildCostAmount * Math.pow(BUILDING_UPGRADE_COST_MULTIPLIER, level - 1));
    }

    public int getLeadDropped(int level) {

    }

    public int getGoldDropped(int level) {

    }

    RobotType(int buildCostLead, int buildCostGold, int actionCooldown, int moveCooldown,
        int DPSLv1, int HPLv1, int actionRadiusSquared, int visionRadiusSquared, int ricochetCount, int bytecodeLimit) {

        this.buildCostLead                  = buildCostLead;
        this.buildCostGold                  = buildCostGold;
        this.actionCooldown                 = actionCooldown;
        this.moveCooldown                   = moveCooldown;
        this.DPSLv1                         = DPSLv1;
        this.HPLv1                          = HPLv1;
        this.actionRadiusSquared            = actionRadiusSquared;
        this.visionRadiusSquared            = visionRadiusSquared;
        this.ricochetCount                  = ricochetCount;
        this.bytecodeLimit                  = bytecodeLimit;
    }

}
package battlecode.common;

/**
 * Contains details on various attributes of the different robots. All of this information is in the specs in a more organized form.
 */

PLACEHOLDER_ACTION_RADIUS = 100;
PLACEHOLDER_VISION_RADIUS = 100;
PLACEHOLDER_BYTECODE_LIMIT = 7500;
NO_COOLDOWN_CLOCK = 10000000;

public enum RobotType {

    // Build Cost Lead, Build Cost Gold, Action Cooldown, Move Cooldown
    // DPS Lv1, HP Lv1, Action Radius (squared), Vision Radius (squared), Reclaim Cost Percentage, Bytecode Limit

    /**
     * Archons are portable buildings that heal and generate robots.
     * Losing all archons means losing the game.
     *
     * @battlecode.doc.robottype
     */
    ARCHON          (  0, 250, 10, NO_COOLDOWN_CLOCK, -5, 700, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0.2, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL, BCG, AC                 MC DPS   HP                         AR                          VR  RCP                         BL
    /**
     * A lead robot
     * Can mine gold or lead at their or an adjacent location.
     *
     * @battlecode.doc.robottype
     */
    MINER           ( 50,   0, 2,       10,   0,  40, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS,  0, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL, BCG, AC       MC  DPS   HP                         AR                         VR RCP                           BL
    /**
     * A lead robot
     * Can build, repair, and upgrade non-Archon buildings.
     *
     * @battlecode.doc.robottype
     */
    BUILDER         ( 40,  0,  10,       10, -10, 30, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL,BCG,  AC        MC  DPS  HP                         AR                        VR RCP                        BL
    /**
     * Alchemist's laboratory
     * Converts lead into gold
     *
     * @battlecode.doc.robottype
     */
    LABORATORY      (800,   0, 10, NO_COOLDOWN_CLOCK, 0, 500, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0.2, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL, BCG, AC                  MC DPS HP                         AR                        VR   RCP                           BL

    /**
     * Lead attacking robot, low-range, medium-damage.
    */
    GUARD           ( 75,   0,  10,       20, 35, 120, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS,   0, PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL, BCG,  AC        MC DPS   HP                         AR                          VR RCP                         BL
    
    /**
     * Gold robot, medium-range, high-damage.
     */
    WIZARD          ( 0,   50,  20,       20, 75, 70, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS,  0,  PLACEHOLDER_BYTECODE_LIMIT),
    //               BCL, BCG,  AC        MC DPS   HP                        AR                          VR RCP                          BL

    /**
    * Guard turret 
    */
    // TODO These are placeholder values only
    GUARD_TURRET   ( 50,    0,  20,       20, 75, 70, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0.2,  PLACEHOLDER_BYTECODE_LIMIT),
    //               BCA, BCG,  AC        MC DPS   HP                        AR                          VR RCP                           BL

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
        );
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

    // COST RELATED FUNCTIONS

    /**
    * @param level to upgrade to
    * @return cost to upgrade (lead if level -> 2, gold if level -> 3)
    */
    public int getUpgradeCost(int level) {
        Map<RobotType, int> toLevel2 = new HashMap<RobotType, int>();
        toLevel2.put(RobotType.ARCHON, 2500);
        toLevel2.put(RobotType.LABORATORY, 400);
        toLevel2.put(RobotType.GUARD_TURRET, 25);

        Map<RobotType, int> toLevel3 = new HashMap<RobotType, int>();
        toLevel3.put(RobotType.ARCHON, 5000);
        toLevel3.put(RobotType.LABORATORY, 800);
        toLevel3.put(RobotType.GUARD_TURRET, 50);

        if (level == 1) return 0;
        if (level == 2) return toLevel2.get(this);
        // otherwise, level 3
        return toLevel3.get(this);
    }

    /**
     * @param level to upgrade to
     * @return lead component of cost to upgrade.
     */
    public int getLeadUpgradeCost(int level) {
        if (level == 2) return getUpgradeCost(level);
        return 0;
    }


    /**
     * @param level to upgrade to
     * @return gold component of cost to upgrade.
     */
    public int getGoldUpgradeCost(int level) {
        if (level == 3) return getUpgradeCost(level);
        return 0;
    }

    /**
     * @param level, current
     * @return lead component of worth
     */
    public int getLeadWorth(int level) {
        int total = this.buildCostLead;
        if(level >= 2) total += getLeadUpgradeCost(level);
        return total;
    }

    /**
     * @param level, current
     * @return gold component of worth
     */
    public int getGoldWorth(int level) {
        int total = this.buildCostGold;
        if(level == 3) total += getGoldUpgradeCost(level);
        return total;
    }

    /**
     * @param level, current 
     */
    public int getGoldDropped(int level) {
        int total = getLeadWorth() * reclaimCostPercentage;
    }

    /**
     * @param level, current
     */
    public int getLeadDropped(int level) {
        int total = getGoldWorth() * reclaimCostPercentage;
    }

    RobotType(int buildCostLead, int buildCostGold, int actionCooldown, int moveCooldown,
        int DPSLv1, int HPLv1, int actionRadiusSquared, int visionRadiusSquared, float reclaimCostPercentage,
        int ricochetCount, int bytecodeLimit) {

        this.buildCostLead                  = buildCostLead;
        this.buildCostGold                  = buildCostGold;
        this.actionCooldown                 = actionCooldown;
        this.moveCooldown                   = moveCooldown;
        this.DPSLv1                         = DPSLv1;
        this.HPLv1                          = HPLv1;
        this.actionRadiusSquared            = actionRadiusSquared;
        this.visionRadiusSquared            = visionRadiusSquared;
        this.reclaimCostPercentage          = reclaimCostPercentage;
        this.bytecodeLimit                  = bytecodeLimit;
    }

}
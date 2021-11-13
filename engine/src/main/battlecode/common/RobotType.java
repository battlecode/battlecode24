package battlecode.common;

/**
* Contains the two currency types for supporting robot build costs.
*/
public enum CurrencyType {
    AU, PB
}

/**
* Contains the Building levels.
*/
public enum BuildingLevel {
    PROTOTYPE, TURRET, PORTABLE
}

/**
 * Contains details on various attributes of the different robots. All of this information is in the specs in a more organized form.
 */

PLACEHOLDER_ACTION_RADIUS = 100;
PLACEHOLDER_VISION_RADIUS = 100;
PLACEHOLDER_BYTECODE_LIMIT = 7500;
NO_COOLDOWN_CLOCK = 10000000;

public enum RobotType {

    // Build Cost Amount, Build Cost Type (one of "Pb" or "Au"), Action Cooldown, Move Cooldown
    // DPS Lv1, HP Lv1, Action Radius (squared), Vision Radius (squared), Reclaim Cost Percentage, Bytecode Limit, IsBuilding, RicochetCount

    /**
     * Archons are portable buildings that heal and generate robots.
     * Losing all archons means losing the game.
     *
     * @battlecode.doc.robottype
     */
    ARCHON          (250, CurrencyType.AU, 10, NO_COOLDOWN_CLOCK, -5, 700, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0.2, PLACEHOLDER_BYTECODE_LIMIT, true),
    //               BCA,             BCT, AC                 MC DPS   HP                         AR                          VR  RCP                      BL       IB
    /**
     * A lead robot
     * Can mine gold or lead at their or an adjacent location.
     *
     * @battlecode.doc.robottype
     */
    MINER           ( 50, CurrencyType.PB, 2,       10,   0,  40, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0, PLACEHOLDER_BYTECODE_LIMIT, false, 0),
    //               BCA,             BCT, AC       MC  DPS   HP                         AR                          VR      RCP                   BL      IB RC
    /**
     * A lead robot
     * Can build, repair, and upgrade non-Archon buildings.
     *
     * @battlecode.doc.robottype
     */
    BUILDER         ( 40, CurrencyType.PB,  10,       10, -10, 30, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0, PLACEHOLDER_BYTECODE_LIMIT, false, 0),
    //               BCA,             BCT,  AC        MC  DPS  HP                         AR                          VR RCP                         BL     IB RC
    /**
     * Alchemist's laboratory
     * Converts lead into gold
     *
     * @battlecode.doc.robottype
     */
    LABORATORY      (800,  CurrencyType.PB, 10, NO_COOLDOWN_CLOCK, 0, 500, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0.2, PLACEHOLDER_BYTECODE_LIMIT, true, 0),
    //               BCA,              BCT, AC                  MC DPS HP                         AR                        VR   RCP                      BL        IB, RC

    /**
     * Lead attacking robot, low-range, medium-damage.
    */
    GUARD           ( 75, CurrencyType.PB,  10,       20, 35, 120, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0, PLACEHOLDER_BYTECODE_LIMIT, false, 0),
    //               BCA,             BCT,  AC        MC DPS   HP                         AR                          VR RCP                         BL     IB RC
    
    /**
     * Lead attacking robot, high-range, low-damage.
    */
    ARCHER          ( 75, CurrencyType.PB,  10,       15, 20,  80, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0, PLACEHOLDER_BYTECODE_LIMIT, false, 0),
    //               BCA,             BCT,  AC        MC  DPS  HP                         AR                          VR RCP                        BL      IB RC

    /**
     * Gold robot, medium-range, high-damage.
     */
    WIZARD          ( 50, CurrencyType.AU,  20,       20, 75, 70, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0, PLACEHOLDER_BYTECODE_LIMIT, false, 0),
    //               BCA,             BCT,  AC        MC DPS   HP                        AR                          VR RCP                        BL      IB RC

    /**
    * Guard turret 
    */
    // NEED TO CHANGE: These are placeholder values only
    GUARD_TURRET   ( 50, CurrencyType.PB,  20,       20, 75, 70, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0.2, PLACEHOLDER_BYTECODE_LIMIT, true, 0),
    //               BCA,             BCT,  AC        MC DPS   HP                        AR                          VR   RCP                       BL    IB  RC

    /**
    * Archer turret
    */
    // NEED TO CHANGE: These are placeholder values only
    ARCHER_TURRET   ( 50, CurrencyType.PB,  20,       20, 75, 70, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0.2, PLACEHOLDER_BYTECODE_LIMIT, true, 0),
    //               BCA,             BCT,  AC        MC DPS   HP                        AR                          VR   RCP                      BL      IB  RC

    /**
    * Wizard turret
    * Has upgradeable ricochet count.
    */
    // NEED TO CHANGE: These are placeholder values only 
    WIZARD_TURRET   ( 50, CurrencyType.PB,  20,       20, 75, 70, PLACEHOLDER_ACTION_RADIUS, PLACEHOLDER_VISION_RADIUS, 0.2, PLACEHOLDER_BYTECODE_LIMIT, true, 2),
    //               BCA,             BCT,  AC        MC DPS   HP                        AR                          VR   RCP                      BL      IB  RC
    ;

    /**
     * Cost to build a given robot or building.
     */
    public final float buildCostAmount;

    /**
     * Type of currency for build cost amount.
     */
    public final float buildCostType;

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
     * How much of the build cost can be reclaimed.
     */
    public final double reclaimCostPercentage;

    /**
     * Base bytecode limit of this robot.
     */
    public final int bytecodeLimit;

    /**
     * Whether this is a Building.
     */
    public final boolean isBuilding;

    /**
     * Ricochet count.
     */
    public final int ricochetCount;

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
    public int getMaxHealth(int level){

        int health = type.HPLv1;
        if(level >= 2)
            health *= GameConstants.HP1_TO_2;
        if(level == 3)
            health *= GameConstants.HP_2_TO_3;
        
        // 11/13/21: https://www.w3schools.com/java/java_type_casting.asp
        return (int)health;
        // end code

    }

    /**
     * Returns the starting HP for a robot by level.
     * @param level of the robot
     * @return the starting HP for a robot by level.
     */
    public int getStartingHealth(int level){
        // 11/13/21: https://www.w3schools.com/java/java_type_casting.asp
        return (int) (type.HPLv1 * GameConstants.PROTOTYPE_STARTING_HEALTH_MULTIPLIER);
        // end code
    }

    /**
     * @return the squared action radius.
     */
    public int getActionRadiusSquared(int level){
        return this.actionRadiusSquared;
    }

    /**
     * @return the squared vision radius.
     */
    public int getVisionRadiusSquared(int level){
        return this.visionRadiusSquared;
    }

    /**
     * @param level
     * @return the DPS for a robot by level.
     */
    public int getDamage(int level){

        int dps = type.DPSLv1;
        if(level >= 2)
            dps *= GameConstants.DPS1_TO_2;
        if(level == 3)
            dps *= GameConstants.DPS_2_TO_3;

        // 11/13/21: https://www.w3schools.com/java/java_type_casting.asp
        return (int) dps;
        // end code
    }

    /**
     * @param level
     * @return the ricochet count by level, controlled by a multiplier and rounded down.
     */
    public int getRicochetCount(int level){
        int RICOCHET_UPGRADE_MULTIPLIER = 1.5;
        // 11/13/21: https://www.w3schools.com/java/java_type_casting.asp
        return (int) (this.ricochetCount * Math.pow(GameConstants.RICOCHET_UPGRADE_MULTIPLIER, level));
        // end code
    }

    /**
     * @return whether or not a given robot is a building.
    */
    public boolean isBuilding(){
        return this.isBuilding;
    }

    /**
     * @param level of destroyed building.
     * @return the amount of lead dropped, based on a multiplier by level.
     */
    public int getLeadDropped(){
        if(this.buildCostType != CurrencyType.PB) return 0;
        return this.getCurrencyDropped();
    }

    /**
     * @param level of destroyed building.
     * @return the amount of gold dropped, based on a multiplier by level.
     */
    public int getGoldDropped(){
        if(this.buildCostType != CurrencyType.AU) return 0;
        return this.getCurrencyDropped();
    }

    /**
     * Calculates the total cost of a building based on level/upgrade multiplier.
     * @param level of destroyed building
     * @return amount of currency dropped by building for the building's build currency type.
        Will be modified by getGoldDropped or getLeadDropped to access the right currency.
     */
    private int getCurrencyDropped(int level){
        // 11/13/21: https://www.w3schools.com/java/java_type_casting.asp
        return (int) (this.buildCostAmount * Math.pow(BUILDING_UPGRADE_COST_MULTIPLIER, level));
        // end code
    }

    RobotType(int buildCostAmount, CurrencyType buildCostType,
        int actionCooldown, int moveCooldown,
        int DPSLv1, int HPLv1,
        int actionRadiusSquared, int visionRadiusSquared,
        float reclaimCostPercentage, int bytecodeLimit, boolean isBuilding,
        int ricochetCount) {

        this.buildCostAmount                = buildCostAmount;
        this.buildCostType                  = buildCostType;
        this.actionCooldown                 = actionCooldown;
        this.moveCooldown                   = moveCooldown;
        this.DPSLv1                         = DPSLv1;
        this.HPLv1                          = HPLv1;
        this.actionRadiusSquared            = actionRadiusSquared;
        this.visionRadiusSquared            = visionRadiusSquared;
        this.reclaimCostPercentage          = reclaimCostPercentage;
        this.bytecodeLimit                  = bytecodeLimit;
        this.isBuilding                     = isBuilding;
        this.ricochetCount                  = ricochetCount;
    }

}

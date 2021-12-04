package battlecode.common;

/**
 * Contains details on various attributes of the different robots. All of this information is in the specs in a more organized form.
 */


public enum RobotType {

    // Build Cost Lead, Build Cost Gold, Action Cooldown, Move Cooldown
    // HP Lv1, DPS Lv1, Action Radius (squared), Vision Radius (squared), Bytecode Limit

    /**
     * Archons are portable buildings that heal and generate robots.
     * Losing all archons means losing the game.
     *
     * @battlecode.doc.robot     */
    ARCHON          (  0, 250, 10, 24, 1000, -2, 20, 34, 20000),
    //               BCL  BCG  AC  MC    HP DPS  AR  VR      BL

    /**
     * Alchemist's laboratory
     * Converts lead into gold
     *
     * @battlecode.doc.robot     */
    LABORATORY      (800,   0, 10, 24, 130,  5, 20, 34, 10000),
    //               BCL  BCG  AC  MC  HP  DPS  AR  VR      BL

    /**
    * Guard turret 
    */
    WATCHTOWER      (180,  0,  10,  24, 130,  5, 20, 34,   10000),
    //               BCL  BCG  AC  MC  HP  DPS  AR  VR       BL

    /**
     * Can mine gold or lead at their or an adjacent location.
     *
     * @battlecode.doc.robot     */
    MINER           ( 50,   0, 2,  20, 40,   0, 2, 20,   7500),
    //               BCL  BCG AC  MC  HP  DPS AR  VR       BL
    /**
     * Can build and repair buildings.
     *
     * @battlecode.doc.robot     */
    BUILDER         ( 40,   0,  10, 20, 30, -1,  5, 20,   7500),
    //               BCL  BCG   AC  MC  HP  DPS  AR  VR      BL
    
    /**
     * Ranged attacking robot.
    */
    SOLDIER         ( 75,   0, 10, 16,  3,  50, 13, 20,  10000),
   //                BCL   BCG AC  MC  HP  DPS  AR  VR       BL
    
    /**
     * Gold robot, causes Anomalies.
     */
    SAGE            ( 0,  50,200, 25, 45, 100, 13, 20,   10000)
    //              BCL  BCG  AC  MC  HP  DPS  AR  VR        BL

    ;

    /**
     * Lead cost to build a given robot or building 
     */
    public final int buildCostLead;

    /**
     * Gold cost to build a given robot or building (except for Archon's)
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
    * Initial health per robot for Level 1.
    */
    public final int HPLv1;

    /**
    * Damage per second for each robot in Level 1.
    */
    public final int DPSLv1;

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
            || this == WATCHTOWER
        );
    }

    /**
     * Returns the max HP for a robot by level.
     * @param level of the robot
     * @return the max HP for a robot by level.
     */
    public int getMaxHealth(int level) {
        if(this.isBuilding() || level == 1) return this.HPLv1;
        if(this == RobotType.ARCHON){
            if(level == 2) return 1100;
            if(level == 3) return 1200;
        }
        if(this == RobotType.LABORATORY){
            if(level == 2) return 110;
            if(level == 3) return 120;
        }
        if(this == RobotType.WATCHTOWER){
            if(level == 2) return 143;
            if(level == 3) return 156;
        }
    }

    /**
     * Returns the starting HP for a robot by level.
     * @param level of the robot
     * @return the starting HP for a robot by level.
     */
    public int getStartingHealth(int level) {
        return (int) (this.HPLv1 * GameConstants.PROTOTYPE_HP_PERCENTAGE);
    }

    /**
     * @param level
     * @return the DPS for a robot by level.
     */
    public int getDamage(int level) {

        if(this.isBuilding() || level == 1 || this == RobotType.LABORATORY)
            return this.HPLv1;
        
        if(this == RobotType.ARCHON){
            if(level == 2) return 3;
            if(level == 3) return 4;
        }
        if(this == RobotType.LABORATORY){
            return 0;
        }
        if(this == RobotType.WATCHTOWER){
            if(level == 2) return 6;
            if(level == 3) return 7;
        }
        
    }

    /**
     * @param level
     * @return the healing per turn for a robot by level.
     */
    public int getHealing(int level){
        if(this != RobotType.ARCHON || this != RobotType.BUILDER) return 0;
        return (int)(-1 * this.DPSLv1);
    }

    // COST RELATED FUNCTIONS

    /**
     * @param level to upgrade to
     * @return lead component of cost to upgrade.
     */
    public int getLeadUpgradeCost(int level) {
        if (level == 2) return 600;
        return 0;
    }

    /**
     * @param level to upgrade to
     * @return gold component of cost to upgrade.
     */
    public int getGoldUpgradeCost(int level) {
        if (level == 3) return 100;
        return 0;
    }

    /**
     * @param level, current
     * @return lead component of worth
     */
    public int getLeadWorth(int level) {
        return this.buildCostLead + this.getLeadUpgradeCost(level);
    }

    /**
     * @param level, current
     * @return gold component of worth
     */
    public int getGoldWorth(int level) {
        return this.buildCostGold + this.getGoldUpgradeCost(level);
    }

    /**
     * @return Reclaim cost percentage for when robot is destroyed.
     */
    public float getReclaimCostPercentage(){
        return (this.isBuilding()) ? 0.2f : 0;
    }

    /**
     * @param level, current 
     */
    public int getGoldDropped(int level) {
        int total = (int) (this.getGoldWorth(level) * this.getReclaimCostPercentage());
    }

    /**
     * @param level, current
     */
    public int getLeadDropped(int level) {
        int total = (int) (this.getLeadWorth(level) * this.getReclaimCostPercentage());
    }

    RobotType(int buildCostLead, int buildCostGold, int actionCooldown, int moveCooldown,
        int HPLv1, int DPSLv1, int actionRadiusSquared, int visionRadiusSquared, int bytecodeLimit) {

        this.buildCostLead                  = buildCostLead;
        this.buildCostGold                  = buildCostGold;
        this.actionCooldown                 = actionCooldown;
        this.moveCooldown                   = moveCooldown;
        this.HPLv1                          = HPLv1;
        this.DPSLv1                         = DPSLv1;
        
        this.actionRadiusSquared            = actionRadiusSquared;
        this.visionRadiusSquared            = visionRadiusSquared;

        this.bytecodeLimit                  = bytecodeLimit;

    }

}
package battlecode.common;

/**
 * Contains details on various attributes of the different robots. All of this information is in the specs in a more organized form.
 */
public enum RobotType {

    // Build Cost Lead, Build Cost Gold, Action Cooldown, Movement Cooldown
    // Health (level 1), Damage (level 1), Action Radius (squared), Vision Radius (squared), Bytecode Limit

    /**
     * Archons are portable buildings that heal and generate robots.
     * Losing all archons means losing the game.
     *
     * @battlecode.doc.robot     */
    ARCHON          (  0, 250, 10, 24, 1000, -2, 20, 34, 20000),
    //               BCL  BCG  AC  MC    HP DMG  AR  VR      BL

    /**
     * Alchemist's laboratory
     * Converts lead into gold
     *
     * @battlecode.doc.robot     */
    LABORATORY      (800,   0, 10, 24, 100,  0,  0, 53,    5000),
    //               BCL  BCG  AC  MC   HP DMG  AR  VR      BL

    /**
    * Guard turret 
    */
    WATCHTOWER      (180,  0,  10,  24, 130,  5, 20, 34,   10000),
    //               BCL  BCG  AC   MC   HP DMG  AR  VR       BL

    /**
     * Can mine gold or lead at their or an adjacent location.
     *
     * @battlecode.doc.robot     */
    MINER           ( 50,   0,  2,  20, 40,   0, 2, 20,   7500),
    //               BCL  BCG  AC   MC  HP  DMG AR  VR       BL
    /**
     * Can build and repair buildings.
     *
     * @battlecode.doc.robot     */
    BUILDER         ( 40,   0,  10, 20, 30, -1,  5, 20,   7500),
    //               BCL  BCG   AC  MC  HP DMG  AR  VR      BL
    
    /**
     * Ranged attacking robot.
    */
    SOLDIER         ( 75,   0,  10, 16,  50,  3, 13, 20,  10000),
    //               BCL   BCG  AC  MC  HP  DMG  AR  VR       BL
    
    /**
     * Gold robot, causes Anomalies.
     */
    SAGE            ( 0,  50, 200, 25, 100, 45, 13, 20,   10000)
    //              BCL  BCG   AC  MC  HP  DMG  AR  VR        BL
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
     * Movement cooldown.
     */
    public final int movementCooldown;

    /**
    * Initial health per robot for Level 1.
    */
    public final int health;

    /**
    * Damage for each robot in Level 1.
    */
    public final int damage;

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
     * Base bytecode limit of this robot
     */
    public final int bytecodeLimit;

    /**
     * @return the squared action radius
     */
    public int getActionRadiusSquared(int level) {
        return this.actionRadiusSquared;
    }

    /**
     * @return the squared vision radius
     */
    public int getVisionRadiusSquared(int level) {
        return this.visionRadiusSquared;
    }

    /**
     * @param builtType type of robot being built
     * @return whether this type can build the given robot type
     */
    public boolean canBuild(RobotType builtType) {
        return (this == ARCHON && (builtType == MINER || 
                                   builtType == BUILDER || 
                                   builtType == SOLDIER || 
                                   builtType == SAGE)) || 
               (this == BUILDER && (builtType == LABORATORY || 
                                    builtType == WATCHTOWER));
    }

    /**
     * @return whether this type can attack
     */
    public boolean canAttack() {
        return (this == WATCHTOWER
            || this == SOLDIER);
    }

    /**
     * @return whether this type can envision anomalies
     */
    public boolean canEnvision() {
        return this == SAGE;
    }

    /**
     * @param repairedType type of robot being repaired
     * @return whether this type can repair the given robot type
     */
    public boolean canRepair(RobotType repairedType) {
        return ((this == ARCHON && !repairedType.isBuilding()) || 
                (this == BUILDER && repairedType.isBuilding()));
    }

    /**
     * @return whether this type can mine
     */
    public boolean canMine() {
        return this == MINER;
    }

    /**
     * @param mutatedType type of robot being mutated
     * @return whether this type can mutate buildings
     */
    public boolean canMutate(RobotType mutatedType) {
        return this == BUILDER && mutatedType.isBuilding();
    }

    /**
     * @return whether this type can transmute lead into gold
     */
    public boolean canTransmute() {
        return this == LABORATORY;
    }

    /**
     * @return whether or not a given robot is a building
    */
    public boolean isBuilding() {
        return (
            this == ARCHON
            || this == LABORATORY
            || this == WATCHTOWER
        );
    }

    /**
     * Returns the max health of a robot by level.
     * @param level of the robot
     * @return the max health of a robot by level
     */
    public int getMaxHealth(int level) {
        if (!this.isBuilding() || level == 1) {
            return this.health;
        } else if (this == ARCHON) {
            return level == 2 ? 1100 : 1200;
        } else if (this == LABORATORY) {
            return level == 2 ? 110 : 120;
        } else {
            return level == 2 ? 143 : 156;
        }
    }

    /**
     * Returns the damage of a robot by level.
     * @param level
     * @return the damage for a robot by level, negative if robot heals
     */
    public int getDamage(int level) {
        if (!this.isBuilding() || level == 1) {
            return this.damage;
        } else if (this == RobotType.ARCHON) {
            return level == 2 ? 3 : 4;
        } else if (this == RobotType.LABORATORY) {
            return 0;
        } else {
            return level == 2 ? 6 : 7;
        }
    }

    /**
     * @param level
     * @return the healing per turn for a robot by level as a positive amount,
     *  0 if robot doesn't heal
     */
    public int getHealing(int level) {
        if (this == ARCHON || this == BUILDER) {
            return (int) (-1 * this.getDamage(level));
        } else {
            return 0;
        }
    }

    // COST RELATED FUNCTIONS

    /**
     * @param level the level to mutate to
     * @return lead component of cost to mutate
     */
    public int getLeadMutateCost(int level) {
        return level == 2 ? 600 : 0;
    }

    /**
     * @param level the level to mutate to
     * @return gold component of cost to mutate.
     */
    public int getGoldMutateCost(int level) {
        return level == 3 ? 100 : 0;
    }

    /**
     * @param level the robot's current level
     * @return lead component of worth
     */
    public int getLeadWorth(int level) {
        int leadWorth = this.buildCostLead;
        for (int i = 2; i <= level; i++) {
            leadWorth += this.getLeadMutateCost(i);
        }
        return leadWorth;
    }

    /**
     * @param level the robot's current level
     * @return gold component of worth
     */
    public int getGoldWorth(int level) {
        int goldWorth = this.buildCostGold;
        for (int i = 2; i <= level; i++) {
            goldWorth += this.getGoldMutateCost(i);
        }
        return goldWorth;
    }

    /**
     * @param level the robot's current level
     * @return the amount of lead dropped
     */
    public int getLeadDropped(int level) {
        return (int) (this.getLeadWorth(level) * GameConstants.RECLAIM_COST_MULTIPLIER);
    }

    /**
     * @param level the robot's current level
     * @return the amount of gold dropped
     */
    public int getGoldDropped(int level) {
        return (int) (this.getGoldWorth(level) * GameConstants.RECLAIM_COST_MULTIPLIER);
    }

    RobotType(int buildCostLead, int buildCostGold, int actionCooldown, int movementCooldown,
        int health, int damage, int actionRadiusSquared, int visionRadiusSquared, int bytecodeLimit) {
        this.buildCostLead                  = buildCostLead;
        this.buildCostGold                  = buildCostGold;
        this.actionCooldown                 = actionCooldown;
        this.movementCooldown               = movementCooldown;
        this.health                         = health;
        this.damage                         = damage;
        this.actionRadiusSquared            = actionRadiusSquared;
        this.visionRadiusSquared            = visionRadiusSquared;
        this.bytecodeLimit                  = bytecodeLimit;
    }
}
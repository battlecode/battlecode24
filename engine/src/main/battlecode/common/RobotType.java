package battlecode.common;

/**
 * Enumerates the possible types of robots. More information about the
 * capabilities of each robot type are available in the game specs.
 *
 * You can check the type of another robot by inspecting {@link RobotInfo#type},
 * or your own type by inspecting {@link RobotController#getType}.
 */
public enum RobotType {

    // Build Cost Lead, Build Cost Gold, Action Cooldown, Movement Cooldown
    // Health (level 1), Damage (level 1), Action Radius (squared), Vision Radius (squared), Bytecode Limit

    /**
     * Archons are the headquarters of your army. They are Buildings that can
     * build and repair Droids.
     *
     * @battlecode.doc.robottype
     */
    ARCHON          (  0, 100, 10, 24, 600, -2, 20, 34, 20000),
    //               BCL  BCG  AC  MC    HP DMG  AR  VR      BL

    /**
     * Laboratories house the magic of the alchemist. They are Buildings that
     * can transmute lead into gold.
     *
     * @battlecode.doc.robottype
     */
    LABORATORY      (180,   0, 10, 24, 100,  0,  0, 53,    5000),
    //               BCL  BCG  AC  MC   HP DMG  AR  VR      BL

    /**
     * Watchtowers are defensive strongholds. They are Buildings that can
     * attack robots that stray too close.
     *
     * @battlecode.doc.robottype
     */
    WATCHTOWER      (150,  0,  10,  24, 150,  4, 20, 34,   10000),
    //               BCL  BCG  AC   MC   HP DMG  AR  VR       BL

    /**
     * Miners are resource-collecting robots. They are Droids that can mine lead
     * or gold at their or an adjacent location.
     *
     * @battlecode.doc.robottype
     */
    MINER           ( 50,   0,  2,  20, 40,   0, 2, 20,  10000),
    //               BCL  BCG  AC   MC  HP  DMG AR  VR       BL

    /**
     * Builders are resource-spending robots. They are Droids that can build,
     * repair and mutate Buildings.
     *
     * @battlecode.doc.robottype
     */
    BUILDER         ( 40,   0,  10, 20, 30, -2,  5, 20,   7500),
    //               BCL  BCG   AC  MC  HP DMG  AR  VR      BL

    /**
     * Soldiers are general-purpose attacking robots. They are Droids that can
     * attack robots within a range.
     *
     * @battlecode.doc.robottype
     */
    SOLDIER         ( 75,   0,  10, 16,  50,  3, 13, 20,  10000),
    //               BCL   BCG  AC  MC  HP  DMG  AR  VR       BL

    /**
     * Sages are wise robots with extraordinary abilities. They are Droids
     * fashioned out of pure gold, able to envision Anomalies to the detriment
     * of the enemy.
     *
     * @battlecode.doc.robottype
     */
    SAGE            ( 0,  20, 200, 25, 100, 45, 25, 34,   10000)
    //              BCL  BCG   AC  MC  HP  DMG  AR  VR        BL
    ;

    /**
     * Lead cost to build a given robot or building.
     */
    public final int buildCostLead;

    /**
     * Gold cost to build a given robot or building.
     */
    public final int buildCostGold;

    /**
     * The action cooldown applied to the robot per action.
    */
    public final int actionCooldown;

    /**
     * The movement cooldown applied to the robot per move.
     */
    public final int movementCooldown;

    /**
     * The maximum health for a Level 1 robot of this type.
     *
     * @see #getMaxHealth
     */
    public final int health;

    /**
     * The damage per attack for a Level 1 robot of this type. This value is
     * negative for robots with repairing abilities.
     *
     * @see #getDamage
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
     * Base bytecode limit of this robot.
     */
    public final int bytecodeLimit;

    /**
     * Returns whether this type can build a robot of another given type.
     *
     * @param builtType type of robot being built
     * @return whether this type can build the given robot type
     *
     * @battlecode.doc.costlymethod
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
     * Returns whether this type can attack.
     *
     * @return whether this type can attack
     * @battlecode.doc.costlymethod
     */
    public boolean canAttack() {
        return (this == WATCHTOWER
            || this == SOLDIER
            || this == SAGE);
    }

    /**
     * Returns whether this type can envision Anomalies.
     *
     * @return whether this type can envision Anomalies
     * @battlecode.doc.costlymethod
     */
    public boolean canEnvision() {
        return this == SAGE;
    }

    /**
     * Returns whether this type can repair another robot of a given type.
     *
     * @param repairedType type of robot being repaired
     * @return whether this type can repair the given robot type
     *
     * @battlecode.doc.costlymethod
     */
    public boolean canRepair(RobotType repairedType) {
        return ((this == ARCHON && !repairedType.isBuilding()) || 
                (this == BUILDER && repairedType.isBuilding()));
    }

    /**
     * Returns whether this type can mine for metal.
     *
     * @return whether this type can mine for metal
     * @battlecode.doc.costlymethod
     */
    public boolean canMine() {
        return this == MINER;
    }

    /**
     * Returns whether this type can mutate another robot of a given type.
     *
     * @param mutatedType type of robot being mutated
     * @return whether this type can mutate another robot of a given type
     *
     * @battlecode.doc.costlymethod
     */
    public boolean canMutate(RobotType mutatedType) {
        return this == BUILDER && mutatedType.isBuilding();
    }

    /**
     * Returns whether this type can transmute lead into gold.
     *
     * @return whether this type can transmute lead into gold
     * @battlecode.doc.costlymethod
     */
    public boolean canTransmute() {
        return this == LABORATORY;
    }

    /**
     * Returns whether or not a given robot is a Building.
     *
     * @return whether or not a given robot is a Building
     * @battlecode.doc.costlymethod
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
     *
     * @param level of the robot
     * @return the max health of a robot by level
     *
     * @battlecode.doc.costlymethod
     */
    public int getMaxHealth(int level) {
        if (!this.isBuilding() || level == 1) {
            return this.health;
        } else if (this == ARCHON) {
            return level == 2 ? 1080 : 1944;
        } else if (this == LABORATORY) {
            return level == 2 ? 180 : 324;
        } else {
            return level == 2 ? 270 : 486;
        }
    }

    /**
     * Determine the damage power of a robot by level.
     *
     * @param level The specific level of the robot.
     * @return the damage for a robot by level, negative if robot repairs
     *
     * @battlecode.doc.costlymethod
     */
    public int getDamage(int level) {
        if (!this.isBuilding() || level == 1) {
            return this.damage;
        } else if (this == RobotType.ARCHON) {
            return level == 2 ? -4 : -6;
        } else if (this == RobotType.LABORATORY) {
            return 0;
        } else {
            return level == 2 ? 8 : 12;
        }
    }

    /**
     * Determine the repairing power of a robot by level.
     *
     * @param level The specific level of the robot.
     * @return the repair per turn for a robot by level as a positive amount,
     *         or 0 if robot doesn't repair
     *
     * @battlecode.doc.costlymethod
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
     * Determine the lead cost of Mutating this robot to a specific level.
     *
     * @param level the level to mutate to
     * @return lead cost to mutate, unspecified if mutation is invalid
     *
     * @battlecode.doc.costlymethod
     */
    public int getLeadMutateCost(int level) {
        if (level != 2) {
            return 0;
        }
        switch (this) {
            case ARCHON:     return 300;
            case WATCHTOWER: return 150;
            case LABORATORY: return 150;
            default:         return 0;
        }
    }

    /**
     * Determine the gold cost of Mutating this robot to a specific level.
     *
     * @param level the level to mutate to
     * @return gold cost to mutate, unspecified if mutation is invalid
     *
     * @battlecode.doc.costlymethod
     */
    public int getGoldMutateCost(int level) {
        if (level != 3) {
            return 0;
        }
        switch (this) {
            case ARCHON:     return 80;
            case WATCHTOWER: return 60;
            case LABORATORY: return 25;
            default:         return 0;
        }
    }

    /**
     * Determine the robot's net worth in lead.
     *
     * @param level the robot's current level
     * @return lead component of worth
     *
     * @battlecode.doc.costlymethod
     */
    public int getLeadWorth(int level) {
        int leadWorth = this.buildCostLead;
        for (int i = 2; i <= level; i++) {
            leadWorth += this.getLeadMutateCost(i);
        }
        return leadWorth;
    }

    /**
     * Determine the robot's net worth in gold.
     *
     * @param level the robot's current level
     * @return gold component of worth
     *
     * @battlecode.doc.costlymethod
     */
    public int getGoldWorth(int level) {
        int goldWorth = this.buildCostGold;
        for (int i = 2; i <= level; i++) {
            goldWorth += this.getGoldMutateCost(i);
        }
        return goldWorth;
    }

    /**
     * Determine the amount of lead dropped if this robot dies.
     *
     * @param level the robot's current level
     * @return the amount of lead dropped
     *
     * @battlecode.doc.costlymethod
     */
    public int getLeadDropped(int level) {
        return (int) (this.getLeadWorth(level) * GameConstants.RECLAIM_COST_MULTIPLIER);
    }

    /**
     * Determine the amount of gold dropped if this robot dies.
     *
     * @param level the robot's current level
     * @return the amount of gold dropped
     *
     * @battlecode.doc.costlymethod
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

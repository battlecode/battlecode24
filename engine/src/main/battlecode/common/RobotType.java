package battlecode.common;

/**
 * Enumerates the possible types of robots. More information about the
 * capabilities of each robot type are available in the game specs.
 *
 * You can check the type of another robot by inspecting {@link RobotInfo#type},
 * or your own type by inspecting {@link RobotController#getType}.
 */
public enum RobotType {

    // Build Cost Adamantium, Build Cost Mana, Build Cost Elixir, Action Cooldown, Movement Cooldown
    // Health, Damage, Action Radius (squared), Vision Radius (squared), Bytecode Limit

    /**
     * Headquarters are the headquarters of your army. These stationary  
     * locations can spawn robots, build anchors, and store materials.
     *
     * @battlecode.doc.robottype
     */
    HEADQUARTERS    ( 0,   0,   0,   10,   -1, 1,   0,  9, 34, 20000),
    //               BCA  BCM  BCE   AC   MC  HP  DMG  AR  VR     BL

    /**
     * Carriers are material-moving robots. They can extract, carry, and
     * transport resources and anchors. They can also attack other robots
     * in a range and specified direction by throwing these resources.
     * 
     * @battlecode.doc.robottype
     */
    CARRIER         ( 50,   0,   0,   10,   0, 15,   0,  9, 20, 10000),
    //               BCA  BCM  BCE   AC   MC  HP  DMG  AR  VR     BL

    /**
     * Launchers are general-purpose attacking robots. They can attack robots
     * within a range and specified direction.
     *
     * @battlecode.doc.robottype
     */
    LAUNCHER        (  0,  60,   0,  10,  20, 20,   6, 16, 20, 10000),
    //               BCA  BCM  BCE   AC   MC  HP  DMG  AR  VR     BL

    /**
     * Destablizers are offensive temporal manipulators. They decelerate time,
     * increasing cooldowns and dealing damage to enemies within a certain range.
     *
     * @battlecode.doc.robottype
     */
    DESTABILIZER    (  0,   0, 200,  70,  25, 30,   5, 13, 20, 10000),
    //               BCA  BCM  BCE   AC   MC  HP  DMG  AR  VR     BL

    /**
     * Booster are supportive temporal manipulators. They accelerate time,
     * decreasing cooldowns for ally robots within a certain range.
     * 
     * @battlecode.doc.robottype
     */
    BOOSTER         (  0,   0, 150, 140,  25, 40,   0, -1, 20, 10000),
    //               BCA  BCM  BCE   AC   MC  HP  DMG  AR  VR     BL
    /**
     * Amplifiers are the key to communication for your army. They enable 
     * robots within a range to write messages to their faction’s shared array.
     *
     * @battlecode.doc.robottype
     */
    AMPLIFIER       ( 40,  40,   0,   -1,  15, 40,   0, -1, 34,  7500)
    //               BCA  BCM  BCE   AC   MC  HP  DMG  AR  VR     BL
    ;

    /**
     * Adamantium cost to build a given robot or building.
     */
    public final int buildCostAdamantium;

    /**
     * Mana cost to build a given robot or building.
     */
    public final int buildCostMana;

    /**
     * Elixir cost to build a given robot or building.
     */
    public final int buildCostElixir;

    /**
     * The action cooldown applied to the robot per action.
    */
    public final int actionCooldown;

    /**
     * The movement cooldown applied to the robot per move.
     */
    public final int movementCooldown;

    /**
     * The maximum health for a robot of this type.
     *
     * @see #getMaxHealth
     */
    public final int health;

    /**
     * The damage per attack for a robot of this type.
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
     * Returns whether this type can attack.
     *
     * @return whether this type can attack
     * @battlecode.doc.costlymethod
     */
    public boolean canAttack() {
        return (this == CARRIER
            || this == LAUNCHER);
    }

    /**
     * Returns whether this type can extract resources
     *
     * @return whether this type can extract resources
     * @battlecode.doc.costlymethod
     */
    public boolean canExtract() {
        return this == CARRIER;
    }

    /**
     * Returns whether this type can place reality anchors
     *
     * @return whether this type can place reality anchors
     * @battlecode.doc.costlymethod
     */
    public boolean canPlaceAnchor() {
        return this == CARRIER;
    }

    /**
     * Returns whether this type can boost nearby allies
     *
     * @return whether this type can buff nearby allies
     * @battlecode.doc.costlymethod
     */
    public boolean canBoost() {
        return this == BOOSTER;
    }

    /**
     * Returns whether this type can destablize nearby enemies
     * 
     * @return whether this type can buff nearby allies
     * @battlecode.doc.costlymethod
     */
    public boolean canDestabilize() {
        return this == DESTABILIZER;
    }

    /**
     * Returns the max health of a robot
     *
     * @return the max health of a robot
     *
     * @battlecode.doc.costlymethod
     */
    public int getMaxHealth() {
        return this.health;
    }

    public int getBuildCost(ResourceType rType) {
        switch (rType) {
            case ADAMANTIUM:
                return this.buildCostAdamantium;
            case MANA:
                return this.buildCostMana;
            case ELIXIR:
                return this.buildCostElixir;
            default:
                return 0;
        }
    }

    RobotType(int buildCostAdamantium, int buildCostMana, int buildCostElixir, int actionCooldown, int movementCooldown,
        int health, int damage, int actionRadiusSquared, int visionRadiusSquared, int bytecodeLimit) {
        this.buildCostAdamantium            = buildCostAdamantium;
        this.buildCostMana                  = buildCostMana;
        this.buildCostElixir                = buildCostElixir;
        this.actionCooldown                 = actionCooldown;
        this.movementCooldown               = movementCooldown;
        this.health                         = health;
        this.damage                         = damage;
        this.actionRadiusSquared            = actionRadiusSquared;
        this.visionRadiusSquared            = visionRadiusSquared;
        this.bytecodeLimit                  = bytecodeLimit;
    }
}
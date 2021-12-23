package battlecode.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import battlecode.common.*;
import battlecode.schema.Action;

/**
 * The representation of a robot used by the server.
 * Comparable ordering:
 *  - tiebreak by creation time (priority to later creation)
 *  - tiebreak by robot ID (priority to lower ID)
 */
public strictfp class InternalRobot implements Comparable<InternalRobot> {

    private final RobotControllerImpl controller;
    private final GameWorld gameWorld;

    private final int ID;
    private Team team;
    private RobotType type;
    private MapLocation location;
    private int level;
    private RobotMode mode;
    private int health;

    private long controlBits;
    private int currentBytecodeLimit;
    private int bytecodesUsed;

    private int roundsAlive;
    private int actionCooldownTurns;
    private int movementCooldownTurns;
    private int numVisibleFriendlyRobots;

    /**
     * Used to avoid recreating the same RobotInfo object over and over.
     */
    private RobotInfo cachedRobotInfo;

    /**
     * Create a new internal representation of a robot
     *
     * @param gw the world the robot exists in
     * @param type the type of the robot
     * @param loc the location of the robot
     * @param team the team of the robot
     */
    @SuppressWarnings("unchecked")
    public InternalRobot(GameWorld gw, int id, RobotType type, MapLocation loc, Team team) {
        this.ID = id;
        this.team = team;
        this.type = type;
        this.location = loc;
        this.level = 1;
        this.mode = this.type.isBuilding() ? RobotMode.PROTOTYPE : RobotMode.DROID;
        this.health = (int) ((this.mode == RobotMode.PROTOTYPE ? GameConstants.PROTOTYPE_STARTING_HEALTH_MULTIPLIER : 1) * this.type.getMaxHealth(this.level));

        this.controlBits = 0;
        this.currentBytecodeLimit = type.bytecodeLimit;
        this.bytecodesUsed = 0;

        this.roundsAlive = 0;
        this.actionCooldownTurns = 0;
        this.addActionCooldownTurns(GameConstants.COOLDOWNS_PER_TURN);
        this.movementCooldownTurns = 0;
        this.addMovementCooldownTurns(GameConstants.COOLDOWNS_PER_TURN);
        this.numVisibleFriendlyRobots = 0;

        this.gameWorld = gw;
        this.controller = new RobotControllerImpl(gameWorld, this);
    }

    // ******************************************
    // ****** GETTER METHODS ********************
    // ******************************************

    public RobotControllerImpl getController() {
        return controller;
    }

    public GameWorld getGameWorld() {
        return gameWorld;
    }

    public int getID() {
        return ID;
    }

    public Team getTeam() {
        return team;
    }

    public RobotType getType() {
        return type;
    }

    public MapLocation getLocation() {
        return location;
    }

    public int getLevel() {
        return level;
    }

    public RobotMode getMode() {
        return mode;
    }

    public int getHealth() {
        return health;
    }

    public long getControlBits() {
        return controlBits;
    }

    public int getBytecodesUsed() {
        return bytecodesUsed;
    }

    public int getRoundsAlive() {
        return roundsAlive;
    }

    public int getActionCooldownTurns() {
        return actionCooldownTurns;
    }

    public int getMovementCooldownTurns() {
        return movementCooldownTurns;
    }

    public int getTransformCooldownTurns() {
        if (this.mode == RobotMode.TURRET)
            return this.actionCooldownTurns;
        if (this.mode == RobotMode.PORTABLE)
            return this.movementCooldownTurns;
        return -1;
    }

    public RobotInfo getRobotInfo() {
        if (cachedRobotInfo != null
                && cachedRobotInfo.ID == ID
                && cachedRobotInfo.team == team
                && cachedRobotInfo.type == type
                && cachedRobotInfo.level == level
                && cachedRobotInfo.health == health
                && cachedRobotInfo.location.equals(location)) {
            return cachedRobotInfo;
        }

        this.cachedRobotInfo = new RobotInfo(ID, team, type, level, health, location);
        return this.cachedRobotInfo;
    }

    // **********************************
    // ****** CHECK METHODS *************
    // **********************************

    /**
     * Returns whether the robot can perform actions, based on mode and cooldowns.
     */
    public boolean canActCooldown() {
        return this.mode.canAct && this.actionCooldownTurns < GameConstants.COOLDOWN_LIMIT;
    }

    /**
     * Returns whether the robot can move, based on mode and cooldowns.
     */
    public boolean canMoveCooldown() {
        return this.mode.canMove && this.movementCooldownTurns < GameConstants.COOLDOWN_LIMIT;
    }

    /**
     * Returns whether the robot can transform, based on mode and cooldowns.
     */
    public boolean canTransformCooldown() {
        if (this.mode == RobotMode.TURRET)
            return this.actionCooldownTurns < GameConstants.COOLDOWN_LIMIT;
        if (this.mode == RobotMode.PORTABLE)
            return this.movementCooldownTurns < GameConstants.COOLDOWN_LIMIT;
        return false;
    }

    /**
     * Returns the robot's action radius squared.
     */
    public int getActionRadiusSquared() {
        return this.type.actionRadiusSquared;
    }

    /**
     * Returns whether this robot can perform actions on the given location.
     * 
     * @param toAct the MapLocation to act
     */
    public boolean canActLocation(MapLocation toAct) {
        return this.location.distanceSquaredTo(toAct) <= getActionRadiusSquared();
    }

    /**
     * Returns whether this robot can act at a given radius away.
     * 
     * @param radiusSquared the distance squared to act
     */
    public boolean canActRadiusSquared(int radiusSquared) {
        return radiusSquared <= getActionRadiusSquared();
    }

    /**
     * Returns the robot's vision radius squared.
     */
    public int getVisionRadiusSquared() {
        return this.type.visionRadiusSquared;
    }

    /**
     * Returns whether this robot can see the given location.
     * 
     * @param toSee the MapLocation to see
     */
    public boolean canSeeLocation(MapLocation toSee) {
        return this.location.distanceSquaredTo(toSee) <= getVisionRadiusSquared();
    }

    /**
     * Returns whether this robot can see a given radius away.
     * 
     * @param radiusSquared the distance squared to act
     */
    public boolean canSeeRadiusSquared(int radiusSquared) {
        return radiusSquared <= getVisionRadiusSquared();
    }

    // ******************************************
    // ****** UPDATE METHODS ********************
    // ******************************************

    /**
     * Sets the location of the robot.
     * 
     * @param loc the new location of the robot
     */
    public void setLocation(MapLocation loc) {
        this.gameWorld.getObjectInfo().moveRobot(this, loc);
        this.location = loc;
    }

    /**
     * Resets the action cooldown.
     */
    public void addActionCooldownTurns(int numActionCooldownToAdd) {
        int newActionCooldownTurns = this.gameWorld.getCooldownWithMultiplier(numActionCooldownToAdd, this.location);
        setActionCooldownTurns(this.actionCooldownTurns + newActionCooldownTurns);
    }

    /**
     * Resets the movement cooldown.
     */
    public void addMovementCooldownTurns(int numMovementCooldownToAdd) {
        int newMovementCooldownTurns = this.gameWorld.getCooldownWithMultiplier(numMovementCooldownToAdd, this.location);
        setMovementCooldownTurns(this.movementCooldownTurns + newMovementCooldownTurns);
    }

    /**
     * Sets the action cooldown given the number of turns.
     * 
     * @param newActionTurns the number of action cooldown turns
     */
    public void setActionCooldownTurns(int newActionTurns) {
        this.actionCooldownTurns = newActionTurns;
    }

    /**
     * Sets the movement cooldown given the number of turns.
     * 
     * @param newMovementTurns the number of movement cooldown turns
     */
    public void setMovementCooldownTurns(int newMovementTurns) {
        this.movementCooldownTurns = newMovementTurns;
    }

    /**
     * Adds health to a robot. Input can be negative to subtract health.
     * 
     * @param healthAmount the amount to change health by (can be negative)
     */
    public void addHealth(int healthAmount) {
        int oldHealth = this.health;
        this.health += healthAmount;
        int maxHealth = this.type.getMaxHealth(this.level);
        if (this.health > maxHealth) {
            this.health = maxHealth;
            if (this.mode == RobotMode.PROTOTYPE)
                this.mode = RobotMode.TURRET;
        }
        if (this.health <= 0) {
            int leadDrop = this.type.getLeadDropped(this.level);
            int goldDrop = this.type.getGoldDropped(this.level);
            // TODO: drop resources at this location (interact with GameWorld)
            this.gameWorld.destroyRobot(this.ID);
        } else if (this.health != oldHealth) {
            // TODO: double check this
            this.gameWorld.getMatchMaker().addAction(getID(), Action.CHANGE_HEALTH, this.health - oldHealth);
        }
    }

    // *********************************
    // ****** ACTION METHODS *********
    // *********************************

    /**
     * Transform from turret to portable mode, or vice versa.
     */
    public void transform() {
        if (this.canTransformCooldown()) {
            if (this.mode == RobotMode.TURRET) {
                this.mode = RobotMode.PORTABLE;
            } else {
                this.mode = RobotMode.TURRET;
            }
        }
    }

    /**
     * Upgrade a building.
     */
    public void upgrade() {
        if (this.mode == RobotMode.DROID || this.mode == RobotMode.PROTOTYPE)
            return;
        if (this.level == GameConstants.MAX_LEVEL)
            return;
        this.level++;
        this.health += this.type.getMaxHealth(this.level) - this.type.getMaxHealth(this.level - 1);
    }

    /**
     * Attacks another robot. Assumes bot is in range.
     * 
     * @param bot the robot to be attacked
     */
    public void attack(InternalRobot bot) {
        int dmg = this.type.getDamage(this.level);
        bot.addHealth(-dmg);
    }

    /**
     * Heals another robot. Assumes bot is in range.
     * 
     * @param bot the robot to be healed
     */
    public void heal(InternalRobot bot) {
        int healingAmount = this.type.getHealing(this.level);
        bot.addHealth(healingAmount);
    }

    // *********************************
    // ****** GAMEPLAY METHODS *********
    // *********************************

    // should be called at the beginning of every round
    public void processBeginningOfRound() {
        // anything
    }

    public void processBeginningOfTurn() {
        this.actionCooldownTurns = Math.max(0, this.actionCooldownTurns - GameConstants.COOLDOWNS_PER_TURN);
        this.movementCooldownTurns = Math.max(0, this.movementCooldownTurns - GameConstants.COOLDOWNS_PER_TURN);
        this.currentBytecodeLimit = getType().bytecodeLimit;
    }

    public void processEndOfTurn() {
        // bytecode stuff!
        this.gameWorld.getMatchMaker().addBytecodes(this.ID, this.bytecodesUsed);
        this.roundsAlive++;
    }

    public void processEndOfRound() {
        // anything
    }

    // *********************************
    // ****** BYTECODE METHODS *********
    // *********************************

    // TODO
    public boolean canExecuteCode() {
        return true;
    }

    public void setBytecodesUsed(int numBytecodes) {
        this.bytecodesUsed = numBytecodes;
    }

    public int getBytecodeLimit() {
        return canExecuteCode() ? this.currentBytecodeLimit : 0;
    }

    // *********************************
    // ****** VARIOUS METHODS **********
    // *********************************

    public void die_exception() {
        this.gameWorld.getMatchMaker().addAction(getID(), Action.DIE_EXCEPTION, -1);
        this.gameWorld.destroyRobot(getID());
    }

    // *****************************************
    // ****** MISC. METHODS ********************
    // *****************************************

    /**
     * @return the number of friendly robots within sensor (vision) radius.
     */
    public int updateNumVisibleFriendlyRobots() {
        return this.numVisibleFriendlyRobots = this.controller.seeNearbyRobots(-1, getTeam()).length;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && (o instanceof InternalRobot)
                && ((InternalRobot) o).getID() == ID;
    }

    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public String toString() {
        return String.format("%s:%s#%d", getTeam(), getType(), getID());
    }

    @Override
    public int compareTo(InternalRobot o) {
        if (this.roundsAlive != o.roundsAlive)
            return this.roundsAlive - o.roundsAlive;
        return this.ID - o.ID;
    }
}

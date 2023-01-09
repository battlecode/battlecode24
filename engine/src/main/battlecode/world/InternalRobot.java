package battlecode.world;

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
    protected final GameWorld gameWorld;

    private final int ID;
    private Team team;
    private RobotType type;
    private MapLocation location;
    protected Inventory inventory;
    private int health;

    private long controlBits;
    private int currentBytecodeLimit;
    private int bytecodesUsed;

    private int roundsAlive;
    private int actionCooldownTurns;
    private int movementCooldownTurns;

    /**
     * Used to avoid recreating the same RobotInfo object over and over.
     */
    private RobotInfo cachedRobotInfo;

    private String indicatorString;

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
        this.gameWorld = gw;

        this.ID = id;
        this.team = team;
        this.type = type;
        this.location = loc;
        switch (this.type) {
            case HEADQUARTERS:
                this.inventory = new Inventory();
                // Add resources at start of game
                break;
            case CARRIER:
                this.inventory = new Inventory(GameConstants.CARRIER_CAPACITY);
                break;
            default:
                this.inventory = new Inventory(0);
                break;
        }
        this.health = this.type.health;

        this.controlBits = 0;
        this.currentBytecodeLimit = type.bytecodeLimit;
        this.bytecodesUsed = 0;

        this.roundsAlive = 0;
        this.actionCooldownTurns = GameConstants.COOLDOWN_LIMIT;
        this.movementCooldownTurns = GameConstants.COOLDOWN_LIMIT;

        this.indicatorString = "";

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

    public int getHealth() {
        return health;
    }

    public int getResource(ResourceType r) {
        return this.inventory.getResource(r);
    }

    public boolean canAdd(int amount) {
        assert(this.getType() == RobotType.CARRIER);
        return this.inventory.canAdd(amount);
    }
    
    private void addResourceChangeAction(ResourceType rType, int amount) {
        switch (rType) {
            case ADAMANTIUM:
                this.gameWorld.getMatchMaker().addAction(getID(), Action.CHANGE_ADAMANTIUM, amount);
                break;
            case MANA:
                this.gameWorld.getMatchMaker().addAction(getID(), Action.CHANGE_MANA, amount);
                break;
            case ELIXIR:
                this.gameWorld.getMatchMaker().addAction(getID(), Action.CHANGE_ELIXIR, amount);
                break;
            case NO_RESOURCE:
                if (amount != 0) 
                    throw new IllegalArgumentException("No resource should have value of 0 but has value of " + amount);
                break;
        }
    }

    public void addResourceAmount(ResourceType rType, int amount) {
        this.inventory.addResource(rType, amount);
        addResourceChangeAction(rType, amount);
    }

    public int getNumAnchors(Anchor anchor) {
        return this.inventory.getNumAnchors(anchor);
    }

    public boolean holdingAnchor() {
        return this.inventory.getTotalAnchors() > 0;
    }

    public Anchor getTypeAnchor() {
        assert(this.type == RobotType.CARRIER); // Don't ever call this with a headquarter
        if (getNumAnchors(Anchor.STANDARD) > 0) {
            return Anchor.STANDARD;
        } else if (getNumAnchors(Anchor.ACCELERATING) > 0) {
            return Anchor.ACCELERATING;
        } else {
            return null;
        }
    }

    public boolean canAddAnchor() {
        return this.inventory.canAdd(GameConstants.ANCHOR_WEIGHT);
    }

    public void addAnchor(Anchor anchor) {
        this.inventory.addAnchor(anchor);
    }

    public void releaseAnchor(Anchor anchor) {
        this.inventory.releaseAnchor(anchor);
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

    public RobotInfo getRobotInfo() {
        if (cachedRobotInfo != null
                && cachedRobotInfo.ID == ID
                && cachedRobotInfo.team == team
                && cachedRobotInfo.type == type
                && cachedRobotInfo.inventory.equals(inventory.copy())
                && cachedRobotInfo.health == health
                && cachedRobotInfo.location.equals(location)) {
            return cachedRobotInfo;
        }

        this.cachedRobotInfo = new RobotInfo(ID, team, type, inventory.copy(), health, location);
        return this.cachedRobotInfo;
    }

    // **********************************
    // ****** CHECK METHODS *************
    // **********************************

    /**
     * Returns whether the robot can perform actions, based on cooldowns.
     */
    public boolean canActCooldown() {
        return this.actionCooldownTurns < GameConstants.COOLDOWN_LIMIT;
    }

    /**
     * Returns whether the robot can move, based on cooldowns.
     */
    public boolean canMoveCooldown() {
        return this.movementCooldownTurns < GameConstants.COOLDOWN_LIMIT;
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
     * Returns whether this robot can sense the given location.
     * 
     * @param toSense the MapLocation to sense
     */
    public boolean canSenseLocation(MapLocation toSense) {
        int visionRadiusSquared = getVisionRadiusSquared();
        if (this.gameWorld.getCloud(toSense) || this.gameWorld.getCloud(this.getLocation())) {
            visionRadiusSquared = GameConstants.CLOUD_VISION_RADIUS_SQUARED;
        }
        return this.location.distanceSquaredTo(toSense) <= visionRadiusSquared;
    }

    /**
     * Returns whether this robot can sense a given radius away.
     * 
     * @param radiusSquared the distance squared to sense
     */
    public boolean canSenseRadiusSquared(int radiusSquared) {
        return radiusSquared <= getVisionRadiusSquared();
    }

    // ******************************************
    // ****** UPDATE METHODS ********************
    // ******************************************

    /**
     * Sets the indicator string of the robot.
     *
     * @param string the new indicator string of the robot
     */
    public void setIndicatorString(String string) {
        this.indicatorString = string;
    }

    /**
     * Sets the location of the robot.
     * 
     * @param loc the new location of the robot
     */
    public void setLocation(MapLocation loc) {
        this.gameWorld.moveRobot(getLocation(), loc);
        this.gameWorld.getObjectInfo().moveRobot(this, loc);
        this.location = loc;
    }

    /**
     * Sets the location of the robot (only for internal use through currents)
     * 
     * @param loc the new location of the robot
     */
    public void setLocationForCurrents(MapLocation loc) {
        this.gameWorld.addRobot(loc, this);
        this.gameWorld.getObjectInfo().addRobotIndex(this, loc);
        this.location = loc;
    }


    /**
     * Resets the action cooldown.
     */
    public void addActionCooldownTurns(int numActionCooldownToAdd) {
        int newActionCooldownTurns = this.gameWorld.getCooldownWithMultiplier(numActionCooldownToAdd, this.location, this.team);
        setActionCooldownTurns(this.actionCooldownTurns + newActionCooldownTurns);
    }

    private int getBaseMovementCooldown() {
        if (this.getType() == RobotType.CARRIER) {
            int cooldownAmount = (int)(Math.floor((GameConstants.CARRIER_MOVEMENT_SLOPE*this.inventory.getWeight()))) + GameConstants.CARRIER_MOVEMENT_INTERCEPT;
            return cooldownAmount;
        } else {
            return this.getType().movementCooldown;
        }
    }
    
    /**
     * Resets the movement cooldown.
     */
    public void addMovementCooldownTurns() {
        int newMovementCooldownTurns = this.gameWorld.getCooldownWithMultiplier(getBaseMovementCooldown(), this.location, this.team);
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
        assert(healthAmount < 0); // No healing!
        if (this.getType() == RobotType.HEADQUARTERS) {
            return; // Can't damage headquarters
        }
        int oldHealth = this.health;
        this.health += healthAmount;
        if (this.health <= 0) {
            this.gameWorld.destroyRobot(this.ID);
        } else if (this.health != oldHealth) {
            this.gameWorld.getMatchMaker().addAction(getID(), Action.CHANGE_HEALTH, this.health - oldHealth);
        }
    }

    // *********************************
    // ****** ACTION METHODS *********
    // *********************************
    
    private int getDamage() {
        assert(this.type == RobotType.LAUNCHER);
        return this.type.damage;
    }

    /**
     * Attacks another robot (launcher). Assumes bot is in range.
     * 
     * @param loc the location of the bot
     */
    public void attack(MapLocation loc) {
        InternalRobot bot = this.gameWorld.getRobot(loc);
        if (!(bot == null)) {
            int dmg = getDamage();
            bot.addHealth(-dmg);
            this.gameWorld.getMatchMaker().addAction(getID(), Action.LAUNCH_ATTACK, bot.getID());
        }
    }

    // *********************************
    // ****** GAMEPLAY METHODS *********
    // *********************************

    // should be called at the beginning of every round
    public void processBeginningOfRound() {
        this.indicatorString = "";
    }

    public void processBeginningOfTurn() {
        this.actionCooldownTurns = Math.max(0, this.actionCooldownTurns - GameConstants.COOLDOWNS_PER_TURN);
        this.movementCooldownTurns = Math.max(0, this.movementCooldownTurns - GameConstants.COOLDOWNS_PER_TURN);
        this.currentBytecodeLimit = getType().bytecodeLimit;
    }

    public void processEndOfTurn() {
        // bytecode stuff!
        this.gameWorld.getMatchMaker().addBytecodes(this.ID, this.bytecodesUsed);
        // indicator strings!
        this.gameWorld.getMatchMaker().addIndicatorString(this.ID, this.indicatorString);
        this.roundsAlive++;
    }

    public void processEndOfRound(int roundNum) {
        // anything
        if (roundNum % GameConstants.PASSIVE_INCREASE_ROUNDS == 0 && this.getType() == RobotType.HEADQUARTERS) {
            // Add resources to team
            this.gameWorld.getTeamInfo().addAdamantium(this.getTeam(), GameConstants.PASSIVE_AD_INCREASE);
            this.addResourceAmount(ResourceType.ADAMANTIUM, GameConstants.PASSIVE_AD_INCREASE);

            this.gameWorld.getTeamInfo().addMana(this.getTeam(), GameConstants.PASSIVE_MN_INCREASE);
            this.addResourceAmount(ResourceType.MANA, GameConstants.PASSIVE_MN_INCREASE);
        }
    }

    // *********************************
    // ****** BYTECODE METHODS *********
    // *********************************

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

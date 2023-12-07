package battlecode.world;

import battlecode.common.*;
import battlecode.schema.Action;
import java.util.Objects;

/**
 * The representation of a robot used by the server.
 * Comparable ordering:
 *  - tiebreak by creation time (priority to later creation)
 *  - tiebreak by robot ID (priority to lower ID)
 */
public strictfp class InternalRobot implements Comparable<InternalRobot> {

    private final RobotControllerImpl controller;
    protected final GameWorld gameWorld;

    private int buildExp;
    private int healExp;
    private int attackExp;

    private final int ID;
    private Team team;
    private MapLocation location;
    private int health;
    private boolean spawned;

    private long controlBits;
    private int currentBytecodeLimit;
    private int bytecodesUsed;

    private int roundsAlive;
    private int actionCooldownTurns;
    private int movementCooldownTurns;
    private int spawnCooldownTurns;

    private Flag flag;

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
    public InternalRobot(GameWorld gw, int id, Team team) {
        this.gameWorld = gw;

        this.ID = id;
        this.team = team;

        this.location = null;
        this.health = GameConstants.DEFAULT_HEALTH;
        this.spawned = false;

        this.buildExp = 0;
        this.healExp = 0;
        this.attackExp = 0;

        this.controlBits = 0;
        this.currentBytecodeLimit = GameConstants.BYTECODE_LIMIT;
        this.bytecodesUsed = 0;

        this.roundsAlive = 0;
        this.actionCooldownTurns = GameConstants.COOLDOWN_LIMIT;
        this.movementCooldownTurns = GameConstants.COOLDOWN_LIMIT;
        this.spawnCooldownTurns = 0;

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

    public MapLocation getLocation() {
        return location;
    }

    public int getHealth() {
        return health;
    }

    public int getExp(SkillType skill){
        if(skill == SkillType.BUILD)
            return buildExp;
        if(skill == SkillType.HEAL)
            return healExp;
        if(skill == SkillType.ATTACK)
            return attackExp;
        return 0;
    }

    public int getLevel(SkillType skill){
        int exp = this.getExp(skill);
        for(int i = 0; i < 5; i++){
            if (exp < skill.getExperience(i+1)){
                return i;
            }
        }
        return 6;
    }

    public int getResource() {
        return this.gameWorld.getTeamInfo().getBread(this.team);
    }

    public void addResourceAmount(int amount) {
        this.gameWorld.getTeamInfo().addBread(this.team, amount);
    }

    public boolean canAddFlag() {
        return flag == null;
    }

    public void addFlag(Flag flag) {
        this.flag = flag;
        flag.pickUp(this);
    }

    public boolean hasFlag() {
        return flag != null;
    }

    public Flag getFlag() {
        return flag;
    }

    public void removeFlag() {
        this.flag = null;
        flag.drop();
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
                && cachedRobotInfo.health == health
                && cachedRobotInfo.location.equals(location)) {
            return cachedRobotInfo;
        }

        this.cachedRobotInfo = new RobotInfo(ID, team, health, location);
        return this.cachedRobotInfo;
    }

    // **********************************
    // ****** CHECK METHODS *************
    // **********************************

    /**
     * Returns whether the robot can spawn, based on cooldowns.
     */
    public boolean canSpawnCooldown() {
        return this.spawnCooldownTurns < GameConstants.COOLDOWN_LIMIT;
    }

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
        return GameConstants.ACTION_RADIUS_SQUARED;
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
        return GameConstants.VISION_RADIUS_SQUARED;
    }

    /**
     * Returns whether this robot can sense the given location.
     * 
     * @param toSense the MapLocation to sense
     */
    public boolean canSenseLocation(MapLocation toSense) {
        return this.location.distanceSquaredTo(toSense) <= getVisionRadiusSquared();
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
        flag.setLoc(loc);
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
        setActionCooldownTurns(this.actionCooldownTurns + numActionCooldownToAdd);
    }
    
    /**
     * Resets the movement cooldown.
     */
    public void addMovementCooldownTurns() {
        setMovementCooldownTurns(this.movementCooldownTurns + this.gameWorld.getMovementCooldown(team));
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
        this.health += healthAmount;
        this.health = Math.min(this.health, GameConstants.DEFAULT_HEALTH);
        if (this.health <= 0) {
            this.gameWorld.despawnRobot(this.ID);
        }
    }

    /**
     * Removes exp from a robot when it is jailed
     */
    public void jailedPenalty(){
        if(this.buildExp == 0 && this.attackExp == 0 && this.healExp == 0) return;
        if(this.buildExp > this.attackExp && this.buildExp > this.healExp) this.buildExp -= SkillType.BUILD.getPenalty(this.getLevel(SkillType.BUILD));
        else if (this.attackExp > this.healExp && this.attackExp > this.healExp) this.attackExp -= SkillType.ATTACK.getPenalty(this.getLevel(SkillType.ATTACK));
        else this.healExp -= SkillType.HEAL.getPenalty(this.getLevel(SkillType.HEAL));
    }

    /**
     * increment exp for a robot
     */
    public void incrementSkill(SkillType skill){
        if(skill == SkillType.BUILD)
            this.buildExp ++;
        if(skill == SkillType.HEAL)
            this.healExp ++;
        if(skill == SkillType.ATTACK)
            this.attackExp ++;
    }

    // *********************************
    // ****** ACTION METHODS *********
    // *********************************

    /**
     * Spawns the robot at the location provided
     * 
     * @param loc the new location of the robot
     */
    public void spawn(MapLocation loc) {
        this.spawned = true;
        this.location = loc;
    }

    public void despawn() {
        this.spawned = false;
        this.location = null;
        this.spawnCooldownTurns = GameConstants.COOLDOWNS_PER_TURN * GameConstants.JAILED_ROUNDS;
        jailedPenalty();
    }

    public boolean isSpawned() {
        return this.spawned;
    }
    
    private int getDamage() {
        return SkillType.ATTACK.skillEffect * SkillType.ATTACK.getSkillEffect(this.getLevel(SkillType.ATTACK));
    }

    private int locationToInt(MapLocation loc) {
        return this.gameWorld.locationToIndex(loc);
    }

    /**
     * Attacks another location (launcher).
     * 
     * @param loc the location of the bot
     */
    public void attack(MapLocation loc) {
        InternalRobot bot = this.gameWorld.getRobot(loc);
        if (bot == null || bot.getTeam() == this.getTeam()) {
            // If robot is null or of your team, no damage; otherwise do damage
            this.getGameWorld().getMatchMaker().addAction(getID(), Action.ATTACK, -locationToInt(loc) - 1);
        } else {
            int dmg = getDamage();
            bot.addHealth(-dmg);
            if(this.getLevel(SkillType.BUILD) < 4 && this.getLevel(SkillType.HEAL) < 4){
                this.attackExp += 1;
            }
            this.gameWorld.getMatchMaker().addAction(getID(), Action.ATTACK, bot.getID());
        }
    }

    public int getHeal() {
        int base_heal = SkillType.HEAL.skillEffect;
        //check for upgrade
        if (this.gameWorld.getTeamInfo().getGlobalUpgrades(team)[2]){
            base_heal += GlobalUpgrade.HEALING.baseHealChange;
        }
        return base_heal * SkillType.HEAL.getSkillEffect(this.getLevel(SkillType.HEAL)); 
    }

    public int getBuildExp() {
        return this.buildExp;
    }

    public int getHealExp() {
        return this.healExp;
    }

    public int getAttackExp() {
        return this.attackExp;
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
        this.spawnCooldownTurns = Math.max(0, this.spawnCooldownTurns - GameConstants.COOLDOWNS_PER_TURN);
        this.currentBytecodeLimit = GameConstants.BYTECODE_LIMIT;
    }

    public void processEndOfTurn() {
        // bytecode stuff!
        this.gameWorld.getMatchMaker().addBytecodes(this.ID, this.bytecodesUsed);
        // indicator strings!
        this.gameWorld.getMatchMaker().addIndicatorString(this.ID, this.indicatorString);
        this.roundsAlive++;
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
        this.gameWorld.despawnRobot(getID());
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
        return String.format("%s#%d", getTeam(), getID());
    }

    @Override
    public int compareTo(InternalRobot o) {
        if (this.roundsAlive != o.roundsAlive)
            return this.roundsAlive - o.roundsAlive;
        return this.ID - o.ID;
    }
}

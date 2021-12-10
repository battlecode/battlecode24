package battlecode.world;

import battlecode.common.*;
import static battlecode.common.GameActionExceptionType.*;
import battlecode.instrumenter.RobotDeathException;
import battlecode.schema.Action;

import java.util.*;


/**
 * The actual implementation of RobotController. Its methods *must* be called
 * from a player thread.
 *
 * It is theoretically possible to have multiple for a single InternalRobot, but
 * that may cause problems in practice, and anyway why would you want to?
 *
 * All overriden methods should assertNotNull() all of their (Object) arguments,
 * if those objects are not explicitly stated to be nullable.
 */
public final strictfp class RobotControllerImpl implements RobotController {

    /**
     * The world the robot controlled by this controller inhabits.
     */
    private final GameWorld gameWorld;

    /**
     * The robot this controller controls.
     */
    private final InternalRobot robot;

    /**
     * An rng based on the world seed.
     */
    private static Random random;

    /**
     * Create a new RobotControllerImpl
     *
     * @param gameWorld the relevant world
     * @param robot the relevant robot
     */
    public RobotControllerImpl(GameWorld gameWorld, InternalRobot robot) {
        this.gameWorld = gameWorld;
        this.robot = robot;

        this.random = new Random(gameWorld.getMapSeed());
    }

    // *********************************
    // ******** INTERNAL METHODS *******
    // *********************************

    /**
     * Throw a null pointer exception if an object is null.
     *
     * @param o the object to test
     */
    private static void assertNotNull(Object o) {
        if (o == null) {
            throw new NullPointerException("Argument has an invalid null value");
        }
    }

    @Override
    public int hashCode() {
        return robot.getID();
    }

    // *********************************
    // ****** GLOBAL QUERY METHODS *****
    // *********************************

    @Override
    public int getRoundNum() {
        return gameWorld.getCurrentRound();
    }

    @Override
    public int getRobotCount() {
        return gameWorld.getObjectInfo().getRobotCount(getTeam());
    }

    @Override
    public int getArchonCount() {
        // TODO: Assumes getArchons() exists in TeamInfo
        return gameWorld.getTeamInfo().getArchons();
    }

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************

    @Override
    public int getID() {
        return this.robot.getID();
    }

    @Override
    public Team getTeam() {
        return this.robot.getTeam();
    }

    @Override
    public RobotType getType() {
        return this.robot.getType();
    }

    @Override
    public MapLocation getLocation() {
        return this.robot.getLocation();
    }
 
    @Override
    public int getHealth() {
        return this.robot.getHeatlh();
    }

    @Override
    public int getUpgradeLevel() {
        return this.robot.getUpgradeLevel();  
    }

    private InternalRobot getRobotByID(int id) {
        if (!gameWorld.getObjectInfo().existsRobot(id))
            return null;
        return this.gameWorld.getObjectInfo().getRobotByID(id);
    }

    /**
     * Returns a fully copied version of the anomaly schedule.
     */
    public AnomalyScheduleEntry[] getAnomalySchedule(){
        this.gameWorld.getAnomalySchedule();
    }


    // ***********************************
    // ****** GENERAL VISION METHODS *****
    // ***********************************

    @Override
    public boolean onTheMap(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        if (!this.robot.canSeeLocation(loc))
            throw new GameActionException(CANT_SEE_THAT,
                    "Target location not within vision range");
        return gameWorld.getGameMap().onTheMap(loc);
    }

    private void assertCanSeeLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        if (!this.robot.canSeeLocation(loc))
            throw new GameActionException(CANT_SEE_THAT,
                    "Target location not within vision range");
        if (!gameWorld.getGameMap().onTheMap(loc))
            throw new GameActionException(CANT_SEE_THAT,
                    "Target location is not on the map");
    }

    @Override
    public boolean canSeeLocation(MapLocation loc) {
        try {
            assertCanSeeLocation(loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public boolean canSeeRadiusSquared(int radiusSquared) {
        return this.robot.canSeeRadiusSquared(radiusSquared);
    }

    @Override
    public boolean canSeeRobotAtLocation(MapLocation loc) throws GameActionException {
        assertCanSeeLocation(loc);
        return this.gameWorld.getRobot(loc) != null;
    }

    @Override
    public RobotInfo seeRobotAtLocation(MapLocation loc) throws GameActionException {
        assertCanSeeLocation(loc);
        InternalRobot bot = gameWorld.getRobot(loc);
        if (bot != null)
            return bot.getRobotInfo(getType().canTrueSense());
        return null;
    }

    @Override
    public boolean canSeeRobot(int id) {
        InternalRobot seenRobot = getRobotByID(id);
        return seenRobot == null ? false : canSeeLocation(seenRobot.getLocation());
    }

    @Override
    public RobotInfo seeRobot(int id) throws GameActionException {
        if (!canSeeRobot(id))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Can't see given robot; It may not exist anymore");
        return getRobotByID(id).getRobotInfo(getType().canTrueSense());
    }

    @Override
    public RobotInfo[] seeNearbyRobots() {
        return seeNearbyRobots(-1);
    }

    @Override
    public RobotInfo[] seeNearbyRobots(int radiusSquared) {
        return seeNearbyRobots(radiusSquared, null);
    }

    @Override
    public RobotInfo[] seeNearbyRobots(int radiusSquared, Team team) {
        return seeNearbyRobots(getLocation(), radiusSquared, team);
    }

    @Override
    public RobotInfo[] seeNearbyRobots(MapLocation center, int radiusSquared, Team team) {
        assertNotNull(center);
        int actualRadiusSquared = radiusSquared == -1 ? getType().sensorRadiusSquared : Math.min(radiusSquared, getType().sensorRadiusSquared);
        InternalRobot[] allSeenRobots = gameWorld.getAllRobotsWithinRadiusSquared(center, actualRadiusSquared);
        List<RobotInfo> validSeenRobots = new ArrayList<>();
        for (InternalRobot seenRobot : allSeenRobots) {
            // check if this robot
            if (seenRobot.equals(this.robot))
                continue;
            // check if can see
            if (!canSeeLocation(seenRobot.getLocation()))
                continue; 
            // check if right team
            if (team != null && seenRobot.getTeam() != team)
                continue;
            validSeenRobots.add(seenRobot.getRobotInfo(getType().canTrueSense()));
        }
        return validSeenRobots.toArray(new RobotInfo[validSeenRobots.size()]);
    }

    @Override 
    public double seePassability(MapLocation loc) throws GameActionException {
        assertCanSeeLocation(loc);
        return this.gameWorld.getPassability(loc);
    }

    @Override 
    public double seeLead(MapLocation loc) throws GameActionException {
        assertCanSeeLocation(loc);
        return this.gameWorld.getLead(loc);
    }

    @Override 
    public double seeGold(MapLocation loc) throws GameActionException {
        assertCanSeeLocation(loc);
        return this.gameWorld.getGold(loc);
    }

    @Override
    public MapLocation adjacentLocation(Direction dir) {
        return getLocation().add(dir);
    }

    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************

    private boolean isLocationOccupied(MapLocation loc) throws GameActionException {
        return this.gameWorld.getRobot(loc) != null;
    }

    private void assertIsActionReady() throws GameActionException {
        if (getActionCooldownTurns() >= 1)
            throw new GameActionException(IS_NOT_READY,
                    "This robot's action cooldown has not expired.");
        if (!robot.getMode().canAct)
            throw new GameActionException(CANT_DO_THAT,
                    "This robot is not in a mode that can act.");
    }

    /**
     * Check if the robot is ready to perform an action. Returns true if
     * the current action cooldown counter is strictly less than 1.
     *
     * @return true if the robot can do an action, false otherwise
     */
    @Override
    public boolean isActionReady() {
        try {
            assertIsActionReady();
            return true;
        } catch (GameActionException e) { return false; }
    }

    /**
     * Return the action cooldown turn counter of the robot. If this is < 1, the robot
     * can perform an action; otherwise, it cannot.
     * The counter is decreased by 1 at the start of every
     * turn, and increased to varying degrees by different actions taken.
     *
     * @return the number of action cooldown turns as a float
     */
    @Override
    public double getActionCooldownTurns() {
        return this.robot.getActionCooldownTurns();
    }

    private void assertIsMovementReady() throws GameActionException {
        if (getMovementCooldownTurns() >= 1)
            throw new GameActionException(IS_NOT_READY,
                    "This robot's movement cooldown has not expired.");
    }

    /**
     * Check if the robot is ready to move. Returns true if
     * the current movement cooldown counter is strictly less than 1.
     *
     * @return true if the robot can move, false otherwise
     */
    @Override
    public boolean isMovementReady() {
        try {
            assertIsMovementReady();
            return true;
        } catch (GameActionException e) { return false; }
    }

    /**
     * Return the movement cooldown turn counter of the robot. If this is < 1, the robot
     * can move; otherwise, it cannot.
     * The counter is decreased by 1 at the start of every
     * turn, and increased by moving.
     *
     * @return the number of cooldown movement turns as a float
     */
    @Override
    public double getMovementCooldownTurns() {
        return this.robot.getMovementCooldownTurns();
    }

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    private void assertCanMove(Direction dir) throws GameActionException {
        assertNotNull(dir);
        assertIsMovementReady();
        if (robot.getMode().canMove)
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot move.");
        MapLocation loc = adjacentLocation(dir);
        if (!onTheMap(loc))
            throw new GameActionException(OUT_OF_RANGE,
                    "Can only move to locations on the map; " + loc + " is not on the map.");
        if (isLocationOccupied(loc))
            throw new GameActionException(CANT_MOVE_THERE,
                    "Cannot move to an occupied location; " + loc + " is occupied.");
        if (!isMovementReady())
            throw new GameActionException(IS_NOT_READY,
                    "Robot is still cooling down! You need to wait before you can perform another action.");
    }

    @Override
    public boolean canMove(Direction dir) {
        try {
            assertCanMove(dir);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public void move(Direction dir) throws GameActionException {
        assertCanMove(dir);
        MapLocation center = adjacentLocation(dir);
        this.robot.addMovementCooldownTurns(this.robot.getType().movementCooldown);
        this.gameWorld.moveRobot(getLocation(), center);
        this.robot.setLocation(center);

        gameWorld.getMatchMaker().addMoved(getID(), getLocation());
    }

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    private void assertCanBuildRobot(RobotType type, Direction dir) throws GameActionException {
        assertNotNull(type);
        assertNotNull(dir);
        assertIsActionReady();
        if (!getType().canBuild(type))
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot build robots of type" + type + ".");

        // CHECK FUNCTION NAMES FOR GETTING LEAD/GOLD COSTS AND SUPPLIES
        int leadNeeded = type.getLeadCost();
        int goldNeeded = type.getGoldCost();
        Team team = getTeam();
        if (gameWorld.getTeamInfo().getLead(team) < leadNeeded) {
            throw new GameActionException(CANT_DO_THAT,
                    "Insufficient amount of lead.");
        }
        if (gameWorld.getTeamInfo().getGold(team) < goldNeeded) {
            throw new GameActionException(CANT_DO_THAT,
                    "Insufficient amount of gold.");
        }

        MapLocation spawnLoc = adjacentLocation(dir);
        if (!onTheMap(spawnLoc))
            throw new GameActionException(OUT_OF_RANGE,
                    "Can only spawn to locations on the map; " + spawnLoc + " is not on the map.");
        if (isLocationOccupied(spawnLoc))
            throw new GameActionException(CANT_MOVE_THERE,
                    "Cannot spawn to an occupied location; " + spawnLoc + " is occupied.");
    }

    @Override
    public boolean canBuildRobot(RobotType type, Direction dir) {
        try {
            assertCanBuildRobot(type, dir);
            return true;
        } catch (GameActionException e) { return false; }
    }

    // TODO: CHECK FUNCTION NAMES
    @Override
    public void buildRobot(RobotType type, Direction dir) throws GameActionException {
        assertCanBuildRobot(type, dir);

        int leadNeeded = type.getLeadCost();
        int goldNeeded = type.getGoldCost();

        this.robot.addCooldownTurns();

        this.robot.addLead(-leadNeeded);
        this.robot.addGold(-goldNeeded);

        int robotID = gameWorld.spawnRobot(this.robot, type, adjacentLocation(dir), getTeam());

        int robotID = gameWorld.spawnRobot(this.robot, type, adjacentLocation(dir), getTeam());

        // Undo because setting cooldown is automatically done 
        // // set cooldown turns here, because not all new robots have cooldown (eg. switching teams)
        // InternalRobot newBot = getRobotByID(robotID);
        // newBot.setCooldownTurns(type.initialCooldown);

        gameWorld.getMatchMaker().addAction(getID(), Action.SPAWN_UNIT, robotID);
    }

    // *****************************
    // **** COMBAT UNIT METHODS **** 
    // *****************************

    private void assertCanAttack(MapLocation loc) throws GameActionException {
        assertIsActionReady();
        if (!getType().canAttack())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot attack.");
        if (!this.robot.canActLocation(loc))
            throw new GameActionException(OUT_OF_RANGE,
                    "Robot can't be attacked because it is out of range.");
        InternalRobot bot = getRobot(loc);
        if (bot.getTeam() == getTeam())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is not on the enemy team.");
    }

    @Override
    boolean canAttack(MapLocation loc){
        try {
            assertCanAttack(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    void attack(MapLocation loc) throws GameActionException{
        assertCanAttack(loc);
        this.robot.attack(loc);
        InternalRobot bot = gameWorld.getRobot(loc);
        int attackedID = bot.getID();
        gameWorld.getMatchMaker().addAction(getID(), Action.ATTACK, attackedID);
    }

    // *****************************
    // ****** ARCHON METHODS ****** 
    // *****************************

    private void assertCanHealDroid(MapLocation loc) throws GameActionException {
        assertIsActionReady();
        if (!getType().canHealDroid()) {
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot heal droids.");
        }else if (!this.robot.canActLocation(loc)){
            throw new GameActionException(OUT_OF_RANGE,
                    "This robot can't be healed belocation can't be min because it is out of range.");
        }
        InternalRobot bot = gameWorld.getRobot(loc);
        if (!(bot.getType().canBeHealed())){
            throw new GameActionException(CANT_DO_THAT, 
                    "Robot is not of a type that can be healed.");
        }
        if (bot.getTeam() != getTeam()){
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is not on your team so can't be healed.");
        }
    }

    @Override
    boolean canHealDroid(MapLocation loc){
        try {
            assertCanHealDroid(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    void healDroid(MapLocation loc) throws GameActionException{
        assertCanHealDroid(loc);
        this.robot.healDroid(loc);
        InternalRobot bot = gameWorld.getRobot(loc);
        int healedID = bot.getID();
        gameWorld.getMatchMaker().addAction(getID(), Action.HEAL_DROID, healedID);
    }

    
    // ***********************
    // **** MINER METHODS **** 
    // ***********************

    private void assertCanMineLead(MapLocation loc) throws GameActionException {
        assertIsActionReady();
        if (!getType().canMine()) {
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot mine.");
        }else if (!this.robot.canActLocation(loc)){
            throw new GameActionException(OUT_OF_RANGE,
                    "Robot can't be healed because it is out of range.");
        }
        int leadAmount = gameWorld.getLeadCount(loc);
        if (leadAmount < 0){
            throw new GameActionException(CANT_DO_THAT, 
                    "Lead amount must be positive to be mined.");
        }
    }

    @Override
    boolean canMineLead(MapLocation loc){
        try {
            assertCanMineLead(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    void mineLead(MapLocation loc) throws GameActionException{
        assertCanMineLead(loc);
        this.robot.mineLead(loc);
        gameWorld.getMatchMaker().addAction(getID(), Action.MINE_LEAD, loc);
    }

    private void assertCanMineGold(MapLocation loc) throws GameActionException {
        assertIsActionReady();
        if (!getType().canMine()) {
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot mine.");
        }else if (!this.robot.canActLocation(loc)){
            throw new GameActionException(OUT_OF_RANGE,
                    "This location can't be mined because it is out of range.");
        }
        int goldAmount = gameWorld.getGoldCount(loc);
        if (goldAmount < 0){
            throw new GameActionException(CANT_DO_THAT, 
                    "Gold amount must be positive to be mined.");
        }
    }

    @Override
    boolean canMineGold(MapLocation loc){
        try {
            assertCanMineGold(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    void mineGold(MapLocation loc) throws GameActionException{
        assertCanMineGold(loc);
        this.robot.mineGold(loc);
        gameWorld.getMatchMaker().addAction(getID(), Action.MINE_GOLD, loc);
    }

    // *************************
    // **** BUILDER METHODS **** 
    // *************************

    private void assertCanUpgrade(MapLocation loc) throws GameActionException {
        assertIsActionReady();
        if (!getType().canUpgrade()) {
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot upgrade buildings.");
        }else if (!this.robot.canActLocation(loc)){
            throw new GameActionException(OUT_OF_RANGE,
                    "Robot can't be upgraded because it is out of range.");
        }
        InternalRobot bot = gameWorld.getRobot(loc);
        if (!(bot.getType().canBeUpgraded())){
            throw new GameActionException(CANT_DO_THAT, 
                    "Robot is not of a type that can be upgraded.");
        }
        if (bot.getTeam() != getTeam()){
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is not on your team so can't be upgraded.");
        }
        if (getLead() < bot.getLeadUpgradeCost()){
            throw new GameActionException(CANT_DO_THAT,
                    "You don't have enough lead to upgrade this robot.");
        }
        if (getGold() < bot.getGoldUpgradeCost()){
            throw new GameActionException(CANT_DO_THAT,
                    "You don't have enough gold to upgrade this robot.");
        }
    }

    @Override
    boolean canUpgrade(MapLocation loc){
        try {
            assertCanUpgrade(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    void upgrade(MapLocation loc) throws GameActionException{
        assertCanUpgrade(loc);
        this.robot.upgrade(loc);
        InternalRobot bot = gameWorld.getRobot(loc);
        int upgradedID = bot.getID();
        gameWorld.getMatchMaker().addAction(getID(), Action.UPGRADE, upgradedID);
    }

    private void assertCanRepairBuilding(MapLocation loc) throws GameActionException {
        assertIsActionReady();
        if (!getType().canRepairBuilding()) {
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot repair buildings.");
        }else if (!this.robot.canActLocation(loc)){
            throw new GameActionException(OUT_OF_RANGE,
                    "Robot can't be repaired because it is out of range.");
        }
        InternalRobot bot = gameWorld.getRobot(loc);
        if (!(bot.getType().canBeUpgraded())){
            throw new GameActionException(CANT_DO_THAT, 
                    "Robot is not of a type that can be repair.");
        }
        if (bot.getTeam() != getTeam()){
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is not on your team so can't be repaired.");
        }
    }

    @Override
    boolean canRepairBuilding(MapLocation loc){
        try {
            assertCanRepairBuilding(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    void repairBuilding(MapLocation loc) throws GameActionException{
        assertCanRepairBuilding(loc);
        this.robot.repairBuilding(loc);
        InternalRobot bot = gameWorld.getRobot(loc);
        int repairedID = bot.getID();
        gameWorld.getMatchMaker().addAction(getID(), Action.REPAIRD, repairedID);
    }

    // *******************************
    // **** ALCHEMIST LAB METHODS **** 
    // *******************************

    private void assertCanConvert() throws GameActionException {
        assertIsActionReady();
        if (!getType().canConvert()) {
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot convert lead to gold.");
        } else if (LEAD_TO_GOLD_RATE > getLead()) {
            throw new GameActionException(CANT_DO_THAT,
                    "You don't have enough lead to be able to convert to gold.");
        }
    }

    @Override
    boolean canConvert(){
        try {
            assertCanConvert();
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    void convert() throws GameActionException{
        assertCanConvert();
        this.robot.convert();
        gameWorld.getMatchMaker().addAction(getID(), Action.CONVERT);
    }

    // ***********************************
    // ****** COMMUNICATION METHODS ****** 
    // ***********************************

    //TODO: Communication needs to be fixed

    private void assertCanSetFlag(int flag) throws GameActionException {
        if (flag < GameConstants.MIN_FLAG_VALUE || flag > GameConstants.MAX_FLAG_VALUE) {
            throw new GameActionException(CANT_DO_THAT, "Flag value out of range");
        }
    }

    @Override
    public boolean canSetFlag(int flag) {
        try {
            assertCanSetFlag(flag);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public void setFlag(int flag) throws GameActionException {
        assertCanSetFlag(flag);
        this.robot.setFlag(flag);
        gameWorld.getMatchMaker().addAction(getID(), Action.SET_FLAG, flag);
    }

    private void assertCanGetFlag(int id) throws GameActionException {
        InternalRobot bot = getRobotByID(id);
        if (bot == null)
            throw new GameActionException(CANT_DO_THAT,
                    "Robot of given ID does not exist.");
        if (getType() != RobotType.ENLIGHTENMENT_CENTER &&
            bot.getType() != RobotType.ENLIGHTENMENT_CENTER &&
            !canSeeLocation(bot.getLocation()))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Robot at location is out of sensor range and not an Enlightenment Center.");
    }

    @Override
    public boolean canGetFlag(int id) {
        try {
            assertCanGetFlag(id);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public int getFlag(int id) throws GameActionException {
        assertCanGetFlag(id);

        return getRobotByID(id).getFlag();
    } 

    //TODO: move this back to public?

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    @Override
    public void disintegrate() {
        throw new RobotDeathException();
    }

    @Override
    public void resign() {
        gameWorld.getObjectInfo().eachRobot((robot) -> {
            if (robot.getTeam() == getTeam()) {
                gameWorld.destroyRobot(robot.getID());
            }
            return true;
        });
    }

    // ***********************************
    // ******** DEBUG METHODS ************
    // ***********************************

    @Override
    public void setIndicatorDot(MapLocation loc, int red, int green, int blue) {
        assertNotNull(loc);
        gameWorld.getMatchMaker().addIndicatorDot(getID(), loc, red, green, blue);
    }

    @Override
    public void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue) {
        assertNotNull(startLoc);
        assertNotNull(endLoc);
        gameWorld.getMatchMaker().addIndicatorLine(getID(), startLoc, endLoc, red, green, blue);
    }

}

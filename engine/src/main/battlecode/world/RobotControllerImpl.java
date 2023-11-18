package battlecode.world;

import battlecode.common.*;

import static battlecode.common.GameActionExceptionType.*;
import battlecode.instrumenter.RobotDeathException;
import battlecode.schema.Action;

import java.util.*;
import java.util.stream.Collectors;

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
        return getID();
    }

    // *********************************
    // ****** GLOBAL QUERY METHODS *****
    // *********************************

    @Override
    public int getRoundNum() {
        return this.gameWorld.getCurrentRound();
    }

    @Override
    public int getMapWidth() {
        return this.gameWorld.getGameMap().getWidth();
    }

    @Override
    public int getMapHeight() {
        return this.gameWorld.getGameMap().getHeight();
    }

    @Override
    public int getRobotCount() {
        return this.gameWorld.getObjectInfo().getRobotCount(getTeam());
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
    public MapLocation getLocation() {
        return this.robot.getLocation();
    }
 
    @Override
    public int getHealth() {
        return this.robot.getHealth();
    }
    @Override
    public int getBreadAmount() {
        return this.robot.getResourceAmount();
    }

    private InternalRobot getRobotByID(int id) {
        if (!this.gameWorld.getObjectInfo().existsRobot(id))
            return null;
        return this.gameWorld.getObjectInfo().getRobotByID(id);
    }

    private int locationToInt(MapLocation loc) {
        return this.gameWorld.locationToIndex(loc);
    }

    // ***********************************
    // ****** GENERAL VISION METHODS *****
    // ***********************************

    @Override
    public boolean onTheMap(MapLocation loc) {
        assertNotNull(loc);
         if (!this.gameWorld.getGameMap().onTheMap(loc))
            return false;
        return true;
    }

    private void assertCanSenseLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        if (!this.gameWorld.getGameMap().onTheMap(loc))
        throw new GameActionException(CANT_SENSE_THAT,
                "Target location is not on the map");
        if (!this.robot.canSenseLocation(loc))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target location not within vision range");
    }

    private void assertCanActLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        if (!this.robot.canActLocation(loc))
            throw new GameActionException(OUT_OF_RANGE,
                    "Target location not within action range");
        if (!this.gameWorld.getGameMap().onTheMap(loc))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target location is not on the map");
    }

    @Override
    public boolean canSenseLocation(MapLocation loc) {
        try {
            assertCanSenseLocation(loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public boolean canActLocation(MapLocation loc) {
        try {
            assertCanActLocation(loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public boolean isLocationOccupied(MapLocation loc) throws GameActionException {
        assertCanSenseLocation(loc);
        return this.gameWorld.getRobot(loc) != null;
    }

    @Override
    public boolean canSenseRobotAtLocation(MapLocation loc) {
        try {
            return isLocationOccupied(loc);
        } catch (GameActionException e) { return false; }
    }

    @Override
    public RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException {
        assertCanSenseLocation(loc);
        InternalRobot bot = this.gameWorld.getRobot(loc);
        return bot == null ? null : bot.getRobotInfo();
    }

    @Override
    public boolean canSenseRobot(int id) {
        InternalRobot sensedRobot = getRobotByID(id);
        return sensedRobot == null ? false : canSenseLocation(sensedRobot.getLocation());
    }

    @Override
    public RobotInfo senseRobot(int id) throws GameActionException {
        if (!canSenseRobot(id))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Can't sense given robot; It may be out of vision range or not exist anymore");
        return getRobotByID(id).getRobotInfo();
    }

    private void assertRadiusNonNegative(int radiusSquared) throws GameActionException {
        if (radiusSquared < -1) {
            throw new GameActionException(CANT_DO_THAT, "The radius for a sense command can't be negative and not -1");
        }
    }

    @Override
    public RobotInfo[] senseNearbyRobots() {
        try {
            return senseNearbyRobots(-1);
        } catch (GameActionException e) {
            return new RobotInfo[0];
        }
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int radiusSquared) throws GameActionException {
        assertRadiusNonNegative(radiusSquared);
        return senseNearbyRobots(radiusSquared, null);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int radiusSquared, Team team) throws GameActionException {
        assertRadiusNonNegative(radiusSquared);
        return senseNearbyRobots(getLocation(), radiusSquared, team);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared, Team team) throws GameActionException {
        assertNotNull(center);
        assertRadiusNonNegative(radiusSquared);
        int actualRadiusSquared = radiusSquared == -1 ? getType().visionRadiusSquared : Math.min(radiusSquared, getType().visionRadiusSquared);
        InternalRobot[] allSensedRobots = gameWorld.getAllRobotsWithinRadiusSquared(center, actualRadiusSquared, team);
        List<RobotInfo> validSensedRobots = new ArrayList<>();
        for (InternalRobot sensedRobot : allSensedRobots) {
            // check if this robot
            if (sensedRobot.equals(this.robot))
                continue;
            // check if can sense
            if (!canSenseLocation(sensedRobot.getLocation()))
                continue; 
            // check if right team
            if (team != null && sensedRobot.getTeam() != team)
                continue;
            validSensedRobots.add(sensedRobot.getRobotInfo());
        }
        return validSensedRobots.toArray(new RobotInfo[validSensedRobots.size()]);
    }

    @Override
    public boolean sensePassability(MapLocation loc) throws GameActionException {
        assertCanSenseLocation(loc);
        return this.gameWorld.isPassable(loc);
    }

    private MapInfo getMapInfo(MapLocation loc) throws GameActionException {
        double[] cooldownMultipliers = new double[2];
        int[][] numActiveElements = new int[2][2];
        int[][] turnsLeft = new int[2][2];
        int BOOST_INDEX = 0;
        int DESTABILIZE_INDEX = 1;
        for (Team team : Team.values()) {
            if (team == Team.NEUTRAL) {
                continue;
            }
            cooldownMultipliers[team.ordinal()] = gameWorld.getCooldownMultiplier(loc, team);
            numActiveElements[team.ordinal()][BOOST_INDEX] = gameWorld.getNumActiveBoosts(loc, team);
            numActiveElements[team.ordinal()][DESTABILIZE_INDEX] = gameWorld.getNumActiveDestabilize(loc, team);
            int oldestBoost = gameWorld.getOldestBoost(loc, team);
            turnsLeft[team.ordinal()][BOOST_INDEX] = oldestBoost == -1 ? -1 : oldestBoost - getRoundNum();
            int oldestDestabilize = gameWorld.getOldestDestabilize(loc, team);
            turnsLeft[team.ordinal()][DESTABILIZE_INDEX] = oldestDestabilize == -1 ? -1 : oldestDestabilize - getRoundNum();
        }
        MapInfo currentLocInfo = new MapInfo(loc, gameWorld.getCloud(loc), !gameWorld.getWall(loc), cooldownMultipliers, gameWorld.getCurrent(loc), numActiveElements, turnsLeft);
        return currentLocInfo;
    }

    @Override
    public MapInfo senseMapInfo(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSenseLocation(loc);
        return getMapInfo(loc);
    }

    @Override
    public MapInfo[] senseNearbyMapInfos() {
        try {
            return senseNearbyMapInfos(-1);
        } catch (GameActionException e) {
            return new MapInfo[0];
        }
    }

    @Override
    public MapInfo[] senseNearbyMapInfos(int radiusSquared) throws GameActionException {
        assertRadiusNonNegative(radiusSquared);
        return senseNearbyMapInfos(getLocation(), radiusSquared);
    }

    @Override
    public MapInfo[] senseNearbyMapInfos(MapLocation center) throws GameActionException {
        assertNotNull(center);
        return senseNearbyMapInfos(center, -1);
    }

    @Override
    public MapInfo[] senseNearbyMapInfos(MapLocation center, int radiusSquared) throws GameActionException {
        assertNotNull(center);
        assertRadiusNonNegative(radiusSquared);
        int actualRadiusSquared = radiusSquared == -1 ? getType().visionRadiusSquared : Math.min(radiusSquared, getType().visionRadiusSquared);
        MapLocation[] allSensedLocs = gameWorld.getAllLocationsWithinRadiusSquared(center, actualRadiusSquared);
        List<MapInfo> validSensedMapInfo = new ArrayList<>();
        for (MapLocation mapLoc : allSensedLocs) {
            // Can't actually sense location
            if (!canSenseLocation(mapLoc)) {
                continue;
            }
            MapInfo mapInfo = getMapInfo(mapLoc);
            validSensedMapInfo.add(mapInfo);
        }
        return validSensedMapInfo.toArray(new MapInfo[validSensedMapInfo.size()]);
    }

    @Override
    public MapLocation adjacentLocation(Direction dir) {
        return getLocation().add(dir);
    }

    @Override
    public MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared) throws GameActionException {
        assertNotNull(center);
        assertRadiusNonNegative(radiusSquared);
        int actualRadiusSquared = radiusSquared == -1 ? getType().visionRadiusSquared : Math.min(radiusSquared, getType().visionRadiusSquared);
        MapLocation[] possibleLocs = this.gameWorld.getAllLocationsWithinRadiusSquared(center, actualRadiusSquared);
        List<MapLocation> visibleLocs = Arrays.asList(possibleLocs).stream().filter(x -> canSenseLocation(x)).collect(Collectors.toList());
        return visibleLocs.toArray(new MapLocation[visibleLocs.size()]);
    }

    // ***********************************
    // ****** MAP LOCATION METHODS *******
    // ***********************************

    private void assertCanSpawn(MapLocation loc) throws GameActionException {
        // TODO implement assertCanSpawn
    }

    @Override
    public boolean canSpawn(MapLocation loc) {
        try {
            assertCanSpawn(loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    private void assertCanHeal(MapLocation loc) throws GameActionException {
        // TODO implement assertCanHeal
    }

    @Override
    public boolean canHeal(MapLocation loc) {
        try {
            assertCanHeal(loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    // ***********************************
    // ****** BUILDING METHODS ********
    // ***********************************

    private void assertCanBuild(TrapType trap, MapLocation loc) throws GameActionException{
        assertNotNull(trap);
        assertCanActLocation(loc);
        assertIsActionReady();
        if (getResourceAmount() < trap.buildCost){
            throw new GameActionException(NOT_ENOUGH_RESOURCE, "Insufficient resources");
        }
        for (InternalRobot rob : this.gameWorld.getAllRobotsWithinRadiusSquared(loc, 2, getTeam().opponent())){
            throw new GameActionException(CANT_DO_THAT, "Cannot place a trap directly on or next to an enemy robot.");
        }
        //can this be used to check for enemy traps??
        if (this.gameWorld.hasTrap(loc)){
            throw new GameActionException(CANT_DO_THAT, "Cannot place a trap on a tile with a trap already on it.");
        }
    }

    @Override
    public boolean canBuild(TrapType trap, MapLocation loc){
        try{
            assertCanBuild(trap, loc);
            return true;
        }
        catch (GameActionException e){
            return false;
        }
    }

    @Override
    public void build(TrapType trap, MapLocation loc) throws GameActionException{
        assertCanBuild(trap, loc);
        Trap toPlace = new Trap(loc, trap, this.getTeam());
        this.gameWorld.placeTrap(loc, toPlace);
        //this.gameWorld.getMatchMaker().addAction(getID(), Action.BUILD_TRAP, trapIndex)
        this.robot.addResourceAmount(-1*(trap.buildCost));
        //TODO: implement cooldown multiplier based on skill level
        this.robot.addActionCooldownTurns(trap.actionCooldownIncrease*(1-COOLDOWNMULTIPLIER));
    }

    private void assertCanFill(MapLocation loc) throws GameActionException {
        // TODO implement assertCanFill
    }

    @Override
    public boolean canFill(MapLocation loc) {
        try {
            assertCanFill(loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    private void assertCanDig(MapLocation loc) throws GameActionException {
        // TODO implement assertCanDig
    }

    @Override
    public boolean canDig(MapLocation loc) {
        try {
            assertCanDig(loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************

    private void assertIsSpawned() throws GameActionException {
        if (!this.robot.isSpawned()) {
            throw new GameActionException(IS_NOT_READY,
                    "This robot is not spawned in.");
        }
    }

    @Override
    public boolean isSpawned() {
        try {
            assertIsSpawned();
            return true;
        } catch (GameActionException e) { return false; }
    }

    private void assertIsActionReady() throws GameActionException {
        assertIsSpawned();
        if (!this.robot.canActCooldown())
            throw new GameActionException(IS_NOT_READY,
                    "This robot's action cooldown has not expired.");
    }

    @Override
    public boolean isActionReady() {
        try {
            assertIsActionReady();
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public int getActionCooldownTurns() {
        return this.robot.getActionCooldownTurns();
    }

    private void assertIsMovementReady() throws GameActionException {
        assertIsSpawned();
        if (!this.robot.canMoveCooldown())
            throw new GameActionException(IS_NOT_READY,
                    "This robot's movement cooldown has not expired.");
    }

    @Override
    public boolean isMovementReady() {
        try {
            assertIsMovementReady();
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public int getMovementCooldownTurns() {
        return this.robot.getMovementCooldownTurns();
    }

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    private void assertCanMove(Direction dir) throws GameActionException {
        assertNotNull(dir);
        assertIsMovementReady();
        MapLocation loc = adjacentLocation(dir);
        if (this.getType() == RobotType.HEADQUARTERS)
            throw new GameActionException(CANT_DO_THAT, "Headquarters can't move");
        if (!onTheMap(loc))
            throw new GameActionException(OUT_OF_RANGE,
                    "Can only move to locations on the map; " + loc + " is not on the map.");
        if (isLocationOccupied(loc))
            throw new GameActionException(CANT_MOVE_THERE,
                    "Cannot move to an occupied location; " + loc + " is occupied.");
        if (!this.gameWorld.isPassable(loc))
            throw new GameActionException(CANT_MOVE_THERE,
                    "Cannot move to an impassable location; " + loc + " is impassable.");
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
        MapLocation nextLoc = adjacentLocation(dir);
        Team[] allSpawnZones = {null, Team.A, Team.B};
        this.robot.setLocation(nextLoc);

        int amtBread = this.gameWorld.getBreadAmount(nextLoc);
        if(amtBread != 0) this.robot.addResourceAmount(amtBread);
        this.gameWorld.removeBread(nextLoc);
        this.robot.addMovementCooldownTurns();

        if (this.robot.hasFlag() && allSpawnZones[this.gameWorld.getSpawnZone(nextLoc)+1] == this.getTeam()) {
            this.gameWorld.getTeamInfo().captureFlag(this.getTeam());
            robot.getFlag().setLoc(null);
            gameWorld.getAllFlags().remove(robot.getFlag());
            this.robot.removeFlag();
        }
    }

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    private void assertCanSpawn(MapLocation loc) throws GameActionException {
        if (isSpawned())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot cannot call spawn when already spawned in.");

        if (!this.robot.canSpawnCooldown())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is not ready to be spawned.");

        // TODO: Implement spawn locations
        if (!this.gameWorld.isSpawnLocation(loc))
            throw new GameActionException(CANT_MOVE_THERE,
                    "Cannot spawn in a non-spawn location; " + loc + " is not a spawn location");

        if (isLocationOccupied(loc)) {
            throw new GameActionException(CANT_MOVE_THERE,
                    "Cannot spawn to an occupied location; " + loc + " is occupied.");
        }

        if (!sensePassability(loc)) {
            throw new GameActionException(CANT_MOVE_THERE,
                    "Cannot spawn to " + loc + "; It has a wall.");
        }
    }

    @Override
    public boolean canSpawn(MapLocation loc) {
        try {
            assertCanSpawn(loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public void spawn(MapLocation loc) throws GameActionException {
        assertCanSpawn(loc);
        this.gameWorld.addRobot(loc, robot);
        this.gameWorld.getObjectInfo().addRobotIndex(robot, loc);
        this.robot.spawn(loc);
    }

    // *****************************
    // **** COMBAT UNIT METHODS **** 
    // *****************************

    private void assertCanAttack(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsActionReady();
    }

    @Override
    public boolean canAttack(MapLocation loc) {
        try {
            assertCanAttack(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void attack(MapLocation loc) throws GameActionException {
        assertCanAttack(loc);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        this.robot.attack(loc);
    }

    // ***************************
    // ******* FLAG METHODS ******
    // ***************************
    
    private void assertCanDropFlag(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        if (!robot.hasFlag())
            throw new GameActionException(CANT_DO_THAT, 
                "This robot is not holding a flag.");
        
        if(!this.gameWorld.isPassable(loc))
        throw new GameActionException(CANT_DO_THAT, 
                "A flag can't be placed at this location.");

        // TODO decide whether flags can be placed on traps, and if so create the code for the check
    }

    @Override
    public boolean canDropFlag(MapLocation loc) {
        try {
            assertCanDropFlag(loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public void dropFlag(MapLocation loc) throws GameActionException{
        assertCanDropFlag(loc);
        this.gameWorld.addFlag(loc, robot.getFlag());
        robot.removeFlag();
    }

    private void assertCanPickupFlag(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        if(robot.hasFlag()) {
            throw new GameActionException(CANT_DO_THAT, "This robot is already holding flag.");
        }
        if(this.gameWorld.getFlags(loc) == null) {
            throw new GameActionException(CANT_DO_THAT, "There aren't any flags at this location.");
        }
    }

    @Override
    public boolean canPickupFlag(MapLocation loc) {
        try {
            assertCanPickupFlag(loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public void pickupFlag(MapLocation loc) throws GameActionException {
        assertCanPickupFlag(loc);
        Flag tempflag = this.gameWorld.getFlags(loc).get(0);
        this.gameWorld.removeFlag(loc, tempflag);
        robot.addFlag(tempflag);
    }

    // ***********************************
    // ****** COMMUNICATION METHODS ****** 
    // ***********************************

    private void assertValidIndex(int index) throws GameActionException {
        if (index < 0 || index >= GameConstants.SHARED_ARRAY_LENGTH)
            throw new GameActionException(CANT_DO_THAT, "You can't access this index as it is not within the shared array.");
    }

    private void assertValidValue(int value) throws GameActionException {
        if (value < 0 || value > GameConstants.MAX_SHARED_ARRAY_VALUE)
            throw new GameActionException(CANT_DO_THAT, "You can't write this value to the shared array " +
                "as it is not within the range of allowable values: [0, " + GameConstants.MAX_SHARED_ARRAY_VALUE + "].");
    }

    @Override
    public int readSharedArray(int index) throws GameActionException {
        assertValidIndex(index);
        return this.gameWorld.getTeamInfo().readSharedArray(getTeam(), index);
    }

    private void assertCanWriteSharedArray(int index, int value) throws GameActionException{
        assertValidIndex(index);
        assertValidValue(value);
        if (!this.gameWorld.inRangeForAmplification(this.robot)) {
            throw new GameActionException(CANT_DO_THAT, "You cannot write to the shared array");
        }
    }

    @Override
    public boolean canWriteSharedArray(int index, int value){
        try {
            assertCanWriteSharedArray(index, value);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void writeSharedArray(int index, int value) throws GameActionException {
        assertCanWriteSharedArray(index, value);
        this.gameWorld.getTeamInfo().writeSharedArray(getTeam(), index, value);
    }



    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    @Override
    public void disintegrate() {
        throw new RobotDeathException();
    }

    @Override
    public void resign() {
        Team team = getTeam();
        gameWorld.getObjectInfo().eachRobot((robot) -> {
            if (robot.getTeam() == team) {
                gameWorld.destroyRobot(robot.getID());
            }
            return true;
        });
        gameWorld.setWinner(team.opponent(), DominationFactor.RESIGNATION);
    }

    // ***********************************
    // ******** DEBUG METHODS ************
    // ***********************************

    @Override
    public void setIndicatorString(String string) {
        if (string.length() > GameConstants.INDICATOR_STRING_MAX_LENGTH) {
            string = string.substring(0, GameConstants.INDICATOR_STRING_MAX_LENGTH);
        }
        this.robot.setIndicatorString(string);
    }

    @Override
    public void setIndicatorDot(MapLocation loc, int red, int green, int blue) {
        assertNotNull(loc);
        this.gameWorld.getMatchMaker().addIndicatorDot(getID(), loc, red, green, blue);
    }

    @Override
    public void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue) {
        assertNotNull(startLoc);
        assertNotNull(endLoc);
        this.gameWorld.getMatchMaker().addIndicatorLine(getID(), startLoc, endLoc, red, green, blue);
    }
}

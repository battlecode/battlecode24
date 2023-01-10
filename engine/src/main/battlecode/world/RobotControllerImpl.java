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
    public RobotType getType() {
        return this.robot.getType();
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
    public int getResourceAmount(ResourceType rType) {
        return this.robot.getResource(rType);  
    }

    @Override
    public Anchor getAnchor() {
        return this.robot.getTypeAnchor();  
    }

    @Override
    public int getNumAnchors(Anchor anchor) {
        if (anchor == null) {
            return this.robot.getNumAnchors(Anchor.STANDARD) + this.robot.getNumAnchors(Anchor.ACCELERATING);
        }
        return this.robot.getNumAnchors(anchor);  
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
    public boolean onTheMap(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
         if (!this.gameWorld.getGameMap().onTheMap(loc))
            return false;
        if (!this.robot.canSenseLocation(loc))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target location not within vision range");
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
    
    @Override
    public double senseCooldownMultiplier(MapLocation loc) throws GameActionException{
        assertCanSenseLocation(loc);
        return this.gameWorld.getCooldownMultiplier(loc, getTeam());
    }

    @Override
    public int senseDestabilizeTurns(MapLocation loc) throws GameActionException{
        assertCanSenseLocation(loc);
        int oldestDestabilize = this.gameWorld.getOldestDestabilize(loc, getTeam());
        return oldestDestabilize == -1 ? -1 : oldestDestabilize - getRoundNum(); 
    }

    @Override
    public int senseBoostTurns(MapLocation loc) throws GameActionException{
        assertCanSenseLocation(loc);
        int oldestBoost = this.gameWorld.getOldestBoost(loc, getTeam());
        return oldestBoost == -1 ? -1 : oldestBoost - getRoundNum();
    }
    
    @Override
    public int senseIsland(MapLocation loc) throws GameActionException {
        assertCanSenseLocation(loc);
        Island island = this.gameWorld.getIsland(loc);
        return island == null ? -1 : island.ID;
    }

    @Override
    public int[] senseNearbyIslands() {
        Island[] allSensedIslands = gameWorld.getAllIslandsWithinVision(this.robot, getType().visionRadiusSquared);
        Set<Integer> islandIdsSet = new HashSet<>();
        for(int i = 0; i < allSensedIslands.length; i++) {
            islandIdsSet.add(allSensedIslands[i].ID);
        }
        int[] islandIds = new int[islandIdsSet.size()];
        int i = 0;
        for (Integer id : islandIdsSet) {
            islandIds[i] = id;
            i++;
        }
        return islandIds;
    }

    @Override
    public MapLocation[] senseNearbyIslandLocations(int idx) throws GameActionException {
        return senseNearbyIslandLocations(-1, idx);
    }
    
    @Override
    public MapLocation[] senseNearbyIslandLocations(int radiusSquared, int idx) throws GameActionException {
        assertRadiusNonNegative(radiusSquared);
        return senseNearbyIslandLocations(getLocation(), radiusSquared, idx);
    }

    @Override
    public MapLocation[] senseNearbyIslandLocations(MapLocation center, int radiusSquared, int idx) throws GameActionException {
        assertNotNull(center);
        assertRadiusNonNegative(radiusSquared);

        int actualRadiusSquared = radiusSquared == -1 ? getType().visionRadiusSquared : Math.min(radiusSquared, getType().visionRadiusSquared);

        Island island = gameWorld.getIsland(idx);
        if (island == null) {
            throw new GameActionException(CANT_SENSE_THAT, "Not a valid island id");
        }

        ArrayList<MapLocation> islandLocs = new ArrayList<>();
        for(MapLocation loc : island.locations) {
            if (canSenseLocation(loc) && center.distanceSquaredTo(loc) <= actualRadiusSquared) {
                islandLocs.add(loc);
            }
        }
        return islandLocs.toArray(new MapLocation[islandLocs.size()]);
    }

    private boolean canSenseIsland(Island island) {
        return Arrays.stream(island.locations).anyMatch(loc -> canSenseLocation(loc));
    }

    @Override
    public Team senseTeamOccupyingIsland(int islandIdx) throws GameActionException {
        Island island = gameWorld.getIsland(islandIdx);
        if (island == null || !canSenseIsland(island)) {
            throw new GameActionException(CANT_SENSE_THAT, "Cannot sense an island with that id");
        }

        return island.teamOwning;
    }

    @Override
    public int senseAnchorPlantedHealth(int islandIdx) throws GameActionException {
        Island island = gameWorld.getIsland(islandIdx);
        if (island == null || !canSenseIsland(island)) {
            throw new GameActionException(CANT_SENSE_THAT, "Cannot sense an island with that id");
        }
        return island.anchorHealth;
    }

    @Override
    public Anchor senseAnchor(int islandIdx) throws GameActionException {
        Island island = gameWorld.getIsland(islandIdx);
        if (island == null || !canSenseIsland(island)) {
            throw new GameActionException(CANT_SENSE_THAT, "Cannot sense an island with that id");
        }
        return island.anchorPlanted;
    }

    @Override
    public WellInfo senseWell(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSenseLocation(loc);
        Well well = this.gameWorld.getWell(loc);
        return well == null ? null : well.getWellInfo();
    }

    @Override
    public WellInfo[] senseNearbyWells() {
        return senseNearbyWells(null);
    }

    @Override
    public WellInfo[] senseNearbyWells(int radiusSquared) throws GameActionException {
        return senseNearbyWells(radiusSquared, null);
    }

    @Override
    public WellInfo[] senseNearbyWells(MapLocation center, int radiusSquared) throws GameActionException {
        return senseNearbyWells(center, radiusSquared, null);
    }

    @Override
    public WellInfo[] senseNearbyWells(ResourceType resourceType) {
        try {
            return senseNearbyWells(-1, resourceType);
        } catch (GameActionException e) {
            return new WellInfo[0];
        }
    }

    @Override
    public WellInfo[] senseNearbyWells(int radiusSquared, ResourceType resourceType) throws GameActionException {
        assertRadiusNonNegative(radiusSquared);
        return senseNearbyWells(getLocation(), radiusSquared, resourceType);
    }

    @Override
    public WellInfo[] senseNearbyWells(MapLocation center, int radiusSquared, ResourceType resourceType) throws GameActionException {
        assertNotNull(center);
        assertRadiusNonNegative(radiusSquared);
        int actualRadiusSquared = radiusSquared == -1 ? getType().visionRadiusSquared : Math.min(radiusSquared, getType().visionRadiusSquared);
        Well[] allSensedWells = gameWorld.getAllWellsWithinRadiusSquared(center, actualRadiusSquared);
        List<WellInfo> validSensedWells = new ArrayList<>();
        for (Well well : allSensedWells) {
            // Can't actually sense location
            if (!canSenseLocation(well.getMapLocation())) {
                continue;
            }
            // Resource types don't match
            if (resourceType != null && well.getResourceType() != resourceType) {
                continue;
            }
            validSensedWells.add(well.getWellInfo());
        }
        return validSensedWells.toArray(new WellInfo[validSensedWells.size()]);
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
        MapInfo currentLocInfo = new MapInfo(loc, gameWorld.getCloud(loc), cooldownMultipliers, gameWorld.getCurrent(loc), numActiveElements, turnsLeft);
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
        if (radiusSquared < 0)
            throw new GameActionException(CANT_DO_THAT,
                    "Radius squared must be non-negative.");
        MapLocation[] possibleLocs = this.gameWorld.getAllLocationsWithinRadiusSquared(center, Math.min(radiusSquared, getType().visionRadiusSquared));
        List<MapLocation> visibleLocs = Arrays.asList(possibleLocs).stream().filter(x -> canSenseLocation(x)).collect(Collectors.toList());
        return visibleLocs.toArray(new MapLocation[visibleLocs.size()]);
    }

    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************

    private void assertIsActionReady() throws GameActionException {
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
        this.robot.setLocation(nextLoc);
        this.robot.addMovementCooldownTurns();
    }

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    private void assertCanBuildRobot(RobotType type, MapLocation loc) throws GameActionException {
        assertNotNull(type);
        assertCanActLocation(loc);
        assertIsActionReady();

        if (getType() != RobotType.HEADQUARTERS)
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot build. Only headquarters can build.");
        for (ResourceType rType : ResourceType.values()) {
            if (rType == ResourceType.NO_RESOURCE)
                continue;
            if (getResourceAmount(rType) < type.getBuildCost(rType)) {
                throw new GameActionException(NOT_ENOUGH_RESOURCE,
                        "Insufficient amount of " + rType);
            }
        }
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
    public boolean canBuildRobot(RobotType type, MapLocation loc) {
        try {
            assertCanBuildRobot(type, loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public void buildRobot(RobotType type, MapLocation loc) throws GameActionException {
        assertCanBuildRobot(type, loc);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        Team team = getTeam();
        for (ResourceType rType : ResourceType.values()) {
            if (rType == ResourceType.NO_RESOURCE)
                continue;
            this.robot.addResourceAmount(rType, -1*type.getBuildCost(rType));
            this.gameWorld.getTeamInfo().addResource(rType, team, -1*type.getBuildCost(rType));
        }
        int newId = this.gameWorld.spawnRobot(type, loc, team);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.SPAWN_UNIT, newId);
    }

    private void assertCanBuildAnchor(Anchor anchor) throws GameActionException {
        assertNotNull(anchor);
        assertIsActionReady();
        if (getType() != RobotType.HEADQUARTERS)
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot build. Only headquarters can build.");
        for (ResourceType rType : ResourceType.values()) {
            if (rType == ResourceType.NO_RESOURCE)
                continue;
            if (getResourceAmount(rType) < anchor.getBuildCost(rType)) {
                throw new GameActionException(NOT_ENOUGH_RESOURCE,
                        "Insufficient amount of " + rType);
            }
        }
    }

    @Override
    public boolean canBuildAnchor(Anchor anchor) {
        try {
            assertCanBuildAnchor(anchor);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public void buildAnchor(Anchor anchor) throws GameActionException {
        assertCanBuildAnchor(anchor);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        Team team = getTeam();
        for (ResourceType rType : ResourceType.values()) {
            if (rType == ResourceType.NO_RESOURCE)
                continue;
            this.robot.addResourceAmount(rType, -1*anchor.getBuildCost(rType));
            this.gameWorld.getTeamInfo().addResource(rType, team, -1*anchor.getBuildCost(rType));
        }
        this.robot.addAnchor(anchor);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.BUILD_ANCHOR, anchor.getAccelerationIndex());
    }

    // *****************************
    // **** COMBAT UNIT METHODS **** 
    // *****************************

    private void assertCanAttack(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsActionReady();
        if (!getType().canAttack())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot attack.");
        InternalRobot bot = this.gameWorld.getRobot(loc);
        if (getType() == RobotType.CARRIER){
            int totalResources = getResourceAmount(ResourceType.ADAMANTIUM)+getResourceAmount(ResourceType.MANA)+getResourceAmount(ResourceType.ELIXIR);
            if (totalResources == 0)
                throw new GameActionException(CANT_DO_THAT,
                    "Robot is a carrier but has no inventory to attack with");
            if (!(bot == null) && bot.getTeam().equals(getTeam())) {
                throw new GameActionException(CANT_DO_THAT,
                        "Robot is not on the enemy team.");
            }
        } else {
            if (bot == null) {
                throw new GameActionException(CANT_DO_THAT,
                "There is no robot to attack");
            }
            if (bot.getTeam().equals(getTeam())) {
                throw new GameActionException(CANT_DO_THAT,
                        "Robot is not on the enemy team.");
            }
        }
        if (bot != null && bot.getType() == RobotType.HEADQUARTERS) {
            throw new GameActionException(CANT_DO_THAT,
            "Can't attack headquarters");
        }
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

    // ***********************************
    // ******** BOOSTERS METHODS *********
    // ***********************************

    private void assertCanBoost() throws GameActionException {
        assertIsActionReady();
        if (getType() != RobotType.BOOSTER)
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot boost.");
    }

    @Override
    public boolean canBoost() {
        try {
            assertCanBoost();
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void boost() throws GameActionException {
        assertCanBoost();
        MapLocation boostLoc = this.getLocation();
        this.gameWorld.addBoost(boostLoc, getTeam());
        this.gameWorld.getMatchMaker().addAction(getID(), Action.BOOST, locationToInt(boostLoc));
        this.robot.addActionCooldownTurns(getType().actionCooldown);
    }

    // ***********************************
    // ****** DESTABILIZER METHODS *******
    // ***********************************

    private void assertCanDestabilize(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsActionReady();
        if (getType() != RobotType.DESTABILIZER)
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot destabilize.");
    }

    @Override
    public boolean canDestabilize(MapLocation loc) {
        try {
            assertCanDestabilize(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void destabilize(MapLocation loc) throws GameActionException {
        assertCanDestabilize(loc);
        this.gameWorld.addDestabilize(loc, getTeam());
        this.gameWorld.getMatchMaker().addAction(getID(), Action.DESTABILIZE, locationToInt(loc));
        this.robot.addActionCooldownTurns(getType().actionCooldown);
    }

    // *************************
    // **** CARRIER METHODS **** 
    // *************************

    private boolean isWell(MapLocation loc) {
        return this.gameWorld.isWell(loc);
    }

    private boolean isHeadquarter(MapLocation loc){
        return this.gameWorld.isHeadquarters(loc);
    }

    private void assertCanTransferResource(MapLocation loc, ResourceType type, int amount) throws GameActionException {
        assertNotNull(loc);
        assertNotNull(type);
        assertCanActLocation(loc);
        assertIsActionReady();

        if (getType() != RobotType.CARRIER) {
            throw new GameActionException(CANT_DO_THAT, "This robot is not a carrier");
        }
        if (amount > 0 && getResourceAmount(type) < amount) { // Carrier is transfering to another location
            throw new GameActionException(CANT_DO_THAT, "Carrier does not have enough of that resource");
        }
        if (!this.robot.getLocation().isAdjacentTo(loc)) {
            throw new GameActionException(CANT_DO_THAT,
                    "Robot needs to be adjacent to transfer.");
        }
        if (amount < 0) { // Carrier is picking up the resource from another location (headquarters)
            if(!this.robot.canAdd(-1*amount)) {
                throw new GameActionException(CANT_DO_THAT, "Carrier does not have enough capacity to collect the resource");
            }
            if (!isHeadquarter(loc)) {
                throw new GameActionException(CANT_DO_THAT, "Carrier can only pick up resources from headquarters");
            }
            if (getTeam() != gameWorld.getRobot(loc).getTeam()) {
                throw new GameActionException(CANT_DO_THAT, "Carrier can only pick up resources from their team");
            }
            if (gameWorld.getRobot(loc).getResource(type) < -amount) {
                throw new GameActionException(CANT_DO_THAT, "Headquarter does not have enough of that resource");
            }
        }
        if (!isWell(loc) && !isHeadquarter(loc)) {
            throw new GameActionException(CANT_DO_THAT, "Cannot transfer to a location that is not a well or a headquarter");
        }
    }

    @Override
    public boolean canTransferResource(MapLocation loc, ResourceType rType, int amount){
        try {
            assertCanTransferResource(loc, rType, amount);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void transferResource(MapLocation loc, ResourceType rType, int amount) throws GameActionException {
        assertCanTransferResource(loc, rType, amount);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        if (isWell(loc)) {
            this.gameWorld.getWell(loc).addResourceAmount(rType, amount);
            this.gameWorld.getTeamInfo().addResource(rType, this.getTeam(), -1*amount);
        } else if(isHeadquarter(loc)){
            InternalRobot headquarter = this.gameWorld.getRobot(loc);
            if (headquarter.getType() != RobotType.HEADQUARTERS) {
                throw new IllegalArgumentException("Headquarter must be the robot at this location");
            }
            headquarter.addResourceAmount(rType, amount);
        }
        this.robot.addResourceAmount(rType, -amount);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.PLACE_RESOURCE, locationToInt(loc));
    }

    private void assertCanCollectResource(MapLocation loc, int amount) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsActionReady();
        if (getType() != RobotType.CARRIER)
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot collect.");
        if (!isWell(loc))
            throw new GameActionException(CANT_DO_THAT, 
                    "Location is not a well");
        if (!this.robot.getLocation().isAdjacentTo(loc)) {
            throw new GameActionException(CANT_DO_THAT,
                    "Robot needs to be adjacent to collect.");
        }
        int rate = this.gameWorld.getWell(loc).getRate();
        amount = amount == -1 ? rate : amount;
        if (amount > rate)
            throw new GameActionException(CANT_DO_THAT, 
                    "Amount is higher than rate");
        if (!this.robot.canAdd(amount))
            throw new GameActionException(CANT_DO_THAT, 
                    "Exceeded robot's carrying capacity");
    }     

    @Override
    public boolean canCollectResource(MapLocation loc, int amount){
        try {
            assertCanCollectResource(loc, amount);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void collectResource(MapLocation loc, int amount) throws GameActionException {
        assertCanCollectResource(loc, amount);
        this.robot.addActionCooldownTurns(getType().actionCooldown);

        ResourceType rType = gameWorld.getWell(loc).getResourceType();
        if (rType == ResourceType.NO_RESOURCE) {
            throw new IllegalArgumentException("Should not be a well with no resource");
        }
        int rate = this.gameWorld.getWell(loc).getRate();
        amount = amount == -1 ? rate : amount;
        this.robot.addResourceAmount(rType, amount);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.PICK_UP_RESOURCE, locationToInt(loc));
        this.gameWorld.getTeamInfo().addResource(rType, this.getTeam(), amount);
    }

    private void assertCanPlaceAnchor() throws GameActionException {
        assertIsActionReady();
        if (getType() != RobotType.CARRIER)
        throw new GameActionException(CANT_DO_THAT,
                "Robot is of type " + getType() + " which cannot have anchors.");
        MapLocation location = this.getLocation();
        Island island = this.gameWorld.getIsland(location);
        if (island == null)
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is not on an island.");
        if (!this.robot.holdingAnchor())
            throw new GameActionException(CANT_DO_THAT,"Robot is not holding anchor.");
        Anchor heldAnchor = this.robot.getTypeAnchor();
        if (!island.canPlaceAnchor(getTeam(), heldAnchor)) {
            throw new GameActionException(CANT_DO_THAT,"Can't place anchor on occupied island.");
        }
    }     

    @Override
    public boolean canPlaceAnchor() {
        try {
            assertCanPlaceAnchor();
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void placeAnchor() throws GameActionException {
        assertCanPlaceAnchor();
        MapLocation location = this.getLocation();
        Island island = this.gameWorld.getIsland(location);
        assert(island != null);
        Anchor heldAnchor = this.robot.getTypeAnchor();
        island.placeAnchor(getTeam(), heldAnchor);
        this.robot.releaseAnchor(heldAnchor);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.PLACE_ANCHOR, island.getID());
    }

    private void assertCanTakeAnchor(MapLocation loc, Anchor anchor) throws GameActionException {
        assertNotNull(loc);
        assertNotNull(anchor);
        assertCanActLocation(loc);
        assertIsActionReady();
        if (getType() != RobotType.CARRIER){
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot collect anchors.");
        }
        if (!isHeadquarter(loc)){
            throw new GameActionException(CANT_DO_THAT, 
                    "Can only take anchors from headquarters.");
        }
        if (getTeam() != gameWorld.getRobot(loc).getTeam()){
            throw new GameActionException(CANT_DO_THAT, 
                    "Can only take anchors from same team.");
        }
        if (!this.robot.getLocation().isAdjacentTo(loc)) {
            throw new GameActionException(CANT_DO_THAT,
                    "Robot needs to be adjacent to collect.");
        }
        InternalRobot hq = this.gameWorld.getRobot(loc);
        if (hq.getNumAnchors(anchor) < 1) {
            throw new GameActionException(CANT_DO_THAT, 
            "Not enough anchors");
        }
        if (!this.robot.canAddAnchor()) {
            throw new GameActionException(CANT_DO_THAT, 
            "Not enough capacity to pick up an anchor.");
        }
    } 

    @Override
    public boolean canTakeAnchor(MapLocation loc, Anchor anchor) {
        try {
            assertCanTakeAnchor(loc, anchor);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void takeAnchor(MapLocation loc, Anchor anchor) throws GameActionException {
        assertCanTakeAnchor(loc, anchor);
        InternalRobot headquarters = this.gameWorld.getRobot(loc);
        headquarters.releaseAnchor(anchor);
        this.robot.addAnchor(anchor);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.PICK_UP_ANCHOR, headquarters.getID()*2 + anchor.getAccelerationIndex());
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

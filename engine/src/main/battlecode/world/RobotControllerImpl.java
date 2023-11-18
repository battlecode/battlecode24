package battlecode.world;

import battlecode.common.*;

import static battlecode.common.GameActionExceptionType.*;
import battlecode.instrumenter.RobotDeathException;
import battlecode.schema.Action;

import java.util.*;
import java.util.stream.Collectors;

import javax.naming.InsufficientResourcesException;

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
    public int getIslandCount() {
        return this.gameWorld.getAllIslands().length;
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
    public int getBreadAmount() {
        return this.robot.getResourceAmount();
    }

    @Override
    public Anchor getAnchor() throws GameActionException {
        if (this.getType() != RobotType.CARRIER) {
            throw new GameActionException(CANT_DO_THAT, "getAnchor can only be called with carrier, use getNumAnchors for headquarters");
        }
        return this.robot.getTypeAnchor();  
    }

    @Override
    public int getNumAnchors(Anchor anchor) {
        if (anchor == null) {
            return this.robot.getNumAnchors(Anchor.STANDARD) + this.robot.getNumAnchors(Anchor.ACCELERATING);
        }
        return this.robot.getNumAnchors(anchor);  
    }

    @Override
    public int getWeight() {
        int resourceAmount = this.getResourceAmount(ResourceType.ADAMANTIUM) + this.getResourceAmount(ResourceType.MANA) + this.getResourceAmount(ResourceType.ELIXIR);
        int anchorAmount = this.getNumAnchors(null);
        return resourceAmount + GameConstants.ANCHOR_WEIGHT * anchorAmount;
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
    public boolean senseCloud(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        int visionRadius = this.getType().visionRadiusSquared;
        if (this.gameWorld.getCloud(this.getLocation())) {
            visionRadius = GameConstants.CLOUD_VISION_RADIUS_SQUARED;
        }
        if (this.getLocation().distanceSquaredTo(loc) > visionRadius) {
            throw new GameActionException(CANT_DO_THAT, "This location cannot be sensed");
        }
        return this.gameWorld.getCloud(loc);
    }

    @Override
    public MapLocation[] senseNearbyCloudLocations() {
        try {
            return senseNearbyCloudLocations(-1);
        } catch (GameActionException e) {
            return new MapLocation[0];
        }
    }

    @Override
    public MapLocation[] senseNearbyCloudLocations(int radiusSquared) throws GameActionException {
        assertRadiusNonNegative(radiusSquared);
        return senseNearbyCloudLocations(getLocation(), radiusSquared);
    }

    @Override
    public MapLocation[] senseNearbyCloudLocations(MapLocation center, int radiusSquared) throws GameActionException {
        assertNotNull(center);
        assertRadiusNonNegative(radiusSquared);
        int actualRadiusSquared = radiusSquared == -1 ? getType().visionRadiusSquared : Math.min(radiusSquared, getType().visionRadiusSquared);
        MapLocation[] allLocations = gameWorld.getAllLocationsWithinRadiusSquared(center, actualRadiusSquared);
        List<MapLocation> validSensedCloudLocs = new ArrayList<>();
        int visionRadius = getType().visionRadiusSquared;
        if (this.gameWorld.getCloud(this.getLocation())) {
            visionRadius = GameConstants.CLOUD_VISION_RADIUS_SQUARED;
        }
        for (MapLocation loc : allLocations) {
            // Can't actually sense location based on radius squared
            if (!getLocation().isWithinDistanceSquared(loc, visionRadius)) {
                continue;
            }
            // Check if location has a cloud
            if (!gameWorld.getCloud(loc)) {
                continue;
            }
            validSensedCloudLocs.add(loc);
        }
        return validSensedCloudLocs.toArray(new MapLocation[validSensedCloudLocs.size()]);
    }

    @Override
    public MapLocation[] senseNearbyFlagLocations(MapLocation center, int radiusSquared) throws GameActionException {
        assertNotNull(center);
        assertRadiusNonNegative(radiusSquared);
        int actualRadiusSquared = radiusSquared == -1 ? getType().visionRadiusSquared : Math.min(radiusSquared, getType().visionRadiusSquared);
        List<MapLocation> validSensedFlagLocs = new ArrayList<>();
        Flag[] allFlagsInRadius = this.gameWorld.getAllFlagsWithinRadiusSquared(center, actualRadiusSquared);
        for (Flag flag : allFlagsInRadius) {
            if (getLocation().isWithinDistanceSquared(flag.getLoc(), GameConstants.VISION_RADIUS))
                validSensedFlagLocs.add(flag.getLoc());
        }
        return validSensedFlagLocs.toArray(new MapLocation[validSensedFlagLocs.size()]);
    }

    @Override
    public MapLocation[] senseBroadcastFlagLocations() {
        List<MapLocation> currentBroadcastLocations = new ArrayList<MapLocation>();
        for(Flag x: gameWorld.getAllFlags()) {
            if(!canSenseLocation(x.getLoc())) {
                currentBroadcastLocations.add(x.getBroadcastLoc());
            }
        }
        return currentBroadcastLocations.toArray(new MapLocation[currentBroadcastLocations.size()]);
    }

    /* @Override
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
    } */

    private MapInfo getMapInfo(MapLocation loc) throws GameActionException {
        GameWorld gw = this.gameWorld;

        MapInfo currentLocInfo = new MapInfo(loc, gw.isPassable(loc), gw.getWall(loc),
            gw.getSpawnZone(loc), gw.getWater(loc), gw.getBreadAmount(loc), gw.getTrapType(loc));

        return currentLocInfo;

        /* // Old stuff
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
        return currentLocInfo; */
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

    private void assertCanDropFlag(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        if (!robot.hasFlag())
            throw new GameActionException(CANT_DO_THAT, 
                "This robot is not holding a flag.");
        
        if(!this.gameWorld.isPassable(loc))
            throw new GameActionException(CANT_DO_THAT, 
                    "A flag can't be placed at this location.");

        if(this.gameWorld.isSetupPhase()) {
            Flag[] flags = this.gameWorld.getAllFlagsWithinRadiusSquared(loc, GameConstants.MIN_FLAG_SPACING_SQUARED);
            for (Flag flag : flags) {
                if(flag.getTeam() == this.robot.getTeam()) 
                    throw new GameActionException(CANT_DO_THAT, 
                            "Flag placement is too close to another flag.");
            }
        }
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
        assertCanActLocation(loc);
        assertIsActionReady();
        if (!this.gameWorld.getWater(loc))
            throw new GameActionException(CANT_DO_THAT, "Can't fill a tile that is not water!");
        if (this.robot.getResourceAmount() < GameConstants.FILL_COST)
            throw new GameActionException(NOT_ENOUGH_RESOURCE, "Insufficient resources to fill.");
    }

    @Override
    public boolean canFill(MapLocation loc) {
        try {
            assertCanFill(loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    public void fill(MapLocation loc) throws GameActionException{
        assertCanFill(loc);
        this.robot.addActionCooldownTurns((GameConstants.FILL_COOLDOWN));
        this.robot.addResourceAmount(-1* GameConstants.FILL_COST);
        //this.gameWorld.getMatchMaker().addAction(getID(), Action.DIG, FILL_INDEX);
        this.gameWorld.setLand(loc);
    }

    private void assertCanDig(MapLocation loc) throws GameActionException {
        assertCanActLocation(loc);
        assertIsActionReady();
        if (this.gameWorld.getWater(loc))
            throw new GameActionException(CANT_DO_THAT, "Cannot dig on a tile that is already water.");
        if (this.gameWorld.getWall(loc))
            throw new GameActionException(CANT_DO_THAT, "Cannot dig on a tile that has a wall.");
        if (isLocationOccupied(loc))
            throw new GameActionException(CANT_DO_THAT, "Cannot dig on a tile that has a robot on it!");
        if (this.robot.getResourceAmount() < GameConstants.DIG_COST)
            throw new GameActionException(NOT_ENOUGH_RESOURCE, "Insufficient resources to dig.");
        if (this.gameWorld.hasFlag(loc))
            throw new GameActionException(CANT_DO_THAT, "Cannot dig under a tile with a flag currently on it.");
    }

    @Override
    public boolean canDig(MapLocation loc) {
        try {
            assertCanDig(loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public void dig(MapLocation loc){
        assertCanDig(loc);
        this.robot.addActionCooldownTurns(GameConstants.DIG_COOLDOWN);
        this.robot.addResourceAmount(-1*GameConstants.DIG_COST);
        //this.gameWorld.getMatchMaker().addAction(getID(), Action.DIG, DIG_INDEX);
        this.gameWorld.setWater(loc);
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
    private boolean isSpawned() {
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
        this.robot.setLocation(nextLoc);
        this.robot.addMovementCooldownTurns();
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

    /* private void assertCanBuildAnchor(Anchor anchor) throws GameActionException {
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
        }
        this.robot.addAnchor(anchor);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.BUILD_ANCHOR, anchor.getAccelerationIndex());
    } */

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
        if (getType() == RobotType.CARRIER){
            int totalWeight = this.getWeight();
            if (totalWeight == 0)
                throw new GameActionException(CANT_DO_THAT,
                    "Robot is a carrier but has no inventory to attack with");
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
        if (amount == 0) {
            throw new GameActionException(CANT_DO_THAT, "Don't transfer 0 resources. Do it again but this time with more");
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
        if (amount < -1)
            throw new GameActionException(CANT_DO_THAT,
                    "Cannot collect a negative amount of resource.");
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
    }

    /* private void assertCanPlaceAnchor() throws GameActionException {
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

    private void assertCanReturnAnchor(MapLocation loc) throws GameActionException{
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsActionReady();
        if (getType() != RobotType.CARRIER){
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot hold anchors.");
        }
        if (!isHeadquarter(loc)){
            throw new GameActionException(CANT_DO_THAT, 
                    "Can only return anchors back to headquarters.");
        }
        if (getTeam() != gameWorld.getRobot(loc).getTeam()){
            throw new GameActionException(CANT_DO_THAT, 
                    "Can only return anchors to the same team.");
        }
        if (!this.robot.getLocation().isAdjacentTo(loc)) {
            throw new GameActionException(CANT_DO_THAT,
                    "Robot needs to be adjacent to return.");
        }
        if (this.robot.getTypeAnchor() == null){
            throw new GameActionException(CANT_DO_THAT,"Robot needs to hold an anchor of specified type to return it.");
        }
    }

    @Override
    public boolean canReturnAnchor(MapLocation loc){
        try{
            assertCanReturnAnchor(loc);
            return true;
        }
        catch (GameActionException e){ return false; }
    } 

    @Override
    public void returnAnchor(MapLocation loc) throws GameActionException{
        assertCanReturnAnchor(loc);
        InternalRobot headquarters = this.gameWorld.getRobot(loc);
        Anchor anchor = this.getAnchor();
        headquarters.addAnchor(anchor);
        this.robot.releaseAnchor(anchor);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.PICK_UP_ANCHOR, -1*(headquarters.getID()*2 + anchor.getAccelerationIndex()) - 1);
    } */

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

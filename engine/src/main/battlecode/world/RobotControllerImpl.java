package battlecode.world;

import battlecode.common.*;

import static battlecode.common.GameActionExceptionType.*;
import battlecode.instrumenter.RobotDeathException;
import battlecode.schema.Action;

import java.util.*;

import org.apache.commons.lang3.NotImplementedException;


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
    public boolean checkHasAnchor() {
        return this.robot.holdingAnchor();  
    }

    private InternalRobot getRobotByID(int id) {
        if (!this.gameWorld.getObjectInfo().existsRobot(id))
            return null;
        return this.gameWorld.getObjectInfo().getRobotByID(id);
    }

    private int locationToInt(MapLocation loc) {
        return loc.x + loc.y * this.gameWorld.getGameMap().getWidth();
    }

    // ***********************************
    // ****** GENERAL VISION METHODS *****
    // ***********************************

    @Override
    public boolean onTheMap(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        if (!this.robot.canSenseLocation(loc))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target location not within vision range");
        return this.gameWorld.getGameMap().onTheMap(loc);
    }

    private void assertCanSenseLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        if (!this.robot.canSenseLocation(loc))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target location not within vision range");
        if (!this.gameWorld.getGameMap().onTheMap(loc))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target location is not on the map");
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
    public boolean canSenseRadiusSquared(int radiusSquared) {
        return this.robot.canSenseRadiusSquared(radiusSquared);
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
            // TODO: why do we need to do this?
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
        InternalRobot[] allSensedRobots = gameWorld.getAllRobotsWithinRadiusSquared(center, actualRadiusSquared);
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
        return island == null ? -1 : island.idx;
    }

    @Override
    public Map<Integer, MapLocation[]> senseNearbyIslandLocations() {
        try {
            return senseNearbyIslandLocations(-1);
        } catch (GameActionException e) {
            return new HashMap<Integer, MapLocation[]>();
        }
    }
    
    @Override
    public Map<Integer, MapLocation[]> senseNearbyIslandLocations(int radiusSquared) throws GameActionException {
        assertRadiusNonNegative(radiusSquared);
        return senseNearbyIslandLocations(getLocation(), radiusSquared);
    }

    @Override
    public Map<Integer, MapLocation[]> senseNearbyIslandLocations(MapLocation center, int radiusSquared) throws GameActionException {
        assertNotNull(center);
        assertRadiusNonNegative(radiusSquared);

        int actualRadiusSquared = radiusSquared == -1 ? getType().visionRadiusSquared : Math.min(radiusSquared, getType().visionRadiusSquared);

        Island[] allSensedIslands = gameWorld.getAllIslandsWithinRadiusSquared(center, actualRadiusSquared);

        Map<Integer, MapLocation[]> islandLocations = new HashMap<Integer, MapLocation[]>();
        for (Island island : allSensedIslands) {
            List<MapLocation> validLocations = Arrays.asList(island.locations);
            validLocations.removeIf(loc -> !canSenseLocation(loc));

            if (validLocations.isEmpty()) {
                continue;
            }

            islandLocations.put(island.idx, validLocations.toArray(new MapLocation[validLocations.size()]));
        }

        return islandLocations;
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
    public int senseTurnsLeftToTurn(int islandIdx) throws GameActionException {
        Island island = gameWorld.getIsland(islandIdx);
        if (island == null || !canSenseIsland(island)) {
            throw new GameActionException(CANT_SENSE_THAT, "Cannot sense an island with that id");
        }

        return island.turnsLeftToRemoveAnchor;
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
    public Well[] senseNearbyWells() {
        return senseNearbyWells(null);
    }

    @Override
    public Well[] senseNearbyWells(int radiusSquared) throws GameActionException {
        return senseNearbyWells(radiusSquared, null);
    }

    @Override
    public Well[] senseNearbyWells(MapLocation center, int radiusSquared) throws GameActionException {
        return senseNearbyWells(center, radiusSquared, null);
    }

    @Override
    public Well[] senseNearbyWells(ResourceType resourceType) {
        try {
            return senseNearbyWells(-1, resourceType);
        } catch (GameActionException e) {
            return new Well[0];
        }
    }

    @Override
    public Well[] senseNearbyWells(int radiusSquared, ResourceType resourceType) throws GameActionException {
        assertRadiusNonNegative(radiusSquared);
        return senseNearbyWells(getLocation(), radiusSquared, resourceType);
    }

    @Override
    public Well[] senseNearbyWells(MapLocation center, int radiusSquared, ResourceType resourceType) throws GameActionException {
        assertNotNull(center);
        assertRadiusNonNegative(radiusSquared);
        int actualRadiusSquared = radiusSquared == -1 ? getType().visionRadiusSquared : Math.min(radiusSquared, getType().visionRadiusSquared);

        // TODO update based on well implementation
        Well[] allSensedWells = gameWorld.getAllWellsWithinRadiusSquared(center, actualRadiusSquared);
        List<Well> validSensedWells = Arrays.asList(allSensedWells);
        validSensedWells.removeIf(well -> !canSenseLocation(well.getMapLocation()) ||
            (resourceType != null && well.getResourceType() != resourceType));
        List<Well> validSensedWellsCopy = new ArrayList<Well>();
        validSensedWells.forEach( (well) -> validSensedWellsCopy.add(well.copy()) );
        return validSensedWellsCopy.toArray(new Well[validSensedWellsCopy.size()]);
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
        return this.gameWorld.getAllLocationsWithinRadiusSquared(center, Math.min(radiusSquared, getType().visionRadiusSquared));
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
        MapLocation center = adjacentLocation(dir);
        this.gameWorld.moveRobot(getLocation(), center);
        this.robot.setLocation(center);
        // this has to happen after robot's location changed because rubble
        this.robot.addMovementCooldownTurns(getType().movementCooldown);
        this.gameWorld.getMatchMaker().addMoved(getID(), getLocation());
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

    @Override
    public boolean canBuildAnchor(Anchor anchor) {
        throw new NotImplementedException("Needs to be implemented");
        // TODO
    }

    @Override
    public void buildAnchor(Anchor anchor) {
        throw new NotImplementedException("Needs to be implemented");
        // TODO
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
        }
        if (!(bot == null) && bot.getTeam() == getTeam()) {
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is not on the enemy team.");
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
        InternalRobot bot = this.gameWorld.getRobot(loc);
        this.robot.attack(bot);
    }

    // ***********************************
    // ******** BOOSTERS METHODS *********
    // ***********************************

    @Override
    public boolean canBoost() {
        throw new NotImplementedException("Needs to be implemented");
        // TODO
    }

    @Override
    public void boost() {
        throw new NotImplementedException("Needs to be implemented");
        // TODO
    }

    // ***********************************
    // ****** DESTABILIZER METHODS *******
    // ***********************************

    @Override
    public boolean canDestabilize() {
        throw new NotImplementedException("Needs to be implemented");
        // TODO
    }

    @Override
    public void destabilize() {
        throw new NotImplementedException("Needs to be implemented");
        // TODO
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
        assertCanActLocation(loc);
        assertIsActionReady();

        if(getType() != RobotType.CARRIER)
            throw new GameActionException(CANT_DO_THAT, "This robot is not a carrier");
        if(amount > 0 && getResourceAmount(type) < amount) // Carrier is transfering to another location
            throw new GameActionException(CANT_DO_THAT, "Carrier does not have enough of that resource");
        if(amount < 0 && this.robot.canAdd(-1*amount)) // Carrier is picking up the resource from another location (probably headquarters)
            throw new GameActionException(CANT_DO_THAT, "Carrier does not have enough capacity to collect the resource");
        if(!isWell(loc) && !isHeadquarter(loc))
            throw new GameActionException(CANT_DO_THAT, "Cannot transfer to a location that is not a well or a headquarter");
    }

    @Override
    public boolean canTransferAd(MapLocation loc, int amount){
        try {
            assertCanTransferResource(loc, ResourceType.ADAMANTIUM, amount);
            return true;
        } catch(GameActionException e) {return false;}
    }

    @Override
    public boolean canTransferMn(MapLocation loc, int amount){
        try {
            assertCanTransferResource(loc, ResourceType.MANA, amount);
            return true;
        } catch(GameActionException e) {return false;}
    }

    @Override
    public boolean canTransferEx(MapLocation loc, int amount){
        try {
            assertCanTransferResource(loc, ResourceType.ELIXIR, amount);
            return true;
        } catch(GameActionException e) {return false;}
    }

    public void transferResource(ResourceType rType, MapLocation loc, int amount) throws GameActionException {
        assertCanTransferResource(loc, rType, amount);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        if(isWell(loc)){
            this.gameWorld.getWell(loc).addResourceAmount(rType, amount);
            this.robot.addResourceAmount(rType, -amount);
            this.gameWorld.getTeamInfo().addResource(rType, this.getTeam(), -1*amount);
        }
        else if(isHeadquarter(loc)){
            this.gameWorld.getRobot(loc).addResourceAmount(rType, amount);
        }
        this.gameWorld.getMatchMaker().addAction(getID(), Action.PLACE_RESOURCE, locationToInt(loc));
    }

    @Override
    public void transferAd(MapLocation loc, int amount) throws GameActionException {
        transferResource(ResourceType.ADAMANTIUM, loc, amount);
    }

    @Override
    public void transferMn(MapLocation loc, int amount) throws GameActionException {
        transferResource(ResourceType.MANA, loc, amount);
    }

    @Override
    public void transferEx(MapLocation loc, int amount) throws GameActionException {
        transferResource(ResourceType.ELIXIR, loc, amount);
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
        int rate = this.gameWorld.getWell(loc).isUpgraded() ? 4:2;
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
    
        // For methods below, Inventory class would have to first be implemented
        // --> Inventory would have methods such as canAdd() and add[ResourceName](amount)
        // Also assuming that ResourceType is a class tht returns an enum
        // --> Would check to see what resources a well holds

        ResourceType rType = gameWorld.getWell(loc).getResourceType();
        if (rType == ResourceType.NO_RESOURCE) {
            throw new IllegalArgumentException("Should not be a well with no resource");
        }
        int rate = this.gameWorld.getWell(loc).isUpgraded() ? 4:2;
        amount = amount == -1 ? rate : amount;
        this.robot.addResourceAmount(rType, amount);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.PICK_UP_RESOURCE, locationToInt(loc));
        this.gameWorld.getTeamInfo().addResource(rType, this.getTeam(), amount);
    }

    @Override
    public boolean canPlaceAnchor() {
        throw new NotImplementedException("Needs to be implemented");
        // TODO
    }

    @Override
    public void placeAnchor() {
        throw new NotImplementedException("Needs to be implemented");
        // TODO
    }

    @Override
    public boolean canTakeAnchor(MapLocation loc, Anchor anchor) {
        throw new NotImplementedException("Needs to be implemented");
        // TODO
    }

    @Override
    public void takeAnchor(MapLocation loc, Anchor anchor) {
        throw new NotImplementedException("Needs to be implemented");
        // TODO
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

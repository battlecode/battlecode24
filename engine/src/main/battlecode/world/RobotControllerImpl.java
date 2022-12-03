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

    @Override
    public int getArchonCount() {
        return this.gameWorld.getObjectInfo().getRobotTypeCount(getTeam(), RobotType.ARCHON);
    }

    @Override
    public int getTeamLeadAmount(Team team) {
        return this.gameWorld.getTeamInfo().getLead(team);
    }

    @Override
    public int getTeamGoldAmount(Team team) {
        return this.gameWorld.getTeamInfo().getGold(team);
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
    public RobotMode getMode() {
        return this.robot.getMode();
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
    public int getLevel() {
        return this.robot.getLevel();  
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

        return validSensedWells.toArray(new Well[validSensedWells.size()]);
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
        if (!this.robot.getMode().canAct)
            throw new GameActionException(CANT_DO_THAT,
                    "This robot is not in a mode that can act.");
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
        if (!this.robot.getMode().canMove)
            throw new GameActionException(CANT_DO_THAT,
                    "This robot is not in a mode that can move.");
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

    private void assertIsTransformReady() throws GameActionException {
        if (!this.robot.getMode().canTransform)
            throw new GameActionException(CANT_DO_THAT,
                    "This robot is not in a mode that can transform.");
        if (!this.robot.canTransformCooldown())
            throw new GameActionException(IS_NOT_READY,
                    "This robot's transform cooldown (either action or movement" +
                    "cooldown, depending on its current mode) has not expired.");
    }

    @Override
    public boolean isTransformReady() {
        try {
            assertIsTransformReady();
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public int getTransformCooldownTurns() throws GameActionException {
        if (!this.robot.getMode().canTransform)
            throw new GameActionException(CANT_DO_THAT,
                    "This robot is not in a mode that can transform.");
        return this.robot.getTransformCooldownTurns();
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
                    "Robot is of type " + getType() + " which cannot build robots of type " + type + ".");

        Team team = getTeam();
        if (this.gameWorld.getTeamInfo().getLead(team) < type.buildCostLead)
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "Insufficient amount of lead.");
        if (this.gameWorld.getTeamInfo().getGold(team) < type.buildCostGold)
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "Insufficient amount of gold.");

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

    @Override
    public void buildRobot(RobotType type, Direction dir) throws GameActionException {
        assertCanBuildRobot(type, dir);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        Team team = getTeam();
        this.gameWorld.getTeamInfo().addLead(team, -type.buildCostLead);
        this.gameWorld.getTeamInfo().addGold(team, -type.buildCostGold);
        int newId = this.gameWorld.spawnRobot(type, adjacentLocation(dir), team);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.SPAWN_UNIT, newId);
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
        if (!(bot == null) && bot.getTeam() == getTeam())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is not on the enemy team.");
        
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

    // *****************************
    // ******** SAGE METHODS ******* 
    // *****************************

    private void assertCanEnvision(AnomalyType anomaly) throws GameActionException {
        assertNotNull(anomaly);
        assertIsActionReady();
        if (!getType().canEnvision())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot envision.");
        if (!anomaly.isSageAnomaly)
            throw new GameActionException(CANT_DO_THAT,
                    "Sage can not use anomaly of type " + anomaly);
    }

    @Override
    public boolean canEnvision(AnomalyType anomaly) {
        try {
            assertCanEnvision(anomaly);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void envision(AnomalyType anomaly) throws GameActionException {
        assertCanEnvision(anomaly);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        switch (anomaly) {
            case ABYSS:
                this.gameWorld.causeAbyssSage(this.robot);
                this.gameWorld.getMatchMaker().addAction(getID(), Action.LOCAL_ABYSS, locationToInt(getLocation()));
                break;
            case CHARGE:
                this.gameWorld.causeChargeSage(this.robot);
                this.gameWorld.getMatchMaker().addAction(getID(), Action.LOCAL_CHARGE, locationToInt(getLocation()));
                break;
            case FURY:
                this.gameWorld.causeFurySage(this.robot);
                this.gameWorld.getMatchMaker().addAction(getID(), Action.LOCAL_FURY, locationToInt(getLocation()));
                break;
        }
    }

    // *****************************
    // ****** REPAIR METHODS *******
    // *****************************

    private void assertCanRepair(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsActionReady();
        InternalRobot bot = this.gameWorld.getRobot(loc);
        if (bot == null)
            throw new GameActionException(NO_ROBOT_THERE,
                    "There is no robot to repair at the target location.");
        if (!getType().canRepair(bot.getType()))
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot repair robots of type " + bot.getType() + ".");
        if (bot.getTeam() != getTeam())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is not on your team so can't be repaired.");
    }

    @Override
    public boolean canRepair(MapLocation loc) {
        try {
            assertCanRepair(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void repair(MapLocation loc) throws GameActionException {
        assertCanRepair(loc);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        InternalRobot bot = this.gameWorld.getRobot(loc);
        this.robot.heal(bot);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.REPAIR, bot.getID());
    }

    // ***********************
    // **** MINER METHODS **** 
    // ***********************

    private boolean isWell(MapLocation loc) {
        //TODO checks if the location is a well
    }

    private boolean isHeadquarter(MapLocation loc){
        //TODO checks if the location is a headquarter
    }

    private void assertCanTransferResource(MapLocation loc, ResourceType type, int amount) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsActionReady();

        if(getType() != RobotType.CARRIER)
            throw new GameActionException(CANT_DO_THAT, "This robot is not a carrier");
        if(amount > 0 && this.robot.getResource(type) < amount) // Carrier is transfering to another location
            throw new GameActionException(CANT_DO_THAT, "Carrier does not have enough of that resource");
        if(amount < 0 && this.robot.getInventory.canAdd(-1*amount)) // Carrier is picking up the resource from another location (probably headquarters)
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
    public void transferAd(MapLocation loc, int amount) throws GameActionException {
        assertCanTransferResource(loc, ResourceType.ADAMANTIUM, amount);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        Inventory robotInv = this.robot.getInventory();
        if(isWell(loc)){
            Well well = this.gameWorld.getWell(loc);
            well.addAdamantium(amount);
            robotInv.addAdamantium(-amount);
        }
        else if(isHeadquarter(loc)){
            Inventory headquarterInv = this.gameWorld.getHeadquarter(loc).getInventory();
            headquarterInv.addAdamantium(amount);
        }
        this.gameWorld.getMatchMaker().addAction(getID(), Action.MINE_LEAD, locationToInt(loc));
        //TODO update addAction once we have new action types!
    }

    @Override
    public boolean canTransferMn(MapLocation loc, int amount){
        try {
            assertCanTransferResource(loc, ResourceType.MANA, amount);
            return true;
        } catch(GameActionException e) {return false;}
    }

    @Override
    public void transferMn(MapLocation loc, int amount) throws GameActionException {
        assertCanTransferResource(loc, ResourceType.MANA, amount);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        Inventory robotInv = this.robot.getInventory();
        if(isWell(loc)){
            Well well = this.gameWorld.getWell(loc);
            well.addMana(amount);
            robotInv.addMana(-amount);
        }
        else if(isHeadquarter(loc)){
            Inventory headquarterInv = this.gameWorld.getHeadquarter(loc).getInventory();
            headquarterInv.addMana(amount);
        }
        this.gameWorld.getMatchMaker().addAction(getID(), Action.MINE_LEAD, locationToInt(loc));
        //TODO update addAction once we have new action types!
    }

    @Override
    public boolean canTransferEx(MapLocation loc, int amount){
        try {
            assertCanTransferResource(loc, ResourceType.ELIXIR, amount);
            return true;
        } catch(GameActionException e) {return false;}
    }

    @Override
    public void transferEx(MapLocation loc, int amount) throws GameActionException {
        assertCanTransferResource(loc, ResourceType.ELIXIR, amount);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        Inventory robotInv = this.robot.getInventory();
        if(isWell(loc)){
            Well well = this.gameWorld.getWell(loc);
            well.addElixir(amount);
            robotInv.addElixir(-amount);
        }
        else if(isHeadquarter(loc)){
            Inventory headquarterInv = this.gameWorld.getHeadquarter(loc).getInventory();
            headquarterInv.addElixir(amount);
        }
        this.gameWorld.getMatchMaker().addAction(getID(), Action.MINE_LEAD, locationToInt(loc));
        //TODO update addAction once we have new action types!
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
        int rate = this.gameWorld.getWell(loc).isUpgraded() ? 2:4;
        if (amount > rate)
            throw new GameActionException(CANT_DO_THAT, 
                    "Amount is higher than rate");
        if (!this.robot.getInventory().canAdd(amount))
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

        Inventory robotInv = this.robot.getInventory();

        if (gameWorld.getWell(loc).getResourceType() == ResourceType.ELIXIR)
            robotInv.addElixir(amount);
        else if (gameWorld.getWell(loc).getResourceType() == ResourceType.MANA)
            robotInv.addMana(amount);
        else
            robotInv.addAdamantium(amount);
    
        // Will need to update this last line
        this.gameWorld.getMatchMaker().addAction(getID(), Action.MINE_GOLD, locationToInt(loc));
    }

    // *************************
    // **** MUTATE METHODS **** 
    // *************************

    private void assertCanMutate(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsActionReady();
        InternalRobot bot = this.gameWorld.getRobot(loc);
        if (bot == null)
            throw new GameActionException(NO_ROBOT_THERE,
                    "There is no robot to mutate at the target location.");
        if (!getType().canMutate(bot.getType()))
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot mutate robots of type " + bot.getType() + ".");
        if (bot.getTeam() != getTeam())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is not on your team so can't be mutated.");
        if (!bot.canMutate())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is either not in a mutable mode, or already at max level.");
        if (gameWorld.getTeamInfo().getLead(getTeam()) < bot.getLeadMutateCost())
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "You don't have enough lead to mutate this robot.");
        if (gameWorld.getTeamInfo().getGold(getTeam()) < bot.getGoldMutateCost())
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "You don't have enough gold to mutate this robot.");
    }

    @Override
    public boolean canMutate(MapLocation loc) {
        try {
            assertCanMutate(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void mutate(MapLocation loc) throws GameActionException {
        assertCanMutate(loc);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        Team team = getTeam();
        InternalRobot bot = this.gameWorld.getRobot(loc);
        int leadNeeded = bot.getLeadMutateCost();
        int goldNeeded = bot.getGoldMutateCost();
        this.gameWorld.getTeamInfo().addLead(team, -leadNeeded);
        this.gameWorld.getTeamInfo().addGold(team, -goldNeeded);
        bot.mutate();
        bot.addActionCooldownTurns(GameConstants.MUTATE_COOLDOWN);
        bot.addMovementCooldownTurns(GameConstants.MUTATE_COOLDOWN);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.MUTATE, bot.getID());
    }

    // ***************************
    // **** TRANSMUTE METHODS **** 
    // ***************************

    @Override
    public int getTransmutationRate() {
        if (getType() != RobotType.LABORATORY) {
            return 0;
        }
        return getTransmutationRate(getLevel());
    }

    @Override
    public int getTransmutationRate(int laboratory_level) {
        final double alchemist_loneliness_k;
        switch (laboratory_level) {
            case 1: alchemist_loneliness_k = GameConstants.ALCHEMIST_LONELINESS_K_L1; break;
            case 2: alchemist_loneliness_k = GameConstants.ALCHEMIST_LONELINESS_K_L2; break;
            case 3: alchemist_loneliness_k = GameConstants.ALCHEMIST_LONELINESS_K_L3; break;
            default: return 0;
        }
        return (int) (GameConstants.ALCHEMIST_LONELINESS_A - GameConstants.ALCHEMIST_LONELINESS_B *
                      Math.exp(-alchemist_loneliness_k * this.robot.getNumVisibleFriendlyRobots(true)));
    }

    private void assertCanTransmute() throws GameActionException {
        assertIsActionReady();
        if (!getType().canTransmute())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot transmute lead to gold.");
        if (this.gameWorld.getTeamInfo().getLead(getTeam()) < getTransmutationRate())
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "You don't have enough lead to transmute to gold.");
    }

    @Override
    public boolean canTransmute() {
        try {
            assertCanTransmute();
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void transmute() throws GameActionException {
        assertCanTransmute();
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        Team team = getTeam();
        this.gameWorld.getTeamInfo().addLead(team, -getTransmutationRate());
        this.gameWorld.getTeamInfo().addGold(team, 1);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.TRANSMUTE, -1);
    }

    // ***************************
    // **** TRANSFORM METHODS **** 
    // ***************************

    private void assertCanTransform() throws GameActionException {
        assertIsTransformReady();
    }

    @Override
    public boolean canTransform() {
        try {
            assertCanTransform();
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void transform() throws GameActionException {
        assertCanTransform();
        this.robot.transform();
        if (this.robot.getMode() == RobotMode.TURRET)
            this.robot.addActionCooldownTurns(GameConstants.TRANSFORM_COOLDOWN);
        else
            this.robot.addMovementCooldownTurns(GameConstants.TRANSFORM_COOLDOWN);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.TRANSFORM, -1);
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
        assertCanWriteSharedArray();
        this.gameWorld.getTeamInfo().writeSharedArray(getTeam(), index, value);
    }



    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    @Override
    public AnomalyScheduleEntry[] getAnomalySchedule() {
        return this.gameWorld.getAnomalySchedule();
    }

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

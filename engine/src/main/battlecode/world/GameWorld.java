package battlecode.world;

import battlecode.common.*;
import battlecode.instrumenter.profiler.ProfilerCollection;
import battlecode.server.ErrorReporter;
import battlecode.server.GameMaker;
import battlecode.server.GameState;
import battlecode.world.control.RobotControlProvider;
import battlecode.world.robots.InternalCarrier;

import java.util.*;

/**
 * The primary implementation of the GameWorld interface for containing and
 * modifying the game map and the objects on it.
 */
public strictfp class GameWorld {
    /**
     * The current round we're running.
     */
    protected int currentRound;

    /**
     * Whether we're running.
     */
    protected boolean running = true;

    protected final IDGenerator idGenerator;
    protected final GameStats gameStats;
    private boolean[] walls;
    private boolean[] clouds;
    private boolean[] water;
    private boolean[] dams;
    private int[] spawnZones; // Team A = 0, Team B = 1, not spawn zone = -1
    private int[] breadAmounts;
    private ArrayList<Trap>[] trapTriggers;
    private Trap[] trapLocations;
    private ArrayList<Integer>[][][] boosts;
    private double[][] cooldownMultipliers;
    private InternalRobot[][] robots;
    private int[] islandIds;
    private HashMap<Integer, Island> islandIdToIsland;
    private final LiveMap gameMap;
    private final TeamInfo teamInfo;
    private final ObjectInfo objectInfo;
    //list of currents, center direction if there is no current in the tile
    private Direction[] currents;
    
    //List of all flags, not indexed by location
    private ArrayList<Flag> allFlags;
    //List of flags on each tile, indexed by location
    private ArrayList<Flag>[] placedFlags;

    private static final int BOOST_INDEX = 0;
    private static final int DESTABILIZE_INDEX = 1;
    private static final int ANCHOR_INDEX = 2;

    private Well[] wells;

    private Map<Team, ProfilerCollection> profilerCollections;

    private final RobotControlProvider controlProvider;
    private Random rand;
    private final GameMaker.MatchMaker matchMaker;

    @SuppressWarnings("unchecked")
    public GameWorld(LiveMap gm, RobotControlProvider cp, GameMaker.MatchMaker matchMaker) {
        this.walls = gm.getWallArray();
        this.clouds = gm.getCloudArray();
        this.water = gm.getWaterArray();
        this.spawnZones = gm.getSpawnZoneArray();
        this.dams = gm.getDamArray();
        this.breadAmounts = gm.getBreadArray();
        this.islandIds = gm.getIslandArray();
        this.robots = new InternalRobot[gm.getWidth()][gm.getHeight()]; // if represented in cartesian, should be height-width, but this should allow us to index x-y
        this.currents = new Direction[gm.getWidth() * gm.getHeight()];
        this.currentRound = 0;
        this.idGenerator = new IDGenerator(gm.getSeed());
        this.gameStats = new GameStats();
        this.gameMap = gm;
        this.objectInfo = new ObjectInfo(gm);

        /* //Initialize currents
        int[] gmCurrents = gm.getCurrentArray();
        Arrays.fill(this.currents, Direction.CENTER);
        for(int i = 0; i < currents.length; i++) {
            this.currents[i] = Direction.DIRECTION_ORDER[gmCurrents[i]];
        } */
        this.profilerCollections = new HashMap<>();

        this.controlProvider = cp;
        this.rand = new Random(this.gameMap.getSeed());
        this.matchMaker = matchMaker;

        this.controlProvider.matchStarted(this);

        this.teamInfo = new TeamInfo(this);

        // Create all robots in their despawned states
        for (int i = 0; i < GameConstants.ROBOT_CAPACITY; i++) {
            createRobot(Team.A);
            createRobot(Team.B);
        }

        /* this.islandIdToIsland = new HashMap<>();
        HashMap<Integer, List<MapLocation>> islandIdToLocations = new HashMap<>();
        // Populate idToIsland map
        for (int idx = 0; idx < islandIds.length; idx++) {
            int islandId = islandIds[idx];
            // Assume islandId 0 is not a real island and all other islands are actual islands
            if (islandId != 0) {
                List<MapLocation> prevLocations = islandIdToLocations.getOrDefault(islandId, new ArrayList<MapLocation>());
                prevLocations.add(this.indexToLocation(idx));
                islandIdToLocations.put(islandId, prevLocations);
            }
        }
        for (int key : islandIdToLocations.keySet()) {
            Island newIsland = new Island(this, key, islandIdToLocations.get(key));
            this.islandIdToIsland.put(key, newIsland);            
        } */

        // Write match header at beginning of match
        this.matchMaker.makeMatchHeader(this.gameMap);
        
        /* this.wells = new Well[gm.getWidth()*gm.getHeight()];
        for(int i = 0; i < gm.getResourceArray().length; i++){
            MapLocation loc = indexToLocation(i);
            ResourceType rType = ResourceType.values()[gm.getResourceArray()[i]];
            if (rType == ResourceType.NO_RESOURCE) {
                this.wells[i] = null;
            } else {
                this.wells[i] = new Well(loc, rType);
            }
        } */

        this.trapTriggers = new ArrayList[gm.getWidth()*gm.getHeight()];
        for (int i = 0; i < trapTriggers.length; i++){
            this.trapTriggers[i] = new ArrayList<Trap>();
        }


        //initialize flags
        this.allFlags = new ArrayList<Flag>();
        this.placedFlags = new ArrayList[gm.getWidth() * gm.getHeight()];

        for (int i = 0; i < placedFlags.length; i++)
            placedFlags[i] = new ArrayList<>();
        
        for (int i = 0; i < gm.getFlagArray().length; i++) {
            int flagVal = gm.getFlagArray()[i];
            if(flagVal == 0) continue;
            Flag flag = new Flag(flagVal == 1 ? Team.A : Team.B, indexToLocation(i));
            allFlags.add(flag);
            placedFlags[i].add(flag);
        }
      
        this.boosts = new ArrayList[gm.getWidth()*gm.getHeight()][2][3];
        for (int i = 0; i < boosts.length; i++){ 
            for (int j = 0; j < boosts[0].length; j++)
                for (int k = 0; k < boosts[0][0].length; k++)
                    this.boosts[i][j][k] = new ArrayList<Integer>();
        }
        this.cooldownMultipliers = new double[gm.getWidth()*gm.getHeight()][2];
        for (int i = 0; i < gm.getHeight()*gm.getWidth(); i++){
            cooldownMultipliers[i][0] = 1.0;
            cooldownMultipliers[i][1] = 1.0;
        }
        for (MapLocation loc : getAllLocations()) {
            if (getCloud(loc)){
                cooldownMultipliers[locationToIndex(loc)][0] += GameConstants.CLOUD_MULTIPLIER; 
                cooldownMultipliers[locationToIndex(loc)][1] += GameConstants.CLOUD_MULTIPLIER;
                cooldownMultipliers[locationToIndex(loc)][0] = Math.round(cooldownMultipliers[locationToIndex(loc)][0] * 100.0)/100.0;
                cooldownMultipliers[locationToIndex(loc)][1] = Math.round(cooldownMultipliers[locationToIndex(loc)][1] * 100.0)/100.0; 
            }
        } */
    }

    /**
     * Run a single round of the game.
     *
     * @return the state of the game after the round has run
     */
    public synchronized GameState runRound() {
        if (!this.isRunning()) {
            List<ProfilerCollection> profilers = new ArrayList<>(2);
            if (!profilerCollections.isEmpty()) {
                profilers.add(profilerCollections.get(Team.A));
                profilers.add(profilerCollections.get(Team.B));
            }

            // Write match footer if game is done
            matchMaker.makeMatchFooter(gameStats.getWinner(), currentRound, profilers);
            return GameState.DONE;
        }

        try {
            this.processBeginningOfRound();
            this.controlProvider.roundStarted();
            // On the first round we want to add the initial amounts to the headquarters
            if (this.currentRound == 1) {
                this.teamInfo.addBread(Team.A, GameConstants.INITIAL_BREAD_AMOUNT);
                this.teamInfo.addBread(Team.B, GameConstants.INITIAL_BREAD_AMOUNT);
            }

            updateDynamicBodies();

            this.controlProvider.roundEnded();
            if (this.currentRound % GameConstants.PASSIVE_INCREASE_ROUNDS == 0){
                this.teamInfo.addBread(Team.A, GameConstants.PASSIVE_BREAD_INCREASE);
                this.teamInfo.addBread(Team.B, GameConstants.PASSIVE_BREAD_INCREASE);
            }
            this.processEndOfRound();

            if (!this.isRunning()) {
                this.controlProvider.matchEnded();
            }

        } catch (Exception e) {
            ErrorReporter.report(e);
            // TODO throw out file?
            return GameState.DONE;
        }
        // Write out round data
        matchMaker.makeRound(currentRound);
        return GameState.RUNNING;
    }

    private void updateDynamicBodies() {
        objectInfo.eachDynamicBodyByExecOrder((body) -> {
            if (body instanceof InternalRobot) {
                return updateRobot((InternalRobot) body);
            } else {
                throw new RuntimeException("non-robot body registered as dynamic");
            }
        });
    }

    private boolean updateRobot(InternalRobot robot) {
        robot.processBeginningOfTurn();
        this.controlProvider.runRobot(robot);
        robot.setBytecodesUsed(this.controlProvider.getBytecodesUsed(robot));
        robot.processEndOfTurn();

        // If the robot terminates but the death signal has not yet
        // been visited:

        // NOTE: changed this from destroy to despawn; double check that this change is correct
        if (this.controlProvider.getTerminated(robot) && objectInfo.getRobotByID(robot.getID()) != null)
            despawnRobot(robot.getID());
        return true;
    }

    // *********************************
    // ****** BASIC MAP METHODS ********
    // *********************************

    public int getMapSeed() {
        return this.gameMap.getSeed();
    }

    public LiveMap getGameMap() {
        return this.gameMap;
    }

    public TeamInfo getTeamInfo() {
        return this.teamInfo;
    }

    public GameStats getGameStats() {
        return this.gameStats;
    }

    public ObjectInfo getObjectInfo() {
        return this.objectInfo;
    }

    public GameMaker.MatchMaker getMatchMaker() {
        return this.matchMaker;
    }

    public Team getWinner() {
        return this.gameStats.getWinner();
    }

    public boolean isRunning() {
        return this.running;
    }

    public int getCurrentRound() {
        return this.currentRound;
    }

    public boolean getWall(MapLocation loc) {
        return this.walls[locationToIndex(loc)];
    }

    public boolean getCloud(MapLocation loc) {
        int idx = locationToIndex(loc);
        if (idx < 0 || idx >= this.clouds.length) {
            return false;
        }
        return this.clouds[idx];
    }

    public boolean getWater(MapLocation loc) {
        return this.water[locationToIndex(loc)];
    }

    public void setWater(MapLocation loc) {
        this.water[locationToIndex(loc)] = true;
    }

    public void setLand(MapLocation loc) {
        this.water[locationToIndex(loc)] = false;
    }

    public int getBreadAmount(MapLocation loc) {
        return this.breadAmounts[locationToIndex(loc)];
    }

    public void removeBread(MapLocation loc) {
        this.breadAmounts[locationToIndex(loc)] = 0;
    }

    /**
     * Checks if a given location is a spawn zone.
     * Returns -1 if not, 0 if it is a Team A spawn zone,
     * and 1 if it is a Team B spawn zone.
     * 
     * @param loc the location to check
     * @return -1 if the location is not a spawn zone,
     * 0 or 1 if it is a Team A or Team B spawn zone respectively
     */
    public int getSpawnZone(MapLocation loc) {
        return this.spawnZones[locationToIndex(loc)];
    }

    public Direction getCurrent(MapLocation loc) {
        return this.currents[locationToIndex(loc)];
    }

    public boolean isPassable(MapLocation loc) {
        if (currentRound <= GameConstants.SETUP_ROUNDS){
            return !this.walls[locationToIndex(loc)] && !this.water[locationToIndex(loc)] && !this.dams[locationToIndex(loc)];
        }
        return !this.walls[locationToIndex(loc)] && !this.water[locationToIndex(loc)];
    }

    public Well getWell(MapLocation loc) {
        return wells[locationToIndex(loc)];
    }

    public Island getIsland(MapLocation loc) {
        return islandIdToIsland.get(this.islandIds[locationToIndex(loc)]);
    }

    public Island getIsland(int islandIdx) {
        if (islandIdToIsland.containsKey(islandIdx)) {
            return islandIdToIsland.get(islandIdx);
        } else {
            return null;
        }
    }

    public ArrayList<Flag> getAllFlags() {
        return allFlags;
    }

    public void addFlag(MapLocation loc, Flag flag) {
        placedFlags[locationToIndex(loc)].add(flag);
        flag.setLoc(loc);
    }

    public ArrayList<Flag> getFlags(MapLocation loc) {
        return placedFlags[locationToIndex(loc)];
    }

    public void removeFlag(MapLocation loc, Flag flag){
        placedFlags[locationToIndex(loc)].remove(flag);
    }

    public boolean hasFlag(MapLocation loc) {
        return placedFlags[locationToIndex(loc)].size() > 0;
    }

    /**
     * Helper method that converts a location into an index.
     * 
     * @param loc the MapLocation
     */
    public int locationToIndex(MapLocation loc) {
        return this.gameMap.locationToIndex(loc);
    }

    /**
     * Helper method that converts an index into a location.
     * 
     * @param idx the index
     */
    public MapLocation indexToLocation(int idx) {
        return gameMap.indexToLocation(idx);
    }

    // ***********************************
    // ****** DAM METHODS **************
    // ***********************************

    public boolean getDam(MapLocation loc){
        if (currentRound <= GameConstants.SETUP_ROUNDS){
            return dams[locationToIndex(loc)];
        }
        else {
            return false;
        }
    }

    // ***********************************
    // ****** TRAP METHODS **************
    // ***********************************
    
    public TrapType getTrapType(MapLocation loc) {
        return this.trapLocations[locationToIndex(loc)].getType();
    }

    public boolean hasTrap(MapLocation loc){
        return !(this.trapLocations[locationToIndex(loc)] == null);
    }

    public void placeTrap(MapLocation loc, Trap trap){
        this.trapLocations[locationToIndex(loc)] = trap;
        //should we be able to trigger traps we are diagonally next to?
        for (MapLocation adjLoc : getAllLocationsWithinRadiusSquared(loc, trap.getType().triggerRadius)){
            this.trapTriggers[locationToIndex(adjLoc)].add(trap);
        }
    }

    public void triggerTrap(Trap trap, boolean entered){
        MapLocation loc = trap.getLocation();
        TrapType type = trap.getType();
        switch(type){
            case STUN:
                for (InternalRobot rob : getAllRobotsWithinRadiusSquared(loc, type.enterRadius, trap.getTeam().opponent())){
                    rob.setMovementCooldownTurns(40);
                    rob.setActionCooldownTurns(40);
                }
                break;
            case EXPLOSIVE:
                int rad = type.interactRadius;
                int dmg = type.enterDamage;
                if (entered){
                    rad = type.enterRadius;
                    dmg = type.enterDamage;
                }
                for (InternalRobot rob : getAllRobotsWithinRadiusSquared(loc, rad, trap.getTeam().opponent())){
                    rob.addHealth(-1*dmg);
                }
                break;
            case WATER:
                for (MapLocation adjLoc : getAllLocationsWithinRadiusSquared(loc, type.enterRadius)){
                    if (getRobot(adjLoc) != null || !isPassable(adjLoc))
                        continue;
                    setWater(adjLoc);
                }
                break;
        }
        for (MapLocation adjLoc : getAllLocationsWithinRadiusSquared(loc, 2)){
            this.trapTriggers[locationToIndex(adjLoc)].remove(trap);
        }
        this.trapLocations[locationToIndex(loc)] = null;
    }

    public void addBoost(MapLocation center, Team team){
        int lastRound = getCurrentRound() + GameConstants.BOOSTER_DURATION;
        int radiusSquared = GameConstants.BOOSTER_RADIUS_SQUARED;
        for (MapLocation loc : getAllLocationsWithinRadiusSquared(center, radiusSquared)){
            ArrayList<Integer> curBoostsList = this.boosts[locationToIndex(loc)][team.ordinal()][BOOST_INDEX];
            //no other boosts at this location
            if (curBoostsList.size() < GameConstants.MAX_BOOST_STACKS) {
                cooldownMultipliers[locationToIndex(loc)][team.ordinal()] += GameConstants.BOOSTER_MULTIPLIER;
                cooldownMultipliers[locationToIndex(loc)][team.ordinal()] = Math.round(cooldownMultipliers[locationToIndex(loc)][team.ordinal()] * 100.0)/100.0;
            }
            curBoostsList.add(lastRound);
        }
    }
    
   public int getOldestBoost(MapLocation loc, Team team){
        ArrayList<Integer> curBoosts = this.boosts[locationToIndex(loc)][team.ordinal()][BOOST_INDEX];
        return curBoosts.size() == 0 ? -1 : curBoosts.get(0);
    }

    public int getNumActiveBoosts(MapLocation loc, Team team) {
        ArrayList<Integer> curBoosts = this.boosts[locationToIndex(loc)][team.ordinal()][BOOST_INDEX];
        return curBoosts.size();
    }

    public void addDestabilize(MapLocation center, Team team){ //team of the destabilizer robot
        int lastRound = getCurrentRound() + GameConstants.DESTABILIZER_DURATION;
        int radiusSquared = GameConstants.DESTABILIZER_RADIUS_SQUARED;
        for (MapLocation loc : getAllLocationsWithinRadiusSquared(center, radiusSquared)){
            ArrayList<Integer> curDestabilizers = this.boosts[locationToIndex(loc)][team.opponent().ordinal()][DESTABILIZE_INDEX];
            if (curDestabilizers.size() < GameConstants.MAX_DESTABILIZE_STACKS) {
                cooldownMultipliers[locationToIndex(loc)][team.opponent().ordinal()] += GameConstants.DESTABILIZER_MULTIPLIER;
                cooldownMultipliers[locationToIndex(loc)][team.opponent().ordinal()] = Math.round(cooldownMultipliers[locationToIndex(loc)][team.opponent().ordinal()] * 100.0)/100.0;
            }
            curDestabilizers.add(lastRound);
        }
    }

    public int getOldestDestabilize(MapLocation loc, Team team){
        ArrayList<Integer> curDestabilizers = this.boosts[locationToIndex(loc)][team.ordinal()][DESTABILIZE_INDEX];
        return curDestabilizers.size() == 0 ? -1 : curDestabilizers.get(0);
    }

    public int getNumActiveDestabilize(MapLocation loc, Team team) {
        ArrayList<Integer> curDestabilizers = this.boosts[locationToIndex(loc)][team.ordinal()][DESTABILIZE_INDEX];
        return curDestabilizers.size();
    }
    
    public void addBoostFromAnchor(Island island){
        if(island.getAnchor() != Anchor.ACCELERATING) {
            throw new InternalError("Anchor should be accelerating");
        }
        int teamOrdinal = island.getTeam().ordinal(); 
        for (MapLocation loc : island.getLocsAffected()){
            ArrayList<Integer> curAnchorList = this.boosts[locationToIndex(loc)][teamOrdinal][ANCHOR_INDEX];
            if (curAnchorList.size() < GameConstants.MAX_ANCHOR_STACKS){
                cooldownMultipliers[locationToIndex(loc)][teamOrdinal] += GameConstants.ANCHOR_MULTIPLIER;
                cooldownMultipliers[locationToIndex(loc)][teamOrdinal] = Math.round(cooldownMultipliers[locationToIndex(loc)][teamOrdinal] * 100.0)/100.0;
            }
            curAnchorList.add(island.getID());
        }
    }
    
    public void removeBoostFromAnchor(Island island){
        int teamOrdinal = island.getTeam().ordinal();
        Integer boostIdentifier = new Integer(island.getID());
        for (MapLocation loc : island.getLocsAffected()){
            ArrayList<Integer> curAnchorList = this.boosts[locationToIndex(loc)][teamOrdinal][ANCHOR_INDEX];
            if (curAnchorList.size() <= GameConstants.MAX_ANCHOR_STACKS){
                cooldownMultipliers[locationToIndex(loc)][teamOrdinal] -= GameConstants.ANCHOR_MULTIPLIER;  
                cooldownMultipliers[locationToIndex(loc)][teamOrdinal] = Math.round(cooldownMultipliers[locationToIndex(loc)][teamOrdinal] * 100.0)/100.0;  
            }
            curAnchorList.remove(boostIdentifier);
        }
    }

    // ***********************************
    // ****** ROBOT METHODS **************
    // ***********************************

    public InternalRobot getRobot(MapLocation loc) {
        return this.robots[loc.x - this.gameMap.getOrigin().x][loc.y - this.gameMap.getOrigin().y];
    }

    public void moveRobot(MapLocation start, MapLocation end) {
        addRobot(end, getRobot(start));
        removeRobot(start);
    }

    public void addRobot(MapLocation loc, InternalRobot robot) {
        this.robots[loc.x - this.gameMap.getOrigin().x][loc.y - this.gameMap.getOrigin().y] = robot;
    }

    public void removeRobot(MapLocation loc) {
        this.robots[loc.x - this.gameMap.getOrigin().x][loc.y - this.gameMap.getOrigin().y] = null;
    }

    public InternalRobot[] getAllRobotsWithinRadiusSquared(MapLocation center, int radiusSquared) {
        return getAllRobotsWithinRadiusSquared(center, radiusSquared, null);
    }

    public InternalRobot[] getAllRobotsWithinRadiusSquared(MapLocation center, int radiusSquared, Team team) {
        ArrayList<InternalRobot> returnRobots = new ArrayList<InternalRobot>();
        for (MapLocation newLocation : getAllLocationsWithinRadiusSquared(center, radiusSquared))
            if (getRobot(newLocation) != null) {
                if (team == null || getRobot(newLocation).getTeam() == team)
                    returnRobots.add(getRobot(newLocation));
            }
        return returnRobots.toArray(new InternalRobot[returnRobots.size()]);
    }

    public Island[] getAllIslandsWithinVision(InternalRobot robot, int radiusSquared) {
        ArrayList<Island> returnIslands = new ArrayList<Island>();
        for (MapLocation newLocation : getAllLocationsWithinRadiusSquared(robot.getLocation(), radiusSquared))
            if (robot.canSenseLocation(newLocation) && getIsland(newLocation) != null)
                returnIslands.add(getIsland(newLocation));
        return returnIslands.toArray(new Island[returnIslands.size()]);
    }

    public Well[] getAllWellsWithinRadiusSquared(MapLocation center, int radiusSquared) {
        ArrayList<Well> returnWells = new ArrayList<Well>();
        for (MapLocation newLocation : getAllLocationsWithinRadiusSquared(center, radiusSquared))
            if (getWell(newLocation) != null)
                returnWells.add(getWell(newLocation));
        return returnWells.toArray(new Well[returnWells.size()]);
    }

    public Flag[] getAllFlagsWithinRadiusSquared(MapLocation center, int radiusSquared) {
        ArrayList<Flag> returnFlags = new ArrayList<Flag>();
        for (MapLocation newLocation : getAllLocationsWithinRadiusSquared(center, radiusSquared))
            if (getFlags(newLocation) != null)
                returnFlags.addAll(getFlags(newLocation));
        return returnFlags.toArray(new Flag[returnFlags.size()]);
    }

    public MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared) {
        return getAllLocationsWithinRadiusSquaredWithoutMap(
            this.gameMap.getOrigin(),
            this.gameMap.getWidth(),
            this.gameMap.getHeight(),
            center, radiusSquared
        );
    }

    public static MapLocation[] getAllLocationsWithinRadiusSquaredWithoutMap(MapLocation origin,
                                                                            int width, int height,
                                                                            MapLocation center, int radiusSquared) {
        ArrayList<MapLocation> returnLocations = new ArrayList<MapLocation>();
        int ceiledRadius = (int) Math.ceil(Math.sqrt(radiusSquared)) + 1; // add +1 just to be safe
        int minX = Math.max(center.x - ceiledRadius, origin.x);
        int minY = Math.max(center.y - ceiledRadius, origin.y);
        int maxX = Math.min(center.x + ceiledRadius, origin.x + width - 1);
        int maxY = Math.min(center.y + ceiledRadius, origin.y + height - 1);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                MapLocation newLocation = new MapLocation(x, y);
                if (center.isWithinDistanceSquared(newLocation, radiusSquared))
                    returnLocations.add(newLocation);
            }
        }
        return returnLocations.toArray(new MapLocation[returnLocations.size()]);
    }

    /**
     * @return all of the locations on the grid
     */
    private MapLocation[] getAllLocations() {
        return getAllLocationsWithinRadiusSquared(new MapLocation(0, 0), Integer.MAX_VALUE);
    }
    
    public double getCooldownMultiplier(MapLocation location, Team team){
        return cooldownMultipliers[locationToIndex(location)][team.ordinal()];
    }
    
   /**
     * @param cooldown without multiplier applied
     * @param location of robot calling the command
     * @param Team of robot calling the command
     * @return the cooldown due to boosts/destabilizes at that location
     */
    public int getCooldownWithMultiplier(int cooldown, MapLocation location, Team team) {
        return (int) Math.round(cooldown*cooldownMultipliers[locationToIndex(location)][team.ordinal()]);
    }

    // *********************************
    // ****** GAMEPLAY *****************
    // *********************************

    public void processBeginningOfRound() {
        //Update flag broadcast locations after a certain number of rounds
        if(currentRound % GameConstants.FLAG_BROADCAST_UPDATE_INTERVAL == 0) updateFlagBroadcastLocations();

        currentRound++;

        // Process beginning of each robot's round
        objectInfo.eachRobot((robot) -> {
            robot.processBeginningOfRound();
            return true;
        });
    }

    private void updateFlagBroadcastLocations() {
        for(Flag flag : allFlags) {
            updateFlagBroadcastLocation(flag);
        }
    }

    private void updateFlagBroadcastLocation(Flag flag) {
        MapLocation[] nearLocs = getAllLocationsWithinRadiusSquared(flag.getLoc(), GameConstants.FLAG_BROADCAST_NOISE_RADIUS);
        flag.setBroadcastLoc(nearLocs[rand.nextInt(nearLocs.length)]);
    }

    public void setWinner(Team t, DominationFactor d) {
        gameStats.setWinner(t);
        gameStats.setDominationFactor(d);
    }

    /**
     * @return whether a team has more sky islands captured
     */
    public boolean setWinnerIfMoreSkyIslands() {
        int skyIslandCountA = 0;
        int skyIslandCountB = 0;
        for(Island island : islandIdToIsland.values()) {
            if (island == null) {
                continue;
            }
            if(island.teamOwning == Team.A) skyIslandCountA++;
            else if(island.teamOwning == Team.B) skyIslandCountB++;
        }

        if (skyIslandCountA > skyIslandCountB) {
            setWinner(Team.A, DominationFactor.MORE_SKY_ISLANDS);
            return true;
        } else if (skyIslandCountA < skyIslandCountB) {
            setWinner(Team.B, DominationFactor.MORE_SKY_ISLANDS);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has more reality anchors placed
     */
    public boolean setWinnerIfMoreRealityAnchors() {
        int realityAnchorCountA = teamInfo.getAnchorsPlaced(Team.A);
        int realityAnchorCountB = teamInfo.getAnchorsPlaced(Team.B);
        
        if (realityAnchorCountA > realityAnchorCountB) {
            setWinner(Team.A, DominationFactor.MORE_REALITY_ANCHORS);
            return true;
        } else if (realityAnchorCountA < realityAnchorCountB) {
            setWinner(Team.B, DominationFactor.MORE_REALITY_ANCHORS);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has a greater net elixir value
     */
    public boolean setWinnerIfMoreElixirValue() {
        int[] totalElixirValues = new int[2];

        // consider team reserves
        totalElixirValues[Team.A.ordinal()] += this.teamInfo.getElixir(Team.A);
        totalElixirValues[Team.B.ordinal()] += this.teamInfo.getElixir(Team.B);
        
        if (totalElixirValues[Team.A.ordinal()] > totalElixirValues[Team.B.ordinal()]) {
            setWinner(Team.A, DominationFactor.MORE_ELIXIR_NET_WORTH);
            return true;
        } else if (totalElixirValues[Team.B.ordinal()] > totalElixirValues[Team.A.ordinal()]) {
            setWinner(Team.B, DominationFactor.MORE_ELIXIR_NET_WORTH);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has a greater net mana value
     */
    public boolean setWinnerIfMoreManaValue() {
        int[] totalManaValues = new int[2];

        // consider team reserves
        totalManaValues[Team.A.ordinal()] += this.teamInfo.getMana(Team.A);
        totalManaValues[Team.B.ordinal()] += this.teamInfo.getMana(Team.B);
        
        if (totalManaValues[Team.A.ordinal()] > totalManaValues[Team.B.ordinal()]) {
            setWinner(Team.A, DominationFactor.MORE_MANA_NET_WORTH);
            return true;
        } else if (totalManaValues[Team.B.ordinal()] > totalManaValues[Team.A.ordinal()]) {
            setWinner(Team.B, DominationFactor.MORE_MANA_NET_WORTH);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has a greater net adamantium value
     */
    public boolean setWinnerIfMoreAdamantiumValue() {
        int[] totalAdamantiumValues = new int[2];

        // consider team reserves
        totalAdamantiumValues[Team.A.ordinal()] += this.teamInfo.getAdamantium(Team.A);
        totalAdamantiumValues[Team.B.ordinal()] += this.teamInfo.getAdamantium(Team.B);
        
        if (totalAdamantiumValues[Team.A.ordinal()] > totalAdamantiumValues[Team.B.ordinal()]) {
            setWinner(Team.A, DominationFactor.MORE_ADAMANTIUM_NET_WORTH);
            return true;
        } else if (totalAdamantiumValues[Team.B.ordinal()] > totalAdamantiumValues[Team.A.ordinal()]) {
            setWinner(Team.B, DominationFactor.MORE_ADAMANTIUM_NET_WORTH);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has more flags
     */
    public boolean setWinnerIfMoreFlags(){
        int[] totalFlagsCaptured = new int[2];

        // consider team reserves
        totalFlagsCaptured[Team.A.ordinal()] += this.teamInfo.getFlagsCaptured(Team.A);
        totalFlagsCaptured[Team.B.ordinal()] += this.teamInfo.getFlagsCaptured(Team.B);
        
        if (totalFlagsCaptured[Team.A.ordinal()] > totalFlagsCaptured[Team.B.ordinal()]) {
            setWinner(Team.A, DominationFactor.MORE_FLAGS_PICKED);
            return true;
        } else if (totalFlagsCaptured[Team.B.ordinal()] > totalFlagsCaptured[Team.A.ordinal()]) {
            setWinner(Team.B, DominationFactor.MORE_FLAGS_PICKED);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has more tier three units
     */
    public boolean setWinnerIfMoreTierThree(){
        int[] totalTierThree = new int[2];

        // consider team reserves
        totalTierThree[Team.A.ordinal()] += this.teamInfo.getTierThree(Team.A);
        totalTierThree[Team.B.ordinal()] += this.teamInfo.getTierThree(Team.B);
        
        if (totalTierThree[Team.A.ordinal()] > totalTierThree[Team.B.ordinal()]) {
            setWinner(Team.A, DominationFactor.TIER_THREE);
            return true;
        } else if (totalTierThree[Team.B.ordinal()] > totalTierThree[Team.A.ordinal()]) {
            setWinner(Team.B, DominationFactor.TIER_THREE);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has more tier two units
     */
    public boolean setWinnerIfMoreTierTwo(){
        int[] totalTierTwo = new int[2];

        // consider team reserves
        totalTierTwo[Team.A.ordinal()] += this.teamInfo.getTierTwo(Team.A);
        totalTierTwo[Team.B.ordinal()] += this.teamInfo.getTierTwo(Team.B);
        
        if (totalTierTwo[Team.A.ordinal()] > totalTierTwo[Team.B.ordinal()]) {
            setWinner(Team.A, DominationFactor.TIER_TWO);
            return true;
        } else if (totalTierTwo[Team.B.ordinal()] > totalTierTwo[Team.A.ordinal()]) {
            setWinner(Team.B, DominationFactor.TIER_TWO);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has more bread
     */
    public boolean setWinnerIfMoreBread(){
        int[] totalBreadValues = new int[2];

        // consider team reserves
        totalBreadValues[Team.A.ordinal()] += this.teamInfo.getBread(Team.A);
        totalBreadValues[Team.B.ordinal()] += this.teamInfo.getBread(Team.B);
        
        if (totalBreadValues[Team.A.ordinal()] > totalBreadValues[Team.B.ordinal()]) {
            setWinner(Team.A, DominationFactor.MORE_BREAD);
            return true;
        } else if (totalBreadValues[Team.B.ordinal()] > totalBreadValues[Team.A.ordinal()]) {
            setWinner(Team.B, DominationFactor.MORE_BREAD);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has more flags picked up (but not sucessfully retrieved)
     */
    public boolean setWinnerIfMoreFlagsPickedUp(){
        int[] totalFlagsPickedUp = new int[2];

        // consider team reserves
        totalFlagsPickedUp[Team.A.ordinal()] += this.teamInfo.getFlagsPickedUp(Team.A);
        totalFlagsPickedUp[Team.B.ordinal()] += this.teamInfo.getFlagsPickedUp(Team.B);
        
        if (totalFlagsPickedUp[Team.A.ordinal()] > totalFlagsPickedUp[Team.B.ordinal()]) {
            setWinner(Team.A, DominationFactor.MORE_FLAGS_PICKED);
            return true;
        } else if (totalFlagsPickedUp[Team.B.ordinal()] > totalFlagsPickedUp[Team.A.ordinal()]) {
            setWinner(Team.B, DominationFactor.MORE_FLAGS_PICKED);
            return true;
        }
        return false;
    }

    /**
     * Sets a winner arbitrarily. Hopefully this is actually random.
     */
    public void setWinnerArbitrary() {
        setWinner(Math.random() < 0.5 ? Team.A : Team.B, DominationFactor.WON_BY_DUBIOUS_REASONS);
    }

    public boolean timeLimitReached() {
        return currentRound >= this.gameMap.getRounds();
    }

    /**
     * Checks end of match and then decides winner based on tiebreak conditions
     */
    public void checkEndOfMatch() {
        if (timeLimitReached() && gameStats.getWinner() == null) {

            //if (setWinnerIfMoreSkyIslands())      return;
            //if (setWinnerIfMoreRealityAnchors())  return;
            //if (setWinnerIfMoreElixirValue())     return;
            //if (setWinnerIfMoreManaValue())       return;
            //if (setWinnerIfMoreAdamantiumValue()) return;

            if (setWinnerIfMoreFlags()) return;
            if (setWinnerIfMoreTierThree()) return;
            if (setWinnerIfMoreTierTwo()) return;
            if (setWinnerIfMoreBread()) return;
            if (setWinnerIfMoreFlagsPickedUp()) return;

            setWinnerArbitrary();
        }
    }

    public void processEndOfRound() {

        //advance turn for all island
        for (Island island : getAllIslands()) {
            island.advanceTurn();
            this.matchMaker.addIslandInfo(island);
        }

        if(currentRound == GameConstants.SETUP_ROUNDS) processEndOfSetupPhase();

        //Reset dropped flags if necessary
        if (!isSetupPhase()) {
            for(Flag flag : allFlags) {
                if(!flag.isPickedUp() && flag.getLoc() != flag.getStartLoc()){ 
                    if(flag.getDroppedRounds() >= GameConstants.FLAG_DROPPED_RESET_ROUNDS)
                        moveFlagSetStartLoc(flag, flag.getStartLoc());
                    else
                        flag.incrementDroppedRounds();
                }
            }
        }
        
        //end any boosts that have finished their duration
        for (MapLocation loc : getAllLocations()){
            for (int teamIndex = 0; teamIndex <= 1; teamIndex++){ 
                ArrayList<Integer> curBoosts = this.boosts[locationToIndex(loc)][teamIndex][BOOST_INDEX];
                for (int j = curBoosts.size()-1; j >= 0; j--){
                    if (curBoosts.get(j) <= getCurrentRound()+1){
                        //update multiplier
                        if (curBoosts.size() <= GameConstants.MAX_BOOST_STACKS) {
                            cooldownMultipliers[locationToIndex(loc)][teamIndex] -= GameConstants.BOOSTER_MULTIPLIER;
                            cooldownMultipliers[locationToIndex(loc)][teamIndex] = Math.round(cooldownMultipliers[locationToIndex(loc)][teamIndex] * 100.0)/100.0;
                        }
                        curBoosts.remove(j);
                    }
                }
                ArrayList<Integer> curDestabilize = this.boosts[locationToIndex(loc)][teamIndex][DESTABILIZE_INDEX];
                for (int j = curDestabilize.size()-1; j >=0; j--){
                    if (curDestabilize.get(j) <= getCurrentRound()+1){
                        
                        //deal damage
                        InternalRobot robot = getRobot(loc);
                        if (robot != null && robot.getTeam().ordinal() == teamIndex) {
                            robot.addHealth(-1*RobotType.DESTABILIZER.damage);
                        }
                        //update multiplier if no longer being destabilized
                        if (curDestabilize.size() <= GameConstants.MAX_DESTABILIZE_STACKS) {
                            cooldownMultipliers[locationToIndex(loc)][teamIndex] -= GameConstants.DESTABILIZER_MULTIPLIER;
                            cooldownMultipliers[locationToIndex(loc)][teamIndex] = Math.round(cooldownMultipliers[locationToIndex(loc)][teamIndex] * 100.0)/100.0;
                        }
                        curDestabilize.remove(j);
                    }
                }
            }
        }
        // Process end of each robot's round
        objectInfo.eachRobot((robot) -> {
            // Add resources to team for each headquarter
            robot.processEndOfRound(currentRound);
            return true;
        });

        for (Well well : this.wells) {
            if (well == null)
                continue;
            this.matchMaker.addWell(well, locationToIndex(well.getMapLocation()));
        }
        this.matchMaker.addTeamInfo(Team.A, this.teamInfo.getRoundAdamantiumChange(Team.A), this.teamInfo.getRoundManaChange(Team.A), this.teamInfo.getRoundElixirChange(Team.A));
        this.matchMaker.addTeamInfo(Team.B, this.teamInfo.getRoundAdamantiumChange(Team.B), this.teamInfo.getRoundManaChange(Team.B), this.teamInfo.getRoundElixirChange(Team.B));
        this.teamInfo.processEndOfRound();

        //Apply currents after CURRENT_STRENGTH rounds
        if(currentRound % GameConstants.CURRENT_STRENGTH == 0){
            applyCurrents();
        }

        objectInfo.eachRobot((robot) -> {
            matchMaker.addMoved(robot.getID(), robot.getLocation());
            return true;
        });

        checkEndOfMatch();

        if (gameStats.getWinner() != null)
            running = false;
    }

    private void processEndOfSetupPhase() {
        ArrayList<Flag> teamAFlags = new ArrayList<>();
        ArrayList<Flag> teamBFlags = new ArrayList<>();
        for (Flag flag : allFlags) {
            if(flag.getTeam() == Team.A) teamAFlags.add(flag);
            else teamBFlags.add(flag);
        }
        confirmFlagPlacements(teamAFlags);
        confirmFlagPlacements(teamBFlags);
    }

    private void confirmFlagPlacements(ArrayList<Flag> teamFlags) {
        boolean validPlacements = true;
        for(int i = 0; i < teamFlags.size(); i++){
            for(int j = i + 1; j < teamFlags.size(); j++){
                Flag a = teamFlags.get(i), b = teamFlags.get(j);
                if(a.getLoc().distanceSquaredTo(b.getLoc()) < GameConstants.MIN_FLAG_SPACING_SQUARED) {
                    validPlacements = false;
                    break;
                }
            }
        }
        if(validPlacements)
            for(Flag flag : teamFlags) moveFlagSetStartLoc(flag, flag.getLoc());
        else
            for(Flag flag : teamFlags) moveFlagSetStartLoc(flag, flag.getStartLoc());
    }

    private void moveFlagSetStartLoc(Flag flag, MapLocation location){
        flag.drop();
        addFlag(location, flag);
        flag.setStartLoc(location);
        if(water[locationToIndex(location)]) 
            water[locationToIndex(location)] = false;
    }

    private void addToNotMoving(InternalRobot robot, HashMap<MapLocation, List<InternalRobot>> forecastedLocToRobot, Set<InternalRobot> notMoving, Set<MapLocation> visited) {
        MapLocation origLocation = robot.getLocation();
        if (visited.contains(origLocation)) {
            return;
        } else {
            visited.add(origLocation);
        }
        notMoving.add(robot);
        for (InternalRobot robotBlocked : forecastedLocToRobot.getOrDefault(origLocation, new ArrayList<>())) {
            addToNotMoving(robotBlocked, forecastedLocToRobot, notMoving, visited);
        }
    }

    private void applyCurrents() {
        HashMap<MapLocation, List<InternalRobot>> forecastedLocToRobot = new HashMap<>();
        // Figure out where each robot will go
        for (InternalRobot robot : this.objectInfo.robots()) {
            MapLocation origLoc = robot.getLocation();
            Direction origCurrent = getCurrent(origLoc);
            List<InternalRobot> robotsOnSquare = forecastedLocToRobot.getOrDefault(origLoc.add(origCurrent), new ArrayList<>());
            robotsOnSquare.add(robot);
            forecastedLocToRobot.put(origLoc.add(origCurrent), robotsOnSquare);
        }

        // Find all the robots that are blocked immediately
        Set<InternalRobot> immediatelyBlocked = new HashSet<>();
        for (MapLocation loc : forecastedLocToRobot.keySet()) {
            if (!isPassable(loc) || !gameMap.onTheMap(loc) || forecastedLocToRobot.get(loc).size() > 1) {
                immediatelyBlocked.addAll(forecastedLocToRobot.getOrDefault(loc, new ArrayList<>()));
            }
        }

        // Find all the robots that are blocked by other robots which are blocked
        Set<MapLocation> visited = new HashSet<>();
        Set<InternalRobot> notMoving = new HashSet<>();

        for (InternalRobot robot : immediatelyBlocked) {
            addToNotMoving(robot, forecastedLocToRobot, notMoving, visited);
        }

        Set<InternalRobot> movingRobots = new HashSet<>();
        // Clear all robots that are going to get moved
        for (InternalRobot robot : this.objectInfo.robots()) {
            if (notMoving.contains(robot) || getCurrent(robot.getLocation()) == Direction.CENTER) {
                continue;
            } else {
                this.objectInfo.clearRobotIndex(robot);
                removeRobot(robot.getLocation());
                movingRobots.add(robot);
            }
        }

        // Move all the robots that need to move, we assume this is a small subset
        for (InternalRobot robot : movingRobots) {
            MapLocation origLoc = robot.getLocation();
            Direction current = getCurrent(origLoc);
            MapLocation newLoc = origLoc.add(current);
            robot.setLocationForCurrents(newLoc);
        }
    }
    
    // *********************************
    // ****** SPAWNING *****************
    // *********************************

    public int createRobot(int ID, Team team) {
        InternalRobot robot = new InternalRobot(this, ID, team);
        objectInfo.createRobot(robot);
        controlProvider.robotSpawned(robot);
        matchMaker.addSpawnedRobot(robot);
        return ID;
    }

    public int createRobot(Team team) {
        int ID = idGenerator.nextID();
        return createRobot(ID, team);
    }

    // *********************************
    // ****** DESTROYING ***************
    // *********************************

    public void despawnRobot(int id) {
        InternalRobot robot = objectInfo.getRobotByID(id);
        robot.despawn();
        removeRobot(robot.getLocation());
        matchMaker.addDied(id);
    }

    /**
     * Permanently destroy a robot; left for internal purposes.
     */
    private void destroyRobot(int id) {
        InternalRobot robot = objectInfo.getRobotByID(id);
        RobotType type = robot.getType();
        Team team = robot.getTeam();
        removeRobot(robot.getLocation());

        controlProvider.robotKilled(robot);
        objectInfo.destroyRobot(id);

        for (ResourceType rType : ResourceType.values()) {
            robot.addResourceAmount(rType, -1*robot.getResource(rType));
        }
        matchMaker.addDied(id);
    }

    // *********************************
    // ********* PROFILER **************
    // *********************************

    public void setProfilerCollection(Team team, ProfilerCollection profilerCollection) {
        if (profilerCollections == null) {
            profilerCollections = new HashMap<>();
        }
        profilerCollections.put(team, profilerCollection);
    }
    
    /*
     * Checks if the given MapLocation contains a headquarters
     */
    public boolean isHeadquarters(MapLocation loc) {
        return getRobot(loc) != null && getRobot(loc).getType() == RobotType.HEADQUARTERS;
    }

    public boolean isWell(MapLocation loc) {
        if (!this.gameMap.onTheMap(loc)) {
            return false;
        }
        return this.wells[locationToIndex(loc)] != null;
    }

    public Island[] getAllIslands(){
        return islandIdToIsland.values().toArray(new Island[islandIdToIsland.size()]);
    }

    public boolean isSetupPhase() {
        return currentRound <= GameConstants.SETUP_ROUNDS;
    }
    
}

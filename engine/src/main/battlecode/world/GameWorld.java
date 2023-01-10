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
        this.islandIds = gm.getIslandArray();
        this.robots = new InternalRobot[gm.getWidth()][gm.getHeight()]; // if represented in cartesian, should be height-width, but this should allow us to index x-y
        this.currents = new Direction[gm.getWidth() * gm.getHeight()];
        this.currentRound = 0;
        this.idGenerator = new IDGenerator(gm.getSeed());
        this.gameStats = new GameStats();

        this.gameMap = gm;
        this.objectInfo = new ObjectInfo(gm);

        //Initialize currents
        int[] gmCurrents = gm.getCurrentArray();
        Arrays.fill(this.currents, Direction.CENTER);
        for(int i = 0; i < currents.length; i++) {
            this.currents[i] = Direction.DIRECTION_ORDER[gmCurrents[i]];
        }
        this.profilerCollections = new HashMap<>();

        this.controlProvider = cp;
        this.rand = new Random(this.gameMap.getSeed());
        this.matchMaker = matchMaker;

        controlProvider.matchStarted(this);

        // Add the robots contained in the LiveMap to this world.
        RobotInfo[] initialBodies = this.gameMap.getInitialBodies();
        for (int i = 0; i < initialBodies.length; i++) {
            RobotInfo robot = initialBodies[i];
            MapLocation newLocation = robot.location.translate(gm.getOrigin().x, gm.getOrigin().y);
            spawnRobot(robot.ID, robot.type, newLocation, robot.team);
        }
        this.teamInfo = new TeamInfo(this);

        this.islandIdToIsland = new HashMap<>();
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
        }

        // Write match header at beginning of match
        this.matchMaker.makeMatchHeader(this.gameMap);
        
        this.wells = new Well[gm.getWidth()*gm.getHeight()];
        for(int i = 0; i < gm.getResourceArray().length; i++){
            MapLocation loc = indexToLocation(i);
            ResourceType rType = ResourceType.values()[gm.getResourceArray()[i]];
            if (rType == ResourceType.NO_RESOURCE) {
                this.wells[i] = null;
            } else {
                this.wells[i] = new Well(loc, rType);
            }
        }

        //indices are: map position, team, boost/destabilize/anchor lists
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
        }

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
                objectInfo.eachDynamicBodyByExecOrder((body) -> {
                    if (body instanceof InternalRobot) {
                        InternalRobot hq = (InternalRobot) body;
                        if (hq.getType() != RobotType.HEADQUARTERS) {
                            throw new RuntimeException("Robots must be headquarters in round 1");
                        }
                        hq.addResourceAmount(ResourceType.ADAMANTIUM, GameConstants.INITIAL_AD_AMOUNT);
                        hq.addResourceAmount(ResourceType.MANA, GameConstants.INITIAL_MN_AMOUNT);
                        // Add initial amounts of resource
                        this.teamInfo.addAdamantium(hq.getTeam(), GameConstants.INITIAL_AD_AMOUNT);                        
                        this.teamInfo.addMana(hq.getTeam(), GameConstants.INITIAL_MN_AMOUNT);
                        return true;
                    } else {
                        throw new RuntimeException("non-robot body registered as dynamic");
                    }
                });
            }

            updateDynamicBodies();

            this.controlProvider.roundEnded();
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
        if (this.controlProvider.getTerminated(robot) && objectInfo.getRobotByID(robot.getID()) != null)
            destroyRobot(robot.getID());
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

    public Direction getCurrent(MapLocation loc) {
        return this.currents[locationToIndex(loc)];
    }

    public boolean isPassable(MapLocation loc) {
        return !this.walls[locationToIndex(loc)];
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


    /**
     * Helper method that converts a location into an index.
     * 
     * @param loc the MapLocation
     */
    public int locationToIndex(MapLocation loc) {
        return loc.x - this.gameMap.getOrigin().x + (loc.y - this.gameMap.getOrigin().y) * this.gameMap.getWidth();
    }

    /**
     * Helper method that converts an index into a location.
     * 
     * @param idx the index
     */
    public MapLocation indexToLocation(int idx) {
        return new MapLocation(idx % this.gameMap.getWidth() + this.gameMap.getOrigin().x,
                               idx / this.gameMap.getWidth() + this.gameMap.getOrigin().y);
    }

    // ***********************************
    // ****** BOOST METHODS **************
    // ***********************************
    
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
        assert(island.getAnchor() == Anchor.ACCELERATING);
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
        int boostIdentifier = island.getID();
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
        // Increment round counter
        currentRound++;

        // Process beginning of each robot's round
        objectInfo.eachRobot((robot) -> {
            robot.processBeginningOfRound();
            return true;
        });
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
        for(int id : islandIds) {
            Island island = islandIdToIsland.get(id);
            if (island == null) {
                assert(id == 0);
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

        // sum live robot worth
        for (InternalRobot robot : objectInfo.robotsArray()) {
            totalElixirValues[robot.getTeam().ordinal()] += robot.getController().getResourceAmount(ResourceType.ELIXIR);
        }
        
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

        // sum live robot worth
        for (InternalRobot robot : objectInfo.robotsArray()) {
            totalManaValues[robot.getTeam().ordinal()] += robot.getController().getResourceAmount(ResourceType.MANA);
        }
        
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

        // sum live robot worth
        for (InternalRobot robot : objectInfo.robotsArray()) {
            totalAdamantiumValues[robot.getTeam().ordinal()] += robot.getController().getResourceAmount(ResourceType.ADAMANTIUM);
        }
        
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

            if (setWinnerIfMoreSkyIslands())      return;
            if (setWinnerIfMoreRealityAnchors())  return;
            if (setWinnerIfMoreElixirValue())     return;
            if (setWinnerIfMoreManaValue())       return;
            if (setWinnerIfMoreAdamantiumValue()) return;

            setWinnerArbitrary();
        }
    }

    public void processEndOfRound() {

        //advance turn for all island
        for (Island island : getAllIslands()) {
            island.advanceTurn();
            this.matchMaker.addIslandInfo(island);
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
                            robot.addHealth(-1*robot.getType().damage);
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
                this.removeRobot(robot.getLocation());
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

    public int spawnRobot(int ID, RobotType type, MapLocation location, Team team) {
        InternalRobot robot;
        switch (type) {
            case CARRIER:
                robot = new InternalCarrier(this, ID, type, location, team);
                break;
            default:
                robot = new InternalRobot(this, ID, type, location, team);
                break;
        }
        objectInfo.spawnRobot(robot);
        addRobot(location, robot);

        controlProvider.robotSpawned(robot);
        matchMaker.addSpawnedRobot(robot);
        return ID;
    }

    public int spawnRobot(RobotType type, MapLocation location, Team team) {
        int ID = idGenerator.nextID();
        return spawnRobot(ID, type, location, team);
    }

    // *********************************
    // ****** DESTROYING ***************
    // *********************************

    public void destroyRobot(int id) {
        destroyRobot(id, true);
    }

    public void destroyRobot(int id, boolean checkArchonDeath) {
        InternalRobot robot = objectInfo.getRobotByID(id);
        RobotType type = robot.getType();
        Team team = robot.getTeam();
        removeRobot(robot.getLocation());

        controlProvider.robotKilled(robot);
        objectInfo.destroyRobot(id);

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

    /*
     * Checks to see if a robot is within range of certain objects and is thus able
     * to write to the shared array
     */
    public boolean inRangeForAmplification(InternalRobot bot) {
        MapLocation loc = bot.getLocation();
        int maxInterestRadius = Math.max(GameConstants.DISTANCE_SQUARED_FROM_HEADQUARTER, GameConstants.DISTANCE_SQUARED_FROM_SIGNAL_AMPLIFIER);
        for(InternalRobot otherRobot: this.getAllRobotsWithinRadiusSquared(bot.getLocation(), maxInterestRadius, bot.getTeam())){
            int maxDistance = 0;
            if (otherRobot.getType() == RobotType.AMPLIFIER) {
                maxDistance = GameConstants.DISTANCE_SQUARED_FROM_SIGNAL_AMPLIFIER;
            } else if (otherRobot.getType() == RobotType.HEADQUARTERS) {
                maxDistance = GameConstants.DISTANCE_SQUARED_FROM_HEADQUARTER;
            }
            int distance = otherRobot.getLocation().distanceSquaredTo(loc);
            if (distance <= maxDistance)
                return true;
        }
        for(Island island: getAllIslands()) {
            if (island.getTeam() == bot.getTeam()) {
                int distance = island.minDistTo(loc);
                if (distance <= GameConstants.DISTANCE_SQUARED_FROM_ISLAND)
                    return true;
            }
        }
        return false;
    }


    public Island[] getAllIslands(){
        return islandIdToIsland.values().toArray(new Island[islandIdToIsland.size()]);
    }

    
}

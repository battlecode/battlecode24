package battlecode.world;

import battlecode.common.*;
import battlecode.instrumenter.profiler.ProfilerCollection;
import battlecode.schema.Action;
import battlecode.server.ErrorReporter;
import battlecode.server.GameMaker;
import battlecode.server.GameState;
import battlecode.world.control.RobotControlProvider;

import java.util.*;

import javax.lang.model.util.ElementScanner6;

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

    private Headquarter[] headquarters;

    private boolean[] passable;
    private int[] rubble;
    private int[] lead;
    private int[] gold;
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
        this.rubble = gm.getRubbleArray();
        this.lead = gm.getLeadArray();
        this.passable = gm.getPassableArray();
        this.gold = new int[this.lead.length];
        this.robots = new InternalRobot[gm.getWidth()][gm.getHeight()]; // if represented in cartesian, should be height-width, but this should allow us to index x-y
        this.islandIds = new int[this.lead.length];
        this.currents = new Direction[gm.getWidth() * gm.getHeight()];
        this.currentRound = 0;
        this.idGenerator = new IDGenerator(gm.getSeed());
        this.gameStats = new GameStats();

        this.gameMap = gm;
        this.objectInfo = new ObjectInfo(gm);

        //indices are: map position, team, boost/destabilize/anchor lists
        ArrayList<Integer>[][][] boosts = new ArrayList[gm.getWidth()*gm.getHeight()][2][3];
        for (int i = 0; i < boosts.length; i++){ 
            for (int j = 0; j < boosts[0].length; j++)
                for (int k = 0; k < boosts[0][0].length; k++)
                    boosts[i][j][k] = new ArrayList<Integer>();
        }
        double[][] cooldownMultipliers = new double[gm.getWidth()*gm.getHeight()][2];
        for (int i = 0; i < gm.getHeight()*gm.getWidth(); i++){
            cooldownMultipliers[i][0] = 1.0;
            cooldownMultipliers[i][1] = 1.0;
        }
        for (MapLocation loc : getAllLocations()){
                if (getCurrent(loc) != null){ 
                   cooldownMultipliers[locationToIndex(loc)][0] += GameConstants.CURRENT_MULTIPLIER; 
                   cooldownMultipliers[locationToIndex(loc)][1] += GameConstants.CURRENT_MULTIPLIER;
                }
                else if (hasCloud(loc)){
                   cooldownMultipliers[locationToIndex(loc)][0] += GameConstants.CLOUD_MULTIPLIER; 
                   cooldownMultipliers[locationToIndex(loc)][1] += GameConstants.CLOUD_MULTIPLIER; 
                }
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
        this.islandIdToIsland.put(0, null);
        for (int key : islandIdToLocations.keySet()) {
            Island newIsland = new Island(this, key, islandIdToLocations.get(key));
            this.islandIdToIsland.put(key, newIsland);            
        }

        //Initialize currents
        int[] gmCurrents = gm.getCurrentArray();
        for(int i = 0; i < currents.length; i++) {
            currents[i] = Direction.DIRECTION_ORDER[i];
        }

        // Add initial amounts of resource
        this.teamInfo.addLead(Team.A, GameConstants.INITIAL_LEAD_AMOUNT);
        this.teamInfo.addLead(Team.B, GameConstants.INITIAL_LEAD_AMOUNT);
        this.teamInfo.addGold(Team.A, GameConstants.INITIAL_GOLD_AMOUNT);
        this.teamInfo.addGold(Team.B, GameConstants.INITIAL_GOLD_AMOUNT);

        // Write match header at beginning of match
        this.matchMaker.makeMatchHeader(this.gameMap);
        
        this.wells = new Well[gm.getWidth()*gm.getHeight()];
        for(int i = 0; i < gm.getWellResourcesArray().length; i++){
            Inventory inv = new Inventory();
            MapLocation loc = indexToLocation(i);
            this.wells[i] = new Well(loc, ResourceType.values()[gm.getWellResourcesArray()[i]]);
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

    /**
     * Defensively copied at the level of LiveMap.
     */
    public AnomalyScheduleEntry[] getAnomalySchedule() {
        return this.gameMap.getAnomalySchedule();
    }

    public boolean isRunning() {
        return this.running;
    }

    public int getCurrentRound() {
        return this.currentRound;
    }

    public int getRubble(MapLocation loc) {
        return this.rubble[locationToIndex(loc)];
    }

    public int getLead(MapLocation loc) {
        return this.lead[locationToIndex(loc)];
    }

    public void setLead(MapLocation loc, int amount) {
        this.lead[locationToIndex(loc)] = amount;
    }

    public int getGold(MapLocation loc) {
        return this.gold[locationToIndex(loc)];
    }

    public void setGold(MapLocation loc, int amount) {
        this.gold[locationToIndex(loc)] = amount;
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
            if (curBoostsList.size() == 0)
                cooldownMultipliers[locationToIndex(loc)][team.ordinal()] += GameConstants.BOOSTER_MULTIPLIER;
            curBoostsList.add(lastRound);
        }
    }
    public void addDestabilize(MapLocation center, Team team){ //team of the destabilizer robot
        int lastRound = getCurrentRound() + GameConstants.DESTABILIZER_DURATION;
        int radiusSquared = GameConstants.DESTABILIZER_RADIUS_SQUARED;
        for (MapLocation loc : getAllLocationsWithinRadiusSquared(center, radiusSquared)){
            ArrayList<Integer> curDestabilizers = this.boosts[locationToIndex(loc)][team.opponent().ordinal()][DESTABILIZE_INDEX];
            if (curDestabilizers.size() == 0)
                cooldownMultipliers[locationToIndex(loc)][team.opponent().ordinal()] += GameConstants.DESTABILIZER_MULTIPLIER;
            curDestabilizers.add(lastRound);
        }
    }
    public void addBoostFromAnchor(Island island){
        assert(island.getAnchor() == Anchor.ACCELERATING);
        int teamOrdinal = island.getTeam().ordinal(); 
        for (MapLocation loc : island.getLocsAffected()){
            ArrayList<Integer> curAnchorList = this.boosts[locationToIndex(loc)][teamOrdinal][ANCHOR_INDEX];
            if (curAnchorList.size() == 0){
                //if current location is already being boosted
                if (this.boosts[locationToIndex(loc)][teamOrdinal][BOOST_INDEX].size() != 0)
                    cooldownMultipliers[locationToIndex(loc)][teamOrdinal] += GameConstants.ANCHOR_MULTIPLIER-GameConstants.BOOSTER_MULTIPLIER;
                else
                    cooldownMultipliers[locationToIndex(loc)][teamOrdinal] += GameConstants.ANCHOR_MULTIPLIER;
            }
            curAnchorList.add(island.getIdx());
        }
    }
    public void removeBoostFromAnchor(Island island){
        int teamOrdinal = island.getTeam().ordinal();
        int boostIdentifier = island.getIdx();
        for (MapLocation loc : island.getLocsAffected()){
            ArrayList<Integer> curAnchorList = this.boosts[locationToIndex(loc)][teamOrdinal][ANCHOR_INDEX];
            curAnchorList.remove(boostIdentifier);
            if (curAnchorList.size() == 0){
                if (this.boosts[locationToIndex(loc)][teamOrdinal][BOOST_INDEX].size() != 0)
                    cooldownMultipliers[locationToIndex(loc)][teamOrdinal] -= GameConstants.ANCHOR_MULTIPLIER;
                else
                    cooldownMultipliers[locationToIndex(loc)][teamOrdinal] -= GameConstants.ANCHOR_MULTIPLIER - GameConstants.BOOSTER_MULTIPLIER;    
            }
    
        }
    }

    // ***********************************
    // ****** ROBOT METHODS **************
    // ***********************************

    public InternalRobot getRobot(MapLocation loc) {
        return this.robots[loc.x - this.gameMap.getOrigin().x][loc.y - this.gameMap.getOrigin().y];
    }

    public boolean isPassable(MapLocation loc) {
        return this.passable[locationToIndex(loc)];
    }

    public Island getIsland(MapLocation loc) {
        return islandIdToIsland.get(this.islandIds[locationToIndex(loc)]);
    }

    public Direction getCurrent(MapLocation loc) {
        return this.currents[locationToIndex(loc)];
    }

    public Island getIsland(int islandIdx) {
        return islandIdToIsland.get(islandIdx);
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
        ArrayList<InternalRobot> returnRobots = new ArrayList<InternalRobot>();
        for (MapLocation newLocation : getAllLocationsWithinRadiusSquared(center, radiusSquared))
            if (getRobot(newLocation) != null)
                returnRobots.add(getRobot(newLocation));
        return returnRobots.toArray(new InternalRobot[returnRobots.size()]);
    }

    public Island[] getAllIslandsWithinRadiusSquared(MapLocation center, int radiusSquared) {
        ArrayList<Island> returnIslands = new ArrayList<Island>();
        for (MapLocation newLocation : getAllLocationsWithinRadiusSquared(center, radiusSquared))
            if (getIsland(newLocation) != null)
                returnIslands.add(getIsland(newLocation));
        return returnIslands.toArray(new Island[returnIslands.size()]);
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

    /**
     * @param cooldown without multiplier applied
     * @param location with rubble of interest, if any
     * @return the cooldown due to rubble
     */
    public int getCooldownWithMultiplier(int cooldown, MapLocation location) {
        return (int) ((1 + getRubble(location) / 10.0) * cooldown);
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
            totalElixirValues[robot.getTeam().ordinal()] += robot.getController().getExAmount();
        }
        
        if (totalElixirValues[0] > totalElixirValues[1]) {
            setWinner(Team.A, DominationFactor.MORE_ELIXIR_NET_WORTH);
            return true;
        } else if (totalElixirValues[1] > totalElixirValues[0]) {
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
            totalManaValues[robot.getTeam().ordinal()] += robot.getController().getMnAmount();
        }
        
        if (totalManaValues[0] > totalManaValues[1]) {
            setWinner(Team.A, DominationFactor.MORE_MANA_NET_WORTH);
            return true;
        } else if (totalManaValues[1] > totalManaValues[0]) {
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
            totalAdamantiumValues[robot.getTeam().ordinal()] += robot.getController().getAdAmount();
        }
        
        if (totalAdamantiumValues[0] > totalAdamantiumValues[1]) {
            setWinner(Team.A, DominationFactor.MORE_ADAMANTIUM_NET_WORTH);
            return true;
        } else if (totalAdamantiumValues[1] > totalAdamantiumValues[0]) {
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
        // Add lead resources to the team
        this.teamInfo.addLead(Team.A, GameConstants.PASSIVE_LEAD_INCREASE);
        this.teamInfo.addLead(Team.B, GameConstants.PASSIVE_LEAD_INCREASE);

        // Process end of each robot's round
        objectInfo.eachRobot((robot) -> {
            robot.processEndOfRound();
            return true;
        });
        
        //end any boosts that have finished their duration
        for (MapLocation loc : getAllLocations()){
            for (int teamIndex = 0; teamIndex <= 1; teamIndex++){ 
                ArrayList<Integer> curBoosts = this.boosts[locationToIndex(loc)][teamIndex][BOOST_INDEX];
                for (int j = curBoosts.size()-1; j >= 0; j--){
                    if (curBoosts.get(j) >= getCurrentRound()+1){
                        curBoosts.remove(j);
                        //update multiplier if no longer being boosted by a booster/anchor
                        if (curBoosts.size() == 0 && this.boosts[locationToIndex(loc)][teamIndex][ANCHOR_INDEX].size() == 0)
                            cooldownMultipliers[locationToIndex(loc)][teamIndex] -= GameConstants.BOOSTER_MULTIPLIER;
                    }
                }
                ArrayList<Integer> curDestabilize = this.boosts[locationToIndex(loc)][teamIndex][DESTABILIZE_INDEX];
                for (int j = curDestabilize.size()-1; j >=0; j--){
                    if (curDestabilize.get(j) >= getCurrentRound()+1){
                        curDestabilize.remove(j);
                        //update multiplier if no longer being destabilized
                        if (curDestabilize.size() == 0)
                            cooldownMultipliers[locationToIndex(loc)][teamIndex] -= GameConstants.DESTABILIZER_MULTIPLIER;
                    }
                }
            }
        }

        // Trigger any anomalies
        // note: singularity is handled below in the "check for end of match"
        AnomalyScheduleEntry nextAnomaly = this.gameMap.viewNextAnomaly();
        if (nextAnomaly != null && nextAnomaly.roundNumber == this.currentRound) {
            AnomalyType anomaly = this.gameMap.takeNextAnomaly().anomalyType;
            if (anomaly == AnomalyType.ABYSS) causeAbyssGlobal();
            if (anomaly == AnomalyType.CHARGE) causeChargeGlobal();
            if (anomaly == AnomalyType.FURY) causeFuryGlobal();
            if (anomaly == AnomalyType.VORTEX) causeVortexGlobal();
        }

        // Add lead resources to the map
        if (this.currentRound % GameConstants.ADD_LEAD_EVERY_ROUNDS == 0)
            for (int i = 0; i < this.lead.length; i++)
                if (this.lead[i] > 0)
                    this.lead[i] += GameConstants.ADD_LEAD;

        this.matchMaker.addTeamInfo(Team.A, this.teamInfo.getRoundLeadChange(Team.A), this.teamInfo.getRoundGoldChange(Team.A));
        this.matchMaker.addTeamInfo(Team.B, this.teamInfo.getRoundLeadChange(Team.B), this.teamInfo.getRoundGoldChange(Team.B));
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

    private boolean attemptApplyCurrent(InternalRobot robot, HashMap<InternalRobot, Boolean> moved){
        //If we already attempted to move the robot, it cannot be moved again
        if(moved.get(robot)) return false;

        moved.put(robot, true);
        MapLocation loc = robot.getLocation();
        Direction current = getCurrent(loc);
        if (current == Direction.CENTER) {
            return false;
        }
        MapLocation moveTo = loc.add(current);

        if(!gameMap.onTheMap(moveTo) || !isPassable(moveTo)) return false;
        InternalRobot inMoveTo = getRobot(moveTo);
        if(inMoveTo == null) {
            robot.setLocation(moveTo);
            return true;
        }
        if(moved.containsKey(inMoveTo) && !moved.get(inMoveTo)) {
            // Set the location earlier so loops work
            robot.setLocation(moveTo);
            if(attemptApplyCurrent(inMoveTo, moved)) {
                return true;
            }
            robot.setLocation(loc);
            return false;
        }
        return false;
    }

    private void applyCurrents() {
        //Map of all robots that are on a space with a current
        //The value is true if an attempt has been made to move the robot
        HashMap<InternalRobot, Boolean> moved = new HashMap<>();
        for(int i = 0; i < robots.length; i++){
            for(int j = 0; j < robots[i].length; j++) {
                InternalRobot robot = robots[i][j];
                MapLocation loc = robot.getLocation();
                if(getCurrent(loc) != Direction.CENTER) {
                    moved.put(robot, false);
                }
            }
        }

        for(InternalRobot robot : moved.keySet()){
            attemptApplyCurrent(robot, moved);
        }
    }

    // *********************************
    // ****** SPAWNING *****************
    // *********************************

    public int spawnRobot(int ID, RobotType type, MapLocation location, Team team) {
        InternalRobot robot = new InternalRobot(this, ID, type, location, team);
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

        int leadDropped = robot.getType().getLeadDropped(robot.getLevel());
        int goldDropped = robot.getType().getGoldDropped(robot.getLevel());

        this.lead[locationToIndex(robot.getLocation())] += leadDropped;
        this.gold[locationToIndex(robot.getLocation())] += goldDropped;

        this.matchMaker.addLeadDrop(robot.getLocation(), leadDropped);
        this.matchMaker.addGoldDrop(robot.getLocation(), goldDropped);

        controlProvider.robotKilled(robot);
        objectInfo.destroyRobot(id);

        if (checkArchonDeath) {
            // this happens here because both teams' Archons can die in the same round
            if (type == RobotType.ARCHON && this.objectInfo.getRobotTypeCount(team, RobotType.ARCHON) == 0)
                setWinner(team == Team.A ? Team.B : Team.A, DominationFactor.ANNIHILATION);
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

    // *********************************
    // ********  ANOMALY  **************
    // *********************************

    /**
     * Finds all of the locations that a given Sage can affect with an Anomaly.
     * @param robot that is causing the anomaly; must be a Sage
     * @return all of the locations that are within range of this sage
     */
    private MapLocation[] getSageActionLocations(InternalRobot robot) {
        assert robot.getType() == RobotType.SAGE;
        MapLocation center = robot.getLocation();
        return getAllLocationsWithinRadiusSquared(center, robot.getType().actionRadiusSquared);
    }

    /**
     * Performs the Abyss anomaly. Changes the resources in the squares and the team.
     * @param reduceFactor associated with anomaly (a decimal percentage)
     * @param locations that can be affected by the Abyss
     */
    private void causeAbyssGridUpdate(float reduceFactor, MapLocation[] locations) {
        for (int i = 0; i < locations.length; i++) {
            int currentLead = getLead(locations[i]);
            int leadUpdate = (int) (reduceFactor * currentLead);
            setLead(locations[i], currentLead - leadUpdate);
            if (leadUpdate != 0) this.matchMaker.addLeadDrop(locations[i], -leadUpdate);

            int currentGold = getGold(locations[i]);
            int goldUpdate = (int) (reduceFactor * currentGold);
            setGold(locations[i], currentGold - goldUpdate);
            if (goldUpdate != 0) this.matchMaker.addGoldDrop(locations[i], -goldUpdate);
        }
    }

    /**
     * Mutates state to perform the Sage Abyss anomaly.
     * @param robot that is the Sage
     */
    public void causeAbyssSage(InternalRobot robot) {
        assert robot.getType() == RobotType.SAGE;
        // calculate the right effect range
        this.causeAbyssGridUpdate(AnomalyType.ABYSS.sagePercentage, this.getSageActionLocations(robot));
    }

    /**
     * Mutates state to perform the global Abyss anomaly.
     */
    public void causeAbyssGlobal() {
        this.causeAbyssGridUpdate(AnomalyType.ABYSS.globalPercentage, this.getAllLocations());
        
        this.teamInfo.addLead(Team.A, (int) (-1 * AnomalyType.ABYSS.globalPercentage * this.teamInfo.getLead(Team.A)));
        this.teamInfo.addLead(Team.B, (int) (-1 * AnomalyType.ABYSS.globalPercentage * this.teamInfo.getLead(Team.B)));

        this.teamInfo.addGold(Team.A, (int) (-1 * AnomalyType.ABYSS.globalPercentage * this.teamInfo.getGold(Team.A)));
        this.teamInfo.addGold(Team.B, (int) (-1 * AnomalyType.ABYSS.globalPercentage * this.teamInfo.getGold(Team.B)));
        this.matchMaker.addAction(-1, Action.ABYSS, -1);
    }

    /**
     * Mutates state to perform the Sage Charge.
     * @param robot performing the Charge, must be a Sage
     */
    public void causeChargeSage(InternalRobot robot) {
        assert robot.getType() == RobotType.SAGE;

        MapLocation[] actionLocations = this.getSageActionLocations(robot);
        for (int i = 0; i < actionLocations.length; i++) {
            InternalRobot currentRobot = getRobot(actionLocations[i]);
            if (currentRobot != null && currentRobot.getTeam() != robot.getTeam() && currentRobot.getMode() == RobotMode.DROID)
                currentRobot.addHealth((int) (-1 * AnomalyType.CHARGE.sagePercentage * currentRobot.getType().getMaxHealth(currentRobot.getLevel())));
        }
    }

    /**
     * Mutates state to peform the global Charge.
     */
    public void causeChargeGlobal() {
        ArrayList<InternalRobot> droids = new ArrayList<InternalRobot>();
        for (InternalRobot currentRobot : this.objectInfo.robotsArray()) {
            if (currentRobot.getMode() == RobotMode.DROID) {
                droids.add(currentRobot);
                currentRobot.updateNumVisibleFriendlyRobots();
            }
        }
        Collections.sort(droids, new SortByFriends());

        int affectedDroidsLimit = (int) (AnomalyType.CHARGE.globalPercentage * droids.size());
        for (int i = 0; i < affectedDroidsLimit; i++) {
            this.destroyRobot(droids.get(i).getID());
        }
        this.matchMaker.addAction(-1, Action.CHARGE, -1);
    }

    /** Used to sort droids for charge */
    class SortByFriends implements Comparator<InternalRobot> {
        public int compare(InternalRobot a, InternalRobot b) {
            return b.getNumVisibleFriendlyRobots(false) - a.getNumVisibleFriendlyRobots(false);
        }
    }

    /**
     * Performs the Fury anomaly. Changes the health of the relevant robots.
     * @param reduceFactor associated with anomaly (a decimal percentage)
     * @param locations that can be affected by the Fury (by radius, not by state of robot)
     */
    public void causeFuryUpdate(float reduceFactor, MapLocation[] locations) {
        for (int i = 0; i < locations.length; i++) {
            InternalRobot robot = this.getRobot(locations[i]);
            if (robot != null && robot.getMode() == RobotMode.TURRET) {
                robot.addHealth((int) (-1 * robot.getType().getMaxHealth(robot.getLevel()) * reduceFactor), false);
            }
        }

        boolean teamAEliminated = this.objectInfo.getRobotTypeCount(Team.A, RobotType.ARCHON) == 0;
        boolean teamBEliminated = this.objectInfo.getRobotTypeCount(Team.B, RobotType.ARCHON) == 0;
        if (teamAEliminated && teamBEliminated) {
            // copy pasted from processEndOfRound
            if (!setWinnerIfMoreGoldValue())
                if (!setWinnerIfMoreLeadValue())
                    setWinnerArbitrary();
        } else if (teamAEliminated) {
            setWinner(Team.B, DominationFactor.ANNIHILATION);
        } else if (teamBEliminated) {
            setWinner(Team.A, DominationFactor.ANNIHILATION);
        }
    }

    /**
     * Mutates state to perform the Sage Fury.
     * @param robot performing the Fury, must be a Sage
     */
    public void causeFurySage(InternalRobot robot) {
        assert robot.getType() == RobotType.SAGE;
        this.causeFuryUpdate(AnomalyType.FURY.sagePercentage, this.getSageActionLocations(robot));
    }

    /**
     * Mutates state to peform the global Fury.
     */
    public void causeFuryGlobal() {
        this.causeFuryUpdate(AnomalyType.FURY.globalPercentage, this.getAllLocations());
        this.matchMaker.addAction(-1, Action.FURY, -1);
    }

    private void rotateRubble() {
        int n = this.gameMap.getWidth();
        for (int x = 0; x < n / 2; x++) {
            for (int y = 0; y < (n + 1) / 2; y++) {
                int curX = x;
                int curY = y;
                int lastRubble = this.rubble[curX + curY * n];
                for (int i = 0; i < 4; i++) {
                    int tempX = curX;
                    curX = curY;
                    curY = (n - 1) - tempX;
                    int idx = curX + curY * n;
                    int tempRubble = this.rubble[idx];
                    this.rubble[idx] = lastRubble;
                    lastRubble = tempRubble;
                }
            }
        }
    }

    private void flipRubbleHorizontally() {
        int w = this.gameMap.getWidth();
        int h = this.gameMap.getHeight();
        for (int x = 0; x < w / 2; x++) {
            for (int y = 0; y < h; y++) {
                int idx = x + y * w;
                int newX = w - 1 - x;
                int newIdx = newX + y * w;
                int prevRubble = this.rubble[idx];
                this.rubble[idx] = this.rubble[newIdx];
                this.rubble[newIdx] = prevRubble;
            }
        }
    }

    private void flipRubbleVertically() {
        int w = this.gameMap.getWidth();
        int h = this.gameMap.getHeight();
        for (int y = 0; y < h / 2; y++) {
            for (int x = 0; x < w; x++) {
                int idx = x + y * w;
                int newY = h - 1 - y;
                int newIdx = x + newY * w;
                int prevRubble = this.rubble[idx];
                this.rubble[idx] = this.rubble[newIdx];
                this.rubble[newIdx] = prevRubble;
            }
        }
    }

    /**
     * Mutates state to peform the global Vortex.
     * Only mutates the rubble array in this class; doesn't change the LiveMap
     */
    public void causeVortexGlobal() {
        int changeIdx = 0;
        switch (this.gameMap.getSymmetry()) {
            case VERTICAL:
                flipRubbleVertically();
                changeIdx = 2;
                break;
            case HORIZONTAL:
                flipRubbleHorizontally();
                changeIdx = 1;
                break;
            case ROTATIONAL:
                // generate random choice of how rotation will occur
                // can only rotate if it's a square map
                boolean squareMap = this.gameMap.getWidth() == this.gameMap.getHeight();
                int randomNumber = this.rand.nextInt(squareMap ? 3 : 2);
                if (!squareMap) {
                    randomNumber++;
                }
                if (randomNumber == 0) {
                    rotateRubble();
                } else if (randomNumber == 1) {
                    flipRubbleHorizontally();
                } else if (randomNumber == 2) {
                    flipRubbleVertically();
                }
                changeIdx = randomNumber;
                break;
        }
        this.matchMaker.addAction(-1, Action.VORTEX, changeIdx);
    }
    
    public boolean isWell(MapLocation loc){
        if (getWell(loc) != null)
            return true;
        else 
            return false;
    }
    

    public Well getWell(MapLocation loc) {
        return this.wells[locationToIndex(loc)];
    }


                
    /*
     * Checks if the given MapLocation contains a headquarters
     */
    public boolean isHeadquarters(MapLocation loc) {
        return getHeadquarters(loc) != null;
    }

    /*
     * Returns the Headquarters at the given location, or null if there is no headquarters
     */
    public Headquarter getHeadquarters(MapLocation loc) {
        for(Headquarter headquarter : headquarters) {
            if(headquarter.getLocation() == loc) return headquarter;
        }
        return null;
    }
}

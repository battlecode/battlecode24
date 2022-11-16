package battlecode.world;

import battlecode.common.*;
import battlecode.instrumenter.profiler.ProfilerCollection;
import battlecode.schema.Action;
import battlecode.server.ErrorReporter;
import battlecode.server.GameMaker;
import battlecode.server.GameState;
import battlecode.world.control.RobotControlProvider;

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
    private int[] currents;
    private int[] islands;
    private int[] resources;
    private InternalRobot[][] robots;
    private int[] islandIds;
    private HashMap<Integer, Island> islandIdToIsland;
    private final LiveMap gameMap;
    private final TeamInfo teamInfo;
    private final ObjectInfo objectInfo;

    private Map<Team, ProfilerCollection> profilerCollections;

    private final RobotControlProvider controlProvider;
    private Random rand;
    private final GameMaker.MatchMaker matchMaker;

    @SuppressWarnings("unchecked")
    public GameWorld(LiveMap gm, RobotControlProvider cp, GameMaker.MatchMaker matchMaker) {
        this.walls = gm.getWallArray();
        this.clouds = gm.getCloudArray();
        this.currents = gm.getCurrentArray();
        this.islandIds = gm.getIslandArray();
        this.resources = gm.getResourceArray();
        this.robots = new InternalRobot[gm.getWidth()][gm.getHeight()]; // if represented in cartesian, should be height-width, but this should allow us to index x-y
        this.currentRound = 0;
        this.idGenerator = new IDGenerator(gm.getSeed());
        this.gameStats = new GameStats();

        this.gameMap = gm;
        this.objectInfo = new ObjectInfo(gm);

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

        // Add initial amounts of resource
        this.teamInfo.addMana(Team.A, GameConstants.INITIAL_MN_AMOUNT);
        this.teamInfo.addMana(Team.B, GameConstants.INITIAL_MN_AMOUNT);
        this.teamInfo.addAdamantium(Team.A, GameConstants.INITIAL_AD_AMOUNT);
        this.teamInfo.addAdamantium(Team.B, GameConstants.INITIAL_AD_AMOUNT);

        // Write match header at beginning of match
        this.matchMaker.makeMatchHeader(this.gameMap);
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
            System.out.println("Round: " + this.currentRound);

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
        return this.clouds[locationToIndex(loc)];
    }

    public int getCurrent(MapLocation loc) {
        return this.currents[locationToIndex(loc)];
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
    // ****** ROBOT METHODS **************
    // ***********************************

    public InternalRobot getRobot(MapLocation loc) {
        return this.robots[loc.x - this.gameMap.getOrigin().x][loc.y - this.gameMap.getOrigin().y];
    }

    public boolean isPassable(MapLocation loc) {
        return !this.walls[locationToIndex(loc)];
    }

    public Island getIsland(MapLocation loc) {
        return islandIdToIsland.get(this.islandIds[locationToIndex(loc)]);
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
        //TODO: implement
        return cooldown;
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
            totalManaValues[robot.getTeam().ordinal()] += robot.getController().getResourceAmount(ResourceType.MANA);
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
            totalAdamantiumValues[robot.getTeam().ordinal()] += robot.getController().getResourceAmount(ResourceType.ADAMANTIUM);
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
        // Add resources to team
        // TODO: this is not enough, it needs to go to a headquarter
        this.teamInfo.addAdamantium(Team.A, GameConstants.PASSIVE_AD_INCREASE);
        this.teamInfo.addAdamantium(Team.B, GameConstants.PASSIVE_AD_INCREASE);

        this.teamInfo.addMana(Team.A, GameConstants.PASSIVE_MN_INCREASE);
        this.teamInfo.addMana(Team.B, GameConstants.PASSIVE_MN_INCREASE);



        // Process end of each robot's round
        objectInfo.eachRobot((robot) -> {
            robot.processEndOfRound();
            return true;
        });

        this.matchMaker.addTeamInfo(Team.A, this.teamInfo.getRoundAdamantiumChange(Team.A), this.teamInfo.getRoundManaChange(Team.A), this.teamInfo.getRoundElixirChange(Team.A));
        this.matchMaker.addTeamInfo(Team.B, this.teamInfo.getRoundAdamantiumChange(Team.B), this.teamInfo.getRoundManaChange(Team.B), this.teamInfo.getRoundElixirChange(Team.B));
        this.teamInfo.processEndOfRound();

        checkEndOfMatch();

        if (gameStats.getWinner() != null)
            running = false;
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

        controlProvider.robotKilled(robot);
        objectInfo.destroyRobot(id);

        // if (checkArchonDeath) {
        //     // this happens here because both teams' Archons can die in the same round
        //     if (type == RobotType.ARCHON && this.objectInfo.getRobotTypeCount(team, RobotType.ARCHON) == 0)
        //         setWinner(team == Team.A ? Team.B : Team.A, DominationFactor.ANNIHILATION);
        // }

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


    // TODO: move this somewhere better
    /*
     * Checks if the given MapLocation contains a headquarters
     */
    public boolean isHeadquarters(MapLocation loc) {
        return getRobot(loc).getType() == RobotType.HEADQUARTERS;
    }
}

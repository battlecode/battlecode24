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

    private double[] passability;
    private int[] leadCount;
    private int[] goldCount;
    private InternalRobot[][] robots;
    private final LiveMap gameMap;
    private final TeamInfo teamInfo;
    private final ObjectInfo objectInfo;

    private Map<Team, ProfilerCollection> profilerCollections;

    private final RobotControlProvider controlProvider;
    private Random rand;
    private final GameMaker.MatchMaker matchMaker;

    @SuppressWarnings("unchecked")
    public GameWorld(LiveMap gm, RobotControlProvider cp, GameMaker.MatchMaker matchMaker) {
        this.passability = gm.getPassabilityArray();
        this.leadCount = gm.getLeadArray();
        this.goldCount = gm.getGoldArray();
        this.robots = new InternalRobot[gm.getWidth()][gm.getHeight()]; // if represented in cartesian, should be
                                                                        // height-width, but this should allow us to
                                                                        // index x-y
        this.currentRound = 0;
        this.idGenerator = new IDGenerator(gm.getSeed());
        this.gameStats = new GameStats();

        this.gameMap = gm;
        this.objectInfo = new ObjectInfo(gm);
        this.teamInfo = new TeamInfo(this);

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
            int newID = spawnRobot(robot.type, newLocation, robot.team);

            initialBodies[i] = new RobotInfo(newID, robot.team, robot.type, 1, robot.health, newLocation);
        }

        // Write match header at beginning of match
        this.matchMaker.makeMatchHeader(this.gameMap);
    }

    /**
     * Run a single round of the game.
     *
     * @return the state of the game after the round has run.
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
            // destroyRobot(robot.getID());
            ; // Freeze robot instead of destroying it
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

    public int getLeadCount(MapLocation loc) {
        return this.leadCount[locationToIndex(loc)];
    }

    public int getGoldCount(MapLocation loc) {
        return this.goldCount[locationToIndex(loc)];
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

    public MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared) {
        ArrayList<MapLocation> returnLocations = new ArrayList<MapLocation>();
        int ceiledRadius = (int) Math.ceil(Math.sqrt(radiusSquared)) + 1; // add +1 just to be safe
        int minX = Math.max(center.x - ceiledRadius, this.gameMap.getOrigin().x);
        int minY = Math.max(center.y - ceiledRadius, this.gameMap.getOrigin().y);
        int maxX = Math.min(center.x + ceiledRadius, this.gameMap.getOrigin().x + this.gameMap.getWidth() - 1);
        int maxY = Math.min(center.y + ceiledRadius, this.gameMap.getOrigin().y + this.gameMap.getHeight() - 1);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                MapLocation newLocation = new MapLocation(x, y);
                if (center.isWithinDistanceSquared(newLocation, radiusSquared))
                    returnLocations.add(newLocation);
            }
        }
        return returnLocations.toArray(new MapLocation[0]);
    }

    /**
     * @return all of the locations on the grid.
     */
    private MapLocation[] getAllLocations() {
        return getAllLocationsWithinRadiusSquared(new MapLocation(0, 0), Integer.MAX_VALUE);
    }

    /**
     * @param cooldown without multiplier applied.
     * @param location with rubble of interest, if any
     * @return the cooldown due to rubble.
     */
    public int getCooldownWithMultiplier(int cooldown, MapLocation location) {
        return (int) ((1 + this.getRubble(location) / 10) * cooldown);
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
     * Sets the winner if one of the teams has been annihilated.
     *
     * @return whether or not a winner was set
     */
    public boolean setWinnerIfAnnihilated() {
        int robotCountA = objectInfo.getRobotCount(Team.A);
        int robotCountB = objectInfo.getRobotCount(Team.B);
        if (robotCountA == 0) {
            setWinner(Team.B, DominationFactor.ANNIHILATED);
            return true;
        } else if (robotCountB == 0) {
            setWinner(Team.A, DominationFactor.ANNIHILATED);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has more archons
     */
    public boolean setWinnerIfMoreArchons() {
        int archonCountA = objectInfo.getRobotTypeCount(Team.A, RobotType.ARCHON);
        int archonCountB = objectInfo.getRobotTypeCount(Team.B, RobotType.ARCHON);

        if (archonCountA > archonCountB) {
            setWinner(Team.A, DominationFactor.MORE_ARCHONS);
            return true;
        } else if (archonCountA < archonCountB) {
            setWinner(Team.B, DominationFactor.MORE_ARCHONS);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has a greater net Au value
     */
    public boolean setWinnerIfMoreGoldValue() {
        int[] totalInfluences = new int[2];
        for (InternalRobot robot : objectInfo.robotsArray()) {
            totalInfluences[robot.getTeam().ordinal()] += robot.getType().getGoldWorth(robot.getLevel());
        }
        if (totalInfluences[0] > totalInfluences[1]) {
            setWinner(Team.A, DominationFactor.MORE_GOLD_NET_WORTH);
            return true;
        } else if (totalInfluences[1] > totalInfluences[0]) {
            setWinner(Team.B, DominationFactor.MORE_GOLD_NET_WORTH);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has a greater net Pb value
     */
    public boolean setWinnerIfMoreLeadValue() {
        int[] totalInfluences = new int[2];
        for (InternalRobot robot : objectInfo.robotsArray()) {
            totalInfluences[robot.getTeam().ordinal()] += robot.getType().getLeadWorth(robot.getLevel());
        }
        if (totalInfluences[0] > totalInfluences[1]) {
            setWinner(Team.A, DominationFactor.MORE_LEAD_NET_WORTH);
            return true;
        } else if (totalInfluences[1] > totalInfluences[0]) {
            setWinner(Team.B, DominationFactor.MORE_LEAD_NET_WORTH);
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

    public void processEndOfRound() {
        // Add lead resources to the map
        if (this.currentRound == GameConstants.ADD_RESOURCE_EVERY_ROUNDS)
            for (int x = 0; x < gameMap.getWidth(); x++)
                for (int y = 0; y < gameMap.getHeight(); y++)
                    if (gameMap.getLeadAtLocation(x, y) >= 1) 
                        gameMap.addLeadAtLocation(x, y, 5);

        // Add lead resources to the team
        teamInfo.changeLead(teamInfo.getLead() + GameConstants.PASSIVE_LEAD_INCREASE);

        // Process end of each robot's round (currently empty in InternalRobot)
        objectInfo.eachRobot((robot) -> {
            robot.processEndOfRound();
            return true;
        });

        // Trigger any anomalies
        // note: singularity is handled below in the "check for end of match"
        if (this.gameMap.viewNextAnomaly().roundNumber == this.currentRound) {
            AnomalyType anomaly = this.gameMap.takeNextAnomaly().anomalyType;
            if (anomaly == AnomalyType.ABYSS) this.causeAbyssGlobal(anomaly);
            if (anomaly == AnomalyType.CHARGE) this.causeChargeGlobal(anomaly);
            if (anomaly == AnomalyType.FURY) this.causeFuryGlobal(anomaly);
            // TODO: uncomment this whenever causeVortexGlobal is called.
            // if (anomaly == AnomalyType.VORTEX) this.causeVortexGlobal(anomaly);
        }

        // Check for end of match
        setWinnerIfAnnihilated();
        if (timeLimitReached() && gameStats.getWinner() == null)
            if (!setWinnerIfMoreArchons())
                if (!setWinnerIfMoreGoldValue())
                    if (!setWinnerIfMoreLeadValue())
                        setWinnerArbitrary();

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

        InternalRobot robot = objectInfo.getRobotByID(id);
        removeRobot(robot.getLocation());
        
        int leadDropped = robot.getType().getLeadDropped(robot.getLevel());
        int goldDropped = robot.getType().getGoldDropped(robot.getLevel());
        
        this.leadCount[locationToIndex(robot.getLocation())] += leadDropped;
        this.goldCount[locationToIndex(robot.getLocation())] += goldDropped;

        controlProvider.robotKilled(robot);
        objectInfo.destroyRobot(id);

        matchMaker.addDied(id);
    }

    // *********************************
    // ******* PROFILER **************
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
     * @param robot that is causing the anomaly. Must be a Sage.
     * @return all of the locations that are within range of this sage.
     */
    private MapLocation[] getSageActionLocations(InternalRobot robot) {
        assert robot.getType() == RobotType.SAGE;
        MapLocation center = robot.getLocation();
        return getAllLocationsWithinRadiusSquared(center, robot.getType().getActionRadiusSquared(robot.getLevel()));
    }

    /**
     * Performs the Abyss anomaly.
     *   Changes the resources in the squares and the team.
     * @param reduceFactor associated with anomaly (a decimal percentage)
     * @param locations that can be affected by the Abyss.
     */
    private void causeAbyssGridUpdate(float reduceFactor, MapLocation[] locations) {
        for (int i = 0; i < locations.length; i++) {
            MapLocation currentLocation = locations[i];
            int x = currentLocation.x;
            int y = currentLocation.y;

            int currentLead = gameMap.getLeadAtLocation(x, y);
            int leadUpdate = (int) (reduceFactor * currentLead);
            gameMap.setLeadAtLocation(x, y, currentLead - leadUpdate);

            int currentGold = gameMap.getGoldAtLocation(x, y);
            int goldUpdate = (int) (reduceFactor * currentGold);
            gameMap.setLeadAtLocation(x, y, currentGold - goldUpdate);
        }
    }

    /**
     * Performs the Fury anomaly.
     *   Changes the health of the relevant robots.
     * @param reduceFactor associated with anomaly (a decimal percentage)
     * @param locations that can be affected by the Fury (by radius, not by state of robot)
     */
    public void causeFuryUpdate(float reduceFactor, MapLocation[] locations) {
        for (int i = 0; i < locations.length; i++) {
            InternalRobot robot = this.getRobot(locations[i]);
            if (robot.getType().isBuilding() && robot.getMode() == RobotMode.TURRET) {
                robot.addHealth((int) (-1 * robot.getType().getMaxHealth(robot.getLevel()) * reduceFactor));
            }
        }
    }

    /**
     * Mutates state to perform the Sage Abyss anomaly.
     * @param robot that is the Sage
     * @param anomaly that corresponds to Abyss type
     */
    public void causeAbyssSage(InternalRobot robot, AnomalyType anomaly) {
        assert robot.getType() == RobotType.SAGE;
        assert anomaly == AnomalyType.ABYSS;
        // calculate the right effect range
        this.causeAbyssGridUpdate(anomaly.sagePercentage, this.getSageActionLocations(robot));
    }

    /**
     * Mutates state to perform the global Abyss anomaly.
     * @param anomaly that corresponds to Abyss type
     */
    public void causeAbyssGlobal(AnomalyType anomaly) {
        assert anomaly == AnomalyType.ABYSS;
        this.causeAbyssGridUpdate(anomaly.globalPercentage, this.getAllLocations());
        // change team resources
        teamInfo.changeLead((int) (-1 * anomaly.globalPercentage * teamInfo.getLead()));
        teamInfo.changeGold((int) (-1 * anomaly.globalPercentage * teamInfo.getGold()));
    }

    /**
     * Mutates state to perform the Sage Fury.
     * @param robot performing the Fury, must be a Sage
     * @param anomaly that corresponds to Fury type
     */
    public void causeFurySage(InternalRobot robot, AnomalyType anomaly) {
        assert robot.getType() == RobotType.SAGE;
        assert anomaly == AnomalyType.FURY;
        this.causeFuryUpdate(anomaly.sagePercentage, this.getSageActionLocations(robot));
    }

    /**
     * Mutates state to peform the global Fury.
     * @param anomaly that corresponds to Fury type
     */
    public void causeFuryGlobal(AnomalyType anomaly) {
        assert anomaly == AnomalyType.FURY;
        this.causeFuryUpdate(anomaly.globalPercentage, this.getAllLocations());
    }

    /**
     * Mutates state to perform the Sage Charge.
     * @param robot performing the Charge, must be a Sage
     * @param anomaly that corresponds to Charge type
     */
    public void causeChargeSage(InternalRobot robot, AnomalyType anomaly) {
        assert robot.getType() == RobotType.SAGE;
        assert anomaly == AnomalyType.CHARGE;

        MapLocation[] actionLocations = this.getSageActionLocations(robot);
        for (int i = 0; i < actionLocations.length; i++) {
            InternalRobot currentRobot = getRobot(actionLocations[i]);
            currentRobot.addHealth((int) (-1 * anomaly.sagePercentage * currentRobot.getType().getMaxHealth(currentRobot.getLevel())));
        }
    }

    /**
     * Mutates state to peform the global Charge.
     * @param anomaly that corresponds to Charge type
     */
    public void causeChargeGlobal(AnomalyType anomaly) {
        assert anomaly == AnomalyType.CHARGE;

        ArrayList<InternalRobot> rawDroids = new ArrayList<InternalRobot>();
        for (InternalRobot currentRobot : this.objectInfo.robotsArray())
            if (!currentRobot.getType().isBuilding())
                rawDroids.add(currentRobot);

        InternalRobot[] droids = rawDroids.toArray(new InternalRobot[rawDroids.size()]);
        Arrays.sort(
            droids,  
            (InternalRobot robot1, InternalRobot robot2) -> 
                // 12/10/21: https://www.geeksforgeeks.org/arrays-sort-in-java-with-examples/
                (robot2.numberOfVisibleFriendlyRobots() - robot1.numberOfVisibleFriendlyRobots())
                // end reference on how to compare things with subtraction
        );

        int affectedDroidsLimit = (int) (anomaly.globalPercentage * droids.length);
        for (int i = 0; i < affectedDroidsLimit; i++) {
            this.destroyRobot(droids[i].getID());
        }
    }
}

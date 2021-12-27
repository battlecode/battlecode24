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
    
    private int[] rubble;
    private int[] lead;
    private int[] gold;
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
        this.rubble = gm.getRubbleArray();
        this.lead = gm.getLeadArray();
        this.gold = new int[this.lead.length];
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
        int numArchons = 0;
        RobotInfo[] initialBodies = this.gameMap.getInitialBodies();
        for (int i = 0; i < initialBodies.length; i++) {
            RobotInfo robot = initialBodies[i];
            MapLocation newLocation = robot.location.translate(gm.getOrigin().x, gm.getOrigin().y);
            int newID = spawnRobot(robot.type, newLocation, robot.team);
            initialBodies[i] = new RobotInfo(newID, robot.team, robot.type, 1, robot.health, newLocation);
            if (robot.team == Team.A && robot.type == RobotType.ARCHON)
                numArchons++;
        }
        this.teamInfo = new TeamInfo(this);

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
            }
            else {
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

    public void setWinner(Team t, DominationFactor d)  {
        gameStats.setWinner(t);
        gameStats.setDominationFactor(d);
    }

    /**
     * @return whether a team has more archons
     */
    public boolean setWinnerIfMoreArchons() {
        int archonCountA = this.objectInfo.getRobotTypeCount(Team.A, RobotType.ARCHON);
        int archonCountB = this.objectInfo.getRobotTypeCount(Team.B, RobotType.ARCHON);

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
        int[] totalGoldValues = new int[2];
        for (InternalRobot robot : objectInfo.robotsArray()) {
            totalGoldValues[robot.getTeam().ordinal()] += robot.getType().getGoldWorth(robot.getLevel());
        }
        if (totalGoldValues[0] > totalGoldValues[1]) {
            setWinner(Team.A, DominationFactor.MORE_GOLD_NET_WORTH);
            return true;
        } else if (totalGoldValues[1] > totalGoldValues[0]) {
            setWinner(Team.B, DominationFactor.MORE_GOLD_NET_WORTH);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has a greater net Pb value
     */
    public boolean setWinnerIfMoreLeadValue() {
        int[] totalLeadValues = new int[2];
        for (InternalRobot robot : objectInfo.robotsArray()) {
            totalLeadValues[robot.getTeam().ordinal()] += robot.getType().getLeadWorth(robot.getLevel());
        }
        if (totalLeadValues[0] > totalLeadValues[1]) {
            setWinner(Team.A, DominationFactor.MORE_LEAD_NET_WORTH);
            return true;
        } else if (totalLeadValues[1] > totalLeadValues[0]) {
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
        if (this.currentRound % GameConstants.ADD_LEAD_EVERY_ROUNDS == 0)
            for (int i = 0; i < this.lead.length; i++)
                if (this.lead[i] > 0) 
                    this.lead[i] += GameConstants.ADD_LEAD;

        // Add lead resources to the team
        this.teamInfo.addLead(Team.A, GameConstants.PASSIVE_LEAD_INCREASE);
        this.teamInfo.addLead(Team.B, GameConstants.PASSIVE_LEAD_INCREASE);

        // Process end of each robot's round
        objectInfo.eachRobot((robot) -> {
            robot.processEndOfRound();
            return true;
        });

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

        // Check for end of match
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
        RobotType type = robot.getType();
        Team team = robot.getTeam();
        removeRobot(robot.getLocation());
        
        int leadDropped = robot.getType().getLeadDropped(robot.getLevel());
        int goldDropped = robot.getType().getGoldDropped(robot.getLevel());
        
        this.lead[locationToIndex(robot.getLocation())] += leadDropped;
        this.gold[locationToIndex(robot.getLocation())] += goldDropped;

        controlProvider.robotKilled(robot);
        objectInfo.destroyRobot(id);

        // this happens here because both teams' Archons can die in the same round
        if (type == RobotType.ARCHON && this.objectInfo.getRobotTypeCount(team, RobotType.ARCHON) == 0)
            setWinner(team == Team.A ? Team.B : Team.A, DominationFactor.ANNIHILATION);

        matchMaker.addDied(id);
    }

    // *********************************
    // *******  PROFILER  **************
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
        return getAllLocationsWithinRadiusSquared(center, robot.getType().getActionRadiusSquared(robot.getLevel()));
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

            int currentGold = getGold(locations[i]);
            int goldUpdate = (int) (reduceFactor * currentGold);
            setGold(locations[i], currentGold - goldUpdate);
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
        this.matchMaker.addAction(-1, ABYSS, -1);
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
            if (currentRobot != null && currentRobot.getTeam() != robot.getTeam())
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
        this.matchMaker.addAction(-1, CHARGE, -1);
    }

    /** Used to sort droids for charge */
    class SortByFriends implements Comparator<InternalRobot> {
        public int compare(InternalRobot a, InternalRobot b) {
            return a.getNumVisibleFriendlyRobots(false) - b.getNumVisibleFriendlyRobots(false);
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
            if (robot.getMode() == RobotMode.TURRET) {
                robot.addHealth((int) (-1 * robot.getType().getMaxHealth(robot.getLevel()) * reduceFactor));
            }
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
        this.matchMaker.addAction(-1, FURY, -1);
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
            case HORIZONTAL:
                flipRubbleVertically();
                changeIdx = 2;
                break;
            case VERTICAL:
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
        this.matchMaker.addAction(-1, VORTEX, changeIdx);
    }
}

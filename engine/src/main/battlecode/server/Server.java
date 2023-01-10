package battlecode.server;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.world.*;
import battlecode.world.control.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Runs matches. Specifically, this class forms a pipeline connecting match and
 * configuration parameters to the game engine and engine output to an abstract
 * match data sink.
 */
public strictfp class Server implements Runnable {
    /**
     * The GameInfo that signals the server to terminate when it is encountered on the game queue.
     */
    private static final GameInfo POISON = new GameInfo(null, null, null, null, null, null, null, null, false) {};

    /**
     * The queue of games to run.
     * When the server encounters the GameInfo POISON, it terminates.
     */
    private final BlockingQueue<GameInfo> gameQueue;

    /**
     * The state of the match that the server is running (or about to run).
     */
    private ServerState state;

    /**
     * The options provided to the server via config file and command line.
     */
    private final Config options;

    /**
     * Whether to wait for notifications to control match run state, or to just
     * run all matches immediately.
     */
    private final boolean interactive;

    /**
     * The GameWorld the server is currently operating on.
     */
    private GameWorld currentWorld;


    /**
     * The server's mode affects how notifications are handled, whether or not
     * an RPC server is set up, and which controllers are chosen for server
     * operation.
     */
    public enum Mode {
        HEADLESS,
    }

    /**
     * Initializes a new server.
     *
     * @param options the configuration to use
     * @param interactive whether to wait for notifications to control the
     *                    match run state
     */
    public Server(Config options, boolean interactive) {
        this.gameQueue = new LinkedBlockingQueue<>();

        this.interactive = interactive;

        this.options = options;
        this.state = ServerState.NOT_READY;
    }

    // ******************************
    // ***** NOTIFICATIONS **********
    // ******************************

    public void startNotification() {
        state = ServerState.READY;
    }

    public void pauseNotification() {
        state = ServerState.PAUSED;
    }

    public void resumeNotification() {
        if (state == ServerState.PAUSED) {
            state = ServerState.RUNNING;
        }
    }

    public void runNotification() {
        if (state != ServerState.PAUSED) {
            state = ServerState.RUNNING;
        }
    }

    public void addGameNotification(GameInfo gameInfo) {
        this.gameQueue.add(gameInfo);
    }

    public void terminateNotification() {
        this.gameQueue.add(POISON);
    }

    // ******************************
    // ***** SIMULATION METHODS *****
    // ******************************

    /**
     * Runs the server. The server will wait for some game info (which
     * specifies the teams and set of maps to run) and then begin running
     * matches.
     */
    public void run() {
        final NetServer netServer;
        if (options.getBoolean("bc.server.websocket")) {
            netServer = new NetServer(options.getInt("bc.server.port"),
                                      options.getBoolean("bc.server.wait-for-client"));
            netServer.start();
        } else {
            netServer = null;
        }

        while (true) {
            final GameInfo currentGame;
            debug("Awaiting match");
            try {
                currentGame = gameQueue.take();
            } catch (InterruptedException e) {
                warn("Interrupted while waiting for next game!");
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return;
            }

            // Note: ==, not .equals()
            if (currentGame == POISON) {
                debug("Shutting down server");
                if (netServer != null) {
                    netServer.finish();
                }
                return;
            }

            GameMaker gameMaker = new GameMaker(currentGame, netServer, options.getBoolean("bc.engine.show-indicators"));
            gameMaker.makeGameHeader();

            debug("Running: "+currentGame);

            // Set up our control provider
            final boolean profilingEnabled = options.getBoolean("bc.engine.enable-profiler");
            final RobotControlProvider prov = createControlProvider(currentGame, gameMaker, profilingEnabled);

            // Count wins
            int aWins = 0, bWins = 0;

            // Loop through the maps in the current game
            for (int matchIndex = 0; matchIndex < currentGame.getMaps().length; matchIndex++) {

                Team winner;
                try {
                    winner = runMatch(currentGame, matchIndex, prov, gameMaker);
                } catch (Exception e) {
                    ErrorReporter.report(e);
                    this.state = ServerState.ERROR;
                    return;
                }

                switch (winner) {
                    case A:
                        aWins++;
                        break;
                    case B:
                        bWins++;
                        break;
                    default:
                        warn("Team "+winner+" won???");
                }

                currentWorld = null;

                if (currentGame.isBestOfThree()) {
                    if (aWins == 2 || bWins == 2) {
                        break;
                    }
                }
            }
            Team winner = aWins >= bWins ? Team.A : Team.B;
            gameMaker.makeGameFooter(winner);
            gameMaker.writeGame(currentGame.getSaveFile());
        }
    }

    private int locationToIndex(LiveMap liveMap, int x, int y) {
        return x + y * liveMap.getWidth();
    }

    private int locationToIndex(LiveMap liveMap, MapLocation loc) {
        return loc.x + loc.y * liveMap.getWidth();
    }

    public boolean onTheMap(LiveMap liveMap, MapLocation loc) {
        return loc.x >= 0 && loc.y >= 0 && loc.x < liveMap.getWidth() && loc.y < liveMap.getHeight();
    }

    public MapLocation indexToLocation(LiveMap liveMap, int idx) {
        return new MapLocation(idx % liveMap.getWidth(),
                               idx / liveMap.getWidth());
    }
    private void validateMapOnGuarantees(LiveMap liveMap) {
        // Check map dimensions
        if (liveMap.getWidth() > GameConstants.MAP_MAX_WIDTH) {
            throw new RuntimeException("MAP WIDTH EXCEEDS GameConstants.MAP_MAX_WIDTH");
        }
        if (liveMap.getWidth() < GameConstants.MAP_MIN_WIDTH) {
            throw new RuntimeException("MAP WIDTH BENEATH GameConstants.MAP_MIN_WIDTH");
        }
        if (liveMap.getHeight() > GameConstants.MAP_MAX_HEIGHT) {
            throw new RuntimeException("MAP HEIGHT EXCEEDS GameConstants.MAP_MAX_HEIGHT");
        }
        if (liveMap.getHeight() < GameConstants.MAP_MIN_HEIGHT) {
            throw new RuntimeException("MAP HEIGHT BENEATH GameConstants.MAP_MIN_HEIGHT");
        }
        
        RobotInfo[] robotArray = new RobotInfo[liveMap.getHeight()*liveMap.getWidth()];
        int headquarterCount = 0;
        for (RobotInfo robotInfo : liveMap.getInitialBodies()) {
            if (robotInfo.type == RobotType.HEADQUARTERS) headquarterCount++;
            else throw new RuntimeException("All initial robots should be headquarters");
            MapLocation robotLocation = robotInfo.getLocation();
            int idx = locationToIndex(liveMap, robotLocation);
            if (robotArray[idx] != null) {
                throw new RuntimeException("Multiple robots can not be in the same place");
            }
            robotArray[idx] = robotInfo;
        }

        // Check starting Headquarters
        if (headquarterCount < GameConstants.MIN_STARTING_HEADQUARTERS * 2) {
            throw new RuntimeException("HEADQUARTERS num of " + headquarterCount + " BENEATH GameConstants.MIN_STARTING_HEADQUARTERS");
        }
        if (headquarterCount > GameConstants.MAX_STARTING_HEADQUARTERS * 8) {
            throw new RuntimeException("HEADQUARTERS num of " + headquarterCount + " EXCEEDS GameConstants.MAX_STARTING_HEADQUARTERS");
        }

        //assert that walls are not on same location as resources/islands/currents/clouds
        for (int i = 0; i < liveMap.getWidth()*liveMap.getHeight(); i++){
            if (liveMap.getWallArray()[i]){
                if (liveMap.getCloudArray()[i])
                    throw new RuntimeException("Walls cannot be on the same square as clouds");
                if (liveMap.getResourceArray()[i] != 0)
                    throw new RuntimeException("Walls cannot be on the same square as resources");
                if (liveMap.getIslandArray()[i] != 0)
                    throw new RuntimeException("Walls cannot be on an island");
                if (liveMap.getCurrentArray()[i] != 0)
                    throw new RuntimeException("Walls cannot be on the same square as currents");
                if (robotArray[i] != null)
                    throw new RuntimeException("Walls cannot be on the same square as headquarters");
            }
            //assert that clouds and currents cannot be on the same square
            if (liveMap.getCloudArray()[i] && (liveMap.getCurrentArray()[i] != 0))
                throw new RuntimeException("Clouds and currents cannot be on the same square");

            //assert that wells are not on same square as headquarters
            if ((liveMap.getResourceArray()[i] != 0) && (robotArray[i] != null))
                throw new RuntimeException("Wells can't be on same square as headquarters");

            //assert that currents are not on same square as headquarters
            if (liveMap.getCurrentArray()[i] != 0 && robotArray[i] != null)
                throw new RuntimeException("Currents can't be on same square as headquarters");
        }

        //assert that island guarantees are met (atleast 4 islands, none of which are larger than 20 units)
        Map<Integer, Integer> islandToAreaMapping = new HashMap<>();
        for (int i : liveMap.getIslandArray()) {
            if (i == 0) {
                continue; // No island
            } else {
                islandToAreaMapping.put(i, islandToAreaMapping.getOrDefault(i, 0) + 1);
            }
        }
        if (islandToAreaMapping.size() < GameConstants.MIN_NUMBER_ISLANDS) {
            throw new RuntimeException("Islands num of " + islandToAreaMapping.size() + " BENEATH GameConstants.MIN_NUMBER_ISLANDS");
        }
        for (int i : islandToAreaMapping.values()) {
            if (i > GameConstants.MAX_ISLAND_AREA) {
                throw new RuntimeException("Island exceeds max allowable area");
            }
        }


        //assert that at least one adamantium well is visible to each team
        boolean[] hasVisibleAdamantium = new boolean[2];
        for (RobotInfo r : liveMap.getInitialBodies()){
            int teamOrdinal = r.getTeam().ordinal();
            if (hasVisibleAdamantium[teamOrdinal]) continue;

            MapLocation[] visibleLocations = GameWorld.getAllLocationsWithinRadiusSquaredWithoutMap(
                liveMap.getOrigin(), 
                liveMap.getWidth(), 
                liveMap.getHeight(), 
                r.getLocation(),
                r.getType().visionRadiusSquared);
            for (MapLocation loc : visibleLocations){
                if (ResourceType.values()[liveMap.getResourceArray()[locationToIndex(liveMap, loc)]] == ResourceType.ADAMANTIUM){
                    hasVisibleAdamantium[teamOrdinal] = true;
                }
            }
        } 
        if (!(hasVisibleAdamantium[0] && hasVisibleAdamantium[1])){
            throw new RuntimeException("Teams must have at least one adamantium well visible.");
        }

        //assert that adamantium wells and mana well are close enough together
        Set<MapLocation> adWells = new HashSet<>();
        Set<MapLocation> mnWells = new HashSet<>();
        for (int i = 0; i < liveMap.getWidth()*liveMap.getHeight(); i++){
            int rType = liveMap.getResourceArray()[i];
            assert(rType < ResourceType.values().length);
            switch (ResourceType.values()[rType]) {
                case ADAMANTIUM:
                    adWells.add(indexToLocation(liveMap, i));
                    break;
                case MANA:
                    mnWells.add(indexToLocation(liveMap, i));
                    break;
                case NO_RESOURCE:
                    break;
                default:
                    throw new RuntimeException("Initial map can only have Adamantium and Mana wells");
            }
        }

        for (MapLocation adWellLoc : adWells) {
            boolean wellWithinRange = false;
            for (MapLocation mnWellLoc : mnWells) {
                if (adWellLoc.isWithinDistanceSquared(mnWellLoc, GameConstants.MAX_DISTANCE_BETWEEN_WELLS)) {
                    wellWithinRange = true;
                    break;
                }
            }
            if (!wellWithinRange) {
                throw new RuntimeException("Adamantium well at " + adWellLoc + " is not within range of any mana wells.");
            }
        }

        for (MapLocation mnWellLoc : mnWells) {
            boolean wellWithinRange = false;
            for (MapLocation adWellLoc : adWells) {
                if (mnWellLoc.isWithinDistanceSquared(adWellLoc, GameConstants.MAX_DISTANCE_BETWEEN_WELLS)) {
                    wellWithinRange = true;
                    break;
                }
            }
            if (!wellWithinRange) {
                throw new RuntimeException("Mana well at " + mnWellLoc + " is not within range of any adamantium wells.");
            }
        }

        int maxNumWells = (int) (liveMap.getHeight()*liveMap.getWidth()*GameConstants.MAX_MAP_PERCENT_WELLS);
        if (adWells.size() > maxNumWells) {
            throw new RuntimeException("There are too many AD wells. It exceeds GameConstants.MAX_MAP_PERCENT_WELLS percent of the map.");
        }
        if (mnWells.size() > maxNumWells) {
            throw new RuntimeException("There are too many MN wells. It exceeds GameConstants.MAX_MAP_PERCENT_WELLS percent of the map.");
        }
    
        
        //assert that no two currents end on the same square (avoid robot collisions)
        HashSet<MapLocation> endingLocations = new HashSet<MapLocation>();
        int[] currentArray = liveMap.getCurrentArray();
        for (int i = 0; i < currentArray.length; i++){
            if (currentArray[i] != 0){
                MapLocation startLocation = indexToLocation(liveMap, i);
                Direction currentDir = Direction.DIRECTION_ORDER[currentArray[i]];
                MapLocation finalLocation = startLocation.add(currentDir);
                if (!onTheMap(liveMap, finalLocation))
                    throw new RuntimeException("Current directs robots outside of the bounds of the map");
                if (liveMap.getWallArray()[locationToIndex(liveMap, finalLocation.x, finalLocation.y)])
                    throw new RuntimeException("Current directs robots into wall");
                boolean unique = endingLocations.add(finalLocation);
                if (!unique)
                    throw new RuntimeException("Two different currents direct robots to the same location: " + finalLocation);
            }
        }
    }

    /**
     * @return the winner of the match
     * @throws Exception if the match fails to run for some reason
     */
    private Team runMatch(GameInfo currentGame,
                          int matchIndex,
                          RobotControlProvider prov,
                          GameMaker gameMaker) throws Exception {

        final String mapName = currentGame.getMaps()[matchIndex];
        final LiveMap loadedMap;

        try {
            loadedMap = GameMapIO.loadMap(mapName, new File(options.get("bc.game.map-path")));
        } catch (IOException e) {
            warn("Couldn't load map " + mapName + ", skipping");
            throw e;
        }

        // Create the game world!
        currentWorld = new GameWorld(loadedMap, prov, gameMaker.getMatchMaker());
        
        // Validate the map
        validateMapOnGuarantees(currentWorld.getGameMap());

        // Get started
        if (interactive) {
            // TODO necessary?
            // Poll for RUNNING, if we're in interactive mode
            while (!ServerState.RUNNING.equals(state)) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {}
            }
        } else {
            // Start the game immediately if we're not in interactive mode
            this.state = ServerState.RUNNING;
        }

        long startTime = System.currentTimeMillis();
        say("-------------------- Match Starting --------------------");
        say(String.format("%s vs. %s on %s", currentGame.getTeamAPackage(), currentGame.getTeamBPackage(), mapName));

        // If there are more rounds to be run, run them and
        // and send the round (and optionally stats) bytes to
        // recipients.
        while (this.state != ServerState.FINISHED) {

            // If not paused/stopped:
            switch (this.state) {

                case RUNNING:
                    GameState state = currentWorld.runRound();

                    if (GameState.DONE.equals(state)) {
                        this.state = ServerState.FINISHED;
                        break;
                    }

                    break;

                case PAUSED:
                    Thread.sleep(250);
                    break;
            }
        }

        say(getWinnerString(currentGame, currentWorld.getWinner(), currentWorld.getCurrentRound()));
        say("-------------------- Match Finished --------------------");

        double timeDiff = (System.currentTimeMillis() - startTime) / 1000.0;
        debug(String.format("match completed in %.4g seconds", timeDiff));
        return currentWorld.getWinner();
    }

    // ******************************
    // ***** CREATOR METHODS ********
    // ******************************

    /**
     * Create a RobotControlProvider for a game.
     *
     * @param game             the game to provide control for
     * @param gameMaker        the game maker containing the output streams for robot logs
     * @param profilingEnabled whether profiling is enabled or not
     * @return a fresh control provider for the game
     */
    private RobotControlProvider createControlProvider(GameInfo game,
                                                       GameMaker gameMaker,
                                                       boolean profilingEnabled) {
        // Strictly speaking, this should probably be somewhere in battlecode.world
        // Whatever

        final TeamControlProvider teamProvider = new TeamControlProvider();

        teamProvider.registerControlProvider(
                Team.A,
                new PlayerControlProvider(
                    Team.A,
                    game.getTeamAPackage(),
                    game.getTeamAURL(),
                    gameMaker.getMatchMaker().getOut(),
                    profilingEnabled
                )
        );
        teamProvider.registerControlProvider(
                Team.B,
                new PlayerControlProvider(
                    Team.B,
                    game.getTeamBPackage(),
                    game.getTeamBURL(),
                    gameMaker.getMatchMaker().getOut(),
                    profilingEnabled
                )
        );
        teamProvider.registerControlProvider(
            Team.NEUTRAL,
            new NullControlProvider()
        );
        return teamProvider;
    }

    // ******************************
    // ***** GETTER METHODS *********
    // ******************************

    /**
     * @return the state of the game
     */
    public ServerState getState() {
        return this.state;
    }

    /**
     * Produces a string for the winner of the match.
     *
     * @return A string representing the match's winner.
     */
    public String getWinnerString(GameInfo game, Team winner, int roundNumber) {

        String teamName;

        switch (winner) {
            case A:
                teamName = game.getTeamAPackage() + " (A)";
                break;

            case B:
                teamName = game.getTeamBPackage() + " (B)";
                break;

            default:
                teamName = "nobody";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < (50 - teamName.length()) / 2; i++)
            sb.append(' ');
        sb.append(teamName);
        sb.append(" wins (round ").append(roundNumber).append(")");

        sb.append("\nReason: ");
        GameStats stats = currentWorld.getGameStats();
        DominationFactor dom = stats.getDominationFactor();

        switch (dom) {
            case CONQUEST:
                sb.append("The winning team won by capturing 75% of sky islands.");
                break;
            case MORE_SKY_ISLANDS:
                sb.append("The winning team won by having more sky islands.");
                break;
            case MORE_REALITY_ANCHORS:
                sb.append("The winning team won on tiebreakers (more reality anchors).");
                break;
            case MORE_ELIXIR_NET_WORTH:
                sb.append("The winning team won on tiebreakers (more elixir net worth).");
                break;
            case MORE_MANA_NET_WORTH:
                sb.append("The winning team won on tiebreakers (more mana net worth).");
                break;
            case MORE_ADAMANTIUM_NET_WORTH:
                sb.append("The winning team won on tiebreakers (more adamantium net worth).");
                break;
            case WON_BY_DUBIOUS_REASONS:
                sb.append("The winning team won arbitrarily (coin flip).");
                break;
            case RESIGNATION:
                sb.append("Other team has resigned. Congrats on scaring them I guess...");
                break;
        }

        return sb.toString();
    }

    /**
     * @return whether we are actively running a match
     */
    public boolean isRunningMatch() {
        return currentWorld != null && currentWorld.isRunning();
    }


    // ******************************
    // ***** CONSOLE MESSAGES *******
    // ******************************


    /**
     * This method is used to display warning messages with formatted output.
     *
     * @param msg the warning message to display
     */
    public static void warn(String msg) {
        for (String line : msg.split("\n")) {
            System.out.printf("[server:warning] %s\n", line);
        }
    }

    /**
     * This method is used to display "official" formatted messages from the
     * server.
     *
     * @param msg the message to display
     */
    public static void say(String msg) {
        for (String line : msg.split("\n")) {
            System.out.printf("[server] %s\n", line);
        }
    }

    /**
     * This method is used to display debugging messages with formatted output.
     *
     * @param msg the debug message to display
     */
    public static void debug(String msg) {
        if (Config.getGlobalConfig().getBoolean("bc.server.debug")) {
            for (String line : msg.split("\n")) {
                System.out.printf("[server:debug] %s\n", line);
            }
        }
    }
}

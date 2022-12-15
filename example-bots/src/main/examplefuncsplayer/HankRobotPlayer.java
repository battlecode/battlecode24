package examplefuncsplayer;
import battlecode.common.*;
import java.util.Random;
import java.util.Map;
import java.util.Collection;

public strictfp class HankRobotPlayer {
    
    static final Random rng = new Random(5844);

    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    private enum State {
        GOTO_WELL,
        GOTO_ISLAND,
        RETURNING,
        COLLECTING,
        DEPOSITING
    }

    static int turnCount = 0;
    static MapLocation headquarterLoc = null;
    
    static boolean madeCarrier = false;

    static MapLocation wellLoc = null;
    static MapLocation islandLoc = null;
    static State state = State.GOTO_WELL;

    public static void run(RobotController rc) {
        while(true) {
            turnCount++;
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                if(turnCount == 1) {
                    init(rc);
                }
                switch (rc.getType()) {
                    case HEADQUARTERS: runHeadquarters(rc);  break;
                    case CARRIER: runCarrierWells(rc);   break;
                    case LAUNCHER: 
                    case BOOSTER: // Examplefuncsplayer doesn't use any of these robot types below.
                    case DESTABILIZER: // You might want to give them a try!
                    case AMPLIFIER: break;
                }
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
        }
    }

    /**
     * Called when the robot has just spawned, stores location of headquarters
     * @param rc
     * @throws GameActionException
     */
    private static void init(RobotController rc) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        for (RobotInfo robot : robots) {
            if (robot.getType() == RobotType.HEADQUARTERS) headquarterLoc = robot.getLocation();
        }
    }

    private static void runHeadquarters(RobotController rc) throws GameActionException {
        // System.out.println("Running headquarters");
        MapLocation buildLocation = rc.getLocation().add(Direction.EAST);
        if (!madeCarrier && rc.canBuildRobot(RobotType.CARRIER, buildLocation)) {
            rc.buildRobot(RobotType.CARRIER, buildLocation);
            madeCarrier = true;
        }
        else if(rc.canBuildAnchor(Anchor.STANDARD)) {
            System.out.println("Building anchor!");
            rc.buildAnchor(Anchor.STANDARD);
        }
    }

    private static void runCarrierWells(RobotController rc) throws GameActionException{
        if(state == State.RETURNING){
            boolean inRange = tryDeposit(rc);
            if(inRange) state = State.DEPOSITING;
            else moveTowardsHQ(rc);
        }
        else if(state == State.DEPOSITING){
            boolean successful = tryDeposit(rc);
            if(!successful) {
                System.out.println("done depositing");
                if(rc.canTakeAnchor(headquarterLoc, Anchor.STANDARD)){
                    System.out.println("Taking anchor!");
                    rc.takeAnchor(headquarterLoc, Anchor.STANDARD);
                    state = State.GOTO_ISLAND;
                }
                else {
                    state = State.GOTO_WELL;
                    moveOrRandom(rc, wellLoc);
                }
            }
        }
        else if(state == State.GOTO_WELL){
            boolean inRange = tryCollect(rc);
            if(inRange) state = State.COLLECTING;
            else moveOrRandom(rc, headquarterLoc);
        }
        else if(state == State.COLLECTING){
            boolean successfull = tryCollect(rc);
            if(!successfull) {
                state = State.RETURNING;
                moveTowardsHQ(rc);
            }
        }
        else if(state == State.GOTO_ISLAND) {
            if(islandLoc == null) scanIslands(rc);
            moveOrRandom(rc, islandLoc);
            if(rc.getLocation().equals(islandLoc)) {
                if(rc.canPlaceAnchor()) {
                    rc.placeAnchor();
                    System.out.println("PLACED AN ANCHOR YAYAYAYAY");
                }
            }
        }
    }

    private static void scanIslands(RobotController rc) throws GameActionException {
        Map<Integer, MapLocation[]> islandLocs = rc.senseNearbyIslandLocations();
        if(islandLocs.keySet().size() > 0){
            islandLoc = islandLocs.get(islandLocs.keySet().toArray()[0])[0];
        }
    }

    private static boolean tryDeposit(RobotController rc) throws GameActionException{
        boolean didTransfer = false;
        if (rc.canTransferResource(headquarterLoc, ResourceType.ADAMANTIUM, 40)) {
            rc.transferResource(headquarterLoc, ResourceType.ADAMANTIUM, 40);
            didTransfer = true;
        }
        if (rc.canTransferResource(headquarterLoc, ResourceType.ELIXIR, 40)) {
            rc.transferResource(headquarterLoc, ResourceType.ELIXIR, 40);
            didTransfer = true;
        }
        if (rc.canTransferResource(headquarterLoc, ResourceType.MANA, 40)) {
            rc.transferResource(headquarterLoc, ResourceType.MANA, 40);
            didTransfer = true;
        }
        return didTransfer;
    }

    private static boolean tryCollect(RobotController rc) throws GameActionException {
        Well[] wells = rc.senseNearbyWells();
        boolean didCollect = false;
        if(wells.length > 0){
            wellLoc = wells[0].getMapLocation();
            while (rc.canCollectResource(wellLoc, -1)) {
                rc.collectResource(wellLoc, -1);
                didCollect = true;
            }
        }
        return didCollect;
    }

    private static void moveOrRandom(RobotController rc, MapLocation moveTo) throws GameActionException{
        MapLocation loc = rc.getLocation();
        Direction dir;
        if(moveTo != null) dir = loc.directionTo(moveTo);
        else dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) rc.move(dir);
    }

    private static void moveTowardsHQ(RobotController rc) throws GameActionException {
        MapLocation loc = rc.getLocation();
        Direction dir = loc.directionTo(headquarterLoc);
        if(rc.canMove(dir)) rc.move(dir);
    }
}

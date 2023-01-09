package examplefuncsplayer;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
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

    static int numAnchors = 0;
    static Set<Integer> islandIds = new HashSet<>();
    static Set<MapLocation> islandLocs = new HashSet<>();

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case HEADQUARTERS:     runHeadquarters(rc);  break;
                    case CARRIER:      runCarrier(rc);   break;
                    case LAUNCHER: runLauncher(rc); break;
                    case BOOSTER: // Examplefuncsplayer doesn't use any of these robot types below.
                    case DESTABILIZER: // You might want to give them a try!
                    case AMPLIFIER:       break;
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
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for an Archon.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runHeadquarters(RobotController rc) throws GameActionException {
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation newLoc = rc.getLocation().add(dir);
        int targetNumAnchors = rc.getRoundNum() / 100 + 1;
        // Build anchors until we reach the targeted amount
        if (numAnchors < targetNumAnchors) {
            if (rc.canBuildAnchor(Anchor.STANDARD)) {
                rc.buildAnchor(Anchor.STANDARD);
                System.out.println("Building anchor! " + rc.getAnchor());
                numAnchors ++;
            }
        }
        if (rng.nextBoolean()) {
            // Let's try to build a carrier.
            rc.setIndicatorString("Trying to build a carrier");
            if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                rc.buildRobot(RobotType.CARRIER, newLoc);
            }
        } else {
            // Let's try to build a launcher.
            rc.setIndicatorString("Trying to build a launcher");
            if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                rc.buildRobot(RobotType.LAUNCHER, newLoc);
            }
        }
    }

    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        // If I have an anchor singularly focus on getting it to it's location
        if (rc.getAnchor() != null) {
            if (islandLocs.size() > 0) {
                MapLocation islandLocation = islandLocs.iterator().next();
                System.out.println("Moving my anchor towards " + islandLocation);
                while (!rc.getLocation().equals(islandLocation)) {
                    Direction dir = rc.getLocation().directionTo(islandLocation);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
                if (rc.canPlaceAnchor()) {
                    System.out.println("Huzzah, placed anchor!");
                    rc.placeAnchor();
                }
            }
        }
        // Try to mine on squares around us.
        MapLocation me = rc.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int radius = rc.getType().actionRadiusSquared;
                Team opponent = rc.getTeam().opponent();
                RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
                if (enemies.length >= 0) {
                    // MapLocation toAttack = enemies[0].location;
                    MapLocation toAttack = rc.getLocation().add(Direction.EAST);
        
                    if (rc.canAttack(toAttack)) {
                        rc.setIndicatorString("Attacking");        
                        rc.attack(toAttack);
                    }
                }
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                if (rc.canCollectResource(mineLocation, -1)) {
                    if (rng.nextBoolean()) {
                        rc.collectResource(mineLocation, -1);
                        System.out.println("Collecting resource from " + mineLocation);
                        rc.setIndicatorString("Collecting, now have, AD:" + 
                            rc.getResourceAmount(ResourceType.ADAMANTIUM) + 
                            " MN: " + rc.getResourceAmount(ResourceType.MANA) + 
                            " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
                    }
                    if (rng.nextBoolean()) {

                        System.out.println("Trying to transfer");
                        ResourceType rType = rc.senseNearbyWells(mineLocation, 0)[0].getResourceType();
                        if (rc.canTransferResource(mineLocation, rType, 1)) {
                            rc.transferResource(mineLocation, rType, 1);
                        }
                    }
                }
            }
        }
        int[] islands = rc.senseNearbyIslands();
        for (int id : islands) {
            islandIds.add(id);
            MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
            islandLocs.addAll(Arrays.asList(thisIslandLocs));
        }
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        for (RobotInfo robot : robots) {
            if (robot.getType() == RobotType.HEADQUARTERS) {
                Direction dir = me.directionTo(robot.getLocation());
                if (rc.canMove(dir) && rng.nextBoolean())
                    rc.move(dir);
                if (robot.getTotalAnchors() > 0) {
                    if (rc.canTransferResource(robot.getLocation(), ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM))) {
                        System.out.println("Dumping all resources");
                        for (ResourceType rType : ResourceType.values()) {
                            if (rc.getResourceAmount(rType) > 0 && rc.canTransferResource(robot.getLocation(), rType, rc.getResourceAmount(rType)))
                                rc.transferResource(robot.getLocation(), rType, rc.getResourceAmount(rType));
                        }
                        System.out.println("Trying to pick up an anchor from headquarter " + robot.getID() + " with initial " + robot.getTotalAnchors() + " anchors.");
                        if (rc.canTakeAnchor(robot.getLocation(), Anchor.STANDARD)) {
                            rc.takeAnchor(robot.getLocation(), Anchor.STANDARD);
                            System.out.println("Picked up anchor, now hq has " + robot.getTotalAnchors() + " and I have " + rc.getAnchor());
                        }
                    }

                } else if (rc.canTransferResource(robot.getLocation(), ResourceType.ADAMANTIUM, 1)) {
                    rc.transferResource(robot.getLocation(), ResourceType.ADAMANTIUM, 1);
                    rc.setIndicatorString("Transfering, now have, AD:" + 
                        rc.getResourceAmount(ResourceType.ADAMANTIUM) + 
                        " MN: " + rc.getResourceAmount(ResourceType.MANA) + 
                        " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
                    System.out.println("Transfering, now have, AD:" + 
                        rc.getResourceAmount(ResourceType.ADAMANTIUM) + 
                        " MN: " + rc.getResourceAmount(ResourceType.MANA) + 
                        " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
                }
            }
        }
        
        Well[] wells = rc.senseNearbyWells();
        if (wells.length > 0 && rng.nextBoolean()) {
            Well well_one = wells[0];
            Direction dir = me.directionTo(well_one.getMapLocation());
            if (rc.canMove(dir)) 
                rc.move(dir);
        }
        // Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length >= 0) {
            // MapLocation toAttack = enemies[0].location;
            MapLocation toAttack = rc.getLocation().add(Direction.EAST);

            if (rc.canAttack(toAttack)) {
                rc.setIndicatorString("Attacking");        
                rc.attack(toAttack);
            }
        }

        // Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}

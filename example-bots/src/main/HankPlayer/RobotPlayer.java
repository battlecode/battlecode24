package HankPlayer;

import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import battlecode.common.*;

public class RobotPlayer {

    static final Random rng = new Random(6147);
    static boolean hasEnemyFlag = false;
    static Team team;

    static Direction[] directions = {
                Direction.NORTH,
                Direction.NORTHEAST,
                Direction.EAST,
                Direction.SOUTHEAST,
                Direction.SOUTH,
                Direction.SOUTHWEST,
                Direction.WEST,
                Direction.NORTHWEST,
            };

    public static void run(RobotController rc) throws GameActionException{
        while (true){
            try {
                runBot(rc);
                // if (!rc.isSpawned()){
                //     for (MapLocation loc : rc.getAllySpawnLocations()){
                //         if (rc.canSpawn(loc)){
                //             rc.spawn(loc);
                //             hasEnemyFlag = false;

                //             FlagInfo[] nearLocs = rc.senseNearbyFlags(-1);
                //             for(FlagInfo flag : nearLocs) {
                //                 System.out.println(flag.getLocation());
                //             }
                //             MapLocation[] broadcastLocs = rc.senseBroadcastFlagLocations();
                //             for(MapLocation otherLoc : broadcastLocs) {
                //                 System.out.println(otherLoc);
                //             }

                //             break;
                //         }
                //     }
                // }
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                e.printStackTrace();
            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                // System.out.println("Exception");
                e.printStackTrace();
            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
        }
    }

    private static void runBot(RobotController rc) throws GameActionException {
        if(team == null) team = rc.getTeam();

        if (!rc.isSpawned()){
            for (MapLocation loc : rc.getAllySpawnLocations()){
                if (rc.canSpawn(loc)){
                    rc.spawn(loc);
                    hasEnemyFlag = false;
                    break;
                }
                moveRandom(rc);
            }
            return;
        }
        if(rc.getLocation() == null) System.out.println("I am alive but my location is null 1");

        if(rc.getRoundNum() < 150){
            FlagInfo[] flags = rc.senseNearbyFlags(-1, team);
            for(FlagInfo flag : flags) {
                MapLocation flagLoc = flag.getLocation();
                if(rc.canPickupFlag(flagLoc)) {
                    rc.pickupFlag(flagLoc);
                    break;
                }
            }
            moveRandom(rc);
        } 
        else if (rc.getRoundNum() >= 150 && rc.getRoundNum() < 200){
            if(rc.canDropFlag(rc.getLocation())){
                rc.dropFlag(rc.getLocation());
                System.out.println("Dropped ally flag!");
            }
            if(rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation())){
                rc.build(TrapType.EXPLOSIVE, rc.getLocation());
                System.out.println("they call me oppenheimer");
            }
            moveRandom(rc);
        }
        else if (!hasEnemyFlag) {
            ArrayList<MapLocation> flagLocs = new ArrayList<>();
            for(FlagInfo flag : rc.senseNearbyFlags(-1, team == Team.A ? Team.B : Team.A)) {
                if(!flag.isPickedUp()) flagLocs.add(flag.getLocation());
            }
            if(flagLocs.size() == 0) {
                MapLocation[] broadcastLocs = rc.senseBroadcastFlagLocations();
                for(MapLocation loc : broadcastLocs) flagLocs.add(loc);
            }

            MapLocation closestFlag = findClosestLocation(rc.getLocation(), flagLocs);
            if (closestFlag != null && rc.canMove(rc.getLocation().directionTo(closestFlag))){
                rc.move(rc.getLocation().directionTo(closestFlag));
            } else if (closestFlag != null && rc.canFill(rc.adjacentLocation(rc.getLocation().directionTo(closestFlag)))){
                rc.fill(rc.adjacentLocation(rc.getLocation().directionTo(closestFlag)));
            } else {
                moveRandom(rc);
            }

            if (closestFlag != null && rc.canPickupFlag(closestFlag)){
                rc.pickupFlag(closestFlag);
                hasEnemyFlag = true;
                System.out.println("Got enemy flag!");
            }
        } 
        else {
            MapLocation[] allySpawns = rc.getAllySpawnLocations();
            MapLocation closestSpawn = findClosestLocation(rc.getLocation(), Arrays.asList(allySpawns));

            if (rc.canMove(rc.getLocation().directionTo(closestSpawn))){
                rc.move(rc.getLocation().directionTo(closestSpawn));
            } else if (rc.canFill(rc.adjacentLocation(rc.getLocation().directionTo(closestSpawn)))){
                rc.fill(rc.adjacentLocation(rc.getLocation().directionTo(closestSpawn)));
            } else {
                moveRandom(rc);
            }
        }

        if(rc.getLocation() == null) System.out.println("I am alive but my location is null 2");
        RobotInfo[] enemies = rc.senseNearbyRobots(GameConstants.ACTION_RADIUS_SQUARED, rc.getTeam().opponent());
        if (enemies.length != 0 && rc.canAttack(enemies[0].getLocation())){
            rc.attack(enemies[0].getLocation());
            //System.out.println("punched someone");
        } 
    }

    private static void moveRandom(RobotController rc) throws GameActionException {
        int b = rng.nextInt(8);
        for(int i = 0; i < 8; i++){
            if(rc.canMove(directions[(b+i)%8])){
                rc.move(directions[(b+i)%8]);
            }
        }
    }

    public static MapLocation findClosestLocation(MapLocation start, List<MapLocation> locs) {
        MapLocation minLoc = null;
        int minDist = Integer.MAX_VALUE;
        for (int i = 0; i < locs.size(); i++){
            int dist = locs.get(i).distanceSquaredTo(start);
            if(dist < minDist){
                minLoc = locs.get(i);
                minDist = dist;
            }
        }
        return minLoc;
    }
}

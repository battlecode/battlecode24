package MaxPlayer;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer {
    static final Random rng = new Random(7598);

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

    static enum MaxPlayerState {
        NOT_SPAWNED,
        SETTING_UP,
        PLACING_FLAG,
        LOOKING_FOR_ENEMY_FLAG,
        BRINGING_FLAG_BACK,
    }

    static MaxPlayerState state;

    private static void runNotSpawned(RobotController rc) throws GameActionException {
        MapLocation[] spawnZoneLocs = rc.getAllySpawnLocations();
        MapLocation[] goodLocs = new MapLocation[spawnZoneLocs.length];
        int numGoodLocs = 0;

        for (MapLocation loc : spawnZoneLocs) {
            if (rc.canSpawn(loc)) {
                goodLocs[numGoodLocs] = loc;
                numGoodLocs++;
            }
        }

        if (numGoodLocs > 0) {
            int rand = rng.nextInt(numGoodLocs);
            MapLocation spawnLoc = goodLocs[rand];
            rc.spawn(spawnLoc);
        }
    }

    private static void runSettingUp(RobotController rc) throws GameActionException {

    }

    private static void runPlacingFlag(RobotController rc) throws GameActionException {
        
    }

    private static void runLookingForEnemyFlag(RobotController rc) throws GameActionException {
        
    }

    private static void runBringingFlagBack(RobotController rc) throws GameActionException {

    }

    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
                switch (state) {
                    case NOT_SPAWNED:
                        runNotSpawned(rc);
                        break;
                    
                    case SETTING_UP:
                        runSettingUp(rc);
                        break;
                    
                    case PLACING_FLAG:
                        runSettingUp(rc);
                        break;

                    case LOOKING_FOR_ENEMY_FLAG:
                        runLookingForEnemyFlag(rc);
                        break;

                    case BRINGING_FLAG_BACK:
                        runBringingFlagBack(rc);
                        break;

                    default:
                        throw new Exception("A COSMIC RAY HIT YOUR COMPUTER AND TRIGGERED THIS ERROR. THERE IS NO OTHER EXPLANATION FOR SUCH A BAD ERROR.");
                }
                if (rc.isSpawned()) {
                    int numGoodDirs = 0;
                    Direction[] goodDirs = new Direction[directions.length];

                    for (Direction dir : directions) {
                        // MapLocation newLoc = rc.adjacentLocation(dir);

                        // if (!rc.onTheMap(newLoc) || rc.isLocationOccupied(newLoc)) {
                        //     break;
                        // }

                        if (rc.canMove(dir)) {
                            goodDirs[numGoodDirs] = dir;
                            numGoodDirs++;
                        }
                    }

                    if (numGoodDirs > 0) {
                        int rand = rng.nextInt(numGoodDirs);
                        Direction moveDir = directions[rand];
                        rc.move(moveDir);
                    }
                } else {
                    
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException! Message: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Other Exception! Type: " + e.getClass() + " Message: " + e.getMessage());
            } finally {
                Clock.yield();
            }
        }
    }
}

package MaxPlayer;

import java.util.Random;

import battlecode.common.*;
import battlecode.schema.GameplayConstants;

import static MaxPlayer.RobotPlayer.MaxPlayerState.*;

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

    static final Direction[] directionsZigzag = {
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.EAST,
        Direction.CENTER,
        Direction.WEST,
        Direction.NORTHEAST,
        Direction.NORTH,
        Direction.NORTHWEST,
    };

    public static enum MaxPlayerState {
        NOT_SPAWNED,
        SETTING_UP,
        TRAVELING_TO_FLAG,
        PLACING_FLAG,
        LOOKING_FOR_ENEMY_FLAG,
        TRAVELING_TO_ENEMY_FLAG,
        BRINGING_FLAG_BACK,
    }

    static MaxPlayerState state;
    static MapLocation target;

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
            if (rc.getRoundNum() < 200) {
                state = SETTING_UP;
            } else {
                state = LOOKING_FOR_ENEMY_FLAG;
            }
        }
    }

    private static void runSettingUp(RobotController rc) throws GameActionException {
        // sense flags nearby
        FlagInfo[] flagLocs = rc.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED, rc.getTeam());
        
        if (flagLocs.length > 0) {
            FlagInfo minRadiusSquaredFlag = null;
            int minRadiusSquared = Integer.MAX_VALUE;

            for (FlagInfo flag : flagLocs) {
                if (flag.isPickedUp()) {
                    continue;
                }

                int radiusSquared = flag.getLocation().distanceSquaredTo(rc.getLocation());

                if (radiusSquared < minRadiusSquared) {
                    minRadiusSquared = radiusSquared;
                    minRadiusSquaredFlag = flag;
                }
            }

            target = minRadiusSquaredFlag.getLocation();
            state = TRAVELING_TO_FLAG;
        }
    }

    private static void runTravelingToFlag(RobotController rc) throws GameActionException {
        MapLocation loc = rc.getLocation();
        int diffSignX = Integer.signum(target.x - loc.x);
        int diffSignY = Integer.signum(target.y - loc.y);
        Direction dir = directionsZigzag[4 - diffSignX - 3 * diffSignY];

        if (dir == Direction.CENTER) {
            rc.pickupFlag(loc);
            state = PLACING_FLAG;
        } else if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    private static void runPlacingFlag(RobotController rc) throws GameActionException {
        
    }

    private static void runLookingForEnemyFlag(RobotController rc) throws GameActionException {
        
    }

    private static void runTravelingToEnemyFlag(RobotController rc) throws GameActionException {

    }

    private static void runBringingFlagBack(RobotController rc) throws GameActionException {

    }

    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
                if (state == null) {
                    state = NOT_SPAWNED;
                } else if (state != NOT_SPAWNED && rc.getRoundNum() >= 200) {
                    state = LOOKING_FOR_ENEMY_FLAG;
                }

                switch (state) {
                    case NOT_SPAWNED:
                        runNotSpawned(rc);
                        break;
                    
                    case SETTING_UP:
                        runSettingUp(rc);
                        break;

                    case TRAVELING_TO_FLAG:
                        runTravelingToFlag(rc);
                        break;
                    
                    case PLACING_FLAG:
                        runPlacingFlag(rc);
                        break;

                    case LOOKING_FOR_ENEMY_FLAG:
                        runLookingForEnemyFlag(rc);
                        break;

                    case TRAVELING_TO_ENEMY_FLAG:
                        runTravelingToEnemyFlag(rc);
                        break;

                    case BRINGING_FLAG_BACK:
                        runBringingFlagBack(rc);
                        break;

                    default:
                        throw new RuntimeException("A COSMIC RAY HIT YOUR COMPUTER AND TRIGGERED THIS ERROR. THERE IS NO OTHER EXPLANATION FOR SUCH A BAD ERROR.");
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException! Message: " + e.getMessage());
            } /* catch (Exception e) {
                System.out.println("Other Exception! Type: " + e.getClass() + " Message: " + e.getMessage());
            } */ finally {
                Clock.yield();
            }
        }
    }
}

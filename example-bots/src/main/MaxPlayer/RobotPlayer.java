package MaxPlayer;

import java.util.Random;

import battlecode.common.*;

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
        BRINGING_ENEMY_FLAG_BACK,
    }

    static MaxPlayerState state;
    static MapLocation target;
    static final int claimedFlagsCommStart = 1;
    static final int nextUnusedFlagCommIndex = 0;

    private static void moveRandom(RobotController rc) throws GameActionException {
        Direction[] goodDirs = new Direction[directions.length];
        int numGoodDirs = 0;

        for (Direction dir : directions) {
            if (rc.canMove(dir)) {
                goodDirs[numGoodDirs] = dir;
                numGoodDirs++;
            }
        }

        if (numGoodDirs > 0) {
            int rand = rng.nextInt(numGoodDirs);
            Direction dir = goodDirs[rand];
            rc.move(dir);
        }
    }

    private static void moveTowards(RobotController rc, MapLocation target) throws GameActionException {
        MapLocation loc = rc.getLocation();

        int diffSignX = Integer.signum(target.x - loc.x);
        int diffSignY = Integer.signum(target.y - loc.y);
        Direction dir = directionsZigzag[4 - diffSignX - 3 * diffSignY];
        Direction dir2 = directionsZigzag[4 - 3 * diffSignY];
        Direction dir3 = directionsZigzag[4 - diffSignX];

        if (rc.canMove(dir)) {
            rc.move(dir);
        } else if (rc.canMove(dir2)) {
            rc.move(dir2);
        } else if (rc.canMove(dir3)) {
            rc.move(dir3);
        }
    }

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

            if (rc.canPickupFlag(spawnLoc)) {
                rc.pickupFlag(spawnLoc);
                state = PLACING_FLAG;
            } else if (rc.getRoundNum() < 200) {
                state = SETTING_UP;
            } else {
                state = LOOKING_FOR_ENEMY_FLAG;
            }
        }
    }

    private static void runSettingUp(RobotController rc) throws GameActionException {
        moveRandom(rc);

        // sense flags nearby
        // FlagInfo[] flagLocs = rc.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED, rc.getTeam());

        // FlagInfo minRadiusSquaredFlag = null;
        // int minRadiusSquared = Integer.MAX_VALUE;

        // for (FlagInfo flag : flagLocs) {
        //     if (flag.isPickedUp()) {
        //         continue;
        //     }

        //     MapLocation loc = flag.getLocation();

        //     boolean bad = false;
        //     int commIndex = rc.readSharedArray(nextUnusedFlagCommIndex);

        //     for (int i = 0; i < (commIndex - claimedFlagsCommStart) / 2; i++) {
        //         int index = claimedFlagsCommStart + 2 * i;
        //         int x = rc.readSharedArray(index);
        //         int y = rc.readSharedArray(index + 1);
        //         System.out.println("Checking [ci=" + index + "] that [" + x + ", " + y + "] != " + loc + ".");

        //         if (loc.x == x && loc.y == y) {
        //             bad = true;
        //         }
        //     }

        //     if (bad) {
        //         continue;
        //     }

        //     int radiusSquared = flag.getLocation().distanceSquaredTo(rc.getLocation());

        //     if (radiusSquared < minRadiusSquared) {
        //         minRadiusSquared = radiusSquared;
        //         minRadiusSquaredFlag = flag;
        //     }
        // }

        // if (minRadiusSquaredFlag != null) {
        //     target = minRadiusSquaredFlag.getLocation();
        //     int numFlagsFound = rc.readSharedArray(nextUnusedFlagCommIndex);

        //     rc.writeSharedArray(nextUnusedFlagCommIndex, commIndex + 2);
        //     rc.writeSharedArray(commIndex, target.x);
        //     rc.writeSharedArray(commIndex + 1, target.y);
        //     System.out.println("Notifying all robots [ci=" + commIndex + "] about claimed flag at " + target + ".");
        //     state = TRAVELING_TO_FLAG;
        // } else {
        //     moveRandom(rc);
        // }
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
        moveRandom(rc);
        // TODO write method
    }

    private static void runLookingForEnemyFlag(RobotController rc) throws GameActionException {
        // sense flags nearby
        FlagInfo[] flagLocs = rc.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED, rc.getTeam().opponent());

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

        if (minRadiusSquaredFlag != null) {
            target = minRadiusSquaredFlag.getLocation();
            state = TRAVELING_TO_ENEMY_FLAG;
            System.out.println("Found enemy flag: " + target);
        } else {
            moveRandom(rc);
        }
    }

    private static void runTravelingToEnemyFlag(RobotController rc) throws GameActionException {
        MapLocation loc = rc.getLocation();

        if (rc.canPickupFlag(loc)) {
            rc.pickupFlag(loc);
            state = BRINGING_ENEMY_FLAG_BACK;
            return;
        }

        FlagInfo[] flags = rc.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED, rc.getTeam().opponent());
        boolean good = false;

        for (FlagInfo flag : flags) {
            if (!flag.isPickedUp() && flag.getLocation() == target) {
                good = true;
            }
        }

        if (!good) {
            state = LOOKING_FOR_ENEMY_FLAG;
            System.out.println("Gave up on enemy flag: " + target);
            return;
        }

        moveTowards(rc, target);
    }

    private static void runBringingEnemyFlagBack(RobotController rc) throws GameActionException {
        moveRandom(rc);
        // TODO write method
    }

    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
                if (state == null) {
                    state = NOT_SPAWNED;
                } else if (rc.getRoundNum() >= 200 && (state == SETTING_UP || state == PLACING_FLAG)) {
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
                        System.out.println("TRAVELING TO ENEMY FLAG");
                        runTravelingToEnemyFlag(rc);
                        break;

                    case BRINGING_ENEMY_FLAG_BACK:
                        runBringingEnemyFlagBack(rc);
                        break;

                    // default:
                    //     throw new RuntimeException("A COSMIC RAY HIT YOUR COMPUTER AND TRIGGERED THIS ERROR. THERE IS NO OTHER EXPLANATION FOR SUCH A BAD ERROR.");
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException! Message: " + e.getMessage());
                System.out.println("\u26A1\u26A1 A COSMIC RAY HIT YOUR COMPUTER AND TRIGGERED THIS ERROR. THERE IS NO OTHER EXPLANATION FOR SUCH A BAD ERROR. I MAKE NO MISTAKES.");
            } /* catch (Exception e) {
                System.out.println("Other Exception! Type: " + e.getClass() + " Message: " + e.getMessage());
            } */ finally {
                Clock.yield();
            }
        }
    }
}

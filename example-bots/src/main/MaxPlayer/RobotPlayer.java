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

    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
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

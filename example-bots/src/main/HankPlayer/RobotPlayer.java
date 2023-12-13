package HankPlayer;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer {

    static final Random rng = new Random(6147);

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

    public static void run(RobotController rc) throws GameActionException{
        while (true){
            if (!rc.isSpawned()) {
                for (MapLocation loc : rc.getAllySpawnLocations()){
                    if (rc.canSpawn(loc)){
                        rc.spawn(loc);
                        System.out.println(" i spawned in :)");
                        break;
                    }
                }
            }
            else {
                for (Direction dir : directions){
                    if (rc.canMove(dir)){
                        rc.move(dir);
                        break;
                    }
                }
                RobotInfo[] enemies = rc.senseNearbyRobots(GameConstants.ACTION_RADIUS_SQUARED, rc.getTeam().opponent());
                if (enemies.length != 0 && rc.canAttack(enemies[0].getLocation())) 
                    rc.attack(enemies[0].getLocation());
            }
            Clock.yield();
        }
    }
}

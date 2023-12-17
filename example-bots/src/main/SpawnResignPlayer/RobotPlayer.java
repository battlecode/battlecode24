package SpawnResignPlayer;

import java.util.Arrays;
import java.util.Random;

import battlecode.common.*;
import battlecode.schema.GameplayConstants;

public class RobotPlayer {

    static final Random rng = new Random(6147);

    public static void run(RobotController rc) throws GameActionException{
        //rc.resign();
        while (true){

        if (rc.getRoundNum() == 1) System.out.println("game has started");
        if (rc.getRoundNum() == 500) rc.resign();
        if (!rc.isSpawned()){
            MapLocation[] allySpawns = rc.getAllySpawnLocations();
            // System.out.println(Arrays.toString(allySpawns));
            // rc.resign();
            for (MapLocation loc : rc.getAllySpawnLocations()){
                //rc.spawn(loc);
                if (rc.canSpawn(loc)){
                    rc.spawn(loc);
                    System.out.println(" i spawned in :)");
                    rc.resign();
                    break;
                }
            }
        }
        else{
            Direction[] directions = {
                Direction.NORTH,
                Direction.NORTHEAST,
                Direction.EAST,
                Direction.SOUTHEAST,
                Direction.SOUTH,
                Direction.SOUTHWEST,
                Direction.WEST,
                Direction.NORTHWEST,
            };
            for (Direction dir : directions){
                if (rc.canMove(dir)){
                    rc.move(dir);
                    break;
                }
            }
            RobotInfo[] enemies = rc.senseNearbyRobots(GameConstants.ACTION_RADIUS_SQUARED, rc.getTeam().opponent());
            if (enemies.length != 0 && rc.canAttack(enemies[0].getLocation())) rc.attack(enemies[0].getLocation());

        }
        Clock.yield();
 


    }
    }
}

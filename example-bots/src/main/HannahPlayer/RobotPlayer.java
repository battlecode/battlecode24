package HannahPlayer;

import java.util.Arrays;
import java.util.Random;

import battlecode.common.*;
import battlecode.schema.GameplayConstants;

public class RobotPlayer {

    static final Random rng = new Random(6147);

    public static void run(RobotController rc) throws GameActionException{
        //rc.resign();
        while (true){
        System.out.println("Round number " + rc.getRoundNum());

        if (rc.getRoundNum() == 1) 
        System.out.println("starting game");
        //if (rc.getRoundNum() == 1000) rc.resign();

        if (!rc.isSpawned()){
            MapLocation[] allySpawns = rc.getAllySpawnLocations();
            // rc.resign();
            for (MapLocation loc : allySpawns){
                //rc.spawn(loc);
                if (rc.canSpawn(loc)){
                    rc.spawn(loc);
                    System.out.println(" i spawned in :)");
                    //rc.resign();
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

            if(rc.getRoundNum() < 200){ //setup phase
                FlagInfo[] myFlags = rc.senseNearbyFlags(3, rc.getTeam());
                if(rc.getRoundNum() < 4 && rc.canPickupFlag(rc.getLocation())){ //spawned with flag
                    rc.pickupFlag(rc.getLocation());
                    System.out.println("spanwed w/ flag");
                }
                else if(rc.canDropFlag(rc.getLocation()) && rc.canBuild(TrapType.WATER, rc.getLocation())){ //we are out of the spawn zone
                    rc.dropFlag(rc.getLocation());
                    System.out.println("out of spawn zone");
                }
                
                else if(myFlags.length != 0){ //flags are very close!
                    if(rc.canBuild(TrapType.WATER, rc.getLocation())){
                        rc.build(TrapType.WATER, rc.getLocation());
                    }
                    else if(rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation())){
                        rc.build(TrapType.EXPLOSIVE, rc.getLocation());
                    }
                    else if(rc.canBuild(TrapType.STUN, rc.getLocation())){
                        rc.build(TrapType.STUN, rc.getLocation());
                    }
                }
                
                for (Direction dir : directions){ //try to move
                    if (rc.canMove(dir)){
                        rc.move(dir);
                        break;
                    }
                }
            }
            else { //no longer in setup phase
                for (Direction dir : directions){ //try to move
                    if (rc.canMove(dir)){
                        rc.move(dir);
                        break;
                    }
                }
                
                RobotInfo[] enemies = rc.senseNearbyRobots(GameConstants.ACTION_RADIUS_SQUARED, rc.getTeam().opponent());
                if (enemies.length != 0 && rc.canAttack(enemies[0].getLocation())){
                    rc.attack(enemies[0].getLocation());
                }
                
                if(rc.canPickupFlag(rc.getLocation())){
                    rc.pickupFlag(rc.getLocation());
                    System.out.println("I have a flag!");
                }
            }
        }
        Clock.yield();
    }
    }
}

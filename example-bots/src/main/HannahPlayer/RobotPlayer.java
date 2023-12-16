package HannahPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Collections;

import battlecode.common.*;
import battlecode.schema.GameplayConstants;

public class RobotPlayer {

    static final Random rng = new Random(6147);

    public static int squaredDist(MapLocation p1, MapLocation p2){
        int xDist = p1.x - p2.x;
        int yDist = p1.y - p2.y;

        return xDist*xDist + yDist*yDist;
    }

    public static MapLocation findClosestFlag(MapLocation[] arr, MapLocation curr){
        int minDist = Integer.MAX_VALUE;
        MapLocation ans = null;

        for (MapLocation m : arr){
            int dist = squaredDist(m, curr);

            if(dist < minDist){
                minDist = dist;
                ans = m;
            }
        }

        return ans;
    }

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
                
                if(myFlags.length != 0){ //flags are very close!
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
                Collections.shuffle(Arrays.asList(directions));
                for (Direction dir : directions){ //try to move
                    if (rc.canMove(dir)){
                        rc.move(dir);
                        break;
                    }
                }
            }
            else { //no longer in setup phase
                MapLocation[] flagLocs = rc.senseBroadcastFlagLocations();
                boolean randomMove = true;
                if (flagLocs.length != 0){
                    MapLocation flag = findClosestFlag(flagLocs, rc.getLocation());
                    
                    //find distances of all new locations to the closest flag
                    int minDist = Integer.MAX_VALUE;
                    Direction bestDir = null;             

                    for (Direction dir : directions){ //find location that gets closest to flag
                        if (!rc.canMove(dir)) continue;
                        MapLocation newLoc = new MapLocation(rc.getLocation().x + dir.dx, rc.getLocation().y + dir.dy);
                        int newDist = squaredDist(newLoc, flag);

                        if(newDist < minDist){
                            minDist = newDist;
                            bestDir = dir;
                            randomMove = false;
                        }
                    }
                    System.out.println("move this direction " + bestDir);
                    rc.move(bestDir);
                }
                if(randomMove){ //move randomly
                    Collections.shuffle(Arrays.asList(directions));
                    for (Direction dir : directions){ //try to move
                        if (rc.canMove(dir)){
                            rc.move(dir);
                            break;
                        }
                    }
                }

                RobotInfo[] nearby = rc.senseNearbyRobots(GameConstants.ACTION_RADIUS_SQUARED, rc.getTeam().opponent());
                if (nearby.length != 0){
                    if (rc.canAttack(nearby[0].getLocation())){
                        rc.attack(nearby[0].getLocation());
                    } 
                    if (rc.canHeal(nearby[0].getLocation())){
                        rc.heal(nearby[0].getLocation());
                    }
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

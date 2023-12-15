package RuthBotPlayer;

import java.util.Random;

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
                if(team == null) team = rc.getTeam();
                if (!rc.isSpawned()){
                    MapLocation[] allySpawns = rc.getAllySpawnLocations();
                    //System.out.println(Arrays.toString(allySpawns));
                    //rc.resign();
                    for (MapLocation loc : rc.getAllySpawnLocations()){
                        //rc.spawn(loc);
                        if (rc.canSpawn(loc)){
                            rc.spawn(loc);
                            hasEnemyFlag = false;
                            //System.out.println(" i spawned in :)");
                            break;
                        }
                    }

                    int b = rng.nextInt(8);
                    
                    for(int i = 0; i < 8; i++){
                        if(rc.canMove(directions[(b+i)%8])){
                            rc.move(directions[(b+i)%8]);
                        }
                    }
                }
                else{
                    // System.out.println(rc.getLocation());
                    MapLocation[] flagLocs = rc.senseNearbyFlagLocations(rc.getLocation(), 36, team);

                    if(rc.getRoundNum() < 150){
                        if(rc.senseNearbyFlagLocations(rc.getLocation(), 1, team).length > 0){
                            if(rc.canPickupFlag(rc.senseNearbyFlagLocations(rc.getLocation(), 1, team)[0])){
                                rc.pickupFlag(rc.senseNearbyFlagLocations(rc.getLocation(), 1, team)[0]);
                            }
                        }

                        
                    } else if (rc.getRoundNum() >= 150 && rc.getRoundNum() < 200){
                        if(rc.canDropFlag(rc.getLocation())){
                            rc.dropFlag(rc.getLocation());
                            System.out.println("Dropped ally flag!");
                        }
                        if(rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation())){
                            rc.build(TrapType.EXPLOSIVE, rc.getLocation());
                            // System.out.println("they call me oppenheimer");
                        }
                    } else if (!hasEnemyFlag) {
                        if(flagLocs.length == 0){
                            flagLocs = rc.senseBroadcastFlagLocations();
                        }
                        if(flagLocs.length != 0){
                            
                        }
                        int minInd = -1;
                        int minDist = 1000;
                        for (int i = 0; i < flagLocs.length; i++){
                            if(flagLocs[i].distanceSquaredTo(rc.getLocation()) < minDist){
                                minInd = i;
                                minDist = flagLocs[i].distanceSquaredTo(rc.getLocation());
                            }
                        }

                        if (minInd != -1 && rc.canMove(rc.getLocation().directionTo(flagLocs[minInd]))){
                            rc.move(rc.getLocation().directionTo(flagLocs[minInd]));
                        } else if (minInd != -1 && rc.canFill(rc.adjacentLocation(rc.getLocation().directionTo(flagLocs[minInd])))){
                            rc.fill(rc.adjacentLocation(rc.getLocation().directionTo(flagLocs[minInd])));
                        } else {
                            for (Direction dir: directions){
                                if (rc.canMove(dir)){
                                    rc.move(dir);
                                }
                            }
                        }

                        if (minInd != -1 && rc.canPickupFlag(flagLocs[minInd])){
                            rc.pickupFlag(flagLocs[minInd]);
                            System.out.println("Got enemy flag!");
                            hasEnemyFlag = true;
                        }

                        
                        /*if (rc.senseNearbyFlagLocations(rc.getLocation(), 36).length > 0) {
                            if (minInd != -1 && rc.canPickupFlag(flagLocs[minInd])){
                                rc.pickupFlag(flagLocs[minInd]);
                                System.out.println("Got enemy flag!");
                                hasEnemyFlag = true;
                            }
                        }*/
                        
                    } else {
                        MapLocation[] allySpawns = rc.getAllySpawnLocations();
                        int minInd = 0;
                        int minDist = 1000;
                        for (int i = 0; i < allySpawns.length; i++){
                            if(allySpawns[i].distanceSquaredTo(rc.getLocation()) < minDist){
                                minInd = i;
                                minDist = allySpawns[i].distanceSquaredTo(rc.getLocation());
                            }
                        }

                        if (rc.canMove(rc.getLocation().directionTo(allySpawns[minInd]))){
                            rc.move(rc.getLocation().directionTo(allySpawns[minInd]));
                        } else if (rc.canFill(rc.adjacentLocation(rc.getLocation().directionTo(allySpawns[minInd])))){
                            rc.fill(rc.adjacentLocation(rc.getLocation().directionTo(allySpawns[minInd])));
                        } else {
                            for (Direction dir: directions){
                                if (rc.canMove(dir)){
                                    rc.move(dir);
                                }
                            }
                        }
                    }


                    RobotInfo[] enemies = rc.senseNearbyRobots(GameConstants.ACTION_RADIUS_SQUARED, rc.getTeam().opponent());
                    if (enemies.length != 0 && rc.canAttack(enemies[0].getLocation())){
                        //rc.attack(enemies[0].getLocation());
                        //System.out.println("punched someone");
                    } 

                }

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                // e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                // System.out.println("Exception");
                // e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
 


    }
    }
}

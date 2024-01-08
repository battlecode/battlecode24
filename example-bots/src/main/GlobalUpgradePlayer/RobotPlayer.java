package GlobalUpgradePlayer;

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
                runBot(rc);
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
            }
            return;
        }

        if(rc.canBuyGlobal(GlobalUpgrade.CAPTURING)) {
            rc.buyGlobal(GlobalUpgrade.CAPTURING);
            System.out.println("Buying capturing upgrade at round " + rc.getRoundNum());
        }

        if(rc.canBuyGlobal(GlobalUpgrade.ACTION)) {
            rc.buyGlobal(GlobalUpgrade.ACTION);
            System.out.println("Buying action upgrade at round " + rc.getRoundNum());
        }
    }
}
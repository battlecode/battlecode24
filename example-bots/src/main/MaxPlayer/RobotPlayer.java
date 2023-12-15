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
        System.out.println("I'm alive");
        rc.setIndicatorString("Hello world!");

        while (true) {
            try {
                if (rc.isSpawned()) {
                    throw new Exception("MaxPlayer TODO");
                } else {
                    throw new Exception("MaxPlayer TODO");
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException! Message: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Other Exception! Message: " + e.getMessage());
            } finally {
                Clock.yield();
            }
        }
    }
}

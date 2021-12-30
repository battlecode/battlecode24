package examplefuncsplayer;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] droids = {
        RobotType.MINER,
        RobotType.BUILDER,
        RobotType.SOLDIER,
        RobotType.SAGE,
    };

    static final RobotType[] buildings = {
        RobotType.LABORATORY,
        RobotType.WATCHTOWER,
    };

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

    static int turnCount;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ARCHON:     runArchon();     break;
                    case LABORATORY: runLaboratory(); break;
                    case WATCHTOWER: runWatchtower(); break;
                    case MINER:      runMiner();      break;
                    case BUILDER:    runBuilder();    break;
                    case SOLDIER:    runSoldier();    break;
                    case SAGE:       runSage();       break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runArchon() throws GameActionException {
        RobotType toBuild = randomDroid();
        int influence = 50;
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir)) {
                rc.buildRobot(toBuild, dir);
            } else {
                break;
            }
        }
    }

    static void runLaboratory() throws GameActionException {
        // TODO
    }

    static void runWatchtower() throws GameActionException {
        // TODO
    }

    static void runMiner() throws GameActionException {
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runBuilder() throws GameActionException {
        // TODO
    }

    static void runSoldier() throws GameActionException {
        // TODO
    }

    static void runSage() throws GameActionException {
        // TODO
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random droid
     *
     * @return a random RobotType
     */
    static RobotType randomDroid() {
        return droids[(int) (Math.random() * droids.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isMovementReady() + " " + rc.getMovementCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
}

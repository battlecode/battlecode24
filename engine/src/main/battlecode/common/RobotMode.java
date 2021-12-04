package battlecode.common;

/**
 * Holds the different "modes" a robot can be in, mostly useful for buildings.
 */
public enum RobotMode {

    DROID       (true,  true),
    PROTOTYPE   (false, false),
    TURRET      (true,  false),
    PORTABLE    (false, true);

    public final boolean canAct;
    public final boolean canMove;

    RobotMode(boolean canAct, boolean canMove) {
        this.canAct     = canAct;
        this.canMove    = canMove;
    }
}
package battlecode.common;

/**
 * Holds the different "modes" a robot can be in, mostly useful for buildings.
 */
public enum RobotMode {

    DROID       (true,  true, false),
    PROTOTYPE   (false, false, false),
    TURRET      (true,  false, true),
    PORTABLE    (false, true, true);

    public final boolean canAct;
    public final boolean canMove;
    public final boolean canTransform;

    RobotMode(boolean canAct, boolean canMove, boolean canTransform) {
        this.canAct         = canAct;
        this.canMove        = canMove;
        this.canTransform   = canTransform;
    }
}
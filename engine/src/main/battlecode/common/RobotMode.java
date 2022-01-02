package battlecode.common;

/**
 * Enumerates the modes that a robot could possibly be in. The mode of a robot
 * determines the set of interactions with the world that it can currently
 * perform legally.
 * <P>
 * You can check the mode of another robot by inspecting {@link RobotInfo#mode},
 * or your own mode by inspecting {@link RobotController#getMode}.
 */
public enum RobotMode {
    /**
     * The Droid mode describes all Droid robots. These robots can always act
     * and move, as long as their cooldowns permit.
     */
    DROID       (true,  true, false),

    /**
     * The Prototype mode describes newly-constructed Buildings. These robots
     * cannot do anything until they have been repaired to full health by a
     * Builder.
     */
    PROTOTYPE   (false, false, false),

    /**
     * The Turret mode describes Buildings in Turret mode. These robots can act,
     * but not move. Turrets can also transform into Portable mode.
     */
    TURRET      (true,  false, true),

    /**
     * The Portable mode describes Buildings in Portable mode. These robots can
     * move, but not act. Portables can also transform into Turret mode.
     */
    PORTABLE    (false, true, true);

    /**
     * Whether robots in this mode may perform actions other than moving.
     */
    public final boolean canAct;

    /**
     * Whether robots in this mode may move.
     */
    public final boolean canMove;

    /**
     * Whether robots in this mode may transform to another mode.
     */
    public final boolean canTransform;

    RobotMode(boolean canAct, boolean canMove, boolean canTransform) {
        this.canAct         = canAct;
        this.canMove        = canMove;
        this.canTransform   = canTransform;
    }
}

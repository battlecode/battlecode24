package battlecode.common;

import battlecode.world.Inventory;

/**
 * RobotInfo stores basic information that was 'sensed' of another Robot. This
 * info is ephemeral and there is no guarantee any of it will remain the same
 * between rounds.
 */
public class RobotInfo {

    /**
     * The unique ID of the robot.
     */
    public final int ID;

    /**
     * The Team that the robot is on.
     */
    public final Team team;

    /**
     * The type of the robot.
     */
    public final RobotType type;

    /**
     * The health of the robot.
     */
    public final int health;

    /**
     * The inventory of the robot.
     */
    public final Inventory inventory;

    /**
     * The current location of the robot.
     */
    public final MapLocation location;

    public RobotInfo(int ID, Team team, RobotType type, Inventory inventory, int health, MapLocation location) {
        super();
        this.ID = ID;
        this.team = team;
        this.type = type;
        this.inventory = inventory.copy();
        this.health = health;
        this.location = location;
    }

    /**
     * Returns the ID of this robot.
     *
     * @return the ID of this robot
     */
    public int getID() {
        return this.ID;
    }

    /**
     * Returns the team that this robot is on.
     *
     * @return the team that this robot is on
     */
    public Team getTeam() {
        return team;
    }

    /**
     * Returns the type of this robot.
     *
     * @return the type of this robot
     */
    public RobotType getType() {
        return type;
    }

    /**
     * Returns the health of this robot.
     *
     * @return the health of this robot
     */
    public int getHealth() {
        return health;
    }

    /**
     * Returns the amount of resource this robot has.
     * 
     * @param rType the resource type of interest
     * @return the amount of resources of this type the robot holds
     */
    public int getResourceAmount(ResourceType rType) {
        return inventory.getResource(rType);
    }

    /**
     * Returns the number of anchors this robot has.
     * 
     * @param anchor the anchor type of interest
     * @return the num of this type of anchor the robot is holding
     */
    public int getNumAnchors(Anchor anchor) {
        return inventory.getNumAnchors(anchor);
    }

    /**
     * Returns the total number of anchors this robot has.
     * 
     * @return the total num of anchors the robot is holding
     */
    public int getTotalAnchors() {
        return inventory.getTotalAnchors();
    }

    /**
     * Returns the location of this robot.
     *
     * @return the location of this robot
     */
    public MapLocation getLocation() {
        return this.location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RobotInfo robotInfo = (RobotInfo) o;

        if (ID != robotInfo.ID) return false;
        if (team != robotInfo.team) return false;
        if (type != robotInfo.type) return false;
        if (inventory.equals(robotInfo.inventory)) return false;
        if (health != robotInfo.health) return false;
        return location.equals(robotInfo.location);
    }

    @Override
    public int hashCode() {
        int result;
        result = ID;
        result = 31 * result + team.hashCode();
        result = 31 * result + type.ordinal();
        result = 31 * result + inventory.hashCode();
        result = 31 * result + health;
        result = 31 * result + location.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RobotInfo{" +
                "ID=" + ID +
                ", team=" + team +
                ", type=" + type +
                ", inventory=" + inventory + 
                ", health=" + health +
                ", location=" + location +
                '}';
    }
}

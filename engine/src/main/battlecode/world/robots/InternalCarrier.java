package battlecode.world.robots;

import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.schema.Action;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;
import battlecode.world.Inventory;

public class InternalCarrier extends InternalRobot {

    /**
     * Create a new internal representation of a Carrier robot
     *
     * @param gw the world the robot exists in
     * @param type the type of the robot
     * @param loc the location of the robot
     * @param team the team of the robot
     */
    public InternalCarrier(GameWorld gw, int id, RobotType type, MapLocation loc, Team team) {
        super(gw, id, type, loc, team);
    }

    // ******************************************
    // ****** ATTACK METHODS ********************
    // ******************************************

    /**
     * Empties the resources from a robot. Used in a throw attack.
     * @param bot
     */
    private void emptyResources() {
        for (ResourceType rType : ResourceType.values()) {
            this.inventory.addResource(rType, -1*this.inventory.getResource(rType));
        }
    }

    private int locationToInt(MapLocation loc) {
        return loc.x + loc.y * this.getGameWorld().getGameMap().getWidth();
    }

    /**
     * Attacks another robot (carrier). Assumes bot is in range.
     * Empties inventory accordingly.
     * 
     * @param bot the robot to be attacked
     */
    public void attack(InternalRobot bot) {
        if (!(bot == null)) {
            int dmg = this.getType().getDamage(this.getResource(ResourceType.ADAMANTIUM)+this.getResource(ResourceType.MANA)+this.getResource(ResourceType.ELIXIR));
            bot.addHealth(-dmg);
            this.getGameWorld().getMatchMaker().addAction(getID(), Action.THROW_ATTACK, bot.getID());
        } else {
            this.getGameWorld().getMatchMaker().addAction(getID(), Action.THROW_ATTACK, -1);
        }
        this.emptyResources();
    }

}
package battlecode.world.robots;

import battlecode.common.Anchor;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.schema.Action;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;

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
            int amount = -1*this.inventory.getResource(rType);
            this.inventory.addResource(rType, amount);
            this.gameWorld.getTeamInfo().addResource(rType, getTeam(), amount);
        }
        while (this.inventory.getNumAnchors(Anchor.STANDARD) > 0)
            this.inventory.releaseAnchor(Anchor.STANDARD);
        while (this.inventory.getNumAnchors(Anchor.ACCELERATING) > 0)
            this.inventory.releaseAnchor(Anchor.ACCELERATING);
    }
    
    private int getDamage() {
        int weight = this.inventory.getWeight();
        return (int) Math.floor(GameConstants.CARRIER_DAMAGE_FACTOR*(weight));
    }

    private int locationToInt(MapLocation loc) {
        return this.gameWorld.locationToIndex(loc);
    }

    /**
     * Attacks another location (carrier).
     * Empties inventory accordingly.
     * 
     * @param loc the location to be attacked
     */
    public void attack(MapLocation loc) {
        InternalRobot bot = this.gameWorld.getRobot(loc);
        if (bot == null || bot.getTeam() == this.getTeam() || bot.getType() == RobotType.HEADQUARTERS) {
            // If robot is null, of your team, or a hq do no damage, otherwise do damage
            this.getGameWorld().getMatchMaker().addAction(getID(), Action.THROW_ATTACK, -locationToInt(loc) - 1);
        } else {
            int dmg = getDamage();
            bot.addHealth(-dmg);
            this.getGameWorld().getMatchMaker().addAction(getID(), Action.THROW_ATTACK, bot.getID());
        }
        this.emptyResources();
    }
}
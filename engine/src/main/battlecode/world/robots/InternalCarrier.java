package battlecode.world.robots;

import battlecode.common.MapLocation;
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
     * Attacks another robot (launcher). Assumes bot is in range.
     * Empties inventory accordingly.
     * 
     * @param bot the robot to be attacked
     */
    public void attack(InternalRobot bot) {
        if (!(bot == null)) {
            int dmg = this.getType().getDamage(bot.getAd()+bot.getElixir()+bot.getMn());
            bot.addHealth(-dmg);
            this.getGameWorld().getMatchMaker().addAction(getID(), Action.ATTACK, bot.getID());
        }
        bot.addAd(-bot.getAd());
        bot.addElixir(-bot.getElixir());
        bot.addMn(-bot.getMn());
    }

}
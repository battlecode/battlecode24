import battlecode.common.MapLocation;
import battlecode.common.Team;

public class Headquarter {
    
    private Inventory inv;

    private MapLocation loc;

    private Team team;

    private int spawnCooldownTurns;

    /*
     * Creates a new Headquarter with an empty inventory
     */
    public Headquarter(Team team, MapLocation loc) {
        inv = new Inventory();
        this.team = team;
        this.loc = loc;
    }

    public Inventory getInventory() {
        return inv;
    }

    public MapLocation getLocation() {
        return loc;
    }

    public Team getTeam() {
        return team;
    }

    public void generateResources() {
        //should these values not be hard-coded?
        inv.addAdamantium(5);
        inv.addMana(5);
    }

    public void buildRobot() {
        //should this method actually build the robot, or should that happen somewhere else?
        cooldownTurns -= 10;
    }

    public boolean canBuildRobot() {
        return cooldownTurns >= 10;
    }
}

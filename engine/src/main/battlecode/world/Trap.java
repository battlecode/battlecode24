package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Team;
import battlecode.common.TrapType;

public class Trap{
    private int id;
    private TrapType type;
    private MapLocation loc;
    private Team owningTeam;

    public Trap(MapLocation loc, TrapType type, Team team, int id){
        this.loc = loc;
        this.type = type;
        this.owningTeam = team;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Team getTeam(){
        return this.owningTeam;
    }

    public MapLocation getLocation(){
        return this.loc;
    }

    public TrapType getType(){
        return this.type;
    }

    public String toString(){
        return "Trap{" + "loc= " + loc + ", type= " + type + ", team= " +owningTeam + "}";
    }
}

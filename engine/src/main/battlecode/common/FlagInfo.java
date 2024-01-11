package battlecode.common;

public class FlagInfo {
    
    private MapLocation loc;

    private Team team;

    private boolean pickedUp;

    private int id;

    public FlagInfo(MapLocation loc, Team team, boolean pickedUp, int id) {
        this.loc = loc;
        this.team = team;
        this.pickedUp = pickedUp;
        this.id = id;
    }

    public MapLocation getLocation() {
        return loc;
    }

    public Team getTeam() {
        return team;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public int getID() {
        return id;
    }
}

package battlecode.common;

public class FlagInfo {
    
    private MapLocation loc;

    private Team team;

    private boolean pickedUp;

    public FlagInfo(MapLocation loc, Team team, boolean pickedUp) {
        this.loc = loc;
        this.team = team;
        this.pickedUp = pickedUp;
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
}

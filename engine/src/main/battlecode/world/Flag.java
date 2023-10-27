package battlecode.world;

import battlecode.common.MapLocation;
import battlecode.common.Team;

public class Flag {
    
    private Team team;

    private MapLocation loc;

    private MapLocation startLoc;

    private MapLocation broadcastLoc;

    private boolean pickedUp;

    public Flag(Team team, MapLocation startLoc){
        this.team = team;
        this.startLoc = startLoc;
        broadcastLoc = startLoc;
        pickedUp = false;
    }

    public Team getTeam() {
        return team;
    }

    public MapLocation getLoc() {
        return loc;
    }

    public void setLoc(MapLocation startLoc){
        this.loc = loc;
    }

    public MapLocation getStartLoc() {
        return startLoc;
    }

    public void setStartLoc(MapLocation startLoc){
        this.startLoc = startLoc;
    }

    public MapLocation getBroadcastLoc() {
        return broadcastLoc;
    }

    public void setBroadcastLoc(MapLocation broadcastLoc) {
        this.broadcastLoc = broadcastLoc;
    }

    public boolean isPickedUp(){
        return pickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

}

package battlecode.world;

import battlecode.common.MapLocation;
import battlecode.common.Team;

public class Flag {
    
    private int id;

    private Team team;

    private MapLocation loc;

    private MapLocation startLoc;

    private MapLocation broadcastLoc;

    private InternalRobot carryingRobot;

    private int droppedRounds;

    public Flag(Team team, MapLocation startLoc, int id){
        this.team = team;
        this.startLoc = startLoc;
        this.id = id;
        loc = startLoc;
        broadcastLoc = startLoc;
    }

    public int getId() {
        return id;
    }

    public Team getTeam() {
        return team;
    }

    public MapLocation getLoc() {
        return loc;
    }

    public void setLoc(MapLocation loc){
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
        return carryingRobot != null;
    }

    public InternalRobot getCarryingRobot() {
        return carryingRobot;
    }

    public void pickUp(InternalRobot robot) {
        carryingRobot = robot;
        droppedRounds = 0;
        loc = carryingRobot.getLocation();
    }

    public void drop() {
        carryingRobot = null;
        droppedRounds = 0;
    }

    public int getDroppedRounds() {
        return droppedRounds;
    }

    public void incrementDroppedRounds() {
        droppedRounds += 1;
    }
}

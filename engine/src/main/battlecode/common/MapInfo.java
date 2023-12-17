package battlecode.common;

import static battlecode.common.GameActionExceptionType.*;

import java.util.Arrays;

public class MapInfo {


    private MapLocation loc;

    private boolean isPassable;

    private boolean isWall;

    private int spawnZone; // 0 = Team A, 1 = Team B, -1 = not a spawn zone

    private boolean isWater;

    private int breadAmount;

    private TrapType trapType;



    public MapInfo(MapLocation loc, boolean isPassable, boolean isWall, int spawnZone, boolean isWater, int breadAmount, TrapType trapType){
        this.loc = loc;
        this.isPassable = isPassable;
        this.isWall = isWall;
        this.spawnZone = spawnZone;
        this.isWater = isWater;
        this.breadAmount = breadAmount;
        this.trapType = trapType;
    }

    private void assertValidTeam(Team team) throws GameActionException {
        if (team != Team.A && team != Team.B) {
            throw new GameActionException(CANT_DO_THAT, "Must pass valid team to get info about a space");
        }
    }


    /**
     * Returns if this square is passable.
     * 
     * @return whether this square is passable
     * 
     * @battlecode.doc.costlymethod
     */
    public boolean isPassable() {
        return isPassable;
    }

    /**
     * Returns if this square is a wall.
     * 
     * @return whether this square is a wall
     * 
     * @battlecode.doc.costlymethod
     */
    public boolean isWall() {
        return isWall;
    }

    /**
     * Returns if this square is a spawn zone.
     * 
     * @return whether this square is a spawn zone
     * 
     * @battlecode.doc.costlymethod
     */
    public boolean isSpawnZone() {
        return spawnZone >= 0;
    }

    /**
     * Returns 0 if this square is a Team A spawn zone,
     * 1 if this square is a Team B spawn zone, and
     * -1 if this square is not a spawn zone.
     * 
     * @return 0 or 1 if the square is a Team A or B spawn zone, respectively; -1 otherwise
     */
    public int getSpawnZoneTeam() {
        return spawnZone;
    }

    /**
     * Returns if this square has water in it.
     * 
     * @return whether this square has water
     * 
     * @battlecode.doc.costlymethod
     */
    public boolean isWater() {
        return isWater;
    }

    /**
     * Returns the amount of bread on this square.
     * If there is no bread on the square, returns 0.
     * 
     * @return the amount of bread on the square
     * 
     * @battlecode.doc.costlymethod
     */
    public int getBreadAmount() {
        return breadAmount;
    }


    /**
     * Returns the location of this square
     * 
     * @return the location of this square
     * 
     * @battlecode.doc.costlymethod
     */
    public MapLocation getMapLocation() {
        return loc;
    }




    public String toString(){
        return "Location{" +
                "loc=" + loc +
                (isWall ? ", wall" : "") +
                (isWater ? ", water" : "") +
                (spawnZone == 0 ? ", team A spawn zone" : "") +
                (spawnZone == 1 ? ", team B spawn zone" : "") +
                (breadAmount == 0 ? "" : ", bread=" + breadAmount) +
                (trapType == null ? "" : ", trap=" + trapType) +
                '}';

    }

}
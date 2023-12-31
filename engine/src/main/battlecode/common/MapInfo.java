package battlecode.common;

public class MapInfo {

    private MapLocation loc;

    private boolean isPassable;

    private boolean isWall;

    //0 = Team A, 1 = Team B, -1 = not a spawn zone
    private int spawnZone;

    private boolean isWater;

    private int crumbsAmount;

    private TrapType trapType;

    public MapInfo(MapLocation loc, boolean isPassable, boolean isWall, int spawnZone, boolean isWater, int crumbsAmount, TrapType trapType){
        this.loc = loc;
        this.isPassable = isPassable;
        this.isWall = isWall;
        this.spawnZone = spawnZone;
        this.isWater = isWater;
        this.crumbsAmount = crumbsAmount;
        this.trapType = trapType;
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
     * Returns the amount of crumbs on this square.
     * If there are no crumbs on the square, returns 0.
     * 
     * @return the amount of crumbs on the square
     * 
     * @battlecode.doc.costlymethod
     */
    public int getCrumbs() {
        return crumbsAmount;
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
                (crumbsAmount == 0 ? "" : ", crumbs=" + crumbsAmount) +
                (trapType == null ? "" : ", trap=" + trapType) +
                '}';
    }

}
package battlecode.world;

import battlecode.common.*;

import java.io.IOException;
import java.util.*;
import java.util.function.*;

import javax.management.RuntimeErrorException;

/**
 * The class represents the map in the game world on which
 * objects interact.
 *
 * This class is STATIC and immutable. It reflects the initial
 * condition of the map. All changes to the map are reflected in GameWorld.
 *
 * It is named LiveMap to distinguish it from a battlecode.schema.GameMap,
 * which represents a serialized LiveMap.
 */
public strictfp class LiveMap {

    /**
     * The width and height of the map.
     */
    private final int width, height;

    /**
     * The coordinates of the origin
     */
    private final MapLocation origin;

    /**
     * The symmetry of the map.
     */
    private final MapSymmetry symmetry;

    /**
     * Whether each square is a wall.
     */
    private boolean[] wallArray;

    /**
     * Whether each square is water.
     */
    private boolean[] waterArray;

    /**
     * Whether each square is a dam.
     */
    private boolean[] damArray;

    /**
     * Amount of bread on each square.
     */
    private int[] breadArray;

    /**
     * An integer representing the team ID of the spawn zone on a square.
     */
    private int[] spawnZoneArray;

    /**
     * The random seed contained in the map file.
     */
    private final int seed;

    /**
     * The maximum number of rounds in the game.
     */
    private final int rounds;

    /**
     * The name of the map.
     */
    private final String mapName;


    

    public LiveMap(int width,
                   int height,
                   MapLocation origin,
                   int seed,
                   int rounds,
                   String mapName) {
        this.width = width;
        this.height = height;
        this.origin = origin;
        this.seed = seed;
        this.rounds = rounds;
        this.mapName = mapName;
        this.symmetry = MapSymmetry.ROTATIONAL;
        
        int numSquares = width * height;

        this.wallArray = new boolean[numSquares];
        this.waterArray = new boolean[numSquares];
        this.spawnZoneArray = new int[numSquares];
        this.breadArray = new int[numSquares];
        this.damArray = new boolean[numSquares];

        // invariant: bodies is sorted by id
      //  Arrays.sort(this.initialBodies, (a, b) -> Integer.compare(a.getID(), b.getID()));
    }

    public LiveMap(int width,
                   int height,
                   MapLocation origin,
                   int seed,
                   int rounds,
                   String mapName,
                   MapSymmetry symmetry,
                   boolean[] wallArray,
                   boolean[] waterArray,
                   boolean[] damArray,
                   int[] breadArray,
                   int[] spawnZoneArray) {
        this.width = width;
        this.height = height;
        this.origin = origin;
        this.seed = seed;
        this.rounds = rounds;
        this.mapName = mapName;
        this.symmetry = symmetry;
       // this.initialBodies = Arrays.copyOf(initialBodies, initialBodies.length);
        this.wallArray = new boolean[wallArray.length];
        for (int i = 0; i < wallArray.length; i++) {
            this.wallArray[i] = wallArray[i];
        }
        this.waterArray = new boolean[waterArray.length];
        for (int i = 0; i < waterArray.length; i++){
            this.waterArray[i] = waterArray[i];
        }
        this.damArray = new boolean[damArray.length];
        for (int i = 0; i < damArray.length; i++){
            this.damArray[i] = damArray[i];
        }
        this.breadArray = new int[
            breadArray.length];
        for (int i = 0; i < breadArray.length; i++) {
            this.breadArray[i] = breadArray[i];
        }
        this.spawnZoneArray = new int[spawnZoneArray.length];
        for (int i = 0; i < spawnZoneArray.length; i++){
            this.spawnZoneArray[i] = spawnZoneArray[i];
        }
        // invariant: bodies is sorted by id
      //  Arrays.sort(this.initialBodies, (a, b) -> Integer.compare(a.getID(), b.getID()));
    }

    /**
     * Creates a deep copy of the input LiveMap, except initial bodies.
     *
     * @param gm the LiveMap to copy.
     */
    public LiveMap(LiveMap gm) {
        this(gm.width, gm.height, gm.origin, gm.seed, gm.rounds, gm.mapName, gm.symmetry,
         gm.wallArray, gm.waterArray, gm.damArray, gm.breadArray, gm.spawnZoneArray);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LiveMap)) return false;
        return this.equals((LiveMap) o);
    }

    /**
     * Returns whether two GameMaps are equal.
     *
     * @param other the other map to compare to
     * @return whether the two maps are equivalent
     */
    public boolean equals(LiveMap other) {
        if (this.rounds != other.rounds) return false;
        if (this.width != other.width) return false;
        if (this.height != other.height) return false;
        if (this.seed != other.seed) return false;
        if (!this.mapName.equals(other.mapName)) return false;
        if (!this.origin.equals(other.origin)) return false;
        if (!Arrays.equals(this.wallArray, other.wallArray)) return false;
        if (!Arrays.equals(this.waterArray, other.waterArray)) return false;
        if (!Arrays.equals(this.damArray, other.damArray)) return false;
        if (!Arrays.equals(this.breadArray, other.breadArray)) return false;
        if (!Arrays.equals(this.spawnZoneArray, other.spawnZoneArray)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + origin.hashCode();
        result = 31 * result + seed;
        result = 31 * result + rounds;
        result = 31 * result + mapName.hashCode();
        result = 31 * result + Arrays.hashCode(wallArray);
        result = 31 * result + Arrays.hashCode(waterArray);
        result = 31 * result + Arrays.hashCode(damArray);
        result = 31 * result + Arrays.hashCode(breadArray);
        result = 31 * result + Arrays.hashCode(spawnZoneArray);
        return result;
    }

    /**
     * Returns the width of this map.
     *
     * @return the width of this map.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the map.
     *
     * @return the height of the map
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the name of the map.
     *
     * @return the name of the map
     */
    public String getMapName() {
        return mapName;
    }

    /**
     * Returns the symmetry of the map.
     *
     * @return the symmetry of the map
     */
    public MapSymmetry getSymmetry() {
        return symmetry;
    }

    /**
     * Determines whether or not the location at the specified
     * coordinates is on the map. The coordinate should be a shifted one
     * (takes into account the origin). Assumes grid format (0 <= x < width).
     *
     * @param x the (shifted) x-coordinate of the location
     * @param y the (shifted) y-coordinate of the location
     * @return true if the given coordinates are on the map,
     *         false if they're not
     */
    private boolean onTheMap(int x, int y) {
        return (x >= origin.x && y >= origin.y && x < origin.x + width && y < origin.y + height);
    }

    /**
     * Determines whether or not the specified location is on the map.
     *
     * @param loc the MapLocation to test
     * @return true if the given location is on the map,
     *         false if it's not
     */
    public boolean onTheMap(MapLocation loc) {
        return onTheMap(loc.x, loc.y);
    }

    /**
     * Determines whether or not the specified circle is completely on the map.
     *
     * @param loc the center of the circle
     * @param radius the radius of the circle
     * @return true if the given circle is on the map,
     *         false if it's not
     */
    public boolean onTheMap(MapLocation loc, int radius) {
        return (onTheMap(loc.translate(-radius, 0)) &&
                onTheMap(loc.translate(radius, 0)) &&
                onTheMap(loc.translate(0, -radius)) &&
                onTheMap(loc.translate(0, radius)));
    }

    /**
     * Gets the maximum number of rounds for this game.
     *
     * @return the maximum number of rounds for this game
     */
    public int getRounds() {
        return rounds;
    }

    /**
     * @return the seed of this map
     */
    public int getSeed() {
        return seed;
    }

    /**
     * Gets the origin (i.e., upper left corner) of the map
     *
     * @return the origin of the map
     */
    public MapLocation getOrigin() {
        return origin;
    }

    /**
     * @return the wall array of the map
     */
    public boolean[] getWallArray() {
        return wallArray;
    }


    public boolean[] getWaterArray() {
        return waterArray;
    }

    /**
     * Returns the array indicating where the spawn zones are.
     * In this array: 0 = not spawn zone, 1 = Team A spawn zone, 2 = Team B spawn zone
     * 
     * @return the array of spawn zones
     */
    public int[] getSpawnZoneArray() {
        return spawnZoneArray;
    }


    /**
     * 
     * @return a 6x2 array of spawn locations; the first 3 are for team A
     */
    public int[][] getSpawnZoneCenters(){
        //first 3 team A next 3 team B
        int[][] spawnZoneCenters = new int[2][6];
        int cur_A = 0;
        int cur_B = 3;
        for (int i = 0; i < spawnZoneArray.length; i++){
            if (spawnZoneArray[i] == 1 && onTheMap(indexToLocation(i-width-1)) && spawnZoneArray[i-width-1] == 1
            && onTheMap(indexToLocation(i+width+1)) && spawnZoneArray[i+width+1] == 1){
                MapLocation center = indexToLocation(i);
                spawnZoneCenters[0][cur_A] = center.x;
                spawnZoneCenters[1][cur_A] = center.y;
                cur_A += 1;
            }
            if (spawnZoneArray[i] == 2 && onTheMap(indexToLocation(i-width-1)) && spawnZoneArray[i-width-1] == 2
            && onTheMap(indexToLocation(i+width+1)) && spawnZoneArray[i+width+1] == 2){
                MapLocation center = indexToLocation(i);
                spawnZoneCenters[0][cur_B] = center.x;
                spawnZoneCenters[1][cur_A] = center.y;
                cur_B += 1;
            }
        }
        return spawnZoneCenters;

    } 

    public boolean[] getDamArray(){
        return damArray;
    }
    
    /**
     * @return the array which stores how much bread is on each location.
     */
    public int[] getBreadArray() {
        return breadArray;
    }

    /**
     * Helper method that converts a location into an index.
     * 
     * @param loc the MapLocation
     */
    public int locationToIndex(MapLocation loc) {
        return loc.x - getOrigin().x + (loc.y - getOrigin().y) * getWidth();
    }

    /**
     * Helper method that converts an index into a location.
     * 
     * @param idx the index
     */
    public MapLocation indexToLocation(int idx) {
        return new MapLocation(idx % getWidth() + getOrigin().x,
                               idx / getWidth() + getOrigin().y);
    }

    public void assertIsValid() throws Exception{
        if (this.width > GameConstants.MAP_MAX_WIDTH) {
            throw new RuntimeException("MAP WIDTH EXCEEDS GameConstants.MAP_MAX_WIDTH");
        }
        if (this.width < GameConstants.MAP_MIN_WIDTH) {
            throw new RuntimeException("MAP WIDTH BENEATH GameConstants.MAP_MIN_WIDTH");
        }
        if (this.height > GameConstants.MAP_MAX_HEIGHT) {
            throw new RuntimeException("MAP HEIGHT EXCEEDS GameConstants.MAP_MAX_HEIGHT");
        }
        if (this.height < GameConstants.MAP_MIN_HEIGHT) {
            throw new RuntimeException("MAP HEIGHT BENEATH GameConstants.MAP_MIN_HEIGHT");
        }
        for (int i = 0; i < this.width*this.height; i++){
            if(this.wallArray[i]) {
                if (this.damArray[i]) {
                    throw new RuntimeException("Walls can't be on the same square as dams.");
                }
                if (this.waterArray[i]) {
                    throw new RuntimeException("Walls can't be on the same square as water.");
                }
                if (this.breadArray[i] != 0) {
                    throw new RuntimeException("Walls can't be on the same square as bread.");
                }
                if(this.spawnZoneArray[i] != 0) {
                    throw new RuntimeException("Walls can't be on the same square as spawn zones.");
                } 
            }
            if(this.damArray[i]) {
                if(this.waterArray[i]) {
                    throw new RuntimeException("Dams can't be on the same square as water.");
                }
                if(this.breadArray[i] != 0) {
                    throw new RuntimeException("Dams can't be on the same square as bread.");
                }
                if(this.spawnZoneArray[i] != 0) {
                    throw new RuntimeException("Dams can't be on the same square as spawn zones.");
                }
            }

            if(this.waterArray[i]) {
                if(this.breadArray[i] != 0) {
                    throw new RuntimeException("Water can't be on the same square as bread.");
                }
                if(this.spawnZoneArray[i] != 0) {
                    throw new RuntimeException("Water can't be on the same square as spawn zones.");
                }
            }

        }
        assertSpawnZoneDistances();
        assertSpawnZonesAreValid();
    }

    private boolean isTeamNumber(int team) {
        return team == 1 || team == 2;
    }

    private int getOpposingTeamNumber(int team) {
        switch (team) {
            case 1:
                return 2;
            case 2:
                return 1;
            default:
                throw new RuntimeException("Argument of LiveMap.getOpposingTeamNumber must be a valid team number, was " + team + ".");
        }
    }

        // WARNING: POSSIBLY BUGGY
        private void assertSpawnZonesAreValid() {
            int numSquares = this.width * this.height;
            boolean[] alreadyChecked = new boolean[numSquares];
    
            for (int i = 0; i < numSquares; i++) {
                int team = this.spawnZoneArray[i];
    
                // if the square is actually a spawn zone
    
                if (isTeamNumber(team)) {
                    boolean bad = floodFillMap(indexToLocation(i),
                        (loc) -> this.spawnZoneArray[locationToIndex(loc)] == getOpposingTeamNumber(team),
                        (loc) -> this.wallArray[locationToIndex(loc)] || this.damArray[locationToIndex(loc)],
                        alreadyChecked);
    
                    if (bad) {
                        throw new RuntimeException("Two spawn zones for opposing teams can reach each other.");
                    }
                }
            }
        }

        private void assertSpawnZoneDistances() {
            //TODO: I changed this to only check distances between the centers of spawn zones, we may need to adjust math accordingly
            ArrayList<MapLocation> team1 = new ArrayList<MapLocation>();
            ArrayList<MapLocation> team2 = new ArrayList<MapLocation>();
    
            int[][] spawnZoneCenters = getSpawnZoneCenters();
            for(int i = 0; i < spawnZoneCenters.length; i ++){
                if (i < spawnZoneCenters.length/2){
                    team1.add(new MapLocation(spawnZoneCenters[i][0], spawnZoneCenters[i][1]));
                }
                else {
                    team2.add(new MapLocation(spawnZoneCenters[i][0], spawnZoneCenters[i][1]));
                }
            }
    
            for(int a = 0; a < team1.size()-1; a ++){
                for(int b = a+1; b < team1.size(); b ++){
                    if ((team1.get(a)).distanceSquaredTo((team1.get(b))) < GameConstants.MIN_FLAG_SPACING_SQUARED){
                        throw new RuntimeException("Two spawn zones on the same team are within 6 units of each other");
                    }
                }
            }
    
            for(int c = 0; c < team2.size()-1; c ++){
                for(int d = c+1; d < team2.size(); d ++){
                    if ((team2.get(c)).distanceSquaredTo((team2.get(d))) < GameConstants.MIN_FLAG_SPACING_SQUARED){
                        throw new RuntimeException("Two spawn zones on the same team are within 6 units of each other");
                    }
                }
            }
        }
    
        /**
         * Performs a flood fill algorithm to check if a predicate is true for any squares
         * that can be reached from a given location (horizontal, vertical, and diagonal steps allowed).
         * 
         * @param startLoc the starting location
         * @param checkForBad the predicate to check for each reachable square
         * @param checkForWall a predicate that checks if the given square has a wall
         * @param alreadyChecked an array indexed by map location indices which has "true" at
         * every location reachable from a spawn zone that has already been checked
         * (WARNING: this array gets updated by floodFillMap)
         * @return if checkForBad returns true for any reachable squares
         */
        private boolean floodFillMap(MapLocation startLoc, Predicate<MapLocation> checkForBad, Predicate<MapLocation> checkForWall, boolean[] alreadyChecked) {
            Queue<MapLocation> queue = new LinkedList<MapLocation>(); // stores map locations by index
    
            if (!onTheMap(startLoc)) {
                throw new RuntimeException("Cannot call floodFillMap with startLocation off the map.");
            }
    
            queue.add(startLoc);
    
            while (!queue.isEmpty()) {
                MapLocation loc = queue.remove();
                int idx = locationToIndex(loc);
    
                if (alreadyChecked[idx]) {
                    continue;
                }
    
                alreadyChecked[idx] = true;
    
                if (!checkForWall.test(loc)) {
                    if (checkForBad.test(loc)) {
                        return true;
                    }
    
                    for (Direction dir : Direction.allDirections()) {
                        if (dir != Direction.CENTER) {
                            MapLocation newLoc = loc.add(dir);
    
                            if (onTheMap(newLoc)) {
                                int newIdx = locationToIndex(newLoc);
    
                                if (!(alreadyChecked[newIdx] || checkForWall.test(newLoc))) {
                                    queue.add(newLoc);
                                }
                            }
                        }
                    }
                }
            }
    
            return false;
        }


    @Override
    public String toString() {
        if (wallArray.length == 0) {
            return "LiveMap{" +
                    "width=" + width +
                    ", height=" + height +
                    ", origin=" + origin +
                    ", seed=" + seed +
                    ", rounds=" + rounds +
                    ", mapName='" + mapName + '\'' +
                    ", len=" + Integer.toString(wallArray.length) +
                    "}";
        } else {
            return "LiveMap{" +
                    "width=" + width +
                    ", height=" + height +
                    ", origin=" + origin +
                    ", seed=" + seed +
                    ", rounds=" + rounds +
                    ", mapName='" + mapName + '\'' +
                    ", damArray=" + Arrays.toString(damArray) + 
                    ", wallArray=" + Arrays.toString(wallArray) +
                    ", waterArray=" + Arrays.toString(waterArray) + 
                    ", spawnZoneArray=" + Arrays.toString(spawnZoneArray) + 
                    ", breadArray=" + Arrays.toString(breadArray) +
                    "}";
        }
    }
}

package battlecode.world;

import battlecode.common.*;

import java.util.*;

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
     * Whether each square is a cloud.
     */
    private final boolean[] cloudArray;

    /**
     * Whether each square is water.
     */
    private boolean[] waterArray;

    /**
     * Whether each square is a dam.
     */
    private boolean[] damArray;

    /**
     * Whether each square is a spawn zone.
     * 0 = Team A, 1 = Team B, -1 = not spawn zone
     */
    private int[] spawnZoneArray;

    /**
     * Direction ID of current on each square.
     */
    private int[] currentArray;

    /**
     * Island ID of current on each square.
     */
    private int[] islandArray;

    /**
     * Amount of bread on each square.
     */
    private int[] breadArray;

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

    /**
     * The bodies to spawn on the map; MapLocations are in world space -
     * i.e. in game correct MapLocations that need to have the origin
     * subtracted from them to be used to index into the map arrays.
     */
    private final RobotInfo[] initialBodies; // contains nothing

    

    public LiveMap(int width,
                   int height,
                   MapLocation origin,
                   int seed,
                   int rounds,
                   String mapName,
                   RobotInfo[] initialBodies) {
        this.width = width;
        this.height = height;
        this.origin = origin;
        this.seed = seed;
        this.rounds = rounds;
        this.mapName = mapName;
        this.symmetry = MapSymmetry.ROTATIONAL;
        this.initialBodies = Arrays.copyOf(initialBodies, initialBodies.length);

        int numSquares = width * height;

        this.wallArray = new boolean[numSquares];
        this.cloudArray = new boolean[numSquares];
        this.waterArray = new boolean[numSquares];
        this.spawnZoneArray = new int[numSquares];
        this.currentArray = new int[numSquares];
        this.islandArray = new int[numSquares];
        this.breadArray = new int[numSquares];
        this.damArray = new boolean[numSquares];

        // invariant: bodies is sorted by id
        Arrays.sort(this.initialBodies, (a, b) -> Integer.compare(a.getID(), b.getID()));
    }

    public LiveMap(int width,
                   int height,
                   MapLocation origin,
                   int seed,
                   int rounds,
                   String mapName,
                   MapSymmetry symmetry,
                   RobotInfo[] initialBodies,
                   boolean[] wallArray,
                   boolean[] cloudArray,
                   int[] currentArray,
                   int[] islandArray,
                   int[] breadArray) {
        this.width = width;
        this.height = height;
        this.origin = origin;
        this.seed = seed;
        this.rounds = rounds;
        this.mapName = mapName;
        this.symmetry = symmetry;
        this.initialBodies = Arrays.copyOf(initialBodies, initialBodies.length);
        this.wallArray = new boolean[wallArray.length];
        for (int i = 0; i < wallArray.length; i++) {
            this.wallArray[i] = wallArray[i];
        }
        this.cloudArray = new boolean[cloudArray.length];
        for (int i = 0; i < cloudArray.length; i++) {
            this.cloudArray[i] = cloudArray[i];
        }
        this.currentArray = new int[currentArray.length];
        for (int i = 0; i < currentArray.length; i++) {
            this.currentArray[i] = currentArray[i];
        }
        this.islandArray = new int[islandArray.length];
        for (int i = 0; i < islandArray.length; i++) {
            this.islandArray[i] = islandArray[i];
        }
        this.breadArray = new int[
            breadArray.length];
        for (int i = 0; i < breadArray.length; i++) {
            this.breadArray[i] = breadArray[i];
        }

        // invariant: bodies is sorted by id
        Arrays.sort(this.initialBodies, (a, b) -> Integer.compare(a.getID(), b.getID()));
    }

    /**
     * Creates a deep copy of the input LiveMap, except initial bodies.
     *
     * @param gm the LiveMap to copy.
     */
    public LiveMap(LiveMap gm) {
        this(gm.width, gm.height, gm.origin, gm.seed, gm.rounds, gm.mapName, gm.symmetry,
             gm.initialBodies, gm.wallArray, gm.cloudArray, gm.currentArray, gm.islandArray, gm.breadArray);
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
        if (!Arrays.equals(this.cloudArray, other.cloudArray)) return false;
        if (!Arrays.equals(this.currentArray, other.currentArray)) return false;
        if (!Arrays.equals(this.islandArray, other.islandArray)) return false;
        if (!Arrays.equals(this.breadArray, other.breadArray)) return false;
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
        result = 31 * result + Arrays.hashCode(cloudArray);
        result = 31 * result + Arrays.hashCode(currentArray);
        result = 31 * result + Arrays.hashCode(islandArray);
        result = 31 * result + Arrays.hashCode(breadArray);
        result = 31 * result + Arrays.hashCode(initialBodies);
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
     * Get a list of the initial bodies on the map.
     *
     * @return the list of starting bodies on the map.
     *         MUST NOT BE MODIFIED.
     */
    public RobotInfo[] getInitialBodies() {
        return initialBodies;
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

    /**
     * @return the cloud array of the map
     */
    public boolean[] getCloudArray() {
        return cloudArray;
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

    public boolean getWater(MapLocation loc) {
        return waterArray[locationToIndex(loc)];
    }

    public void setWater(MapLocation loc) {
        waterArray[locationToIndex(loc)] = true;
    }

    public boolean[] getDamArray(){
        return damArray;
    }
    public void setLand(MapLocation loc) {
        waterArray[locationToIndex(loc)] = false;
    }

    /**
     * @return the current array of the map
     */
    public int[] getCurrentArray() {
        return currentArray;
    }

    /**
     * @return the island id array of the map
     */
    public int[] getIslandArray() {
        return islandArray;
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
                    ", initialBodies=" + Arrays.toString(initialBodies) +
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
                    ", initialBodies=" + Arrays.toString(initialBodies) +
                    ", damArray=" + Arrays.toString(damArray) + 
                    ", wallArray=" + Arrays.toString(wallArray) +
                    /* ", cloudArray=" + Arrays.toString(cloudArray) +
                    ", currentArray=" + Arrays.toString(currentArray) +
                    ", islandArray=" + Arrays.toString(islandArray) + */
                    ", breadArray=" + Arrays.toString(breadArray) +
                    "}";
        }
    }
}

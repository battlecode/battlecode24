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
     * How much lead is stored per square.
     */
    private final int[][] leadMap;

    /**
     * The random seed contained in the map file
     */
    private final int seed;

    /**
     * The maximum number of rounds in the game
     */
    private final int rounds;

    /**
     * The name of the map
     */
    private final String mapName;

    /**
     * List of anomalies that will occur at certain rounds.
     */
    private final AnomalyScheduleEntry[] anomalySchedule;

    /**
     * Index of next anomaly to occur (excluding Singularity which always occurs)
     */
    private int nextAnomalyIndex;

    /**
     * The bodies to spawn on the map; MapLocations are in world space -
     * i.e. in game correct MapLocations that need to have the origin
     * subtracted from them to be used to index into the map arrays.
     */
    private final RobotInfo[] initialBodies; // only contains Enlightenment Centers

    private int[] rubbleArray; // factor to multiply cooldowns by

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
        this.initialBodies = Arrays.copyOf(initialBodies, initialBodies.length);
        this.rubbleArray = new int[width * height];
        for (int i = 0; i < rubbleArray.length; i++) {
            this.rubbleArray[i] = 1; // default cooldown factor is 1
        }

        // invariant: bodies is sorted by id
        Arrays.sort(this.initialBodies, (a, b) -> Integer.compare(a.getID(), b.getID()));
        
        // TODO: initialize with potentially hardcoded anomalies
        this.anomalySchedule = null;
        this.nextAnomalyIndex = 0;

    }

    public LiveMap(int width,
                   int height,
                   MapLocation origin,
                   int seed,
                   int rounds,
                   String mapName,
                   RobotInfo[] initialBodies,
                   int[] rubbleArray) {
        this.width = width;
        this.height = height;
        this.origin = origin;
        this.seed = seed;
        this.rounds = rounds;
        this.mapName = mapName;
        this.initialBodies = Arrays.copyOf(initialBodies, initialBodies.length);
        this.rubbleArray = new int[rubbleArray.length];
        for (int i = 0; i < rubbleArray.length; i++) {
            this.rubbleArray[i] = rubbleArray[i];
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
        this(gm.width, gm.height, gm.origin, gm.seed, gm.rounds, gm.mapName, gm.initialBodies,
             gm.rubbleArray);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LiveMap)) return false;

        return this.equals((LiveMap) o);
    }

    /**
     * Returns whether two GameMaps are equal.
     *
     * @param other the other map to compare to.
     * @return whether the two maps are equivalent.
     */
    public boolean equals(LiveMap other) {
        if (this.rounds != other.rounds) return false;
        if (this.width != other.width) return false;
        if (this.height != other.height) return false;
        if (this.seed != other.seed) return false;
        if (!this.mapName.equals(other.mapName)) return false;
        if (!this.origin.equals(other.origin)) return false;
        if (!Arrays.equals(this.rubbleArray, other.rubbleArray)) return false;
        return Arrays.equals(this.initialBodies, other.initialBodies);
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + origin.hashCode();
        result = 31 * result + seed;
        result = 31 * result + rounds;
        result = 31 * result + mapName.hashCode();
        result = 31 * result + Arrays.hashCode(rubbleArray);
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
     * Returns the height of this map.
     *
     * @return the height of this map.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the name of the map.
     *
     * @return the name o the map.
     */
    public String getMapName() {
        return mapName;
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
    public boolean onTheMap(MapLocation loc, int radius){
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

    public int[] getRubbleArray() {
        return rubbleArray;
    }

    /**
     * @param x to get lead at
     * @param y to get lead at
     * @return the amount of lead at this location
     */
    public int getLeadAtLocation(int x, int y){
        assert onTheMap(new MapLocation(x, y));
        return this.leadMap[x][y];
    }

    /**
     * Changes the amount of lead to amount
     * @param x to set lead at
     * @param y to set lead at 
     * @param amount of lead to put at this location
     */
    public void setLeadAtLocation(int x, int y, int amount){
        assert onTheMap(new MapLocation(x, y));
        this.leadMap[x][y] = amount;
    }

    /**
     * Adds the amount of lead to current amount at a given square
     * @param x to set lead at
     * @param y to set lead at 
     * @param amountToAdd
     */
    public void addLeadAtLocation(int x, int y, int amountToAdd){
        assert onTheMap(new MapLocation(x, y));
        this.leadMap[x][y] += amountToAdd; 
    }

    /**
     * @return a copy of the next Anomaly that hasn't happened yet.
     */
    public AnomalyScheduleEntry viewNextAnomaly(){
        return this.anomalySchedule[this.nextAnomalyIndex].copyEntry();
    }

    /**
     * Removes the current anomaly by advancing to the next one.
     * @return the next Anomaly.
     */
    public AnomalyScheduleEntry takeNextAnomaly(){
        return this.anomalySchedule[this.nextAnomalyIndex++].copyEntry();
    }

    /**
     * @return a copy of the anomaly schedule
     */
    public AnomalyScheduleEntry[] getAnomalySchedule(){

        AnomalyScheduleEntry[] anomalyCopy = new AnomalyScheduleEntry[this.anomalySchedule.length];

        for(int i = 0; i < this.anomalySchedule.length ; i++)
            anomalyCopy[i] = this.anomalySchedule[i].copyEntry();

        return anomalyCopy;
    }


    @Override
    public String toString() {
        if (rubbleArray.length == 0){
            return "LiveMap{" +
                    "width=" + width +
                    ", height=" + height +
                    ", origin=" + origin +
                    ", seed=" + seed +
                    ", rounds=" + rounds +
                    ", mapName='" + mapName + '\'' +
                    ", initialBodies=" + Arrays.toString(initialBodies) +
                    ", len=" + Integer.toString(rubbleArray.length) +
                    "}";
        }
        else{
            return "LiveMap{" +
                    "width=" + width +
                    ", height=" + height +
                    ", origin=" + origin +
                    ", seed=" + seed +
                    ", rounds=" + rounds +
                    ", mapName='" + mapName + '\'' +
                    ", initialBodies=" + Arrays.toString(initialBodies) +
                    ", rubbleArray=:)" +  Arrays.toString(rubbleArray) +
                    "}"; 
        }
    }
}
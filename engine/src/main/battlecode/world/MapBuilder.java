package battlecode.world;

import battlecode.common.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.*;

/**
 * Build and validate maps easily.
 */
public class MapBuilder {

    public String name;
    public int width;
    public int height;
    public MapLocation origin;
    public int seed;
    private MapSymmetry symmetry;
    private boolean[] wallArray;
    private boolean[] damArray;
    private boolean[] waterArray;
    private boolean[] cloudArray;
    private int[] currentArray;
    private int[] islandArray;
    private int[] resourceArray;
    private int[] spawnZoneArray;

    private int idCounter;

    private List<RobotInfo> bodies;

    // TODO: get rid of origin
    public MapBuilder(String name, int width, int height, int originX, int originY, int seed) {
        assert(originX == 0);
        assert(originY == 0);
        this.name = name;
        this.width = width;
        this.height = height;
        this.origin = new MapLocation(originX, originY);
        this.seed = seed;
        this.bodies = new ArrayList<>();

        // default values
        this.symmetry = MapSymmetry.ROTATIONAL;
        this.idCounter = 0;
        
        int numSquares = width * height;

        this.wallArray = new boolean[numSquares];
        this.waterArray = new boolean[numSquares];
        this.damArray = new boolean[numSquares];
        this.cloudArray = new boolean[numSquares];
        this.currentArray = new int[numSquares];
        this.islandArray = new int[numSquares];
        this.resourceArray = new int[numSquares];
        this.spawnZoneArray = new int[numSquares];

        //for testing purposes
        for (int i = 0; i < numSquares; i++){
            if (i % 2 == 0)
            this.spawnZoneArray[i] = 1;
            else
            this.spawnZoneArray[i] = 2;
        }
    }

    // ********************
    // BASIC METHODS
    // ********************

    /**
     * Convert location to index. Critical: must conform with GameWorld.indexToLocation.
     * @param x
     * @param y
     * @return
     */
    private int locationToIndex(int x, int y) {
        return x + y * width;
    }

    private int locationToIndex(MapLocation loc) {
        return loc.x + loc.y * width;
    }

    public void setWall(int x, int y, boolean value) {
        this.wallArray[locationToIndex(x, y)] = value;
    }

    public void setCloud(int x, int y, boolean value) {
        this.cloudArray[locationToIndex(x, y)] = value;
    }

    public void setCurrent(int x, int y, int value) {
        this.currentArray[locationToIndex(x, y)] = value;
    }

    public void setIsland(int x, int y, int value) {
        this.islandArray[locationToIndex(x, y)] = value;
    }

    public void setResource(int x, int y, int value) {
        this.resourceArray[locationToIndex(x, y)] = value;
    }

    public void setSpawnZone(int x, int y, int value) {
        this.spawnZoneArray[locationToIndex(x, y)] = value;
    }

    public void setSymmetry(MapSymmetry symmetry) {
        this.symmetry = symmetry;
    }

    // ********************
    // SYMMETRY METHODS
    // ********************

    public int symmetricY(int y) {
        return symmetricY(y, symmetry);
    }

    public int symmetricX(int x) {
        return symmetricX(x, symmetry);
    }

    public int symmetricY(int y, MapSymmetry symmetry) {
        switch (symmetry) {
            case VERTICAL:
                return y;
            case HORIZONTAL:
            case ROTATIONAL:
            default:
                return height - 1 - y;
        }
    }

    public int symmetricX(int x, MapSymmetry symmetry) {
        switch (symmetry) {
            case HORIZONTAL:
                return x;
            case VERTICAL:
            case ROTATIONAL:
            default:
                return width - 1 - x;
        }
    }

    public MapLocation symmetryLocation(MapLocation p) {
        return new MapLocation(symmetricX(p.x), symmetricY(p.y));
    }

    public void setSymmetricWalls(int x, int y, boolean value) {
        this.wallArray[locationToIndex(x, y)] = value;
        this.wallArray[locationToIndex(symmetricX(x), symmetricY(y))] = value;
    }

    public void setSymmetricCloud(int x, int y, boolean value) {
        this.cloudArray[locationToIndex(x, y)] = value;
        this.cloudArray[locationToIndex(symmetricX(x), symmetricY(y))] = value;
    }

    private int getSymmetricCurrent(int value) {
        Direction currentDirection = Direction.DIRECTION_ORDER[value];
        return currentDirection.opposite().getDirectionOrderNum();
    }

    public void setSymmetricCurrent(int x, int y, int value) {
        this.currentArray[locationToIndex(x, y)] = value;
        this.currentArray[locationToIndex(symmetricX(x), symmetricY(y))] = getSymmetricCurrent(value);
    }

    private int getSymmetricIsland(int id) {
        return id == 0 ? 0 : this.islandArray.length-id;
    }

    public void setSymmetricIsland(int x, int y, int id) {
        this.islandArray[locationToIndex(x, y)] = id;
        this.islandArray[locationToIndex(symmetricX(x), symmetricY(y))] = getSymmetricIsland(id);
    }

    public void setSymmetricResource(int x, int y, int id) {
        this.resourceArray[locationToIndex(x, y)] = id;
        this.resourceArray[locationToIndex(symmetricX(x), symmetricY(y))] = id;
    }

    // ********************
    // BUILDING AND SAVING
    // ********************

    public LiveMap build() {
        return new LiveMap(width, height, origin, seed, 2000, name, symmetry, wallArray, waterArray, damArray, resourceArray, spawnZoneArray);
    }

    /**
     * Saves the map to the specified location.
     * @param pathname
     * @throws IOException
     */
    public void saveMap(String pathname) throws IOException {
        // validate
        assertIsValid();
        System.out.println("Saving " + this.name + ".");
        GameMapIO.writeMap(this.build(), new File(pathname));
    }

    /**
     * Throws a RuntimeException if the map is invalid.
     */
    public void assertIsValid() {
        System.out.println("Validating " + name + "...");

        if (width < GameConstants.MAP_MIN_WIDTH || height < GameConstants.MAP_MIN_HEIGHT || 
            width > GameConstants.MAP_MAX_WIDTH || height > GameConstants.MAP_MAX_HEIGHT)
            throw new RuntimeException("The map size must be between " + GameConstants.MAP_MIN_WIDTH + "x" +
                                       GameConstants.MAP_MIN_HEIGHT + " and " + GameConstants.MAP_MAX_WIDTH + "x" +
                                       GameConstants.MAP_MAX_HEIGHT + ", inclusive");

        for (int i = 0; i < this.width*this.height; i++){
            //TODO check for multiple things existing on the same tile
        }

        //assertSpawnZonesAreValid();
       // assertSpawnZoneDistances();
    }

    private boolean isTeamNumber(int team) {
        return team == 0 || team == 1;
    }

    private int getOpposingTeamNumber(int team) {
        switch (team) {
            case 0:
                return 1;
            case 1:
                return 0;
            default:
                throw new RuntimeException("Argument of MapBuilder.getOpposingTeamNumber must be a valid team number, was " + team + ".");
        }
    }

    // WARNING: POSSIBLY BUGGY
    private void assertSpawnZonesAreValid() {
        int numSquares = this.width * this.height;
        boolean[] alreadyChecked = new boolean[numSquares];

        for (int i = 0; i < numSquares; i++) {
            int team = this.spawnZoneArray[i];

            // if the square is actually a spawn zone

            //TODO need to include dam in reachability check
            if (isTeamNumber(team)) {
                boolean bad = floodFillMap(indexToLocation(i),
                    (loc) -> this.spawnZoneArray[locationToIndex(loc)] == getOpposingTeamNumber(team),
                    (loc) -> this.wallArray[locationToIndex(loc)],
                    alreadyChecked);

                if (bad) {
                    throw new RuntimeException("Two spawn zones for opposing teams can reach each other.");
                }
            }
        }
    }


    private void assertSpawnZoneDistances() {
        ArrayList<Integer> team1 = new ArrayList<Integer>();
        ArrayList<Integer> team2 = new ArrayList<Integer>();

        for(int i = 0; i < spawnZoneArray.length; i ++){
            if (spawnZoneArray[i] == 0){
                team1.add(i);
            }
            else if (spawnZoneArray[i] == 1){
                team2.add(i);
            }
        }

        for(int a = 0; a < team1.size(); a ++){
            for(int b = 1; b < team1.size(); b ++){
                if (indexToLocation(team1.get(a)).distanceSquaredTo(indexToLocation(team1.get(b))) < GameConstants.MIN_FLAG_SPACING_SQUARED){
                    throw new RuntimeException("Two spawn zones on the same team are within 6 units of each other");
                }
            }
        }

        for(int c = 0; c < team2.size(); c ++){
            for(int d = 1; d < team2.size(); d ++){
                if (indexToLocation(team2.get(c)).distanceSquaredTo(indexToLocation(team2.get(d))) < GameConstants.MIN_FLAG_SPACING_SQUARED){
                    throw new RuntimeException("Two spawn zones on the same team are within 6 units of each other");
                }
            }
        }
    }
    /**
     * Performs a flood fill algorithm to check if a predicate is true for any squares
     * that can be reached from a given location (horizontal, vertical, and diagonal steps allowed).
     * 
     * @param checkForBad the predicate to check for each reachable square
     * @param checkForWall a predicate that checks if the given square has a wall
     * @param action an action that is performed when scanning each square (might be called multiple times per square)
     * @return if checkForBad returns true for any reachable squares
     */
    private boolean floodFillMap(MapLocation startLoc, Predicate<MapLocation> checkForBad, Predicate<MapLocation> checkForWall, boolean[] alreadyChecked) {
        Queue<MapLocation> queue = new LinkedList<MapLocation>(); // stores map locations by index
        queue.add(startLoc);
        //TODO: gave arrayindexoutofbounds -19 on line 319; probably need to check that the newlocation is within map bounds first

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
                        int newIdx = locationToIndex(newLoc);

                        if (!(alreadyChecked[newIdx] || checkForWall.test(newLoc))) {
                            queue.add(newLoc);
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean onTheMap(MapLocation loc) {
        return loc.x >= 0 && loc.y >= 0 && loc.x < width && loc.y < height;
    }

    public MapLocation indexToLocation(int idx) {
        return new MapLocation(idx % this.width,
                               idx / this.width);
    }

    /**
     * @return the list of symmetries, empty if map is invalid
     */
    private ArrayList<MapSymmetry> getSymmetry(RobotInfo[] robots) {
        ArrayList<MapSymmetry> possible = new ArrayList<MapSymmetry>();
        possible.add(MapSymmetry.ROTATIONAL);
        possible.add(MapSymmetry.HORIZONTAL);
        possible.add(MapSymmetry.VERTICAL);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                MapLocation current = new MapLocation(x, y);
                int curIdx = locationToIndex(current.x, current.y);
                for (int i = possible.size() - 1; i >= 0; i--) { // iterating backwards so we can remove in the loop
                    MapSymmetry symmetry = possible.get(i);
                    MapLocation symm = new MapLocation(symmetricX(x, symmetry), symmetricY(y, symmetry));
                    int symIdx = locationToIndex(symm.x, symm.y);
                    if (wallArray[curIdx] != wallArray[symIdx]) {
                        possible.remove(symmetry);
                    }
                    else if (cloudArray[curIdx] != cloudArray[symIdx]) {
                        possible.remove(symmetry);
                    }
                    else if (getSymmetricCurrent(currentArray[curIdx]) != currentArray[symIdx]) {
                        possible.remove(symmetry);
                    }
                    else if (getSymmetricIsland(islandArray[curIdx]) != islandArray[symIdx]) {
                        possible.remove(symmetry);
                    }
                    else if (resourceArray[curIdx] != resourceArray[symIdx]) {
                        possible.remove(symmetry);
                    }
                }
            }
        }
        return possible;
    }

    private boolean symmetricTeams(Team a, Team b) {
        return a != b;
    }
}

package battlecode.world;

import battlecode.common.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    private boolean[] cloudArray;
    private int[] currentArray;
    private int[] islandArray;
    private int[] resourceArray;

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
        this.wallArray = new boolean[width * height];
        this.cloudArray = new boolean[width * height];
        this.currentArray = new int[width * height];
        this.islandArray = new int[width * height];
        this.resourceArray = new int[width * height];
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

    public void addHeadquarter(int id, Team team, MapLocation loc) {
        // check if something already exists here, if so shout
        for (RobotInfo r : bodies) {
            if (r.location.equals(loc)) {
                throw new RuntimeException("CANNOT ADD ROBOT TO SAME LOCATION AS OTHER ROBOT");
            }
        }
        bodies.add(new RobotInfo(
                id,
                team,
                RobotType.HEADQUARTERS,
                new Inventory(),
                RobotType.HEADQUARTERS.health,
                loc
        ));
    }

    public void addHeadquarter(int x, int y, Team team) {
        addHeadquarter(
                idCounter++,
                team,
                new MapLocation(x, y)
        );
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

    /**
     * Add team A Headquarters to (x,y) and team B Headquarters to symmetric position.
     * @param x x position
     * @param y y position
     */
    public void addSymmetricHeadquarter(int x, int y) {
        addHeadquarter(x, y, Team.A);
        addHeadquarter(symmetricX(x), symmetricY(y), Team.B);
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
        return new LiveMap(width, height, origin, seed, GameConstants.GAME_MAX_NUMBER_OF_ROUNDS, name,
                symmetry, bodies.toArray(new RobotInfo[bodies.size()]), wallArray, cloudArray, currentArray, islandArray, resourceArray);
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

        // get robots
        RobotInfo[] robots = new RobotInfo[width * height];
        for (RobotInfo r : bodies) {
            assert(r.getType() == RobotType.HEADQUARTERS);
            if (robots[locationToIndex(r.location.x, r.location.y)] != null)
                throw new RuntimeException("Two robots on the same square");
            robots[locationToIndex(r.location.x, r.location.y)] = r;
        }

        if (width < GameConstants.MAP_MIN_WIDTH || height < GameConstants.MAP_MIN_HEIGHT || 
            width > GameConstants.MAP_MAX_WIDTH || height > GameConstants.MAP_MAX_HEIGHT)
            throw new RuntimeException("The map size must be between " + GameConstants.MAP_MIN_WIDTH + "x" +
                                       GameConstants.MAP_MIN_HEIGHT + " and " + GameConstants.MAP_MAX_WIDTH + "x" +
                                       GameConstants.MAP_MAX_HEIGHT + ", inclusive");

        // checks between 1 and 4 Headquarters (inclusive) of each team
        // only needs to check the Headquarters of Team A, because symmetry is checked
        int numTeamARobots = 0;
        for (RobotInfo r : bodies) {
            if (r.getTeam() == Team.A) {
                numTeamARobots++;
            }
        }
        if (numTeamARobots < GameConstants.MIN_STARTING_HEADQUARTERS ||
            numTeamARobots > GameConstants.MAX_STARTING_HEADQUARTERS) {
            throw new RuntimeException("Map must have between " + GameConstants.MIN_STARTING_HEADQUARTERS +
                                       " and " + GameConstants.MAX_STARTING_HEADQUARTERS + " starting Headquarters of each team");
        }

        //assert that walls are not on same location as resources/islands/currents/clouds
        for (int i = 0; i < this.width*this.height; i++){
            if (this.wallArray[i]){
                if (this.cloudArray[i])
                    throw new RuntimeException("Walls cannot be on the same square as clouds");
                if (this.resourceArray[i] != 0)
                    throw new RuntimeException("Walls cannot be on the same square as resources");
                if (this.islandArray[i] != 0)
                    throw new RuntimeException("Walls cannot be on an island");
                if (this.currentArray[i] != 0)
                    throw new RuntimeException("Walls cannot be on the same square as currents");
                if (robots[i] != null)
                    throw new RuntimeException("Walls cannot be on the same square as headquarters");
            }
            //assert that clouds and currents cannot be on the same square
            if (this.cloudArray[i] && this.currentArray[i] != 0)
                throw new RuntimeException("Clouds and currents cannot be on the same square");

            //assert that wells are not on same square as headquarters
            if (this.resourceArray[i] != 0 && robots[i] != null)
                throw new RuntimeException("Wells can't be on same square as headquarters");

            //assert that currents are not on same square as headquarters
            if (this.currentArray[i] != 0 && robots[i] != null)
                throw new RuntimeException("Currents can't be on same square as headquarters");

            //assert that currents are not on same square as wells
            if (this.currentArray[i] != 0 && this.resourceArray[i] != 0)
                throw new RuntimeException("Currents can't be on same square as wells");
        }
        
        // assert rubble, lead, and headquarter symmetry
        ArrayList<MapSymmetry> allMapSymmetries = getSymmetry(robots);
        if (!allMapSymmetries.contains(this.symmetry)) {
            throw new RuntimeException("Headquarters, walls, clouds, currents, islands and resources must be symmetric");
        }

       //assert that at least one resource well of each type is visible to each team
        boolean[] hasVisibleAdamantium = new boolean[2];
        boolean[] hasVisibleMana = new boolean[2];
        for (RobotInfo r : bodies){
            if (r.getType() != RobotType.HEADQUARTERS) continue;
            int teamOrdinal = r.getTeam().ordinal();
            if (hasVisibleAdamantium[teamOrdinal] && hasVisibleMana[teamOrdinal]) continue;

            MapLocation[] visibleLocations = GameWorld.getAllLocationsWithinRadiusSquaredWithoutMap(
                this.origin, 
                this.width, 
                this.height, 
                r.getLocation(),
                r.getType().visionRadiusSquared);
            for (MapLocation loc : visibleLocations){
                if (this.resourceArray[locationToIndex(loc.x, loc.y)] == ResourceType.ADAMANTIUM.resourceID){
                    hasVisibleAdamantium[teamOrdinal] = true;
                }
                else if (this.resourceArray[locationToIndex(loc.x, loc.y)] == ResourceType.MANA.resourceID){
                    hasVisibleMana[teamOrdinal] = true;
                }
            }
        } 
        if (!(hasVisibleAdamantium[0] && hasVisibleAdamantium[1])){
            throw new RuntimeException("Teams must have at least one adamantium well visible.");
        }
        
        //assert that no two currents end on the same square (avoid robot collisions)
        HashSet<MapLocation> endingLocations = new HashSet<MapLocation>();
        for (int i = 0; i < currentArray.length; i++){
            if (currentArray[i] != 0){
                MapLocation startLocation = indexToLocation(i);
                Direction currentDir = Direction.DIRECTION_ORDER[currentArray[i]];
                MapLocation finalLocation = startLocation.add(currentDir);
                if (!onTheMap(finalLocation))
                    throw new RuntimeException("Current directs robots outside of the bounds of the map");
                if (this.wallArray[locationToIndex(finalLocation)])
                    throw new RuntimeException("Current directs robots into wall");
                boolean unique = endingLocations.add(finalLocation);
                if (!unique)
                    throw new RuntimeException("Two different currents direct robots to the same location.");

            }
        }
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
                RobotInfo cri = robots[locationToIndex(current.x, current.y)];
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
                    else {
                        RobotInfo sri = robots[locationToIndex(symm.x, symm.y)];
                        if (cri != null || sri != null) {
                            if (cri == null || sri == null) {
                                possible.remove(symmetry);
                            } else if (cri.getType() != sri.getType()) {
                                possible.remove(symmetry);
                            } else if (!symmetricTeams(cri.getTeam(), sri.getTeam())) {
                                possible.remove(symmetry);
                            }
                        }
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

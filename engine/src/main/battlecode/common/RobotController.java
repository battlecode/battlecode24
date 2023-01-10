package battlecode.common;

import java.util.Map;

/**
 * A RobotController allows contestants to make their robot sense and interact
 * with the game world. When a contestant's <code>RobotPlayer</code> is
 * constructed, it is passed an instance of <code>RobotController</code> that
 * controls the newly created robot.
 */
@SuppressWarnings("unused")
public strictfp interface RobotController {

    // *********************************
    // ****** GLOBAL QUERY METHODS *****
    // *********************************

    /**
     * Returns the current round number, where round 1 is the first round of the
     * match.
     *
     * @return the current round number, where round 1 is the first round of the
     * match
     *
     * @battlecode.doc.costlymethod
     */
    int getRoundNum();

    /**
     * Returns the width of the game map. Valid x coordinates range from
     * 0 (inclusive) to the width (exclusive).
     *
     * @return the map width
     *
     * @battlecode.doc.costlymethod
     */
    int getMapWidth();

    /**
     * Returns the height of the game map. Valid y coordinates range from
     * 0 (inclusive) to the height (exclusive).
     *
     * @return the map height
     *
     * @battlecode.doc.costlymethod
     */
    int getMapHeight();

    /**
     * Returns the number of robots on your team, including Headquarters.
     * If this number ever reaches zero, you immediately lose.
     *
     * @return the number of robots on your team
     *
     * @battlecode.doc.costlymethod
     */
    int getRobotCount();

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************

    /**
     * Returns the ID of this robot.
     *
     * @return the ID of this robot
     *
     * @battlecode.doc.costlymethod
     */
    int getID();

    /**
     * Returns this robot's Team.
     *
     * @return this robot's Team
     *
     * @battlecode.doc.costlymethod
     */
    Team getTeam();

    /**
     *
     * @return this robot's type
     *
     * @battlecode.doc.costlymethod
     */
    RobotType getType();

    /**
     * Returns this robot's current location.
     *
     * @return this robot's current location
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation getLocation();

    /**
     * Returns this robot's current health.
     *
     * @return this robot's current health
     *
     * @battlecode.doc.costlymethod
     */
    int getHealth();

    /**
     * Returns the amount of the specified resource that this robot is holding
     *
     * @param rType the resource type the query is about
     * @return the amount of rType resource this robot is holding
     *
     * @battlecode.doc.costlymethod
     */
    int getResourceAmount(ResourceType rType);

    /**
     * Returns type of anchor being held by carrier
     *
     * @return type of anchor being held by carrier, null if none
     *
     * @battlecode.doc.costlymethod
     */
    Anchor getAnchor();

    /**
     * Returns num of anchors being held by robot
     * 
     * @param anchorType type of anchor to query, null for total
     * @return num of anchor being held by robot
     *
     * @battlecode.doc.costlymethod
     */
    int getNumAnchors(Anchor anchorType);

    // ***********************************
    // ****** GENERAL VISION METHODS *****
    // ***********************************

    /**
     * Checks whether a MapLocation is on the map. Will throw an exception if
     * the location is not within the vision range.
     *
     * @param loc the location to check
     * @return true if the location is on the map; false otherwise
     * @throws GameActionException if the location is not within vision range
     *
     * @battlecode.doc.costlymethod
     */
    boolean onTheMap(MapLocation loc) throws GameActionException;

    /**
     * Checks whether the given location is within the robot's vision range, and if it is on the map.
     *
     * @param loc the location to check
     * @return true if the given location is within the robot's vision range and is on the map; false otherwise
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseLocation(MapLocation loc);

    /**
     * Checks whether the given location is within the robot's action range, and if it is on the map.
     *
     * @param loc the location to check
     * @return true if the given location is within the robot's action range and is on the map; false otherwise
     *
     * @battlecode.doc.costlymethod
     */
    boolean canActLocation(MapLocation loc);

    /**
     * Checks whether a robot is at a given location. Assumes the location is valid.
     *
     * @param loc the location to check
     * @return true if a robot is at the location
     * @throws GameActionException if the location is not within vision range or on the map
     *
     * @battlecode.doc.costlymethod
     */
    boolean isLocationOccupied(MapLocation loc) throws GameActionException;

    /**
     * Checks whether a robot is at a given location. Assume the location is valid.
     *
     * @param loc the location to check
     * @return true if a robot is at the location, false if there is no robot or the location can not be sensed.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseRobotAtLocation(MapLocation loc);

    /**
     * Senses the robot at the given location, or null if there is no robot
     * there.
     *
     * @param loc the location to check
     * @return the robot at the given location
     * @throws GameActionException if the location is not within vision range
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException;

    /**
     * Tests whether the given robot exists and if it is within this robot's
     * vision range.
     *
     * @param id the ID of the robot to query
     * @return true if the given robot is within this robot's vision range and exists;
     * false otherwise
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseRobot(int id);

    /**
     * Senses information about a particular robot given its ID.
     *
     * @param id the ID of the robot to query
     * @return a RobotInfo object for the sensed robot
     * @throws GameActionException if the robot cannot be sensed (for example,
     * if it doesn't exist or is out of vision range)
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo senseRobot(int id) throws GameActionException;

    /**
     * Returns all robots within vision radius. The objects are returned in no
     * particular order.
     *
     * @return array of RobotInfo objects, which contain information about all
     * the robots you saw
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots();

    /**
     * Returns all robots that can be sensed within a certain distance of this
     * robot. The objects are returned in no particular order.
     *
     * @param radiusSquared return robots this distance away from the center of
     * this robot; if -1 is passed, all robots within vision radius are returned;
     * if radiusSquared is larger than the robot's vision radius, the vision
     * radius is used
     * @return array of RobotInfo objects of all the robots you saw
     * @throws GameActionException if the radius is negative (and not -1)
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(int radiusSquared) throws GameActionException;

    /**
     * Returns all robots of a given team that can be sensed within a certain
     * distance of this robot. The objects are returned in no particular order.
     *
     * @param radiusSquared return robots this distance away from the center of
     * this robot; if -1 is passed, all robots within vision radius are returned;
     * if radiusSquared is larger than the robot's vision radius, the vision
     * radius is used
     * @param team filter game objects by the given team; if null is passed,
     * robots from any team are returned
     * @return array of RobotInfo objects of all the robots you saw
     * @throws GameActionException if the radius is negative (and not -1)
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(int radiusSquared, Team team) throws GameActionException;

    /**
     * Returns all robots of a given team that can be sensed within a certain
     * radius of a specified location. The objects are returned in no particular
     * order.
     *
     * @param center center of the given search radius
     * @param radiusSquared return robots this distance away from the center of
     * this robot; if -1 is passed, all robots within vision radius are returned;
     * if radiusSquared is larger than the robot's vision radius, the vision
     * radius is used
     * @param team filter game objects by the given team; if null is passed,
     * objects from all teams are returned
     * @return array of RobotInfo objects of the robots you saw
     * @throws GameActionException if the radius is negative (and not -1) or the center given is null
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared, Team team) throws GameActionException;

    /**
     * Given a location, returns whether that location is passable.
     * 
     * @param loc the given location
     * @return whether that location is passable
     * @throws GameActionException if the robot cannot sense the given location
     *
     * @battlecode.doc.costlymethod
     */
    boolean sensePassability(MapLocation loc) throws GameActionException;

    /**
     * Given a location, returns the muliplier that cooldowns are multiplied by
     * for a robot on that location.
     * 
     * @param loc the given location
     * @return the cooldown multiplier of that location
     * @throws GameActionException if the robot cannot sense the given location
     */
    double senseCooldownMultiplier(MapLocation loc) throws GameActionException;

    /**
     * Given a location, returns the number of turns left on the oldest 
     * enemy destabilization there. If the location is not currently being
     * destabilized, returns -1.
     *
     * @param loc the given location
     * @return the number of turns remaining of the oldest destabilize
     * @throws GameActionException if the robot cannot sense the given location
     */
    int senseDestabilizeTurns(MapLocation loc) throws GameActionException;

    /**
     * Given a location, returns the number of turns left on the oldest allied
     * boost there. If the location is not currently being boosted, returns -1.
     *
     * @param loc the given location
     * @return the number of turns remaining of the oldest boost
     * @throws GameActionException if the robot cannot sense the given location
     */
    int senseBoostTurns(MapLocation loc) throws GameActionException;

    /**
     * Given a location, returns the index of the island located at that location.
     * 
     * @param loc the given location
     * @return the index of the island at that location or -1 if there is no island
     * @throws GameActionException if the robot cannot sense the given location
     *
     * @battlecode.doc.costlymethod
     */
    int senseIsland(MapLocation loc) throws GameActionException;

    /**
     * Returns an array containing the indexes of all islands that have at least one location within vision radius
     *
     * @return an array of nearby island indexes
     *
     * @battlecode.doc.costlymethod
     */
    int[] senseNearbyIslands();

    /**
     * Returns an array of all locations that belong to the island with the given index that are within
     * vision radius.
     * 
     * @param idx the index of the island to search for
     * @return array of nearby locations on the island
     * @throws GameActionException if the robot cannot sense the island with the specificed id
     * 
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyIslandLocations(int idx) throws GameActionException;

    /**
     * Returns an array of all locations that belong to the island with the given index that are
     * within a specified radius of your robot location.
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations within vision radius
     * are returned.
     *
     * @param radiusSquared the squared radius of all locations to be returned
     * @param idx the index of the island to search for
     * @return array of nearby locations on the island
     * @throws GameActionException if the radius is negative (and not -1) or specified island cannot be sensed
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyIslandLocations(int radiusSquared, int idx) throws GameActionException;

    /**
     * Returns an array of all locations that belong to the island with the given index that are
     * within a specified radius of the center location.
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations within vision radius
     * are returned.
     *
     * @param center the center of the search area
     * @param radiusSquared the squared radius of all locations to be returned
     * @param idx the index of the island to search for
     * @return array of nearby locations on the island
     * @throws GameActionException if the radius is negative (and not -1) or specified island cannot be sensed
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyIslandLocations(MapLocation center, int radiusSquared, int idx) throws GameActionException;

    /**
     * Return the team controlling the island with index islandIdx.
     * 
     * @param islandIdx the index of the specified island
     * @return team controlling the island.
     * @throws GameActionException if islandIdx does not correspond to a visible island
     * 
     * @battlecode.doc.costlymethod
     */
    Team senseTeamOccupyingIsland(int islandIdx) throws GameActionException;

    /**
     * Return the number of turns left to remove the anchor on the island with index idlandIdx, -1 if there is no anchor.
     * 
     * @param islandIdx the index of the specified island
     * @return number of turns left to remove the anchor on the island, -1 if there is no anchor.
     * @throws GameActionException if islandIdx does not correspond to a visible island
     * 
     * @battlecode.doc.costlymethod
     */
    int senseAnchorPlantedHealth(int islandIdx) throws GameActionException;

    /**
     * Return type of anchor on this island, null if there is no anchor.
     * 
     * @param islandIdx the index of the specified island
     * @return type of anchor on this island, null if there is no anchor.
     * @throws GameActionException if islandIdx does not correspond to a visible island
     * 
     * @battlecode.doc.costlymethod
     */
    Anchor senseAnchor(int islandIdx) throws GameActionException;

    /**
     * Sense well at location.
     *
     * @param loc to sense well at
     * @return Well at given location
     * @throws GameActionException if location can not be sensed
     *
     * @battlecode.doc.costlymethod
     */
    WellInfo senseWell(MapLocation loc) throws GameActionException;

    /**
     * Return all wells.
     *
     * @return all locations within vision radius that contain wells
     *
     * @battlecode.doc.costlymethod
     */
    WellInfo[] senseNearbyWells();

    /**
     * Return all wells within a specified radius of a center location.
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations within vision radius
     * are returned.
     *
     * @param radiusSquared the squared radius of all locations to be returned
     * @return all locations that contain wells within the radius
     * @throws GameActionException if the radius is negative (and not -1)
     *
     * @battlecode.doc.costlymethod
     */
    WellInfo[] senseNearbyWells(int radiusSquared) throws GameActionException;

    /**
     * Return all wells within a specified radius of a center location.
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations within vision radius
     * are returned.
     *
     * @param center the center of the search area
     * @param radiusSquared the squared radius of all locations to be returned
     * @return all locations that contain wells within the radius
     * @throws GameActionException if the radius is negative (and not -1)
     *
     * @battlecode.doc.costlymethod
     */
    WellInfo[] senseNearbyWells(MapLocation center, int radiusSquared) throws GameActionException;

    /**
     * Return all wells of the given resource type
     *
     * @param resourceType the resource type to filter on
     * @return all locations within vision radius that contain wells of the given resource type
     *
     * @battlecode.doc.costlymethod
     */
    WellInfo[] senseNearbyWells(ResourceType resourceType);

    /**
     * Return all wells within a specified radius of a center location of the given resource type
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations within vision radius
     * are returned.
     *
     * @param radiusSquared the squared radius of all locations to be returned
     * @param resourceType the resource type to filter on
     * @return all locations that contain wells within the radius
     * @throws GameActionException if the radius is negative (and not -1)
     *
     * @battlecode.doc.costlymethod
     */
    WellInfo[] senseNearbyWells(int radiusSquared, ResourceType resourceType) throws GameActionException;

    /**
     * Return all wells within a specified radius of a center location of the given resource type
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations within vision radius
     * are returned.
     *
     * @param center the center of the search area
     * @param radiusSquared the squared radius of all locations to be returned
     * @param resourceType the resource type to filter on
     * @return all locations that contain wells within the radius
     * @throws GameActionException if the radius is negative (and not -1)
     *
     * @battlecode.doc.costlymethod
     */
    WellInfo[] senseNearbyWells(MapLocation center, int radiusSquared, ResourceType resourceType) throws GameActionException;

    /**
     * Sense the map info at a location 
     * MapInfo includes if there is a cloud, current direction, cooldown multiplier, number of various boosts.
     *
     * @param loc to sense map at
     * @return MapInfo describing map at location
     * @throws GameActionException if location can not be sensed
     *
     * @battlecode.doc.costlymethod
     */
    MapInfo senseMapInfo(MapLocation loc) throws GameActionException;

    /**
     * Return map info for all senseable locations. 
     * MapInfo includes if there is a cloud, current direction, cooldown multiplier, number of various boosts.
     *
     * @return MapInfo about all locations within vision radius
     *
     * @battlecode.doc.costlymethod
     */
    MapInfo[] senseNearbyMapInfos();

    /**
     * Return map info for all senseable locations within a radius squared. 
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations within vision radius
     * are returned.
     * MapInfo includes if there is a cloud, current direction, cooldown multiplier, number of various boosts.
     *
     * @param radiusSquared the squared radius of all locations to be returned
     * @return MapInfo about all locations within vision radius
     * @throws GameActionException if the radius is negative (and not -1)
     *
     * @battlecode.doc.costlymethod
     */
    MapInfo[] senseNearbyMapInfos(int radiusSquared) throws GameActionException;

    /**
     * Return map info for all senseable locations within vision radius of a center location. 
     * MapInfo includes if there is a cloud, current direction, cooldown multiplier, number of various boosts.
     *
     * @param center the center of the search area
     * @return MapInfo about all locations within vision radius
     * @throws GameActionException if center is null
     *
     * @battlecode.doc.costlymethod
     */
    MapInfo[] senseNearbyMapInfos(MapLocation center) throws GameActionException;

    /**
     * Return map info for all senseable locations within a radius squared of a center location. 
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations within vision radius
     * are returned.
     * MapInfo includes if there is a cloud, current direction, cooldown multiplier, number of various boosts.
     *
     * @param center the center of the search area
     * @param radiusSquared the squared radius of all locations to be returned
     * @return MapInfo about all locations within vision radius
     * @throws GameActionException if the radius is negative (and not -1)
     *
     * @battlecode.doc.costlymethod
     */
    MapInfo[] senseNearbyMapInfos(MapLocation center, int radiusSquared) throws GameActionException;

    /**
     * Returns the location adjacent to current location in the given direction.
     *
     * @param dir the given direction
     * @return the location adjacent to current location in the given direction
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation adjacentLocation(Direction dir);

    /**
     * Returns a list of all locations within the given radiusSquared of a location.
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead.
     *
     * Checks that radiusSquared is non-negative.
     *
     * @param center the given location
     * @param radiusSquared return locations within this distance away from center
     * @return list of locations on the map and within radiusSquared of center
     * @throws GameActionException if the radius is negative (and not -1)
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared) throws GameActionException;

    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************

    /**
     * Tests whether the robot can act.
     * 
     * @return true if the robot can act
     *
     * @battlecode.doc.costlymethod
     */
    boolean isActionReady();

    /**
     * Returns the number of action cooldown turns remaining before this unit can act again.
     * When this number is strictly less than {@link GameConstants#COOLDOWN_LIMIT}, isActionReady()
     * is true and the robot can act again. This number decreases by
     * {@link GameConstants#COOLDOWNS_PER_TURN} every turn.
     *
     * @return the number of action turns remaining before this unit can act again
     *
     * @battlecode.doc.costlymethod
     */
    int getActionCooldownTurns();

    /**
     * Tests whether the robot can move.
     * 
     * @return true if the robot can move
     *
     * @battlecode.doc.costlymethod
     */
    boolean isMovementReady();

    /**
     * Returns the number of movement cooldown turns remaining before this unit can move again.
     * When this number is strictly less than {@link GameConstants#COOLDOWN_LIMIT}, isMovementReady()
     * is true and the robot can move again. This number decreases by
     * {@link GameConstants#COOLDOWNS_PER_TURN} every turn.
     *
     * @return the number of cooldown turns remaining before this unit can move again
     *
     * @battlecode.doc.costlymethod
     */
    int getMovementCooldownTurns();

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    /**
     * Checks whether this robot can move one step in the given direction.
     * Returns false if the robot is not in a mode that can move, if the target
     * location is not on the map, if the target location is occupied, if the target
     * location is impassible, or if there are cooldown turns remaining.
     *
     * @param dir the direction to move in
     * @return true if it is possible to call <code>move</code> without an exception
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMove(Direction dir);

    /**
     * Moves one step in the given direction.
     *
     * @param dir the direction to move in
     * @throws GameActionException if the robot cannot move one step in this
     * direction, such as cooldown being too high, the target location being
     * off the map, or the target destination being occupied by another robot,
     * or the target destination being impassible.
     *
     * @battlecode.doc.costlymethod
     */
    void move(Direction dir) throws GameActionException;

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    /**
     * Tests whether the robot can build a robot of the given type in the
     * given location. Checks that the robot is of a type that can build,
     * that the robot can build the desired type, that the target location is
     * on the map, that the target location is not occupied, that the robot has
     * the amount of resources it's trying to spend, and that there are no
     * cooldown turns remaining.
     *
     * @param type the type of robot to build
     * @param loc the location to spawn the robot
     * @return whether it is possible to build a robot of the given type in the
     * given direction
     *
     * @battlecode.doc.costlymethod
     */
    boolean canBuildRobot(RobotType type, MapLocation loc);

    /**
     * Builds a robot of the given type in the given location.
     *
     * @param type the type of robot to build
     * @param loc the location to spawn the unit
     * @throws GameActionException if the conditions of <code>canBuildRobot</code>
     * are not all satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void buildRobot(RobotType type, MapLocation loc) throws GameActionException;

    // ****************************
    // ***** ATTACK METHODS ***** 
    // ****************************

    /**
     * Tests whether this robot can attack the given location.
     * 
     * Checks that the robot is an attacking type unit and that the given location
     * is within the robot's reach (based on attack type). Also checks that 
     * there are no cooldown turns remaining and that a robot of the same team
     * is not at the given location.
     *
     * @param loc target location to attack 
     * @return whether it is possible to attack the given location
     *
     * @battlecode.doc.costlymethod
     */
    boolean canAttack(MapLocation loc);

    /** 
     * Attack a given location.
     *
     * @param loc the target location to attack
     * @throws GameActionException if conditions for attacking are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void attack(MapLocation loc) throws GameActionException;

    // ***********************************
    // ******** BOOSTERS METHODS *********
    // ***********************************

    /**
     * Tests whether this robot is able to boost
     * 
     * Checks that the robot can boost other units. Also checks that there are no 
     * cooldown turns remaining.
     *
     * @return whether it is possible for this robot to boost
     *
     * @battlecode.doc.costlymethod
     */
    boolean canBoost();


    /** 
     * Boosts at a given location.
     *
     * @throws GameActionException if conditions for boosting are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void boost() throws GameActionException;

    // ***********************************
    // ****** DESTABILIZER METHODS *******
    // ***********************************

    /**
     * Tests whether this robot is able to destabilize
     * 
     * Checks that the robot can destabilize other units. Also checks that there are no 
     * cooldown turns remaining and the location is within range to act upon.
     *
     * @param loc the location at which the destabilizing attack will be centered
     * @return whether it is possible for this robot to destabilize
     *
     * @battlecode.doc.costlymethod
     */
    boolean canDestabilize(MapLocation loc);


    /** 
     * Destabilizes at a given location.
     *
     * @param loc the location at which the destabilizing attack will be centered
     * @throws GameActionException if conditions for destabilizing are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void destabilize(MapLocation loc) throws GameActionException;


    // ***************************
    // ***** CARRIER METHODS *****
    // ***************************

    /**
     * Tests whether the robot can collect resource from a given location.
     * 
     * Checks that the robot is a Carrier, the given location is a valid well location, 
     * and there are no cooldown turns remaining. 
     * 
     * Valid locations must be the current location or adjacent to the current 
     * location. 
     * 
     * Checks that carrier can collect the amount (amount does not exceed
     * current well rate, carrier has sufficient capacity).
     *
     * @param loc target location to collect 
     * @param amount amount to be collected, -1 to collect max possible
     * @return whether it is possible to collect amount to the given location
     *
     * @battlecode.doc.costlymethod
     */
    boolean canCollectResource(MapLocation loc, int amount);

    /** 
     * Collect resource from the given location. 
     *
     * @param loc target well location
     * @param amount amount to collect, -1 to collect max possible
     * @throws GameActionException if conditions for collecting are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void collectResource(MapLocation loc, int amount) throws GameActionException;

    /**
     * Tests whether the robot can transfer resource to a given location.
     * 
     * Checks that the robot is a Carrier, the given location is a valid HQ
     * or well location, and there are no cooldown turns remaining. 
     * 
     * Valid locations must be the current location or adjacent to the current 
     * location. 
     * 
     * Checks that carrier can transfer the amount (donor has sufficient 
     * resource). Wells can only be transferred to. 
     *
     * @param loc target location to transfer to
     * @param rType type of resource to transfer
     * @param amount amount to be transferred (negative = from loc, positive = to)
     * @return whether it is possible to transfer amount to the given location
     *
     * @battlecode.doc.costlymethod
     */
    boolean canTransferResource(MapLocation loc, ResourceType rType, int amount);

    /** 
     * Transfers resource to given location. Transferred material is 
     * limited by carrier capacity. 
     * 
     * @param loc target location to transfer to/from
     * @param rType type of resource to transfer
     * @param amount amount to be transferred (negative = from loc, positive = to)
     * @throws GameActionException if conditions for transferring are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void transferResource(MapLocation loc, ResourceType rType, int amount) throws GameActionException;

    /**
     * Tests if the current robot can build an anchor of type anchor. 
     * 
     * To be able to build an anchor a robot must be a headquarter and 
     * have sufficient resources for the anchor being built
     * 
     * @param anchor the anchor to be built
     * @return whether it is possible to build the specified anchor
     * 
     * @battlecode.doc.costlymethod
     */
    boolean canBuildAnchor(Anchor anchor);

    /**
     * Builds an anchor of type anchor. Subtracts the necessary resources.
     * 
     * @param anchor the anchor to be built
     * @throws GameActionException if conditions for building anchors are not satsified
     * 
     * @battlecode.doc.costlymethod
     */
    void buildAnchor(Anchor anchor) throws GameActionException;

    /**
     * Tests whether the robot can take an anchor from a HQ.
     * 
     * Checks that the robot is a Carrier, the given location is a valid HQ,
     * with the desired anchor and there are no cooldown turns remaining. 
     * 
     * Valid locations must be the current location or adjacent to the current 
     * location. 
     * 
     * Checks that carrier has sufficient capacity for the anchor. 
     *
     * @param loc target HQ location
     * @param anchorType type of anchor to take
     * @return whether it is possible to take anchor from given location
     * 
     * @battlecode.doc.costlymethod
     */
    boolean canTakeAnchor(MapLocation loc, Anchor anchorType);

    /** 
     * Take an anchor from the given location. 
     *
     * @param loc target HQ location
     * @param anchorType type of anchor to take
     * @throws GameActionException if conditions for taking are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void takeAnchor(MapLocation loc, Anchor anchorType) throws GameActionException;

    /**
     * Tests whether the robot can place an anchor at its current location.
     * 
     * Checks that the robot is a Carrier, the robot is holding an anchor,
     * the given location is a valid sky island, and there are no cooldown turns remaining. 
     * 
     * Valid locations must be a sky island not already controlled by the opposing team. 
     *
     * @return whether it is possible to place an anchor
     *
     * @battlecode.doc.costlymethod
     */
    boolean canPlaceAnchor();

    /** 
     * Places an anchor at the current location. 
     * 
     * @throws GameActionException if conditions for placing anchors are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void placeAnchor() throws GameActionException;

    // ***********************************
    // ****** COMMUNICATION METHODS ****** 
    // ***********************************

    /** 
     * Given an index, returns the value at that index in the team array.
     *
     * @param index the index in the team's shared array, 0-indexed
     * @return the value at that index in the team's shared array,
     * @throws GameActionException if the index is invalid
     *
     * @battlecode.doc.costlymethod
     */
    int readSharedArray(int index) throws GameActionException;

    /**
     * Test whether this robot can write to the shared array.
     * 
     * A robot can write to the shared array when it is within range
     * of a signal amplifier, a planted reality anchor, or a headquarter.
     * 
     * @param index the index in the team's shared array, 0-indexed
     * @param value the value to set that index to
     * @return whether it is possible to write to the shared array
     * 
     * @battlecode.doc.costlymethod
     */
    boolean canWriteSharedArray(int index, int value);

    /** 
     * Sets the team's array value at a specified index if the robot is allowed
     * to write to the array. No change occurs if the index or value is invalid
     * or if the robot is not able to write to the array (see canWriteSharedArray).
     *
     * @param index the index in the team's shared array, 0-indexed
     * @param value the value to set that index to
     * @throws GameActionException if the index is invalid, the value
     *         is out of bounds, or the robot cannot write to the array.
     *
     * @battlecode.doc.costlymethod
     */
    void writeSharedArray(int index, int value) throws GameActionException;

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    /**
     * Destroys the robot. 
     *
     * @battlecode.doc.costlymethod
    **/
    void disintegrate();
    
    /**
     * Causes your team to lose the game. It's like typing "gg."
     *
     * @battlecode.doc.costlymethod
     */
    void resign();

    // ***********************************
    // ******** DEBUG METHODS ************
    // ***********************************

    /**
     * Sets the indicator string for this robot for debugging purposes. Only the first
     * {@link GameConstants#INDICATOR_STRING_MAX_LENGTH} characters are used.
     *
     * @param string the indicator string this round
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorString(String string);

    /**
     * Draw a dot on the game map for debugging purposes.
     *
     * @param loc the location to draw the dot
     * @param red the red component of the dot's color
     * @param green the green component of the dot's color
     * @param blue the blue component of the dot's color
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorDot(MapLocation loc, int red, int green, int blue);

    /**
     * Draw a line on the game map for debugging purposes.
     *
     * @param startLoc the location to draw the line from
     * @param endLoc the location to draw the line to
     * @param red the red component of the line's color
     * @param green the green component of the line's color
     * @param blue the blue component of the line's color
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue);
}

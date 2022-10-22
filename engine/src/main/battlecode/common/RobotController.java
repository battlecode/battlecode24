package battlecode.common;
import java.util.ArrayList;

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
     * Returns the number of robots on your team, including Archons.
     * If this number ever reaches zero, you immediately lose.
     *
     * @return the number of robots on your team
     *
     * @battlecode.doc.costlymethod
     */
    int getRobotCount();

    /**
     * Returns the number of Archons on your team.
     * If this number ever reaches zero, you immediately lose.
     *
     * @return the number of Archons on your team
     *
     * @battlecode.doc.costlymethod
     */
    int getArchonCount();

    /**
     * Returns the amount of lead a team has in its reserves.
     *
     * @param team the team being queried.
     * @return the amount of lead a team has in its reserves.
     *
     * @battlecode.doc.costlymethod
     */
    int getTeamLeadAmount(Team team);

    /**
     * Returns the amount of gold a team has in its reserves.
     *
     * @param team the team being queried.
     * @return the amount of gold a team has in its reserves.
     *
     * @battlecode.doc.costlymethod
     */
    int getTeamGoldAmount(Team team);

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
     * Returns this robot's type (MINER, ARCHON, BUILDER, etc.).
     *
     * @return this robot's type
     *
     * @battlecode.doc.costlymethod
     */
    RobotType getType();

    /**
     * Returns this robot's mode (DROID, PROTOTYPE, TURRET, PORTABLE).
     *
     * @return this robot's mode
     *
     * @battlecode.doc.costlymethod
     */
    RobotMode getMode();

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
     * Returns this robot's current level.
     *
     * @return this robot's current level
     *
     * @battlecode.doc.costlymethod
     */
    int getLevel();

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
     * Checks whether a point at the given radius squared is within the robot's vision range.
     *
     * @param radiusSquared the radius to check
     * @return true if the given radius is within the robot's vision range; false otherwise
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseRadiusSquared(int radiusSquared);

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
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(int radiusSquared);

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
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(int radiusSquared, Team team);

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
     * @return sorted array of RobotInfo objects of the robots you saw
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared, Team team);

    /**
     * Given a location, returns the rubble of that location.
     *
     * Higher rubble means that robots on this location may be penalized
     * greater cooldowns for making actions.
     * 
     * @param loc the given location
     * @return the rubble of that location
     * @throws GameActionException if the robot cannot sense the given location
     *
     * @battlecode.doc.costlymethod
     */
    int senseRubble(MapLocation loc) throws GameActionException;

    /**
     * Given a location, returns the lead count of that location.
     * 
     * @param loc the given location
     * @return the amount of lead at that location
     * @throws GameActionException if the robot cannot sense the given location
     *
     * @battlecode.doc.costlymethod
     */
    int senseLead(MapLocation loc) throws GameActionException;

    /**
     * Given a location, returns the gold count of that location.
     * 
     * @param loc the given location
     * @return the amount of gold at that location
     * @throws GameActionException if the robot cannot sense the given location
     *
     * @battlecode.doc.costlymethod
     */
    int senseGold(MapLocation loc) throws GameActionException;

    /**
     * Return all locations that contain a nonzero amount of lead.
     *
     * @return all locations within vision radius that contain a nonzero amount of lead
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithLead();

    /**
     * Return all locations that contain a nonzero amount of lead, within a
     * specified radius of your robot location.
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations with vision radius
     * are returned.
     *
     * @param radiusSquared the squared radius of all locations to be returned
     * @return all locations that contain a nonzero amount of lead within the radius
     * @throws GameActionException if the radius is negative
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithLead(int radiusSquared) throws GameActionException;

    /**
     * Return all locations that contain a nonzero amount of lead, within a
     * specified radius of a center location.
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations with vision radius
     * are returned.
     *
     * @param center the center of the search area
     * @param radiusSquared the squared radius of all locations to be returned
     * @return all locations that contain a nonzero amount of lead within the radius
     * @throws GameActionException if the radius is negative
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithLead(MapLocation center, int radiusSquared) throws GameActionException;

    /**
     * Return all locations that contain at least a certain amount of lead, within a
     * specified radius of your robot location.
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations with vision radius
     * are returned.
     *
     * @param radiusSquared the squared radius of all locations to be returned
     * @param minLead the minimum amount of lead
     * @return all locations that contain at least minLead lead within the radius
     * @throws GameActionException if the radius is negative
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithLead(int radiusSquared, int minLead) throws GameActionException;

    /**
     * Return all locations that contain at least a certain amount of lead, within a
     * specified radius of a center location.
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations with vision radius
     * are returned.
     *
     * @param center the center of the search area
     * @param radiusSquared the squared radius of all locations to be returned
     * @param minLead the minimum amount of lead
     * @return all locations that contain at least minLead lead within the radius
     * @throws GameActionException if the radius is negative
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithLead(MapLocation center, int radiusSquared, int minLead) throws GameActionException;

    /**
     * Return all locations that contain a nonzero amount of gold.
     *
     * @return all locations within vision radius that contain a nonzero amount of gold
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithGold();

    /**
     * Return all locations that contain a nonzero amount of gold, within a
     * specified radius of your robot location.
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations with vision radius
     * are returned.
     *
     * @param radiusSquared the squared radius of all locations to be returned
     * @return all locations that contain a nonzero amount of gold within the radius
     * @throws GameActionException if the radius is negative
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithGold(int radiusSquared) throws GameActionException;

    /**
     * Return all locations that contain a nonzero amount of gold, within a
     * specified radius of a center location.
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations with vision radius
     * are returned.
     *
     * @param center the center of the search area
     * @param radiusSquared the squared radius of all locations to be returned
     * @return all locations that contain a nonzero amount of gold within the radius
     * @throws GameActionException if the radius is negative
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithGold(MapLocation center, int radiusSquared) throws GameActionException;

    /**
     * Return all locations that contain at least a certain amount of gold, within a
     * specified radius of your robot location.
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations with vision radius
     * are returned.
     *
     * @param radiusSquared the squared radius of all locations to be returned
     * @param minGold the minimum amount of gold
     * @return all locations that contain at least minGold gold within the radius
     * @throws GameActionException if the radius is negative
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithGold(int radiusSquared, int minGold) throws GameActionException;

    /**
     * Return all locations that contain at least a certain amount of gold, within a
     * specified radius of a center location.
     * If radiusSquared is larger than the robot's vision radius, uses the robot's
     * vision radius instead. If -1 is passed, all locations with vision radius
     * are returned.
     *
     * @param center the center of the search area
     * @param radiusSquared the squared radius of all locations to be returned
     * @param minGold the minimum amount of gold
     * @return all locations that contain at least minGold gold within the radius
     * @throws GameActionException if the radius is negative
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithGold(MapLocation center, int radiusSquared, int minGold) throws GameActionException;

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
     * @throws GameActionException if the radius is negative
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

    /**
     * Tests whether the robot can transform.
     *
     * Checks if the robot's mode is TURRET or PORTABLE. Also checks action
     * or movement cooldown turns, depending on the robot's current mode.
     * 
     * @return true if the robot can transform
     *
     * @battlecode.doc.costlymethod
     */
    boolean isTransformReady();

    /**
     * Returns the number of cooldown turns remaining before this unit can transform again.
     * When this number is strictly less than {@link GameConstants#COOLDOWN_LIMIT}, isTransformReady()
     * is true and the robot can transform again. This number decreases by
     * {@link GameConstants#COOLDOWNS_PER_TURN} every turn.
     *
     * @return the number of cooldown turns remaining before this unit can transform again
     * @throws GameActionException if the robot's mode is not TURRET or PORTABLE
     *
     * @battlecode.doc.costlymethod
     */
    int getTransformCooldownTurns() throws GameActionException;

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    /**
     * Checks whether this robot can move one step in the given direction.
     * Returns false if the robot is not in a mode that can move, if the target
     * location is not on the map, if the target location is occupied, or if
     * there are cooldown turns remaining.
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
     * off the map, or the target destination being occupied by another robot
     *
     * @battlecode.doc.costlymethod
     */
    void move(Direction dir) throws GameActionException;

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    /**
     * Tests whether the robot can build a robot of the given type in the
     * given direction. Checks that the robot is of a type that can build,
     * that the robot can build the desired type, that the target location is
     * on the map, that the target location is not occupied, that the robot has
     * the amount of lead/gold it's trying to spend, and that there are no
     * cooldown turns remaining.
     *
     * @param type the type of robot to build
     * @param dir the direction to build in
     * @return whether it is possible to build a robot of the given type in the
     * given direction
     *
     * @battlecode.doc.costlymethod
     */
    boolean canBuildRobot(RobotType type, Direction dir);

    /**
     * Builds a robot of the given type in the given direction.
     *
     * @param type the type of robot to build
     * @param dir the direction to spawn the unit
     * @throws GameActionException if the conditions of <code>canBuildRobot</code>
     * are not all satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void buildRobot(RobotType type, Direction dir) throws GameActionException;

    // ****************************
    // ***** LAUNCHER METHODS ***** 
    // ****************************

    /**
     * Tests whether this robot can attack the given location.
     * 
     * Checks that the robot is an attacking type unit and that the given location
     * is within the robot's reach (based on attack type). Also checks that an 
     * enemy unit exists in the given square, and there are no cooldown turns remaining.
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

    // **************************************
    // ******** DESTABILIZER METHODS ********
    // **************************************

    /**
     * Tests whether this robot can envision an anomaly centered at the robot's location.
     * 
     * Checks that the robot is a sage, and there are no cooldown turns remaining.
     *
     * @param anomaly the type of anomaly being queried
     * @return whether it is possible to envision an anomaly centered at the robots location
     *
     * @battlecode.doc.costlymethod
     */
    boolean canEnvision(AnomalyType anomaly);

    /** 
     * Envision an anomaly centered at the robot's location.
     *
     * @param anomaly the type of anomaly to envision
     * @throws GameActionException if conditions for envisioning are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void envision(AnomalyType anomaly) throws GameActionException;

   // ***********************************
    // ******** BOOSTERS METHODS *********
    // ***********************************

    /**
     * Tests whether this robot is able to boost
     * 
     * Checks that the robot can boost other units. Also checks that there are no 
     * cooldown turns remaining.
     *
     * @param  none
     * @return whether it is possible for this robot to boost
     *
     * @battlecode.doc.costlymethod
     */
    boolean canBoost();


    /** 
     * Boosts at a given location.
     *
     * @param none
     * @throws GameActionException if conditions for boosting are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void boost() throws GameActionException;


    // ***************************
    // ***** CARRIER METHODS *****
    // ***************************

    /**
     * Tests whether the robot can mine lead at a given location.
     * 
     * Checks that the robot is a Miner, and the given location is a valid 
     * mining location. Valid mining locations must be the current location 
     * or adjacent to the current location. Valid mining locations must also
     * have positive lead amounts. Also checks that no cooldown turns remain.
     *
     * @param loc target location to mine 
     * @return whether it is possible to mine at the given location
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMineLead(MapLocation loc);

    /** 
     * Mine lead at a given location.
     *
     * @param loc target location to mine
     * @throws GameActionException if conditions for mining are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void mineLead(MapLocation loc) throws GameActionException;

    /**
     * Tests whether the robot can mine gold at a given location.
     * 
     * Checks that the robot is a Miner, that the given location is a valid 
     * mining location. Valid mining locations must be the current location 
     * or adjacent to the current location. Valid mining locations must also
     * have positive gold amounts. Also checks that no cooldown turns remain.
     *
     * @param loc target location to mine 
     * @return whether it is possible to mine at the given location
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMineGold(MapLocation loc);

    /** 
     * Mine a gold at given location.
     *
     * @param loc target location to mine
     * @throws GameActionException if conditions for mining are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void mineGold(MapLocation loc) throws GameActionException;

    // ***************************
    // **** AMPLIFIER METHODS **** 
    // ***************************

    /**
     * Tests whether this robot can mutate the building at the given location.
     * 
     * Checks that the robot is a Builder, that the given location is a valid 
     * mutate location. Valid mutate locations must be adjacent to the current 
     * location and contain a mutable building. The mutation must also be
     * affordable, and there must be no cooldown turns remaining.
     *
     * @param loc target location to mutate 
     * @return whether it is possible to mutate at the given location
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMutate(MapLocation loc);

    /** 
     * Mutate a building at a given location.
     *
     * @param loc target location of the building to mutate
     * @throws GameActionException if conditions for mutating are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void mutate(MapLocation loc) throws GameActionException;

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
     * Sets a team's array value at a specified index.
     * No change occurs if the index or value is invalid.
     *
     * @param index the index in the team's shared array, 0-indexed
     * @param value the value to set that index to
     * @throws GameActionException if the index is invalid, or the value
     *         is out of bounds
     *
     * @battlecode.doc.costlymethod
     */
    void writeSharedArray(int index, int value) throws GameActionException;

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    /**
     * @return the anomaly schedule
     *
     * @battlecode.doc.costlymethod
     */
    AnomalyScheduleEntry[] getAnomalySchedule();

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

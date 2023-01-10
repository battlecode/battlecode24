package battlecode.common;

import static battlecode.common.GameActionExceptionType.*;

import java.util.Arrays;

public class MapInfo {

    private static int BOOST_INDEX = 0;
    private static int DESTABILIZE_INDEX = 1;

    private MapLocation loc;

    private boolean hasCloud;

    private double[] cooldownMultipliers;

    private Direction currentDirection;

    private int[][] numActiveElements; // [Team.A, Team.B][Booster, Destabilizer]

    private int[][] turnsLeft; // [Team.A, Team.B][Booster, Destabilizer]

    public MapInfo(MapLocation loc, boolean hasCloud, double[] cooldownMultipliers, Direction curDirection, int[][] numActiveElements, int[][] turnsLeft){
        this.loc = loc;
        this.hasCloud = hasCloud;
        assert(cooldownMultipliers.length == 2);
        this.cooldownMultipliers = cooldownMultipliers;
        this.currentDirection = curDirection;
        assert(numActiveElements.length == 2);
        assert(numActiveElements[0].length == 2);
        assert(numActiveElements[1].length == 2);
        this.numActiveElements = numActiveElements;
        assert(turnsLeft.length == 2);
        assert(turnsLeft[0].length == 2);
        assert(turnsLeft[1].length == 2);
        this.turnsLeft = turnsLeft;
    }

    private void assertValidTeam(Team team) throws GameActionException {
        if (team != Team.A && team != Team.B) {
            throw new GameActionException(CANT_DO_THAT, "Must pass valid team to get info about a space");
        }
    }

    /**
     * Returns the cooldownMultiplier currently on this square.
     * 
     * @param team the team to query the cooldown multiplier for
     * @return the cooldownMultiplier currently on this square
     * @throws GameActionException if team is not valid
     * 
     * @battlecode.doc.costlymethod
     */
    public double getCooldownMuliplier(Team team) throws GameActionException {
        assertValidTeam(team);
        return this.cooldownMultipliers[team.ordinal()];
    }

    /**
     * Returns the location of this square
     * 
     * @return the location of this square
     * 
     * @battlecode.doc.costlymethod
     */
    public MapLocation getMapLocation(){
        return loc;
    }

    /**
     * Returns the direction of the current on this square
     * 
     * @return the direction of the current on this square
     * 
     * @battlecode.doc.costlymethod
     */
    public Direction getCurrentDirection(){
        return this.currentDirection;
    }
  
    /**
     * Returns the number of boosts currently applying to this square.
     * 
     * @param team the team to query the boosts for
     * @return the number of boosts currently applying to this square
     * @throws GameActionException if team is not valid
     * 
     * @battlecode.doc.costlymethod
     */
    public int getNumBoosts(Team team) throws GameActionException {
        assertValidTeam(team);
        return this.numActiveElements[team.ordinal()][BOOST_INDEX];
    }

    /**
     * Returns the number of destabilizier currently applying to this square.
     * 
     * @param team the team to query the destabilizes for
     * @return the number of destabilizer currently applying to this square
     * @throws GameActionException if team is not valid
     * 
     * @battlecode.doc.costlymethod
     */
    public int getNumDestabilizers(Team team) throws GameActionException {
        assertValidTeam(team);
        return this.numActiveElements[team.ordinal()][DESTABILIZE_INDEX];
    }

    /**
     * Returns the number of turns left before a booster is removed
     * 
     * @param team the team to query the remaining boost turns for
     * @return the number of turns left before a booster is removed, -1 if none active
     * @throws GameActionException if team is not valid
     * 
     * @battlecode.doc.costlymethod
     */
    public int getBoostTurnsLeft(Team team) throws GameActionException {
        assertValidTeam(team);
        return this.turnsLeft[team.ordinal()][BOOST_INDEX];
    }

    /**
     * Returns the number of turns left before a destabilizer is removed
     * 
     * @param team the team to query the remaining destabilize turns for
     * @return the number of turns left before a destabilizer is removed, -1 if none active
     * @throws GameActionException if team is not valid
     * 
     * @battlecode.doc.costlymethod
     */
    public int getDestabilizerTurnsLeft(Team team) throws GameActionException {
        assertValidTeam(team);
        return this.turnsLeft[team.ordinal()][DESTABILIZE_INDEX];
    }

    public String toString(){
        return "Location{" +
                "loc=" + loc +
                ", cloud=" +  this.hasCloud +
                ", cooldownMultipliers=" +  Arrays.toString(this.cooldownMultipliers) +
                ", current=" + this.currentDirection +
                '}';

    }

}
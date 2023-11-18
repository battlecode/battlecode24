package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.SkillType;
import battlecode.common.Team;
import java.util.*;
import static battlecode.common.GameActionExceptionType.*;

/**
 * This class is used to hold information regarding team specific values such as
 * team names.
 */
public class TeamInfo {

    private GameWorld gameWorld;
    private int[] breadCounts; 
    private int[] tierThree;
    private int[] tierTwo;
    private int[][] sharedArrays; 
    private int[] totalFlagsCaptured;

    private int[] oldBreadCounts;

    /**
     * Create a new representation of TeamInfo
     *
     * @param gameWorld the gameWorld the teams exist in
     */
    public TeamInfo(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        this.breadCounts = new int[2];
        this.sharedArrays = new int[2][GameConstants.SHARED_ARRAY_LENGTH];
        this.totalFlagsCaptured = new int[2];
        this.oldBreadCounts = new int[2];
        this.tierThree = new int[2];
        this.tierTwo = new int[2];
    }
    
    // *********************************
    // ***** GETTER METHODS ************
    // *********************************

    /**
     * Get the amount of bread.
     * 
     * @param team the team to query
     * @return the team's bread count
     */
    public int getBread(Team team) {
    	return this.breadCounts[team.ordinal()];
    }

    /**
     * Get the total number of flags captured by the team over the game
     * @param team the team to query
     * @return the total flags captured
     */
    public int getFlagsCaptured(Team team) {
        return this.totalFlagsCaptured[team.ordinal()];
    }
    
    /**
     * Reads the shared array value.
     *
     * @param team the team to query
     * @param index the index in the array
     * @return the value at that index in the team's shared array
     */
    public int readSharedArray(Team team, int index) {
        return this.sharedArrays[team.ordinal()][index];
    }

    // *********************************
    // ***** UPDATE METHODS ************
    // *********************************

    
    /**
     * Add to the amount of bread. If amount is negative, subtract from bread instead.
     * 
     * @param team the team to query
     * @param amount the change in the bread count
     * @throws IllegalArgumentException if the resulting amount of bread is negative
     */
    public void addBread(Team team, int amount) throws IllegalArgumentException {
    	if (this.breadCounts[team.ordinal()] + amount < 0) {
    		throw new IllegalArgumentException("Invalid bread change");
    	}
    	this.breadCounts[team.ordinal()] += amount;
    }

    private void checkWin (Team team){ 
        if (this.totalFlagsCaptured[team.ordinal()] < GameConstants.NUMBER_FLAGS) {
            throw new InternalError("Reporting incorrect win");
        }
        this.gameWorld.gameStats.setWinner(team);
        this.gameWorld.gameStats.setDominationFactor(DominationFactor.CAPTURE);
    }

    /**
     * Increment the number of flags captured for a team.
     * @param team the team to query
     */
    public void captureFlag(Team team) {
        this.totalFlagsCaptured[team.ordinal()]++;
        if (this.totalFlagsCaptured[team.ordinal()] >= GameConstants.NUMBER_FLAGS){
            checkWin(team);
        }
    }

    /**
     * Counts number of tier 3 units.
     * @param team to query
     * @return number of level 3 units
     */
    public int getTierThree(Team team){
        ArrayList<InternalRobot> robots = new ArrayList<InternalRobot>();
        SkillType[] skills = {SkillType.HEAL, SkillType.ATTACK, SkillType.BUILD};
        gameWorld.getObjectInfo().eachRobot((robot)->{
            for (SkillType s: skills){
                if (robot.getLevel(s) >= 3){
                    robots.add(robot);
                    return true;
                }
            }
            return true;
        });
        return robots.size();
    }

    /**
     * Counts number of tier 2 units.
     * @param team to query
     * @return number of level 2 units
     */
    public int getTierTwo(Team team){
        ArrayList<InternalRobot> robots = new ArrayList<InternalRobot>();
        SkillType[] skills = {SkillType.HEAL, SkillType.ATTACK, SkillType.BUILD};
        gameWorld.getObjectInfo().eachRobot((robot)->{
            for (SkillType s: skills){
                if (robot.getLevel(s) >= 3){
                    return true;
                }
            }
            for (SkillType s: skills){
                if (robot.getLevel(s) == 2){
                    robots.add(robot);
                    return true;
                }
            }
            return true;
        });
        return robots.size();
    }

    /**
     * Sets an index in the team's shared array to a given value.
     *
     * @param team the team to query
     * @param index the index in the shared array
     * @param value the new value
     */
    public void writeSharedArray(Team team, int index, int value) {
        this.sharedArrays[team.ordinal()][index] = value;
    }
    
    public int getRoundBreadChange(Team team) {
    	return this.breadCounts[team.ordinal()] - this.oldBreadCounts[team.ordinal()];
    }

    public void processEndOfRound() {
        this.oldBreadCounts[0] = this.breadCounts[0];
        this.oldBreadCounts[1] = this.breadCounts[1];
    }


}

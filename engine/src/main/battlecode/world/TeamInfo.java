package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.Team;
import java.util.*;
import static battlecode.common.GameActionExceptionType.*;

/**
 * This class is used to hold information regarding team specific values such as
 * team names.
 */
public class TeamInfo {

    private GameWorld gameWorld;
    private int[] leadCounts;
    private int[] goldCounts;
    private int[][] sharedArrays;

    // for reporting round statistics to client
    private int[] oldLeadCounts;
    private int[] oldGoldCounts;

    /**
     * Create a new representation of TeamInfo
     *
     * @param gameWorld the gameWorld the teams exist in
     */
    public TeamInfo(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        this.leadCounts = new int[2];
        this.goldCounts = new int[2];
        this.sharedArrays = new int[2][GameConstants.SHARED_ARRAY_LENGTH];
        this.oldLeadCounts = new int[2];
        this.oldGoldCounts = new int[2];
    }
    
    // *********************************
    // ***** GETTER METHODS ************
    // *********************************

    /**
     * Get the amount of lead.
     *
     * @param team the team to query
     * @return the team's lead count
     */
    public int getLead(Team team) {
        return this.leadCounts[team.ordinal()];
    }

    /**
     * Get the amount of gold.
     *
     * @param team the team to query
     * @return the team's gold count
     */
    public int getGold(Team team) {
        return this.goldCounts[team.ordinal()];
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
     * Add to the amount of lead. If amount is negative, subtract from lead instead. 
     * 
     * @param team the team to query
     * @param amount the change in the lead count
     * @throws IllegalArgumentException if the resulting amount of lead is negative
     */
    public void addLead(Team team, int amount) throws IllegalArgumentException {
        if (this.leadCounts[team.ordinal()] + amount < 0) {
            throw new IllegalArgumentException("Invalid lead change");
        }
        this.leadCounts[team.ordinal()] += amount;
    }

    /**
     * Add to the amount of gold. If amount is negative, subtract from gold instead. 
     * 
     * @param team the team to query
     * @param amount the change in the gold count
     * @throws IllegalArgumentException if the resulting amount of gold is negative
     */
    public void addGold(Team team, int amount) throws IllegalArgumentException {
        if (this.goldCounts[team.ordinal()] + amount < 0) {
            throw new IllegalArgumentException("Invalid gold change");
        }
        this.goldCounts[team.ordinal()] += amount;
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

    public int getRoundLeadChange(Team team) {
        return this.leadCounts[team.ordinal()] - this.oldLeadCounts[team.ordinal()];
    }

    public int getRoundGoldChange(Team team) {
        return this.goldCounts[team.ordinal()] - this.oldGoldCounts[team.ordinal()];
    }

    public void processEndOfRound() {
        this.oldLeadCounts[0] = this.leadCounts[0];
        this.oldLeadCounts[1] = this.leadCounts[1];
        this.oldGoldCounts[0] = this.goldCounts[0];
        this.oldGoldCounts[1] = this.goldCounts[1];
    }
}

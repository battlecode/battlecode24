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

    /**
     * Create a new representation of TeamInfo
     *
     * @param gameWorld the gameWorld the teams exist in
     */
    public TeamInfo(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        this.leadCount = new int[2];
        this.goldCount = new int[2];
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
        return this.leadCount[team.ordinal()];
    }

    /**
     * Get the amount of gold.
     *
     * @param team the team to query
     * @return the team's gold count
     */
    public int getGold(Team team) {
        return this.goldCount[team.ordinal()];
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
        if (this.leadCount[team.ordinal()] + amount < 0) {
            throw new IllegalArgumentException("Invalid lead change");
        }
        this.leadCount[team.ordinal()] += amount;
    }

    /**
     * Add to the amount of gold. If amount is negative, subtract from gold instead. 
     * 
     * @param team the team to query
     * @param amount the change in the gold count
     * @throws IllegalArgumentException if the resulting amount of gold is negative
     */
    public void addGold(Team team, int amount) throws IllegalArgumentException {
        if (this.goldCount[team.ordinal()] + amount < 0) {
            throw new IllegalArgumentException("Invalid gold change");
        }
        this.goldCount[team.ordinal()] += amount;
    }
}

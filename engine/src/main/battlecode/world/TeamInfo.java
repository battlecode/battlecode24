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
    private int[] elixirCounts;
    private int[] manaCounts;
    private int[] adamantiumCounts; 
    private int[][] sharedArrays; 

    // for reporting round statistics to client
    private int[] oldElixirCounts;
    private int[] oldManaCounts;
    private int[] oldAdamantiumCounts;

    /**
     * Create a new representation of TeamInfo
     *
     * @param gameWorld the gameWorld the teams exist in
     */
    public TeamInfo(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        this.elixirCounts = new int[2];
        this.manaCounts = new int[2];
        this.adamantiumCounts = new int[2];
        this.sharedArrays = new int[2][GameConstants.SHARED_ARRAY_LENGTH];
        this.oldElixirCounts = new int[2];
        this.oldManaCounts = new int[2];
        this.oldAdamantiumCounts = new int[2];
    }
    
    // *********************************
    // ***** GETTER METHODS ************
    // *********************************

    /**
     * Get the amount of elixir.
     *
     * @param team the team to query
     * @return the team's elixir count
     */
    public int getElixir(Team team) {
        return this.elixirCounts[team.ordinal()];
    }

    /**
     * Get the amount of mana.
     *
     * @param team the team to query
     * @return the team's mana count
     */
    public int getMana(Team team) {
        return this.manaCounts[team.ordinal()];
    }

    /**
     * Get the amount of adamantium.
     * 
     * @param team the team to query
     * @return the team's adamantium count
     */
    public int getAdamantium(Team team) {
    	return this.adamantiumCounts[team.ordinal()];
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
     * Add to the amount of elixir. If amount is negative, subtract from elixir instead. 
     * 
     * @param team the team to query
     * @param amount the change in the elixir count
     * @throws IllegalArgumentException if the resulting amount of elixir is negative
     */
    public void addElixir(Team team, int amount) throws IllegalArgumentException {
        if (this.elixirCounts[team.ordinal()] + amount < 0) {
            throw new IllegalArgumentException("Invalid elixir change");
        }
        this.elixirCounts[team.ordinal()] += amount;
    }

    /**
     * Add to the amount of mana. If amount is negative, subtract from mana instead. 
     * 
     * @param team the team to query
     * @param amount the change in the mana count
     * @throws IllegalArgumentException if the resulting amount of mana is negative
     */
    public void addMana(Team team, int amount) throws IllegalArgumentException {
        if (this.manaCounts[team.ordinal()] + amount < 0) {
            throw new IllegalArgumentException("Invalid mana change");
        }
        this.manaCounts[team.ordinal()] += amount;
    }
    
    /**
     * Add to the amount of adamantium. If amount is negative, subtract from adamantium instead.
     * 
     * @param team the team to query
     * @param amount the change in the mana count
     * @throws IllegalArgumentException if the resulting amount of adamantium is negative
     */
    public void addAdamantium(Team team, int amount) throws IllegalArgumentException {
    	if (this.adamantiumCounts[team.ordinal()] + amount < 0) {
    		throw new IllegalArgumentException("Invalid adamantium change");
    	}
    	this.adamantiumCounts[team.ordinal()] += amount;
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

    public int getRoundElixirChange(Team team) {
        return this.elixirCounts[team.ordinal()] - this.oldElixirCounts[team.ordinal()];
    }

    public int getRoundManaChange(Team team) {
        return this.manaCounts[team.ordinal()] - this.oldManaCounts[team.ordinal()];
    }
    
    public int getRoundAdamantiumChange(Team team) {
    	return this.adamantiumCounts[team.ordinal()] - this.oldAdamantiumCounts[team.ordinal()];
    }

    public void processEndOfRound() {
        this.oldElixirCounts[0] = this.elixirCounts[0];
        this.oldElixirCounts[1] = this.elixirCounts[1];
        this.oldManaCounts[0] = this.manaCounts[0];
        this.oldManaCounts[1] = this.manaCounts[1];
        this.oldAdamantiumCounts[0] = this.adamantiumCounts[0];
        this.oldAdamantiumCounts[1] = this.adamantiumCounts[1];
    }
}

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
    private int archonCount;
    private int leadCount;
    private int goldCount;

    /**
     * Create a new representation of TeamInfo
     *
     * @param gameWorld the gameWorld the team exists in
     * @param archonCount the number of remaining archons
     * @param leadCount the amount of lead stored
     * @param goldCount the amount of gold stored
     */
    public TeamInfo(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        this.archonCount = 0;
        this.leadCount = 0;
        this.goldCount = 0;
    }
    
    // *********************************
    // ***** GETTER METHODS ************
    // *********************************

    /**
     * Get the number of remaining Archons.
     *
     * @return the number of archons remaining
     */
    public int getArchonCount() {
        return this.archonCount;
    }

    /**
     * Get the amount of lead.
     *
     * @return the team's lead count
     */
    public int getLead() {
        return this.leadCount;
    }

    /**
     * Get the amount of gold.
     *
     * @return the team's gold count
     */
    public int getGold() {
        return this.goldCount;
    }

    // *********************************
    // ***** UPDATE METHODS ************
    // *********************************

    /**
     * Set the number of Archons. 
     * 
     * @param newArchonCount the new number of Archons
     * 
     * @throws IllegalArgumentException if the newArchonCount is negative
     */
    public void setArchonCount(int newArchonCount) throws IllegalArgumentException {
        if (newArchonCount < 0) {
            throw new IllegalArgumentException("Invalid archon count");
        }
        this.archonCount = newArchonCount;
    }

    /**
     * Add to the amount of lead. If leadChange is negative, subtract from lead instead. 
     * 
     * @param leadChange the change in the lead count
     * 
     * @throws IllegalArgumentException if the resulting amount of lead is negative
     */
    public void changeLead(int leadChange) throws IllegalArgumentException {
        if (leadCount + leadChange < 0) {
            throw new IllegalArgumentException("Invalid lead change");
        }
        this.leadCount += leadChange;
    }

    /**
     * Add to the amount of gold. If goldChange is negative, subtract from gold instead. 
     * 
     * @param goldChange the change in the gold count
     * 
     * @throws IllegalArgumentException if the resulting amount of gold is negative
     */
    public void changeGold(int goldChange) throws IllegalArgumentException {
        if (goldCount + goldChange < 0) {
            throw new IllegalArgumentException("Invalid gold change");
        }
        this.goldCount += goldChange;
    }
}

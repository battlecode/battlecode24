package battlecode.world;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import battlecode.common.*;



public class Island {
    final int idx;
    final MapLocation[] locations; // TODO: do we want to make this immutable or change it to a copy to be extra safe?
    final GameWorld gw;
    Team teamOwning;
    Anchor anchorPlanted;
    int turnsLeftToRemoveAnchor;

    public Island(GameWorld gw, int idx, List<MapLocation> locations) {
        // TODO: this may not be the right place for this assertion but as we rebalance this needs to be true
        assert(GameConstants.PERCENT_OPPOSING_TEAM_ISLAND + GameConstants.PERCENT_OWNING_TEAM_ISLAND > 1.0f);
        this.gw = gw;
        this.idx = idx;
        this.locations = new MapLocation[locations.size()];
        for (int i = 0; i < locations.size(); i ++) {
            this.locations[i] = locations.get(i);
        }
        this.teamOwning = Team.NEUTRAL;
        this.anchorPlanted = null;
        this.turnsLeftToRemoveAnchor = 0;
    }

    public Team getTeam() {
        return this.teamOwning;
    }

    public int getIdx() {
        return idx;
    }

    public Anchor getAnchor() {
        return this.anchorPlanted;
    }

    private void assertCanPlaceAnchor(Team placingTeam, Anchor toPlace) throws GameActionException {
        if (this.anchorPlanted != null) {
            if (this.teamOwning != placingTeam) {
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "Can't place anchor on enemy island until you remove enemy anchor");
            }
        }
    }

    public boolean canPlaceAnchor(Team placingTeam, Anchor toPlace) {
        try {
            assertCanPlaceAnchor(placingTeam, toPlace);
            return true;
        } catch (GameActionException e) { return false; }
    }

    public void placeAnchor(Team placingTeam, Anchor toPlace) throws GameActionException {
        assertCanPlaceAnchor(placingTeam, toPlace);
        this.anchorPlanted = toPlace;
        if (toPlace == Anchor.ACCELERATING) {
            this.gw.addBoostFromAnchor(this);
        }
        this.teamOwning = placingTeam;
        this.turnsLeftToRemoveAnchor = toPlace.turnsToRemove;
        this.gw.getTeamInfo().placeAnchor(placingTeam);
    }

    public void advanceTurn() { 
        if (teamOwning == Team.NEUTRAL) {
            return;
        }       
        int[] numOccupied = new int[2];
        for (MapLocation loc : locations) {
            Team robotTeam = gw.getRobot(loc).getTeam();
            numOccupied[robotTeam.ordinal()] ++;
        }
        if ((float) (numOccupied[teamOwning.ordinal()]) / locations.length >= GameConstants.PERCENT_OWNING_TEAM_ISLAND) {
            // If the team controlling the island controls enough of the island reset the count
            this.turnsLeftToRemoveAnchor = this.anchorPlanted.turnsToRemove;
        } else if ((float) (numOccupied[teamOwning.opponent().ordinal()]) / locations.length >= GameConstants.PERCENT_OPPOSING_TEAM_ISLAND) {
            // If the opposing team controls enough of the island decrease the count
            this.turnsLeftToRemoveAnchor -= 1;
            if (this.turnsLeftToRemoveAnchor <= 0) {
                this.teamOwning = Team.NEUTRAL;
                if (this.anchorPlanted == Anchor.ACCELERATING) {
                    this.gw.removeBoostFromAnchor(this);
                }
                this.anchorPlanted = null;
                this.turnsLeftToRemoveAnchor = 0;
            }

        }
    }

    public int minDistTo(MapLocation compareLoc) {
        int minDist = Integer.MAX_VALUE;
        for (MapLocation loc : this.locations) {
            minDist = Math.min(minDist, loc.distanceSquaredTo(compareLoc));
        }
        return minDist;
    }

    public Set<MapLocation> getLocsAffected() {
        Set<MapLocation> locsWithinRange = new HashSet<>();
        if (this.anchorPlanted != null) {
            if (this.anchorPlanted == Anchor.ACCELERATING) {
                for (MapLocation loc : this.locations) {
                    locsWithinRange.addAll(Arrays.asList(this.gw.getAllLocationsWithinRadiusSquared(loc, this.anchorPlanted.unitsAffected)));
                }            
            }
        }
        return locsWithinRange;
    }

}



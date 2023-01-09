package battlecode.world;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import battlecode.common.*;



public class Island {
    final int ID;
    final MapLocation[] locations;
    final GameWorld gw;
    Team teamOwning;
    Anchor anchorPlanted;
    int anchorHealth;

    public Island(GameWorld gw, int ID, List<MapLocation> locations) {
        this.gw = gw;
        this.ID = ID;
        this.locations = new MapLocation[locations.size()];
        for (int i = 0; i < locations.size(); i ++) {
            this.locations[i] = locations.get(i);
        }
        this.teamOwning = Team.NEUTRAL;
        this.anchorPlanted = null;
        this.anchorHealth = 0;
    }

    public Team getTeam() {
        return this.teamOwning;
    }

    public int getTeamInt() {
        switch (getTeam()) {
            case NEUTRAL:
                return 0;
            case A:
                return 1;
            case B:
                return 2;
            default:
                return 0;
        }
    }

    public int getID() {
        return ID;
    }

    public Anchor getAnchor() {
        return this.anchorPlanted;
    }

    public int getHealth() {
        return this.anchorHealth;
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
        boolean prevOwnedIsland = this.teamOwning == placingTeam;
        assertCanPlaceAnchor(placingTeam, toPlace);
        this.anchorPlanted = toPlace;
        if (toPlace == Anchor.ACCELERATING) {
            this.gw.addBoostFromAnchor(this);
        }
        this.teamOwning = placingTeam;
        this.anchorHealth = toPlace.totalHealth;
        if (!prevOwnedIsland) {
            this.gw.getTeamInfo().placeAnchor(placingTeam);
        }
    }

    public void advanceTurn() { 
        if (teamOwning == Team.NEUTRAL) {
            return;
        }       
        int[] numOccupied = new int[2];
        for (MapLocation loc : this.locations) {
            InternalRobot robot = gw.getRobot(loc);
            if (robot != null) {
                Team robotTeam = robot.getTeam();
                numOccupied[robotTeam.ordinal()] ++;
            }
        }
        int diffPctOccupied = (100*(numOccupied[teamOwning.ordinal()] - numOccupied[teamOwning.opponent().ordinal()]))/(locations.length);
        this.anchorHealth = Math.min(this.anchorPlanted.totalHealth, this.anchorHealth + diffPctOccupied);
        if (this.anchorHealth <= 0) {
            this.gw.getTeamInfo().removeAnchor(this.teamOwning);
            this.teamOwning = Team.NEUTRAL;
            if (this.anchorPlanted == Anchor.ACCELERATING) {
                this.gw.removeBoostFromAnchor(this);
            }
            this.anchorPlanted = null;
            this.anchorHealth = 0;
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



package battlecode.world;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lets maps be built easily, for testing purposes.
 */
public class TestMapBuilder {
    private MapBuilder mapBuilder;

    public TestMapBuilder(String name, int oX, int oY, int width, int height, int seed) {
        this.mapBuilder = new MapBuilder(name, width, height, oX, oY, seed);
    }

    public TestMapBuilder addArchon(int id, Team team, MapLocation loc) {
        this.mapBuilder.addArchon(id, team, loc);
        return this;
    }
    
    public TestMapBuilder setRubble(int x, int y, int value) {
        this.mapBuilder.setRubble(x, y, value);
        return this;
    }

    public TestMapBuilder setLead(int x, int y, int value) {
        this.mapBuilder.setLead(x, y, value);
        return this;
    }

    public TestMapBuilder addAnomalyScheduleEntry(int round, AnomalyType anomaly) {
        this.mapBuilder.addAnomalyScheduleEntry(round, anomaly);
        return this;
    }

    public LiveMap build() {
        return this.mapBuilder.build();
    }
}

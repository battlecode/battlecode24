package battlecode.world;
import battlecode.common.*;

public class AnomalyScheduleEntry {

    public final int roundNumber;
    public final AnomalyType anomalyType;

    public AnomalyScheduleEntry(int round, AnomalyType anomaly) {
        this.roundNumber = round;
        this.anomalyType = anomaly;
    }

    /**
     * @return a copy of the entry
     */
    public AnomalyScheduleEntry copyEntry(AnomalyType anomalyType) {
        return new AnomalyScheduleEntry(this.roundNumber, this.anomalyType);
    }
}
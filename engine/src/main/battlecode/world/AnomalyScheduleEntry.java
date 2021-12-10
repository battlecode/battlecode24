package battlecode.world;

public class AnomalyScheduleEntry {

    public final int roundNumber;
    public final AnomalyInfo anomalyType;

    public AnomalyScheduleEntry(int round, int anomaly){
        this.roundNumber = round;
        this.anomalyType = anomaly;
    }

}
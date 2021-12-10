package battlecode.world;

public class AnomalyScheduleEntry {

    public final int roundNumber;
    public final AnomalyInfo anomalyType;

    public AnomalyScheduleEntry(int round, int anomaly){
        this.roundNumber = round;
        this.anomalyType = anomaly;
    }

    /**
     * @return a copy of the entry
     */
    public copyEntry(AnomalyType anomalyType){
        return new AnomalyScheduleEntry(
                anomalyType.round,
                new AnomalyType(
                    anomalyType.isGlobalAnomaly,
                    anomalyType.isLocalAnomaly,
                    anomalyType.globalPercentage,
                    anomalyType.sagePercentage
                )
        );
    }

}
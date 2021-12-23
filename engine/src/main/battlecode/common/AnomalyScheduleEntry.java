package battlecode.common;

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
    public AnomalyScheduleEntry copyEntry() {
        return new AnomalyScheduleEntry(this.roundNumber, this.anomalyType);
    }

    /**
     * Returns whether two AnomalyScheduleEntrys are equal.
     *
     * @param other the other anomaly schedule entry to compare to
     * @return whether the two anomaly schedules entry are equivalent
     */
    public boolean equals(AnomalyScheduleEntry other) {
        if (this.roundNumber != other.roundNumber) return false;
        return this.anomalyType == other.anomalyType;
    }
}
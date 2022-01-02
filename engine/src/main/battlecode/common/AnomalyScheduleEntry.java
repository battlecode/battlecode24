package battlecode.common;

/**
 * AnomalyScheduleEntry describes a single Anomaly in the Anomaly schedule. The
 * schedule of Anomalies is predetermined for each map, and you can retrieve it
 * by calling {@link RobotController#getAnomalySchedule}. Each schedule entry
 * provides information about the round on which it occurs, and the type of the
 * Anomaly.
 * <P>
 * Note that the Singularity is not included in the schedule. You should instead
 * check {@link GameConstants#GAME_MAX_NUMBER_OF_ROUNDS}.
 */
public class AnomalyScheduleEntry {
    /**
     * The round on which this Anomaly will occur. The Anomaly will occur at the
     * end of this round, that is, after all robots have taken their turn.
     */
    public final int roundNumber;

    /**
     * The type of this Anomaly.
     */
    public final AnomalyType anomalyType;

    /**
     * Constructs an AnomalyScheduleEntry using the given round number and
     * Anomaly type. Note that this does not actually insert the Anomaly into
     * the schedule, but only creates a local object that you are free to
     * manipulate. The global Anomaly schedule is fixed and cannot be changed;
     * to envision an Anomaly with a Sage, see {@link RobotController#envision}.
     *
     * @param round the round number of the Anomaly
     * @param type the type of the Anomaly
     */
    public AnomalyScheduleEntry(int round, AnomalyType type) {
        this.roundNumber = round;
        this.anomalyType = type;
    }

    /**
     * Constructs a copy of this schedule entry. Note that this does not
     * actually cause the Anomaly to happen twice, but only creates a local
     * object that you are free to manipulate. The global Anomaly schedule is
     * fixed and cannot be changed; to envision an Anomaly with a Sage, see
     * {@link RobotController#envision}.
     *
     * @return a copy of this AnomalyScheduleEntry
     */
    public AnomalyScheduleEntry copyEntry() {
        return new AnomalyScheduleEntry(this.roundNumber, this.anomalyType);
    }

    /**
     * Returns whether two AnomalyScheduleEntry objects are equivalent.
     *
     * @param other the AnomalyScheduleEntry to compare with
     * @return whether the two AnomalyScheduleEntry objects are equivalent
     *
     * @battlecode.doc.costlymethod
     */
    @Override
    public boolean equals(Object other) {
        if (other == null || getClass() != other.getClass()) return false;
        AnomalyScheduleEntry casted = (AnomalyScheduleEntry) other;
        return this.roundNumber == casted.roundNumber && this.anomalyType == casted.anomalyType;
    }
}

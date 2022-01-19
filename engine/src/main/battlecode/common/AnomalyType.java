package battlecode.common;

/**
 * AnomalyType represents the type of a scheduled or envisioned Anomaly. You
 * can access the type of scheduled Anomalies from
 * {@link AnomalyScheduleEntry#anomalyType}, and envision your own Anomalies with
 * Sages by using {@link RobotController#envision}.
 * <P>
 * AnomalyType also provides information about the strenghts of each Anomaly, as
 * well as other properties.
 * <P>
 * Note that the Singularity is not included in the schedule. You should instead
 * check {@link GameConstants#GAME_MAX_NUMBER_OF_ROUNDS}.
 */
public enum AnomalyType {
    /**
     * Abyss causes proportional amounts of metal resources to be lost. When
     * global, the entire map as well as team reserves are affected. When
     * envisioned, a local region of the map is affected.
     * <P>
     * {@link #globalPercentage} and {@link #sagePercentage} specify the
     * proportion of metals lost.
     */
    ABYSS       (true,    true,   0.1f,    0.99f),

    /**
     * Charge deals concentrated damage to Droids. When global, the top
     * {@link #globalPercentage} Droids with the most nearby friendly units are
     * destroyed. When envisioned, all nearby enemy Droids lose
     * {@link #sagePercentage} of their maximum health.
     */
    CHARGE      (true,    true,   0.05f,   0.22f),

    /**
     * Fury deals concentrated proportional damage to Turrets. When global, all
     * Turrets are affected. When envisioned, a local region is affected.
     * <P>
     * {@link #globalPercentage} and {@link #sagePercentage} specify the
     * amount of damage dealt, as a proportion of the Turret's maximum health.
     */
    FURY        (true,    true,   0.05f,   0.1f),

    /**
     * Vortex upends the world by transforming the map terrain. It can only
     * occur globally.
     * <P>
     * The values of {@link #globalPercentage} and {@link #sagePercentage} are
     * unused.
     */
    VORTEX      (true,    false,  0,      0);

    /**
     * Whether this type of Anomaly could appear in the global schedule.
     */
    public final boolean isGlobalAnomaly;

    /**
     * Whether this type of Anomaly could be envisioned by Sages.
     */
    public final boolean isSageAnomaly;

    /**
     * The strength of this Anomaly when globally scheduled. The precise
     * definition of this value depends on the Anomaly type.
     */
    public final float globalPercentage;

    /**
     * The strength of this Anomaly when envisioned by a Sage. The precise
     * definition of this value depends on the Anomaly type.
     */
    public final float sagePercentage;

    AnomalyType(boolean isGlobalAnomaly, boolean isSageAnomaly, float globalPercentage, float sagePercentage) {
        this.isGlobalAnomaly     = isGlobalAnomaly;
        this.isSageAnomaly       = isSageAnomaly;
        this.globalPercentage    = globalPercentage;
        this.sagePercentage      = sagePercentage;
    }
}

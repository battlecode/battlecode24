package battlecode.common;

/**
 * AnomalyType enumerates the different types of anomalies in the game.
 * You can also access properties about these anomalies, such as their strengths
 * and whether they can be performed by Sages.
 */
public enum AnomalyType {
    ABYSS       (true,    true,   0.1f,    0.2f),
    CHARGE      (true,    true,   0.05f,   0.1f),
    FURY        (true,    true,   0.05f,   0.1f),
    VORTEX      (true,    false,  0,      0),
    SINGULARITY (true,    false,  0,      0);

    public final boolean isGlobalAnomaly;
    public final boolean isSageAnomaly;
    public final float globalPercentage;
    public final float sagePercentage;

    AnomalyType(boolean isGlobalAnomaly, boolean isSageAnomaly, float globalPercentage, float sagePercentage) {
        this.isGlobalAnomaly     = isGlobalAnomaly;
        this.isSageAnomaly       = isSageAnomaly;
        this.globalPercentage    = globalPercentage;
        this.sagePercentage      = sagePercentage;
    }
}

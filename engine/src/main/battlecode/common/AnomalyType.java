package battlecode.common;

/**
 * Holds the different anomalies in the game.
 */
public enum AnomalyType {
    ABYSS       (true,    true,   0.1f,    0.2f),
    CHARGE      (true,    true,   0.05f,   0.1f),
    FURY        (true,    true,   0.05f,   0.1f),
    VORTEX      (true,    false,  0,      0);
    
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
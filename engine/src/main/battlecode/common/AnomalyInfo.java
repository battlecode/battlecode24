package battlecode.common;

/**
 * Holds the different anomalies in the game.
 */
public enum AnomalyType {

    ABYSS       (true,    true,   0.1,    0.2),
    CHARGE      (true,    true,   0.05,   0.1),
    FURY        (true,    true,   0.05,   0.1),
    VORTEX      (true,    false,  0,      0),
    SINGULARITY ();
    

    public final boolean isGlobalAnomaly;
    public final boolean isSageAnomaly;
    public final float globalPercentage;
    public final float sagePercentage;


    AnomalyMode(boolean isGlobalAnomaly, boolean isLocalAnomaly, float globalPercentage, float sagePercentage) {
        this.isGlobalAnomaly     = isGlobalAnomaly;
        this.isLocalAnomaly      = isLocalAnomaly;
        this.globalPercentage    = globalPercentage;
        this.sagePercentage      = sagePercentage;

    }
}
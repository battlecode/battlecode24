package battlecode.common;

/**
 * Holds the different anomalies in the game.
 */
public enum AnomalyType {

    ABYSS     (true,  true, GameConstants.ABYSS_COOLDOWN),
    CHARGE    (true,  true, GameConstants.CHARGE_COOLDOWN),
    FURY      (true,  true, GameConstants.FURY_COOLDOWN),
    VORTEX    (true,  false, -1);

    public final boolean isGlobalAnomaly;
    public final boolean isSageAnomaly;
    public final int sageCooldown;

    AnomalyMode(boolean isGlobalAnomaly, boolean isLocalAnomaly, int sageCooldown) {
        this.isGlobalAnomaly     = isGlobalAnomaly;
        this.isLocalAnomaly      = isLocalAnomaly;
        this.sageCooldown        = sageCooldown;
    }
}
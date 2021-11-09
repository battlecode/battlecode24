package battlecode.common;

/**
 * Defines constants that affect gameplay.
 */
@SuppressWarnings("unused")
public class GameConstants {

    /**
     * The current spec version the server compiles with.
     */
    public static final String SPEC_VERSION = "1.0";

    // *********************************
    // ****** MAP CONSTANTS ************
    // *********************************

    /** The minimum possible map height. */
    public static final int MAP_MIN_HEIGHT = 32;

    /** The maximum possible map height. */
    public static final int MAP_MAX_HEIGHT = 64;

    /** The minimum possible map width. */
    public static final int MAP_MIN_WIDTH = 32;

    /** The maximum possible map width. */
    public static final int MAP_MAX_WIDTH = 64;

    // *********************************
    // ****** GAME PARAMETERS **********
    // *********************************

    /** The number of indicator strings that a player can associate with a robot. */
    public static final int NUMBER_OF_INDICATOR_STRINGS = 3;

    /** The bytecode penalty that is imposed each time an exception is thrown. */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;

    ///** Maximum ID a Robot will have */
    //public static final int MAX_ROBOT_ID = 32000;   Cannot be guaranteed in Battlecode 2021.

    // *********************************
    // ****** COOLDOWNS ****************
    // *********************************

    /** If the number of cooldown turns is >= this number, a robot cannot act. */
    public static final int COOLDOWN_LIMIT = 10;

    /** The number of cooldown turns reduced per turn. */
    public static final int COOLDOWNS_PER_TURN = 10;

    /** The number of cooldown turns per transformation. */
    public static final int TRANSFORM_COOLDOWN = 100;

    /** The number of cooldown turns per upgrade. */
    public static final int UPGRADE_COOLDOWN = 100;

    // *********************************
    // ****** GAME MECHANICS ***********
    // *********************************

    /** A prototype building's starting health, as a multiplier of max health. */
    public static final float PROTOTYPE_STARTING_HEALTH_MULTIPLIER = 0.1f;

    /** The maximum level a building can be. */
    public static final int MAX_LEVEL = 3;

    /** The amount damage is reduced by every ricochet. */
    public static final float RICOCHET_DAMAGE_MULTIPLIER = 0.8f;

    /**
     * The amount of healing an archon does, based on level.
     *
     * @param level the level of the archon
     * @return the healing amount
     */
    public static int getArchonHealing(int level) {
        return 8 * level; // TODO: update this. maybe move to RobotType? so both archons and builders have this function
    }

    // *********************************
    // ****** GAMEPLAY PROPERTIES ******
    // *********************************

    /** The default game seed. **/
    public static final int GAME_DEFAULT_SEED = 6370;

    /** The maximum number of rounds in a game.  **/
    public static final int GAME_MAX_NUMBER_OF_ROUNDS = 1500;
}

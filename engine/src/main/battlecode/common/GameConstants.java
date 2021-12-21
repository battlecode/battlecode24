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
    public static final int MAP_MIN_HEIGHT = 30;

    /** The maximum possible map height. */
    public static final int MAP_MAX_HEIGHT = 80;

    /** The minimum possible map width. */
    public static final int MAP_MIN_WIDTH = 30;

    /** The maximum possible map width. */
    public static final int MAP_MAX_WIDTH = 80;

    // *********************************
    // ****** GAME PARAMETERS **********
    // *********************************

    /** The number of indicator strings that a player can associate with a robot. */
    public static final int NUMBER_OF_INDICATOR_STRINGS = 3;

    /** The bytecode penalty that is imposed each time an exception is thrown. */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;

    /** The amount of lead each team gains per turn. */
    public static final int PASSIVE_LEAD_INCREASE = 5;

    /** The number of rounds between adding lead resources to the map. */
    public static final int ADD_RESOURCE_EVERY_ROUNDS = 20;
    
    // *********************************
    // ****** COOLDOWNS ****************
    // *********************************

    /** If the number of cooldown turns is >= this number, a robot cannot act. */
    public static final int COOLDOWN_LIMIT = 10;

    /** The number of cooldown turns reduced per turn. */
    public static final int COOLDOWNS_PER_TURN = 10;

    /** The number of cooldown turns per transformation. */
    public static final int TRANSFORM_COOLDOWN = 100;

    // *********************************
    // ****** GAME MECHANICS ***********
    // *********************************

    /** A prototype building's starting health, as a multiplier of max health. */
    public static final float PROTOTYPE_STARTING_HEALTH_MULTIPLIER = 0.1f;

    /** The amount of health a prototype building has as a multiplier. */
    public static final float PROTOTYPE_HP_PERCENTAGE = 0.1f;

    /** The multiplier for reclaiming a building's cost. */
    public static final float RECLAIM_COST_MULTIPLIER = 0.2f;

    /** The maximum level a building can be. */
    public static final int MAX_LEVEL = 3;

    /** Constants for alchemists converting lead to gold. */
    public static final double ALCHEMIST_LONELINESS_A = 20;
    public static final double ALCHEMIST_LONELINESS_B = 15;
    public static final double ALCHEMIST_LONELINESS_K = 0.02;

    // *********************************
    // ****** GAMEPLAY PROPERTIES ******
    // *********************************

    /** The default game seed. **/
    public static final int GAME_DEFAULT_SEED = 6370;

    /** The maximum number of rounds in a game.  **/
    public static final int GAME_MAX_NUMBER_OF_ROUNDS = 2000;
}

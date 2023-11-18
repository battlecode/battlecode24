package battlecode.common;

/**
 * GameConstants defines constants that affect gameplay.
 */
@SuppressWarnings("unused")
public class GameConstants {

    //TODO: Let's organize this better but I'm lazy :)
    /**
     * The current spec version the server compiles with.
     */
    public static final String SPEC_VERSION = "3.0.14";

    // *********************************
    // ****** MAP CONSTANTS ************
    // *********************************

    /** The minimum possible map height. */
    public static final int MAP_MIN_HEIGHT = 20;

    /** The maximum possible map height. */
    public static final int MAP_MAX_HEIGHT = 60;

    /** The minimum possible map width. */
    public static final int MAP_MIN_WIDTH = 20;

    /** The maximum possible map width. */
    public static final int MAP_MAX_WIDTH = 60;

    /** The minimum distance between ally flags in the initial map and at the end of the seutp phase */
    public static final int MIN_FLAG_SPACING_SQUARED = 36;

    // *********************************
    // ****** GAME PARAMETERS **********
    // *********************************

    /** The number of flags a player starts with. */
    public static final int NUMBER_FLAGS = 3;

    /** The maximum length of indicator strings that a player can associate with a robot. */
    public static final int INDICATOR_STRING_MAX_LENGTH = 64;

    /** The length of each team's shared communication array. */
    public static final int SHARED_ARRAY_LENGTH = 64;

    /** The maximum value in shared communication arrays. */
    public static final int MAX_SHARED_ARRAY_VALUE = (1 << 16) - 1;

    /** The bytecode penalty that is imposed each time an exception is thrown. */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;

    /** The total number of robots a team has (both despawned or spawned). */
    public static final int ROBOT_CAPACITY = 50;

    /** The initial amount of bread each team starts with. */
    public static final int INITIAL_BREAD_AMOUNT = 200;

    /** The amount of bread each team gains per turn. */
    public static final int PASSIVE_BREAD_INCREASE = 6;

    /** The number of rounds between adding resources to teams. */
    public static final int PASSIVE_INCREASE_ROUNDS = 5;

    // *********************************
    // ****** COOLDOWNS ****************
    // *********************************

    /** If the amount of cooldown is at least this value, a robot cannot act. */
    public static final int COOLDOWN_LIMIT = 10;

    /** The number of cooldown turns reduced per turn. */
    public static final int COOLDOWNS_PER_TURN = 10;

    public static final int VISION_RADIUS = 20;

    // *********************************
    // ****** GAME MECHANICS ***********
    // *********************************

    /** Constants for dig and fill costs and cooldowns. */
    public static final int DIG_COST = 2;
    public static final int DIG_COOLDOWN = 20;
    public static final int FILL_COST = 1;
    public static final int FILL_COOLDOWN = 20;

    /** Constants for flags */
    public static final int FLAG_BROADCAST_UPDATE_INTERVAL = 100;
    public static final int FLAG_BROADCAST_NOISE_RADIUS = 10;
    public static final int FLAG_DROPPED_RESET_ROUNDS = 4;
    
    // *********************************
    // ****** GAMEPLAY PROPERTIES ******
    // *********************************

    /** The default game seed. **/
    public static final int GAME_DEFAULT_SEED = 6370;

    /** The maximum number of rounds in a game.  **/
    public static final int GAME_MAX_NUMBER_OF_ROUNDS = 2000;

    /** The end of the setup rounds in the game */
    public static final int SETUP_ROUNDS = 200;
}

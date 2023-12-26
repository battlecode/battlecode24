package battlecode.common;

/**
 * GameConstants defines constants that affect gameplay.
 */
@SuppressWarnings("unused")
public class GameConstants {

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

    /** The default game seed. **/
    public static final int GAME_DEFAULT_SEED = 6370;

    /** The maximum number of rounds in a game.  **/
    public static final int GAME_MAX_NUMBER_OF_ROUNDS = 2000;

    /** The maximum number of bytecodes a bot is allow to use in one turn */
    public static final int BYTECODE_LIMIT = 20000;

    /** The maximum length of indicator strings that a player can associate with a robot. */
    public static final int INDICATOR_STRING_MAX_LENGTH = 64;

    /** The length of each team's shared communication array. */
    public static final int SHARED_ARRAY_LENGTH = 64;

    /** The maximum value in shared communication arrays. */
    public static final int MAX_SHARED_ARRAY_VALUE = (1 << 16) - 1;

    /** The bytecode penalty that is imposed each time an exception is thrown. */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;

    /** health each robot starts with */
    public static final int DEFAULT_HEALTH = 100;

    /** The total number of robots a team has (both despawned or spawned). */
    public static final int ROBOT_CAPACITY = 50;

    // *********************************
    // ****** GAME MECHANICS ***********
    // *********************************

    /** The number of flags a player starts with. */
    public static final int NUMBER_FLAGS = 3;

    /** Constants for dig and fill costs and cooldowns. */
    public static final int DIG_COST = 2;
    public static final int DIG_COOLDOWN = 20;
    public static final int FILL_COST = 1;
    public static final int FILL_COOLDOWN = 20;

    /** Constants for flags */
    public static final int FLAG_BROADCAST_UPDATE_INTERVAL = 100;
    public static final int FLAG_BROADCAST_NOISE_RADIUS = 10;
    public static final int FLAG_DROPPED_RESET_ROUNDS = 4;

    /** The initial amount of bread each team starts with. */
    public static final int INITIAL_BREAD_AMOUNT = 200;

    /** The amount of bread each team gains per turn. */
    public static final int PASSIVE_BREAD_INCREASE = 6;

    /** The number of rounds between adding resources to teams. */
    public static final int PASSIVE_INCREASE_ROUNDS = 5;

    /** The end of the setup rounds in the game */
    public static final int SETUP_ROUNDS = 200;

    /** Number of rounds between adding a global upgrade point */
    public static final int GLOBAL_UPGRADE_ROUNDS = 750;

    /** Number of rounds robots must spend in jail before respawning */
    public static final int JAILED_ROUNDS = 10;

    /** The maximum distance from a robot where information can be sensed */
    public static final int VISION_RADIUS_SQUARED = 20;

    /** The maximum distance for attacking an enemy robot */
    public static final int ATTACK_RADIUS_SQUARED = 4;

    /** The maximum distance for healing an ally robot */
    public static final int HEAL_RADIUS_SQUARED = 4;

    /** The maximum distnace for picking up / dropping flags, building traps, digging, and filling */
    public static final int INTERACT_RADIUS_SQUARED = 2;

    // *********************************
    // ****** COOLDOWNS ****************
    // *********************************

    /** If the amount of cooldown is at least this value, a robot cannot act. */
    public static final int COOLDOWN_LIMIT = 10;

    /** The number of cooldown turns reduced per turn. */
    public static final int COOLDOWNS_PER_TURN = 10;

    /** The amount added to the movement cooldown counter when moving without a flag */
    public static final int MOVEMENT_COOLDOWN_INCREASE = 10;

    /** The amount added to the movement cooldown counter when moving while carrying a flag  */
    public static final int FLAG_MOVEMENT_COOLDOWN_INCREASE = 20;

    /** The amount added to the action cooldown counter after attacking */
    public static final int ATTACK_COOLDOWN = 20;

    /** The amount added to the action cooldown counter after healing */
    public static final int HEAL_COOLDOWN = 20;
}

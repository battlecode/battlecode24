package battlecode.common;

/**
 * GameConstants defines constants that affect gameplay.
 */
@SuppressWarnings("unused")
public class GameConstants {

    /**
     * The current spec version the server compiles with.
     */
    public static final String SPEC_VERSION = "2022.0.1.0";

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

    /** The minimum number of starting Archons per team. */
    public static final int MIN_STARTING_ARCHONS = 1;

    /** The maximum number of starting Archons per team. */
    public static final int MAX_STARTING_ARCHONS = 4;

    /** The minimum amount of rubble per square. */
    public static final int MIN_RUBBLE = 0;

    /** The maximum amount of rubble per square. */
    public static final int MAX_RUBBLE = 100;

    // *********************************
    // ****** GAME PARAMETERS **********
    // *********************************

    /** The maximum length of indicator strings that a player can associate with a robot. */
    public static final int INDICATOR_STRING_MAX_LENGTH = 64;

    /** The length of each team's shared communication array. */
    public static final int SHARED_ARRAY_LENGTH = 64;

    /** The maximum value in shared communication arrays. */
    public static final int MAX_SHARED_ARRAY_VALUE = Short.MAX_VALUE;

    /** The bytecode penalty that is imposed each time an exception is thrown. */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;

    /** The initial amount of lead each team starts with. */
    public static final int INITIAL_LEAD_AMOUNT = 200;

    /** The initial amount of gold each team starts with. */
    public static final int INITIAL_GOLD_AMOUNT = 0;

    /** The amount of lead each team gains per turn. */
    public static final int PASSIVE_LEAD_INCREASE = 2;

    /** The number of rounds between adding lead resources to the map. */
    public static final int ADD_LEAD_EVERY_ROUNDS = 20;

    /** The amount of lead to add each round that lead is added. */
    public static final int ADD_LEAD = 5;
    
    // *********************************
    // ****** COOLDOWNS ****************
    // *********************************

    /** If the amount of cooldown is at least this value, a robot cannot act. */
    public static final int COOLDOWN_LIMIT = 10;

    /** The number of cooldown turns reduced per turn. */
    public static final int COOLDOWNS_PER_TURN = 10;

    /** The number of cooldown turns per transformation. */
    public static final int TRANSFORM_COOLDOWN = 100;

    /** The number of cooldown turns per mutation. */
    public static final int MUTATE_COOLDOWN = 100;

    // *********************************
    // ****** GAME MECHANICS ***********
    // *********************************

    /** A blueprint building's health, as a multiplier of max health. */
    public static final float PROTOTYPE_HP_PERCENTAGE = 0.9f;

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

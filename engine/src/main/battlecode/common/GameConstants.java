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

    /** The minimum number of starting Headquarters per team. */
    public static final int MIN_STARTING_HEADQUARTERS = 1;

    /** The maximum number of starting Headquarters per team. */
    public static final int MAX_STARTING_HEADQUARTERS = 4;

    /** The minimum number of islands on the map. */
    public static final int MIN_NUMBER_ISLANDS = 4;

    /** The maximum number of islands on the map. */
    public static final int MAX_NUMBER_ISLANDS = 35;

    /** The maximum area of an island in units. */
    public static final int MAX_ISLAND_AREA = 20;

    /** The maximum distance between wells of different types. */
    public static final int MAX_DISTANCE_BETWEEN_WELLS = 100;

    /** The minimum distance from a headquarter to the nearest adamantium well. */
    public static final int MIN_NEAREST_AD_DISTANCE = 100;

    /** The maximum percentage of the map that can be wells of a certain type. */
    public static final float MAX_MAP_PERCENT_WELLS = 0.04f;

    /** The minimum distance between ally flags in the initial map and at the end of the seutp phase */
    public static final int MIN_FLAG_SPACING_SQUARED = 36;

    // *********************************
    // ****** GAME PARAMETERS **********
    // *********************************

    /** The health that a robot has by default. */
    public static final int DEFAULT_HEALTH = 100;

    /** The max number of robots a team can spawn. */
    public static final int SPAWN_LIMIT = 50;

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

    /** The amount of adamantium or mana needed to upgrade a well to elixir */
    public static final int UPGRADE_TO_ELIXIR = 600;

    /** The amount of adamantium or mana needed to upgrade a well's transfer rate */
    public static final int UPGRADE_WELL_AMOUNT = 1400;

    /** The percentage of islands that need to be occupied for a team to win */
    public static final float WIN_PERCENTAGE_OF_ISLANDS_OCCUPIED = 0.75f;
    
    /** The distance a robot must be from a signal amplifier to be able to write to the shared array */
    public static final int DISTANCE_SQUARED_FROM_SIGNAL_AMPLIFIER = 20;

    /** The distance a robot must be from an island to be able to write to the shared array */
    public static final int DISTANCE_SQUARED_FROM_ISLAND = 4;

    /** The distance a robot must be from a headquarter to be able to write to the shared array */
    public static final int DISTANCE_SQUARED_FROM_HEADQUARTER = 9;

    /** The discount factor on the amount of damage a carrier can do based on their capacity */
    public static final float CARRIER_DAMAGE_FACTOR = 1.25f;

    /** The slope of the function to determine movement cooldown for carriers */
    public static final float CARRIER_MOVEMENT_SLOPE = 0.375f;

    /** The intercept of the function to determine movement cooldown for carriers */
    public static final int CARRIER_MOVEMENT_INTERCEPT = 5;

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

    /** The number of game rounds between applying movement due to currents */
    public static final int CURRENT_STRENGTH = 1;

    /** The maximum capacity a carrier can carry */
    public static final int CARRIER_CAPACITY = 40;

    /** The weight of an anchor */
    public static final int ANCHOR_WEIGHT = CARRIER_CAPACITY;

    /** Constant for vision radius when affected by cloud */
    public static final int CLOUD_VISION_RADIUS_SQUARED = 4;

    /** Constants for cooldown multipliers. */
    public static final double BOOSTER_MULTIPLIER = -.1;
    public static final double DESTABILIZER_MULTIPLIER = .1;
    public static final double ANCHOR_MULTIPLIER = -.15;
    public static final double CLOUD_MULTIPLIER = .2;

    /** Constants for boost radii squared. */
    public static final int BOOSTER_RADIUS_SQUARED = 20;
    public static final int DESTABILIZER_RADIUS_SQUARED = 15;

    /** Constants for boost durations. */
    public static final int BOOSTER_DURATION = 10;
    public static final int DESTABILIZER_DURATION = 5;

    /** Constants for number of boosts that stack. */
    public static final int MAX_BOOST_STACKS = 3;
    public static final int MAX_DESTABILIZE_STACKS = 2;
    public static final int MAX_ANCHOR_STACKS = 1;

    /** Constants for dig and fill costs and cooldowns. */
    public static final int DIG_COST = 2;
    public static final int DIG_COOLDOWN = 20;
    public static final int FILL_COST = 1;
    public static final int FILL_COOLDOWN = 20;

    /** Constants for well rates. */
    public static final int WELL_STANDARD_RATE = 1;
    public static final int WELL_ACCELERATED_RATE = 3;

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

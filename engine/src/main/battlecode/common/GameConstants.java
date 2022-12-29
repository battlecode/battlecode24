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
    public static final String SPEC_VERSION = "2022.2.2.0";

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

    // *********************************
    // ****** GAME PARAMETERS **********
    // *********************************

    /** The maximum length of indicator strings that a player can associate with a robot. */
    public static final int INDICATOR_STRING_MAX_LENGTH = 64;

    /** The length of each team's shared communication array. */
    public static final int SHARED_ARRAY_LENGTH = 64;

    /** The maximum value in shared communication arrays. */
    public static final int MAX_SHARED_ARRAY_VALUE = (1 << 16) - 1;

    /** The bytecode penalty that is imposed each time an exception is thrown. */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;

    //TODO: fix -> I think I changed these to correct amounts (200)
    /** The initial amount of mana each team starts with. */
    public static final int INITIAL_MN_AMOUNT = 200;

    /** The initial amount of adamantium each team starts with. */
    public static final int INITIAL_AD_AMOUNT = 200;

    //TODO: We may want to reduce this since it is added to each hq
    /** The amount of adamantium each headquarter on a team gains per turn. */
    public static final int PASSIVE_AD_INCREASE = 2;

    /** The amount of mana each headquarter on a team gains per turn. */
    public static final int PASSIVE_MN_INCREASE = 2;

    /** The number of rounds between adding resources to headquarters. */
    public static final int PASSIVE_INCREASE_ROUNDS = 5;

    /** The amount of adamantium or mana needed to upgrade a well to elixir */
    public static final int UPGRADE_TO_ELIXIR = 1500;

    /** The amount of adamantium or mana needed to upgrade a well's transfer rate */
    public static final int UPGRADE_WELL_AMOUNT = 3600;

    /** The percentage of islands that need to be occupied for a team to win */
    public static final double WIN_PERCENTAGE_OF_ISLANDS_OCCUPIED = 0.75;
    
    /** The distance a robot must be from a signal amplifier to be able to write to the shared array */
    public static final int DISTANCE_FROM_SIGNAL_AMPLIFIER = 36;

    /** The distance a robot must be from an island to be able to write to the shared array */
    public static final int DISTANCE_FROM_ISLAND = 45;

    /** The distance a robot must be from a headquarter to be able to write to the shared array */
    public static final int DISTANCE_FROM_HEADQUARTER = 50;

    /** The amount of damage a launcher can do */
    public static final int LAUNCHER_ATTACK_DAMAGE = 6;
    // TODO: this doesn't make sense, max capacity is like 50

    /** The discout factor on the amount of damage a carrier can do based on their capacity */
    public static final float CARRIER_DAMAGE_FACTOR = 0.2f;

    // *********************************
    // ****** COOLDOWNS ****************
    // *********************************

    /** If the amount of cooldown is at least this value, a robot cannot act. */
    public static final int COOLDOWN_LIMIT = 10;

    /** The number of cooldown turns reduced per turn. */
    public static final int COOLDOWNS_PER_TURN = 10;

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

    /** Constant for damage dealt by destabilizer */
    public static final int DESTABILIZER_DAMAGE = 10;

    /** Constants for boost radii squared. */
    public static final int DESTABILIZER_RADIUS_SQUARED = 20;
    public static final int BOOSTER_RADIUS_SQUARED = 40;

    /** Constants for boost durations. */
    public static final int BOOSTER_DURATION = 10;
    public static final int DESTABILIZER_DURATION = 5;

    /** Constants for number of boosts that stack. */
    public static final int MAX_BOOST_STACKS = 3;
    public static final int MAX_DESTABILIZE_STACKS = 2;
    public static final int MAX_ANCHOR_STACKS = 1;

    /** Constants for well rates. */
    public static final int WELL_STANDARD_RATE = 4;
    public static final int WELL_ACCELERATED_RATE = 10;
    
    // *********************************
    // ****** GAMEPLAY PROPERTIES ******
    // *********************************

    /** The default game seed. **/
    public static final int GAME_DEFAULT_SEED = 6370;

    /** The maximum number of rounds in a game.  **/
    public static final int GAME_MAX_NUMBER_OF_ROUNDS = 2000;
}

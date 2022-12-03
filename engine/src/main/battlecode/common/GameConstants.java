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
    public static final int MAX_SHARED_ARRAY_VALUE = (1 << 16) - 1;

    /** The bytecode penalty that is imposed each time an exception is thrown. */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;

    //TODO: fix
    /** The initial amount of mana each team starts with. */
    public static final int INITIAL_MN_AMOUNT = 100;

    /** The initial amount of adamantium each team starts with. */
    public static final int INITIAL_AD_AMOUNT = 100;

    //TODO: We may want to reduce this since it is added to each hq
    /** The amount of adamantium each headquarter on a team gains per turn. */
    public static final int PASSIVE_AD_INCREASE = 2;

    /** The amount of mana each headquarter on a team gains per turn. */
    public static final int PASSIVE_MN_INCREASE = 2;

    /** The number of rounds between adding resources to headquarters. */
    public static final int PASSIVE_INCREASE_ROUNDS = 5;

    /** The amount of lead to add each round that lead is added. */
    public static final int ADD_LEAD = 5;

    /** The amount of adamantium or mana needed to upgrade a well to elixir */
    public static final int UPGRADE_TO_ELIXIR = 15000;

    /** The amount of adamantium or mana needed to upgrade a well's transfer rate */
    public static final int UPGRADE_WELL_RATE = 10000;
    
    /** The distance a robot must be from a signal amplifier to be able to write to the shared array */
    public static final int DISTANCE_FROM_SIGNAL_AMPLIFIER = 36;

    /** The distance a robot must be from a planted reality anchor to be able to write to the shared array */
    public static final int DISTANCE_FROM_REALITY_ANCHOR = 45;

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

    /** The number of cooldown turns per transformation. */
    public static final int TRANSFORM_COOLDOWN = 100;

    /** The number of cooldown turns per mutation. */
    public static final int MUTATE_COOLDOWN = 100;

    // *********************************
    // ****** GAME MECHANICS ***********
    // *********************************

    // TODO: this is currently based on the percentage of total number of squares in the island
    // rather than the number of occupied square. It is unclear which we want it to be.
    /** The percentage of an island that needs to be owned by the team which owns the island to reset the anchor strength. */
    public static final float PERCENT_OWNING_TEAM_ISLAND = 0.85f;

    /** The percentage of an island that needs to be owned by the opposing team to reduce the anchor strength. */
    public static final float PERCENT_OPPOSING_TEAM_ISLAND = 0.4f;
    
    /** A blueprint building's health, as a multiplier of max health. */
    public static final float PROTOTYPE_HP_PERCENTAGE = 0.8f;

    /** The multiplier for reclaiming a building's cost. */
    public static final float RECLAIM_COST_MULTIPLIER = 0.2f;

    /** The maximum level a building can be. */
    public static final int MAX_LEVEL = 3;

    /** The maximum capacity a carrier can carry */
    public static final int CARRIER_CAPACITY = 40;

    /** The weight of an anchor */
    public static final int ANCHOR_WEIGHT = CARRIER_CAPACITY;

    /** Constants for alchemists converting lead to gold. */
    public static final double ALCHEMIST_LONELINESS_A = 20;
    public static final double ALCHEMIST_LONELINESS_B = 18;
    public static final double ALCHEMIST_LONELINESS_K_L1 = 0.02;
    public static final double ALCHEMIST_LONELINESS_K_L2 = 0.01;
    public static final double ALCHEMIST_LONELINESS_K_L3 = 0.005;

    /** Constants for cooldown multipliers. */
    public static final double BOOSTER_MULTIPLIER = .1;
    public static final double DESTABILIZER_MULTIPLIER = -.1;
    public static final double ANCHOR_MULTIPLIER = .15;
    public static final double CLOUD_MULTIPLIER = -.2;
    public static final double CURRENT_MULTIPLIER = .1;

    /** Constants for boost radii squared. */
    public static final int DESTABILIZER_RADIUS_SQUARED = 20;
    public static final int BOOSTER_RADIUS_SQUARED = 40;

    /** Constants for boost durations. */
    public static final int BOOSTER_DURATION = 10;
    public static final int DESTABILIZER_DURATION = 5;

    /** Constants for well rates. */
    public static final int WELL_STANDARD_RATE = 2;
    public static final int WELL_ACCELERATED_RATE = 4;
    
    // *********************************
    // ****** GAMEPLAY PROPERTIES ******
    // *********************************

    /** The default game seed. **/
    public static final int GAME_DEFAULT_SEED = 6370;

    /** The maximum number of rounds in a game.  **/
    //TOOD: change pls
    public static final int GAME_MAX_NUMBER_OF_ROUNDS = 20;
}

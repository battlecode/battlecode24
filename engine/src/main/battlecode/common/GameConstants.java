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

    // TODO: what is the actual cooldown value for the following values:

    public static final int BUILDING_UPGRADE_COOLDOWN = 500;
    public static final float FRESH_ROBOT_ARCHON_COOLDOWN = 10f;
    // turret to portable, or portable to turret
    public static final int BUILDING_CONVERSION_COOLDOWN = 10000;

    // *********************************
    // ****** LEVEL MULTIPLIERS ********
    // *********************************

    public static final float PLACEHOLDER_LEVEL_MULTIPLIER = 1.2f;

    public static final float DPS_1_TO_2 = PLACEHOLDER_LEVEL_MULTIPLIER;
    public static final float HP_1_TO_2 = PLACEHOLDER_LEVEL_MULTIPLIER;

    public static final float DPS_2_TO_3 = PLACEHOLDER_LEVEL_MULTIPLIER;
    public static final float HP_2_TO_3 = PLACEHOLDER_LEVEL_MULTIPLIER;

    public static final float DPS_UNIT_TO_TURRET = PLACEHOLDER_LEVEL_MULTIPLIER;
    public static final float HP_UNIT_TO_TURRET = PLACEHOLDER_LEVEL_MULTIPLIER;

    // TODO
    // turret vision = unit vision + ???
    // building portable mode move cooldown = ???

    public static final float BUILDING_UPGRADE_COST_MULTIPLER = 0.5f;

    // *********************************
    // ****** GAME MECHANICS ***********
    // *********************************

    public static final float PROTOTYPE_HP_PERCENTAGE = 0.1f;
    public static final float AUTOMATIC_ARCHON_HEAL_AMOUNT = 10f;
    public static final float AUTOMATIC_BUILDER_HEAL_AMOUNT = 10f;

    public static final float PER_ALCHEMIST_DECREASE_RATE = 0.1f;

    public static final int LEAD_TO_GOLD_RATE = 20;
    public static final int INITIAL_LEAD = 200;

    // Older constants below, maintaining them for now.

    /** The maximum level a building can be. */
    public static final int MAX_LEVEL = 3;

    /** The amount damage is reduced by every ricochet. */
    public static final float RICOCHET_DAMAGE_MULTIPLIER = 0.8f;

    // *********************************
    // ****** GAMEPLAY PROPERTIES ******
    // *********************************

    /** The default game seed. **/
    public static final int GAME_DEFAULT_SEED = 6370;

    /** The maximum number of rounds in a game.  **/
    public static final int GAME_MAX_NUMBER_OF_ROUNDS = 1500;
}

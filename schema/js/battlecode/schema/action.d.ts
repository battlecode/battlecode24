export declare enum Action {
    ATTACK = 0,
    HEAL = 1,
    DIG = 2,
    FILL = 3,
    EXPLOSIVE_TRAP = 4,
    WATER_TRAP = 5,
    STUN_TRAP = 6,
    PICKUP_FLAG = 7,
    DROP_FLAG = 8,
    CAPTURE_FLAG = 9,
    RESET_FLAG = 10,
    GLOBAL_UPGRADE = 11,
    /**
     * Dies due to an uncaught exception
     * Target: none
     */
    DIE_EXCEPTION = 12
}

import * as flatbuffers from 'flatbuffers';
export declare class GameConstants {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): GameConstants;
    static getRootAsGameConstants(bb: flatbuffers.ByteBuffer, obj?: GameConstants): GameConstants;
    static getSizePrefixedRootAsGameConstants(bb: flatbuffers.ByteBuffer, obj?: GameConstants): GameConstants;
    setupPhaseLength(): number;
    flagMinDistance(): number;
    globalUpgradeRoundDelay(): number;
    passiveResourceRate(): number;
    robotBaseHealth(): number;
    jailedRounds(): number;
    visionRadius(): number;
    actionRadius(): number;
    static startGameConstants(builder: flatbuffers.Builder): void;
    static addSetupPhaseLength(builder: flatbuffers.Builder, setupPhaseLength: number): void;
    static addFlagMinDistance(builder: flatbuffers.Builder, flagMinDistance: number): void;
    static addGlobalUpgradeRoundDelay(builder: flatbuffers.Builder, globalUpgradeRoundDelay: number): void;
    static addPassiveResourceRate(builder: flatbuffers.Builder, passiveResourceRate: number): void;
    static addRobotBaseHealth(builder: flatbuffers.Builder, robotBaseHealth: number): void;
    static addJailedRounds(builder: flatbuffers.Builder, jailedRounds: number): void;
    static addVisionRadius(builder: flatbuffers.Builder, visionRadius: number): void;
    static addActionRadius(builder: flatbuffers.Builder, actionRadius: number): void;
    static endGameConstants(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createGameConstants(builder: flatbuffers.Builder, setupPhaseLength: number, flagMinDistance: number, globalUpgradeRoundDelay: number, passiveResourceRate: number, robotBaseHealth: number, jailedRounds: number, visionRadius: number, actionRadius: number): flatbuffers.Offset;
}

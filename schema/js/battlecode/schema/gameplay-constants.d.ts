import * as flatbuffers from 'flatbuffers';
export declare class GameplayConstants {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): GameplayConstants;
    static getRootAsGameplayConstants(bb: flatbuffers.ByteBuffer, obj?: GameplayConstants): GameplayConstants;
    static getSizePrefixedRootAsGameplayConstants(bb: flatbuffers.ByteBuffer, obj?: GameplayConstants): GameplayConstants;
    setupPhaseLength(): number;
    flagMinDistance(): number;
    globalUpgradeRoundDelay(): number;
    passiveResourceRate(): number;
    robotBaseHealth(): number;
    jailedRounds(): number;
    visionRadius(): number;
    actionRadius(): number;
    static startGameplayConstants(builder: flatbuffers.Builder): void;
    static addSetupPhaseLength(builder: flatbuffers.Builder, setupPhaseLength: number): void;
    static addFlagMinDistance(builder: flatbuffers.Builder, flagMinDistance: number): void;
    static addGlobalUpgradeRoundDelay(builder: flatbuffers.Builder, globalUpgradeRoundDelay: number): void;
    static addPassiveResourceRate(builder: flatbuffers.Builder, passiveResourceRate: number): void;
    static addRobotBaseHealth(builder: flatbuffers.Builder, robotBaseHealth: number): void;
    static addJailedRounds(builder: flatbuffers.Builder, jailedRounds: number): void;
    static addVisionRadius(builder: flatbuffers.Builder, visionRadius: number): void;
    static addActionRadius(builder: flatbuffers.Builder, actionRadius: number): void;
    static endGameplayConstants(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createGameplayConstants(builder: flatbuffers.Builder, setupPhaseLength: number, flagMinDistance: number, globalUpgradeRoundDelay: number, passiveResourceRate: number, robotBaseHealth: number, jailedRounds: number, visionRadius: number, actionRadius: number): flatbuffers.Offset;
}

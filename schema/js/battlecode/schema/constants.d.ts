import * as flatbuffers from 'flatbuffers';
export declare class Constants {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): Constants;
    static getRootAsConstants(bb: flatbuffers.ByteBuffer, obj?: Constants): Constants;
    static getSizePrefixedRootAsConstants(bb: flatbuffers.ByteBuffer, obj?: Constants): Constants;
    setupPhaseLength(): number;
    flagMinDistance(): number;
    globalUpgradeRoundDelay(): number;
    passiveResourceRate(): number;
    robotBaseHealth(): number;
    jailedRounds(): number;
    visionRadius(): number;
    actionRadius(): number;
    static startConstants(builder: flatbuffers.Builder): void;
    static addSetupPhaseLength(builder: flatbuffers.Builder, setupPhaseLength: number): void;
    static addFlagMinDistance(builder: flatbuffers.Builder, flagMinDistance: number): void;
    static addGlobalUpgradeRoundDelay(builder: flatbuffers.Builder, globalUpgradeRoundDelay: number): void;
    static addPassiveResourceRate(builder: flatbuffers.Builder, passiveResourceRate: number): void;
    static addRobotBaseHealth(builder: flatbuffers.Builder, robotBaseHealth: number): void;
    static addJailedRounds(builder: flatbuffers.Builder, jailedRounds: number): void;
    static addVisionRadius(builder: flatbuffers.Builder, visionRadius: number): void;
    static addActionRadius(builder: flatbuffers.Builder, actionRadius: number): void;
    static endConstants(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createConstants(builder: flatbuffers.Builder, setupPhaseLength: number, flagMinDistance: number, globalUpgradeRoundDelay: number, passiveResourceRate: number, robotBaseHealth: number, jailedRounds: number, visionRadius: number, actionRadius: number): flatbuffers.Offset;
}

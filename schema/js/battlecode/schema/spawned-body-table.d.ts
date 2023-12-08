import * as flatbuffers from 'flatbuffers';
import { VecTable } from '../../battlecode/schema/vec-table';
export declare class SpawnedBodyTable {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): SpawnedBodyTable;
    static getRootAsSpawnedBodyTable(bb: flatbuffers.ByteBuffer, obj?: SpawnedBodyTable): SpawnedBodyTable;
    static getSizePrefixedRootAsSpawnedBodyTable(bb: flatbuffers.ByteBuffer, obj?: SpawnedBodyTable): SpawnedBodyTable;
    robotIds(index: number): number | null;
    robotIdsLength(): number;
    robotIdsArray(): Int32Array | null;
    teamIds(index: number): number | null;
    teamIdsLength(): number;
    teamIdsArray(): Int8Array | null;
    locs(obj?: VecTable): VecTable | null;
    attacksPerformed(index: number): number | null;
    attacksPerformedLength(): number;
    attacksPerformedArray(): Int32Array | null;
    attackLevels(index: number): number | null;
    attackLevelsLength(): number;
    attackLevelsArray(): Int32Array | null;
    buildsPerformed(index: number): number | null;
    buildsPerformedLength(): number;
    buildsPerformedArray(): Int32Array | null;
    buildLevels(index: number): number | null;
    buildLevelsLength(): number;
    buildLevelsArray(): Int32Array | null;
    healsPerformed(index: number): number | null;
    healsPerformedLength(): number;
    healsPerformedArray(): Int32Array | null;
    healLevels(index: number): number | null;
    healLevelsLength(): number;
    healLevelsArray(): Int32Array | null;
    holdingFlag(index: number): boolean | null;
    holdingFlagLength(): number;
    holdingFlagArray(): Int8Array | null;
    static startSpawnedBodyTable(builder: flatbuffers.Builder): void;
    static addRobotIds(builder: flatbuffers.Builder, robotIdsOffset: flatbuffers.Offset): void;
    static createRobotIdsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createRobotIdsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startRobotIdsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addTeamIds(builder: flatbuffers.Builder, teamIdsOffset: flatbuffers.Offset): void;
    static createTeamIdsVector(builder: flatbuffers.Builder, data: number[] | Int8Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createTeamIdsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startTeamIdsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addLocs(builder: flatbuffers.Builder, locsOffset: flatbuffers.Offset): void;
    static addAttacksPerformed(builder: flatbuffers.Builder, attacksPerformedOffset: flatbuffers.Offset): void;
    static createAttacksPerformedVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createAttacksPerformedVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startAttacksPerformedVector(builder: flatbuffers.Builder, numElems: number): void;
    static addAttackLevels(builder: flatbuffers.Builder, attackLevelsOffset: flatbuffers.Offset): void;
    static createAttackLevelsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createAttackLevelsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startAttackLevelsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addBuildsPerformed(builder: flatbuffers.Builder, buildsPerformedOffset: flatbuffers.Offset): void;
    static createBuildsPerformedVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createBuildsPerformedVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startBuildsPerformedVector(builder: flatbuffers.Builder, numElems: number): void;
    static addBuildLevels(builder: flatbuffers.Builder, buildLevelsOffset: flatbuffers.Offset): void;
    static createBuildLevelsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createBuildLevelsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startBuildLevelsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addHealsPerformed(builder: flatbuffers.Builder, healsPerformedOffset: flatbuffers.Offset): void;
    static createHealsPerformedVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createHealsPerformedVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startHealsPerformedVector(builder: flatbuffers.Builder, numElems: number): void;
    static addHealLevels(builder: flatbuffers.Builder, healLevelsOffset: flatbuffers.Offset): void;
    static createHealLevelsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createHealLevelsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startHealLevelsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addHoldingFlag(builder: flatbuffers.Builder, holdingFlagOffset: flatbuffers.Offset): void;
    static createHoldingFlagVector(builder: flatbuffers.Builder, data: boolean[]): flatbuffers.Offset;
    static startHoldingFlagVector(builder: flatbuffers.Builder, numElems: number): void;
    static endSpawnedBodyTable(builder: flatbuffers.Builder): flatbuffers.Offset;
}

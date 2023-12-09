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
    static endSpawnedBodyTable(builder: flatbuffers.Builder): flatbuffers.Offset;
}

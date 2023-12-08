import * as flatbuffers from 'flatbuffers';
export declare class CommTable {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): CommTable;
    static getRootAsCommTable(bb: flatbuffers.ByteBuffer, obj?: CommTable): CommTable;
    static getSizePrefixedRootAsCommTable(bb: flatbuffers.ByteBuffer, obj?: CommTable): CommTable;
    team1(index: number): number | null;
    team1Length(): number;
    team1Array(): Int32Array | null;
    team2(index: number): number | null;
    team2Length(): number;
    team2Array(): Int32Array | null;
    static startCommTable(builder: flatbuffers.Builder): void;
    static addTeam1(builder: flatbuffers.Builder, team1Offset: flatbuffers.Offset): void;
    static createTeam1Vector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createTeam1Vector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startTeam1Vector(builder: flatbuffers.Builder, numElems: number): void;
    static addTeam2(builder: flatbuffers.Builder, team2Offset: flatbuffers.Offset): void;
    static createTeam2Vector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createTeam2Vector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startTeam2Vector(builder: flatbuffers.Builder, numElems: number): void;
    static endCommTable(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createCommTable(builder: flatbuffers.Builder, team1Offset: flatbuffers.Offset, team2Offset: flatbuffers.Offset): flatbuffers.Offset;
}

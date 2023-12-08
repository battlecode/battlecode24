import * as flatbuffers from 'flatbuffers';
export declare class TeamData {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): TeamData;
    static getRootAsTeamData(bb: flatbuffers.ByteBuffer, obj?: TeamData): TeamData;
    static getSizePrefixedRootAsTeamData(bb: flatbuffers.ByteBuffer, obj?: TeamData): TeamData;
    name(): string | null;
    name(optionalEncoding: flatbuffers.Encoding): string | Uint8Array | null;
    packageName(): string | null;
    packageName(optionalEncoding: flatbuffers.Encoding): string | Uint8Array | null;
    teamId(): number;
    static startTeamData(builder: flatbuffers.Builder): void;
    static addName(builder: flatbuffers.Builder, nameOffset: flatbuffers.Offset): void;
    static addPackageName(builder: flatbuffers.Builder, packageNameOffset: flatbuffers.Offset): void;
    static addTeamId(builder: flatbuffers.Builder, teamId: number): void;
    static endTeamData(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createTeamData(builder: flatbuffers.Builder, nameOffset: flatbuffers.Offset, packageNameOffset: flatbuffers.Offset, teamId: number): flatbuffers.Offset;
}

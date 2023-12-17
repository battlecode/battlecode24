import * as flatbuffers from 'flatbuffers';
import { ProfilerFile } from '../../battlecode/schema/profiler-file';
/**
 * Sent to end a match.
 */
export declare class MatchFooter {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): MatchFooter;
    static getRootAsMatchFooter(bb: flatbuffers.ByteBuffer, obj?: MatchFooter): MatchFooter;
    static getSizePrefixedRootAsMatchFooter(bb: flatbuffers.ByteBuffer, obj?: MatchFooter): MatchFooter;
    /**
     * The ID of the winning team.
     */
    winner(): number;
    /**
     * The number of rounds played.
     */
    totalRounds(): number;
    /**
     * Profiler data for team A and B if profiling is enabled.
     */
    profilerFiles(index: number, obj?: ProfilerFile): ProfilerFile | null;
    profilerFilesLength(): number;
    static startMatchFooter(builder: flatbuffers.Builder): void;
    static addWinner(builder: flatbuffers.Builder, winner: number): void;
    static addTotalRounds(builder: flatbuffers.Builder, totalRounds: number): void;
    static addProfilerFiles(builder: flatbuffers.Builder, profilerFilesOffset: flatbuffers.Offset): void;
    static createProfilerFilesVector(builder: flatbuffers.Builder, data: flatbuffers.Offset[]): flatbuffers.Offset;
    static startProfilerFilesVector(builder: flatbuffers.Builder, numElems: number): void;
    static endMatchFooter(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createMatchFooter(builder: flatbuffers.Builder, winner: number, totalRounds: number, profilerFilesOffset: flatbuffers.Offset): flatbuffers.Offset;
}

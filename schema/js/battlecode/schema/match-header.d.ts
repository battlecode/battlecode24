import * as flatbuffers from 'flatbuffers';
import { GameMap } from '../../battlecode/schema/game-map';
/**
 * Sent to start a match.
 */
export declare class MatchHeader {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): MatchHeader;
    static getRootAsMatchHeader(bb: flatbuffers.ByteBuffer, obj?: MatchHeader): MatchHeader;
    static getSizePrefixedRootAsMatchHeader(bb: flatbuffers.ByteBuffer, obj?: MatchHeader): MatchHeader;
    map(obj?: GameMap): GameMap | null;
    maxRounds(): number;
    static startMatchHeader(builder: flatbuffers.Builder): void;
    static addMap(builder: flatbuffers.Builder, mapOffset: flatbuffers.Offset): void;
    static addMaxRounds(builder: flatbuffers.Builder, maxRounds: number): void;
    static endMatchHeader(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createMatchHeader(builder: flatbuffers.Builder, mapOffset: flatbuffers.Offset, maxRounds: number): flatbuffers.Offset;
}

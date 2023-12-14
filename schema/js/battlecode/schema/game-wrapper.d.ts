import * as flatbuffers from 'flatbuffers';
import { EventWrapper } from '../../battlecode/schema/event-wrapper';
/**
 * If events are not otherwise delimited, this wrapper structure
 * allows a game to be stored in a single buffer.
 * The first event will be a GameHeader; the last event will be a GameFooter.
 * matchHeaders[0] is the index of the 0th match header in the event stream,
 * corresponding to matchFooters[0]. These indices allow quick traversal of
 * the file.
 */
export declare class GameWrapper {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): GameWrapper;
    static getRootAsGameWrapper(bb: flatbuffers.ByteBuffer, obj?: GameWrapper): GameWrapper;
    static getSizePrefixedRootAsGameWrapper(bb: flatbuffers.ByteBuffer, obj?: GameWrapper): GameWrapper;
    /**
     * The series of events comprising the game.
     */
    events(index: number, obj?: EventWrapper): EventWrapper | null;
    eventsLength(): number;
    /**
     * The indices of the headers of the matches, in order.
     */
    matchHeaders(index: number): number | null;
    matchHeadersLength(): number;
    matchHeadersArray(): Int32Array | null;
    /**
     * The indices of the footers of the matches, in order.
     */
    matchFooters(index: number): number | null;
    matchFootersLength(): number;
    matchFootersArray(): Int32Array | null;
    static startGameWrapper(builder: flatbuffers.Builder): void;
    static addEvents(builder: flatbuffers.Builder, eventsOffset: flatbuffers.Offset): void;
    static createEventsVector(builder: flatbuffers.Builder, data: flatbuffers.Offset[]): flatbuffers.Offset;
    static startEventsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addMatchHeaders(builder: flatbuffers.Builder, matchHeadersOffset: flatbuffers.Offset): void;
    static createMatchHeadersVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createMatchHeadersVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startMatchHeadersVector(builder: flatbuffers.Builder, numElems: number): void;
    static addMatchFooters(builder: flatbuffers.Builder, matchFootersOffset: flatbuffers.Offset): void;
    static createMatchFootersVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createMatchFootersVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startMatchFootersVector(builder: flatbuffers.Builder, numElems: number): void;
    static endGameWrapper(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createGameWrapper(builder: flatbuffers.Builder, eventsOffset: flatbuffers.Offset, matchHeadersOffset: flatbuffers.Offset, matchFootersOffset: flatbuffers.Offset): flatbuffers.Offset;
}

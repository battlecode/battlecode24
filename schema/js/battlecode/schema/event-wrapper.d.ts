import * as flatbuffers from 'flatbuffers';
import { Event } from '../../battlecode/schema/event';
/**
 * Necessary due to flatbuffers requiring unions to be wrapped in tables.
 */
export declare class EventWrapper {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): EventWrapper;
    static getRootAsEventWrapper(bb: flatbuffers.ByteBuffer, obj?: EventWrapper): EventWrapper;
    static getSizePrefixedRootAsEventWrapper(bb: flatbuffers.ByteBuffer, obj?: EventWrapper): EventWrapper;
    eType(): Event;
    e<T extends flatbuffers.Table>(obj: any): any | null;
    static startEventWrapper(builder: flatbuffers.Builder): void;
    static addEType(builder: flatbuffers.Builder, eType: Event): void;
    static addE(builder: flatbuffers.Builder, eOffset: flatbuffers.Offset): void;
    static endEventWrapper(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createEventWrapper(builder: flatbuffers.Builder, eType: Event, eOffset: flatbuffers.Offset): flatbuffers.Offset;
}

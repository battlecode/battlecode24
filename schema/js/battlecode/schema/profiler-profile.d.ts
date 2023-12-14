import * as flatbuffers from 'flatbuffers';
import { ProfilerEvent } from '../../battlecode/schema/profiler-event';
/**
 * A profile contains all events and is labeled with a name.
 */
export declare class ProfilerProfile {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): ProfilerProfile;
    static getRootAsProfilerProfile(bb: flatbuffers.ByteBuffer, obj?: ProfilerProfile): ProfilerProfile;
    static getSizePrefixedRootAsProfilerProfile(bb: flatbuffers.ByteBuffer, obj?: ProfilerProfile): ProfilerProfile;
    /**
     * The display-friendly name of the profile.
     */
    name(): string | null;
    name(optionalEncoding: flatbuffers.Encoding): string | Uint8Array | null;
    /**
     * The events that occurred in the profile.
     */
    events(index: number, obj?: ProfilerEvent): ProfilerEvent | null;
    eventsLength(): number;
    static startProfilerProfile(builder: flatbuffers.Builder): void;
    static addName(builder: flatbuffers.Builder, nameOffset: flatbuffers.Offset): void;
    static addEvents(builder: flatbuffers.Builder, eventsOffset: flatbuffers.Offset): void;
    static createEventsVector(builder: flatbuffers.Builder, data: flatbuffers.Offset[]): flatbuffers.Offset;
    static startEventsVector(builder: flatbuffers.Builder, numElems: number): void;
    static endProfilerProfile(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createProfilerProfile(builder: flatbuffers.Builder, nameOffset: flatbuffers.Offset, eventsOffset: flatbuffers.Offset): flatbuffers.Offset;
}

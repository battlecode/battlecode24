import * as flatbuffers from 'flatbuffers';
/**
 * These tables are set-up so that they match closely with speedscope's file format documented at
 * https://github.com/jlfwong/speedscope/wiki/Importing-from-custom-sources.
 * The client uses speedscope to show the recorded data in an interactive interface.
 * A single event in a profile. Represents either an open event (meaning a
 * method has been entered) or a close event (meaning the method was exited).
 */
export declare class ProfilerEvent {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): ProfilerEvent;
    static getRootAsProfilerEvent(bb: flatbuffers.ByteBuffer, obj?: ProfilerEvent): ProfilerEvent;
    static getSizePrefixedRootAsProfilerEvent(bb: flatbuffers.ByteBuffer, obj?: ProfilerEvent): ProfilerEvent;
    /**
     * Whether this is an open event (true) or a close event (false).
     */
    isOpen(): boolean;
    /**
     * The bytecode counter at the time the event occurred.
     */
    at(): number;
    /**
     * The index of the method name in the ProfilerFile.frames array.
     */
    frame(): number;
    static startProfilerEvent(builder: flatbuffers.Builder): void;
    static addIsOpen(builder: flatbuffers.Builder, isOpen: boolean): void;
    static addAt(builder: flatbuffers.Builder, at: number): void;
    static addFrame(builder: flatbuffers.Builder, frame: number): void;
    static endProfilerEvent(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createProfilerEvent(builder: flatbuffers.Builder, isOpen: boolean, at: number, frame: number): flatbuffers.Offset;
}

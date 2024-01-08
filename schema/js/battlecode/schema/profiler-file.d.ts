import * as flatbuffers from 'flatbuffers';
import { ProfilerProfile } from '../../battlecode/schema/profiler-profile';
/**
 * A profiler file is a collection of profiles.
 * When profiling is enabled there is one of these per team per match.
 */
export declare class ProfilerFile {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): ProfilerFile;
    static getRootAsProfilerFile(bb: flatbuffers.ByteBuffer, obj?: ProfilerFile): ProfilerFile;
    static getSizePrefixedRootAsProfilerFile(bb: flatbuffers.ByteBuffer, obj?: ProfilerFile): ProfilerFile;
    /**
     * The method names that are referred to in the events.
     */
    frames(index: number): string;
    frames(index: number, optionalEncoding: flatbuffers.Encoding): string | Uint8Array;
    framesLength(): number;
    /**
     * The recorded profiles, one per robot.
     */
    profiles(index: number, obj?: ProfilerProfile): ProfilerProfile | null;
    profilesLength(): number;
    static startProfilerFile(builder: flatbuffers.Builder): void;
    static addFrames(builder: flatbuffers.Builder, framesOffset: flatbuffers.Offset): void;
    static createFramesVector(builder: flatbuffers.Builder, data: flatbuffers.Offset[]): flatbuffers.Offset;
    static startFramesVector(builder: flatbuffers.Builder, numElems: number): void;
    static addProfiles(builder: flatbuffers.Builder, profilesOffset: flatbuffers.Offset): void;
    static createProfilesVector(builder: flatbuffers.Builder, data: flatbuffers.Offset[]): flatbuffers.Offset;
    static startProfilesVector(builder: flatbuffers.Builder, numElems: number): void;
    static endProfilerFile(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createProfilerFile(builder: flatbuffers.Builder, framesOffset: flatbuffers.Offset, profilesOffset: flatbuffers.Offset): flatbuffers.Offset;
}

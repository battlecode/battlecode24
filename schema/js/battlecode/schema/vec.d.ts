import * as flatbuffers from 'flatbuffers';
export declare class Vec {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): Vec;
    x(): number;
    y(): number;
    static sizeOf(): number;
    static createVec(builder: flatbuffers.Builder, x: number, y: number): flatbuffers.Offset;
}

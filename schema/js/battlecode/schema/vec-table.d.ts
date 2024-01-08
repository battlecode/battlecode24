import * as flatbuffers from 'flatbuffers';
export declare class VecTable {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): VecTable;
    static getRootAsVecTable(bb: flatbuffers.ByteBuffer, obj?: VecTable): VecTable;
    static getSizePrefixedRootAsVecTable(bb: flatbuffers.ByteBuffer, obj?: VecTable): VecTable;
    xs(index: number): number | null;
    xsLength(): number;
    xsArray(): Int32Array | null;
    ys(index: number): number | null;
    ysLength(): number;
    ysArray(): Int32Array | null;
    static startVecTable(builder: flatbuffers.Builder): void;
    static addXs(builder: flatbuffers.Builder, xsOffset: flatbuffers.Offset): void;
    static createXsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createXsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startXsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addYs(builder: flatbuffers.Builder, ysOffset: flatbuffers.Offset): void;
    static createYsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createYsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startYsVector(builder: flatbuffers.Builder, numElems: number): void;
    static endVecTable(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createVecTable(builder: flatbuffers.Builder, xsOffset: flatbuffers.Offset, ysOffset: flatbuffers.Offset): flatbuffers.Offset;
}

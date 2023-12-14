import * as flatbuffers from 'flatbuffers';
export declare class RGBTable {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): RGBTable;
    static getRootAsRGBTable(bb: flatbuffers.ByteBuffer, obj?: RGBTable): RGBTable;
    static getSizePrefixedRootAsRGBTable(bb: flatbuffers.ByteBuffer, obj?: RGBTable): RGBTable;
    red(index: number): number | null;
    redLength(): number;
    redArray(): Int32Array | null;
    green(index: number): number | null;
    greenLength(): number;
    greenArray(): Int32Array | null;
    blue(index: number): number | null;
    blueLength(): number;
    blueArray(): Int32Array | null;
    static startRGBTable(builder: flatbuffers.Builder): void;
    static addRed(builder: flatbuffers.Builder, redOffset: flatbuffers.Offset): void;
    static createRedVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createRedVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startRedVector(builder: flatbuffers.Builder, numElems: number): void;
    static addGreen(builder: flatbuffers.Builder, greenOffset: flatbuffers.Offset): void;
    static createGreenVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createGreenVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startGreenVector(builder: flatbuffers.Builder, numElems: number): void;
    static addBlue(builder: flatbuffers.Builder, blueOffset: flatbuffers.Offset): void;
    static createBlueVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createBlueVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startBlueVector(builder: flatbuffers.Builder, numElems: number): void;
    static endRGBTable(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createRGBTable(builder: flatbuffers.Builder, redOffset: flatbuffers.Offset, greenOffset: flatbuffers.Offset, blueOffset: flatbuffers.Offset): flatbuffers.Offset;
}

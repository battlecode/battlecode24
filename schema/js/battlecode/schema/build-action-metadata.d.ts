import * as flatbuffers from 'flatbuffers';
import { BuildActionType } from '../../battlecode/schema/build-action-type';
export declare class BuildActionMetadata {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): BuildActionMetadata;
    static getRootAsBuildActionMetadata(bb: flatbuffers.ByteBuffer, obj?: BuildActionMetadata): BuildActionMetadata;
    static getSizePrefixedRootAsBuildActionMetadata(bb: flatbuffers.ByteBuffer, obj?: BuildActionMetadata): BuildActionMetadata;
    type(): BuildActionType;
    cost(): number;
    buildCooldown(): number;
    static startBuildActionMetadata(builder: flatbuffers.Builder): void;
    static addType(builder: flatbuffers.Builder, type: BuildActionType): void;
    static addCost(builder: flatbuffers.Builder, cost: number): void;
    static addBuildCooldown(builder: flatbuffers.Builder, buildCooldown: number): void;
    static endBuildActionMetadata(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createBuildActionMetadata(builder: flatbuffers.Builder, type: BuildActionType, cost: number, buildCooldown: number): flatbuffers.Offset;
}

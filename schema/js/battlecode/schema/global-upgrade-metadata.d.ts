import * as flatbuffers from 'flatbuffers';
import { GlobalUpgradeType } from '../../battlecode/schema/global-upgrade-type';
export declare class GlobalUpgradeMetadata {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): GlobalUpgradeMetadata;
    static getRootAsGlobalUpgradeMetadata(bb: flatbuffers.ByteBuffer, obj?: GlobalUpgradeMetadata): GlobalUpgradeMetadata;
    static getSizePrefixedRootAsGlobalUpgradeMetadata(bb: flatbuffers.ByteBuffer, obj?: GlobalUpgradeMetadata): GlobalUpgradeMetadata;
    type(): GlobalUpgradeType;
    upgradeAmount(): number;
    static startGlobalUpgradeMetadata(builder: flatbuffers.Builder): void;
    static addType(builder: flatbuffers.Builder, type: GlobalUpgradeType): void;
    static addUpgradeAmount(builder: flatbuffers.Builder, upgradeAmount: number): void;
    static endGlobalUpgradeMetadata(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createGlobalUpgradeMetadata(builder: flatbuffers.Builder, type: GlobalUpgradeType, upgradeAmount: number): flatbuffers.Offset;
}

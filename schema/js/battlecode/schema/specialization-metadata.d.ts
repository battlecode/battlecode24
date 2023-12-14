import * as flatbuffers from 'flatbuffers';
import { SpecializationType } from '../../battlecode/schema/specialization-type';
export declare class SpecializationMetadata {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): SpecializationMetadata;
    static getRootAsSpecializationMetadata(bb: flatbuffers.ByteBuffer, obj?: SpecializationMetadata): SpecializationMetadata;
    static getSizePrefixedRootAsSpecializationMetadata(bb: flatbuffers.ByteBuffer, obj?: SpecializationMetadata): SpecializationMetadata;
    type(): SpecializationType;
    level(): number;
    actionCost(): number;
    actionJailedPenalty(): number;
    cooldownReduction(): number;
    damageIncrease(): number;
    healIncrease(): number;
    static startSpecializationMetadata(builder: flatbuffers.Builder): void;
    static addType(builder: flatbuffers.Builder, type: SpecializationType): void;
    static addLevel(builder: flatbuffers.Builder, level: number): void;
    static addActionCost(builder: flatbuffers.Builder, actionCost: number): void;
    static addActionJailedPenalty(builder: flatbuffers.Builder, actionJailedPenalty: number): void;
    static addCooldownReduction(builder: flatbuffers.Builder, cooldownReduction: number): void;
    static addDamageIncrease(builder: flatbuffers.Builder, damageIncrease: number): void;
    static addHealIncrease(builder: flatbuffers.Builder, healIncrease: number): void;
    static endSpecializationMetadata(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createSpecializationMetadata(builder: flatbuffers.Builder, type: SpecializationType, level: number, actionCost: number, actionJailedPenalty: number, cooldownReduction: number, damageIncrease: number, healIncrease: number): flatbuffers.Offset;
}

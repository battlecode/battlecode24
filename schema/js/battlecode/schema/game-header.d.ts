import * as flatbuffers from 'flatbuffers';
import { BuildActionMetadata } from '../../battlecode/schema/build-action-metadata';
import { Constants } from '../../battlecode/schema/constants';
import { GlobalUpgradeMetadata } from '../../battlecode/schema/global-upgrade-metadata';
import { SpecializationMetadata } from '../../battlecode/schema/specialization-metadata';
import { TeamData } from '../../battlecode/schema/team-data';
/**
 * The first event sent in the game. Contains all metadata about the game.
 */
export declare class GameHeader {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): GameHeader;
    static getRootAsGameHeader(bb: flatbuffers.ByteBuffer, obj?: GameHeader): GameHeader;
    static getSizePrefixedRootAsGameHeader(bb: flatbuffers.ByteBuffer, obj?: GameHeader): GameHeader;
    specVersion(): string | null;
    specVersion(optionalEncoding: flatbuffers.Encoding): string | Uint8Array | null;
    teams(index: number, obj?: TeamData): TeamData | null;
    teamsLength(): number;
    specializationMetadata(index: number, obj?: SpecializationMetadata): SpecializationMetadata | null;
    specializationMetadataLength(): number;
    buildActionMetadata(index: number, obj?: BuildActionMetadata): BuildActionMetadata | null;
    buildActionMetadataLength(): number;
    globalUpgradeMetadata(index: number, obj?: GlobalUpgradeMetadata): GlobalUpgradeMetadata | null;
    globalUpgradeMetadataLength(): number;
    constants(obj?: Constants): Constants | null;
    static startGameHeader(builder: flatbuffers.Builder): void;
    static addSpecVersion(builder: flatbuffers.Builder, specVersionOffset: flatbuffers.Offset): void;
    static addTeams(builder: flatbuffers.Builder, teamsOffset: flatbuffers.Offset): void;
    static createTeamsVector(builder: flatbuffers.Builder, data: flatbuffers.Offset[]): flatbuffers.Offset;
    static startTeamsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addSpecializationMetadata(builder: flatbuffers.Builder, specializationMetadataOffset: flatbuffers.Offset): void;
    static createSpecializationMetadataVector(builder: flatbuffers.Builder, data: flatbuffers.Offset[]): flatbuffers.Offset;
    static startSpecializationMetadataVector(builder: flatbuffers.Builder, numElems: number): void;
    static addBuildActionMetadata(builder: flatbuffers.Builder, buildActionMetadataOffset: flatbuffers.Offset): void;
    static createBuildActionMetadataVector(builder: flatbuffers.Builder, data: flatbuffers.Offset[]): flatbuffers.Offset;
    static startBuildActionMetadataVector(builder: flatbuffers.Builder, numElems: number): void;
    static addGlobalUpgradeMetadata(builder: flatbuffers.Builder, globalUpgradeMetadataOffset: flatbuffers.Offset): void;
    static createGlobalUpgradeMetadataVector(builder: flatbuffers.Builder, data: flatbuffers.Offset[]): flatbuffers.Offset;
    static startGlobalUpgradeMetadataVector(builder: flatbuffers.Builder, numElems: number): void;
    static addConstants(builder: flatbuffers.Builder, constantsOffset: flatbuffers.Offset): void;
    static endGameHeader(builder: flatbuffers.Builder): flatbuffers.Offset;
}

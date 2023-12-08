"use strict";
// automatically generated by the FlatBuffers compiler, do not modify
Object.defineProperty(exports, "__esModule", { value: true });
exports.GameHeader = void 0;
var flatbuffers = require("flatbuffers");
var build_action_metadata_1 = require("../../battlecode/schema/build-action-metadata");
var constants_1 = require("../../battlecode/schema/constants");
var global_upgrade_metadata_1 = require("../../battlecode/schema/global-upgrade-metadata");
var specialization_metadata_1 = require("../../battlecode/schema/specialization-metadata");
var team_data_1 = require("../../battlecode/schema/team-data");
/**
 * The first event sent in the game. Contains all metadata about the game.
 */
var GameHeader = /** @class */ (function () {
    function GameHeader() {
        this.bb = null;
        this.bb_pos = 0;
    }
    GameHeader.prototype.__init = function (i, bb) {
        this.bb_pos = i;
        this.bb = bb;
        return this;
    };
    GameHeader.getRootAsGameHeader = function (bb, obj) {
        return (obj || new GameHeader()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
    };
    GameHeader.getSizePrefixedRootAsGameHeader = function (bb, obj) {
        bb.setPosition(bb.position() + flatbuffers.SIZE_PREFIX_LENGTH);
        return (obj || new GameHeader()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
    };
    GameHeader.prototype.specVersion = function (optionalEncoding) {
        var offset = this.bb.__offset(this.bb_pos, 4);
        return offset ? this.bb.__string(this.bb_pos + offset, optionalEncoding) : null;
    };
    GameHeader.prototype.teams = function (index, obj) {
        var offset = this.bb.__offset(this.bb_pos, 6);
        return offset ? (obj || new team_data_1.TeamData()).__init(this.bb.__indirect(this.bb.__vector(this.bb_pos + offset) + index * 4), this.bb) : null;
    };
    GameHeader.prototype.teamsLength = function () {
        var offset = this.bb.__offset(this.bb_pos, 6);
        return offset ? this.bb.__vector_len(this.bb_pos + offset) : 0;
    };
    GameHeader.prototype.specializationMetadata = function (index, obj) {
        var offset = this.bb.__offset(this.bb_pos, 8);
        return offset ? (obj || new specialization_metadata_1.SpecializationMetadata()).__init(this.bb.__indirect(this.bb.__vector(this.bb_pos + offset) + index * 4), this.bb) : null;
    };
    GameHeader.prototype.specializationMetadataLength = function () {
        var offset = this.bb.__offset(this.bb_pos, 8);
        return offset ? this.bb.__vector_len(this.bb_pos + offset) : 0;
    };
    GameHeader.prototype.buildActionMetadata = function (index, obj) {
        var offset = this.bb.__offset(this.bb_pos, 10);
        return offset ? (obj || new build_action_metadata_1.BuildActionMetadata()).__init(this.bb.__indirect(this.bb.__vector(this.bb_pos + offset) + index * 4), this.bb) : null;
    };
    GameHeader.prototype.buildActionMetadataLength = function () {
        var offset = this.bb.__offset(this.bb_pos, 10);
        return offset ? this.bb.__vector_len(this.bb_pos + offset) : 0;
    };
    GameHeader.prototype.globalUpgradeMetadata = function (index, obj) {
        var offset = this.bb.__offset(this.bb_pos, 12);
        return offset ? (obj || new global_upgrade_metadata_1.GlobalUpgradeMetadata()).__init(this.bb.__indirect(this.bb.__vector(this.bb_pos + offset) + index * 4), this.bb) : null;
    };
    GameHeader.prototype.globalUpgradeMetadataLength = function () {
        var offset = this.bb.__offset(this.bb_pos, 12);
        return offset ? this.bb.__vector_len(this.bb_pos + offset) : 0;
    };
    GameHeader.prototype.constants = function (obj) {
        var offset = this.bb.__offset(this.bb_pos, 14);
        return offset ? (obj || new constants_1.Constants()).__init(this.bb.__indirect(this.bb_pos + offset), this.bb) : null;
    };
    GameHeader.startGameHeader = function (builder) {
        builder.startObject(6);
    };
    GameHeader.addSpecVersion = function (builder, specVersionOffset) {
        builder.addFieldOffset(0, specVersionOffset, 0);
    };
    GameHeader.addTeams = function (builder, teamsOffset) {
        builder.addFieldOffset(1, teamsOffset, 0);
    };
    GameHeader.createTeamsVector = function (builder, data) {
        builder.startVector(4, data.length, 4);
        for (var i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    };
    GameHeader.startTeamsVector = function (builder, numElems) {
        builder.startVector(4, numElems, 4);
    };
    GameHeader.addSpecializationMetadata = function (builder, specializationMetadataOffset) {
        builder.addFieldOffset(2, specializationMetadataOffset, 0);
    };
    GameHeader.createSpecializationMetadataVector = function (builder, data) {
        builder.startVector(4, data.length, 4);
        for (var i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    };
    GameHeader.startSpecializationMetadataVector = function (builder, numElems) {
        builder.startVector(4, numElems, 4);
    };
    GameHeader.addBuildActionMetadata = function (builder, buildActionMetadataOffset) {
        builder.addFieldOffset(3, buildActionMetadataOffset, 0);
    };
    GameHeader.createBuildActionMetadataVector = function (builder, data) {
        builder.startVector(4, data.length, 4);
        for (var i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    };
    GameHeader.startBuildActionMetadataVector = function (builder, numElems) {
        builder.startVector(4, numElems, 4);
    };
    GameHeader.addGlobalUpgradeMetadata = function (builder, globalUpgradeMetadataOffset) {
        builder.addFieldOffset(4, globalUpgradeMetadataOffset, 0);
    };
    GameHeader.createGlobalUpgradeMetadataVector = function (builder, data) {
        builder.startVector(4, data.length, 4);
        for (var i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    };
    GameHeader.startGlobalUpgradeMetadataVector = function (builder, numElems) {
        builder.startVector(4, numElems, 4);
    };
    GameHeader.addConstants = function (builder, constantsOffset) {
        builder.addFieldOffset(5, constantsOffset, 0);
    };
    GameHeader.endGameHeader = function (builder) {
        var offset = builder.endObject();
        return offset;
    };
    return GameHeader;
}());
exports.GameHeader = GameHeader;

"use strict";
// automatically generated by the FlatBuffers compiler, do not modify
Object.defineProperty(exports, "__esModule", { value: true });
exports.TeamData = void 0;
var flatbuffers = require("flatbuffers");
var TeamData = /** @class */ (function () {
    function TeamData() {
        this.bb = null;
        this.bb_pos = 0;
    }
    TeamData.prototype.__init = function (i, bb) {
        this.bb_pos = i;
        this.bb = bb;
        return this;
    };
    TeamData.getRootAsTeamData = function (bb, obj) {
        return (obj || new TeamData()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
    };
    TeamData.getSizePrefixedRootAsTeamData = function (bb, obj) {
        bb.setPosition(bb.position() + flatbuffers.SIZE_PREFIX_LENGTH);
        return (obj || new TeamData()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
    };
    TeamData.prototype.name = function (optionalEncoding) {
        var offset = this.bb.__offset(this.bb_pos, 4);
        return offset ? this.bb.__string(this.bb_pos + offset, optionalEncoding) : null;
    };
    TeamData.prototype.packageName = function (optionalEncoding) {
        var offset = this.bb.__offset(this.bb_pos, 6);
        return offset ? this.bb.__string(this.bb_pos + offset, optionalEncoding) : null;
    };
    TeamData.prototype.teamId = function () {
        var offset = this.bb.__offset(this.bb_pos, 8);
        return offset ? this.bb.readInt8(this.bb_pos + offset) : 0;
    };
    TeamData.startTeamData = function (builder) {
        builder.startObject(3);
    };
    TeamData.addName = function (builder, nameOffset) {
        builder.addFieldOffset(0, nameOffset, 0);
    };
    TeamData.addPackageName = function (builder, packageNameOffset) {
        builder.addFieldOffset(1, packageNameOffset, 0);
    };
    TeamData.addTeamId = function (builder, teamId) {
        builder.addFieldInt8(2, teamId, 0);
    };
    TeamData.endTeamData = function (builder) {
        var offset = builder.endObject();
        return offset;
    };
    TeamData.createTeamData = function (builder, nameOffset, packageNameOffset, teamId) {
        TeamData.startTeamData(builder);
        TeamData.addName(builder, nameOffset);
        TeamData.addPackageName(builder, packageNameOffset);
        TeamData.addTeamId(builder, teamId);
        return TeamData.endTeamData(builder);
    };
    return TeamData;
}());
exports.TeamData = TeamData;

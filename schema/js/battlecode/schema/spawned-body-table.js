"use strict";
// automatically generated by the FlatBuffers compiler, do not modify
Object.defineProperty(exports, "__esModule", { value: true });
exports.SpawnedBodyTable = void 0;
var flatbuffers = require("flatbuffers");
var vec_table_1 = require("../../battlecode/schema/vec-table");
var SpawnedBodyTable = /** @class */ (function () {
    function SpawnedBodyTable() {
        this.bb = null;
        this.bb_pos = 0;
    }
    SpawnedBodyTable.prototype.__init = function (i, bb) {
        this.bb_pos = i;
        this.bb = bb;
        return this;
    };
    SpawnedBodyTable.getRootAsSpawnedBodyTable = function (bb, obj) {
        return (obj || new SpawnedBodyTable()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
    };
    SpawnedBodyTable.getSizePrefixedRootAsSpawnedBodyTable = function (bb, obj) {
        bb.setPosition(bb.position() + flatbuffers.SIZE_PREFIX_LENGTH);
        return (obj || new SpawnedBodyTable()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
    };
    SpawnedBodyTable.prototype.robotIds = function (index) {
        var offset = this.bb.__offset(this.bb_pos, 4);
        return offset ? this.bb.readInt32(this.bb.__vector(this.bb_pos + offset) + index * 4) : 0;
    };
    SpawnedBodyTable.prototype.robotIdsLength = function () {
        var offset = this.bb.__offset(this.bb_pos, 4);
        return offset ? this.bb.__vector_len(this.bb_pos + offset) : 0;
    };
    SpawnedBodyTable.prototype.robotIdsArray = function () {
        var offset = this.bb.__offset(this.bb_pos, 4);
        return offset ? new Int32Array(this.bb.bytes().buffer, this.bb.bytes().byteOffset + this.bb.__vector(this.bb_pos + offset), this.bb.__vector_len(this.bb_pos + offset)) : null;
    };
    SpawnedBodyTable.prototype.teamIds = function (index) {
        var offset = this.bb.__offset(this.bb_pos, 6);
        return offset ? this.bb.readInt8(this.bb.__vector(this.bb_pos + offset) + index) : 0;
    };
    SpawnedBodyTable.prototype.teamIdsLength = function () {
        var offset = this.bb.__offset(this.bb_pos, 6);
        return offset ? this.bb.__vector_len(this.bb_pos + offset) : 0;
    };
    SpawnedBodyTable.prototype.teamIdsArray = function () {
        var offset = this.bb.__offset(this.bb_pos, 6);
        return offset ? new Int8Array(this.bb.bytes().buffer, this.bb.bytes().byteOffset + this.bb.__vector(this.bb_pos + offset), this.bb.__vector_len(this.bb_pos + offset)) : null;
    };
    SpawnedBodyTable.prototype.locs = function (obj) {
        var offset = this.bb.__offset(this.bb_pos, 8);
        return offset ? (obj || new vec_table_1.VecTable()).__init(this.bb.__indirect(this.bb_pos + offset), this.bb) : null;
    };
    SpawnedBodyTable.startSpawnedBodyTable = function (builder) {
        builder.startObject(3);
    };
    SpawnedBodyTable.addRobotIds = function (builder, robotIdsOffset) {
        builder.addFieldOffset(0, robotIdsOffset, 0);
    };
    SpawnedBodyTable.createRobotIdsVector = function (builder, data) {
        builder.startVector(4, data.length, 4);
        for (var i = data.length - 1; i >= 0; i--) {
            builder.addInt32(data[i]);
        }
        return builder.endVector();
    };
    SpawnedBodyTable.startRobotIdsVector = function (builder, numElems) {
        builder.startVector(4, numElems, 4);
    };
    SpawnedBodyTable.addTeamIds = function (builder, teamIdsOffset) {
        builder.addFieldOffset(1, teamIdsOffset, 0);
    };
    SpawnedBodyTable.createTeamIdsVector = function (builder, data) {
        builder.startVector(1, data.length, 1);
        for (var i = data.length - 1; i >= 0; i--) {
            builder.addInt8(data[i]);
        }
        return builder.endVector();
    };
    SpawnedBodyTable.startTeamIdsVector = function (builder, numElems) {
        builder.startVector(1, numElems, 1);
    };
    SpawnedBodyTable.addLocs = function (builder, locsOffset) {
        builder.addFieldOffset(2, locsOffset, 0);
    };
    SpawnedBodyTable.endSpawnedBodyTable = function (builder) {
        var offset = builder.endObject();
        return offset;
    };
    return SpawnedBodyTable;
}());
exports.SpawnedBodyTable = SpawnedBodyTable;
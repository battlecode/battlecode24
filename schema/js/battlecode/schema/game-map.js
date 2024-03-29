"use strict";
// automatically generated by the FlatBuffers compiler, do not modify
Object.defineProperty(exports, "__esModule", { value: true });
exports.GameMap = void 0;
var flatbuffers = require("flatbuffers");
var spawned_body_table_1 = require("../../battlecode/schema/spawned-body-table");
var vec_1 = require("../../battlecode/schema/vec");
var vec_table_1 = require("../../battlecode/schema/vec-table");
var GameMap = /** @class */ (function () {
    function GameMap() {
        this.bb = null;
        this.bb_pos = 0;
    }
    GameMap.prototype.__init = function (i, bb) {
        this.bb_pos = i;
        this.bb = bb;
        return this;
    };
    GameMap.getRootAsGameMap = function (bb, obj) {
        return (obj || new GameMap()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
    };
    GameMap.getSizePrefixedRootAsGameMap = function (bb, obj) {
        bb.setPosition(bb.position() + flatbuffers.SIZE_PREFIX_LENGTH);
        return (obj || new GameMap()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
    };
    GameMap.prototype.name = function (optionalEncoding) {
        var offset = this.bb.__offset(this.bb_pos, 4);
        return offset ? this.bb.__string(this.bb_pos + offset, optionalEncoding) : null;
    };
    GameMap.prototype.size = function (obj) {
        var offset = this.bb.__offset(this.bb_pos, 6);
        return offset ? (obj || new vec_1.Vec()).__init(this.bb_pos + offset, this.bb) : null;
    };
    GameMap.prototype.symmetry = function () {
        var offset = this.bb.__offset(this.bb_pos, 8);
        return offset ? this.bb.readInt32(this.bb_pos + offset) : 0;
    };
    GameMap.prototype.bodies = function (obj) {
        var offset = this.bb.__offset(this.bb_pos, 10);
        return offset ? (obj || new spawned_body_table_1.SpawnedBodyTable()).__init(this.bb.__indirect(this.bb_pos + offset), this.bb) : null;
    };
    GameMap.prototype.randomSeed = function () {
        var offset = this.bb.__offset(this.bb_pos, 12);
        return offset ? this.bb.readInt32(this.bb_pos + offset) : 0;
    };
    GameMap.prototype.walls = function (index) {
        var offset = this.bb.__offset(this.bb_pos, 14);
        return offset ? !!this.bb.readInt8(this.bb.__vector(this.bb_pos + offset) + index) : false;
    };
    GameMap.prototype.wallsLength = function () {
        var offset = this.bb.__offset(this.bb_pos, 14);
        return offset ? this.bb.__vector_len(this.bb_pos + offset) : 0;
    };
    GameMap.prototype.wallsArray = function () {
        var offset = this.bb.__offset(this.bb_pos, 14);
        return offset ? new Int8Array(this.bb.bytes().buffer, this.bb.bytes().byteOffset + this.bb.__vector(this.bb_pos + offset), this.bb.__vector_len(this.bb_pos + offset)) : null;
    };
    GameMap.prototype.water = function (index) {
        var offset = this.bb.__offset(this.bb_pos, 16);
        return offset ? !!this.bb.readInt8(this.bb.__vector(this.bb_pos + offset) + index) : false;
    };
    GameMap.prototype.waterLength = function () {
        var offset = this.bb.__offset(this.bb_pos, 16);
        return offset ? this.bb.__vector_len(this.bb_pos + offset) : 0;
    };
    GameMap.prototype.waterArray = function () {
        var offset = this.bb.__offset(this.bb_pos, 16);
        return offset ? new Int8Array(this.bb.bytes().buffer, this.bb.bytes().byteOffset + this.bb.__vector(this.bb_pos + offset), this.bb.__vector_len(this.bb_pos + offset)) : null;
    };
    GameMap.prototype.divider = function (index) {
        var offset = this.bb.__offset(this.bb_pos, 18);
        return offset ? !!this.bb.readInt8(this.bb.__vector(this.bb_pos + offset) + index) : false;
    };
    GameMap.prototype.dividerLength = function () {
        var offset = this.bb.__offset(this.bb_pos, 18);
        return offset ? this.bb.__vector_len(this.bb_pos + offset) : 0;
    };
    GameMap.prototype.dividerArray = function () {
        var offset = this.bb.__offset(this.bb_pos, 18);
        return offset ? new Int8Array(this.bb.bytes().buffer, this.bb.bytes().byteOffset + this.bb.__vector(this.bb_pos + offset), this.bb.__vector_len(this.bb_pos + offset)) : null;
    };
    GameMap.prototype.spawnLocations = function (obj) {
        var offset = this.bb.__offset(this.bb_pos, 20);
        return offset ? (obj || new vec_table_1.VecTable()).__init(this.bb.__indirect(this.bb_pos + offset), this.bb) : null;
    };
    GameMap.prototype.resourcePiles = function (obj) {
        var offset = this.bb.__offset(this.bb_pos, 22);
        return offset ? (obj || new vec_table_1.VecTable()).__init(this.bb.__indirect(this.bb_pos + offset), this.bb) : null;
    };
    GameMap.prototype.resourcePileAmounts = function (index) {
        var offset = this.bb.__offset(this.bb_pos, 24);
        return offset ? this.bb.readInt32(this.bb.__vector(this.bb_pos + offset) + index * 4) : 0;
    };
    GameMap.prototype.resourcePileAmountsLength = function () {
        var offset = this.bb.__offset(this.bb_pos, 24);
        return offset ? this.bb.__vector_len(this.bb_pos + offset) : 0;
    };
    GameMap.prototype.resourcePileAmountsArray = function () {
        var offset = this.bb.__offset(this.bb_pos, 24);
        return offset ? new Int32Array(this.bb.bytes().buffer, this.bb.bytes().byteOffset + this.bb.__vector(this.bb_pos + offset), this.bb.__vector_len(this.bb_pos + offset)) : null;
    };
    GameMap.startGameMap = function (builder) {
        builder.startObject(11);
    };
    GameMap.addName = function (builder, nameOffset) {
        builder.addFieldOffset(0, nameOffset, 0);
    };
    GameMap.addSize = function (builder, sizeOffset) {
        builder.addFieldStruct(1, sizeOffset, 0);
    };
    GameMap.addSymmetry = function (builder, symmetry) {
        builder.addFieldInt32(2, symmetry, 0);
    };
    GameMap.addBodies = function (builder, bodiesOffset) {
        builder.addFieldOffset(3, bodiesOffset, 0);
    };
    GameMap.addRandomSeed = function (builder, randomSeed) {
        builder.addFieldInt32(4, randomSeed, 0);
    };
    GameMap.addWalls = function (builder, wallsOffset) {
        builder.addFieldOffset(5, wallsOffset, 0);
    };
    GameMap.createWallsVector = function (builder, data) {
        builder.startVector(1, data.length, 1);
        for (var i = data.length - 1; i >= 0; i--) {
            builder.addInt8(+data[i]);
        }
        return builder.endVector();
    };
    GameMap.startWallsVector = function (builder, numElems) {
        builder.startVector(1, numElems, 1);
    };
    GameMap.addWater = function (builder, waterOffset) {
        builder.addFieldOffset(6, waterOffset, 0);
    };
    GameMap.createWaterVector = function (builder, data) {
        builder.startVector(1, data.length, 1);
        for (var i = data.length - 1; i >= 0; i--) {
            builder.addInt8(+data[i]);
        }
        return builder.endVector();
    };
    GameMap.startWaterVector = function (builder, numElems) {
        builder.startVector(1, numElems, 1);
    };
    GameMap.addDivider = function (builder, dividerOffset) {
        builder.addFieldOffset(7, dividerOffset, 0);
    };
    GameMap.createDividerVector = function (builder, data) {
        builder.startVector(1, data.length, 1);
        for (var i = data.length - 1; i >= 0; i--) {
            builder.addInt8(+data[i]);
        }
        return builder.endVector();
    };
    GameMap.startDividerVector = function (builder, numElems) {
        builder.startVector(1, numElems, 1);
    };
    GameMap.addSpawnLocations = function (builder, spawnLocationsOffset) {
        builder.addFieldOffset(8, spawnLocationsOffset, 0);
    };
    GameMap.addResourcePiles = function (builder, resourcePilesOffset) {
        builder.addFieldOffset(9, resourcePilesOffset, 0);
    };
    GameMap.addResourcePileAmounts = function (builder, resourcePileAmountsOffset) {
        builder.addFieldOffset(10, resourcePileAmountsOffset, 0);
    };
    GameMap.createResourcePileAmountsVector = function (builder, data) {
        builder.startVector(4, data.length, 4);
        for (var i = data.length - 1; i >= 0; i--) {
            builder.addInt32(data[i]);
        }
        return builder.endVector();
    };
    GameMap.startResourcePileAmountsVector = function (builder, numElems) {
        builder.startVector(4, numElems, 4);
    };
    GameMap.endGameMap = function (builder) {
        var offset = builder.endObject();
        return offset;
    };
    return GameMap;
}());
exports.GameMap = GameMap;

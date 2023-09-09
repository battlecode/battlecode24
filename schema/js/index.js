"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    Object.defineProperty(o, k2, { enumerable: true, get: function() { return m[k]; } });
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
exports.__esModule = true;
exports.flatbuffers = exports.schema = void 0;
var battlecode_generated_1 = require("./battlecode_generated");
var schema = battlecode_generated_1.battlecode.schema;
exports.schema = schema;
var flatbuffers_1 = require("flatbuffers");
__createBinding(exports, flatbuffers_1, "flatbuffers");
// export { battlecode.schema as schema } from './battlecode_generated';

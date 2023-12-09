import * as flatbuffers from 'flatbuffers';
import { Action } from '../../battlecode/schema/action';
import { BuildActionType } from '../../battlecode/schema/build-action-type';
import { CommTable } from '../../battlecode/schema/comm-table';
import { RGBTable } from '../../battlecode/schema/rgbtable';
import { SpawnedBodyTable } from '../../battlecode/schema/spawned-body-table';
import { VecTable } from '../../battlecode/schema/vec-table';
/**
 * A single time-step in a Game.
 * The bulk of the data in the file is stored in tables like this.
 * Note that a struct-of-arrays format is more space efficient than an array-
 * of-structs.
 */
export declare class Round {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): Round;
    static getRootAsRound(bb: flatbuffers.ByteBuffer, obj?: Round): Round;
    static getSizePrefixedRootAsRound(bb: flatbuffers.ByteBuffer, obj?: Round): Round;
    /**
     * The IDs of teams in the Game.
     */
    teamIds(index: number): number | null;
    teamIdsLength(): number;
    teamIdsArray(): Int32Array | null;
    /**
     * The total amount of resource this round per team
     */
    teamResourceAmounts(index: number): number | null;
    teamResourceAmountsLength(): number;
    teamResourceAmountsArray(): Int32Array | null;
    teamCommunication(obj?: CommTable): CommTable | null;
    robotIds(index: number): number | null;
    robotIdsLength(): number;
    robotIdsArray(): Int32Array | null;
    robotLocs(obj?: VecTable): VecTable | null;
    robotMoveCooldowns(index: number): number | null;
    robotMoveCooldownsLength(): number;
    robotMoveCooldownsArray(): Int32Array | null;
    robotActionCooldowns(index: number): number | null;
    robotActionCooldownsLength(): number;
    robotActionCooldownsArray(): Int32Array | null;
    robotHealths(index: number): number | null;
    robotHealthsLength(): number;
    robotHealthsArray(): Int32Array | null;
    attacksPerformed(index: number): number | null;
    attacksPerformedLength(): number;
    attacksPerformedArray(): Int32Array | null;
    attackLevels(index: number): number | null;
    attackLevelsLength(): number;
    attackLevelsArray(): Int32Array | null;
    buildsPerformed(index: number): number | null;
    buildsPerformedLength(): number;
    buildsPerformedArray(): Int32Array | null;
    buildLevels(index: number): number | null;
    buildLevelsLength(): number;
    buildLevelsArray(): Int32Array | null;
    healsPerformed(index: number): number | null;
    healsPerformedLength(): number;
    healsPerformedArray(): Int32Array | null;
    healLevels(index: number): number | null;
    healLevelsLength(): number;
    healLevelsArray(): Int32Array | null;
    /**
     * New bodies.
     */
    spawnedBodies(obj?: SpawnedBodyTable): SpawnedBodyTable | null;
    /**
     * The IDs of bodies that died.
     */
    diedIds(index: number): number | null;
    diedIdsLength(): number;
    diedIdsArray(): Int32Array | null;
    /**
     * The IDs of robots that performed actions.
     * IDs may repeat.
     */
    actionIds(index: number): number | null;
    actionIdsLength(): number;
    actionIdsArray(): Int32Array | null;
    /**
     * The actions performed. These actions allow us to track many things about the current state.
     */
    actions(index: number): Action | null;
    actionsLength(): number;
    actionsArray(): Int8Array | null;
    /**
     * The 'targets' of the performed actions. Actions without targets may have any value
     */
    actionTargets(index: number): number | null;
    actionTargetsLength(): number;
    actionTargetsArray(): Int32Array | null;
    claimedResourcePiles(obj?: VecTable): VecTable | null;
    trapAddedIds(index: number): number | null;
    trapAddedIdsLength(): number;
    trapAddedIdsArray(): Int32Array | null;
    trapAddedLocations(obj?: VecTable): VecTable | null;
    trapAddedTypes(index: number): BuildActionType | null;
    trapAddedTypesLength(): number;
    trapAddedTypesArray(): Int8Array | null;
    trapAddedTeams(index: number): number | null;
    trapAddedTeamsLength(): number;
    trapAddedTeamsArray(): Int8Array | null;
    trapTriggeredIds(index: number): number | null;
    trapTriggeredIdsLength(): number;
    trapTriggeredIdsArray(): Int32Array | null;
    digLocations(obj?: VecTable): VecTable | null;
    fillLocations(obj?: VecTable): VecTable | null;
    /**
     * The IDs of the robots who changed their indicator strings
     */
    indicatorStringIds(index: number): number | null;
    indicatorStringIdsLength(): number;
    indicatorStringIdsArray(): Int32Array | null;
    /**
     * The messages of the robots who changed their indicator strings
     */
    indicatorStrings(index: number): string;
    indicatorStrings(index: number, optionalEncoding: flatbuffers.Encoding): string | Uint8Array;
    indicatorStringsLength(): number;
    /**
     * The IDs of bodies that set indicator dots
     */
    indicatorDotIds(index: number): number | null;
    indicatorDotIdsLength(): number;
    indicatorDotIdsArray(): Int32Array | null;
    /**
     * The location of the indicator dots
     */
    indicatorDotLocs(obj?: VecTable): VecTable | null;
    /**
     * The RGB values of the indicator dots
     */
    indicatorDotRgbs(obj?: RGBTable): RGBTable | null;
    /**
     * The IDs of bodies that set indicator lines
     */
    indicatorLineIds(index: number): number | null;
    indicatorLineIdsLength(): number;
    indicatorLineIdsArray(): Int32Array | null;
    /**
     * The start location of the indicator lines
     */
    indicatorLineStartLocs(obj?: VecTable): VecTable | null;
    /**
     * The end location of the indicator lines
     */
    indicatorLineEndLocs(obj?: VecTable): VecTable | null;
    /**
     * The RGB values of the indicator lines
     */
    indicatorLineRgbs(obj?: RGBTable): RGBTable | null;
    /**
     * The first sent Round in a match should have index 1. (The starting state,
     * created by the MatchHeader, can be thought to have index 0.)
     * It should increase by one for each following round.
     */
    roundId(): number;
    /**
     * The IDs of player bodies.
     */
    bytecodeIds(index: number): number | null;
    bytecodeIdsLength(): number;
    bytecodeIdsArray(): Int32Array | null;
    /**
     * The bytecodes used by the player bodies.
     */
    bytecodesUsed(index: number): number | null;
    bytecodesUsedLength(): number;
    bytecodesUsedArray(): Int32Array | null;
    static startRound(builder: flatbuffers.Builder): void;
    static addTeamIds(builder: flatbuffers.Builder, teamIdsOffset: flatbuffers.Offset): void;
    static createTeamIdsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createTeamIdsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startTeamIdsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addTeamResourceAmounts(builder: flatbuffers.Builder, teamResourceAmountsOffset: flatbuffers.Offset): void;
    static createTeamResourceAmountsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createTeamResourceAmountsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startTeamResourceAmountsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addTeamCommunication(builder: flatbuffers.Builder, teamCommunicationOffset: flatbuffers.Offset): void;
    static addRobotIds(builder: flatbuffers.Builder, robotIdsOffset: flatbuffers.Offset): void;
    static createRobotIdsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createRobotIdsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startRobotIdsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addRobotLocs(builder: flatbuffers.Builder, robotLocsOffset: flatbuffers.Offset): void;
    static addRobotMoveCooldowns(builder: flatbuffers.Builder, robotMoveCooldownsOffset: flatbuffers.Offset): void;
    static createRobotMoveCooldownsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createRobotMoveCooldownsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startRobotMoveCooldownsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addRobotActionCooldowns(builder: flatbuffers.Builder, robotActionCooldownsOffset: flatbuffers.Offset): void;
    static createRobotActionCooldownsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createRobotActionCooldownsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startRobotActionCooldownsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addRobotHealths(builder: flatbuffers.Builder, robotHealthsOffset: flatbuffers.Offset): void;
    static createRobotHealthsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createRobotHealthsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startRobotHealthsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addAttacksPerformed(builder: flatbuffers.Builder, attacksPerformedOffset: flatbuffers.Offset): void;
    static createAttacksPerformedVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createAttacksPerformedVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startAttacksPerformedVector(builder: flatbuffers.Builder, numElems: number): void;
    static addAttackLevels(builder: flatbuffers.Builder, attackLevelsOffset: flatbuffers.Offset): void;
    static createAttackLevelsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createAttackLevelsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startAttackLevelsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addBuildsPerformed(builder: flatbuffers.Builder, buildsPerformedOffset: flatbuffers.Offset): void;
    static createBuildsPerformedVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createBuildsPerformedVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startBuildsPerformedVector(builder: flatbuffers.Builder, numElems: number): void;
    static addBuildLevels(builder: flatbuffers.Builder, buildLevelsOffset: flatbuffers.Offset): void;
    static createBuildLevelsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createBuildLevelsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startBuildLevelsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addHealsPerformed(builder: flatbuffers.Builder, healsPerformedOffset: flatbuffers.Offset): void;
    static createHealsPerformedVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createHealsPerformedVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startHealsPerformedVector(builder: flatbuffers.Builder, numElems: number): void;
    static addHealLevels(builder: flatbuffers.Builder, healLevelsOffset: flatbuffers.Offset): void;
    static createHealLevelsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createHealLevelsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startHealLevelsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addSpawnedBodies(builder: flatbuffers.Builder, spawnedBodiesOffset: flatbuffers.Offset): void;
    static addDiedIds(builder: flatbuffers.Builder, diedIdsOffset: flatbuffers.Offset): void;
    static createDiedIdsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createDiedIdsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startDiedIdsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addActionIds(builder: flatbuffers.Builder, actionIdsOffset: flatbuffers.Offset): void;
    static createActionIdsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createActionIdsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startActionIdsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addActions(builder: flatbuffers.Builder, actionsOffset: flatbuffers.Offset): void;
    static createActionsVector(builder: flatbuffers.Builder, data: Action[]): flatbuffers.Offset;
    static startActionsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addActionTargets(builder: flatbuffers.Builder, actionTargetsOffset: flatbuffers.Offset): void;
    static createActionTargetsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createActionTargetsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startActionTargetsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addClaimedResourcePiles(builder: flatbuffers.Builder, claimedResourcePilesOffset: flatbuffers.Offset): void;
    static addTrapAddedIds(builder: flatbuffers.Builder, trapAddedIdsOffset: flatbuffers.Offset): void;
    static createTrapAddedIdsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createTrapAddedIdsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startTrapAddedIdsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addTrapAddedLocations(builder: flatbuffers.Builder, trapAddedLocationsOffset: flatbuffers.Offset): void;
    static addTrapAddedTypes(builder: flatbuffers.Builder, trapAddedTypesOffset: flatbuffers.Offset): void;
    static createTrapAddedTypesVector(builder: flatbuffers.Builder, data: BuildActionType[]): flatbuffers.Offset;
    static startTrapAddedTypesVector(builder: flatbuffers.Builder, numElems: number): void;
    static addTrapAddedTeams(builder: flatbuffers.Builder, trapAddedTeamsOffset: flatbuffers.Offset): void;
    static createTrapAddedTeamsVector(builder: flatbuffers.Builder, data: number[] | Int8Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createTrapAddedTeamsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startTrapAddedTeamsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addTrapTriggeredIds(builder: flatbuffers.Builder, trapTriggeredIdsOffset: flatbuffers.Offset): void;
    static createTrapTriggeredIdsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createTrapTriggeredIdsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startTrapTriggeredIdsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addDigLocations(builder: flatbuffers.Builder, digLocationsOffset: flatbuffers.Offset): void;
    static addFillLocations(builder: flatbuffers.Builder, fillLocationsOffset: flatbuffers.Offset): void;
    static addIndicatorStringIds(builder: flatbuffers.Builder, indicatorStringIdsOffset: flatbuffers.Offset): void;
    static createIndicatorStringIdsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createIndicatorStringIdsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startIndicatorStringIdsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addIndicatorStrings(builder: flatbuffers.Builder, indicatorStringsOffset: flatbuffers.Offset): void;
    static createIndicatorStringsVector(builder: flatbuffers.Builder, data: flatbuffers.Offset[]): flatbuffers.Offset;
    static startIndicatorStringsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addIndicatorDotIds(builder: flatbuffers.Builder, indicatorDotIdsOffset: flatbuffers.Offset): void;
    static createIndicatorDotIdsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createIndicatorDotIdsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startIndicatorDotIdsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addIndicatorDotLocs(builder: flatbuffers.Builder, indicatorDotLocsOffset: flatbuffers.Offset): void;
    static addIndicatorDotRgbs(builder: flatbuffers.Builder, indicatorDotRgbsOffset: flatbuffers.Offset): void;
    static addIndicatorLineIds(builder: flatbuffers.Builder, indicatorLineIdsOffset: flatbuffers.Offset): void;
    static createIndicatorLineIdsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createIndicatorLineIdsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startIndicatorLineIdsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addIndicatorLineStartLocs(builder: flatbuffers.Builder, indicatorLineStartLocsOffset: flatbuffers.Offset): void;
    static addIndicatorLineEndLocs(builder: flatbuffers.Builder, indicatorLineEndLocsOffset: flatbuffers.Offset): void;
    static addIndicatorLineRgbs(builder: flatbuffers.Builder, indicatorLineRgbsOffset: flatbuffers.Offset): void;
    static addRoundId(builder: flatbuffers.Builder, roundId: number): void;
    static addBytecodeIds(builder: flatbuffers.Builder, bytecodeIdsOffset: flatbuffers.Offset): void;
    static createBytecodeIdsVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createBytecodeIdsVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startBytecodeIdsVector(builder: flatbuffers.Builder, numElems: number): void;
    static addBytecodesUsed(builder: flatbuffers.Builder, bytecodesUsedOffset: flatbuffers.Offset): void;
    static createBytecodesUsedVector(builder: flatbuffers.Builder, data: number[] | Int32Array): flatbuffers.Offset;
    /**
     * @deprecated This Uint8Array overload will be removed in the future.
     */
    static createBytecodesUsedVector(builder: flatbuffers.Builder, data: number[] | Uint8Array): flatbuffers.Offset;
    static startBytecodesUsedVector(builder: flatbuffers.Builder, numElems: number): void;
    static endRound(builder: flatbuffers.Builder): flatbuffers.Offset;
}

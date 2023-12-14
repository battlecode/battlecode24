import * as flatbuffers from 'flatbuffers';
/**
 * The final event sent in the game.
 */
export declare class GameFooter {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): GameFooter;
    static getRootAsGameFooter(bb: flatbuffers.ByteBuffer, obj?: GameFooter): GameFooter;
    static getSizePrefixedRootAsGameFooter(bb: flatbuffers.ByteBuffer, obj?: GameFooter): GameFooter;
    /**
     * The ID of the winning team of the game.
     */
    winner(): number;
    static startGameFooter(builder: flatbuffers.Builder): void;
    static addWinner(builder: flatbuffers.Builder, winner: number): void;
    static endGameFooter(builder: flatbuffers.Builder): flatbuffers.Offset;
    static createGameFooter(builder: flatbuffers.Builder, winner: number): flatbuffers.Offset;
}

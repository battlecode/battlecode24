import { schema } from 'battlecode-schema';
import assert from 'assert';
import Game, { Team } from './Game';
import Turn from './Turn';
import TurnStat from './TurnStat';
import { CurrentMap, StaticMap } from './Map';
import Actions from './Actions';
import Bodies from './Bodies';


const SNAPSHOT_EVERY = 50;

export default class Match {
    public readonly game: Game;
    private readonly deltas: schema.Round[];
    private readonly snapshots: Turn[];
    private currentTurn: Turn;
    private readonly maxTurn: number;
    public readonly stats: TurnStat[] = [];
    public readonly winner: Team;
    private readonly map: StaticMap;

    constructor(game: Game, header: schema.MatchHeader, turns: schema.Round[], footer: schema.MatchFooter) {
        this.game = game;
        this.winner = game.teams[footer.winner()];

        const mapData = header.map() ?? assert.fail('Map data not found in header');
        this.map = new StaticMap(mapData);

        const firstBodies = new Bodies(game, mapData.bodies() ?? assert.fail('Initial bodies not found in header'));

        this.maxTurn = header.maxRounds();

        this.currentTurn = new Turn(this, 0, new CurrentMap(this.map), firstBodies, new Actions(), new TurnStat(game));
        this.snapshots = [this.currentTurn];

        this.deltas = turns;
        this.deltas.forEach((delta, i) => assert(delta.roundID() === i, `Wrong turn ID: is ${delta.roundID()}, should be ${i}`));

        assert(footer.totalRounds() === this.deltas.length, `Wrong total turn count: is ${footer.totalRounds()}, should be ${this.deltas.length - 1}`);
        assert(footer.totalRounds() == this.maxTurn + 1, `(not sure why theres two fields, maybe I misunderstood one of them) Wrong total turn count: is ${footer.totalRounds()}, should be ${this.maxTurn}`);
    }

    /**
     * Change the rounds current turn to the current turn + delta.
     */
    public stepTurn(delta: number): void {
        this.jumpToTurn(this.currentTurn.turnNumber + delta);
    }

    /**
     * Sets the current turn to the turn at the given turn number.
     */
    public jumpToTurn(turnNumber: number): void {
        const snapshotIndex = Math.min(Math.floor(turnNumber / SNAPSHOT_EVERY), this.snapshots.length - 1);
        let turn = this.snapshots[snapshotIndex].copy();

        while (turn.turnNumber < turnNumber) {
            turn.applyDelta(this.deltas[turn.turnNumber + 1]);
            if (turn.turnNumber % SNAPSHOT_EVERY === 0 && this.snapshots.length < turn.turnNumber / SNAPSHOT_EVERY) {
                this.snapshots.push(turn.copy());
            }
        }

        this.currentTurn = turn;
    }
}
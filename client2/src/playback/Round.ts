import { Team } from './game';
import Turn from './Turn';
import TurnStat from './TurnStat';
import { CurrentMap, StaticMap } from './Map';

const SNAPSHOT_EVERY = 50;

export default class Round {
    private readonly snapshots: Turn[];
    private currentTurn: Turn;
    public readonly stats: TurnStat[] = [];
    public readonly winner: Team;
    public readonly loser: Team;
    private readonly map: StaticMap;

    constructor(something: any) {
        this.winner = something.winner;
        this.loser = something.loser;
        this.map = something.map;

        this.currentTurn = new Turn(this, 0, new CurrentMap(this.map), something.initialBodies, new TurnStat());
        this.snapshots = [this.currentTurn];
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
            turn.applyDelta('todo');
            if (turn.turnNumber % SNAPSHOT_EVERY === 0 && this.snapshots.length < turn.turnNumber / SNAPSHOT_EVERY) {
                this.snapshots.push(turn.copy());
            }
        }

        this.currentTurn = turn;
    }
}
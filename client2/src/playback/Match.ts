import { schema } from 'battlecode-schema'
import assert from 'assert'
import Game, { Team } from './Game'
import Turn from './Turn'
import TurnStat from './TurnStat'
import { CurrentMap, StaticMap } from './Map'
import Actions from './Actions'
import Bodies from './Bodies'

const SNAPSHOT_EVERY = 50

export default class Match {
    public readonly game: Game
    private readonly deltas: schema.Round[]
    private readonly snapshots: Turn[]
    public currentTurn: Turn
    private readonly maxTurn: number
    public readonly stats: TurnStat[] = []
    public readonly winner: Team
    private readonly map: StaticMap

    constructor(game: Game, header: schema.MatchHeader, turns: schema.Round[], footer: schema.MatchFooter) {
        this.game = game
        this.winner = game.teams[footer.winner()]

        const mapData = header.map() ?? assert.fail('Map data not found in header')
        this.map = new StaticMap(mapData)

        const firstStats = new TurnStat(game)
        const firstBodies = new Bodies(game, mapData.bodies() ?? assert.fail('Initial bodies not found in header'), firstStats)

        this.maxTurn = header.maxRounds()

        this.currentTurn = new Turn(this, 0, new CurrentMap(this.map), firstBodies, new Actions(), firstStats)
        this.snapshots = [this.currentTurn]
        this.stats = [this.currentTurn.stat]

        this.deltas = turns
        this.deltas.forEach((delta, i) =>
            assert(delta.roundID() === i + 1, `Wrong turn ID: is ${delta.roundID()}, should be ${i}`)
        )
    }

    /**
     * Change the rounds current turn to the current turn + delta.
     */
    public stepTurn(delta: number): void {
        this.jumpToTurn(this.currentTurn.turnNumber + delta)
    }

    /**
     * Sets the current turn to the turn at the given turn number.
     */
    public jumpToTurn(turnNumber: number): void {
        turnNumber = Math.min(turnNumber, this.deltas.length)
        const snapshotIndex = Math.min(Math.floor(turnNumber / SNAPSHOT_EVERY), this.snapshots.length - 1)
        let turn = this.snapshots[snapshotIndex].copy()

        while (turn.turnNumber < turnNumber) {
            turn.applyDelta(this.deltas[turn.turnNumber])
            if (turn.turnNumber % SNAPSHOT_EVERY === 0 && this.snapshots.length < turn.turnNumber / SNAPSHOT_EVERY) {
                this.snapshots.push(turn.copy())
            }
        }

        this.currentTurn = turn
    }
}

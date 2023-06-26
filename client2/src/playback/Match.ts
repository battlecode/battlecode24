import { schema } from 'battlecode-schema'
import assert from 'assert'
import Game, { Team } from './Game'
import Turn from './Turn'
import TurnStat from './TurnStat'
import { CurrentMap, StaticMap } from './Map'
import Actions from './Actions'
import Bodies from './Bodies'
import { publishEvent, EventType } from '../app-events'

const SNAPSHOT_EVERY = 50

export default class Match {
    public currentTurn: Turn
    private readonly snapshots: Turn[]
    public readonly stats: TurnStat[]
    constructor(
        public readonly game: Game,
        private readonly deltas: schema.Round[],
        private readonly maxTurn: number,
        public readonly winner: Team,
        public readonly map: StaticMap,
        firstBodies: Bodies,
        firstStats: TurnStat
    ) {
        this.currentTurn = new Turn(this, 0, new CurrentMap(map), firstBodies, new Actions(), firstStats)
        this.snapshots = [this.currentTurn]
        this.stats = [this.currentTurn.stat]
    }

    /**
     * Creates a blank match for use in the map editor.
     */
    public static createBlank(game: Game, bodies: Bodies, map: StaticMap): Match {
        const firstStats = new TurnStat(game)
        return new Match(game, [], 0, game.teams[0], map, bodies, firstStats)
    }

    /**
     * Creates a match from a map for loading into the map editor from an existing file.
     */
    public static fromMap(schema_map: schema.GameMap, game: Game, map: StaticMap): Match {
        const firstStats = new TurnStat(game)
        const mapBodies = schema_map.bodies() ?? assert.fail('Initial bodies not found in header')
        const bodies = new Bodies(game, mapBodies, firstStats)
        return new Match(game, [], 0, game.teams[0], map, bodies, firstStats)
    }

    public static fromSchema(
        game: Game,
        header: schema.MatchHeader,
        turns: schema.Round[],
        footer: schema.MatchFooter
    ) {
        const winner = game.teams[footer.winner()]

        const mapData = header.map() ?? assert.fail('Map data not found in header')
        const map = StaticMap.fromSchema(mapData)

        const firstStats = new TurnStat(game)
        const firstBodies = new Bodies(
            game,
            mapData.bodies() ?? assert.fail('Initial bodies not found in header'),
            firstStats
        )

        const maxTurn = header.maxRounds()

        const deltas = turns
        deltas.forEach((delta, i) =>
            assert(delta.roundID() === i + 1, `Wrong turn ID: is ${delta.roundID()}, should be ${i}`)
        )

        return new Match(game, deltas, maxTurn, winner, map, firstBodies, firstStats)
    }

    /**
     * Change the rounds current turn to the current turn + delta.
     */
    public stepTurn(delta: number): void {
        this.jumpToTurn(this.currentTurn.turnNumber + delta)
    }

    /**
     * Sets the current turn to the last turn.
     */
    public jumpToEnd(): void {
        this.jumpToTurn(this.maxTurn)
    }

    /**
     * Sets the current turn to the turn at the given turn number.
     */
    public jumpToTurn(turnNumber: number): void {
        if (!this.game.playable) return

        turnNumber = Math.max(0, Math.min(turnNumber, this.deltas.length))
        if (turnNumber == this.currentTurn.turnNumber) return

        const snapshotIndex = Math.min(Math.floor(turnNumber / SNAPSHOT_EVERY), this.snapshots.length - 1)
        let turn = this.snapshots[snapshotIndex].copy()

        let steps = 0
        while (turn.turnNumber < turnNumber) {
            steps += 1
            turn.applyDelta(this.deltas[turn.turnNumber])
            if (
                turn.turnNumber % SNAPSHOT_EVERY === 0 &&
                this.snapshots.length < turn.turnNumber / SNAPSHOT_EVERY + 1
            ) {
                this.snapshots.push(turn.copy())
            }
        }

        // console.log(`Calculated ${steps} steps to generate turn ${turnNumber}.`)

        this.currentTurn = turn
        publishEvent(EventType.TURN_PROGRESS, {})
    }
}

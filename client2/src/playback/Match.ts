import { schema } from 'battlecode-schema'
import assert from 'assert'
import Game, { Team } from './Game'
import Turn from './Turn'
import TurnStat from './TurnStat'
import { CurrentMap, StaticMap } from './Map'
import Actions from './Actions'
import Bodies from './Bodies'
import { publishEvent, EventType } from '../app-events'

// Amount of turns before a snapshot of the game state is saved for the next recalculation
const SNAPSHOT_EVERY = 50

// Amount of simulation steps before the turn counter is progressed
export const MAX_SIMULATION_STEPS = 500

export default class Match {
    public currentTurn: Turn
    private readonly snapshots: Turn[]
    public readonly stats: TurnStat[]
    private currentSimulationStep: number = 0
    constructor(
        public readonly game: Game,
        private readonly deltas: schema.Round[],
        public readonly maxTurn: number,
        public readonly winner: Team,
        public readonly map: StaticMap,
        firstBodies: Bodies,
        firstStats: TurnStat
    ) {
        this.currentTurn = new Turn(this, 0, new CurrentMap(map), firstBodies, new Actions(), firstStats)
        this.snapshots = [this.currentTurn.copy()]
        this.stats = [this.snapshots[0].stat]
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
        const bodies = new Bodies(game, mapBodies, firstStats, map)
        return new Match(game, [], 0, game.teams[0], map, bodies, firstStats)
    }

    public static fromSchema(
        game: Game,
        header: schema.MatchHeader,
        turns: schema.Round[],
        footer: schema.MatchFooter
    ) {
        const winner = game.teams[footer.winner() - 1]

        const mapData = header.map() ?? assert.fail('Map data not found in header')
        const map = StaticMap.fromSchema(mapData)

        const firstStats = new TurnStat(game)
        const firstBodies = new Bodies(
            game,
            mapData.bodies() ?? assert.fail('Initial bodies not found in header'),
            firstStats
        )

        // header.maxRounds() is always 2000

        const deltas = turns
        deltas.forEach((delta, i) =>
            assert(delta.roundID() === i + 1, `Wrong turn ID: is ${delta.roundID()}, should be ${i}`)
        )

        const maxTurn = deltas.length

        return new Match(game, deltas, maxTurn, winner, map, firstBodies, firstStats)
    }

    /**
     * Returns the normalized 0-1 value indicating the simulation progression for this turn.
     */
    public getInterpolationFactor(): number {
        return (Math.abs(this.currentSimulationStep) % MAX_SIMULATION_STEPS) / MAX_SIMULATION_STEPS
    }

    /**
     * Force a rerender by publishing a RENDER event
     */
    public rerender(): void {
        publishEvent(EventType.RENDER, {})
    }

    /**
     * Change the simulation step to the current step + delta. If the step reaches the max simulation steps, the turn counter is increased accordingly
     */
    public stepSimulation(delta: number): void {
        this.currentSimulationStep += delta
        if (this.currentSimulationStep < 0) this.currentSimulationStep = 0

        const prevTurn = this.currentTurn.turnNumber
        this.jumpToTurn(Math.floor(this.currentSimulationStep / MAX_SIMULATION_STEPS))

        // jumpToTurn will call render if the turn number changes so we shouldn't
        // do it again
        if (prevTurn == this.currentTurn.turnNumber) this.rerender()
    }

    /**
     * Clear any excess simulation steps and round it to the nearest turn
     */
    public roundSimulation(): void {
        this.currentSimulationStep -= this.currentSimulationStep % MAX_SIMULATION_STEPS
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

        // If we are stepping backwards, we must always recompute from the latest checkpoint
        const reversed = turnNumber < this.currentTurn.turnNumber

        // If the new turn is closer to a snapshot than from the current turn, compute from the snapshot
        const snapshotIndex = Math.floor(turnNumber / SNAPSHOT_EVERY)
        const closeSnapshot =
            snapshotIndex > Math.floor(this.currentTurn.turnNumber / SNAPSHOT_EVERY) &&
            snapshotIndex < this.snapshots.length

        const computeFromSnapshot = reversed || closeSnapshot
        let updatingTurn = this.currentTurn
        if (computeFromSnapshot) updatingTurn = this.snapshots[snapshotIndex].copy()

        while (updatingTurn.turnNumber < turnNumber) {
            const delta = this.deltas[updatingTurn.turnNumber]
            const nextDelta =
                updatingTurn.turnNumber < this.deltas.length - 1 ? this.deltas[updatingTurn.turnNumber + 1] : null
            updatingTurn.applyDelta(delta, nextDelta)

            if (
                updatingTurn.turnNumber % SNAPSHOT_EVERY === 0 &&
                this.snapshots.length < updatingTurn.turnNumber / SNAPSHOT_EVERY + 1
            ) {
                this.snapshots.push(updatingTurn.copy())
            }
        }

        // Update the simulation step so it remains consistent with the total turn value
        // while also keeping the current simulation progress
        this.currentSimulationStep =
            updatingTurn.turnNumber * MAX_SIMULATION_STEPS + (this.currentSimulationStep % MAX_SIMULATION_STEPS)

        this.currentTurn = updatingTurn
        publishEvent(EventType.TURN_PROGRESS, {})
        this.rerender()
    }
}

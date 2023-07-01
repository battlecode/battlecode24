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
    public readonly game: Game
    private readonly deltas: schema.Round[]
    private readonly snapshots: Turn[]
    public currentTurn: Turn
    private currentSimulationStep: number
    private readonly maxTurn: number
    public readonly stats: TurnStat[] = []
    public readonly winner: Team
    public readonly map: StaticMap

    constructor(game: Game, header: schema.MatchHeader, turns: schema.Round[], footer: schema.MatchFooter) {
        this.game = game
        this.winner = game.teams[footer.winner()]

        const mapData = header.map() ?? assert.fail('Map data not found in header')
        this.map = new StaticMap(mapData)

        const firstStats = new TurnStat(game)
        const firstBodies = new Bodies(
            game,
            mapData.bodies() ?? assert.fail('Initial bodies not found in header'),
            firstStats
        )

        this.maxTurn = header.maxRounds()

        this.currentSimulationStep = 0
        this.currentTurn = new Turn(this, 0, new CurrentMap(this.map), firstBodies, new Actions(), firstStats)
        this.snapshots = [this.currentTurn]
        this.stats = [this.currentTurn.stat]

        this.deltas = turns
        this.deltas.forEach((delta, i) =>
            assert(delta.roundID() === i + 1, `Wrong turn ID: is ${delta.roundID()}, should be ${i}`)
        )
    }

    /**
     * Returns the normalized 0-1 value indicating the simulation progression for this turn
     */
    public getInterpolationFactor(): number {
        return this.currentSimulationStep / MAX_SIMULATION_STEPS
    }

    /**
     * Change the simulation step to the current step + delta. If the step reaches
     * the max simulation steps, the turn counter is increased accordingly
     */
    public stepSimulation(delta: number): void {
        this.currentSimulationStep += delta

        if (this.currentSimulationStep >= MAX_SIMULATION_STEPS) {
            this.jumpToTurn(this.currentTurn.turnNumber + Math.floor(this.currentSimulationStep / MAX_SIMULATION_STEPS))
            return
        }

        publishEvent(EventType.RENDER, {})
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
        turnNumber = Math.max(0, Math.min(turnNumber, this.deltas.length))
        if (turnNumber == this.currentTurn.turnNumber) return

        // TODO: we don't need to recompute from snapshot every time, we can
        // definitely reuse the previous turn sometimes
        const snapshotIndex = Math.min(Math.floor(turnNumber / SNAPSHOT_EVERY), this.snapshots.length - 1)
        let turn = this.snapshots[snapshotIndex].copy()

        while (turn.turnNumber < turnNumber) {
            const delta = this.deltas[turn.turnNumber]
            const nextDelta = turn.turnNumber < this.deltas.length - 1 ? this.deltas[turn.turnNumber + 1] : null
            turn.applyDelta(delta, nextDelta)
            if (
                turn.turnNumber % SNAPSHOT_EVERY === 0 &&
                this.snapshots.length < turn.turnNumber / SNAPSHOT_EVERY + 1
            ) {
                this.snapshots.push(turn.copy())
            }
        }

        this.currentSimulationStep = 0
        this.currentTurn = turn
        publishEvent(EventType.TURN_PROGRESS, {})
        publishEvent(EventType.RENDER, {})
    }
}

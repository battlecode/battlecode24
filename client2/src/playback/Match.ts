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
    private currentSimulationStep: number = 0
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

        this.currentTurn = new Turn(this, 0, new CurrentMap(this.map), firstBodies, new Actions(), firstStats)
        this.snapshots = [this.currentTurn.copy()]
        this.stats = [this.currentTurn.stat]

        this.deltas = turns
        this.deltas.forEach((delta, i) =>
            assert(delta.roundID() === i + 1, `Wrong turn ID: is ${delta.roundID()}, should be ${i}`)
        )
    }

    /**
     * Returns the normalized 0-1 value indicating the simulation progression for this turn.
     */
    public getInterpolationFactor(): number {
        return (Math.abs(this.currentSimulationStep) % MAX_SIMULATION_STEPS) / MAX_SIMULATION_STEPS
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
        if (prevTurn == this.currentTurn.turnNumber) publishEvent(EventType.RENDER, {})
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
        publishEvent(EventType.RENDER, {})
    }
}

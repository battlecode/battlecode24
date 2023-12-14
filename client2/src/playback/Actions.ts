import Turn from './Turn'
import { schema } from 'battlecode-schema'
import assert from 'assert'
import * as renderUtils from '../util/RenderUtil'
import * as vectorUtils from './Vector'
import { Dimension } from './Map'
import { Team } from './Game'
import Match from './Match'

export default class Actions {
    actions: Action[] = []

    constructor() {}

    applyDelta(turn: Turn, delta: schema.Round): void {
        for (let i = 0; i < this.actions.length; i++) {
            this.actions[i].duration--
            if (this.actions[i].duration == 0) {
                this.actions.splice(i, 1)
                i--
            }
        }

        if (delta.actionsLength() > 0) {
            for (let i = 0; i < delta.actionsLength(); i++) {
                const action = delta.actions(i) ?? assert.fail('actions not found in round')
                const robotID = delta.actionIds(i) ?? assert.fail('actionIDs not found in round')
                const target = delta.actionTargets(i) ?? assert.fail('actionTargets not found in round')
                const actionClass =
                    ACTION_DEFINITIONS[action] ?? assert.fail(`Action ${action} not found in ACTION_DEFINITIONS`)
                const newAction = new actionClass(robotID, target)
                this.actions.push(newAction)
                newAction.apply(turn)
            }
        }
    }

    copy(): Actions {
        const newActions = new Actions()
        newActions.actions = this.actions.map((action) => action.copy())
        return newActions
    }

    draw(match: Match, ctx: CanvasRenderingContext2D) {
        for (const action of this.actions) {
            action.draw(match, ctx)
        }
    }
}

export class Action {
    constructor(protected robotID: number, protected target: number, public duration: number = 1) {}

    /**
     * Applies this action to the turn provided. If stat is provided, it will be mutated to reflect the action as well
     *
     * @param turn the turn to apply this action to
     * @param stat if provided, this action will mutate the stat to reflect the action
     */
    apply(turn: Turn): void {}
    draw(match: Match, ctx: CanvasRenderingContext2D) {}
    copy(): Action {
        // creates a new object using this object's prototype and all its parameters. this is a shallow copy, override this if you need a deep copy
        return Object.create(Object.getPrototypeOf(this), Object.getOwnPropertyDescriptors(this))
    }
}

export const ACTION_DEFINITIONS: Record<schema.Action, typeof Action> = {
    [schema.Action.DIE_EXCEPTION]: class DieException extends Action {
        apply(turn: Turn): void {
            console.log(`Exception occured: robotID(${this.robotID}), target(${this.target}`)
        }
    },
    [schema.Action.ATTACK]: class Dig extends Action {
        apply(turn: Turn): void {
            // To dicuss
        }
        draw(match: Match, ctx: CanvasRenderingContext2D) {
            const body = match.currentTurn.bodies.getById(this.robotID) ?? assert.fail('Attacking body not found')
            const interpStart = renderUtils.getInterpolatedCoords(
                body.pos,
                body.nextPos,
                match.getInterpolationFactor()
            )

            let interpEnd
            // Target is negative when it represents a miss (map location hit). Otherwise,
            // the attack hit a bot so we must transform the target into the bot's location
            if (this.target >= 0) {
                const targetBody =
                    match.currentTurn.bodies.getById(this.target) ?? assert.fail('Attack target not found')
                interpEnd = renderUtils.getInterpolatedCoords(
                    targetBody.pos,
                    targetBody.nextPos,
                    match.getInterpolationFactor()
                )
            } else {
                interpEnd = match.currentTurn.map.indexToLocation(-this.target - 1)
            }

            // Compute the start and end points for the animation projectile
            const dir = vectorUtils.vectorSub(interpEnd, interpStart)
            const len = vectorUtils.vectorLength(dir)
            vectorUtils.vectorMultiplyInPlace(dir, 1 / len)
            const projectileStart = vectorUtils.vectorAdd(
                interpStart,
                vectorUtils.vectorMultiply(dir, len * match.getInterpolationFactor())
            )
            const projectileEnd = vectorUtils.vectorAdd(
                interpStart,
                vectorUtils.vectorMultiply(dir, len * Math.min(match.getInterpolationFactor() + 0.2, 1.0))
            )

            // True direction
            renderUtils.renderLine(
                ctx,
                renderUtils.getRenderCoords(interpStart.x, interpStart.y, match.currentTurn.map.staticMap.dimension),
                renderUtils.getRenderCoords(interpEnd.x, interpEnd.y, match.currentTurn.map.staticMap.dimension),
                body.team,
                0.05,
                0.1,
                true
            )

            // Projectile animation
            renderUtils.renderLine(
                ctx,
                renderUtils.getRenderCoords(
                    projectileStart.x,
                    projectileStart.y,
                    match.currentTurn.map.staticMap.dimension
                ),
                renderUtils.getRenderCoords(
                    projectileEnd.x,
                    projectileEnd.y,
                    match.currentTurn.map.staticMap.dimension
                ),
                body.team,
                0.05,
                1.0,
                false
            )
        }
    },
    [schema.Action.HEAL]: class Heal extends Action {
        apply(turn: Turn): void {
            const body = turn.bodies.getById(this.robotID)
            //if (!turn.stat.completed) turn.stat.getTeamStat(body.team).total_hp[body.type] += this.target
            //body.hp += this.target

            // Implementation Questions:
            // How much do you heal by?
        }
    },
    [schema.Action.DIG]: class Dig extends Action {
        apply(turn: Turn): void {
            // To dicuss
        }
    },
    [schema.Action.FILL]: class Fill extends Action {
        apply(turn: Turn): void {
            // To dicuss
        }
    },
    [schema.Action.EXPLOSIVE_TRAP]: class ExplosiveTrap extends Action {
        apply(turn: Turn): void {
            // To dicuss
        }
    },
    [schema.Action.WATER_TRAP]: class WaterTrap extends Action {
        apply(turn: Turn): void {
            // To dicuss
        }
    },
    [schema.Action.STUN_TRAP]: class StunTrap extends Action {
        apply(turn: Turn): void {
            // To dicuss
        }
    },
    [schema.Action.PICKUP_FLAG]: class PickupFlag extends Action {
        apply(turn: Turn): void {
            // To dicuss
        }
    },
    [schema.Action.DROP_FLAG]: class DropFlag extends Action {
        apply(turn: Turn): void {
            // To dicuss
        }
    },
    [schema.Action.GLOBAL_UPGRADE]: class GlobalUpgrade extends Action {
        apply(turn: Turn): void {
            // To dicuss
        }
    }
}

/*
export const ACTION_DEFINITIONS: Record<number, typeof Action> = { 
    [schema.Action.CHANGE_HEALTH]: class ChangeHealth extends Action {
        apply(turn: Turn): void {
            const body = turn.bodies.getById(this.robotID)
            if (!turn.stat.completed) turn.stat.getTeamStat(body.team).total_hp[body.type] += this.target
            body.hp += this.target
        }
    },
    [schema.Action.CHANGE_MANA]: class ChangeMana extends Action {
        apply(turn: Turn): void {
            const body = turn.bodies.getById(this.robotID)
            if (!turn.stat.completed && body.type !== schema.BodyType.HEADQUARTERS)
                turn.stat.getTeamStat(body.team).manaMined += this.target
            body.mana += this.target
        }
    },
    [schema.Action.CHANGE_ELIXIR]: class ChangeElixir extends Action {
        apply(turn: Turn): void {
            const body = turn.bodies.getById(this.robotID)
            if (!turn.stat.completed && body.type !== schema.BodyType.HEADQUARTERS)
                turn.stat.getTeamStat(body.team).elixirMined += this.target
            body.elixir += this.target
        }
    },
    [schema.Action.CHANGE_ADAMANTIUM]: class ChangeAdamantium extends Action {
        apply(turn: Turn): void {
            const body = turn.bodies.getById(this.robotID)
            if (!turn.stat.completed && body.type !== schema.BodyType.HEADQUARTERS)
                turn.stat.getTeamStat(body.team).adamantiumMined += this.target
            body.adamantium += this.target
        }
    },
    [schema.Action.THROW_ATTACK]: class Throw extends Action {
        constructor(robotID: number, target: number) {
            super(robotID, target, 1)
        }
        apply(turn: Turn): void {
            const body = turn.bodies.getById(this.robotID)
            assert(body.type === schema.BodyType.CARRIER, 'Cannot throw from non-carrier')
            body.clearResources()
        }
        draw(match: Match, ctx: CanvasRenderingContext2D) {
            //const targetLoc = turn.map.indexToLocation(this.target)
        }
    },
    [schema.Action.LAUNCH_ATTACK]: class Launch extends Action {
        apply(turn: Turn): void {
            const body = turn.bodies.getById(this.robotID) ?? assert.fail('Attacking body not found')
            assert(body.type === schema.BodyType.LAUNCHER, 'Cannot launch from non-launcher')
        }
        draw(match: Match, ctx: CanvasRenderingContext2D) {
            const body = match.currentTurn.bodies.getById(this.robotID) ?? assert.fail('Attacking body not found')
            const interpStart = renderUtils.getInterpolatedCoords(
                body.pos,
                body.nextPos,
                match.getInterpolationFactor()
            )

            let interpEnd
            // Target is negative when it represents a miss (map location hit). Otherwise,
            // the attack hit a bot so we must transform the target into the bot's location
            if (this.target >= 0) {
                const targetBody =
                    match.currentTurn.bodies.getById(this.target) ?? assert.fail('Attack target not found')
                interpEnd = renderUtils.getInterpolatedCoords(
                    targetBody.pos,
                    targetBody.nextPos,
                    match.getInterpolationFactor()
                )
            } else {
                interpEnd = match.currentTurn.map.indexToLocation(-this.target - 1)
            }

            // Compute the start and end points for the animation projectile
            const dir = vectorUtils.vectorSub(interpEnd, interpStart)
            const len = vectorUtils.vectorLength(dir)
            vectorUtils.vectorMultiplyInPlace(dir, 1 / len)
            const projectileStart = vectorUtils.vectorAdd(
                interpStart,
                vectorUtils.vectorMultiply(dir, len * match.getInterpolationFactor())
            )
            const projectileEnd = vectorUtils.vectorAdd(
                interpStart,
                vectorUtils.vectorMultiply(dir, len * Math.min(match.getInterpolationFactor() + 0.2, 1.0))
            )

            // True direction
            renderUtils.renderLine(
                ctx,
                renderUtils.getRenderCoords(interpStart.x, interpStart.y, match.currentTurn.map.staticMap.dimension),
                renderUtils.getRenderCoords(interpEnd.x, interpEnd.y, match.currentTurn.map.staticMap.dimension),
                body.team,
                0.05,
                0.1,
                true
            )

            // Projectile animation
            renderUtils.renderLine(
                ctx,
                renderUtils.getRenderCoords(
                    projectileStart.x,
                    projectileStart.y,
                    match.currentTurn.map.staticMap.dimension
                ),
                renderUtils.getRenderCoords(
                    projectileEnd.x,
                    projectileEnd.y,
                    match.currentTurn.map.staticMap.dimension
                ),
                body.team,
                0.05,
                1.0,
                false
            )
        }
    },

    // Unused (not visualized)
    [schema.Action.SPAWN_UNIT]: Action,
    [schema.Action.PICK_UP_ANCHOR]: Action,
    [schema.Action.PLACE_ANCHOR]: Action,
    [schema.Action.DESTABILIZE]: Action,
    [schema.Action.DESTABILIZE_DAMAGE]: Action,
    [schema.Action.BOOST]: Action,
    [schema.Action.BUILD_ANCHOR]: Action,
    [schema.Action.PICK_UP_RESOURCE]: Action,
    [schema.Action.PLACE_RESOURCE]: Action
}

*/

import Turn from './Turn'
import { schema } from 'battlecode-schema'
import assert from 'assert'
import * as renderUtils from '../util/RenderUtil'
import { vectorAdd, vectorLength, vectorMultiply, vectorSub, vectorMultiplyInPlace } from './Vector'
import { Dimension } from './Map'
import { Team } from './Game'
import Match from './Match'
import { Body } from './Bodies'
import { render } from '@headlessui/react/dist/utils/render'
import { ATTACK_COLOR, GRASS_COLOR, HEAL_COLOR, TEAM_COLORS, WATER_COLOR } from '../constants'

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

export abstract class ToFromAction extends Action {
    constructor(robotID: number, target: number) {
        super(robotID, target)
    }

    abstract drawToFrom(
        match: Match,
        ctx: CanvasRenderingContext2D,
        from: { x: number; y: number },
        to: { x: number; y: number },
        body: Body
    ): void

    draw(match: Match, ctx: CanvasRenderingContext2D) {
        const body = match.currentTurn.bodies.getById(this.robotID) ?? assert.fail('Acting body not found')
        const interpStart = renderUtils.getInterpolatedCoordsFromBody(body, match.getInterpolationFactor())
        const targetBody = match.currentTurn.bodies.getById(this.target) ?? assert.fail('Action target not found')
        const interpEnd = renderUtils.getInterpolatedCoordsFromBody(targetBody, match.getInterpolationFactor())
        this.drawToFrom(match, ctx, interpStart, interpEnd, body)
    }
}

export const ACTION_DEFINITIONS: Record<schema.Action, typeof Action> = {
    [schema.Action.DIE_EXCEPTION]: class DieException extends Action {
        apply(turn: Turn): void {
            console.log(`Exception occured: robotID(${this.robotID}), target(${this.target}`)
        }
    },
    [schema.Action.ATTACK]: class Dig extends ToFromAction {
        apply(turn: Turn): void {
            // To dicuss
        }
        drawToFrom(
            match: Match,
            ctx: CanvasRenderingContext2D,
            from: { x: number; y: number },
            to: { x: number; y: number },
            body: Body
        ): void {
            // Compute the start and end points for the animation projectile
            const dir = vectorSub(to, from)
            const len = vectorLength(dir)
            vectorMultiplyInPlace(dir, 1 / len)
            const projectileStart = vectorAdd(from, vectorMultiply(dir, len * match.getInterpolationFactor()))
            const projectileEnd = vectorAdd(
                from,
                vectorMultiply(dir, len * Math.min(match.getInterpolationFactor() + 0.2, 1.0))
            )

            // True direction
            renderUtils.renderLine(
                ctx,
                renderUtils.getRenderCoords(from.x, from.y, match.currentTurn.map.staticMap.dimension),
                renderUtils.getRenderCoords(to.x, to.y, match.currentTurn.map.staticMap.dimension),
                { teamForOffset: body.team, color: body.team.color, lineWidth: 0.05, opacity: 0.1, renderArrow: false }
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
                { teamForOffset: body.team, color: body.team.color, lineWidth: 0.05, opacity: 1.0, renderArrow: false }
            )
        }
    },
    [schema.Action.HEAL]: class Heal extends ToFromAction {
        apply(turn: Turn): void {
            // To dicuss
        }
        drawToFrom(
            match: Match,
            ctx: CanvasRenderingContext2D,
            from: { x: number; y: number },
            to: { x: number; y: number },
            body: Body
        ): void {
            renderUtils.renderLine(
                ctx,
                renderUtils.getRenderCoords(from.x, from.y, match.currentTurn.map.staticMap.dimension),
                renderUtils.getRenderCoords(to.x, to.y, match.currentTurn.map.staticMap.dimension),
                {
                    color: HEAL_COLOR,
                    lineWidth: 0.05,
                    opacity: 0.1,
                    renderArrow: true
                }
            )
        }
    },
    [schema.Action.DIG]: class Dig extends Action {
        apply(turn: Turn): void {
            turn.map.water[this.target] = 0
        }
    },
    [schema.Action.FILL]: class Fill extends Action {
        apply(turn: Turn): void {
            turn.map.water[this.target] = 1
        }
    },
    [schema.Action.EXPLOSIVE_TRAP]: class ExplosiveTrap extends Action {
        apply(turn: Turn): void {
            // To dicuss
        }
        draw(match: Match, ctx: CanvasRenderingContext2D): void {
            const map = match.currentTurn.map
            const trapId = this.target
            const trapData = map.trapData.get(trapId)!
            const coords = trapData.location
            const radius = 3.3 // in between the two sizes of the explosion
            ctx.strokeStyle = TEAM_COLORS[trapData.team]
            ctx.fillStyle = ATTACK_COLOR
            ctx.beginPath()
            ctx.arc(
                renderUtils.getRenderCoords(coords.x, coords.y, map.dimension).x,
                renderUtils.getRenderCoords(coords.x, coords.y, map.dimension).y,
                radius,
                0,
                2 * Math.PI
            )
            ctx.fill()
            ctx.stroke()
        }
    },
    [schema.Action.WATER_TRAP]: class WaterTrap extends Action {
        apply(turn: Turn): void {
            const trapId = this.target
            const trapData = turn.map.trapData.get(trapId)!
            const coords = trapData.location
            const rad = 3
            // change all non wall and non spawnzone tiles to water
            for (let x = coords.x - rad; x <= coords.x + rad; x++) {
                for (let y = coords.y - rad; y <= coords.y + rad; y++) {
                    if (x < 0 || y < 0 || x >= turn.map.width || y >= turn.map.height) continue
                    if (Math.pow(x - coords.x, 2) + Math.pow(y - coords.y, 2) > rad * rad) continue
                    const idx = turn.map.locationToIndex(x, y)
                    if (turn.map.staticMap.walls[idx]) continue
                    let inSpawnZone = false
                    for (const spawnZone of turn.map.staticMap.spawnLocations)
                        inSpawnZone ||= Math.abs(spawnZone.x - x) <= 1 && Math.abs(spawnZone.y - y) <= 1
                    if (inSpawnZone) continue

                    turn.map.water[idx] = 1
                }
            }
        }
        draw(match: Match, ctx: CanvasRenderingContext2D): void {
            const map = match.currentTurn.map
            const trapId = this.target
            const trapData = map.trapData.get(trapId)!
            const coords = trapData.location
            const radius = 3
            ctx.strokeStyle = TEAM_COLORS[trapData.team]
            ctx.fillStyle = WATER_COLOR
            ctx.beginPath()
            ctx.arc(
                renderUtils.getRenderCoords(coords.x, coords.y, match.currentTurn.map.staticMap.dimension).x,
                renderUtils.getRenderCoords(coords.x, coords.y, match.currentTurn.map.staticMap.dimension).y,
                radius,
                0,
                2 * Math.PI
            )
            ctx.fill()
            ctx.stroke()
        }
    },
    [schema.Action.STUN_TRAP]: class StunTrap extends Action {
        apply(turn: Turn): void {
            // To dicuss
        }
        draw(match: Match, ctx: CanvasRenderingContext2D): void {
            const map = match.currentTurn.map
            const trapId = this.target
            const trapData = map.trapData.get(trapId)!
            const coords = trapData.location
            const radius = Math.sqrt(13)
            ctx.strokeStyle = TEAM_COLORS[trapData.team]
            ctx.fillStyle = 'black'
            ctx.beginPath()
            ctx.arc(
                renderUtils.getRenderCoords(coords.x, coords.y, match.currentTurn.map.staticMap.dimension).x,
                renderUtils.getRenderCoords(coords.x, coords.y, match.currentTurn.map.staticMap.dimension).y,
                radius,
                0,
                2 * Math.PI
            )
            ctx.fill()
            ctx.stroke()
        }
    },
    [schema.Action.PICKUP_FLAG]: class PickupFlag extends Action {
        apply(turn: Turn): void {
            const flagId = this.target
            const flagData = turn.map.flagData.get(flagId)!
            flagData.carrierId = this.robotID
        }
    },
    [schema.Action.DROP_FLAG]: class DropFlag extends Action {
        apply(turn: Turn): void {
            // Find flag id based on carrying robot
            let flagId = -1
            turn.map.flagData.forEach((v, k) => {
                if (v.carrierId == this.robotID) {
                    flagId = k
                }
            })
            const flagData = turn.map.flagData.get(flagId)!
            flagData.carrierId = null
            flagData.location = turn.map.indexToLocation(this.target)
        }
    },
    [schema.Action.CAPTURE_FLAG]: class CaptureFlag extends Action {
        apply(turn: Turn): void {
            const flagId = this.target
            turn.map.flagData.delete(flagId)
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
            const dir = vectorSub(interpEnd, interpStart)
            const len = vectorLength(dir)
            vectorMultiplyInPlace(dir, 1 / len)
            const projectileStart = vectorAdd(
                interpStart,
                vectorMultiply(dir, len * match.getInterpolationFactor())
            )
            const projectileEnd = vectorAdd(
                interpStart,
                vectorMultiply(dir, len * Math.min(match.getInterpolationFactor() + 0.2, 1.0))
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

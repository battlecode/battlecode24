import { flatbuffers, schema } from 'battlecode-schema'
import assert from 'assert'
import Game, { Team } from './Game'
import Turn from './Turn'
import TurnStat from './TurnStat'
import { getImageIfLoaded, loadImage } from '../util/ImageLoader'
import * as renderUtils from '../util/RenderUtil'
import { MapEditorBrush } from '../components/sidebar/map-editor/MapEditorBrush'
import { Dimension, StaticMap } from './Map'
import { Vector } from './Vector'
import { ATTACK_COLOR, BUILD_COLOR, HEAL_COLOR, TOOLTIP_PATH_LENGTH } from '../constants'
import Match from './Match'
import { TestDuckBrush } from './Brushes'

export default class Bodies {
    public bodies: Map<number, Body> = new Map()

    constructor(
        public readonly game: Game,
        initialBodies?: schema.SpawnedBodyTable,
        initialStats?: TurnStat,
        mapToVerify?: StaticMap
    ) {
        if (initialBodies) this.insertBodies(initialBodies, initialStats)

        // We have initial bodies this year, but we don't know how spawn zones work quite yet
        /*
        if (mapToVerify) {
            for (let i = 0; i < mapToVerify.width * mapToVerify.height; i++) {
                if (mapToVerify.walls[i] == 1 || mapToVerify.initialResources[i] > 0) {
                    for (const body of this.bodies.values()) {
                        if (body.pos.x == i % mapToVerify.width && body.pos.y == Math.floor(i / mapToVerify.width)) {
                            assert.fail(`Body at (${body.pos.x}, ${body.pos.y}) is on top of a wall or resource`)
                        }
                    }
                }
            }
        }
        */
    }

    updateBodyPositions(delta: schema.Round, allowNullBodies: boolean) {
        const movedLocs = delta.robotLocs()
        if (!movedLocs) return
        const movedIds = delta.robotIdsArray() ?? assert.fail('movedIDsArray not found in round')
        const xsArray = movedLocs.xsArray() ?? assert.fail('movedLocs.xsArray not found in round')
        const ysArray = movedLocs.ysArray() ?? assert.fail('movedLocs.ysArray not found in round')
        for (let i = 0; i < delta.robotIdsLength(); i++) {
            const id = movedIds[i]
            const body = this.bodies.get(id)

            assert(!body?.dead, `Moved body ${id} is dead`)
            assert.equal(allowNullBodies || !!body, true, `Moved body ${id} not found in bodies`)

            if (body) body.moveTo({ x: xsArray[i], y: ysArray[i] })
        }
    }

    /**
     * Applies a delta to the bodies array. Because of update order, bodies will first
     * be inserted, followed by a call to scopedCallback() in which all bodies are valid.
     */
    applyDelta(turn: Turn, delta: schema.Round, nextDelta: schema.Round | null): void {
        // remove all that are dead
        for (const body of this.bodies.values()) {
            if (body.dead) this.bodies.delete(body.id)
        }

        const bodies = delta.spawnedBodies()
        if (bodies) this.insertBodies(bodies, turn.stat.completed ? undefined : turn.stat)

        // Update positions with respect to interpolation. The first call to update will set the body's
        // target location to the value in delta. This is important when the body eventually gets removed
        // because it should still interpolate to its final position. The second call to update will set the
        // target position to the body's true next position iff it exists. In this case, we allow null
        // bodies and skip them since they may not exist in the next turn. Most of the updates here are extra
        // since the first call is really only necessary for bodies that die, so there is potential for
        // optimization.
        this.updateBodyPositions(delta, false)
        if (nextDelta) {
            this.updateBodyPositions(nextDelta, true)
            for (const body of this.bodies) {
                body[1].addToPrevSquares()
            }
        }

        const diedIds = delta.diedIdsArray() ?? assert.fail('diedIDsArray not found in round')
        for (let i = 0; i < delta.diedIdsLength(); i++) {
            const diedBody =
                this.bodies.get(diedIds[i]) ?? assert.fail(`Body with id ${delta.diedIds(i)} not found in bodies`)
            if (!turn.stat.completed) {
                const teamStat =
                    turn.stat.getTeamStat(diedBody.team) ?? assert.fail(`team ${i} not found in team stats in turn`)
                teamStat.robots -= 1
                teamStat.total_hp -= diedBody.hp
            }
            diedBody.dead = true
        }
    }

    private insertBodies(bodies: schema.SpawnedBodyTable, stat?: TurnStat): void {
        const teams = bodies.teamIdsArray() ?? assert.fail('Initial body teams not found in header')
        const locs = bodies.locs() ?? assert.fail('Initial body locations not found in header')
        const xsArray = locs.xsArray() ?? assert.fail('Initial body x locations not found in header')
        const ysArray = locs.ysArray() ?? assert.fail('Initial body y locations not found in header')
        const idsArray = bodies.robotIdsArray() ?? assert.fail('Initial body IDs not found in header')

        for (let i = 0; i < bodies.robotIdsLength(); i++) {
            const id = idsArray[i]
            const bodyClass = BODY_DEFINITIONS[0] ?? assert.fail(`Body type ${0} not found in BODY_DEFINITIONS`)
            const health = this.game.playable ? this.game.constants.robotBaseHealth() : 1

            this.bodies.set(
                id,
                new bodyClass({ x: xsArray[i], y: ysArray[i] }, health, this.game.getTeamByID(teams[i]), id)
            )
            if (stat) {
                const teamStat =
                    stat.getTeamStat(this.game.getTeamByID(teams[i])) ??
                    assert.fail(`team ${i} not found in team stats in turn`)
                teamStat.robots += 1
                teamStat.total_hp += health
            }
        }
    }

    getById(id: number): Body {
        return this.bodies.get(id) ?? assert.fail(`Body with id ${id} not found in bodies`)
    }

    hasId(id: number): boolean {
        return this.bodies.has(id)
    }

    copy(): Bodies {
        const newBodies = new Bodies(this.game)
        newBodies.bodies = new Map(this.bodies)
        for (const body of this.bodies.values()) newBodies.bodies.set(body.id, body.copy())

        return newBodies
    }

    draw(match: Match, ctx: CanvasRenderingContext2D): void {
        for (const body of this.bodies.values()) {
            body.draw(match, ctx)
        }
    }

    getNextID(): number {
        return Math.max(-1, ...this.bodies.keys()) + 1
    }

    getBodyAtLocation(x: number, y: number, team?: Team): Body | undefined {
        let found_dead_body: Body | undefined = undefined
        for (const body of this.bodies.values()) {
            if ((!team || body.team === team) && body.pos.x === x && body.pos.y === y) {
                if (body.dead) found_dead_body = body
                else return body
            }
        }
        return found_dead_body
    }

    isEmpty(): boolean {
        return this.bodies.size === 0
    }

    getEditorBrushes(map: StaticMap): MapEditorBrush[] {
        return [new TestDuckBrush(this, map)]
    }

    toSpawnedBodyTable(builder: flatbuffers.Builder): number {
        const robotIDs: Uint8Array = new Uint8Array(this.bodies.size)
        const teamIDs: Uint8Array = new Uint8Array(this.bodies.size)
        const xs: Uint8Array = new Uint8Array(this.bodies.size)
        const ys: Uint8Array = new Uint8Array(this.bodies.size)

        Array.from(this.bodies.values()).forEach((body, i) => {
            robotIDs[i] = body.id
            teamIDs[i] = body.team.id
            xs[i] = body.pos.x
            ys[i] = body.pos.y
        })

        const robotIDsVector = schema.SpawnedBodyTable.createRobotIdsVector(builder, robotIDs)
        const teamIDsVector = schema.SpawnedBodyTable.createTeamIdsVector(builder, teamIDs)

        const xsTable = schema.VecTable.createXsVector(builder, xs)
        const ysTable = schema.VecTable.createYsVector(builder, ys)
        schema.VecTable.startVecTable(builder)
        schema.VecTable.addXs(builder, xsTable)
        schema.VecTable.addYs(builder, ysTable)
        const locsVecTable = schema.VecTable.endVecTable(builder)

        schema.SpawnedBodyTable.startSpawnedBodyTable(builder)
        schema.SpawnedBodyTable.addRobotIds(builder, robotIDsVector)
        schema.SpawnedBodyTable.addTeamIds(builder, teamIDsVector)
        schema.SpawnedBodyTable.addLocs(builder, locsVecTable)
        return schema.SpawnedBodyTable.endSpawnedBodyTable(builder)
    }
}

export class Body {
    public robotName: string = ''
    public actionRadius: number = 0
    public visionRadius: number = 0
    protected imgPath: string = ''
    public nextPos: Vector
    public prevSquares: Vector[]
    public dead: boolean = false
    constructor(
        public pos: Vector,
        public hp: number,
        public readonly team: Team,
        public readonly id: number,
        public healLevel: number = 0,
        public attackLevel: number = 0,
        public buildLevel: number = 0,
        public bytecodesUsed: number = 0
    ) {
        this.nextPos = this.pos
        this.prevSquares = [this.pos]
    }

    public draw(match: Match, ctx: CanvasRenderingContext2D): void {
        const interpCoords = renderUtils.getInterpolatedCoords(this.pos, this.nextPos, match.getInterpolationFactor())
        if (this.dead) ctx.globalAlpha = 0.5
        renderUtils.renderCenteredImageOrLoadingIndicator(
            ctx,
            getImageIfLoaded(this.imgPath),
            renderUtils.getRenderCoords(interpCoords.x, interpCoords.y, match.currentTurn.map.staticMap.dimension),
            1
        )
        ctx.globalAlpha = 1
    }

    public getInterpolatedCoords(turn: Turn): Vector {
        return renderUtils.getInterpolatedCoords(this.pos, this.nextPos, turn.match.getInterpolationFactor())
    }

    public onHoverInfo(): string[] {
        return [
            (this.dead ? 'DEAD: ' : '') + this.robotName,
            `ID: ${this.id}`,
            `Location: (${this.pos.x}, ${this.pos.y})`,
            `Bytecodes Used: ${this.bytecodesUsed}`
        ]
    }

    public copy(): Body {
        // creates a new object using this object's prototype and all its parameters. this is a shallow copy, override this if you need a deep copy
        return Object.create(Object.getPrototypeOf(this), Object.getOwnPropertyDescriptors(this))
    }

    public moveTo(pos: Vector): void {
        this.pos = this.nextPos
        this.nextPos = pos
    }

    public addToPrevSquares(): void {
        this.prevSquares.push(this.pos)
        if (this.prevSquares.length > TOOLTIP_PATH_LENGTH) {
            this.prevSquares.splice(0, 1)
        }
    }
}

export const BODY_DEFINITIONS: Record<number, typeof Body> = {
    // For future games, this dictionary translate schema values of robot
    // types to their respective class, such as this:
    //
    // [schema.BodyType.HEADQUARTERS]: class Headquarters extends Body {
    // 	public robotName = 'Headquarters'
    // 	public actionRadius = 8
    // 	public visionRadius = 34
    // 	public type = schema.BodyType.HEADQUARTERS
    // 	constructor(pos: Vector, hp: number, team: Team, id: number) {
    // 		super(pos, hp, team, id)
    // 		this.imgPath = `robots/${team.color}_headquarters_smaller.png`
    //	}
    //	onHoverInfo(): string[] {
    // 		return super.onHoverInfo();
    // 	}
    // },
    //
    // This game has no types or headquarters to speak of, so there is only
    // one type pointed to by 0:

    0: class Duck extends Body {
        public draw(match: Match, ctx: CanvasRenderingContext2D): void {
            this.imgPath = `robots/${this.team.color.toLowerCase()}/${this.getSpecialization()}_64x64.png`
            super.draw(match, ctx)

            const levelIndicators: [string, number, [number, number]][] = [
                [ATTACK_COLOR, this.attackLevel, [0.8, -0.5]],
                [BUILD_COLOR, this.buildLevel, [0.5, -0.8]],
                [HEAL_COLOR, this.healLevel, [0.2, -0.2]]
            ]
            const interpCoords = renderUtils.getInterpolatedCoords(
                this.pos,
                this.nextPos,
                match.getInterpolationFactor()
            )
            for (const [color, level, [dx, dy]] of levelIndicators) {
                this.drawPetals(match, ctx, color, level, interpCoords.x + dx, interpCoords.y + dy)
            }
        }

        private drawPetals(
            match: Match,
            ctx: CanvasRenderingContext2D,
            color: string,
            level: number,
            x: number,
            y: number
        ): void {
            if (level == 0) return
            // const imgPath = `levels/${image}/${level}_64x64.png`
            // const img = getImageIfLoaded(imgPath)

            const drawCoords = renderUtils.getRenderCoords(x, y, match.currentTurn.map.staticMap.dimension)

            ctx.fillStyle = color
            ctx.strokeStyle = 'black'
            ctx.beginPath()
            ctx.moveTo(drawCoords.x, drawCoords.y)
            for (let i = 0; i < level; i++) {
                const petalWidthRads = (2 * Math.PI) / 12
                const angle = i * petalWidthRads * 2
                const petalLength = 0.15
                ctx.bezierCurveTo(
                    drawCoords.x + ((petalLength * 1) / 3) * Math.cos(angle - petalWidthRads * 2.5),
                    drawCoords.y + ((petalLength * 1) / 3) * Math.sin(angle - petalWidthRads * 2.5),
                    drawCoords.x + ((petalLength * 2) / 3) * Math.cos(angle - (petalWidthRads * 2.5) / 2),
                    drawCoords.y + ((petalLength * 2) / 3) * Math.sin(angle - (petalWidthRads * 2.5) / 2),
                    drawCoords.x + petalLength * Math.cos(angle),
                    drawCoords.y + petalLength * Math.sin(angle)
                )
                ctx.bezierCurveTo(
                    drawCoords.x + ((petalLength * 2) / 3) * Math.cos(angle + (petalWidthRads * 2.5) / 2),
                    drawCoords.y + ((petalLength * 2) / 3) * Math.sin(angle + (petalWidthRads * 2.5) / 2),
                    drawCoords.x + ((petalLength * 1) / 3) * Math.cos(angle + petalWidthRads * 2.5),
                    drawCoords.y + ((petalLength * 1) / 3) * Math.sin(angle + petalWidthRads * 2.5),
                    drawCoords.x,
                    drawCoords.y
                )
            }
            ctx.lineWidth = 0.05
            ctx.globalAlpha = 0.5
            ctx.stroke()
            ctx.globalAlpha = 0.75
            ctx.fill()
            ctx.globalAlpha = 1

            // renderUtils.renderCenteredImageOrLoadingIndicator(
            //     ctx,
            //     img,
            //     renderUtils.getRenderCoords(x, y, match.currentTurn.map.staticMap.dimension),
            //     0.45
            // )
        }

        private getSpecialization(): string {
            assert(this.attackLevel >= 0 && this.attackLevel <= 6, 'Attack level out of bounds')
            assert(this.healLevel >= 0 && this.healLevel <= 6, 'Heal level out of bounds')
            assert(this.buildLevel >= 0 && this.buildLevel <= 6, 'Build level out of bounds')
            assert([this.attackLevel, this.healLevel, this.buildLevel].sort()[1] <= 3, 'Specialization level too high')
            if (this.attackLevel > 3) return 'attack'
            if (this.healLevel > 3) return 'heal'
            if (this.buildLevel > 3) return 'build'
            return 'base'
        }
    }
}

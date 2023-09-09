import { schema } from 'battlecode-schema'
import assert from 'assert'
import Game, { Team } from './Game'
import Turn from './Turn'
import TurnStat from './TurnStat'
import { getImageIfLoaded, loadImage } from '../util/ImageLoader'
import * as renderUtils from '../util/RenderUtil'
import {
    MapEditorBrush,
    MapEditorBrushField,
    MapEditorBrushFieldType
} from '../components/sidebar/map-editor/MapEditorBrush'
import { StaticMap } from './Map'

export default class Bodies {
    public bodies: Map<number, Body> = new Map()

    constructor(
        public readonly game: Game,
        initialBodies?: schema.SpawnedBodyTable,
        initialStats?: TurnStat,
        mapToVerify?: StaticMap
    ) {
        if (initialBodies) this.insertBodies(initialBodies, initialStats)

        if (mapToVerify) {
            for (let i = 0; i < mapToVerify.width * mapToVerify.height; i++) {
                if (mapToVerify.walls[i] == 1 || mapToVerify.initialResources[i] > 0) {
                    for (const body of this.bodies.values()) {
                        if (body.x == i % mapToVerify.width && body.y == Math.floor(i / mapToVerify.width)) {
                            assert.fail(`Body at (${body.x}, ${body.y}) is on top of a wall or resource`)
                        }
                    }
                }
            }
        }
    }

    /**
     * Applies a delta to the bodies array. Because of update order, bodies will first
     * be inserted, followed by a call to scopedCallback() in which all bodies are valid.
     * Afterwards, diedBodies will be deleted, so any methods which reference bodies should
     * most likely be inside scopedCallback()
     */
    applyDelta(turn: Turn, delta: schema.Round, scopedCallback: () => void): void {
        const bodies = delta.spawnedBodies(this.game._bodiesSlot)
        if (bodies) this.insertBodies(bodies, turn.stat.completed ? undefined : turn.stat)

        const movedLocs = delta.movedLocs(this.game._vecTableSlot1)
        if (movedLocs) {
            const movedIds = delta.movedIDsArray() ?? assert.fail('movedIDsArray not found in round')
            const xsArray = movedLocs.xsArray() ?? assert.fail('movedLocs.xsArray not found in round')
            const ysArray = movedLocs.ysArray() ?? assert.fail('movedLocs.ysArray not found in round')
            for (let i = 0; i < delta.movedIDsLength(); i++) {
                const id = movedIds[i]
                const body = this.bodies.get(id) ?? assert.fail(`Moved body ${id} not found in bodies`)
                body.moveTo(xsArray[i], ysArray[i])
            }
        }

        scopedCallback()

        const diedIds = delta.diedIDsArray() ?? assert.fail('diedIDsArray not found in round')
        if (delta.diedIDsLength() > 0) {
            for (let i = 0; i < delta.diedIDsLength(); i++) {
                const diedBody =
                    this.bodies.get(diedIds[i]) ?? assert.fail(`Body with id ${delta.diedIDs(i)} not found in bodies`)
                if (!turn.stat.completed) {
                    const teamStat =
                        turn.stat.getTeamStat(diedBody.team) ?? assert.fail(`team ${i} not found in team stats in turn`)
                    teamStat.robots[diedBody.type] -= 1
                    teamStat.total_hp[diedBody.type] -= diedBody.hp
                }
                assert(this.bodies.delete(diedBody.id))
            }
        }
    }

    insertBodies(bodies: schema.SpawnedBodyTable, stat?: TurnStat): void {
        var teams = bodies.teamIDsArray() ?? assert.fail('Initial body teams not found in header')
        var types = bodies.typesArray() ?? assert.fail('Initial body types not found in header')

        const locs = bodies.locs(this.game._vecTableSlot1) ?? assert.fail('Initial body locations not found in header')
        const xsArray = locs.xsArray() ?? assert.fail('Initial body x locations not found in header')
        const ysArray = locs.ysArray() ?? assert.fail('Initial body y locations not found in header')
        const idsArray = bodies.robotIDsArray() ?? assert.fail('Initial body IDs not found in header')

        for (let i = 0; i < bodies.robotIDsLength(); i++) {
            const id = idsArray[i]
            const bodyClass =
                BODY_DEFINITIONS[types[i]] ?? assert.fail(`Body type ${types[i]} not found in BODY_DEFINITIONS`)
            const health = this.game.playable ? this.game.typeMetadata[types[i]].health() : 1

            this.bodies.set(id, new bodyClass(xsArray[i], ysArray[i], health, this.game.getTeamByID(teams[i]), id))
            if (stat) {
                const teamStat =
                    stat.getTeamStat(this.game.getTeamByID(teams[i])) ??
                    assert.fail(`team ${i} not found in team stats in turn`)
                teamStat.robots[types[i]] += 1
                teamStat.total_hp[types[i]] += health
            }
        }
    }

    getById(id: number): Body {
        return this.bodies.get(id) ?? assert.fail(`Body with id ${id} not found in bodies`)
    }

    copy(): Bodies {
        const newBodies = new Bodies(this.game)
        newBodies.bodies = new Map(this.bodies)
        for (const body of this.bodies.values()) newBodies.bodies.set(body.id, body.copy())

        return newBodies
    }

    draw(turn: Turn, ctx: CanvasRenderingContext2D): void {
        for (const body of this.bodies.values()) body.draw(turn, ctx)
    }

    getNextID(): number {
        return Math.max(-1, ...this.bodies.keys()) + 1
    }

    getBodyAtLocation(x: number, y: number, team?: Team, type?: schema.BodyType): Body | undefined {
        let found_body = undefined
        this.bodies.forEach((body, id) => {
            if (type && body.type !== type) return
            if ((!team || body.team === team) && body.x === x && body.y === y) found_body = body
        })
        return found_body
    }

    isEmpty(): boolean {
        return this.bodies.size === 0
    }

    getEditorBrushes(map: StaticMap): MapEditorBrush[] {
        return [new ArchonBrush(this, map)]
    }

    toSpawnedBodyTable(builder: flatbuffers.Builder): number {
        const robotIDs: Uint8Array = new Uint8Array(this.bodies.size)
        const teamIDs: Uint8Array = new Uint8Array(this.bodies.size)
        const types: schema.BodyType[] = []
        const xs: Uint8Array = new Uint8Array(this.bodies.size)
        const ys: Uint8Array = new Uint8Array(this.bodies.size)

        Array.from(this.bodies.values()).forEach((body, i) => {
            robotIDs[i] = body.id
            teamIDs[i] = body.team.id
            types[i] = body.type
            xs[i] = body.x
            ys[i] = body.y
        })

        const robotIDsVector = schema.SpawnedBodyTable.createRobotIDsVector(builder, robotIDs)
        const teamIDsVector = schema.SpawnedBodyTable.createTeamIDsVector(builder, teamIDs)
        const typesVector = schema.SpawnedBodyTable.createTypesVector(builder, types)

        const xsTable = schema.VecTable.createXsVector(builder, xs)
        const ysTable = schema.VecTable.createYsVector(builder, ys)
        schema.VecTable.startVecTable(builder)
        schema.VecTable.addXs(builder, xsTable)
        schema.VecTable.addYs(builder, ysTable)
        const locsVecTable = schema.VecTable.endVecTable(builder)

        schema.SpawnedBodyTable.startSpawnedBodyTable(builder)
        schema.SpawnedBodyTable.addRobotIDs(builder, robotIDsVector)
        schema.SpawnedBodyTable.addTeamIDs(builder, teamIDsVector)
        schema.SpawnedBodyTable.addTypes(builder, typesVector)
        schema.SpawnedBodyTable.addLocs(builder, locsVecTable)
        return schema.SpawnedBodyTable.endSpawnedBodyTable(builder)
    }
}

export class Body {
    static robotName: string
    public type: schema.BodyType = 0 //this is dumb, maybe should figure out how to make this an abstract field
    protected imgPath: string = ''
    constructor(
        public x: number,
        public y: number,
        public hp: number,
        public readonly team: Team,
        public readonly id: number,
        public adamantium: number = 0,
        public elixir: number = 0,
        public mana: number = 0,
        public anchor: number = 0,
        public bytecodesUsed: number = 0
    ) {}

    public draw(turn: Turn, ctx: CanvasRenderingContext2D): void {
        renderUtils.renderCenteredImageOrLoadingIndicator(
            ctx,
            getImageIfLoaded(this.imgPath),
            renderUtils.getRenderCoords(this.x, this.y, turn.map.staticMap.dimension),
            1
        )
    }

    public onHoverInfo(): string {
        return Object.getPrototypeOf(this).constructor.robotName
    }

    public copy(): Body {
        // creates a new object using this object's prototype and all its parameters. this is a shallow copy, override this if you need a deep copy
        return Object.create(Object.getPrototypeOf(this), Object.getOwnPropertyDescriptors(this))
    }

    public moveTo(x: number, y: number): void {
        this.x = x
        this.y = y
    }

    public clearResources(): void {
        this.adamantium = 0
        this.elixir = 0
        this.mana = 0
        this.anchor = 0
    }
}

export const BODY_DEFINITIONS: Record<number, typeof Body> = {
    [schema.BodyType.HEADQUARTERS]: class Headquarters extends Body {
        static robotName = 'Headquarters'
        public type = schema.BodyType.HEADQUARTERS
        constructor(x: number, y: number, hp: number, team: Team, id: number) {
            super(x, y, hp, team, id)
            this.imgPath = `robots/${team.color}_headquarters_smaller.png`
        }
        onHoverInfo(): string {
            return 'Headquarters'
        }
    },
    [schema.BodyType.LAUNCHER]: class Launcher extends Body {
        static robotName = 'Launcher'
        public type = schema.BodyType.LAUNCHER
        constructor(x: number, y: number, hp: number, team: Team, id: number) {
            super(x, y, hp, team, id)
            this.imgPath = `robots/${team.color}_launcher_smaller.png`
        }
        onHoverInfo(): string {
            return Launcher.robotName
        }
    },
    [schema.BodyType.CARRIER]: class Carrier extends Body {
        static robotName = 'Carrier'
        public type = schema.BodyType.CARRIER
        constructor(x: number, y: number, hp: number, team: Team, id: number) {
            super(x, y, hp, team, id)
            this.imgPath = `robots/${team.color}_carrier_smaller.png`
        }
        onHoverInfo(): string {
            return 'Carrier'
        }
    },
    [schema.BodyType.BOOSTER]: class Booster extends Body {
        static robotName = 'Booster'
        public type = schema.BodyType.BOOSTER
        constructor(x: number, y: number, hp: number, team: Team, id: number) {
            super(x, y, hp, team, id)
            this.imgPath = `robots/${team.color}_booster_smaller.png`
        }
        onHoverInfo(): string {
            return Booster.robotName
        }
    },
    [schema.BodyType.DESTABILIZER]: class Destabilizer extends Body {
        static robotName = 'Destabilizer'
        public type = schema.BodyType.DESTABILIZER
        constructor(x: number, y: number, hp: number, team: Team, id: number) {
            super(x, y, hp, team, id)
            this.imgPath = `robots/${team.color}_destabilizer_smaller.png`
        }
        onHoverInfo(): string {
            return Destabilizer.robotName
        }
    },
    [schema.BodyType.AMPLIFIER]: class Amplifier extends Body {
        static robotName = 'Amplifier'
        public type = schema.BodyType.AMPLIFIER
        constructor(x: number, y: number, hp: number, team: Team, id: number) {
            super(x, y, hp, team, id)
            this.imgPath = `robots/${team.color}_amplifier_smaller.png`
        }
        onHoverInfo(): string {
            return Amplifier.robotName
        }
    }
}

export class ArchonBrush extends MapEditorBrush {
    public readonly name = 'Archons'
    public readonly fields = {
        is_archon: {
            type: MapEditorBrushFieldType.ADD_REMOVE,
            value: true
        },
        team: {
            type: MapEditorBrushFieldType.TEAM,
            value: 0
        }
    }

    constructor(private readonly bodies: Bodies, private readonly map: StaticMap) {
        super()
    }

    public apply(x: number, y: number, fields: Record<string, MapEditorBrushField>) {
        const symmetryPoint = this.map.applySymmetry({ x: x, y: y })
        if (symmetryPoint.x == x && symmetryPoint.y == y) return // dont allow the case where the archon is on the symmetry line

        const is_archon: boolean = fields.is_archon.value

        if (is_archon) {
            if (this.bodies.getBodyAtLocation(x, y)) return

            const team = this.bodies.game.teams[fields.team.value]
            const otherTeam = this.bodies.game.teams[(fields.team.value + 1) % 2]

            const archonClass = BODY_DEFINITIONS[schema.BodyType.HEADQUARTERS]
            const archon = new archonClass(x, y, 1, team, this.bodies.getNextID())
            this.bodies.bodies.set(archon.id, archon)
            const otherArchon = new archonClass(symmetryPoint.x, symmetryPoint.y, 1, otherTeam, this.bodies.getNextID())
            this.bodies.bodies.set(otherArchon.id, otherArchon)
        } else {
            let archon = this.bodies.getBodyAtLocation(x, y, undefined, schema.BodyType.HEADQUARTERS)
            let otherArchon = this.bodies.getBodyAtLocation(
                symmetryPoint.x,
                symmetryPoint.y,
                undefined,
                schema.BodyType.HEADQUARTERS
            )
            if (archon || otherArchon) {
                assert(archon && otherArchon, 'Archon and otherArchon should both be defined or both be undefined')
                this.bodies.bodies.delete(archon.id)
                this.bodies.bodies.delete(otherArchon.id)
            }
        }
    }
}

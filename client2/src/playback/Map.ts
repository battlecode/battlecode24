import { schema } from 'battlecode-schema'
import assert from 'assert'
import { Vector } from './Vector'
import * as cst from '../constants'
import * as renderUtils from '../util/RenderUtil'
import { getImageIfLoaded } from '../util/ImageLoader'
import {
    MapEditorBrush,
    MapEditorBrushField,
    MapEditorBrushFieldType,
    SymmetricMapEditorBrush,
    Symmetry
} from '../components/sidebar/map-editor/MapEditorBrush'

export type Dimension = {
    minCorner: Vector
    maxCorner: Vector
    width: number
    height: number
}

type ResourceWellStat = {
    adamantium: number
    mana: number
    elixir: number
    upgraded: boolean
}

type IslandStat = {
    owner: number
    flip_progress: number
    locations: number[]
    is_accelerated: boolean
    id: number
}

export class CurrentMap {
    public readonly staticMap: StaticMap
    public readonly resource_well_stats: Map<number, ResourceWellStat>
    public readonly island_stats: Map<number, IslandStat>
    public readonly resources: Int8Array

    get width(): number {
        return this.staticMap.dimension.width
    }
    get height(): number {
        return this.staticMap.dimension.height
    }

    constructor(from: StaticMap | CurrentMap) {
        if (from instanceof StaticMap) {
            // create current map from static map
            this.staticMap = from
            this.resources = from.initialResources
            this.resource_well_stats = new Map()
            for (let i = 0; i < this.resources.length; i++) {
                if (this.resources[i] != 0) {
                    this.resource_well_stats.set(i, {
                        adamantium: 0,
                        mana: 0,
                        elixir: 0,
                        upgraded: false
                    })
                }
            }
            this.island_stats = new Map()
            for (let i = 0; i < this.staticMap.islands.length; i++) {
                if (this.staticMap.islands[i] != 0) {
                    let island_id = this.staticMap.islands[i]
                    if (this.island_stats.has(island_id)) {
                        this.island_stats.get(island_id)!.locations.push(i)
                    } else {
                        this.island_stats.set(island_id, {
                            owner: 0,
                            flip_progress: 0,
                            locations: [i],
                            is_accelerated: false,
                            id: island_id
                        })
                    }
                }
            }
        } else {
            // create current map from current map (copy)
            this.resource_well_stats = new Map(from.resource_well_stats)
            for (let [key, value] of this.resource_well_stats) this.resource_well_stats.set(key, { ...value })

            this.island_stats = new Map(from.island_stats)
            for (let [key, value] of this.island_stats) this.island_stats.set(key, { ...value })

            this.staticMap = from.staticMap
            this.resources = new Int8Array(from.resources)
        }
    }

    indexToLocation(index: number): { x: number; y: number } {
        return this.staticMap.indexToLocation(index)
    }

    locationToIndex(x: number, y: number): number {
        return this.staticMap.locationToIndex(x, y)
    }

    copy(): CurrentMap {
        return new CurrentMap(this)
    }

    /**
     * Mutates this currentMap to reflect the given delta.
     */
    applyDelta(delta: schema.Round): void {
        for (let i = 0; i < delta.resourceWellLocsLength(); i++) {
            const well_index = delta.resourceWellLocs(i) ?? assert.fail(`resource well loc at ${i} not found`)
            this.resources[well_index] = delta.resourceID(i) ?? assert.fail(`resource id at ${i} not found`)

            const current_resource_stats =
                this.resource_well_stats.get(well_index) ??
                assert.fail(`resource well stats at ${well_index} not found`)
            current_resource_stats.adamantium =
                delta.wellAdamantiumValues(i) ?? assert.fail(`resource adamantium at ${i} not found`)
            current_resource_stats.mana = delta.wellManaValues(i) ?? assert.fail(`resource mana at ${i} not found`)
            current_resource_stats.elixir =
                delta.wellElixirValues(i) ?? assert.fail(`resource elixir at ${i} not found`)
            current_resource_stats.upgraded =
                (delta.wellAccelerationID(i) ?? assert.fail(`resource acceleration id at ${i} not found`)) > 0
        }

        for (let i = 0; i < delta.islandIDsLength(); i++) {
            const id = delta.islandIDs(i) ?? assert.fail(`island id at ${i} not found`)
            const owner = delta.islandOwnership(i) ?? assert.fail(`island ownership at ${i} not found`)
            const turnover = delta.islandTurnoverTurns(i) ?? assert.fail(`island turnover turns at ${i} not found`)
            const island_stats = this.island_stats.get(id) ?? assert.fail(`island stats at ${id} not found`)
            island_stats.flip_progress = turnover
            if (island_stats.owner != owner) {
                island_stats.is_accelerated = false
            }
            island_stats.owner = owner
        }
    }

    private renderIsland(ctx: CanvasRenderingContext2D, i: number, j: number, islandStat: IslandStat) {
        renderUtils.renderRounded(ctx, i, j, this.staticMap.dimension, this.staticMap.islands, (scale) => {
            ctx.globalAlpha = 0.7

            const sigmoid = (x: number) => {
                return 1 / (1 + Math.exp(-x))
            }
            const blendColors = (colorA: string, colorB: string, amount: number) => {
                const [rA, gA, bA] = colorA.match(/\w\w/g)!.map((c: string) => parseInt(c, 16))
                const [rB, gB, bB] = colorB.match(/\w\w/g)!.map((c: string) => parseInt(c, 16))
                const r = Math.round(rA + (rB - rA) * amount)
                    .toString(16)
                    .padStart(2, '0')
                const g = Math.round(gA + (gB - gA) * amount)
                    .toString(16)
                    .padStart(2, '0')
                const b = Math.round(bA + (bB - bA) * amount)
                    .toString(16)
                    .padStart(2, '0')
                return '#' + r + g + b
            }

            let first_color = '#666666'
            if (islandStat.owner != 0)
                first_color = blendColors(
                    first_color,
                    cst.TEAM_COLORS[islandStat.owner - 1],
                    Math.min(1, sigmoid(islandStat.flip_progress / 15 - 2) + 0.3)
                )

            let second_color = islandStat.is_accelerated ? '#EEAC09' : first_color

            const coords = renderUtils.getRenderCoords(i, j, this.staticMap.dimension)
            const x = coords.x
            const y = coords.y
            let d = scale / 8

            ctx.fillStyle = first_color
            ctx.beginPath()
            ctx.moveTo(x, y)
            ctx.lineTo(x + d, y)
            ctx.lineTo(x, y + d)
            ctx.closePath()
            ctx.fill()

            ctx.fillStyle = second_color
            ctx.beginPath()
            ctx.moveTo(x + 3 * d, y)
            ctx.lineTo(x + 5 * d, y)
            ctx.lineTo(x, y + 5 * d)
            ctx.lineTo(x, y + 3 * d)
            ctx.closePath()
            ctx.fill()

            ctx.fillStyle = first_color
            ctx.beginPath()
            ctx.moveTo(x + 7 * d, y)
            ctx.lineTo(x + 8 * d, y)
            ctx.lineTo(x + 8 * d, y + d)
            ctx.lineTo(x + d, y + 8 * d)
            ctx.lineTo(x, y + 8 * d)
            ctx.lineTo(x, y + 7 * d)
            ctx.closePath()
            ctx.fill()

            ctx.fillStyle = second_color
            ctx.beginPath()
            ctx.moveTo(x + 5 * d, y + 8 * d)
            ctx.lineTo(x + 3 * d, y + 8 * d)
            ctx.lineTo(x + 8 * d, y + 3 * d)
            ctx.lineTo(x + 8 * d, y + 5 * d)
            ctx.closePath()
            ctx.fill()

            ctx.fillStyle = first_color
            ctx.beginPath()
            ctx.moveTo(x + 8 * d, y + 8 * d)
            ctx.lineTo(x + 7 * d, y + 8 * d)
            ctx.lineTo(x + 8 * d, y + 7 * d)
            ctx.closePath()
            ctx.fill()
        })
    }

    draw(ctx: CanvasRenderingContext2D) {
        const dimension = this.staticMap.dimension
        for (let i = 0; i < dimension.width; i++) {
            for (let j = 0; j < dimension.height; j++) {
                const schemaIdx = renderUtils.getSchemaIdx(i, j, dimension)
                const coords = renderUtils.getRenderCoords(i, j, dimension)

                // Render island
                if (this.staticMap.islands[schemaIdx]) {
                    const islandStat = this.island_stats.get(this.staticMap.islands[schemaIdx])!
                    this.renderIsland(ctx, i, j, islandStat)
                }

                // Render resource
                if (this.resources[schemaIdx] > 0) {
                    // Main image
                    const upgraded = this.resource_well_stats.get(schemaIdx)!.upgraded
                    const resource = this.resources[schemaIdx]
                    const size = upgraded ? 0.95 : 0.85
                    const resourcename = cst.RESOURCE_NAMES[resource]
                    const img = getImageIfLoaded(
                        `resources/${resourcename}_well_${upgraded ? 'upgraded_' : ''}smaller.png`
                    )
                    renderUtils.renderCenteredImageOrLoadingIndicator(ctx, img, coords, size)
                }
            }
        }
    }

    getEditorBrushes() {
        const brushes: MapEditorBrush[] = [new ResourcesBrush(this)]
        return brushes.concat(this.staticMap.getEditorBrushes())
    }

    isEmpty(): boolean {
        return this.resource_well_stats.size == 0 && this.island_stats.size == 0 && this.staticMap.isEmpty()
    }

    /**
     * Creates a packet of flatbuffers data which will later be inserted
     * This and the next function are seperated due to how flatbuffers works
     */
    getSchemaPacket(builder: flatbuffers.Builder): [number, number, number, number, number] {
        const walls = schema.GameMap.createWallsVector(
            builder,
            Array.from(this.staticMap.walls).map((x) => !!x)
        )
        const resources = schema.GameMap.createResourcesVector(builder, Uint8Array.from(this.resources))
        const clouds = schema.GameMap.createCloudsVector(
            builder,
            Array.from(this.staticMap.clouds).map((x) => !!x)
        )
        const currents = schema.GameMap.createCurrentsVector(builder, Uint8Array.from(this.staticMap.currents))
        const islands = schema.GameMap.createIslandsVector(builder, Uint8Array.from(this.staticMap.islands))
        return [walls, resources, clouds, currents, islands]
    }

    /**
     * Inserts an existing packet of flatbuffers data into the given builder
     * This and the previous function are seperated due to how flatbuffers works
     */
    insertSchemaPacket(builder: flatbuffers.Builder, packet: [number, number, number, number, number]) {
        schema.GameMap.addWalls(builder, packet[0])
        schema.GameMap.addResources(builder, packet[1])
        schema.GameMap.addClouds(builder, packet[2])
        schema.GameMap.addCurrents(builder, packet[3])
        schema.GameMap.addIslands(builder, packet[4])
    }
}

export class StaticMap {
    constructor(
        public name: string,
        public readonly randomSeed: number, // I dont know what this is for
        public readonly symmetry: number,
        public readonly dimension: Dimension,
        public readonly walls: Int8Array,
        public readonly clouds: Int8Array,
        public readonly currents: Int8Array,
        public readonly initialResources: Int8Array,
        public readonly islands: Int32Array
    ) {
        if (symmetry < 0 || symmetry > 2 || !Number.isInteger(symmetry)) throw new Error(`Invalid symmetry ${symmetry}`)
    }

    static fromSchema(schemaMap: schema.GameMap) {
        const name = schemaMap.name() as string
        const randomSeed = schemaMap.randomSeed()
        const symmetry = schemaMap.symmetry()

        assert(schemaMap.minCorner(), 'minCorner() is missing')
        assert(schemaMap.maxCorner(), 'maxCorner() is missing')
        const minCorner = { x: schemaMap.minCorner()!.x(), y: schemaMap.minCorner()!.y() }
        const maxCorner = { x: schemaMap.maxCorner()!.x(), y: schemaMap.maxCorner()!.y() }
        const dimension = {
            minCorner,
            maxCorner,
            width: maxCorner.x - minCorner.x,
            height: maxCorner.y - minCorner.y
        }

        const walls = schemaMap.wallsArray() ?? assert.fail('wallsArray() is null')
        const clouds = schemaMap.cloudsArray() ?? assert.fail('cloudsArray() is null')
        const currents = Int8Array.from(schemaMap.currentsArray() ?? assert.fail('currentsArray() is null'))
        const resources = Int8Array.from(schemaMap.resourcesArray() ?? assert.fail('resourcesArray() is null'))
        const islands = schemaMap.islandsArray() ?? assert.fail('islandsArray() is null')
        return new StaticMap(name, randomSeed, symmetry, dimension, walls, clouds, currents, resources, islands)
    }

    static fromParams(width: number, height: number, symmetry: Symmetry) {
        const name = 'Custom Map'
        const randomSeed = 0

        const minCorner = { x: 0, y: 0 }
        const maxCorner = { x: width, y: height }
        const dimension = {
            minCorner,
            maxCorner,
            width: maxCorner.x - minCorner.x,
            height: maxCorner.y - minCorner.y
        }

        const walls = new Int8Array(width * height)
        const clouds = new Int8Array(width * height)
        const currents = new Int8Array(width * height)
        const resources = new Int8Array(width * height)
        const islands = new Int32Array(width * height)
        return new StaticMap(name, randomSeed, symmetry, dimension, walls, clouds, currents, resources, islands)
    }

    get width(): number {
        return this.dimension.width
    }
    get height(): number {
        return this.dimension.height
    }

    indexToLocation(index: number): { x: number; y: number } {
        const target_x = index % this.width
        const target_y = (index - target_x) / this.width
        assert(target_x >= 0 && target_x < this.width, `target_x ${target_x} out of bounds`)
        assert(target_y >= 0 && target_y < this.height, `target_y ${target_y} out of bounds`)
        return { x: target_x, y: target_y }
    }

    locationToIndex(x: number, y: number): number {
        assert(x >= 0 && x < this.width, `x ${x} out of bounds`)
        assert(y >= 0 && y < this.height, `y ${y} out of bounds`)
        return y * this.width + x
    }

    /**
     * Returns a point representing the reflection of the given point following the map's symmetry.
     */
    applySymmetry(point: { x: number; y: number }): { x: number; y: number } {
        switch (this.symmetry) {
            case Symmetry.VERTICAL:
                return { x: this.width - point.x - 1, y: point.y }
            case Symmetry.HORIZONTAL:
                return { x: point.x, y: this.height - point.y - 1 }
            case Symmetry.ROTATIONAL:
                return { x: this.width - point.x - 1, y: this.height - point.y - 1 }
            default:
                throw new Error(`Invalid symmetry ${this.symmetry}`)
        }
    }

    draw(ctx: CanvasRenderingContext2D) {
        // Fill background
        ctx.fillStyle = '#BAAD99'
        ctx.fillRect(
            this.dimension.minCorner.x,
            this.dimension.minCorner.y,
            this.dimension.width,
            this.dimension.height
        )

        for (let i = 0; i < this.dimension.width; i++) {
            for (let j = 0; j < this.dimension.height; j++) {
                const schemaIdx = renderUtils.getSchemaIdx(i, j, this.dimension)
                const coords = renderUtils.getRenderCoords(i, j, this.dimension)

                // Render rounded (clipped) wall
                if (this.walls[schemaIdx]) {
                    renderUtils.renderRounded(ctx, i, j, this.dimension, this.walls, (scale) => {
                        ctx.fillStyle = '#333333'
                        ctx.fillRect(coords.x, coords.y, scale, scale)
                    })
                }

                // Render cloud
                if (this.clouds[schemaIdx]) {
                    renderUtils.renderRounded(ctx, i, j, this.dimension, this.clouds, (scale) => {
                        ctx.fillStyle = 'white'
                        ctx.globalAlpha = 0.3
                        ctx.fillRect(coords.x, coords.y, scale, scale)
                    })
                }

                // Render current
                if (this.currents[schemaIdx]) {
                    renderUtils.renderRounded(
                        ctx,
                        i,
                        j,
                        this.dimension,
                        this.currents,
                        (scale) => {
                            ctx.fillStyle = 'purple'
                            ctx.globalAlpha = 0.2
                            ctx.fillRect(coords.x, coords.y, scale, scale)
                            ctx.globalAlpha = 0.1
                            ctx.fillStyle = 'black'
                            renderUtils.renderTileArrow(ctx, coords, this.currents[schemaIdx])
                        },
                        1.01,
                        (c) => c == this.currents[schemaIdx]
                    )
                }

                // Draw grid
                const showGrid = true
                if (showGrid) {
                    const thickness = 0.02
                    renderUtils.applyStyles(
                        ctx,
                        { strokeStyle: 'black', lineWidth: thickness, globalAlpha: 0.05 },
                        () => {
                            ctx.strokeRect(
                                coords.x + thickness / 2,
                                coords.y + thickness / 2,
                                1 - thickness,
                                1 - thickness
                            )
                        }
                    )
                }
            }
        }
    }

    isEmpty(): boolean {
        return (
            this.walls.every((x) => x == 0) &&
            this.clouds.every((x) => x == 0) &&
            this.currents.every((x) => x == 0) &&
            this.initialResources.every((x) => x == 0) &&
            this.islands.every((x) => x == 0)
        )
    }


    getEditorBrushes(): MapEditorBrush[] {
        return [new WallsBrush(this), new CloudsBrush(this), new CurrentsBrush(this)]
    }
}

class ResourcesBrush extends SymmetricMapEditorBrush {
    public readonly name = 'Resources'
    public readonly fields = {
        is_resource: {
            type: MapEditorBrushFieldType.ADD_REMOVE,
            value: true
        },
        resource: {
            type: MapEditorBrushFieldType.SINGLE_SELECT,
            value: 1,
            label: 'Resource',
            options: Object.entries(cst.RESOURCE_NAMES).map(([index, label]) => ({
                value: parseInt(index),
                label
            }))
        }
    }

    constructor(private readonly currentMap: CurrentMap) {
        super(currentMap.staticMap)
    }

    public symmetricApply(x: number, y: number, fields: Record<string, MapEditorBrushField>) {
        const target_idx = this.map.locationToIndex(x, y)
        const is_resource: boolean = fields.is_resource.value
        const resource: number = fields.resource.value
        const resource_val = is_resource ? resource : 0
        if (this.currentMap.resources[target_idx] != resource_val) {
            this.currentMap.resources[target_idx] = resource_val
            if (is_resource) {
                this.currentMap.resource_well_stats.set(target_idx, {
                    adamantium: 0,
                    mana: 0,
                    elixir: 0,
                    upgraded: false
                })
            } else {
                this.currentMap.resource_well_stats.delete(target_idx)
            }
        }
    }
}

class WallsBrush extends SymmetricMapEditorBrush {
    public readonly name = 'Walls'
    public readonly fields = {
        is_wall: {
            type: MapEditorBrushFieldType.ADD_REMOVE,
            value: true
        },
        radius: {
            type: MapEditorBrushFieldType.POSITIVE_INTEGER,
            value: 1,
            label: 'Radius'
        }
    }

    constructor(map: StaticMap) {
        super(map)
    }

    public symmetricApply(x: number, y: number, fields: Record<string, MapEditorBrushField>) {
        const radius: number = fields.radius.value - 1
        for (let i = -radius; i <= radius; i++) {
            for (let j = -radius; j <= radius; j++) {
                if (Math.sqrt(i * i + j * j) <= radius) {
                    const target_x = x + i
                    const target_y = y + j
                    if (target_x >= 0 && target_x < this.map.width && target_y >= 0 && target_y < this.map.height) {
                        const target_idx = this.map.locationToIndex(target_x, target_y)
                        const is_wall: boolean = fields.is_wall.value
                        this.map.walls[target_idx] = is_wall ? 1 : 0
                    }
                }
            }
        }
    }
}

class CloudsBrush extends SymmetricMapEditorBrush {
    public readonly name = 'Clouds'
    public readonly fields = {
        is_cloud: {
            type: MapEditorBrushFieldType.ADD_REMOVE,
            value: true
        },
        radius: {
            type: MapEditorBrushFieldType.POSITIVE_INTEGER,
            value: 1,
            label: 'Radius'
        }
    }

    constructor(map: StaticMap) {
        super(map)
    }

    public symmetricApply(x: number, y: number, fields: Record<string, MapEditorBrushField>) {
        const target_idx = this.map.locationToIndex(x, y)
        const is_cloud: boolean = fields.is_cloud.value
        this.map.clouds[target_idx] = is_cloud ? 1 : 0
    }
}

class CurrentsBrush extends SymmetricMapEditorBrush {
    public readonly name = 'Currents'
    public readonly fields = {
        is_current: {
            type: MapEditorBrushFieldType.ADD_REMOVE,
            value: true
        },
        direction: {
            type: MapEditorBrushFieldType.POSITIVE_INTEGER,
            value: 1,
            label: 'Direction'
        }
    }

    constructor(map: StaticMap) {
        super(map)
    }

    public symmetricApply(x: number, y: number, fields: Record<string, MapEditorBrushField>) {
        const target_idx = this.map.locationToIndex(x, y)
        const is_current: boolean = fields.is_current.value
        const direction: number = fields.direction.value
        this.map.currents[target_idx] = is_current ? direction : 0
    }
}

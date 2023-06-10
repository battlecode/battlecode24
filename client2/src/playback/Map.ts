import { schema } from 'battlecode-schema'
import assert from 'assert'
import { Vector } from './Vector'
import * as cst from '../constants'
import * as renderUtils from '../util/RenderUtil'

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
            this.resources = from.resources
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
            for (let [key, value] of this.resource_well_stats)
                this.resource_well_stats.set(key, { ...value })

            this.island_stats = new Map(from.island_stats)
            for (let [key, value] of this.island_stats) this.island_stats.set(key, { ...value })

            this.staticMap = from.staticMap
            this.resources = new Int8Array(from.resources)
        }
    }

    indexToLocation(index: number): { x: number; y: number } {
        const target_x = index % this.width
        const target_y = (index - target_x) / this.width
        assert(target_x >= 0 && target_x < this.width, `target_x ${target_x} out of bounds`)
        assert(target_y >= 0 && target_y < this.height, `target_y ${target_y} out of bounds`)
        return { x: target_x, y: target_y }
    }

    copy(): CurrentMap {
        return new CurrentMap(this)
    }

    /**
     * Mutates this currentMap to reflect the given delta.
     */
    applyDelta(delta: schema.Round): void {
        for (let i = 0; i < delta.resourceWellLocsLength(); i++) {
            const well_index =
                delta.resourceWellLocs(i) ?? assert.fail(`resource well loc at ${i} not found`)
            this.resources[well_index] =
                delta.resourceID(i) ?? assert.fail(`resource id at ${i} not found`)

            const current_resource_stats =
                this.resource_well_stats.get(well_index) ??
                assert.fail(`resource well stats at ${well_index} not found`)
            current_resource_stats.adamantium =
                delta.wellAdamantiumValues(i) ??
                assert.fail(`resource adamantium at ${i} not found`)
            current_resource_stats.mana =
                delta.wellManaValues(i) ?? assert.fail(`resource mana at ${i} not found`)
            current_resource_stats.elixir =
                delta.wellElixirValues(i) ?? assert.fail(`resource elixir at ${i} not found`)
            current_resource_stats.upgraded =
                (delta.wellAccelerationID(i) ??
                    assert.fail(`resource acceleration id at ${i} not found`)) > 0
        }

        for (let i = 0; i < delta.islandIDsLength(); i++) {
            const id = delta.islandIDs(i) ?? assert.fail(`island id at ${i} not found`)
            const owner =
                delta.islandOwnership(i) ?? assert.fail(`island ownership at ${i} not found`)
            const turnover =
                delta.islandTurnoverTurns(i) ??
                assert.fail(`island turnover turns at ${i} not found`)
            const island_stats =
                this.island_stats.get(id) ?? assert.fail(`island stats at ${id} not found`)
            island_stats.flip_progress = turnover
            if (island_stats.owner != owner) {
                island_stats.is_accelerated = false
            }
            island_stats.owner = owner
        }
    }

    private renderIsland(
        ctx: CanvasRenderingContext2D,
        i: number,
        j: number,
        islandStat: IslandStat
    ) {
        renderUtils.renderRounded(
            ctx,
            i,
            j,
            this.staticMap.dimension,
            this.staticMap.islands,
            (scale) => {
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
            }
        )
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
                if (this.staticMap.resources[schemaIdx] > 0) {
                    // Main image
                    const upgraded = this.resource_well_stats.get(schemaIdx)!.upgraded
                    const size = upgraded ? 0.95 : 0.85
                    const img = null //this.imgs.resource_wells[map.resources[idxVal]][upgraded ? 1 : 0]
                    ctx.fillStyle = 'red'
                    ctx.fillRect(coords.x, coords.y, size, size)
                    //renderUtils.renderCenteredImage(ctx, img, coords, size)
                }
            }
        }
    }
}

export class StaticMap {
    public readonly name: string
    public readonly randomSeed: number
    public readonly symmetry: number
    public readonly dimension: Dimension

    public readonly walls: Int8Array
    public readonly clouds: Int8Array
    public readonly currents: Int8Array
    public readonly resources: Int8Array
    public readonly islands: Int32Array

    constructor(map: schema.GameMap) {
        this.name = map.name() as string
        this.randomSeed = map.randomSeed()
        this.symmetry = map.symmetry()

        const minCorner = { x: map.minCorner()!.x(), y: map.minCorner()!.y() }
        const maxCorner = { x: map.maxCorner()!.x(), y: map.maxCorner()!.y() }
        this.dimension = {
            minCorner,
            maxCorner,
            width: maxCorner.x - minCorner.x,
            height: maxCorner.y - minCorner.y
        }

        this.walls = map.wallsArray() ?? assert.fail('wallsArray() is null')
        this.clouds = map.cloudsArray() ?? assert.fail('cloudsArray() is null')
        this.currents = Int8Array.from(
            map.currentsArray() ?? assert.fail('currentsArray() is null')
        )
        this.resources = Int8Array.from(
            map.resourcesArray() ?? assert.fail('resourcesArray() is null')
        )
        this.islands = map.islandsArray() ?? assert.fail('islandsArray() is null')
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
}

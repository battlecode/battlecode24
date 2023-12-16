import { flatbuffers, schema } from 'battlecode-schema'
import assert from 'assert'
import { Vector, getEmptyVector } from './Vector'
import Match from './Match'
import { MapEditorBrush, Symmetry } from '../components/sidebar/map-editor/MapEditorBrush'
import { packVecTable, parseVecTable } from './SchemaHelpers'
import { DividerBrush, ResourcePileBrush, SpawnZoneBrush, TestTrapBrush, WallsBrush, WaterBrush } from './Brushes'
import {
    DIVIDER_COLOR,
    GRASS_COLOR,
    WALLS_COLOR,
    WATER_COLOR,
    TEAM_COLORS,
    BUILD_NAMES,
    TEAM_COLOR_NAMES,
    DIVIDER_DROP_TURN,
    INDICATOR_LINE_WIDTH,
    INDICATOR_DOT_SIZE
} from '../constants'
import * as renderUtils from '../util/RenderUtil'
import { getImageIfLoaded } from '../util/ImageLoader'
import { Body } from './Bodies'
import { ClientConfig } from '../client-config';

export type Dimension = {
    minCorner: Vector
    maxCorner: Vector
    width: number
    height: number
}

type ResourcePileData = {
    amount: number
}

type TrapData = {
    location: Vector
    type: schema.BuildActionType
    team: number
}

type FlagData = {
    team: number
    location: Vector
    carrierId: number | null
}

type SchemaPacket = {
    wallsOffset: number
    waterOffset: number
    dividerOffset: number
    spawnLocationOffset: number
    resourcePileOffset: number
    resourcePileAmountOffset: number
}

type IndicatorDotData = {
    id: number
    location: Vector
    color: string
}

type IndicatorLineData = {
    id: number
    start: Vector
    end: Vector
    color: string
}

export class CurrentMap {
    public readonly staticMap: StaticMap
    public readonly resourcePileData: Map<number, ResourcePileData>
    public readonly trapData: Map<number, TrapData>
    public readonly flagData: Map<number, FlagData>
    public readonly water: Int8Array
    private indicatorDotData: IndicatorDotData[] = []
    private indicatorLineData: IndicatorLineData[] = []

    get width(): number {
        return this.dimension.width
    }
    get height(): number {
        return this.dimension.height
    }
    get dimension(): Dimension {
        return this.staticMap.dimension
    }

    constructor(from: StaticMap | CurrentMap) {
        this.resourcePileData = new Map()
        this.trapData = new Map()
        this.flagData = new Map()
        if (from instanceof StaticMap) {
            // Create current map from static map

            this.staticMap = from
            this.trapData = new Map()
            this.water = new Int8Array(from.initialWater)
            for (let i = 0; i < from.initialResourcePileAmounts.length; i++) {
                const id = this.locationToIndex(from.resourcePileLocations[i].x, from.resourcePileLocations[i].y)
                this.resourcePileData.set(id, { amount: from.initialResourcePileAmounts[i] })
            }
            for (let i = 0; i < from.spawnLocations.length; i++) {
                // Assign initial flag data, ids are same order as spawn zones
                const team = i % 2
                const location = from.spawnLocations[i]
                this.flagData.set(i, { team, location, carrierId: null })
            }
        } else {
            // Create current map from current map (copy)

            this.staticMap = from.staticMap
            for (let [key, value] of from.resourcePileData) {
                this.resourcePileData.set(key, { ...value })
            }
            for (let [key, value] of from.trapData) {
                this.trapData.set(key, { ...value })
            }
            for (let [key, value] of from.flagData) {
                this.flagData.set(key, { ...value })
            }
            this.water = new Int8Array(from.water)
            this.indicatorDotData = [...from.indicatorDotData]
            this.indicatorLineData = [...from.indicatorLineData]
        }
    }

    indexToLocation(index: number): { x: number; y: number } {
        return this.staticMap.indexToLocation(index)
    }

    locationToIndex(x: number, y: number): number {
        return this.staticMap.locationToIndex(x, y)
    }

    applySymmetry(point: Vector): Vector {
        return this.staticMap.applySymmetry(point)
    }

    copy(): CurrentMap {
        return new CurrentMap(this)
    }

    /**
     * Mutates this currentMap to reflect the given delta.
     */
    applyDelta(delta: schema.Round): void {
        const claimedPiles = delta.claimedResourcePiles() ?? assert.fail(`Delta missing claimedResourcePiles`)
        const digLocations = delta.digLocations() ?? assert.fail(`Delta missing digLocations`)
        const fillLocations = delta.fillLocations() ?? assert.fail(`Delta missing fillLocations`)
        const trapAddedLocations = delta.trapAddedLocations() ?? assert.fail(`Delta missing trapAddedLocations`)
        for (let i = 0; i < claimedPiles.xsLength(); i++) {
            const schemaIdx = this.locationToIndex(claimedPiles.xs(i)!, claimedPiles.ys(i)!)
            this.resourcePileData.get(schemaIdx)!.amount = 0
        }
        for (let i = 0; i < digLocations.xsLength(); i++) {
            const schemaIdx = this.locationToIndex(digLocations.xs(i)!, digLocations.ys(i)!)
            this.water[schemaIdx] = 1
        }
        for (let i = 0; i < fillLocations.xsLength(); i++) {
            const schemaIdx = this.locationToIndex(digLocations.xs(i)!, digLocations.ys(i)!)
            this.water[schemaIdx] = 0
        }
        for (let i = 0; i < delta.trapAddedIdsLength(); i++) {
            const id = delta.trapAddedIds(i)!
            const location = { x: trapAddedLocations.xs(i)!, y: trapAddedLocations.ys(i)! }
            const type = delta.trapAddedTypes(i)!
            const team = delta.trapAddedTeams(i)!
            this.trapData.set(id, { location, type, team })
        }
        for (let i = 0; i < delta.trapTriggeredIdsLength(); i++) {
            this.trapData.delete(delta.trapAddedIds(i)!)
        }

        this.indicatorDotData = []
        const locs = delta.indicatorDotLocs() ?? assert.fail(`Delta missing indicatorDotLocs`)
        const dotColors = delta.indicatorDotRgbs() ?? assert.fail(`Delta missing indicatorDotRgbs`)
        for (let i = 0; i < locs.xsLength(); i++) {
            this.indicatorDotData.push({
                location: { x: locs.xs(i)!, y: locs.ys(i)! },
                color: renderUtils.rgbToHex(dotColors.red(i)!, dotColors.green(i)!, dotColors.blue(i)!),
                id: delta.indicatorDotIds(i)!
            })
        }

        this.indicatorLineData = []
        const starts = delta.indicatorLineStartLocs() ?? assert.fail(`Delta missing indicatorLineStarts`)
        const ends = delta.indicatorLineEndLocs() ?? assert.fail(`Delta missing indicatorLineEnds`)
        const lineColors = delta.indicatorLineRgbs() ?? assert.fail(`Delta missing indicatorLineRgbs`)
        for (let i = 0; i < starts.xsLength(); i++) {
            this.indicatorLineData.push({
                start: { x: starts.xs(i)!, y: starts.ys(i)! },
                end: { x: ends.xs(i)!, y: ends.ys(i)! },
                color: renderUtils.rgbToHex(lineColors.red(i)!, lineColors.green(i)!, lineColors.blue(i)!),
                id: delta.indicatorLineIds(i)!
            })
        }
    }

    draw(match: Match, ctx: CanvasRenderingContext2D, config: ClientConfig, selectedBody?: Body) {
        const dimension = this.dimension
        for (let i = 0; i < dimension.width; i++) {
            for (let j = 0; j < dimension.height; j++) {
                const schemaIdx = this.locationToIndex(i, j)
                const coords = renderUtils.getRenderCoords(i, j, dimension)

                // Render rounded (clipped) water
                if (this.water[schemaIdx]) {
                    renderUtils.renderRounded(
                        ctx,
                        i,
                        j,
                        this,
                        this.water,
                        () => {
                            ctx.fillStyle = WATER_COLOR
                            ctx.fillRect(coords.x, coords.y, 1.0, 1.0)
                        },
                        { x: true, y: false }
                    )
                }

                // Render rounded (clipped) divider
                if (match.currentTurn.turnNumber < DIVIDER_DROP_TURN && this.staticMap.divider[schemaIdx]) {
                    renderUtils.renderRounded(
                        ctx,
                        i,
                        j,
                        this,
                        this.staticMap.divider,
                        () => {
                            ctx.fillStyle = DIVIDER_COLOR
                            ctx.fillRect(coords.x, coords.y, 1.0, 1.0)
                        },
                        { x: false, y: true }
                    )
                }
            }
        }

        // Render flags
        for (const flagId of this.flagData.keys()) {
            const data = this.flagData.get(flagId)!
            let loc: Vector = getEmptyVector()
            if (data.carrierId) {
                // Bot is carrying flag
                loc = match.currentTurn.bodies.getById(data.carrierId).getInterpolatedCoords(match.currentTurn)
            } else {
                loc = data.location
            }
            renderUtils.renderCenteredImageOrLoadingIndicator(
                ctx,
                getImageIfLoaded('resources/bread_64x64.png'),
                renderUtils.getRenderCoords(loc.x, loc.y, this.dimension),
                1
            )
        }

        // Render resource piles
        for (const pileId of this.resourcePileData.keys()) {
            const data = this.resourcePileData.get(pileId)!
            if (data.amount == 0) continue
            const loc = this.indexToLocation(pileId)
            const size = (data.amount / 10) * 0.3 + 0.75
            const coords = renderUtils.getRenderCoords(loc.x, loc.y, this.dimension)
            const crumbVersion = ((loc.x * 37 + loc.y * 19) % 3) + 1
            renderUtils.renderCenteredImageOrLoadingIndicator(
                ctx,
                getImageIfLoaded(`resources/crumb_${crumbVersion}_64x64.png`),
                coords,
                size
            )
        }

        // Render traps
        for (const trapId of this.trapData.keys()) {
            const data = this.trapData.get(trapId)!
            const file = `traps/${BUILD_NAMES[data.type]}_64x64.png`
            const loc = data.location
            const coords = renderUtils.getRenderCoords(loc.x, loc.y, this.dimension)
            ctx.beginPath()
            const r90 = 0.5 * Math.PI
            ctx.moveTo(coords.x + 0.1, coords.y + 0.2)
            ctx.arc(coords.x + 0.2, coords.y + 0.2, 0.1, 2 * r90, 3 * r90)
            ctx.lineTo(coords.x + 0.8, coords.y + 0.1)
            ctx.arc(coords.x + 0.8, coords.y + 0.2, 0.1, 3 * r90, 4 * r90)
            ctx.lineTo(coords.x + 0.9, coords.y + 0.8)
            ctx.arc(coords.x + 0.8, coords.y + 0.8, 0.1, 0, r90)
            ctx.lineTo(coords.x + 0.2, coords.y + 0.9)
            ctx.arc(coords.x + 0.2, coords.y + 0.8, 0.1, r90, 2 * r90)
            ctx.lineTo(coords.x + 0.1, coords.y + 0.2)
            ctx.closePath()
            ctx.strokeStyle = TEAM_COLORS[data.team]
            ctx.lineWidth = 0.075
            ctx.stroke()

            renderUtils.renderCenteredImageOrLoadingIndicator(ctx, getImageIfLoaded(file), coords, 0.8)
        }

        // Render indicator dots
        for (const data of this.indicatorDotData) {
            if ((selectedBody && data.id === selectedBody.id) || config.showAllIndicators) {
                const coords = renderUtils.getRenderCoords(data.location.x, data.location.y, this.dimension)
                ctx.beginPath()
                ctx.arc(coords.x + 0.5, coords.y + 0.5, INDICATOR_DOT_SIZE, 0, 2 * Math.PI, false)
                ctx.fillStyle = data.color
                ctx.fill()
            }
        }

        ctx.lineWidth = INDICATOR_LINE_WIDTH
        for (const data of this.indicatorLineData) {
            if ((selectedBody && data.id === selectedBody.id) || config.showAllIndicators) {
                const start = renderUtils.getRenderCoords(data.start.x, data.start.y, this.dimension)
                const end = renderUtils.getRenderCoords(data.end.x, data.end.y, this.dimension)
                ctx.beginPath()
                ctx.moveTo(start.x + 0.5, start.y + 0.5)
                ctx.lineTo(end.x + 0.5, end.y + 0.5)
                ctx.strokeStyle = data.color
                ctx.stroke()
            }
        }
    }

    getEditorBrushes() {
        const brushes: MapEditorBrush[] = [
            new WaterBrush(this),
            new ResourcePileBrush(this),
            new SpawnZoneBrush(this),
            new TestTrapBrush(this),
            new WallsBrush(this)
        ]
        return brushes.concat(this.staticMap.getEditorBrushes())
    }

    isEmpty(): boolean {
        return this.resourcePileData.size == 0 && this.staticMap.isEmpty()
    }

    /**
     * Creates a packet of flatbuffers data which will later be inserted
     * This and the next function are seperated due to how flatbuffers works
     */
    getSchemaPacket(builder: flatbuffers.Builder): SchemaPacket {
        const wallsOffset = schema.GameMap.createWallsVector(
            builder,
            Array.from(this.staticMap.walls).map((x) => !!x)
        )
        const waterOffset = schema.GameMap.createWaterVector(
            builder,
            Array.from(this.staticMap.initialWater).map((x) => !!x)
        )
        const dividerOffset = schema.GameMap.createDividerVector(
            builder,
            Array.from(this.staticMap.divider).map((x) => !!x)
        )
        const resourcePileAmountOffset = schema.GameMap.createResourcePileAmountsVector(
            builder,
            Array.from(this.resourcePileData.values()).map((x) => x.amount)
        )
        const spawnLocationOffset = packVecTable(builder, this.staticMap.spawnLocations)
        const resourcePileOffset = packVecTable(builder, this.staticMap.resourcePileLocations)

        return {
            wallsOffset,
            waterOffset,
            dividerOffset,
            spawnLocationOffset,
            resourcePileOffset,
            resourcePileAmountOffset
        }
    }

    /**
     * Inserts an existing packet of flatbuffers data into the given builder
     * This and the previous function are seperated due to how flatbuffers works
     */
    insertSchemaPacket(builder: flatbuffers.Builder, packet: SchemaPacket) {
        schema.GameMap.addWalls(builder, packet.wallsOffset)
        schema.GameMap.addWater(builder, packet.waterOffset)
        schema.GameMap.addDivider(builder, packet.dividerOffset)
        schema.GameMap.addSpawnLocations(builder, packet.spawnLocationOffset)
        schema.GameMap.addResourcePiles(builder, packet.resourcePileOffset)
        schema.GameMap.addResourcePileAmounts(builder, packet.resourcePileAmountOffset)
    }
}

export class StaticMap {
    constructor(
        public name: string,
        public readonly randomSeed: number, // I dont know what this is for
        public readonly symmetry: number,
        public readonly dimension: Dimension,
        public readonly walls: Int8Array,
        public readonly divider: Int8Array,
        public readonly spawnLocations: Vector[],
        public readonly resourcePileLocations: Vector[],
        public readonly initialResourcePileAmounts: Int32Array,
        public readonly initialWater: Int8Array
    ) {
        if (symmetry < 0 || symmetry > 2 || !Number.isInteger(symmetry)) throw new Error(`Invalid symmetry ${symmetry}`)

        if (walls.length != dimension.width * dimension.height) throw new Error('Invalid walls length')
        if (divider.length != dimension.width * dimension.height) throw new Error('Invalid divider length')

        if (walls.some((x) => x !== 0 && x !== 1)) throw new Error('Invalid walls value')
        if (divider.some((x) => x !== 0 && x !== 1)) throw new Error('Invalid divider value')
    }

    static fromSchema(schemaMap: schema.GameMap) {
        const name = schemaMap.name() as string
        const randomSeed = schemaMap.randomSeed()
        const symmetry = schemaMap.symmetry()

        const size = schemaMap.size() ?? assert.fail('Map size() is missing')
        const minCorner = { x: 0, y: 0 }
        const maxCorner = { x: size.x(), y: size.y() }
        const dimension = {
            minCorner,
            maxCorner,
            width: maxCorner.x - minCorner.x,
            height: maxCorner.y - minCorner.y
        }

        const walls = schemaMap.wallsArray() ?? assert.fail('wallsArray() is null')
        const divider = schemaMap.dividerArray() ?? assert.fail('dividerArray() is null')
        const spawnLocations = parseVecTable(schemaMap.spawnLocations() ?? assert.fail('spawnLocations() is null'))
        const resourcePileLocations = parseVecTable(schemaMap.resourcePiles() ?? assert.fail('resourcePiles() is null'))
        const initialResourcePileAmounts =
            schemaMap.resourcePileAmountsArray() ?? assert.fail('resourcePileAmountsArray() is null')
        const initialWater = schemaMap.waterArray() ?? assert.fail('waterArray() is null')
        return new StaticMap(
            name,
            randomSeed,
            symmetry,
            dimension,
            walls,
            divider,
            spawnLocations,
            resourcePileLocations,
            initialResourcePileAmounts,
            initialWater
        )
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
        const divider = new Int8Array(width * height)
        const spawnLocations: Vector[] = []
        const resourcePileLocations: Vector[] = []
        const initialResourcePileAmounts = new Int32Array()
        const initialWater = new Int8Array(width * height)
        return new StaticMap(
            name,
            randomSeed,
            symmetry,
            dimension,
            walls,
            divider,
            spawnLocations,
            resourcePileLocations,
            initialResourcePileAmounts,
            initialWater
        )
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
        return Math.floor(y) * this.width + Math.floor(x)
    }

    /**
     * Returns a point representing the reflection of the given point following the map's symmetry.
     */
    applySymmetry(point: Vector): Vector {
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
        ctx.fillStyle = GRASS_COLOR
        ctx.fillRect(
            this.dimension.minCorner.x,
            this.dimension.minCorner.y,
            this.dimension.width,
            this.dimension.height
        )

        // Populate buffer with values where spawn zones should be rendered
        const spawnZoneDrawAreas = Array(this.width * this.height).fill(0)
        for (let i = 0; i < this.spawnLocations.length; i++) {
            const pos = this.spawnLocations[i]
            for (let x = -1; x <= 1; x++) {
                for (let y = -1; y <= 1; y++) {
                    const target_x = pos.x + x
                    const target_y = pos.y + y
                    if (target_x >= 0 && target_x < this.width && target_y >= 0 && target_y < this.height) {
                        const idx = this.locationToIndex(target_x, target_y)
                        if (this.walls[idx] || this.initialWater[idx]) continue
                        // Team A: 1, Team B: 2
                        spawnZoneDrawAreas[idx] = (i % 2) + 1
                    }
                }
            }
        }

        for (let i = 0; i < this.dimension.width; i++) {
            for (let j = 0; j < this.dimension.height; j++) {
                const schemaIdx = this.locationToIndex(i, j)
                const coords = renderUtils.getRenderCoords(i, j, this.dimension)

                // Render rounded (clipped) wall
                if (this.walls[schemaIdx]) {
                    renderUtils.renderRounded(ctx, i, j, this, this.walls, () => {
                        ctx.fillStyle = WALLS_COLOR
                        ctx.fillRect(coords.x, coords.y, 1.0, 1.0)
                    })
                }
                // Render spawn zones
                if (spawnZoneDrawAreas[schemaIdx]) {
                    const color = TEAM_COLORS[spawnZoneDrawAreas[schemaIdx] - 1]
                    renderUtils.renderRounded(ctx, i, j, this, spawnZoneDrawAreas, () => {
                        renderUtils.drawDiagonalLines(ctx, coords, 1.0, color)
                    })
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
            this.divider.every((x) => x == 0) &&
            this.spawnLocations.length == 0 &&
            this.resourcePileLocations.length == 0
        )
    }

    getEditorBrushes(): MapEditorBrush[] {
        return [new DividerBrush(this)]
    }
}

import { flatbuffers, schema } from 'battlecode-schema'
import assert from 'assert'
import { Vector } from './Vector'
import * as renderUtils from '../util/RenderUtil'
import { MapEditorBrush, Symmetry } from '../components/sidebar/map-editor/MapEditorBrush'
import { packVecTable, parseVecTable } from './SchemaHelpers'
import { DividerBrush, ResourcePileBrush, SpawnZoneBrush, WallsBrush, WaterBrush } from './Brushes'

export type Dimension = {
    minCorner: Vector
    maxCorner: Vector
    width: number
    height: number
}

type ResourcePileData = {
    amount: number
}

type SchemaPacket = {
    wallsOffset: number
    waterOffset: number
    dividerOffset: number
    spawnLocationOffset: number
    resourcePileOffset: number
    resourcePileAmountOffset: number
}

export class CurrentMap {
    public readonly staticMap: StaticMap
    public readonly resourcePileData: Map<number, ResourcePileData>
    public readonly water: Int8Array

    get width(): number {
        return this.staticMap.dimension.width
    }
    get height(): number {
        return this.staticMap.dimension.height
    }

    constructor(from: StaticMap | CurrentMap) {
        if (from instanceof StaticMap) {
            // Create current map from static map

            this.staticMap = from
            this.resourcePileData = new Map()
            this.water = new Int8Array(from.initialWater)
            for (let i = 0; i < from.initialResourcePileAmounts.length; i++) {
                const id = renderUtils.getSchemaIdx(
                    from.resourcePileLocations[i].x,
                    from.resourcePileLocations[i].y,
                    from.dimension
                )
                this.resourcePileData.set(id, { amount: from.initialResourcePileAmounts[i] })
            }
        } else {
            // Create current map from current map (copy)

            this.staticMap = from.staticMap
            this.resourcePileData = new Map()
            for (let [key, value] of from.resourcePileData) {
                this.resourcePileData.set(key, { ...value })
            }
            this.water = new Int8Array(from.water)
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
        const claimedPiles = delta.claimedResourcePiles() ?? assert.fail(`Delta missing claimedResourcePiles`)
        const digLocations = delta.digLocations() ?? assert.fail(`Delta missing digLocations`)
        const fillLocations = delta.fillLocations() ?? assert.fail(`Delta missing fillLocations`)
        for (let i = 0; i < claimedPiles.xsLength(); i++) {}
        for (let i = 0; i < digLocations.xsLength(); i++) {}
        for (let i = 0; i < fillLocations.xsLength(); i++) {}
        for (let i = 0; i < delta.trapIdsLength(); i++) {}
        for (let i = 0; i < delta.trapTriggeredIdsLength(); i++) {}
    }

    draw(ctx: CanvasRenderingContext2D) {
        const dimension = this.staticMap.dimension
        for (let i = 0; i < dimension.width; i++) {
            for (let j = 0; j < dimension.height; j++) {
                const schemaIdx = renderUtils.getSchemaIdx(i, j, dimension)
                const coords = renderUtils.getRenderCoords(i, j, dimension)

                // TODO: render
            }
        }
    }

    getEditorBrushes() {
        const brushes: MapEditorBrush[] = []
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
            Array.from(this.water).map((x) => !!x)
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
        const divider = schemaMap.dividerArray() ?? assert.fail('divierArray() is null')
        const spawnLocations = parseVecTable(schemaMap.spawnLocations() ?? assert.fail('divierArray() is null'))
        const resourcePileLocations = parseVecTable(schemaMap.resourcePiles() ?? assert.fail('divierArray() is null'))
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
        ctx.fillStyle = '#28683e'
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
                        ctx.fillStyle = 'grey'
                        ctx.fillRect(coords.x, coords.y, scale, scale)
                    })
                }

                // Render rounded (clipped) water
                if (this.initialWater[schemaIdx]) {
                    renderUtils.renderRounded(ctx, i, j, this.dimension, this.initialWater, (scale) => {
                        ctx.fillStyle = '#2b58a5'
                        ctx.fillRect(coords.x, coords.y, scale, scale)
                    })
                }

                // Render rounded (clipped) divider
                if (this.divider[schemaIdx]) {
                    renderUtils.renderRounded(ctx, i, j, this.dimension, this.divider, (scale) => {
                        ctx.fillStyle = '#000000'
                        ctx.fillRect(coords.x, coords.y, scale, scale)
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
        return [
            new WallsBrush(this),
            new WaterBrush(this),
            new DividerBrush(this),
            new SpawnZoneBrush(this),
            new ResourcePileBrush(this)
        ]
    }
}

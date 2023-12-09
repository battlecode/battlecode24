import {
    MapEditorBrush,
    MapEditorBrushField,
    MapEditorBrushFieldType,
    SymmetricMapEditorBrush
} from '../components/sidebar/map-editor/MapEditorBrush'
import * as cst from '../constants'
import { CurrentMap, StaticMap } from './Map'

const applyInRadius = (
    map: CurrentMap | StaticMap,
    x: number,
    y: number,
    radius: number,
    func: (idx: number) => void
) => {
    for (let i = -radius; i <= radius; i++) {
        for (let j = -radius; j <= radius; j++) {
            if (Math.sqrt(i * i + j * j) <= radius) {
                const target_x = x + i
                const target_y = y + j
                if (target_x >= 0 && target_x < map.width && target_y >= 0 && target_y < map.height) {
                    const target_idx = map.locationToIndex(target_x, target_y)
                    func(target_idx)
                }
            }
        }
    }
}

export class WallsBrush extends SymmetricMapEditorBrush<StaticMap> {
    public readonly name = 'Walls'
    public readonly fields = {
        should_add: {
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
        applyInRadius(this.map, x, y, radius, (idx) => {
            this.map.walls[idx] = fields.should_add.value ? 1 : 0
        })
    }
}

export class DividerBrush extends SymmetricMapEditorBrush<StaticMap> {
    public readonly name = 'Divider'
    public readonly fields = {
        should_add: {
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
        applyInRadius(this.map, x, y, radius, (idx) => {
            this.map.divider[idx] = fields.should_add.value ? 1 : 0
        })
    }
}

export class SpawnZoneBrush extends SymmetricMapEditorBrush<StaticMap> {
    public readonly name = 'Spawn Zones'
    public readonly fields = {
        should_add: {
            type: MapEditorBrushFieldType.ADD_REMOVE,
            value: true
        }
    }

    constructor(map: StaticMap) {
        super(map)
    }

    public symmetricApply(x: number, y: number, fields: Record<string, MapEditorBrushField>) {
        const foundIdx = this.map.spawnLocations.findIndex((l) => l.x == x && l.y == y)

        if (fields.should_add.value) {
            if (foundIdx != -1) return
            this.map.spawnLocations.push({ x, y })
            return
        }

        if (foundIdx == -1) return
        for (let i = foundIdx; i < this.map.spawnLocations.length - 1; i++)
            this.map.spawnLocations[i] = this.map.spawnLocations[i + 1]
        this.map.spawnLocations.pop()
    }
}

export class WaterBrush extends SymmetricMapEditorBrush<CurrentMap> {
    public readonly name = 'Water'
    public readonly fields = {
        should_add: {
            type: MapEditorBrushFieldType.ADD_REMOVE,
            value: true
        },
        radius: {
            type: MapEditorBrushFieldType.POSITIVE_INTEGER,
            value: 1,
            label: 'Radius'
        }
    }

    constructor(map: CurrentMap) {
        super(map)
    }

    public symmetricApply(x: number, y: number, fields: Record<string, MapEditorBrushField>) {
        const radius: number = fields.radius.value - 1
        applyInRadius(this.map, x, y, radius, (idx) => {
            this.map.water[idx] = fields.should_add.value ? 1 : 0
        })
    }
}

export class ResourcePileBrush extends SymmetricMapEditorBrush<CurrentMap> {
    public readonly name = 'Resource Piles'
    public readonly fields = {
        should_add: {
            type: MapEditorBrushFieldType.ADD_REMOVE,
            value: true
        },
        amount: {
            type: MapEditorBrushFieldType.SINGLE_SELECT,
            options: [
                { value: 10, label: '10' },
                { value: 20, label: '20' },
                { value: 30, label: '30' }
            ],
            label: 'Amount',
            value: 10
        }
    }
    constructor(map: CurrentMap) {
        super(map)
    }

    public symmetricApply(x: number, y: number, fields: Record<string, MapEditorBrushField>) {
        const foundIdx = this.map.staticMap.resourcePileLocations.findIndex((l) => l.x == x && l.y == y)
        const schemaIdx = this.map.locationToIndex(x, y)

        if (fields.should_add.value) {
            if (foundIdx != -1) return
            this.map.resourcePileData.set(schemaIdx, { amount: fields.amount.value })
            this.map.staticMap.resourcePileLocations.push({ x, y })
            return
        }

        if (foundIdx == -1) return
        for (let i = foundIdx; i < this.map.staticMap.resourcePileLocations.length - 1; i++)
            this.map.staticMap.resourcePileLocations[i] = this.map.staticMap.resourcePileLocations[i + 1]
        this.map.staticMap.resourcePileLocations.pop()
        this.map.resourcePileData.delete(schemaIdx)
    }
}

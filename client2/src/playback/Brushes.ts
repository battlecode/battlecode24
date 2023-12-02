import {
    MapEditorBrush,
    MapEditorBrushField,
    MapEditorBrushFieldType,
    SymmetricMapEditorBrush
} from '../components/sidebar/map-editor/MapEditorBrush'
import * as cst from '../constants'
import { CurrentMap, StaticMap } from './Map'

const applyInRadius = (map: StaticMap, x: number, y: number, radius: number, func: (idx: number) => void) => {
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

export class WallsBrush extends SymmetricMapEditorBrush {
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

export class WaterBrush extends SymmetricMapEditorBrush {
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

    constructor(map: StaticMap) {
        super(map)
    }

    public symmetricApply(x: number, y: number, fields: Record<string, MapEditorBrushField>) {
        const radius: number = fields.radius.value - 1
        applyInRadius(this.map, x, y, radius, (idx) => {
            const is_water: boolean = fields.is_water.value
            this.map.initialWater[idx] = is_water ? 1 : 0
        })
    }
}

export class DividerBrush extends SymmetricMapEditorBrush {
    public readonly name = 'Divider'
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
        applyInRadius(this.map, x, y, 1, (idx) => {
            this.map.divider[idx] = fields.should_add.value ? 1 : 0
        })
    }
}

export class SpawnZoneBrush extends SymmetricMapEditorBrush {
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
        this.map.spawnLocations.push({ x, y })
    }
}

export class ResourcePileBrush extends SymmetricMapEditorBrush {
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
            label: 'Amount'
        }
    }
    constructor(map: StaticMap) {
        super(map)
    }

    public symmetricApply(x: number, y: number, fields: Record<string, MapEditorBrushField>) {
        this.map.initialResourcePileAmounts[this.map.resourcePileLocations.length] = fields.amount.value
        this.map.resourcePileLocations.push({ x, y })
    }
}

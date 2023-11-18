import {
    MapEditorBrush,
    MapEditorBrushField,
    MapEditorBrushFieldType,
    SymmetricMapEditorBrush
} from '../components/sidebar/map-editor/MapEditorBrush'
import * as cst from '../constants'
import { CurrentMap, StaticMap } from './Map'

export class WallsBrush extends SymmetricMapEditorBrush {
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

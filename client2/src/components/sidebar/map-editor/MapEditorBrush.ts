export abstract class MapEditorBrush {
    abstract name: string
    abstract fields: Record<string, MapEditorBrushField>
    abstract apply(x: number, y: number, fields: Record<string, MapEditorBrushField>): void
    public open: boolean = false

    public opened(open: boolean): MapEditorBrush {
        const newBrush = { ...this, open: open }
        Object.setPrototypeOf(newBrush, Object.getPrototypeOf(this))
        return newBrush
    }
}

export interface MapEditorBrushField {
    type: MapEditorBrushFieldType
    value: any
    label?: string
    options?: { value: any; label: string }[]
    min?: number
    max?: number
}

export enum MapEditorBrushFieldType {
    POSITIVE_INTEGER,
    ADD_REMOVE,
    TEAM,
    SINGLE_SELECT
}

export enum Symmetry {
    ROTATIONAL,
    HORIZONTAL,
    VERTICAL
}

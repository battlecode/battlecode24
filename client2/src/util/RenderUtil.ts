import * as cst from '../constants'
import { Dimension } from '../playback/Map'
import { Vector } from '../playback/Vector'

export const getRenderCoords = (cellX: number, cellY: number, dims: Dimension) => {
    const cx = dims.minCorner.x + cellX
    const cy = dims.minCorner.y + dims.height - cellY - 1 // Y is flipped
    return { x: cx, y: cy }
}

export const getSchemaIdx = (cellX: number, cellY: number, dims: Dimension) => {
    return Math.floor(cellY) * dims.width + Math.floor(cellX)
}

export const get9SliceClipPath = (
    i: number,
    j: number,
    dims: Dimension,
    vals: number[] | Int8Array | Int32Array,
    valFunc: (v: number | boolean) => boolean = (v) => (v ? true : false)
): number[][] => {
    let edge = 0.07
    let bevel = 0.13
    let neighbors: boolean[] = []
    for (let v = 1; v < 9; v++) {
        let x = cst.DIRECTIONS[v][0] + i
        let y = cst.DIRECTIONS[v][1] + j
        neighbors.push(
            x < 0 || y < 0 || x == dims.width || y == dims.height
                ? false
                : valFunc(vals[getSchemaIdx(x, y, dims)])
        )
    }
    let points: number[][] = []
    let corners: Record<number, { cx: number; cy: number; sx: number; sy: number }> = {
        0: { cx: 0, cy: 1, sx: 1, sy: -1 },
        1: { cx: 1, cy: 1, sx: -1, sy: -1 },
        2: { cx: 1, cy: 0, sx: -1, sy: 1 },
        3: { cx: 0, cy: 0, sx: 1, sy: 1 }
    }
    for (let corner = 0; corner < 4; corner++) {
        let cl = corner * 2
        let cc = corner * 2 + 1
        let cr = (corner * 2 + 2) % 8

        let { cx, cy, sx, sy } = corners[corner]

        if (neighbors[cl] || neighbors[cr]) {
            if (neighbors[cl] && neighbors[cr]) {
                if (neighbors[cc]) {
                    points.push([cx, cy])
                } else {
                    if (corner % 2 == 1) points.push([cx + sx * edge, cy])
                    points.push([cx, cy + sy * edge])
                    if (corner % 2 == 0) points.push([cx + sx * edge, cy])
                }
            } else if (neighbors[cr])
                points.push(corner % 2 == 0 ? [cx + sx * edge, cy] : [cx, cy + sy * edge])
            else if (neighbors[cl])
                points.push(corner % 2 == 1 ? [cx + sx * edge, cy] : [cx, cy + sy * edge])
        } else {
            if (corner % 2 == 0) points.push([cx + sx * edge, cy + sy * bevel])
            points.push([cx + sx * bevel, cy + sy * edge])
            if (corner % 2 == 1) points.push([cx + sx * edge, cy + sy * bevel])
        }
    }
    return points
}

export const applyClipScaled = (
    ctx: CanvasRenderingContext2D,
    i: number,
    j: number,
    scale: number,
    path: number[][],
    render: () => void
) => {
    ctx.save()
    ctx.beginPath()
    let started = false
    for (let point of path) {
        if (!started) ctx.moveTo((point[0] + i) * scale, (point[1] + j) * scale)
        else ctx.lineTo((point[0] + i) * scale, (point[1] + j) * scale)
        started = true
    }
    ctx.closePath()
    ctx.clip()
    render()
    ctx.restore()
}

export const renderRounded = (
    ctx: CanvasRenderingContext2D,
    i: number,
    j: number,
    dims: Dimension,
    values: number[] | Int8Array | Int32Array,
    render: (scale: number) => void,
    renderScale: number = 1.01,
    valueCheck: (v: number | boolean) => boolean = (v) => !!v
) => {
    const path = get9SliceClipPath(i, j, dims, values, valueCheck)
    const coords = getRenderCoords(i, j, dims)
    applyClipScaled(ctx, coords.x / renderScale, coords.y / renderScale, renderScale, path, () =>
        render(renderScale)
    )
}

export const applyStyles = (
    ctx: CanvasRenderingContext2D,
    styles: Record<string, any>,
    render: () => void
) => {
    const saved: Record<string, any> = {}
    for (const style in styles) {
        saved[style] = (ctx as any)[style]
        ;(ctx as any)[style] = styles[style]
    }
    render()
    for (const style in saved) (ctx as any)[style] = saved[style]
}

export const renderTileArrow = (
    ctx: CanvasRenderingContext2D,
    coords: Vector,
    direction: number
) => {
    ctx.beginPath()

    let dir = cst.DIRECTIONS[direction]
    let len = (1 / Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1])) * 0.4
    let x = coords.x + 0.5
    let y = coords.y + 0.5

    ctx.moveTo(x + dir[0] * len, y + dir[1] * len)
    let right = [(-dir[1] * len) / 2, (dir[0] * len) / 2]
    ctx.lineTo(x - dir[0] * len * 0.7 + right[0], y - dir[1] * len * 0.7 + right[1])
    ctx.lineTo(x - dir[0] * len * 0.7 - right[0], y - dir[1] * len * 0.7 - right[1])
    ctx.closePath()
    ctx.fill()
}

// Draws an image at (x, y) such that it is centered in a SIZE*SIZE grid of cells
export const renderCenteredImage = (
    ctx: CanvasRenderingContext2D,
    img: CanvasImageSource,
    coords: Vector,
    size: number
) => {
    ctx.drawImage(img, coords.x + (1 - size) / 2, coords.y + (1 - size) / 2, size, size)
}

/**
 * Renders the image centerd at the location if image exists, otherwise renders a loading indicator
 */
export const renderCenteredImageOrLoadingIndicator = (
    ctx: CanvasRenderingContext2D,
    img: CanvasImageSource | undefined,
    coords: Vector,
    size: number
) => {
    if (img) {
        renderCenteredImage(ctx, img, coords, size)
    } else {
        ctx.beginPath()
        ctx.arc(coords.x + 0.5, coords.y + 0.5, size / 2.5, 0, 2 * Math.PI)
        ctx.fillStyle = '#0005'
        ctx.fill()
    }
}

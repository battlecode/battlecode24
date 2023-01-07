import * as config from '../../config'
import * as cst from '../../constants'

import { Game, GameWorld, schema } from 'battlecode-playback'
import { AllImages } from '../../imageloader'

import { GameMap, MapUnit } from '../index'

/**
 * Renders the world.
 *
 * Note that all rendering functions draw in world-units,
 */
export default class MapRenderer {
  private conf: config.Config

  readonly canvas: HTMLCanvasElement
  readonly ctx: CanvasRenderingContext2D
  readonly imgs: AllImages

  // Callbacks for clicking robots and trees on the canvas
  readonly onclickUnit: (id: number) => void
  readonly onclickBlank: (x, y) => void
  readonly onMouseover: (x: number, y: number, walls: boolean, resource: number) => void
  readonly onDrag: (x, y) => void

  // Other useful values
  readonly bgPattern: CanvasPattern
  private width: number // in world units
  private height: number // in world units

  private map: GameMap //the current map

  constructor(canvas: HTMLCanvasElement, imgs: AllImages, conf: config.Config,
    onclickUnit: (id: number) => void, onclickBlank: (x: number, y: number) => void,
    onMouseover: (x: number, y: number, walls: boolean, resource: number) => void,
    onDrag: (x: number, y: number) => void) {
    this.canvas = canvas
    this.conf = conf
    this.imgs = imgs
    this.onclickUnit = onclickUnit
    this.onclickBlank = onclickBlank
    this.onMouseover = onMouseover
    this.onDrag = onDrag

    let ctx = canvas.getContext("2d")
    if (ctx === null) {
      throw new Error("Couldn't load canvas2d context")
    } else {
      this.ctx = ctx
    }

    //this.bgPattern = <CanvasPattern>this.ctx.createPattern(imgs.tiles[0], 'repeat');
    this.setEventListeners()
  }

  /**
   * Renders the game map.
   */
  render(map: GameMap): void {
    const scale = this.canvas.width / map.width
    this.width = map.width
    this.height = map.height
    this.map = map

    // setup correct rendering
    this.ctx.restore()
    this.ctx.save()
    this.ctx.scale(scale, scale)

    this.renderBackground(map)
    this.renderBodies(map)
    this.renderResources(map)
    this.renderIslands(map)
    this.renderObstacles(map)
    // restore default rendering
  }

  renderIndividual(x: number, y: number, map: GameMap, updateNeighbors = true): void {
    let render_y = (map.height - y - 1)
    const scale = this.canvas.width / map.width
    this.width = map.width
    this.height = map.height
    this.map = map

    // setup correct rendering
    this.ctx.restore()
    this.ctx.save()
    this.ctx.scale(scale, scale)

    this.renderTile(x, render_y, map.walls[x + this.width * y], map, updateNeighbors)

    map.originalBodies.forEach((body: MapUnit) => {
      if (body.x == x && body.y == y)
        this.renderBody(body)
    })
    map.symmetricBodies.forEach((body: MapUnit) => {
      if (body.x == x && body.y == y)
        this.renderBody(body)
    })

    this.renderResource(x, render_y, map.resource_wells[x + this.width * y])
    this.renderIsland(x, render_y, map, updateNeighbors)
    this.renderObstacle(x, render_y, map, updateNeighbors)
  }

  /**
   * Returns the mirrored y coordinate to be consistent with (0, 0) in the
   * bottom-left corner (top-left corner is canvas default).
   * params: y coordinate to flip
   *         height coordinate of the maximum edge
   */
  private flip(y: number, height: number) {
    return height - y
  }

  /**
   * Draw the background
   */
  private renderBackground(map: GameMap): void {
    for (let i = 0; i < this.width; i++) {
      for (let j = 0; j < this.height; j++) {
        const wall = map.walls[(map.height - j - 1) * this.width + i]
        this.renderTile(i, j, wall, map, false)
      }
    }
  }

  private renderObstacles(map: GameMap): void {
    for (let i = 0; i < this.width; i++) {
      for (let j = 0; j < this.height; j++) {
        this.renderObstacle(i, j, map, false)
      }
    }
  }

  private renderObstacle(i: number, j: number, map: GameMap, updateNeighbors = true) {
    if (map.clouds[(map.height - j - 1) * this.width + i]) {
      // this.renderOverlay(i, j, "white", .3)
      this.ctx.save()
      const scale = 20
      this.ctx.scale(1 / scale, 1 / scale)
      this.ctx.globalAlpha = .3
      this.ctx.fillStyle = "white"
      let path = this.get9SliceClipPath(i, (map.height - j - 1), map.clouds, map)
      this.applyClipScaled(i, j, scale, path)
      this.ctx.fillRect(i * scale, j * scale, scale, scale)
      this.ctx.restore()
    }
    if (map.currents[(map.height - j - 1) * this.width + i]) {
      // this.renderOverlay(i, j, "purple", .2)
      this.ctx.save()
      const scale = 20
      this.ctx.scale(1 / scale, 1 / scale)
      this.ctx.globalAlpha = .2
      this.ctx.fillStyle = "purple"
      let path = this.get9SliceClipPath(i, (map.height - j - 1), map.currents, map, (c) => c == map.currents[(map.height - j - 1) * this.width + i])
      this.applyClipScaled(i, j, scale, path)
      this.ctx.fillRect(i * scale, j * scale, scale, scale)
      this.ctx.restore()
      this.renderArrow(i, j, map.currents[(map.height - j - 1) * this.width + i])
    }

    if (updateNeighbors)
      this.updateAllNeighbors(i, j, map)
  }

  private renderIslands(map: GameMap): void {
    for (let i = 0; i < this.width; i++) {
      for (let j = 0; j < this.height; j++) {
        this.renderIsland(i, j, map, false)
      }
    }
  }

  renderTile(i: number, j: number, wall: boolean, map: GameMap, updateNeighbors = true) {
    this.ctx.save()
    const scale = 20
    this.ctx.scale(1 / scale, 1 / scale)
    // this.ctx.drawImage(this.imgs.tiles[0], i * scale, j * scale, scale, scale)
    this.ctx.fillStyle = "#faedcd"
    this.ctx.fillStyle = "#BAAD99"
    this.ctx.fillRect(i * scale, j * scale, scale, scale)

    if (wall) {
      this.ctx.save()
      let path = this.get9SliceClipPath(i, (map.height - j - 1), map.walls, map)
      this.applyClipScaled(i, j, scale, path)
      // this.ctx.drawImage(this.imgs.tiles[1], i * scale, j * scale, scale, scale)
      this.ctx.fillStyle = cst.UI_GREY
      this.ctx.fillRect(i * scale, j * scale, scale, scale)
      this.ctx.restore()
    }

    this.ctx.strokeStyle = 'black'
    this.ctx.globalAlpha = .1

    let thickness = .02
    this.ctx.lineWidth = thickness * scale
    this.ctx.strokeRect((i + thickness / 2) * scale, (j + thickness / 2) * scale, scale * (1 - thickness), scale * (1 - thickness))
    this.ctx.restore()

    //redraw all neighbors (not recursive though)
    if (updateNeighbors)
      this.updateAllNeighbors(i, j, map)
  }

  //      [1]   [2]   [3]
  //
  //      [0]         [4]
  //
  //      [7]   [6]   [5]
  // magic code
  private get9SliceClipPath(i: number, j: number, vals: boolean[] | number[], map: GameMap, valFunc: (v: number | boolean) => boolean = (v) => v ? true : false): number[][] {
    let edge = .07
    let bevel = .13
    let neighbors: boolean[] = []
    for (let v = 1; v < 9; v++) {
      let x = (cst.DIRECTIONS[v][0] + i)
      let y = (cst.DIRECTIONS[v][1] + j)
      neighbors.push((x < 0 || y < 0 || x == map.width || y == map.height) ? false : (valFunc(vals[x + y * map.width])))
    }
    let points: number[][] = []
    let corners: Record<number, { cx: number, cy: number, sx: number, sy: number }> = {
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
            if (corner % 2 == 1)
              points.push([cx + sx * edge, cy])
            points.push([cx, cy + sy * edge])
            if (corner % 2 == 0)
              points.push([cx + sx * edge, cy])
          }
        } else if (neighbors[cr])
          points.push(corner % 2 == 0 ? [cx + sx * edge, cy] : [cx, cy + sy * edge])
        else if (neighbors[cl])
          points.push(corner % 2 == 1 ? [cx + sx * edge, cy] : [cx, cy + sy * edge])
      } else {
        if (corner % 2 == 0)
          points.push([cx + sx * edge, cy + sy * bevel])
        points.push([cx + sx * bevel, cy + sy * edge])
        if (corner % 2 == 1)
          points.push([cx + sx * edge, cy + sy * bevel])
      }
    }
    return points
  }

  private applyClipScaled(i: number, j: number, scale: number, path: number[][]) {
    this.ctx.beginPath()
    let started = false
    for (let point of path) {
      if (!started)
        this.ctx.moveTo((point[0] + i) * scale, (point[1] + j) * scale)
      else
        this.ctx.lineTo((point[0] + i) * scale, (point[1] + j) * scale)
      started = true
    }
    this.ctx.closePath()
    this.ctx.clip()
  }

  private updateAllNeighbors(i: number, j: number, map: GameMap) {
    for (let v = 1; v < 9; v++) {
      let x = cst.DIRECTIONS[v][0] + i
      let y = cst.DIRECTIONS[v][1] + (map.height - j - 1)
      if (x < 0 || y < 0 || x == map.width || y == map.height)
        continue
      this.renderIndividual(x, y, map, false)
    }
  }

  private renderOverlay(i: number, j: number, color: string, opacity: number) {
    this.ctx.save()
    const scale = 20
    this.ctx.scale(1 / scale, 1 / scale)
    this.ctx.globalAlpha = opacity
    this.ctx.fillStyle = color
    this.ctx.fillRect(i * scale, j * scale, scale, scale)
    this.ctx.restore()
  }

  private renderArrow(i: number, j: number, direction: number) {
    this.ctx.save()
    const scale = 20
    this.ctx.scale(1 / scale, 1 / scale)
    this.ctx.globalAlpha = .1
    this.ctx.fillStyle = "black"
    this.ctx.beginPath()

    let dir = cst.DIRECTIONS[direction]
    let len = scale / Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1]) * .4
    let x = (i + .5) * scale
    let y = (j + .5) * scale

    this.ctx.moveTo(x + dir[0] * len, y + dir[1] * len)
    let right = [-dir[1] * len / 2, dir[0] * len / 2]
    this.ctx.lineTo(x - dir[0] * len * .7 + right[0], y - dir[1] * len * .7 + right[1])
    this.ctx.lineTo(x - dir[0] * len * .7 - right[0], y - dir[1] * len * .7 - right[1])
    this.ctx.closePath()
    this.ctx.fill()
    this.ctx.restore()
  }

  renderIsland(i: number, j: number, map: GameMap, updateNeighbors = true) {
    if (map.islands[(map.height - j - 1) * this.width + i] == 0) {
      return
    }
    let island = map.islands[(map.height - j - 1) * this.width + i]

    this.ctx.save()
    const scale = 20
    this.ctx.scale(1 / scale, 1 / scale)
    this.ctx.fillStyle = "black"
    this.ctx.globalAlpha = .3

    let path = this.get9SliceClipPath(i, (map.height - j - 1), map.islands, map)
    this.applyClipScaled(i, j, scale, path)

    let x = i * scale
    let y = j * scale
    let d = scale / 8

    this.ctx.beginPath()
    this.ctx.moveTo(x, y)
    this.ctx.lineTo(x + d, y)
    this.ctx.lineTo(x, y + d)
    this.ctx.closePath()
    this.ctx.fill()

    this.ctx.beginPath()
    this.ctx.moveTo(x + 3 * d, y)
    this.ctx.lineTo(x + 5 * d, y)
    this.ctx.lineTo(x, y + 5 * d)
    this.ctx.lineTo(x, y + 3 * d)
    this.ctx.closePath()
    this.ctx.fill()

    this.ctx.beginPath()
    this.ctx.moveTo(x + 7 * d, y)
    this.ctx.lineTo(x + 8 * d, y)
    this.ctx.lineTo(x + 8 * d, y + d)
    this.ctx.lineTo(x + d, y + 8 * d)
    this.ctx.lineTo(x, y + 8 * d)
    this.ctx.lineTo(x, y + 7 * d)
    this.ctx.closePath()
    this.ctx.fill()

    this.ctx.beginPath()
    this.ctx.moveTo(x + 5 * d, y + 8 * d)
    this.ctx.lineTo(x + 3 * d, y + 8 * d)
    this.ctx.lineTo(x + 8 * d, y + 3 * d)
    this.ctx.lineTo(x + 8 * d, y + 5 * d)
    this.ctx.closePath()
    this.ctx.fill()

    this.ctx.beginPath()
    this.ctx.moveTo(x + 8 * d, y + 8 * d)
    this.ctx.lineTo(x + 7 * d, y + 8 * d)
    this.ctx.lineTo(x + 8 * d, y + 7 * d)
    this.ctx.closePath()
    this.ctx.fill()

    this.ctx.globalAlpha = 1
    // this.ctx.fillText(island + "", i * scale, (j + .5) * scale, scale * 1.7)
    this.ctx.restore()

    if (updateNeighbors)
      this.updateAllNeighbors(i, j, map)
  }

  private renderResources(map: GameMap) {
    this.ctx.save()
    this.ctx.globalAlpha = 1

    for (let i = 0; i < this.width; i++) for (let j = 0; j < this.height; j++) {
      const resource = map.resource_wells[(map.height - j - 1) * this.width + i]
      this.renderResource(i, j, resource)
    }

    this.ctx.restore()
  }

  renderResource(i, j, resource) {
    const adamantiumImg = this.imgs.resource_wells[cst.ADAMANTIUM][0]
    const manaImg = this.imgs.resource_wells[cst.MANA][0]
    const scale = 1
    this.ctx.globalAlpha = 1
    const cx = i * scale, cy = j * scale

    if (resource == 1) {
      let size = 1
      this.ctx.drawImage(adamantiumImg, cx + (1 - size) / 2, cy + (1 - size) / 2, scale * size, scale * size)
    }
    if (resource == 2) {
      let size = 1
      this.ctx.drawImage(manaImg, cx + (1 - size) / 2, cy + (1 - size) / 2, scale * size, scale * size)
    }
  }

  /**
   * Draw trees and units on the canvas
   */
  private renderBodies(map: GameMap) {

    this.ctx.fillStyle = "#84bf4b"
    map.originalBodies.forEach((body: MapUnit) => {
      this.renderBody(body)
      // this.drawGoodies(x: number, y: number, radius, body.containedBullets, body.containedBody);
    })

    map.symmetricBodies.forEach((body: MapUnit) => {
      this.renderBody(body)
      // this.drawGoodies(x: number, y: number, radius, body.containedBullets, body.containedBody);
    })
  }

  private renderBody(body: MapUnit) {
    const x = body.x
    const y = this.flip(body.y, this.map.height)
    const radius = body.radius
    let img: HTMLImageElement

    const teamID = body.teamID || 0
    img = this.imgs.robots[body.type][teamID]
    this.drawImage(img, x, y, radius)
  }

  /**
   * Sets the map editor display to contain of the information of the selected
   * tree, or on the selected coordinate if there is no tree.
   */
  private setEventListeners() {

    let hoverPos: { x: number, y: number } | null = null

    const whilemousedown = () => {
      if (hoverPos !== null) {
        const { x, y } = hoverPos
        this.onDrag(x, y)
      }
    }

    var interval: number
    this.canvas.onmousedown = (event: MouseEvent) => {
      const { x, y } = this.getIntegerLocation(event, this.map)
      // Get the ID of the selected unit
      let selectedID
      this.map.originalBodies.forEach(function (body: MapUnit, id: number) {
        if (x == body.x && y == body.y) {
          selectedID = id
        }
      })
      this.map.symmetricBodies.forEach(function (body: MapUnit, id: number) {
        if (x == body.x && y == body.y) {
          selectedID = id
        }
      })

      if (selectedID) {
        this.onclickUnit(selectedID)
      } else {
        this.onclickBlank(x, y)
      }

      interval = window.setInterval(whilemousedown, 50)
    }

    this.canvas.onmouseup = () => {
      clearInterval(interval)
    }

    this.canvas.onmousemove = (event) => {
      const { x, y } = this.getIntegerLocation(event, this.map)
      this.onMouseover(x, y, this.map.walls[(y) * this.width + x], this.map.resource_wells[y * this.width + x])
      hoverPos = { x: x, y: y }
    }

    this.canvas.onmouseout = (event) => {
      hoverPos = null
      clearInterval(interval)
    }
  }

  private getIntegerLocation(event: MouseEvent, map: GameMap) {
    let x = map.width * event.offsetX / this.canvas.offsetWidth
    let y = this.flip(map.height * event.offsetY / this.canvas.offsetHeight, map.height)
    return { x: Math.floor(x), y: Math.floor(y) }
  }

  /**
   * Draws an image centered at (x, y) with the given radius
   */
  private drawImage(img: HTMLImageElement, x: number, y: number, radius: number) {
    this.ctx['imageSmoothingEnabled'] = false
    this.ctx.drawImage(img, x, y - radius * 2, radius * 2, radius * 2)
  }

  private stringToColor(str) {
    var hash = 0
    for (var i = 0; i < str.length; i++) {
      hash = str.charCodeAt(i) + ((hash << 5) - hash)
    }
    var color = '#'
    for (var i = 0; i < 3; i++) {
      var value = (hash >> (i * 8)) & 0xFF
      color += ('00' + value.toString(16)).substr(-2)
    }
    return color
  }
}

import * as config from '../../config'
import * as cst from '../../constants'

import { GameWorld, schema } from 'battlecode-playback'
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
  readonly onMouseover: (x: number, y: number, rubble: number, lead: number) => void
  readonly onDrag: (x, y) => void

  // Other useful values
  readonly bgPattern: CanvasPattern
  private width: number // in world units
  private height: number // in world units

  private map: GameMap //the current map

  constructor(canvas: HTMLCanvasElement, imgs: AllImages, conf: config.Config,
    onclickUnit: (id: number) => void, onclickBlank: (x: number, y: number) => void,
    onMouseover: (x: number, y: number, rubble: number, lead: number) => void,
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
    this.renderObstacles(map)
    this.renderIslands(map)
    // restore default rendering
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
        this.renderTile(i, j, wall)

      }
    }
  }

  private renderObstacles(map: GameMap): void {
    for (let i = 0; i < this.width; i++) {
      for (let j = 0; j < this.height; j++) {
        if (map.clouds[(map.height - j - 1) * this.width + i])
          this.renderOverlay(i, j, "white", .3)
        if (map.currents[(map.height - j - 1) * this.width + i]){
          this.renderOverlay(i, j, "purple", .2)
          this.renderArrow(i,j,map.currents[(map.height - j - 1) * this.width + i])
        }
      }
    }
  }

  private renderIslands(map: GameMap): void {
    for (let i = 0; i < this.width; i++) {
      for (let j = 0; j < this.height; j++) {
        if (map.islands[(map.height - j - 1) * this.width + i] != 0) {
          this.renderIsland(i, j, map.islands[(map.height - j - 1) * this.width + i])
        }
      }
    }
  }

  private renderTile(i: number, j: number, wall: boolean) {
    this.ctx.save()
    const scale = 20
    this.ctx.scale(1 / scale, 1 / scale)
    const tileImg = this.imgs.tiles[wall ? 1 : 0]
    this.ctx.drawImage(tileImg, i * scale, j * scale, scale, scale)
    this.ctx.strokeStyle = 'gray'
    this.ctx.globalAlpha = 1
    this.ctx.strokeRect(i * scale, j * scale, scale, scale)
    this.ctx.restore()
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
    this.ctx.globalAlpha = 1
    this.ctx.fillStyle = "black"
    this.ctx.beginPath()

    let dir = cst.DIRECTIONS[direction]
    let len = scale / Math.sqrt(dir[0]*dir[0] + dir[1] * dir[1])
    let x = (i+.5) * scale
    let y = (j+.5) * scale

    this.ctx.moveTo(x + dir[0] * len, y + dir[0] * len)
    let right = [-dir[1]/2, dir[0]/2]
    this.ctx.lineTo(x - dir[0] * len / 2 + right[0], y - dir[1] * len / 2 + right[1])
    this.ctx.lineTo(x - dir[0] * len / 2 - right[0], y - dir[1] * len / 2 - right[1])
    this.ctx.closePath()
    this.ctx.fill()
    this.ctx.restore()
  }

  private renderIsland(i: number, j: number, island: number) {
    this.ctx.save()
    const scale = 20
    this.ctx.scale(1 / scale, 1 / scale)
    this.ctx.fillStyle = "black"
    this.ctx.globalAlpha = .3
    // this.ctx.fillRect(i * scale, j * scale, scale, scale)
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
    this.ctx.fillText(island + "", i * scale, (j + .5) * scale, scale * 1.7)
    this.ctx.restore()
  }

  private renderResources(map: GameMap) {
    this.ctx.save()
    this.ctx.globalAlpha = 1

    const adamantiumImg = this.imgs.resources[cst.ADAMANTIUM]
    const manaImg = this.imgs.resources[cst.MANA]
    const scale = 1

    for (let i = 0; i < this.width; i++) for (let j = 0; j < this.height; j++) {
      const resource = map.resource_wells[(map.height - j - 1) * this.width + i]

      this.ctx.globalAlpha = 1
      const cx = i * scale, cy = j * scale

      if (resource == 1) {
        let size = 1
        this.ctx.drawImage(adamantiumImg, cx + (1 - size) / 2, cy + (1 - size) / 2, scale * size, scale * size)

        this.ctx.strokeStyle = '#59727d'
        this.ctx.lineWidth = 1 / 30
        this.ctx.strokeRect(cx + .05, cy + .05, scale * .9, scale * .9)
      }
      if (resource == 2) {
        let size = 1
        this.ctx.drawImage(manaImg, cx + (1 - size) / 2, cy + (1 - size) / 2, scale * size, scale * size)

        this.ctx.strokeStyle = '#59727d'
        this.ctx.lineWidth = 1 / 30
        this.ctx.strokeRect(cx + .05, cy + .05, scale * .9, scale * .9)
      }
    }

    this.ctx.restore()
  }

  /**
   * Draw trees and units on the canvas
   */
  private renderBodies(map: GameMap) {

    this.ctx.fillStyle = "#84bf4b"
    map.originalBodies.forEach((body: MapUnit) => {
      this.renderBody(body)
      // this.drawGoodies(x, y, radius, body.containedBullets, body.containedBody);
    })

    map.symmetricBodies.forEach((body: MapUnit) => {
      this.renderBody(body)
      // this.drawGoodies(x, y, radius, body.containedBullets, body.containedBody);
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
      //this.onMouseover(x, y, this.map.rubble[(y)*this.width + x], this.map.leadVals[y*this.width + x]);
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
}

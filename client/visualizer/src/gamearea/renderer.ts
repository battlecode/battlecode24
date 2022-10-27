import * as config from '../config'
import * as cst from '../constants'
import NextStep from './nextstep'

import { GameWorld, Metadata, schema, Game } from 'battlecode-playback'
import { AllImages } from '../imageloader'
import Victor = require('victor')
import { constants } from 'buffer'

/**
 * Renders the world.
 *
 * Note that all rendering functions draw in world-units,
 */
export default class Renderer {

  readonly ctx: CanvasRenderingContext2D

  // For rendering robot information on click
  private lastSelectedID: number
  // position of mouse cursor hovering
  private hoverPos: { xrel: number, yrel: number } | null = null;

  private lastTime: number = 0;
  private lastAnomaly: number = 0;

  constructor(readonly canvas: HTMLCanvasElement, readonly imgs: AllImages, private conf: config.Config, readonly metadata: Metadata,
    readonly onRobotSelected: (id: number) => void,
    readonly onMouseover: (x: number, y: number, xrel: number, yrel: number, walls: number, resource: { type: number, adamantium: number, mana: number, elixir: number, upgraded: boolean }) => void) {
      let ctx = canvas.getContext("2d")
      if (ctx === null) {
        throw new Error("Couldn't load canvas2d context")
      } else {
        this.ctx = ctx
      }
      this.ctx['imageSmoothingEnabled'] = false
      //this.ctx.imageSmoothingQuality = "high"
    }

  /**
   * world: world to render
   * viewMin: min corner of view (in world units)
   * viewMax: max corner of view (in world units)
   * timeDelta: real time passed since last call to render
   * nextStep: contains positions of bodies after the next turn
   * lerpAmount: fractional progress between this turn and the next
   */
  render(world: GameWorld, viewMin: Victor, viewMax: Victor, curTime: number, nextStep?: NextStep, lerpAmount?: number) {
    // setup correct rendering
    const viewWidth = viewMax.x - viewMin.x
    const viewHeight = viewMax.y - viewMin.y
    const scale = this.canvas.width / (!this.conf.doingRotate ? viewWidth : viewHeight)

    this.ctx.save()
    this.ctx.scale(scale, scale)
    if (!this.conf.doingRotate) this.ctx.translate(-viewMin.x, -viewMin.y)
    else this.ctx.translate(-viewMin.y, -viewMin.x)

    this.renderBackground(world)
    this.renderResources(world)

    this.renderBodies(world, curTime, nextStep, lerpAmount)
    this.renderHoverBox(world)

    this.renderIndicatorDotsLines(world)
    this.setMouseoverEvent(world)

    // restore default rendering
    this.ctx.restore()
  }

  /**
   * Release resources.
   */
  release() {
    // nothing to do yet?
  }

  private renderBackground(world: GameWorld) {
    this.ctx.save()
    this.ctx.fillStyle = "white"
    this.ctx.globalAlpha = 1

    let minX = world.minCorner.x
    let minY = world.minCorner.y
    let width = world.maxCorner.x - world.minCorner.x
    let height = world.maxCorner.y - world.minCorner.y

    const scale = 20

    this.ctx.scale(1 / scale, 1 / scale)

    // scale the background pattern
    if (!this.conf.doingRotate) this.ctx.fillRect(minX * scale, minY * scale, width * scale, height * scale)
    else this.ctx.fillRect(minY * scale, minX * scale, height * scale, width * scale)

    const map = world.mapStats

    for (let i = 0; i < width; i++) for (let j = 0; j < height; j++) {
      let idxVal = map.getIdx(i, j)
      let plotJ = height - j - 1

      const cx = (minX + i) * scale, cy = (minY + plotJ) * scale

      this.ctx.globalAlpha = 1

      // Fetch and draw tile image
      // const walls = cst.getLevel(map.walls[idxVal])
      const tileImg = this.imgs.tiles[map.walls[idxVal]]

      //since tiles arent completely opaque
      if (!this.conf.doingRotate) this.ctx.clearRect(cx, cy, scale + .5, scale + .5)
      else this.ctx.clearRect(cx, cy, scale + .5, scale + .5)

      //+1 is so that there arent gaps in the map
      if (!this.conf.doingRotate) this.ctx.drawImage(tileImg, cx, cy, scale + .5, scale + .5)
      else this.ctx.drawImage(tileImg, cy, cx, scale + .5, scale + .5)

      // Draw grid
      if (this.conf.showGrid) {
        this.ctx.strokeStyle = 'gray'
        this.ctx.globalAlpha = 1
        if (!this.conf.doingRotate) this.ctx.strokeRect(cx, cy, scale, scale)
        else this.ctx.strokeRect(cy, cx, scale, scale)
      }
    }

    // let i = world.mapStats.anomalyRounds.indexOf(world.turn);
    // const d = new Date();
    // let time = d.getTime();
    // if (this.conf.showAnomalies) {
    //   if (i != -1) {
    //     let anom = world.mapStats.anomalies[i] + schema.Action.ABYSS;
    //     let color = (anom === schema.Action.ABYSS) ? "Blue" : (anom === schema.Action.CHARGE) ? "Yellow" : (anom === schema.Action.FURY) ? "Red" : (anom === schema.Action.VORTEX) ? "Purple" : "White";
    //     this.ctx.strokeStyle = color;
    //     this.ctx.lineWidth = 15;
    //     this.ctx.globalAlpha = 0.5;
    //     this.ctx.strokeRect(0, 0, width*scale, height*scale);
    //     this.ctx.stroke();
    //     this.lastTime = time;
    //     this.lastAnomaly = anom;
    //   } else if (time - this.lastTime < 1000) {
    //     let anom = this.lastAnomaly;
    //     let color = (anom === schema.Action.ABYSS) ? "Blue" : (anom === schema.Action.CHARGE) ? "Yellow" : (anom === schema.Action.FURY) ? "Red" : (anom === schema.Action.VORTEX) ? "Purple" : "White";
    //     this.ctx.strokeStyle = color;
    //     this.ctx.lineWidth = 15;
    //     this.ctx.globalAlpha = 0.5;
    //     this.ctx.strokeRect(0, 0, width*scale, height*scale);
    //     this.ctx.stroke();
    //     //this.ctx.globalAlpha = 1;
    //   }
    // }

    this.ctx.restore()
  }

  private renderHoverBox(world: GameWorld) {
    if (this.hoverPos != null) {
      this.ctx.save()
      this.ctx.fillStyle = "white"
      this.ctx.globalAlpha = 1

      let minX = world.minCorner.x
      let minY = world.minCorner.y
      let width = world.maxCorner.x - world.minCorner.x
      let height = world.maxCorner.y - world.minCorner.y

      const scale = 20

      this.ctx.scale(1 / scale, 1 / scale)
      const { xrel: x, yrel: y } = this.hoverPos
      const cx = (minX + x) * scale, cy = (minY + (height - y - 1)) * scale
      this.ctx.strokeStyle = 'purple'
      this.ctx.lineWidth = 2
      this.ctx.globalAlpha = 1
      if (!this.conf.doingRotate) this.ctx.strokeRect(cx, cy, scale, scale)
      else this.ctx.strokeRect(cy, cx, scale, scale)
      this.ctx.restore()
    }
  }

  private renderResources(world: GameWorld) {
    this.ctx.save()
    this.ctx.globalAlpha = 1

    let minX = world.minCorner.x
    let minY = world.minCorner.y
    let width = world.maxCorner.x - world.minCorner.x
    let height = world.maxCorner.y - world.minCorner.y

    const map = world.mapStats
    const scale = 1

    const sigmoid = (x) => {
      return 1 / (1 + Math.exp(-x))
    }

    for (let i = 0; i < width; i++) for (let j = 0; j < height; j++) {
      let idxVal = map.getIdx(i, j)
      let plotJ = height - j - 1

      this.ctx.lineWidth = 1 / 20
      this.ctx.globalAlpha = 1

      const cx = (minX + i) * scale, cy = (minY + plotJ) * scale

      if (map.resources[idxVal] > 0) {
        // let size = sigmoid(map.goldVals[idxVal] / 50) * .94
        let size = map.resource_well_stats.get(idxVal)!.upgraded ? 1 : .75
        let img = this.imgs.resources[map.resources[idxVal]]
        if (!this.conf.doingRotate) this.ctx.drawImage(img, cx + (1 - size) / 2, cy + (1 - size) / 2, scale * size, scale * size)
        else this.ctx.drawImage(img, cy + (1 - size) / 2, cx + (1 - size) / 2, scale * size, scale * size)
      }
    }

    this.ctx.restore()
  }

  private renderBodies(world: GameWorld, curTime: number, nextStep?: NextStep, lerpAmount?: number) {
    const bodies = world.bodies
    const length = bodies.length
    const types = bodies.arrays.type
    const teams = bodies.arrays.team
    const hps = bodies.arrays.hp
    const ids = bodies.arrays.id
    const xs = bodies.arrays.x
    const ys = bodies.arrays.y
    const actions = bodies.arrays.action
    const targets = bodies.arrays.target
    const targetxs = bodies.arrays.targetx
    const targetys = bodies.arrays.targety
    const minY = world.minCorner.y
    const maxY = world.maxCorner.y - 1

    let nextXs: Int32Array, nextYs: Int32Array, realXs: Float32Array, realYs: Float32Array

    if (nextStep && lerpAmount) {
      nextXs = nextStep.bodies.arrays.x
      nextYs = nextStep.bodies.arrays.y
      lerpAmount = lerpAmount || 0
    }

    // Calculate the real xs and ys
    realXs = new Float32Array(length)
    realYs = new Float32Array(length)
    for (let i = 0; i < length; i++) {
      if (nextStep && lerpAmount) {
        // Interpolated
        // @ts-ignore
        realXs[i] = xs[i] + (nextXs[i] - xs[i]) * lerpAmount
        // @ts-ignore
        realYs[i] = this.flip(ys[i] + (nextYs[i] - ys[i]) * lerpAmount, minY, maxY)
      } else {
        // Not interpolated
        realXs[i] = xs[i]
        realYs[i] = this.flip(ys[i], minY, maxY)
      }
    }

    // Render the robots
    // render images with priority last to have them be on top of other units.

    const drawEffect = (effect: string, x: number, y: number) => {
      const effectImgs: HTMLImageElement[] = this.imgs.effects[effect]
      const whichImg = (Math.floor(curTime / cst.EFFECT_STEP) % effectImgs.length)
      const effectImg = effectImgs[whichImg]
      //this.drawBot(effectImg, x, y, 0)
    }

    const renderBot = (i: number) => {
      let img = this.imgs.robots[cst.bodyTypeToString(types[i])][teams[i]]
      let max_hp = this.metadata.types[types[i]].health
      this.drawBot(img, realXs[i], realYs[i], hps[i], hps[i] / max_hp, cst.bodyTypeToSize(types[i]))
      this.drawSightRadii(realXs[i], realYs[i], types[i], ids[i] === this.lastSelectedID)

      // // draw effect
      // if (actions[i] == schema.Action.ATTACK) {
      //   let yshift = (teams[i] - 1.5) * .15 + 0.5
      //   let xshift = (teams[i] - 1.5) * .15 + 0.5
      //   this.ctx.save()
      //   this.ctx.beginPath()
      //   this.ctx.moveTo(realXs[i] + xshift, realYs[i] + yshift)
      //   this.ctx.lineTo(targetxs[i] + xshift, this.flip(targetys[i], minY, maxY) + yshift)
      //   this.ctx.strokeStyle = teams[i] == 1 ? 'red' : 'blue'
      //   this.ctx.lineWidth = 0.05
      //   this.ctx.stroke()
      //   this.ctx.restore()
      // }

      // if (this.conf.showAnomalies) {
      //   if (actions[i] == schema.Action.LOCAL_ABYSS || actions[i] == schema.Action.LOCAL_CHARGE || actions[i] == schema.Action.LOCAL_FURY) {
      //     this.ctx.save()
      //     this.ctx.globalAlpha = 1
      //     this.ctx.beginPath()
      //     this.ctx.arc(realXs[i] + 0.5, realYs[i] + 0.5, Math.sqrt(this.metadata.types[types[i]].actionRadiusSquared), 0, 2 * Math.PI, false)
      //     //this.ctx.fillStyle = actions[i] == schema.Action.LOCAL_ABYSS ? "purple" : actions[i] == schema.Action.LOCAL_CHARGE ? "yellow" : "red";
      //     this.ctx.setLineDash([1, 2])
      //     this.ctx.strokeStyle = teams[i] == 1 ? 'red' : 'blue'
      //     this.ctx.stroke()

      //     this.ctx.beginPath()
      //     this.ctx.arc(realXs[i] + 0.5, realYs[i] + 0.5, Math.sqrt(this.metadata.types[types[i]].actionRadiusSquared), 0, 2 * Math.PI, false)
      //     this.ctx.strokeStyle = (actions[i] === schema.Action.LOCAL_ABYSS) ? "Blue" : (actions[i] === schema.Action.LOCAL_CHARGE) ? "Yellow" : (actions[i] === schema.Action.LOCAL_FURY) ? "Red" : "White"
      //     this.ctx.setLineDash([1, 1.5])
      //     this.ctx.stroke()

      //     this.ctx.globalAlpha = 1
      //     this.ctx.restore()
      //   }
      // }

      // if (actions[i] == schema.Action.REPAIR) {
      //   let yshift = 0.5
      //   let xshift = 0.5
      //   this.ctx.save()
      //   this.ctx.beginPath()
      //   this.ctx.moveTo(realXs[i] + xshift, realYs[i] + yshift)
      //   this.ctx.lineTo(targetxs[i] + xshift, this.flip(targetys[i], minY, maxY) + yshift)
      //   this.ctx.strokeStyle = '#54FF79'
      //   this.ctx.lineWidth = 0.075
      //   this.ctx.stroke()
      //   this.ctx.restore()
      // };

      // TODO: handle abilities/actions
      // let effect: string | null = cst.abilityToEffectString(abilities[i]);
      // if (effect !== null) drawEffect(effect, realXs[i], realYs[i]);
    }

    let priorityIndices: number[] = []

    for (let i = 0; i < length; i++) {
      if (cst.bodyTypePriority.includes(types[i])) {
        priorityIndices.push(i)
        continue
      }
      renderBot(i)
    }

    priorityIndices.forEach((i) => renderBot(i))

    this.setInfoStringEvent(world, xs, ys)
  }

  /**
   * Returns the mirrored y coordinate to be consistent with (0, 0) in the
   * bottom-left corner (top-left corner is canvas default).
   * params: y coordinate to flip
   *         yMin coordinate of the minimum edge
   *         yMax coordinate of the maximum edge
   */
  private flip(y: number, yMin: number, yMax: number) {
    return yMin + yMax - y
  }

  /**
   * Draws a cirlce centered at (x,y) with given squared radius and color.
   */
  private drawBotRadius(x: number, y: number, radiusSquared: number, color: string) {
    if (this.conf.doingRotate) [x, y] = [y, x]
    this.ctx.beginPath()
    this.ctx.arc(x + 0.5, y + 0.5, Math.sqrt(radiusSquared), 0, 2 * Math.PI)
    this.ctx.strokeStyle = color
    this.ctx.lineWidth = cst.SIGHT_RADIUS_LINE_WIDTH
    this.ctx.stroke()
  }

  /**
   * Draws the sight radii of the robot.
   */
  private drawSightRadii(x: number, y: number, type: schema.BodyType, single?: Boolean) {
    // handle bots with no radius here, if necessary
    if (this.conf.seeActionRadius || single) {
      this.drawBotRadius(x, y, this.metadata.types[type].actionRadiusSquared, cst.ACTION_RADIUS_COLOR)
    }

    if (this.conf.seeVisionRadius || single) {
      this.drawBotRadius(x, y, this.metadata.types[type].visionRadiusSquared, cst.VISION_RADIUS_COLOR)
    }
  }

  /**
   * Draws an image centered at (x, y) with the given radius
   */
  private drawImage(img: HTMLImageElement, x: number, y: number, radius: number) {
    if (this.conf.doingRotate) [x, y] = [y, x]
    this.ctx.drawImage(img, x - radius, y - radius, radius * 2, radius * 2)
  }

  /**
   * Draws an image centered at (x, y), such that an image with default size covers a 1x1 cell
   */
  private drawBot(img: HTMLImageElement, x: number, y: number, c: number, ratio: number, img_size: number) {
    if (this.conf.doingRotate) [x, y] = [y, x]
    let realWidth = img.naturalWidth / img_size
    let realHeight = img.naturalHeight / img_size
    const sigmoid = (x) => {
      return 1 / (1 + Math.exp(-x))
    }
    //this.ctx.filter = `brightness(${sigmoid(c - 100) * 30 + 90}%)`;
    let size = ratio * 0.5 + 0.5
    this.ctx.drawImage(img, x + (1 - realWidth * size) / 2, y + (1 - realHeight * size) / 2, realWidth * size, realHeight * size)
    this.ctx.beginPath()
    this.ctx.moveTo(x + realWidth * size / 2 - realWidth * ratio / 2, y + 1)
    this.ctx.lineTo(x + realWidth * size / 2 + realWidth * ratio / 2, y + 1)
    this.ctx.strokeStyle = "green"
    this.ctx.lineWidth = cst.SIGHT_RADIUS_LINE_WIDTH
    this.ctx.stroke()
  }

  private setInfoStringEvent(world: GameWorld,
    xs: Int32Array, ys: Int32Array) {
    // world information
    const length = world.bodies.length
    const width = world.maxCorner.x - world.minCorner.x
    const height = world.maxCorner.y - world.minCorner.y
    const ids: Int32Array = world.bodies.arrays.id
    // TODO: why is this Int8Array and not Int32Array?
    const types: Int8Array = world.bodies.arrays.type
    // const radii: Float32Array = world.bodies.arrays.radius;
    const onRobotSelected = this.onRobotSelected

    this.canvas.onmousedown = (event: MouseEvent) => {
      const { x, y } = this.getIntegerLocation(event, world)

      // Get the ID of the selected robot
      let selectedRobotID
      let possiblePriorityID: number | undefined = undefined
      for (let i = 0; i < length; i++) {
        if (xs[i] == x && ys[i] == y) {
          selectedRobotID = ids[i]
          if (cst.bodyTypePriority.includes(types[i]))
            possiblePriorityID = ids[i]
        }
      }

      // if there are two robots in same cell, choose the one with priority
      if (possiblePriorityID != undefined) selectedRobotID = possiblePriorityID
      // Set the info string even if the robot is undefined
      this.lastSelectedID = selectedRobotID
      onRobotSelected(selectedRobotID)
    }
  }

  private setMouseoverEvent(world: GameWorld) {
    // world information
    // const width = world.maxCorner.x - world.minCorner.x;
    // const height = world.maxCorner.y - world.minCorner.y;
    const onMouseover = this.onMouseover
    // const minY = world.minCorner.y;
    // const maxY = world.maxCorner.y - 1;

    if (this.hoverPos) {
      const { xrel, yrel } = this.hoverPos
      const x = xrel + world.minCorner.x
      const y = yrel + world.minCorner.y
      const idx = world.mapStats.getIdx(xrel, yrel)
      onMouseover(x, y, xrel, yrel, world.mapStats.walls[idx], world.mapStats.resources[idx])
    }

    this.canvas.onmousemove = (event) => {
      // const x = width * event.offsetX / this.canvas.offsetWidth + world.minCorner.x;
      // const _y = height * event.offsetY / this.canvas.offsetHeight + world.minCorner.y;
      // const y = this.flip(_y, minY, maxY)

      // Set the location of the mouseover
      const { x, y } = this.getIntegerLocation(event, world)
      const xrel = x - world.minCorner.x
      const yrel = y - world.minCorner.y
      const idx = world.mapStats.getIdx(xrel, yrel)
      onMouseover(x, y, xrel, yrel, world.mapStats.walls[idx], world.mapStats.resources[idx])
      this.hoverPos = { xrel: xrel, yrel: yrel }
    }

    this.canvas.onmouseout = (event) => {
      this.hoverPos = null
    }
  }

  private getIntegerLocation(event: MouseEvent, world: GameWorld) {
    const width = world.maxCorner.x - world.minCorner.x
    const height = world.maxCorner.y - world.minCorner.y
    const minY = world.minCorner.y
    const maxY = world.maxCorner.y - 1
    var _x: number
    var _y: number
    if (!this.conf.doingRotate) {
      _x = width * event.offsetX / this.canvas.offsetWidth + world.minCorner.x
      _y = height * event.offsetY / this.canvas.offsetHeight + world.minCorner.y
      _y = this.flip(_y, minY, maxY)
    }
    else {
      _y = (world.maxCorner.y - world.minCorner.y - 1) - height * event.offsetX / this.canvas.offsetWidth + world.minCorner.y
      _x = width * event.offsetY / this.canvas.offsetHeight + world.minCorner.x
    }
    return { x: Math.floor(_x), y: Math.floor(_y + 1) }
  }

  private renderIndicatorDotsLines(world: GameWorld) {
    if (!this.conf.indicators && !this.conf.allIndicators) {
      return
    }

    const dots = world.indicatorDots
    const lines = world.indicatorLines

    // Render the indicator dots
    const dotsID = dots.arrays.id
    const dotsX = dots.arrays.x
    const dotsY = dots.arrays.y
    const dotsRed = dots.arrays.red
    const dotsGreen = dots.arrays.green
    const dotsBlue = dots.arrays.blue
    const minY = world.minCorner.y
    const maxY = world.maxCorner.y - 1

    console.log(dots.length)

    for (let i = 0; i < dots.length; i++) {
      if (dotsID[i] === this.lastSelectedID || this.conf.allIndicators) {
        const red = dotsRed[i]
        const green = dotsGreen[i]
        const blue = dotsBlue[i]
        const x = dotsX[i]
        const y = this.flip(dotsY[i], minY, maxY)

        this.ctx.beginPath()
        this.ctx.arc(x + 0.5, y + 0.5, cst.INDICATOR_DOT_SIZE, 0, 2 * Math.PI, false)
        this.ctx.fillStyle = `rgb(${red}, ${green}, ${blue})`
        this.ctx.fill()
      }
    }

    // Render the indicator lines
    const linesID = lines.arrays.id
    const linesStartX = lines.arrays.startX
    const linesStartY = lines.arrays.startY
    const linesEndX = lines.arrays.endX
    const linesEndY = lines.arrays.endY
    const linesRed = lines.arrays.red
    const linesGreen = lines.arrays.green
    const linesBlue = lines.arrays.blue
    this.ctx.lineWidth = cst.INDICATOR_LINE_WIDTH

    for (let i = 0; i < lines.length; i++) {
      if (linesID[i] === this.lastSelectedID || this.conf.allIndicators) {
        const red = linesRed[i]
        const green = linesGreen[i]
        const blue = linesBlue[i]
        const startX = linesStartX[i] + 0.5
        const startY = this.flip(linesStartY[i], minY, maxY) + 0.5
        const endX = linesEndX[i] + 0.5
        const endY = this.flip(linesEndY[i], minY, maxY) + 0.5

        this.ctx.beginPath()
        this.ctx.moveTo(startX, startY)
        this.ctx.lineTo(endX, endY)
        this.ctx.strokeStyle = `rgb(${red}, ${green}, ${blue})`
        this.ctx.stroke()
      }
    }
  }
}

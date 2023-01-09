import * as config from '../config'
import * as cst from '../constants'
import NextStep from './nextstep'

import { GameWorld, Metadata, schema, Game } from 'battlecode-playback'
import { AllImages } from '../imageloader'
import Victor = require('victor')
import { constants } from 'buffer'
import { CanvasType } from './gamearea'

/**
 * Renders the world.
 *
 * Note that all rendering functions draw in world-units,
 */
export default class Renderer {

  readonly ctx: Record<CanvasType, CanvasRenderingContext2D>

  // For rendering robot information on click
  private lastSelectedID: number
  // position of mouse cursor hovering
  private hoverPos: { xrel: number, yrel: number } | null = null;

  private staticRendered = false;

  constructor(
    readonly canvases: Record<CanvasType, HTMLCanvasElement>, readonly imgs: AllImages, private conf: config.Config, readonly metadata: Metadata,
    readonly onRobotSelected: (id: number) => void,
    readonly onMouseover: (x: number, y: number, xrel: number, yrel: number, resource: number,
      well_stats: { adamantium: number, mana: number, elixir: number, upgraded: boolean },
      island_stats: { owner: number, flip_progress: number, locations: number[], is_accelerated: boolean, accelerated_tiles: Set<number> } | undefined) => void
  ) {
    this.ctx = {} as Record<CanvasType, CanvasRenderingContext2D>
    for (let key in canvases) {
      const canvas = canvases[key]
      let ctx = canvas.getContext("2d")
      ctx['imageSmoothingEnabled'] = false
      //ctx.imageSmoothingQuality = "high"
      if (ctx === null) {
        throw new Error("Couldn't load canvas2d context")
      } else {
        this.ctx[key] = ctx
      }
    }
  }

  /**
   * world: world to render
   * viewMin: min corner of view (in world units)
   * viewMax: max corner of view (in world units)
   * timeDelta: real time passed since last call to render
   * nextStep: contains positions of bodies after the next turn
   * lerpAmount: fractional progress between this turn and the next
   */
  render(world: GameWorld, viewMin: Victor, viewMax: Victor, curTime: number, nextStep?: NextStep, lerpAmount?: number, selectedTrail: { x: number, y: number }[] | undefined = undefined) {
    // setup correct rendering
    for (let key in this.ctx) {
      const canvas = this.canvases[key]
      const ctx = this.ctx[key]

      const viewWidth = viewMax.x - viewMin.x
      const viewHeight = viewMax.y - viewMin.y
      const scale = canvas.width / (!this.conf.doingRotate ? viewWidth : viewHeight)

      ctx.save()
      ctx.scale(scale, scale)
      if (!this.conf.doingRotate) ctx.translate(-viewMin.x, -viewMin.y)
      else ctx.translate(-viewMin.y, -viewMin.x)
    }

    if (!this.staticRendered) {
      this.staticRendered = true

      this.clearCanvas(CanvasType.BACKGROUND)
      this.renderBackground(world)

      this.clearCanvas(CanvasType.OVERLAY)
      this.renderObstacles(world)
    }

    const updateDynamic = true
    if (updateDynamic) {
      this.clearCanvas(CanvasType.DYNAMIC)
      this.renderIslands(world)
      this.renderResources(world)
      //if (selectedTrail)
      //  this.renderTrail(world, selectedTrail)
      this.renderBodies(world, curTime, nextStep, lerpAmount)
      if (this.lastSelectedID)
        this.renderPath(world)
      this.renderEffects(world)
      this.renderHoverBox(world)
      this.renderIndicatorDotsLines(world)

    }


    this.setMouseoverEvent(world)

    // restore default rendering
    for (let key in this.ctx)
      this.ctx[key].restore()
  }

  redrawStatic() {
    this.staticRendered = false
  }

  /**
   * Release resources.
   */
  release() {
    // nothing to do yet?
  }

  private clearCanvas(canvasType: CanvasType) {
    const canvas = this.canvases[canvasType]
    this.ctx[canvasType].clearRect(0, 0, canvas.width, canvas.height)
  }

  private renderBackground(world: GameWorld) {
    const ctx = this.ctx[CanvasType.BACKGROUND]
    ctx.globalAlpha = 1
    let minX = world.minCorner.x, minY = world.minCorner.y
    let width = world.maxCorner.x - world.minCorner.x, height = world.maxCorner.y - world.minCorner.y

    ctx.fillStyle = "#BAAD99"
    if (!this.conf.doingRotate) ctx.fillRect(minX, minY, width, height)
    else ctx.fillRect(minY, minX, height, width)

    const map = world.mapStats
    for (let i = 0; i < width; i++) for (let j = 0; j < height; j++) {
      let idxVal = map.getIdx(i, j)
      let plotJ = height - j - 1
      const cx = (minX + i), cy = (minY + plotJ)
      ctx.globalAlpha = 1

      if (map.walls[idxVal]) {
        ctx.save()
        let path = this.get9SliceClipPath(i, j, map.walls, height, width)
        this.applyClipScaled(ctx, cx / 1.01, cy / 1.01, 1.01, path)
        ctx.fillStyle = cst.UI_GREY
        if (!this.conf.doingRotate) ctx.fillRect(cx, cy, 1.01, 1.01)
        else ctx.fillRect(cy, cx, 1.01, 1.01)
        ctx.restore()
      }

      // Draw grid
      if (this.conf.showGrid) {
        ctx.strokeStyle = 'black'
        ctx.globalAlpha = .05
        let thickness = .02
        ctx.lineWidth = thickness
        if (!this.conf.doingRotate) ctx.strokeRect(cx + thickness / 2, cy + thickness / 2, 1 - thickness, 1 - thickness)
        else ctx.strokeRect(cy + thickness / 2, cx + thickness / 2, 1 - thickness, 1 - thickness)
      }
    }
  }

  private renderHoverBox(world: GameWorld) {
    const ctx = this.ctx[CanvasType.DYNAMIC]

    if (this.hoverPos != null) {
      ctx.save()
      ctx.fillStyle = "white"
      ctx.globalAlpha = 1

      let minX = world.minCorner.x
      let minY = world.minCorner.y
      let width = world.maxCorner.x - world.minCorner.x
      let height = world.maxCorner.y - world.minCorner.y

      const scale = 20

      ctx.scale(1 / scale, 1 / scale)
      const { xrel: x, yrel: y } = this.hoverPos
      const cx = (minX + x) * scale, cy = (minY + (height - y - 1)) * scale
      ctx.strokeStyle = "black"
      ctx.lineWidth = 1
      ctx.globalAlpha = .2
      if (!this.conf.doingRotate) ctx.strokeRect(cx, cy, scale, scale)
      else ctx.strokeRect(cy, cx, scale, scale)
      ctx.restore()
    }
  }

  private renderEffects(world: GameWorld) {
    let width = world.maxCorner.x - world.minCorner.x
    let height = world.maxCorner.y - world.minCorner.y

    for (var effect of world.mapStats.effects) {
      let color = 'white'
      if (effect.type == 'destabilize') {
        color = effect.turns_remaining == 0 ? "#D53E433F" : "#573f5e3F"
      }
      if (effect.type == 'boost') {
        color = "#00F0003F"
      }
      this.drawCircle(effect.x, height - effect.y - 1, 25, color, cst.TEAM_COLORS[effect.team])
    }

    //accelerated islands
    world.mapStats.island_stats.forEach((island_stat, key) => {
      if (island_stat.is_accelerated) {
        island_stat.accelerated_tiles.forEach(tile_loc => {
          let tile_x = tile_loc % width
          let tile_y = (tile_loc - tile_x) / width
          const ctx = this.ctx[CanvasType.DYNAMIC]
          ctx.fillStyle = cst.TEAM_COLORS[island_stat.owner - 1]
          ctx.globalAlpha = .1
          ctx.fillRect(tile_x, (height - tile_y - 1), 1, 1)
        })
      }
    })
  }

  private renderResources(world: GameWorld) {
    const ctx = this.ctx[CanvasType.DYNAMIC]
    ctx.globalAlpha = 1
    let minX = world.minCorner.x
    let minY = world.minCorner.y
    let width = world.maxCorner.x - world.minCorner.x
    let height = world.maxCorner.y - world.minCorner.y
    const map = world.mapStats
    for (let i = 0; i < width; i++) for (let j = 0; j < height; j++) {
      let idxVal = map.getIdx(i, j)
      let plotJ = height - j - 1

      ctx.lineWidth = 1 / 20
      ctx.globalAlpha = 1
      const cx = (minX + i), cy = (minY + plotJ)

      if (map.resources[idxVal] > 0) {
        let upgraded = map.resource_well_stats.get(idxVal)!.upgraded
        let size = upgraded ? .95 : .85
        let img = this.imgs.resource_wells[map.resources[idxVal]][upgraded ? 1 : 0]
        if (!this.conf.doingRotate) ctx.drawImage(img, cx + (1 - size) / 2, cy + (1 - size) / 2, size, size)
        else ctx.drawImage(img, cy + (1 - size) / 2, cx + (1 - size) / 2, size, size)

        //resource outline
        ctx.strokeStyle = '#8d6544'
        ctx.globalAlpha = .1
        let inset = .03
        ctx.lineWidth = .02
        if (!this.conf.doingRotate) ctx.strokeRect(cx + inset, cy + inset, 1 - 2 * inset, 1 - 2 * inset)
        else ctx.strokeRect(cy + inset, cx + 2 * inset, 1 - 2 * inset, 1 - 2 * inset)
      }
    }
  }

  private renderArrow(i: number, j: number, direction: number, ctx: CanvasRenderingContext2D) {
    ctx.globalAlpha = .1
    ctx.fillStyle = "black"
    ctx.beginPath()

    let dir = cst.DIRECTIONS[direction]
    let len = 1 / Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1]) * .4
    let x = (i + .5)
    let y = (j + .5)

    ctx.moveTo(x + dir[0] * len, y + dir[1] * len)
    let right = [-dir[1] * len / 2, dir[0] * len / 2]
    ctx.lineTo(x - dir[0] * len * .7 + right[0], y - dir[1] * len * .7 + right[1])
    ctx.lineTo(x - dir[0] * len * .7 - right[0], y - dir[1] * len * .7 - right[1])
    ctx.closePath()
    ctx.fill()
  }

  private renderObstacles(world: GameWorld): void {
    const ctx = this.ctx[CanvasType.OVERLAY]
    ctx.globalAlpha = 1
    let minX = world.minCorner.x, minY = world.minCorner.y
    let width = world.maxCorner.x - world.minCorner.x, height = world.maxCorner.y - world.minCorner.y
    const map = world.mapStats
    for (let i = 0; i < width; i++) for (let j = 0; j < height; j++) {
      let idxVal = map.getIdx(i, j)
      const cx = (minX + i), cy = (minY + height - j - 1)

      if (map.clouds[idxVal]) {
        ctx.save()
        ctx.globalAlpha = .3
        ctx.fillStyle = "white"
        let path = this.get9SliceClipPath(i, j, map.clouds, height, width)
        this.applyClipScaled(ctx, cx / 1.01, cy / 1.01, 1.01, path)
        ctx.fillRect(cx, cy, 1.01, 1.01)
        ctx.restore()
      }
      if (map.currents[idxVal]) {
        ctx.save()
        ctx.globalAlpha = .2
        ctx.fillStyle = "purple"
        let path = this.get9SliceClipPath(i, j, map.currents, height, width, (c) => c == map.currents[idxVal])
        this.applyClipScaled(ctx, cx / 1.01, cy / 1.01, 1.01, path)
        ctx.fillRect(cx, cy, 1.01, 1.01)
        ctx.restore()
        this.renderArrow(cx, cy, map.currents[idxVal], ctx)
      }
    }
  }

  private renderIslands(world: GameWorld): void {
    const ctx = this.ctx[CanvasType.DYNAMIC]
    ctx.globalAlpha = 1
    let minX = world.minCorner.x, minY = world.minCorner.y
    let width = world.maxCorner.x - world.minCorner.x, height = world.maxCorner.y - world.minCorner.y
    const map = world.mapStats
    for (let i = 0; i < width; i++) for (let j = 0; j < height; j++) {
      let idxVal = map.getIdx(i, j)
      const cx = (minX + i), cy = (minY + height - j - 1)

      if (map.islands[idxVal]) {
        ctx.save()
        let path = this.get9SliceClipPath(i, j, map.islands, height, width)
        this.applyClipScaled(ctx, cx / 1.01, cy / 1.01, 1.01, path)
        let island_stat = map.island_stats.get(map.islands[idxVal])!
        if (!this.conf.doingRotate) this.drawIsland(cx / 1.01, cy / 1.01, 1.01, ctx, island_stat)
        else this.drawIsland(cy / 1.01, cx / 1.01, 1.01, ctx, island_stat)
        ctx.restore()
      }
    }
  }

  private drawIsland(i: number, j: number, scale: number, ctx: CanvasRenderingContext2D, island_stat: { owner: number; flip_progress: number; locations: number[]; is_accelerated: boolean }) {
    ctx.globalAlpha = .7

    const sigmoid = (x) => { return 1 / (1 + Math.exp(-x)) }
    const blendColors = (colorA, colorB, amount) => {
      const [rA, gA, bA] = colorA.match(/\w\w/g).map((c) => parseInt(c, 16))
      const [rB, gB, bB] = colorB.match(/\w\w/g).map((c) => parseInt(c, 16))
      const r = Math.round(rA + (rB - rA) * amount).toString(16).padStart(2, '0')
      const g = Math.round(gA + (gB - gA) * amount).toString(16).padStart(2, '0')
      const b = Math.round(bA + (bB - bA) * amount).toString(16).padStart(2, '0')
      return '#' + r + g + b
    }

    let first_color = '#666666'
    if (island_stat.owner != 0)
      first_color = blendColors(first_color, cst.TEAM_COLORS[island_stat.owner - 1], Math.min(1, sigmoid(island_stat.flip_progress / 15 - 2) + .3))

    let second_color = island_stat.is_accelerated ? "#EEAC09" : first_color

    let x = i * scale
    let y = j * scale
    let d = scale / 8

    ctx.fillStyle = first_color
    ctx.beginPath()
    ctx.moveTo(x, y)
    ctx.lineTo(x + d, y)
    ctx.lineTo(x, y + d)
    ctx.closePath()
    ctx.fill()

    ctx.fillStyle = second_color
    ctx.beginPath()
    ctx.moveTo(x + 3 * d, y)
    ctx.lineTo(x + 5 * d, y)
    ctx.lineTo(x, y + 5 * d)
    ctx.lineTo(x, y + 3 * d)
    ctx.closePath()
    ctx.fill()

    ctx.fillStyle = first_color
    ctx.beginPath()
    ctx.moveTo(x + 7 * d, y)
    ctx.lineTo(x + 8 * d, y)
    ctx.lineTo(x + 8 * d, y + d)
    ctx.lineTo(x + d, y + 8 * d)
    ctx.lineTo(x, y + 8 * d)
    ctx.lineTo(x, y + 7 * d)
    ctx.closePath()
    ctx.fill()

    ctx.fillStyle = second_color
    ctx.beginPath()
    ctx.moveTo(x + 5 * d, y + 8 * d)
    ctx.lineTo(x + 3 * d, y + 8 * d)
    ctx.lineTo(x + 8 * d, y + 3 * d)
    ctx.lineTo(x + 8 * d, y + 5 * d)
    ctx.closePath()
    ctx.fill()

    ctx.fillStyle = first_color
    ctx.beginPath()
    ctx.moveTo(x + 8 * d, y + 8 * d)
    ctx.lineTo(x + 7 * d, y + 8 * d)
    ctx.lineTo(x + 8 * d, y + 7 * d)
    ctx.closePath()
    ctx.fill()
  }

  private renderPath(world: GameWorld) {
    const path = world.pathHistory.get(this.lastSelectedID)
    if (!path || path.length < 2) return
    if (world.bodies.lookup(this.lastSelectedID).type == cst.HEADQUARTERS) return
    const ctx = this.ctx[CanvasType.DYNAMIC]
    const height = world.maxCorner.y - world.minCorner.y

    const startLineWidth = 0.15
    ctx.strokeStyle = "white"
    ctx.lineWidth = startLineWidth

    ctx.beginPath()
    this.drawCircle(path[0].x, height - path[0].y - 1, ctx.lineWidth / 10, "white", "white")
    ctx.moveTo(path[0].x + .5, (height - path[0].y - .5))
    for (let i = 1; i < path.length; i++) {
      ctx.globalAlpha = 1 / Math.sqrt(i)
      ctx.lineWidth = startLineWidth / Math.sqrt(i * 0.5)
      ctx.lineTo(path[i].x + .5, (height - path[i].y - .5))
      ctx.stroke()
      this.drawCircle(path[i].x, height - path[i].y - 1, ctx.lineWidth / 10, "white", "white")
      ctx.moveTo(path[i].x + .5, (height - path[i].y - .5))
    }
    ctx.globalAlpha = 1
  }

  private renderTrail(world: GameWorld, selectedTrail: { x: number, y: number }[]) {
    if (selectedTrail.length < 2) return
    const ctx = this.ctx[CanvasType.DYNAMIC]
    const height = world.maxCorner.y - world.minCorner.y

    ctx.save()
    ctx.strokeStyle = "black"
    ctx.lineWidth = .3
    ctx.lineCap = "round"
    ctx.lineJoin = "round"

    // selectedTrail = selectedTrail.filter((i, index) => {
    //   if (index != 0)
    //     return selectedTrail[index - 1].x != i.x || selectedTrail[index - 1].y != i.y
    //   return i
    // })

    let i = selectedTrail.length - 1

    let limit = 25
    let orig_i = i
    ctx.beginPath()
    ctx.moveTo(selectedTrail[i].x + .5, (height - selectedTrail[i].y - .5))
    for (let i = selectedTrail.length - 2; i >= 0; i--) {
      ctx.globalAlpha = 1 / (Math.sqrt(selectedTrail.length - i) * Math.min(limit, orig_i))
      ctx.lineTo(selectedTrail[i].x + .5, (height - selectedTrail[i].y - .5))
      ctx.stroke()
      // no inifinite trail
      if (orig_i - i > limit)
        break
    }
    ctx.restore()
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
    const adamantiums = bodies.arrays.adamantium
    const manas = bodies.arrays.mana
    const elixirs = bodies.arrays.elixir
    const prevAdamantiums = bodies.arrays.previous_adamantium
    const prevManas = bodies.arrays.previous_mana
    const prevElixirs = bodies.arrays.previous_elixir
    const normal_anchors = bodies.arrays.normal_anchors
    const accelerated_anchors = bodies.arrays.accelerated_anchors
    const minY = world.minCorner.y
    const maxY = world.maxCorner.y - 1


    const ctx = this.ctx[CanvasType.DYNAMIC]
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
        realXs[i] = xs[i] + (nextXs![i] - xs[i]) * lerpAmount
        realYs[i] = this.flip(ys[i] + (nextYs![i] - ys[i]) * lerpAmount, minY, maxY)
      } else {
        // Not interpolated
        realXs[i] = xs[i]
        realYs[i] = this.flip(ys[i], minY, maxY)
      }
    }

    // Render the robots
    // render images with priority last to have them be on top of other units.
    const renderBot = (i: number) => {
      let img = this.imgs.robots[types[i]][teams[i]]
      let bot_square_idx = xs[i] + ys[i] * (world.maxCorner.x - world.minCorner.x)
      let max_hp = this.metadata.types[types[i]].health
      this.drawBot(img, realXs[i], realYs[i], hps[i], Math.min(1, hps[i] / max_hp), cst.bodyTypeToSize(types[i]))

      //draw sight and action radiuses
      let selected = ids[i] === this.lastSelectedID
      if (this.conf.seeActionRadius || selected)
        this.drawBotRadius(realXs[i], realYs[i], this.metadata.types[types[i]].actionRadiusSquared, cst.ACTION_RADIUS_COLOR)
      let vis_radius = world.mapStats.clouds[bot_square_idx] ? 4 : this.metadata.types[types[i]].visionRadiusSquared
      if (this.conf.seeVisionRadius || selected)
        this.drawBotRadius(realXs[i], realYs[i], vis_radius, cst.VISION_RADIUS_COLOR)

      //draw rescoures
      const adamantiumColor = "#838D63"
      const manaColor = "#D79DA2"
      const elixirColor = "#FBCC3F"
      if (normal_anchors[i] > 0) {
        let anchorColor = "#6C6C6C"
        this.drawCircle(realXs[i] - 0.45, realYs[i] - 0.45, 0.005, anchorColor, "#00000088")
      }
      if (accelerated_anchors[i] > 0) {
        let anchorColor = "#EEAC09"
        this.drawCircle(realXs[i] - 0.2, realYs[i] - 0.45, 0.005, anchorColor, "#00000088")
      }
      if (adamantiums[i])
        this.drawCircle(realXs[i] + .05, realYs[i] - 0.5, 0.004, adamantiumColor, "#00000088")
      if (manas[i])
        this.drawCircle(realXs[i] + 0.25, realYs[i] - 0.5, 0.004, manaColor, "#00000088")
      if (elixirs[i])
        this.drawCircle(realXs[i] + 0.45, realYs[i] - 0.5, 0.004, elixirColor, "#00000088")

      // draw effect
      if (actions[i] == schema.Action.THROW_ATTACK || actions[i] == schema.Action.LAUNCH_ATTACK) {
        // Direction
        let yshift = (teams[i] - 1.5) * .15 + 0.5
        let xshift = (teams[i] - 1.5) * .15 + 0.5
        ctx.save()
        ctx.beginPath()
        const startX = realXs[i] + xshift
        const startY = realYs[i] + yshift
        const endX = targetxs[i] + xshift
        const endY = this.flip(targetys[i], minY, maxY) + yshift

        // Line
        ctx.moveTo(startX, startY)
        ctx.lineTo(endX, endY)
        ctx.strokeStyle = teams[i] == 1 ? 'red' : 'blue'
        ctx.lineWidth = 0.05
        ctx.stroke()

        // Arrow
        const midX = (startX + endX) * 0.5
        const midY = (startY + endY) * 0.5
        let dirVec = { x: endX - startX, y: endY - startY }
        const dirVecMag = Math.sqrt(dirVec.x * dirVec.x + dirVec.y * dirVec.y)
        dirVec = { x: dirVec.x / dirVecMag, y: dirVec.y / dirVecMag }
        const rightVec = { x: dirVec.y, y: -dirVec.x }
        ctx.moveTo(midX, midY)
        ctx.lineTo(midX + (-dirVec.x - rightVec.x) * 0.1, midY + (-dirVec.y - rightVec.y) * 0.1)
        ctx.stroke()
        ctx.moveTo(midX, midY)
        ctx.lineTo(midX + (-dirVec.x + rightVec.x) * 0.1, midY + (-dirVec.y + rightVec.y) * 0.1)
        ctx.stroke()

        // Draw resources if thrower
        if (actions[i] == schema.Action.THROW_ATTACK) {
          const dv = 0.1 // Scale for distance between dots
          const dr = 0.0 // Scale for distance from dots to line
          const rad = 0.001
          if (prevAdamantiums[i])
            this.drawCircle(midX - 0.5 - rightVec.x * dr, midY - 0.5 - rightVec.y * dr, rad, adamantiumColor, "#00000088")
          if (prevManas[i])
            this.drawCircle(midX - 0.5 - rightVec.x * dr - dirVec.x * dv, midY - 0.5 - rightVec.y * dr - dirVec.y * dv, rad, manaColor, "#00000088")
          if (prevElixirs[i])
            this.drawCircle(midX - 0.5 - rightVec.x * dr + dirVec.x * dv, midY - 0.5 - rightVec.y * dr + dirVec.y * dv, rad, elixirColor, "#00000088")
        }

        ctx.restore()
      }

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
    const ctx = this.ctx[CanvasType.DYNAMIC]
    if (this.conf.doingRotate) [x, y] = [y, x]
    ctx.beginPath()
    ctx.arc(x + 0.5, y + 0.5, Math.sqrt(radiusSquared), 0, 2 * Math.PI)
    ctx.strokeStyle = color
    ctx.lineWidth = cst.SIGHT_RADIUS_LINE_WIDTH
    ctx.stroke()
  }

  private drawCircle(x: number, y: number, radiusSquared: number, color: string, borderColor: string) {
    const ctx = this.ctx[CanvasType.DYNAMIC]
    if (this.conf.doingRotate) [x, y] = [y, x]
    ctx.save()
    ctx.beginPath()
    ctx.arc(x + 0.5, y + 0.5, Math.sqrt(radiusSquared), 0, 2 * Math.PI)
    ctx.strokeStyle = borderColor
    ctx.lineWidth = radiusSquared < .005 ? 0.05 : 0.10
    ctx.stroke()
    ctx.fillStyle = color
    ctx.fill()
    ctx.restore()
  }

  /**
   * Draws an image centered at (x, y) with the given radius
   */
  /*
 private drawImage(img: HTMLImageElement, x: number, y: number, radius: number) {
   if (this.conf.doingRotate) [x, y] = [y, x]
   this.ctx.drawImage(img, x - radius, y - radius, radius * 2, radius * 2)
 }
 */

  /**
   * Draws an image centered at (x, y), such that an image with default size covers a 1x1 cell
   */
  private drawBot(img: HTMLImageElement, x: number, y: number, c: number, ratio: number, img_size: number) {
    const ctx = this.ctx[CanvasType.DYNAMIC]
    if (this.conf.doingRotate) [x, y] = [y, x]
    let realWidth = img.naturalWidth / img_size
    let realHeight = img.naturalHeight / img_size
    let size = ratio * 0.5 + 0.5
    ctx.drawImage(img, x + (1 - realWidth * size) / 2, y + (1 - realHeight * size) / 2, realWidth * size, realHeight * size)
    if (ratio < 1) {
      ctx.beginPath()
      ctx.moveTo(x + realWidth * size / 2 - realWidth * ratio / 2, y + 1)
      ctx.lineTo(x + realWidth * size / 2 + realWidth * ratio / 2, y + 1)
      ctx.strokeStyle = cst.UI_GREEN
      ctx.lineWidth = cst.SIGHT_RADIUS_LINE_WIDTH
      ctx.stroke()
    }
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

    // Overlay is on top so we will use it for mouse events
    const canvas = this.canvases[CanvasType.OVERLAY]
    canvas.onmousedown = (event: MouseEvent) => {
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
      onMouseover(x, y, xrel, yrel, world.mapStats.resources[idx],
        world.mapStats.resource_well_stats.get(idx)!,
        world.mapStats.island_stats.get(world.mapStats.islands[idx]))
    }

    // Overlay is on top so we will use it for mouse events
    const canvas = this.canvases[CanvasType.OVERLAY]
    canvas.onmousemove = (event) => {
      // const x = width * event.offsetX / this.canvas.offsetWidth + world.minCorner.x;
      // const _y = height * event.offsetY / this.canvas.offsetHeight + world.minCorner.y;
      // const y = this.flip(_y, minY, maxY)

      // Set the location of the mouseover
      const { x, y } = this.getIntegerLocation(event, world)
      const xrel = x - world.minCorner.x
      const yrel = y - world.minCorner.y
      const idx = world.mapStats.getIdx(xrel, yrel)
      onMouseover(x, y, xrel, yrel, world.mapStats.resources[idx],
        world.mapStats.resource_well_stats.get(idx)!,
        world.mapStats.island_stats.get(world.mapStats.islands[idx]))
      this.hoverPos = { xrel: xrel, yrel: yrel }
    }

    canvas.onmouseout = (event) => {
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
    const canvas = this.canvases[CanvasType.BACKGROUND]
    if (!this.conf.doingRotate) {
      _x = width * event.offsetX / canvas.offsetWidth + world.minCorner.x
      _y = height * event.offsetY / canvas.offsetHeight + world.minCorner.y
      _y = this.flip(_y, minY, maxY)
    }
    else {
      _y = (world.maxCorner.y - world.minCorner.y - 1) - height * event.offsetX / canvas.offsetWidth + world.minCorner.y
      _x = width * event.offsetY / canvas.offsetHeight + world.minCorner.x
    }
    return { x: Math.floor(_x), y: Math.floor(_y + 1) }
  }

  private renderIndicatorDotsLines(world: GameWorld) {
    const ctx = this.ctx[CanvasType.DYNAMIC]
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

    // console.log(dots.length)

    for (let i = 0; i < dots.length; i++) {
      if (dotsID[i] === this.lastSelectedID || this.conf.allIndicators) {
        const red = dotsRed[i]
        const green = dotsGreen[i]
        const blue = dotsBlue[i]
        const x = dotsX[i]
        const y = this.flip(dotsY[i], minY, maxY)

        ctx.beginPath()
        ctx.arc(x + 0.5, y + 0.5, cst.INDICATOR_DOT_SIZE, 0, 2 * Math.PI, false)
        ctx.fillStyle = `rgb(${red}, ${green}, ${blue})`
        ctx.fill()
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
    ctx.lineWidth = cst.INDICATOR_LINE_WIDTH

    for (let i = 0; i < lines.length; i++) {
      if (linesID[i] === this.lastSelectedID || this.conf.allIndicators) {
        const red = linesRed[i]
        const green = linesGreen[i]
        const blue = linesBlue[i]
        const startX = linesStartX[i] + 0.5
        const startY = this.flip(linesStartY[i], minY, maxY) + 0.5
        const endX = linesEndX[i] + 0.5
        const endY = this.flip(linesEndY[i], minY, maxY) + 0.5

        ctx.beginPath()
        ctx.moveTo(startX, startY)
        ctx.lineTo(endX, endY)
        ctx.strokeStyle = `rgb(${red}, ${green}, ${blue})`
        ctx.stroke()
      }
    }
  }

  private get9SliceClipPath(i: number, j: number, vals, height: number, width: number, valFunc: (v: number | boolean) => boolean = (v) => v ? true : false): number[][] {
    let edge = .07
    let bevel = .13
    let neighbors: boolean[] = []
    for (let v = 1; v < 9; v++) {
      let x = (cst.DIRECTIONS[v][0] + i)
      let y = (cst.DIRECTIONS[v][1] + j)
      neighbors.push((x < 0 || y < 0 || x == width || y == height) ? false : (valFunc(vals[x + y * width])))
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

  private applyClipScaled(ctx, i: number, j: number, scale: number, path: number[][]) {
    ctx.beginPath()
    let started = false
    for (let point of path) {
      if (!started)
        ctx.moveTo((point[0] + i) * scale, (point[1] + j) * scale)
      else
        ctx.lineTo((point[0] + i) * scale, (point[1] + j) * scale)
      started = true
    }
    ctx.closePath()
    ctx.clip()
  }
}

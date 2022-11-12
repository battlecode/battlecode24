import StructOfArrays from './soa'
import Metadata from './metadata'
import { flatbuffers, schema } from 'battlecode-schema'
import { playbackConfig } from './game'

// necessary because victor doesn't use exports.default
import Victor = require('victor')
import deepcopy = require('deepcopy')

// TODO use Victor for representing positions
export type DeadBodiesSchema = {
  id: Int32Array,
  x: Int32Array,
  y: Int32Array,
}

export type BodiesSchema = {
  id: Int32Array,
  team: Int8Array,
  type: Int8Array,
  x: Int32Array,
  y: Int32Array,
  bytecodesUsed: Int32Array, // TODO: is this needed?
  action: Int8Array,
  target: Int32Array,
  targetx: Int32Array,
  targety: Int32Array,
  // parent: Int32Array,

  adamantium: Int32Array,
  elixir: Int32Array,
  mana: Int32Array,
  anchor: Int8Array,

  hp: Int32Array,
}

// NOTE: consider changing MapStats to schema to use SOA for better performance, if it has large data
export type MapStats = {
  name: string,
  minCorner: Victor,
  maxCorner: Victor,
  bodies: schema.SpawnedBodyTable,
  randomSeed: number,

  walls: Int8Array,
  clouds: Int8Array,
  currents: Int8Array,

  islands: Int32Array,
  island_stats: Map<number, { owner: number, flip_progress: number, locations: number[], is_accelerated: boolean }>,

  resources: Int8Array,
  resource_well_stats: Map<number, { adamantium: number, mana: number, elixir: number, upgraded: boolean }>,


  //these are unused because there is no way for a resource to be dropped on the ground
  //   adamantiumVals: Int32Array
  //   manaVals: Int32Array
  //   elixirVals: Int32Array

  symmetry: number

  getIdx: (x: number, y: number) => number
  getLoc: (idx: number) => Victor
}

export type TeamStats = {
  // An array of numbers corresponding to team stats, which map to RobotTypes
  // HEADQUARTERS, CARRIER, LAUNCHER, AMPLIFIER, DESTABILIZER, BOOSTER
  robots: [number, number, number, number, number, number],
  total_hp: [number, number, number, number, number, number],

  adamantium: number,
  mana: number,
  elixir: number,

  adamantiumChange: number,
  manaChange: number,
  elixirChange: number,
  adamantiumMined: number,
  manaMined: number,
  elixirMined: number,

  //is this efficient? probably fine
  adamantiumMinedHist: number[],
  manaMinedHist: number[],
  elixirMinedHist: number[],
}

export type IndicatorDotsSchema = {
  id: Int32Array,
  x: Int32Array,
  y: Int32Array,
  red: Int32Array,
  green: Int32Array,
  blue: Int32Array
}

export type IndicatorLinesSchema = {
  id: Int32Array,
  startX: Int32Array,
  startY: Int32Array,
  endX: Int32Array,
  endY: Int32Array,
  red: Int32Array,
  green: Int32Array,
  blue: Int32Array
}

export type Log = {
  team: string, // 'A' | 'B'
  robotType: string, // All loggable bodies with team
  id: number,
  round: number,
  text: string
}

/**
 * A frozen image of the game world.
 *
 * TODO(jhgilles): better access control on contents.
 */
export default class GameWorld {
  /**
   * Bodies that died this round.
   */
  diedBodies: StructOfArrays<DeadBodiesSchema>

  /**
   * Everything that isn't an indicator string.
   */
  bodies: StructOfArrays<BodiesSchema>

  /*
   * Stats for each team
   */
  teamStats: Map<number, TeamStats> // Team ID to their stats

  /*
   * Stats for each team
   */
  mapStats: MapStats // Team ID to their stats

  /**
   * Indicator dots.
   */
  indicatorDots: StructOfArrays<IndicatorDotsSchema>

  /**
   * Indicator lines.
   */
  indicatorLines: StructOfArrays<IndicatorLinesSchema>

  /**
      * Indicator strings.
      * Stored as a dictionary of robot ids and that robot's string
      */
  indicatorStrings: object

  /**
   * The current turn.
   */
  turn: number

  // duplicate with mapStats, but left for compatibility.
  // TODO: change dependencies and remove these map variables
  /**
   * The minimum corner of the game world.
   */
  minCorner: Victor

  /**
   * The maximum corner of the game world.
   */
  maxCorner: Victor

  /**
   * The name of the map.
   */
  mapName: string

  /**
   * Metadata about the current game.
   */
  meta: Metadata

  /**
   * Whether to process logs.
   */
  config: playbackConfig

  /**
   * Recent logs, bucketed by round.
   */
  logs: Log[][] = [];

  /**
   * The ith index of this.logs corresponds to round (i + this.logsShift).
   */
  logsShift: number = 1;


  // Cache fields
  // We pass these into flatbuffers functions to avoid allocations, 
  // but that's it, they don't hold any state
  private _bodiesSlot: schema.SpawnedBodyTable
  private _vecTableSlot1: schema.VecTable
  private _vecTableSlot2: schema.VecTable
  private _rgbTableSlot: schema.RGBTable

  /**
   * IDs of robots who performed a temporary ability in the previous round,
   * which should be removed in the current round.
   */
  private actionRobots: number[] = [];

  constructor(meta: Metadata, config: playbackConfig) {
    this.meta = meta

    this.diedBodies = new StructOfArrays({
      id: new Int32Array(0),
      x: new Int32Array(0),
      y: new Int32Array(0),
    }, 'id')

    this.bodies = new StructOfArrays({
      id: new Int32Array(0),
      team: new Int8Array(0),
      type: new Int8Array(0),
      x: new Int32Array(0),
      y: new Int32Array(0),
      bytecodesUsed: new Int32Array(0),
      action: new Int8Array(0),
      target: new Int32Array(0),
      targetx: new Int32Array(0),
      targety: new Int32Array(0),
      anchor: new Int8Array(0),
      adamantium: new Int32Array(0),
      elixir: new Int32Array(0),
      mana: new Int32Array(0),
      hp: new Int32Array(0),
    }, 'id')

    // Instantiate teamStats
    this.teamStats = new Map<number, TeamStats>()
    for (let team in this.meta.teams) {
      var teamID = this.meta.teams[team].teamID
      this.teamStats.set(teamID, {
        robots: [0, 0, 0, 0, 0, 0],
        adamantium: 0,
        mana: 0,
        elixir: 0,
        total_hp: [0, 0, 0, 0, 0, 0],
        adamantiumChange: 0,
        manaChange: 0,
        elixirChange: 0,
        adamantiumMined: 0,
        manaMined: 0,
        elixirMined: 0,
        adamantiumMinedHist: [],
        manaMinedHist: [],
        elixirMinedHist: [],
      })
    }

    // Instantiate mapStats
    this.mapStats = {
      name: '????',
      minCorner: new Victor(0, 0),
      maxCorner: new Victor(0, 0),
      bodies: new schema.SpawnedBodyTable(),
      randomSeed: 0,

      walls: new Int8Array(0),
      clouds: new Int8Array(0),
      currents: new Int8Array(0),
      islands: new Int32Array(0),
      island_stats: new Map(),
      resources: new Int8Array(0),
      resource_well_stats: new Map(),
      // adamantiumVals: new Int8Array(0),
      // manaVals: new Int8Array(0),
      // elixirVals: new Int8Array(0),

      symmetry: 0,

      getIdx: (x: number, y: number) => 0,
      getLoc: (idx: number) => new Victor(0, 0)
    }


    this.indicatorDots = new StructOfArrays({
      id: new Int32Array(0),
      x: new Int32Array(0),
      y: new Int32Array(0),
      red: new Int32Array(0),
      green: new Int32Array(0),
      blue: new Int32Array(0)
    }, 'id')

    this.indicatorLines = new StructOfArrays({
      id: new Int32Array(0),
      startX: new Int32Array(0),
      startY: new Int32Array(0),
      endX: new Int32Array(0),
      endY: new Int32Array(0),
      red: new Int32Array(0),
      green: new Int32Array(0),
      blue: new Int32Array(0)
    }, 'id')

    this.indicatorStrings = {}

    this.turn = 0
    this.minCorner = new Victor(0, 0)
    this.maxCorner = new Victor(0, 0)
    this.mapName = '????'

    this._bodiesSlot = new schema.SpawnedBodyTable()
    this._vecTableSlot1 = new schema.VecTable()
    this._vecTableSlot2 = new schema.VecTable()
    this._rgbTableSlot = new schema.RGBTable()

    this.config = config
  }

  loadFromMatchHeader(header: schema.MatchHeader) {
    const map = header.map()

    const name = map.name() as string
    if (name) {
      this.mapName = map.name() as string
      this.mapStats.name = map.name() as string
    }

    const minCorner = map.minCorner()
    this.minCorner.x = minCorner.x()
    this.minCorner.y = minCorner.y()
    this.mapStats.minCorner.x = minCorner.x()
    this.mapStats.minCorner.y = minCorner.y()

    const maxCorner = map.maxCorner()
    this.maxCorner.x = maxCorner.x()
    this.maxCorner.y = maxCorner.y()
    this.mapStats.maxCorner.x = maxCorner.x()
    this.mapStats.maxCorner.y = maxCorner.y()

    // this.mapStats.leadVals = map.leadArray()

    const bodies = map.bodies(this._bodiesSlot)
    if (bodies && bodies.robotIDsLength) {
      this.insertBodies(bodies)
    }

    this.mapStats.randomSeed = map.randomSeed()

    this.mapStats.walls = map.wallsArray()

    this.mapStats.resources = Int8Array.from(map.resourcesArray())
    for (let i = 0; i < this.mapStats.resources.length; i++) {
      if (this.mapStats.resources[i] != 0) {
        this.mapStats.resource_well_stats.set(i, { adamantium: 0, mana: 0, elixir: 0, upgraded: false })
      }
    }

    this.mapStats.islands = map.islandsArray()
    for (let i = 0; i < this.mapStats.islands.length; i++) {
      if (this.mapStats.islands[i] != 0) {
        let island_id = this.mapStats.islands[i]
        if (this.mapStats.island_stats.has(island_id)) {
          let existing_island = this.mapStats.island_stats.get(island_id)
          existing_island.locations.push(i)
          // this.mapStats.island_stats.set(island_id, existing_island)
        } else {
          this.mapStats.island_stats.set(island_id, { owner: 0, flip_progress: 0, locations: [i], is_accelerated: false })
        }
      }
    }


    const width = (maxCorner.x() - minCorner.x())
    this.mapStats.getIdx = (x: number, y: number) => (
      Math.floor(y) * width + Math.floor(x)
    )
    this.mapStats.getLoc = (idx: number) => (
      new Victor(idx % width, Math.floor(idx / width))
    )

    this.mapStats.symmetry = map.symmetry()

    // this.mapStats.anomalies = Int32Array.from(map.anomaliesArray())
    // this.mapStats.anomalyRounds = Int32Array.from(map.anomalyRoundsArray())

    // Check with header.totalRounds() ?
  }

  /**
   * Create a copy of the world in its current state.
   */
  copy(): GameWorld {
    const result = new GameWorld(this.meta, this.config)
    result.copyFrom(this)
    return result
  }

  copyFrom(source: GameWorld) {
    this.turn = source.turn
    this.minCorner = source.minCorner
    this.maxCorner = source.maxCorner
    this.mapName = source.mapName
    this.diedBodies.copyFrom(source.diedBodies)
    this.bodies.copyFrom(source.bodies)
    this.indicatorDots.copyFrom(source.indicatorDots)
    this.indicatorLines.copyFrom(source.indicatorLines)
    this.indicatorStrings = Object.assign({}, source.indicatorStrings)
    this.teamStats = new Map<number, TeamStats>()
    source.teamStats.forEach((value: TeamStats, key: number) => {
      this.teamStats.set(key, deepcopy(value))
    })
    this.mapStats = deepcopy(source.mapStats)
    this.actionRobots = Array.from(source.actionRobots)
    this.logs = Array.from(source.logs)
    this.logsShift = source.logsShift
  }

  /**
   * Process a set of changes.
   */
  processDelta(delta: schema.Round) { // Change to reflect current game
    if (delta.roundID() != this.turn + 1) {
      throw new Error(`Bad Round: this.turn = ${this.turn}, round.roundID() = ${delta.roundID()}`)
    }

    // Process team info changes
    for (var i = 0; i < delta.teamIDsLength(); i++) {
      let teamID = delta.teamIDs(i)
      let statObj = this.teamStats.get(teamID)

      statObj.adamantium += delta.teamAdChanges(i)
      statObj.mana += delta.teamMnChanges(i)
      statObj.elixir += delta.teamExChanges(i)

      statObj.adamantiumChange = delta.teamAdChanges(i)
      statObj.manaChange = delta.teamMnChanges(i)
      statObj.elixirChange = delta.teamExChanges(i)

      statObj.adamantiumMined = 0
      statObj.manaMined = 0
      statObj.elixirMined = 0

      this.teamStats.set(teamID, statObj)
    }

    // Location changes on bodies
    const movedLocs = delta.movedLocs(this._vecTableSlot1)
    if (movedLocs) {
      this.bodies.alterBulk({
        id: delta.movedIDsArray(),
        x: movedLocs.xsArray(),
        y: movedLocs.ysArray(),
      })
    }

    // Spawned bodies
    const bodies = delta.spawnedBodies(this._bodiesSlot)
    if (bodies) {
      this.insertBodies(bodies)
    }

    // Remove abilities from previous round
    this.bodies.alterBulk({
      id: new Int32Array(this.actionRobots), action: (new Int8Array(this.actionRobots.length)).fill(-1),
      target: new Int32Array(this.actionRobots.length), targetx: new Int32Array(this.actionRobots.length), targety: new Int32Array(this.actionRobots.length)
    })
    this.actionRobots = []

    // Map changes
    // const leadLocations = delta.leadDropLocations(this._vecTableSlot1)
    // if (leadLocations) {
    //   const xs = leadLocations.xsArray()
    //   const ys = leadLocations.ysArray()

    //   xs.forEach((x, i) => {
    //     const y = ys[i]
    //     this.mapStats.leadVals[this.mapStats.getIdx(x, y)] += delta.leadDropValues(i)
    //   })
    // }

    // const goldLocations = delta.goldDropLocations(this._vecTableSlot1)
    // if (goldLocations) {
    //   const xs = goldLocations.xsArray()
    //   const ys = goldLocations.ysArray()
    //   let inst = this
    //   xs.forEach((x, i) => {
    //     const y = ys[i]
    //     inst.mapStats.goldVals[inst.mapStats.getIdx(x, y)] += delta.goldDropValues(i)
    //   })
    // }

    // Are we doing this in this way this year
    // if (delta.roundID() % this.meta.constants.increasePeriod() == 0) {
    //   this.mapStats.leadVals.forEach((x, i) => {
    //     this.mapStats.leadVals[i] = x > 0 ? x + this.meta.constants.leadAdditiveIncease() : 0
    //   })
    // }

    // Actions
    if (delta.actionsLength() > 0) {
      for (let i = 0; i < delta.actionsLength(); i++) {
        const action = delta.actions(i)
        const robotID = delta.actionIDs(i)
        const target = delta.actionTargets(i)
        const body = robotID != -1 ? this.bodies.lookup(robotID) : null
        const teamStatsObj = body != null ? this.teamStats.get(body.team) : null


        const setAction = (set_target: Boolean = false, set_target_loc_from_body_id: Boolean = false, set_target_loc_from_location: Boolean = false) => {
          this.bodies.alter({ id: robotID, action: action as number })

          if (set_target)
            this.bodies.alter({ id: robotID, target: target })
          else if (set_target_loc_from_body_id) {
            const target_body = this.bodies.lookup(target)
            this.bodies.alter({ id: robotID, targetx: target_body.x, targety: target_body.y })
          } else if (set_target_loc_from_location) {
            const width = this.mapStats.maxCorner.x - this.mapStats.minCorner.x
            const target_x = target % width
            const target_y = (target - target_x) / width
            this.bodies.alter({ id: robotID, targetx: target_x, targety: target_y })
          }

          this.actionRobots.push(robotID)
        }


        switch (action) {
          case schema.Action.THROW_ATTACK:
            setAction(true, false)
            break
          case schema.Action.LAUNCH_ATTACK:
            setAction(true, false)
            break
          case schema.Action.PICK_UP_RESOURCE:
            setAction(false, false, true)
            break

          case schema.Action.PLACE_RESOURCE:
            setAction(false, false, true)
            break

          case schema.Action.DESTABILIZE:
            setAction(false, false, true)
            break

          case schema.Action.DESTABILIZE_DAMAGE:
            setAction(false, false, true)
            break

          case schema.Action.BOOST:
            setAction(false, false, true)
            break

          case schema.Action.PICK_UP_ANCHOR:
            setAction()
            this.bodies.alter({ id: robotID, anchor: target + 1 })
            break

          case schema.Action.PLACE_ANCHOR:
            setAction(false, false, true)
            this.bodies.alter({ id: robotID, anchor: 0 })
            let curr_island = this.mapStats.island_stats.get(target)
            let curr_robot = this.bodies.lookup(robotID)
            curr_island.is_accelerated = curr_robot.anchor == 2
            break

          case schema.Action.CHANGE_ADAMANTIUM:
            if (target > 0 && body.type != schema.BodyType.HEADQUARTERS)
              teamStatsObj.adamantiumMined += target
            this.bodies.alter({ id: robotID, adamantium: body.adamantium + target })
            break

          case schema.Action.CHANGE_ELIXIR:
            if (target > 0 && body.type != schema.BodyType.HEADQUARTERS)
              teamStatsObj.elixirMined += target
            this.bodies.alter({ id: robotID, elixir: body.elixir + target })
            break

          case schema.Action.CHANGE_MANA:
            if (target > 0 && body.type != schema.BodyType.HEADQUARTERS)
              teamStatsObj.manaMined += target
            this.bodies.alter({ id: robotID, mana: body.mana + target })
            break

          case schema.Action.SPAWN_UNIT:
            break

          case schema.Action.CHANGE_HEALTH:
            this.bodies.alter({ id: robotID, hp: body.hp + target })
            teamStatsObj.total_hp[body.type] += target
            break

          case schema.Action.DIE_EXCEPTION:
            console.log(`Exception occured: robotID(${robotID}), target(${target}`)
            break

          default:
            console.log(`Undefined action: action(${action}), robotID(${robotID}, target(${target}))`)
            break
        }
        if (body) this.teamStats.set(body.team, teamStatsObj)
      }
    }

    //resource wells (THIS SHOULD JUST BE IDS)
    // const wells = delta.resourceWellLocs(this._vecTableSlot1)

    for (let i = 0; i < delta.resourceWellLocsLength(); i++) {
      let well_index = delta.resourceWellLocs(i)
      let well_resource = delta.resourceID(i)
      let well_adamantium_change = delta.wellAdamantiumChange(i)
      let well_elixir_change = delta.wellElixirChange(i)
      let well_mana_change = delta.wellManaChange(i)

      let current_resource_stats = this.mapStats.resource_well_stats.get(well_index)

      this.mapStats.resources[well_index] = well_resource

      //WHAT DO WE DO WHEN THEY ARE FULL AND CONVERT
      current_resource_stats.adamantium += well_adamantium_change
      current_resource_stats.mana += well_mana_change
      current_resource_stats.elixir += well_elixir_change
      current_resource_stats.upgraded = delta.resourceAccelerationID(i) > 0
    }

    for (let i = 0; i < delta.islandIDsLength(); i++) {
      let id = delta.islandIDs(i)
      let owner = delta.islandOwnership(i)
      let turnover = delta.islandTurnoverTurns(i)
      let island_stats = this.mapStats.island_stats.get(id)
      island_stats.flip_progress = turnover
      if (island_stats.owner != owner) {
        island_stats.is_accelerated = false
      }
      island_stats.owner = owner
    }


    //mining history 
    for (let team in this.meta.teams) {
      let teamID = this.meta.teams[team].teamID
      let statsObj = this.teamStats.get(teamID) as TeamStats

      statsObj.adamantiumMinedHist.push(statsObj.adamantiumMined)
      if (statsObj.adamantiumMinedHist.length > 100) statsObj.adamantiumMinedHist.shift()

      statsObj.manaMinedHist.push(statsObj.manaMined)
      if (statsObj.manaMinedHist.length > 100) statsObj.manaMinedHist.shift()

      statsObj.elixirMinedHist.push(statsObj.elixirMined)
      if (statsObj.elixirMinedHist.length > 100) statsObj.elixirMinedHist.shift()
    }

    // Died bodies
    if (delta.diedIDsLength() > 0) {
      // Update team stats
      var indices = this.bodies.lookupIndices(delta.diedIDsArray())
      for (let i = 0; i < delta.diedIDsLength(); i++) {
        let index = indices[i]
        let team = this.bodies.arrays.team[index]
        let type = this.bodies.arrays.type[index]
        let statObj = this.teamStats.get(team)
        if (!statObj) { continue } // In case this is a neutral bot
        statObj.robots[type][this.bodies.arrays.level[index] - 1] -= 1
        let hp = this.bodies.arrays.hp[index]
        let level = this.bodies.arrays.level[index]
        statObj.total_hp[type][level - 1] -= hp
        this.teamStats.set(team, statObj)
      }

      // Update bodies soa
      this.insertDiedBodies(delta)
      this.bodies.deleteBulk(delta.diedIDsArray())
    }

    // Insert indicator dots and lines
    this.insertIndicatorDots(delta)
    this.insertIndicatorLines(delta)

    //indicator strings
    for (var i = 0; i < delta.indicatorStringsLength(); i++) {
      let bodyID = delta.indicatorStringIDs(i)
      this.indicatorStrings[bodyID] = delta.indicatorStrings(i)
    }

    // Increase the turn count
    this.turn = delta.roundID()

    // Update bytecode costs
    if (delta.bytecodeIDsLength() > 0) {
      this.bodies.alterBulk({
        id: delta.bytecodeIDsArray(),
        bytecodesUsed: delta.bytecodesUsedArray()
      })
    }
  }

  private insertDiedBodies(delta: schema.Round) {
    // Delete the died bodies from the previous round
    this.diedBodies.clear()

    // Insert the died bodies from the current round
    const startIndex = this.diedBodies.insertBulk({
      id: delta.diedIDsArray()
    })

    // Extra initialization
    const endIndex = startIndex + delta.diedIDsLength()
    const idArray = this.diedBodies.arrays.id
    const xArray = this.diedBodies.arrays.x
    const yArray = this.diedBodies.arrays.y
    for (let i = startIndex; i < endIndex; i++) {
      const body = this.bodies.lookup(idArray[i])
      xArray[i] = body.x
      yArray[i] = body.y
    }
  }

  private insertIndicatorDots(delta: schema.Round) {
    // Delete the dots from the previous round
    this.indicatorDots.clear()

    // Insert the dots from the current round
    if (delta.indicatorDotIDsLength() > 0) {
      const locs = delta.indicatorDotLocs(this._vecTableSlot1)
      const rgbs = delta.indicatorDotRGBs(this._rgbTableSlot)
      this.indicatorDots.insertBulk({
        id: delta.indicatorDotIDsArray(),
        x: locs.xsArray(),
        y: locs.ysArray(),
        red: rgbs.redArray(),
        green: rgbs.greenArray(),
        blue: rgbs.blueArray()
      })
    }
  }

  private insertIndicatorLines(delta: schema.Round) {
    // Delete the lines from the previous round
    this.indicatorLines.clear()

    // Insert the lines from the current round
    if (delta.indicatorLineIDsLength() > 0) {
      const startLocs = delta.indicatorLineStartLocs(this._vecTableSlot1)
      const endLocs = delta.indicatorLineEndLocs(this._vecTableSlot2)
      const rgbs = delta.indicatorLineRGBs(this._rgbTableSlot)
      this.indicatorLines.insertBulk({
        id: delta.indicatorLineIDsArray(),
        startX: startLocs.xsArray(),
        startY: startLocs.ysArray(),
        endX: endLocs.xsArray(),
        endY: endLocs.ysArray(),
        red: rgbs.redArray(),
        green: rgbs.greenArray(),
        blue: rgbs.blueArray()
      })
    }
  }

  private insertBodies(bodies: schema.SpawnedBodyTable) {

    // Store frequently used arrays
    var teams = bodies.teamIDsArray()
    var types = bodies.typesArray()
    var hps = new Int32Array(bodies.robotIDsLength())

    // Update spawn stats
    for (let i = 0; i < bodies.robotIDsLength(); i++) {
      // if(teams[i] == 0) continue;
      var statObj = this.teamStats.get(teams[i])
      statObj.robots[types[i]] += 1 // TODO: handle level
      statObj.total_hp[types[i]] += this.meta.types[types[i]].health // TODO: extract meta info
      this.teamStats.set(teams[i], statObj)
      hps[i] = this.meta.types[types[i]].health
    }

    const locs = bodies.locs(this._vecTableSlot1)
    // Note: this allocates 6 objects with each call.
    // (One for the container, one for each TypedArray.)
    // All of the objects are small; the TypedArrays are basically
    // (pointer, length) pairs.
    // You can't reuse TypedArrays easily, so I'm inclined to
    // let this slide for now.

    // Insert bodies

    this.bodies.insertBulk({
      id: bodies.robotIDsArray(),
      team: teams,
      type: types,
      x: locs.xsArray(),
      y: locs.ysArray(),
      bytecodesUsed: new Int32Array(bodies.robotIDsLength()),
      action: (new Int8Array(bodies.robotIDsLength())).fill(-1),
      target: new Int32Array(bodies.robotIDsLength()),
      targetx: new Int32Array(bodies.robotIDsLength()),
      targety: new Int32Array(bodies.robotIDsLength()),
      parent: new Int32Array(bodies.robotIDsLength()),
      hp: hps,
      adamantium: new Int32Array(bodies.robotIDsLength()),
      elixir: new Int32Array(bodies.robotIDsLength()),
      mana: new Int32Array(bodies.robotIDsLength()),
      anchor: new Int8Array(bodies.robotIDsLength()),
    })
  }

  /**
    * Parse logs for a round.
    */
  private parseLogs(round: number, logs: string) {
    // TODO regex this properly
    // Regex
    let lines = logs.split(/\r?\n/)
    let header = /^\[(A|B):(ENLIGHTENMENT_CENTER|POLITICIAN|SLANDERER|MUCKRAKER)#(\d+)@(\d+)\] (.*)/

    let roundLogs = new Array<Log>()

    // Parse each line
    let index: number = 0
    while (index < lines.length) {
      let line = lines[index]
      let matches = line.match(header)

      // Ignore empty string
      if (line === "") {
        index += 1
        continue
      }

      // The entire string and its 5 parenthesized substrings must be matched!
      if (matches === null || (matches && matches.length != 6)) {
        // throw new Error(`Wrong log format: ${line}`);
        console.log(`Wrong log format: ${line}`)
        console.log('Omitting logs')
        return
      }

      let shortenRobot = new Map()
      shortenRobot.set("ENLIGHTENMENT_CENTER", "EC")
      shortenRobot.set("POLITICIAN", "P")
      shortenRobot.set("SLANDERER", "SL")
      shortenRobot.set("MUCKRAKER", "MCKR")

      // Get the matches
      let team = matches[1]
      let robotType = matches[2]
      let id = parseInt(matches[3])
      let logRound = parseInt(matches[4])
      let text = new Array<string>()
      let mText = "<span class='consolelogheader consolelogheader1'>[" + team + ":" + robotType + "#" + id + "@" + logRound + "]</span>"
      let mText2 = "<span class='consolelogheader consolelogheader2'>[" + team + ":" + shortenRobot.get(robotType) + "#" + id + "@" + logRound + "]</span> "
      text.push(mText + mText2 + matches[5])
      index += 1

      // If there is additional non-header text in the following lines, add it
      while (index < lines.length && !lines[index].match(header)) {
        text.push(lines[index])
        index += 1
      }

      if (logRound != round) {
        console.warn(`Your computation got cut off while printing a log statement at round ${logRound}; the actual print happened at round ${round}`)
      }

      // Push the parsed log
      roundLogs.push({
        team: team,
        robotType: robotType,
        id: id,
        round: logRound,
        text: text.join('\n')
      })
    }
    this.logs.push(roundLogs)
  }
}

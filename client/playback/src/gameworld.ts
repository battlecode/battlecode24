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
  parent: Int32Array,
  hp: Int32Array,
  level: Int8Array,
  portable: Int8Array,
  prototype: Int8Array
}

// NOTE: consider changing MapStats to schema to use SOA for better performance, if it has large data
export type MapStats = {
  name: string,
  minCorner: Victor,
  maxCorner: Victor,
  bodies: schema.SpawnedBodyTable,
  randomSeed: number,

  rubble: Int32Array, // double
  leadVals: Int32Array
  goldVals: Int32Array

  symmetry: number

  anomalies: Int8Array
  anomalyRounds: Int8Array

  getIdx: (x: number, y: number) => number
  getLoc: (idx: number) => Victor
}

export type TeamStats = {
  // An array of numbers corresponding to team stats, which map to RobotTypes
  // Corresponds to robot type (including NONE. length 5)
  // First four are droids (guard, wizard, builder, miner), last three are buildings (turret, archon, lab)
  robots: [number[], number[], number[], number[], number[], number[], number[]],
  lead: number,
  gold: number,
  total_hp: [number[], number[], number[], number[], number[], number[], number[]],
  leadChange: number,
  goldChange: number
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
  private bidRobots: number[] = [];

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
      parent: new Int32Array(0),
      hp: new Int32Array(0),
      level: new Int8Array(0),
      portable: new Int8Array(0),
      prototype: new Int8Array(0)
    }, 'id')

    // Instantiate teamStats
    this.teamStats = new Map<number, TeamStats>()
    for (let team in this.meta.teams) {
      var teamID = this.meta.teams[team].teamID
      this.teamStats.set(teamID, {
        robots: [[0], [0], [0], [0], [0, 0, 0], [0, 0, 0], [0, 0, 0]],
        lead: 0,
        gold: 0,
        total_hp: [[0], [0], [0], [0], [0, 0, 0], [0, 0, 0], [0, 0, 0]],
        leadChange: 0,
        goldChange: 0
      })
    }

    // Instantiate mapStats
    this.mapStats = {
      name: '????',
      minCorner: new Victor(0, 0),
      maxCorner: new Victor(0, 0),
      bodies: new schema.SpawnedBodyTable(),
      randomSeed: 0,

      rubble: new Int32Array(0),
      leadVals: new Int32Array(0),
      goldVals: new Int32Array(0),

      symmetry: 0,
      anomalies: new Int8Array(0),
      anomalyRounds: new Int8Array(0),

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

    this.mapStats.goldVals = new Int32Array(maxCorner.x() * maxCorner.y())
    this.mapStats.leadVals = map.leadArray()

    const bodies = map.bodies(this._bodiesSlot)
    if (bodies && bodies.robotIDsLength) {
      this.insertBodies(bodies)
    }

    this.mapStats.randomSeed = map.randomSeed()

    this.mapStats.rubble = map.rubbleArray()

    const width = (maxCorner.x() - minCorner.x())
    this.mapStats.getIdx = (x: number, y: number) => (
      Math.floor(y) * width + Math.floor(x)
    )
    this.mapStats.getLoc = (idx: number) => (
      new Victor(idx % width, Math.floor(idx / width))
    )

    this.mapStats.symmetry = map.symmetry()

    this.mapStats.anomalies = Int8Array.from(map.anomaliesArray())
    this.mapStats.anomalyRounds = Int8Array.from(map.anomalyRoundsArray())

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
    this.bidRobots = Array.from(source.bidRobots)
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

      statObj.lead += delta.teamLeadChanges(i)
      statObj.gold += delta.teamGoldChanges(i)
      statObj.leadChange = delta.teamLeadChanges(i)
      statObj.goldChange = delta.teamGoldChanges(i)

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
    this.bodies.alterBulk({id: new Int32Array(this.actionRobots), action: (new Int8Array(this.actionRobots.length)).fill(-1), 
      target: new Int32Array(this.actionRobots.length), targetx: new Int32Array(this.actionRobots.length), targety: new Int32Array(this.actionRobots.length)});
    this.actionRobots = [];

    // Remove bids from previous round
    this.bodies.alterBulk({ id: new Int32Array(this.bidRobots), bid: new Int32Array(this.bidRobots.length) })
    this.bidRobots = []

    // Map changes
    const leadLocations = delta.leadDropLocations(this._vecTableSlot1)
    if (leadLocations) {
      const xs = leadLocations.xsArray()
      const ys = leadLocations.ysArray()

      xs.forEach((x, i) => {
        const y = ys[i]
        this.mapStats.leadVals[this.mapStats.getIdx(x, y)] += delta.leadDropValues(i)
      })
    }

    const goldLocations = delta.goldDropLocations(this._vecTableSlot1)
    if (goldLocations) {
      const xs = goldLocations.xsArray()
      const ys = goldLocations.ysArray()
      let inst = this
      xs.forEach((x, i) => {
        const y = ys[i]
        inst.mapStats.goldVals[inst.mapStats.getIdx(x, y)] += delta.goldDropValues(i)
      })
    }

    if (delta.roundID() % this.meta.constants.increasePeriod() == 0) {
      this.mapStats.leadVals.forEach((x, i) => {
        this.mapStats.leadVals[i] = x > 0 ? x + this.meta.constants.leadAdditiveIncease() : 0
      })
    }

    // Actions
    if(delta.actionsLength() > 0){
      const arrays = this.bodies.arrays;
      
      for(let i=0; i<delta.actionsLength(); i++){
        const action = delta.actions(i);
        const robotID = delta.actionIDs(i);
        const target = delta.actionTargets(i);
        const body = robotID != -1 ? this.bodies.lookup(robotID) : null;
        const teamStatsObj = body != null ? this.teamStats.get(body.team) : null;
        const setAction = (set_target: Boolean = false, set_target_loc: Boolean = false) => {
          this.bodies.alter({id: robotID, action: action as number});
          if (set_target) this.bodies.alter({id: robotID, target: target});
          if (set_target_loc) {
            const target_body = this.bodies.lookup(target);
            this.bodies.alter({id: robotID, targetx: target_body.x, targety: target_body.y});
          }
          this.actionRobots.push(robotID);
        }; // should be called for actions performed *by* the robot.
        switch (action) {
          // TODO: validate actions?
          // Actions list from battlecode.fbs enum Action

          case schema.Action.ATTACK:
            setAction(true, true);
            break;
          /// Slanderers passively generate influence for the
          /// Enlightenment Center that created them.
          /// Target: parent ID
          case schema.Action.LOCAL_ABYSS:
            setAction()
            break

          case schema.Action.LOCAL_CHARGE:
            setAction()
            break

          case schema.Action.LOCAL_FURY:
            setAction()
            break

          case schema.Action.TRANSMUTE:
            setAction();
            // teamStatsObj.gold += target;
            // teamStatsObj.lead -= 0;
            break;

          case schema.Action.TRANSFORM:
            setAction()
            this.bodies.alter({ id: robotID, portable: 1 - body.portable })
            break

          case schema.Action.MUTATE:
            setAction()
            teamStatsObj.robots[body.type][body.level - 1] -= 1
            teamStatsObj.robots[body.type][body.level + 1 - 1] += 1
            teamStatsObj.total_hp[body.type][body.level - 1] -= body.hp
            teamStatsObj.total_hp[body.type][body.level + 1 - 1] += body.hp
            this.bodies.alter({ id: robotID, level: body.level + 1 })
            break

          /// Builds a unit (enlightent center).
          /// Target: spawned unit
          case schema.Action.SPAWN_UNIT:
            setAction()
            this.bodies.alter({ id: target, parent: robotID })
            break

          case schema.Action.REPAIR:
            setAction()
            break

          case schema.Action.CHANGE_HEALTH:
            this.bodies.alter({ id: robotID, hp: body.hp + target});
            teamStatsObj.total_hp[body.type][body.level - 1] += target;
            break;

          case schema.Action.FULLY_REPAIRED:
            this.bodies.alter({ id: robotID, prototype: 0});
            //teamStatsObj.total_hp[body.type][body.level] += target;
            break;

          case schema.Action.DIE_EXCEPTION:
            console.log(`Exception occured: robotID(${robotID}), target(${target}`)
            break

          case schema.Action.VORTEX:
            let w = this.mapStats.maxCorner.x - this.mapStats.minCorner.x;
            let h = this.mapStats.maxCorner.y - this.mapStats.minCorner.y;
            switch (target) {
              case 0:
                for (let x = 0; x < w / 2; x++) {
                  for (let y = 0; y < (w + 1) / 2; y++) {
                    let curX = x
                    let curY = y
                    let lastRubble = this.mapStats.rubble[curX + curY * w]
                    for (let i = 0; i < 4; i++) {
                      let tempX = curX
                      curX = curY
                      curY = (w - 1) - tempX
                      let idx = curX + curY * w
                      let tempRubble = this.mapStats.rubble[idx]
                      this.mapStats.rubble[idx] = lastRubble
                      lastRubble = tempRubble
                    }
                  }
                }
                break
              case 1:
                for (let x = 0; x < w / 2; x++) {
                  for (let y = 0; y < h; y++) {
                    let idx = x + y * w
                    let newX = w - 1 - x
                    let newIdx = newX + y * w
                    let prevRubble = this.mapStats.rubble[idx]
                    this.mapStats.rubble[idx] = this.mapStats.rubble[newIdx]
                    this.mapStats.rubble[newIdx] = prevRubble
                  }
                }
                break
              case 2:
                for (let y = 0; y < h / 2; y++) {
                  for (let x = 0; x < w; x++) {
                    let idx = x + y * w
                    let newY = h - 1 - y
                    let newIdx = x + newY * w
                    let prevRubble = this.mapStats.rubble[idx]
                    this.mapStats.rubble[idx] = this.mapStats.rubble[newIdx]
                    this.mapStats.rubble[newIdx] = prevRubble
                  }
                }
                break
            }

          default:
            //console.log(`Undefined action: action(${action}), robotID(${robotID}, target(${target}))`);
            break
        }
        if (body) this.teamStats.set(body.team, teamStatsObj)
      }
    }

    // for (let team in this.meta.teams) {
    //   let teamID = this.meta.teams[team].teamID;
    //   let teamStats = this.teamStats.get(teamID) as TeamStats;
    //   teamStats.income = 0;
    // }

    // income
    // this.bodies.arrays.type.forEach((type, i) => {
    //   let robotID = this.bodies.arrays.id[i];
    //   let team = this.bodies.arrays.team[i];
    //   let ability = this.bodies.arrays.ability[i];
    //   let influence = this.bodies.arrays.influence[i];
    //   let income = this.bodies.arrays.income[i];
    //   let parent = this.bodies.arrays.parent[i];
    //   var teamStatsObj = this.teamStats.get(team);
    //   if (ability === 3) {
    //       let delta = Math.floor((1/50 + 0.03 * Math.exp(-0.001 * influence)) * influence);
    //       teamStatsObj.income += delta;
    //       this.bodies.alter({id: parent, income: delta});
    //   } else if (type === schema.BodyType.ENLIGHTENMENT_CENTER && teamStatsObj) {
    //      let delta = Math.ceil(0.2 * Math.sqrt(this.turn));
    //      teamStatsObj.income += delta;
    //      this.bodies.alter({id: robotID, income: delta});
    //   } else if (income !== 0) {
    //     this.bodies.alter({id: robotID, income: 0});
    //   }
    //   this.teamStats.set(team, teamStatsObj);
    // })

    // Died bodies
    if (delta.diedIDsLength() > 0) {
      // Update team stats
      var indices = this.bodies.lookupIndices(delta.diedIDsArray());
      for(let i = 0; i < delta.diedIDsLength(); i++) {
          let index = indices[i];
          let team = this.bodies.arrays.team[index];
          let type = this.bodies.arrays.type[index];
          let statObj = this.teamStats.get(team);
          if(!statObj) {continue;} // In case this is a neutral bot
          statObj.robots[type][this.bodies.arrays.level[index] - 1] -= 1;
          let hp = this.bodies.arrays.hp[index];
          let level = this.bodies.arrays.level[index];
          statObj.total_hp[type][level - 1] -= hp;
          this.teamStats.set(team, statObj);
      }

      // Update bodies soa
      this.insertDiedBodies(delta)

      this.bodies.deleteBulk(delta.diedIDsArray())
    }

    // Insert indicator dots and lines
    this.insertIndicatorDots(delta)
    this.insertIndicatorLines(delta)

    //indicator strings
    for(var i = 0; i < delta.indicatorStringsLength(); i++){
      let bodyID = delta.indicatorStringIDs(i)
      this.indicatorStrings[bodyID] = delta.indicatorStrings(i)
    }

    // Logs
    // TODO

    // Message pool
    // TODO

    // Increase the turn count
    this.turn = delta.roundID()

    // Update bytecode costs
    if (delta.bytecodeIDsLength() > 0) {
      this.bodies.alterBulk({
        id: delta.bytecodeIDsArray(),
        bytecodesUsed: delta.bytecodesUsedArray()
      })
    }

    // TODO: process indicator strings

    // // Process logs
    // if (this.config.processLogs) this.parseLogs(delta.roundID(), delta.logs() ? <string> delta.logs(flatbuffers.Encoding.UTF16_STRING) : "");
    // else this.logsShift++;

    // while (this.logs.length >= 25) {
    //   this.logs.shift();
    //   this.logsShift++;
    // }
    // console.log(delta.roundID(), this.logsShift, this.logs[0]);
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
    var teams = bodies.teamIDsArray();
    var types = bodies.typesArray();
    var hps = new Int32Array(bodies.robotIDsLength());
    var prototypes = new Int8Array(bodies.robotIDsLength());

    // Update spawn stats
    for (let i = 0; i < bodies.robotIDsLength(); i++) {
      // if(teams[i] == 0) continue;
      var statObj = this.teamStats.get(teams[i]);
      statObj.robots[types[i]][0] += 1; // TODO: handle level
      statObj.total_hp[types[i]][0] += this.meta.types[types[i]].health; // TODO: extract meta info
      this.teamStats.set(teams[i], statObj);
      hps[i] = this.meta.types[types[i]].health;
      prototypes[i] = (this.meta.buildingTypes.includes(types[i]) && types[i] != schema.BodyType.ARCHON) ? 1 : 0;
    }

    const locs = bodies.locs(this._vecTableSlot1)
    // Note: this allocates 6 objects with each call.
    // (One for the container, one for each TypedArray.)
    // All of the objects are small; the TypedArrays are basically
    // (pointer, length) pairs.
    // You can't reuse TypedArrays easily, so I'm inclined to
    // let this slide for now.

    // Initialize convictions

    // Insert bodies

    const levels = new Int8Array(bodies.robotIDsLength())
    levels.fill(1)

    this.bodies.insertBulk({
      id: bodies.robotIDsArray(),
      team: teams,
      type: types,
      x: locs.xsArray(),
      y: locs.ysArray(),
      flag: new Int32Array(bodies.robotIDsLength()),
      bytecodesUsed: new Int32Array(bodies.robotIDsLength()),
      action: (new Int8Array(bodies.robotIDsLength())).fill(-1),
      target: new Int32Array(bodies.robotIDsLength()),
      targetx: new Int32Array(bodies.robotIDsLength()),
      targety: new Int32Array(bodies.robotIDsLength()),
      bid: new Int32Array(bodies.robotIDsLength()),
      parent: new Int32Array(bodies.robotIDsLength()),
      hp: hps,
      level: levels,
      portable: new Int8Array(bodies.robotIDsLength()),
      prototype: prototypes,
    });
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

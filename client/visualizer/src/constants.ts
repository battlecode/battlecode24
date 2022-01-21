import { schema } from 'battlecode-playback'
import { Symmetry } from './mapeditor/index'

// Body types
export const ARCHON = schema.BodyType.ARCHON
export const BUILDER = schema.BodyType.BUILDER
export const LABORATORY = schema.BodyType.LABORATORY
export const MINER = schema.BodyType.MINER
export const SAGE = schema.BodyType.SAGE
export const SOLDIER = schema.BodyType.SOLDIER
export const WATCHTOWER = schema.BodyType.WATCHTOWER

export const bodyTypeList: number[] = [ARCHON, WATCHTOWER, LABORATORY, SOLDIER, BUILDER,  MINER, SAGE]
export const buildingTypeList: number[] = [ARCHON, LABORATORY, WATCHTOWER];
export const initialBodyTypeList: number[] = [ARCHON]
export const anomalyList = [0, 1, 2, 3]

export const bodyTypePriority: number[] = [] // for guns, drones, etc. that should be drawn over other robots

// export const TILE_COLORS: Array<number>[] = [
//   [168, 137, 97],
//   [147, 117, 77],
//   [88, 129, 87],
//   [58, 90, 64],
//   [52, 78, 65],
//   [11, 32, 39],
//   [8, 20, 20]
// ]

export const TILE_COLORS: Array<number>[] = [
  [204, 191, 173],
  [191, 179, 163],
  [184, 169, 151],
  [171, 157, 138],
  [161, 146, 127],
  [156, 143, 126],
  [145, 130, 110],
  [130, 117, 100],
  [122, 109,  91],
  [115, 102,  85],
  [102,  92,  75]
]
// flashy colors
// [0, 147, 83], // turquoise
// [29, 201, 2], // green
// [254, 205, 54], // yellow
// [222, 145, 1], // brown
// [255, 0, 0], // red
// [242, 0, 252] // pink

// Given passability, get index of tile to use.
export const getLevel = (x: number): number => {
  const nLev = TILE_COLORS.length
  const level = Math.floor((x + 9) / 10);
  return Math.min(nLev - 1, Math.max(0, level))
}

export const passiveInfluenceRate = (round: number): number => {
  //return Math.floor((1/50 + 0.03 * Math.exp(-0.001 * x)) * x); this one's for slanderers
  return Math.ceil(0.2 * Math.sqrt(round))
}

export const buffFactor = (numBuffs: number): number => {
  return 1 + 0.001 * numBuffs
}

export const ACTION_RADIUS_COLOR = "#46ff00"
export const VISION_RADIUS_COLOR = "#0000ff"

// Expected bot image size
//export const IMAGE_SIZE = 25

export function bodyTypeToSize(bodyType: schema.BodyType) {
  switch (bodyType) {
    case ARCHON:
      return 50
    case WATCHTOWER:
      return 50
    case BUILDER:
      return 25
    case MINER:
      return 35
    case SAGE:
      return 25
    case SOLDIER:
      return 35
    case LABORATORY:
      return 50
    default: throw new Error("invalid body type")
  }
}

// Game canvas rendering sizes
export const INDICATOR_DOT_SIZE = .3
export const INDICATOR_LINE_WIDTH = .3
export const SIGHT_RADIUS_LINE_WIDTH = .15

// Game canvas rendering parameters
export const EFFECT_STEP = 200 //time change between effect animations

// Map editor canvas parameters
export const DELTA = .0001
export const MIN_DIMENSION = 20
export const MAX_DIMENSION = 60

// Initial (default) HP of archons, for map editor
export const INITIAL_HP = 100

// Server settings
export const NUMBER_OF_TEAMS = 2
// export const VICTORY_POINT_THRESH = 1000;

// Other constants
// export const BULLET_THRESH = 10000;

// Maps available in the server.
// The key is the map name and the value is the type
export enum MapType {
  DEFAULT,
  SPRINT_1,
  SPRINT_2,
  QUALIFYING,
  HS_NEWBIE,
  FINAL,
  CUSTOM
};

// Map types to filter in runner
export const mapTypes: MapType[] = [MapType.DEFAULT,
MapType.SPRINT_1,
MapType.SPRINT_2,
MapType.QUALIFYING,
MapType.HS_NEWBIE,
MapType.FINAL,
MapType.CUSTOM]

export const SERVER_MAPS: Map<string, MapType> = new Map<string, MapType>([
  ["maptestsmall", MapType.DEFAULT],
  ["eckleburg", MapType.DEFAULT],
  ["intersection", MapType.DEFAULT],
  ["colosseum", MapType.SPRINT_1],
  ["fortress", MapType.SPRINT_1],
  ["jellyfish", MapType.SPRINT_1],
  ["nottestsmall", MapType.SPRINT_1],
  ["progress", MapType.SPRINT_1],
  ["rivers", MapType.SPRINT_1],
  ["sandwich", MapType.SPRINT_1],
  ["squer", MapType.SPRINT_1],
  ["uncomfortable", MapType.SPRINT_1],
  ["underground", MapType.SPRINT_1],
  ["valley", MapType.SPRINT_1],
  ["chessboard", MapType.SPRINT_2],
  ["collaboration", MapType.SPRINT_2],
  ["dodgeball", MapType.SPRINT_2],
  ["equals", MapType.SPRINT_2],
  ["highway", MapType.SPRINT_2],
  ["nyancat", MapType.SPRINT_2],
  ["panda", MapType.SPRINT_2],
  ["pillars", MapType.SPRINT_2],
  ["snowflake", MapType.SPRINT_2],
  ["spine", MapType.SPRINT_2],
  ["stronghold", MapType.SPRINT_2],
  ["tower", MapType.SPRINT_2]
])

export function bodyTypeToString(bodyType: schema.BodyType) {
  switch (bodyType) {
    case ARCHON:
      return "archon"
    case WATCHTOWER:
      return "watchtower"
    case BUILDER:
      return "builder"
    case MINER:
      return "miner"
    case SAGE:
      return "sage"
    case SOLDIER:
      return "soldier"
    case LABORATORY:
      return "laboratory"
    default: throw new Error("invalid body type")
  }
}

export function symmetryToString(symmetry: Symmetry) {
  switch (symmetry) {
    case Symmetry.ROTATIONAL: return "Rotational"
    case Symmetry.HORIZONTAL: return "Horizontal"
    case Symmetry.VERTICAL: return "Vertical"
    default: throw new Error("invalid symmetry")
  }
}

export function anomalyToString(anomaly: schema.Action) {
  switch (anomaly) {
    case 3:
      return "vortex"
    case 2:
      return "fury"
    case 0:
      return "abyss"
    case 1:
      return "charge"
    default: throw new Error("invalid anomaly")
  }
}

export function abilityToEffectString(effect: number): string | null {
  switch (effect) {
    case 1:
      return "empower"
    case 2:
      return "expose"
    case 3:
      return "embezzle"
    case 4:
      return "camouflage_red"
    case 5:
      return "camouflage_blue"
    default:
      return null
  }
}

// TODO: fix radius (is this vision that can be toggled in sidebar?)
export function radiusFromBodyType(bodyType: schema.BodyType) {
  return -1
  // switch(bodyType) {
  //   case MINER:
  //   case LANDSCAPER:
  //   case DRONE:
  //   case NET_GUN:
  //   case COW:
  //   case REFINERY:
  //   case VAPORATOR:
  //   case HQ:
  //   case DESIGN_SCHOOL:
  //   case FULFILLMENT_CENTER: return 1;
  //   default: throw new Error("invalid body type");
  // }
}

// export function waterLevel(x: number) {
//   return (Math.exp(0.0028*x-1.38*Math.sin(0.00157*x-1.73)+1.38*Math.sin(-1.73))-1)
// }

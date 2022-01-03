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

export const bodyTypeList: number[] = [ARCHON, BUILDER, LABORATORY, MINER, SAGE, SOLDIER, WATCHTOWER]
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
      return 40
    case WATCHTOWER:
      return 40
    case BUILDER:
      return 25
    case MINER:
      return 35
    case SAGE:
      return 25
    case SOLDIER:
      return 35
    case LABORATORY:
      return 40
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
export const MIN_DIMENSION = 15
export const MAX_DIMENSION = 100

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
  ["circle", MapType.DEFAULT],
  ["quadrants", MapType.DEFAULT],
  ["Andromeda", MapType.SPRINT_1],
  ["Arena", MapType.SPRINT_1],
  ["Bog", MapType.SPRINT_1],
  ["Branches", MapType.SPRINT_1],
  ["Chevron", MapType.SPRINT_1],
  ["Corridor", MapType.SPRINT_1],
  ["Cow", MapType.SPRINT_1],
  ["CrossStitch", MapType.SPRINT_1],
  ["CrownJewels", MapType.SPRINT_1],
  ["ExesAndOhs", MapType.SPRINT_1],
  ["FiveOfHearts", MapType.SPRINT_1],
  ["Gridlock", MapType.SPRINT_1],
  ["Illusion", MapType.SPRINT_1],
  ["NotAPuzzle", MapType.SPRINT_1],
  ["Rainbow", MapType.SPRINT_1],
  ["SlowMusic", MapType.SPRINT_1],
  ["Snowflake", MapType.SPRINT_1],
  ["BadSnowflake", MapType.SPRINT_2],
  ["CringyAsF", MapType.SPRINT_2],
  ["FindYourWay", MapType.SPRINT_2],
  ["GetShrekt", MapType.SPRINT_2],
  ["Goldfish", MapType.SPRINT_2],
  ["HexesAndOhms", MapType.SPRINT_2],
  ["Licc", MapType.SPRINT_2],
  ["MainCampus", MapType.SPRINT_2],
  ["Punctuation", MapType.SPRINT_2],
  ["Radial", MapType.SPRINT_2],
  ["SeaFloor", MapType.SPRINT_2],
  ["Sediment", MapType.SPRINT_2],
  ["Smile", MapType.SPRINT_2],
  ["SpaceInvaders", MapType.SPRINT_2],
  ["Surprised", MapType.SPRINT_2],
  ["VideoGames", MapType.SPRINT_2],
  ["AmidstWe", MapType.QUALIFYING],
  ["BattleCode", MapType.QUALIFYING],
  ["BattleCodeToo", MapType.QUALIFYING],
  ["BlobWithLegs", MapType.QUALIFYING],
  ["ButtonsAndBows", MapType.QUALIFYING],
  ["CowTwister", MapType.QUALIFYING],
  ["Extensions", MapType.QUALIFYING],
  ["Hourglass", MapType.QUALIFYING],
  ["Maze", MapType.QUALIFYING],
  ["NextHouse", MapType.QUALIFYING],
  ["Superposition", MapType.QUALIFYING],
  ["TicTacTie", MapType.QUALIFYING],
  ["UnbrandedWordGame", MapType.QUALIFYING],
  ["Z", MapType.QUALIFYING],
  ["Zodiac", MapType.QUALIFYING],
  ["Flawars", MapType.HS_NEWBIE],
  ["FrogOrBath", MapType.HS_NEWBIE],
  ["HappyBoba", MapType.HS_NEWBIE],
  ["Networking", MapType.HS_NEWBIE],
  ["NoInternet", MapType.HS_NEWBIE],
  ["PaperWindmill", MapType.HS_NEWBIE],
  ["Randomized", MapType.HS_NEWBIE],
  ["Star", MapType.HS_NEWBIE],
  ["Tiger", MapType.HS_NEWBIE],
  ["WhatISeeInMyDreams", MapType.HS_NEWBIE],
  ["Yoda", MapType.HS_NEWBIE],
  ["Blotches", MapType.FINAL],
  ["CToE", MapType.FINAL],
  ["Circles", MapType.FINAL],
  ["EggCarton", MapType.FINAL],
  ["InaccurateBritishFlag", MapType.FINAL],
  ["JerryIsEvil", MapType.FINAL],
  ["Legends", MapType.FINAL],
  ["Mario", MapType.FINAL],
  ["Misdirection", MapType.FINAL],
  ["OneCallAway", MapType.FINAL],
  ["Saturn", MapType.FINAL],
  ["Stonks", MapType.FINAL],
  ["TheClientMapEditorIsSuperiorToGoogleSheetsEom", MapType.FINAL],
  ["TheSnackThatSmilesBack", MapType.FINAL]
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

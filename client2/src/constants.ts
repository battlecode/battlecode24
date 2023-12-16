import { schema } from 'battlecode-schema'

/*
 * General constants
 */
export const BATTLECODE_YEAR: number = 2024
export const DIRECTIONS: Record<number, Array<number>> = {
    0: [0, 0],
    1: [-1, 0],
    2: [-1, -1],
    3: [0, -1],
    4: [1, -1],
    5: [1, 0],
    6: [1, 1],
    7: [0, 1],
    8: [-1, 1]
}

export const SERVER_MAPS: string[] = [
    'Test map 1',
    'Test map 2',
    'Test map 3',
    'Test map 4',
    'Test map 5',
    'Test map 6',
    'AllElements',
    'DefaultMap',
    'maptestsmall'
]
/*
 * Color constants (defined in tailwind.config.js as well)
 */
export const TEAM_WHITE = '#bfbaa8'
export const TEAM_BROWN = '#b99c76'
export const TEAM_COLORS = [TEAM_WHITE, TEAM_BROWN]
export const TEAM_COLOR_NAMES = ['White', 'Brown']

export const WATER_COLOR = '#1d4f6c' // brighter version '#29B0D9'
export const WALLS_COLOR = '#3B6B4C'
export const DIVIDER_COLOR = '#7b4724'
export const GRASS_COLOR = '#153e30' // brighter version '#3CCA6E'
export const GAMEAREA_BACKGROUND = WATER_COLOR

export const DIVIDER_DROP_TURN = 200

export const ATTACK_COLOR = '#db6b5c'
export const BUILD_COLOR = '#c573c9'
export const HEAL_COLOR = '#f2b804'
export const SPECIALTY_COLORS = [ATTACK_COLOR, BUILD_COLOR, HEAL_COLOR]

/*
 * Renderer constants
 */
export const TILE_RESOLUTION: number = 50 // Pixels per axis per tile
export const TOOLTIP_PATH_LENGTH = 8
export const TOOLTIP_PATH_INIT_R = 0.2
export const TOOLTIP_PATH_DECAY_R = 0.9
export const TOOLTIP_PATH_DECAY_OPACITY = 0.95

// Map build types to image filenames
export const BUILD_NAMES: Record<schema.BuildActionType, string> = {
    [schema.BuildActionType.EXPLOSIVE_TRAP]: 'explosive',
    [schema.BuildActionType.WATER_TRAP]: 'water',
    [schema.BuildActionType.STUN_TRAP]: 'stun',
    [schema.BuildActionType.DIG]: '',
    [schema.BuildActionType.FILL]: ''
}

export const MAP_SIZE_RANGE = {
    min: 20,
    max: 60
}

export const SPEC_VERSION = '0.0.1'

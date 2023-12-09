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
export const TEAM_BLUE = '#407496'
export const TEAM_RED = '#D53E43'
export const TEAM_COLORS = [TEAM_RED, TEAM_BLUE]
export const TEAM_NAMES = ['Red', 'Blue']

export const WATER_COLOR = '#29B0D9'
export const WALLS_COLOR = '#3B6B4C'
export const DIVIDER_COLOR = '#FF5F15'
export const GRASS_COLOR = '#3CCA6E'
export const GAMEAREA_BACKGROUND = WATER_COLOR

export const ATTACK_COLOR = '#db6b5c'
export const BUILD_COLOR = '#c573c9'
export const HEAL_COLOR = '#f2b804'
/*
 * Renderer constants
 */
export const TILE_RESOLUTION: number = 50 // Pixels per axis per tile
export const TOOLTIP_PATH_LENGTH = 8
export const TOOLTIP_PATH_INIT_R = 0.2
export const TOOLTIP_PATH_DECAY_R = 0.9
export const TOOLTIP_PATH_DECAY_OPACITY = 0.95

// currently just used to get the file names from the resource id
export const RESOURCE_NAMES: Record<number, string> = {
    1: 'adamantium',
    2: 'mana',
    3: 'elixir'
}

export const MAP_SIZE_RANGE = {
    min: 20,
    max: 60
}

export const SPEC_VERSION = '0.0.1'

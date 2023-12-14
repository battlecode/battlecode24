import { schema, flatbuffers } from 'battlecode-schema'
import Game from '../../../playback/Game'
import Match from '../../../playback/Match'
import { CurrentMap, StaticMap } from '../../../playback/Map'
import Turn from '../../../playback/Turn'
import Bodies from '../../../playback/Bodies'
import { BATTLECODE_YEAR, DIRECTIONS } from '../../../constants'

export function loadFileAsMap(file: File): Promise<Game> {
    return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.readAsArrayBuffer(file)
        reader.onload = () => {
            const data = new Uint8Array(reader.result as ArrayBuffer)
            const schema_map = schema.GameMap.getRootAsGameMap(new flatbuffers.ByteBuffer(data))
            const game = new Game()
            const map = StaticMap.fromSchema(schema_map)
            game.currentMatch = Match.fromMap(schema_map, game, map)
            resolve(game)
        }
    })
}

export function exportMap(turn: Turn) {
    if (!verifyMapGuarantees(turn)) return

    let name = prompt('Enter a name for this map') ?? 'Untitled'
    turn.map.staticMap.name = name

    const data = mapToFile(turn.map, turn.bodies)
    exportFile(data, name + `.map${BATTLECODE_YEAR % 100}`)
}

function verifyMapGuarantees(turn: Turn) {
    if (turn.map.isEmpty() && turn.bodies.isEmpty()) {
        alert('Map is empty')
        return false
    }

    const spawnZoneCount = turn.map.staticMap.spawnLocations.length
    if (spawnZoneCount !== 6) {
        alert(`Map has ${spawnZoneCount} spawn zones. Must have exactly 6`)
        return false
    }

    for (let i = 0; i < spawnZoneCount; i++) {
        for (let j = i + 1; j < spawnZoneCount; j++) {
            const distSquared =
                Math.pow(turn.map.staticMap.spawnLocations[i].x - turn.map.staticMap.spawnLocations[j].x, 2) +
                Math.pow(turn.map.staticMap.spawnLocations[i].y - turn.map.staticMap.spawnLocations[j].y, 2)
            if (distSquared < 36) {
                alert(
                    `Spawn zones ${i} and ${j} are too close together, they must be at least sqrt(36) units apart (6 tiles)`
                )
                return false
            }
        }
    }

    let totalSpawnableLocations = 0
    for (let i = 0; i < spawnZoneCount; i++) {
        const loc = turn.map.staticMap.spawnLocations[i]
        const mapIdx = turn.map.locationToIndex(loc.x, loc.y)
        for (let x = loc.x - 1; x <= loc.x + 1; x++) {
            for (let y = loc.y - 1; y <= loc.y + 1; y++) {
                if (
                    x >= 0 &&
                    x < turn.map.width &&
                    y >= 0 &&
                    y < turn.map.height &&
                    !turn.map.water[mapIdx] &&
                    !turn.map.staticMap.walls[mapIdx] &&
                    !turn.map.staticMap.divider[mapIdx]
                ) {
                    totalSpawnableLocations++
                }
            }
        }
    }
    if (totalSpawnableLocations < 18) {
        alert(`Map has ${totalSpawnableLocations} spawnable locations. Must have at least 9 for each team`)
        return false
    }

    const floodMask = new Int8Array(turn.map.width * turn.map.height)
    const floodQueue: number[] = []
    const spawnZone = turn.map.staticMap.spawnLocations[0]
    const spawnZoneIdx = turn.map.locationToIndex(spawnZone.x, spawnZone.y)
    floodMask[spawnZoneIdx] = 1
    floodQueue.push(spawnZoneIdx)
    let totalFlooded = 1
    while (floodQueue.length > 0) {
        const idx = floodQueue.shift()!
        for (let i = 1; i < 9; i++) {
            const x = DIRECTIONS[i][0] + turn.map.indexToLocation(idx).x
            const y = DIRECTIONS[i][1] + turn.map.indexToLocation(idx).y
            if (x < 0 || x >= turn.map.width || y < 0 || y >= turn.map.height) continue
            const newIdx = turn.map.locationToIndex(x, y)
            if (!turn.map.staticMap.divider[newIdx] && !floodMask[newIdx]) {
                floodMask[newIdx] = 1
                floodQueue.push(newIdx)
                totalFlooded++
            }
        }
    }
    if (totalFlooded >= 0.5 * turn.map.width * turn.map.height) {
        alert(`Map is too open. Must be divided into at least 2 sections by the divider`)
        return false
    }

    return true
}

/**
 * The order in which the data is written is important. When we change the schema, this may need to be refactored
 * Only one table or object can be created at once, so we have to create the bodies table first, then start the actual map object
 */
function mapToFile(currentMap: CurrentMap, initialBodies: Bodies): Uint8Array {
    const builder = new flatbuffers.Builder()
    const name = builder.createString(currentMap.staticMap.name)
    const initialBodiesTable = initialBodies.toSpawnedBodyTable(builder)
    const mapPacket = currentMap.getSchemaPacket(builder)

    schema.GameMap.startGameMap(builder)
    schema.GameMap.addName(builder, name)
    schema.GameMap.addSize(builder, schema.Vec.createVec(builder, currentMap.width, currentMap.height))
    schema.GameMap.addSymmetry(builder, currentMap.staticMap.symmetry)
    schema.GameMap.addBodies(builder, initialBodiesTable)
    schema.GameMap.addRandomSeed(builder, Math.round(Math.random() * 1000))
    currentMap.insertSchemaPacket(builder, mapPacket)

    builder.finish(schema.GameMap.endGameMap(builder))
    return builder.asUint8Array()
}

function exportFile(data: Uint8Array, fileName: string) {
    const mimeType = 'application/octet-stream'
    const blob = new Blob([data], { type: mimeType })
    const url = window.URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.style.display = 'none'
    link.click()
    link.remove()

    setTimeout(function () {
        return window.URL.revokeObjectURL(url)
    }, 30000)
}

import { schema, flatbuffers } from 'battlecode-schema'
import Game from '../../../playback/Game'
import Match from '../../../playback/Match'
import { CurrentMap, StaticMap } from '../../../playback/Map'
import Turn from '../../../playback/Turn'
import Bodies from '../../../playback/Bodies'
import { current } from 'tailwindcss/colors'
import { BATTLECODE_YEAR } from '../../../constants'

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

export function saveMapToFile(turn: Turn, fileName: string) {
    const data = mapToFile(turn.map, turn.bodies)
    exportFile(data, fileName + `.map${BATTLECODE_YEAR % 100}`)
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
    schema.GameMap.addMinCorner(builder, schema.Vec.createVec(builder, 0, 0))
    schema.GameMap.addMaxCorner(builder, schema.Vec.createVec(builder, currentMap.width, currentMap.height))
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

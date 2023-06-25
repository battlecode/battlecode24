import React, { useEffect } from 'react'
import { CurrentMap, StaticMap } from '../../../playback/Map'
import { MapEditorBrushRow } from './map-editor-brushes'
import Bodies from '../../../playback/Bodies'
import Game from '../../../playback/Game'
import { Button, BrightButton } from '../../button'
import { NumInput, Select } from '../../forms'
import { useAppContext } from '../../../app-context'
import Match from '../../../playback/Match'
import { EventType, useListenEvent } from '../../../app-events'
import { MapEditorBrush } from './MapEditorBrush'

const MIN_MAP_SIZE = 20
const MAX_MAP_SIZE = 60

type MapParams = {
    width: number
    height: number
    symmetry: number
    import_from?: ArrayBuffer
}

export const MapEditorPage: React.FC = () => {
    const context = useAppContext()
    const [mapParams, setMapParams] = React.useState<MapParams>({ width: 30, height: 30, symmetry: 0 })
    const [brushes, setBrushes] = React.useState<MapEditorBrush[]>([])

    const openBrush = brushes.find((b) => b.open)

    const setOpenBrush = (brush: MapEditorBrush | null) => {
        setBrushes(brushes.map((b) => b.opened(b === brush)))
    }

    const applyBrush = (point: { x: number; y: number }) => {
        if (openBrush) openBrush.apply(point.x, point.y, openBrush.fields)
    }

    useListenEvent(EventType.TILE_CLICK, applyBrush, [brushes])
    useListenEvent(EventType.TILE_DRAG, applyBrush, [brushes])

    useEffect(() => {
        const game = new Game()
        const map = StaticMap.fromParams(mapParams.width, mapParams.height, mapParams.symmetry)
        const bodies = new Bodies(game)
        game.currentMatch = Match.createBlank(game, bodies, map)
        context.setState({
            ...context.state,
            activeGame: game,
            activeMatch: game.currentMatch
        })

        // these do not need to change ever really, but its fine to do it when map params are changed
        const brushes = game.currentMatch.currentTurn.map.getEditorBrushes().concat(bodies.getEditorBrushes())
        brushes[0].open = true
        setBrushes(brushes)
    }, [mapParams])

    const changeWidth = (newWidth: number) => {
        newWidth = Math.max(MIN_MAP_SIZE, Math.min(MAX_MAP_SIZE, newWidth))
        setMapParams({ ...mapParams, width: newWidth })
    }
    const changeHeight = (newHeight: number) => {
        newHeight = Math.max(MIN_MAP_SIZE, Math.min(MAX_MAP_SIZE, newHeight))
        setMapParams({ ...mapParams, height: newHeight })
    }
    const changeSymmetry = (symmetry: string) => {
        const symmetryInt = parseInt(symmetry)
        if (symmetryInt < 0 || symmetryInt > 2) throw new Error('invalid symmetry value')
        setMapParams({ ...mapParams, symmetry: symmetryInt })
    }

    const inputRef = React.useRef<HTMLInputElement>(null)
    const fileUploaded = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!e.target.files || e.target.files.length == 0) return
        const file = e.target.files[0]
        const reader = new FileReader()
        reader.onload = () => {
            setMapParams({
                width: file.width,
                height: file.height,
                symmetry: file.symmetry,
                import_from: reader.result as ArrayBuffer
            })
        }
        reader.readAsArrayBuffer(file)
    }

    return (
        <>
            <input type="file" hidden ref={inputRef} onChange={fileUploaded} />

            <div className="flex flex-col flex-grow">
                {brushes.map((brush, i) => (
                    <MapEditorBrushRow
                        key={i}
                        brush={brush}
                        open={brush == openBrush}
                        onClick={() => {
                            if (brush == openBrush) setOpenBrush(null)
                            else setOpenBrush(brush)
                        }}
                    />
                ))}

                <div className="flex flex-row mt-10 items-center justify-center">
                    <span className="mr-2 text-sm">Width: </span>
                    <NumInput value={mapParams.width} changeValue={changeWidth} min={MIN_MAP_SIZE} max={MAX_MAP_SIZE} />
                    <span className="ml-3 mr-2 text-sm">Height: </span>
                    <NumInput
                        value={mapParams.height}
                        changeValue={changeHeight}
                        min={MIN_MAP_SIZE}
                        max={MAX_MAP_SIZE}
                    />
                </div>
                <div className="flex flex-row mt-3 items-center justify-center">
                    <span className="mr-5 text-sm">Symmetry: </span>
                    <Select onChange={changeSymmetry} value={mapParams.symmetry}>
                        <option value="0">Rotational</option>
                        <option value="1">Horizontal</option>
                        <option value="2">Vertical</option>
                    </Select>
                </div>

                <div className="flex flex-row mt-8">
                    <BrightButton onClick={() => alert('ask for name and stuff')}>Export</BrightButton>
                    <Button onClick={() => inputFile.current?.click()}>Import</Button>
                </div>
            </div>
        </>
    )
}

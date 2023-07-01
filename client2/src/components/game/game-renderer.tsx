import React from 'react'
import { useAppContext } from '../../app-context'
import { Vector } from '../../playback/Vector'
import { EventType, publishEvent, useListenEvent } from '../../app-events'
import * as cst from '../../constants'

export enum CanvasType {
    BACKGROUND = 'BACKGROUND',
    DYNAMIC = 'DYNAMIC',
    OVERLAY = 'OVERLAY'
}

const CANVAS_Z_INDICES = [0, 1, 2]

export const GameRenderer: React.FC = () => {
    const wrapperRef = React.useRef(null)
    const appContext = useAppContext()
    const canvases = React.useRef({} as Record<string, HTMLCanvasElement | null>)
    const game = appContext.state.activeGame

    const getCanvasContext = (ct: CanvasType) => {
        return canvases.current[ct]?.getContext('2d')
    }

    // TODO: could potentially have performance settings that allows rendering
    // at a lower resolution and then upscaling
    const updateCanvasDimensions = (ct: CanvasType, dims: Vector) => {
        const elem = canvases.current[ct]
        if (!elem) return
        elem.width = dims.x * cst.TILE_RESOLUTION
        elem.height = dims.y * cst.TILE_RESOLUTION
        elem.getContext('2d')?.scale(cst.TILE_RESOLUTION, cst.TILE_RESOLUTION)
    }

    // Since this is a callback, we need to ensure we recreate the function when the
    // active match changes. Similarly, the event listener needs to be updated, which
    // will happen automatically via dependencies
    const render = React.useCallback(() => {
        const match = appContext.state.activeMatch
        if (!match) return

        const ctx = getCanvasContext(CanvasType.DYNAMIC)!
        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)
        match.currentTurn.map.draw(ctx)
        match.currentTurn.bodies.draw(match.currentTurn, ctx)
        match.currentTurn.actions.draw(match.currentTurn, ctx)
    }, [appContext.state.activeMatch])
    useListenEvent(EventType.RENDER, render, [appContext.state.activeMatch])

    // We want to rerender if the match changes
    React.useEffect(() => {
        const match = appContext.state.activeMatch
        if (!match) return

        // Update canvases to reflect board size of new match
        updateCanvasDimensions(CanvasType.BACKGROUND, {
            x: match.currentTurn.map.width,
            y: match.currentTurn.map.height
        })
        updateCanvasDimensions(CanvasType.DYNAMIC, { x: match.currentTurn.map.width, y: match.currentTurn.map.height })

        // Redraw static background
        match.currentTurn.map.staticMap.draw(getCanvasContext(CanvasType.BACKGROUND)!)

        publishEvent(EventType.RENDER, {})
    }, [appContext.state.activeMatch])

    if (!canvases) return <></>

    // TODO: better support for strange aspect ratios, for now it is fine
    return (
        <div className="w-full h-screen flex items-center justify-center">
            {!game || !game.currentMatch ? (
                <p className="text-white text-center">Select a game from the queue</p>
            ) : (
                <div ref={wrapperRef} className="relative w-full h-full">
                    {Object.getOwnPropertyNames(CanvasType).map((ct, idx) => (
                        <canvas
                            className="absolute top-1/2 left-1/2 h-full max-w-full max-h-full aspect-square"
                            style={{
                                transform: 'translate(-50%, -50%)',
                                zIndex: CANVAS_Z_INDICES[idx]
                            }}
                            key={`canv${ct}`}
                            ref={(ref) => (canvases.current[ct] = ref)}
                        />
                    ))}
                </div>
            )}
        </div>
    )
}

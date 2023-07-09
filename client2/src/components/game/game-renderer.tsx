import React from 'react'
import { useAppContext } from '../../app-context'
import { Vector } from '../../playback/Vector'
import * as cst from '../../constants'
import Tooltip from './tooltip'

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
    const [tooltipCanvas, setTooltipCanvas] = React.useState<HTMLCanvasElement>()

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

    React.useEffect(() => {
        const match = appContext.state.activeMatch
        if (!match) return
        /*
         * If this isnt running at a regular interval (in general, we should probably have it only draw on changes),
         * then we need to make it also draw on image load (see imageloader.triggerOnImageLoad()) unless we decide to
         * block until all images are loaded (which is probably a bad idea)
         */
        const render = () => {
            const turn = match.currentTurn

            updateCanvasDimensions(CanvasType.BACKGROUND, { x: turn.map.width, y: turn.map.height })
            let ctx = getCanvasContext(CanvasType.BACKGROUND)!
            match.currentTurn.map.staticMap.draw(ctx)

            updateCanvasDimensions(CanvasType.DYNAMIC, { x: turn.map.width, y: turn.map.height })
            ctx = getCanvasContext(CanvasType.DYNAMIC)!
            match.currentTurn.map.draw(ctx)
            match.currentTurn.bodies.draw(match.currentTurn, ctx)
            match.currentTurn.actions.draw(match.currentTurn, ctx)

            updateCanvasDimensions(CanvasType.OVERLAY, { x: turn.map.width, y: turn.map.height })
            ctx = getCanvasContext(CanvasType.OVERLAY)!
        }

        const renderInterval = setInterval(render, 100)

        return () => {
            clearInterval(renderInterval)
        }
    }, [canvases, appContext.state.activeMatch])

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
                            className="absolute top-1/2 left-1/2 h-full max-w-full max-h-full"
                            style={{
                                transform: 'translate(-50%, -50%)',
                                zIndex: CANVAS_Z_INDICES[idx],
                            }}
                            key={`canv${ct}`}
                            ref={(ref) => {
                                canvases.current[ct] = ref
                                // TODO: there's def a better way to do this but idk how rn
                                if (ct == CanvasType.OVERLAY && ref && tooltipCanvas !== ref) {
                                    setTooltipCanvas(ref)
                                }
                            }}
                        />
                    ))}
                    <Tooltip canvas={tooltipCanvas} wrapperRef={wrapperRef} />
                </div>
            )}
        </div>
    )
}

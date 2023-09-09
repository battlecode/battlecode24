import React from 'react'
import { useAppContext } from '../../app-context'
import { Vector } from '../../playback/Vector'
import * as cst from '../../constants'
import { publishEvent, EventType } from '../../app-events'
import assert from 'assert'

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

    React.useEffect(() => {
        const match = appContext.state.activeMatch
        if (!match) return
        /*
         * If this isnt running at a regular interval (in general, we should probably have it only draw on changes),
         * then we need to make it also draw on image load (see imageloader.triggerOnImageLoad()) unless we decide to
         * block until all images are loaded (which is probably a bad idea)
         */
        // match.jumpToTurn(1244)
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
        }

        const renderInterval = setInterval(render, 100)

        const noContextMenu = (e: MouseEvent) => {
            e.preventDefault()
        }
        if (canvases.current) {
            Object.values(canvases.current).forEach((canvas) => {
                canvas!.addEventListener('contextmenu', noContextMenu)
            })
        }

        return () => {
            clearInterval(renderInterval)

            if (canvases.current) {
                Object.values(canvases.current).forEach((canvas) => {
                    canvas!.removeEventListener('contextmenu', noContextMenu)
                })
            }
        }
    }, [canvases, appContext.state.activeMatch])

    const eventToPoint = (e: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        const canvas = e.target as HTMLCanvasElement
        const rect = canvas.getBoundingClientRect()
        const map = game!.currentMatch!.currentTurn!.map ?? assert.fail('map is null in onclick')
        let x = Math.floor(((e.clientX - rect.left) / rect.width) * map.width)
        let y = Math.floor((1 - (e.clientY - rect.top) / rect.height) * map.height)
        x = Math.max(0, Math.min(x, map.width - 1))
        y = Math.max(0, Math.min(y, map.height - 1))
        return { x: x, y: y }
    }

    const onCanvasClick = (e: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        publishEvent(EventType.TILE_CLICK, eventToPoint(e))
    }

    const mouseDown = React.useRef(false)
    const lastFiredDragEvent = React.useRef({ x: -1, y: -1 })
    const onMouseUp = () => {
        mouseDown.current = false
        lastFiredDragEvent.current = { x: -1, y: -1 }
    }
    const onCanvasDrag = (e: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        const point = eventToPoint(e)
        if (point.x === lastFiredDragEvent.current.x && point.y === lastFiredDragEvent.current.y) return
        lastFiredDragEvent.current = point
        publishEvent(EventType.TILE_DRAG, point)
    }

    const mouseDownRightPrev = React.useRef(false)
    const mouseDownRight = (down: boolean, e?: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        if (down === mouseDownRightPrev.current) return
        mouseDownRightPrev.current = down
        if (!down && e) onCanvasClick(e)
        publishEvent(EventType.CANVAS_RIGHT_CLICK, { down: down })
    }

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
                            onClick={onCanvasClick}
                            onMouseMove={(e) => {
                                if (mouseDown.current) onCanvasDrag(e)
                            }}
                            onMouseDown={() => {
                                mouseDown.current = true
                            }}
                            onMouseUp={() => onMouseUp}
                            onMouseLeave={(e) => {
                                onMouseUp()
                                mouseDownRight(false)
                            }}
                            onMouseDownCapture={(e) => {
                                if (e.button == 2) mouseDownRight(true, e)
                            }}
                            onMouseUpCapture={(e) => {
                                onMouseUp()
                                if (e.button == 2) mouseDownRight(false, e)
                            }}
                        />
                    ))}
                </div>
            )}
        </div>
    )
}

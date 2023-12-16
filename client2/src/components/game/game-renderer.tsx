import React, { useEffect } from 'react'
import { useAppContext } from '../../app-context'
import { Vector } from '../../playback/Vector'
import { EventType, publishEvent, useListenEvent } from '../../app-events'
import * as cst from '../../constants'
import assert from 'assert'
import Tooltip from './tooltip'
import { Body } from '../../playback/Bodies'
import Match from '../../playback/Match'

export enum CanvasType {
    BACKGROUND = 'BACKGROUND',
    DYNAMIC = 'DYNAMIC',
    OVERLAY = 'OVERLAY'
}

const CANVAS_Z_INDICES = [0, 1, 2]

export const GameRenderer: React.FC = () => {
    const wrapperRef = React.useRef(null)
    const canvases = React.useRef({} as Record<string, HTMLCanvasElement | null>)

    const appContext = useAppContext()
    const { activeGame, activeMatch } = appContext.state

    const [selectedBody, setSelectedBody] = React.useState<Body | undefined>(undefined)
    const [hoveredTile, setHoveredTile] = React.useState<{ x: number; y: number } | undefined>(undefined)

    const getCanvasContext = (ct: CanvasType) => {
        return canvases.current[ct]?.getContext('2d')
    }

    const getCanvas = (ct: CanvasType) => {
        return canvases.current[ct] || undefined
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

    const drawBodyPath = (match: Match, ctx: CanvasRenderingContext2D, body: Body) => {
        const interpolatedCoords = body.getInterpolatedCoords(match.currentTurn)

        let alphaValue = 1
        let radius = cst.TOOLTIP_PATH_INIT_R
        let lastPos: Vector = { x: -1, y: -1 }

        for (const prevPos of [interpolatedCoords].concat(body.prevSquares.slice().reverse())) {
            const color = `rgba(255, 255, 255, ${alphaValue})`

            ctx.beginPath()
            ctx.fillStyle = color
            ctx.ellipse(prevPos.x + 0.5, match.map.height - (prevPos.y + 0.5), radius, radius, 0, 0, 360)
            ctx.fill()

            alphaValue *= cst.TOOLTIP_PATH_DECAY_OPACITY
            radius *= cst.TOOLTIP_PATH_DECAY_R

            if (lastPos.x != -1 && lastPos.y != -1) {
                ctx.beginPath()
                ctx.strokeStyle = color
                ctx.lineWidth = radius / 2

                ctx.moveTo(lastPos.x + 0.5, match.map.height - (lastPos.y + 0.5))
                ctx.lineTo(prevPos.x + 0.5, match.map.height - (prevPos.y + 0.5))

                ctx.stroke()
            }

            lastPos = prevPos
        }
    }

    const drawHoveredTile = (ctx: CanvasRenderingContext2D, tileWidth: number, tileHeight: number) => {
        if (!hoveredTile) return
        const { x, y } = hoveredTile
        ctx.beginPath()
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.8)'
        ctx.strokeRect(x * tileWidth, y * tileHeight, tileWidth, tileHeight)
    }

    let hoveredBody: Body | undefined = undefined
    if (appContext.state.activeMatch?.currentTurn?.bodies && hoveredTile)
        hoveredBody = appContext.state.activeMatch?.currentTurn?.bodies.getBodyAtLocation(
            hoveredTile.x,
            hoveredTile.y
        )

    const renderOverlay = () => {
        const overlayCanvas = getCanvas(CanvasType.OVERLAY)
        const ctx = getCanvasContext(CanvasType.DYNAMIC)
        const overlayCtx = getCanvasContext(CanvasType.OVERLAY)
        if (!activeMatch || !overlayCanvas || !ctx || !overlayCtx) return
        const map = activeMatch.currentTurn.map
        overlayCtx.clearRect(0, 0, overlayCtx.canvas.width, overlayCtx.canvas.height)
        if (selectedBody) drawBodyPath(activeMatch, overlayCtx, selectedBody)
        drawHoveredTile(overlayCtx, overlayCanvas.width / map.width, overlayCanvas.height / map.height)
    }
    useEffect(renderOverlay, [hoveredTile])

    const render = React.useCallback(() => {
        const ctx = getCanvasContext(CanvasType.DYNAMIC)
        if (!activeMatch || !ctx) return

        const currentTurn = activeMatch.currentTurn
        const map = currentTurn.map

        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)
        map.draw(activeMatch, ctx, appContext.state.config, selectedBody)
        currentTurn.bodies.draw(activeMatch, ctx, appContext.state.config, selectedBody, hoveredBody)
        currentTurn.actions.draw(activeMatch, ctx)

        renderOverlay()
    }, [activeMatch])
    useListenEvent(EventType.RENDER, render, [render])

    const fullRender = () => {
        const match = appContext.state.activeMatch
        if (!match) return
        match.currentTurn.map.staticMap.draw(getCanvasContext(CanvasType.BACKGROUND)!)
        render()
    }
    useListenEvent(EventType.INITIAL_RENDER, fullRender, [fullRender])

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
        updateCanvasDimensions(CanvasType.OVERLAY, { x: match.currentTurn.map.width, y: match.currentTurn.map.height })

        const noContextMenu = (e: MouseEvent) => {
            e.preventDefault()
        }
        if (canvases.current) {
            Object.values(canvases.current)
                .filter((c) => c)
                .forEach((canvas) => {
                    canvas!.addEventListener('contextmenu', noContextMenu)
                })
        }

        publishEvent(EventType.INITIAL_RENDER, {})

        return () => {
            if (canvases.current) {
                Object.values(canvases.current)
                    .filter((c) => c)
                    .forEach((canvas) => {
                        canvas!.removeEventListener('contextmenu', noContextMenu)
                    })
            }
        }
    }, [canvases, appContext.state.activeMatch])

    const eventToPoint = (e: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        const canvas = e.target as HTMLCanvasElement
        const rect = canvas.getBoundingClientRect()
        const map = activeGame!.currentMatch!.currentTurn!.map ?? assert.fail('map is null in onclick')
        let x = Math.floor(((e.clientX - rect.left) / rect.width) * map.width)
        let y = Math.floor((1 - (e.clientY - rect.top) / rect.height) * map.height)
        x = Math.max(0, Math.min(x, map.width - 1))
        y = Math.max(0, Math.min(y, map.height - 1))
        return { x: x, y: y }
    }

    const onCanvasClick = (e: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        const point = eventToPoint(e)
        const clickedBody = activeGame?.currentMatch?.currentTurn?.bodies.getBodyAtLocation(point.x, point.y)
        setSelectedBody(clickedBody)
        publishEvent(EventType.TILE_CLICK, point)
    }

    const mouseDown = React.useRef(false)
    const lastFiredDragEvent = React.useRef({ x: -1, y: -1 })
    const onMouseUp = () => {
        mouseDown.current = false
        lastFiredDragEvent.current = { x: -1, y: -1 }
    }
    const onMouseDown = (e: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        mouseDown.current = true
    }
    const onMouseMove = (e: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        setHoveredTile(eventToPoint(e))
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

    return (
        <div className="w-full h-screen flex items-center justify-center">
            {!activeGame || !activeGame.currentMatch ? (
                <p className="text-white text-center">Select a game from the queue</p>
            ) : (
                <div ref={wrapperRef} className="relative w-full h-full">
                    {Object.getOwnPropertyNames(CanvasType).map((ct, idx) => (
                        <canvas
                            className="absolute top-1/2 left-1/2 h-full max-w-full max-h-full"
                            style={{
                                transform: 'translate(-50%, -50%)',
                                zIndex: CANVAS_Z_INDICES[idx],
                                cursor: 'pointer'
                            }}
                            key={`canv${ct}`}
                            ref={(ref) => {
                                canvases.current[ct] = ref
                            }}
                            {...(ct === CanvasType.OVERLAY && {
                                onClick: onCanvasClick,
                                onMouseMove: (e) => {
                                    if (mouseDown.current) onCanvasDrag(e)
                                    onMouseMove(e)
                                },
                                onMouseDown: onMouseDown,
                                onMouseUp: onMouseUp,
                                onMouseLeave: (e) => {
                                    onMouseUp()
                                    mouseDownRight(false)
                                },
                                onMouseEnter: (e) => {
                                    if (e.buttons == 1) mouseDown.current = true
                                },
                                onMouseDownCapture: (e) => {
                                    if (e.button == 2) mouseDownRight(true, e)
                                },
                                onMouseUpCapture: (e) => {
                                    onMouseUp()
                                    if (e.button == 2) mouseDownRight(false, e)
                                }
                            })}
                        />
                    ))}
                    <Tooltip
                        overlayCanvas={getCanvas(CanvasType.OVERLAY)}
                        selectedBody={selectedBody}
                        hoveredBody={hoveredBody}
                        wrapper={wrapperRef}
                    />
                </div>
            )}
        </div>
    )
}

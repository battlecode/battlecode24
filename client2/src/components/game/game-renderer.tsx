import React, { useEffect, useRef, useState } from 'react'
import { useAppContext } from '../../app-context'
import { Vector } from '../../playback/Vector'
import { EventType, publishEvent, useListenEvent } from '../../app-events'
import assert from 'assert'
import { Tooltip } from './tooltip'
import { Body } from '../../playback/Bodies'
import Match from '../../playback/Match'
import { TILE_RESOLUTION, TOOLTIP_PATH_DECAY_OPACITY, TOOLTIP_PATH_DECAY_R, TOOLTIP_PATH_INIT_R } from '../../constants'
import { CurrentMap } from '../../playback/Map'

export const GameRenderer: React.FC = () => {
    const wrapperRef = useRef<HTMLDivElement | null>(null)
    const backgroundCanvas = useRef<HTMLCanvasElement | null>(null)
    const dynamicCanvas = useRef<HTMLCanvasElement | null>(null)
    const overlayCanvas = useRef<HTMLCanvasElement | null>(null)

    const appContext = useAppContext()
    const { activeGame, activeMatch } = appContext.state

    const [selectedBody, setSelectedBody] = useState<Body | undefined>(undefined)
    const [hoveredTile, setHoveredTile] = useState<{ x: number; y: number } | undefined>(undefined)
    const [hoveredBody, setHoveredBody] = useState<Body | undefined>(undefined)
    const calculateHoveredBody = () => {
        const match = appContext.state.activeMatch
        if (!match || !hoveredTile) return
        setHoveredBody(match.currentTurn.bodies.getBodyAtLocation(hoveredTile.x, hoveredTile.y))
    }
    useEffect(calculateHoveredBody, [appContext.state.activeMatch, hoveredTile])
    useListenEvent(EventType.TURN_PROGRESS, calculateHoveredBody)

    useEffect(() => {
        renderOverlay(overlayCanvas.current?.getContext('2d')!, activeMatch, selectedBody, hoveredTile)
    }, [hoveredTile])

    const render = React.useCallback(() => {
        const ctx = dynamicCanvas.current?.getContext('2d')
        if (!activeMatch || !ctx) return

        const currentTurn = activeMatch.currentTurn
        const map = currentTurn.map

        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)
        map.draw(activeMatch, ctx, appContext.state.config, selectedBody)
        currentTurn.bodies.draw(activeMatch, ctx, appContext.state.config, selectedBody, hoveredBody)
        currentTurn.actions.draw(activeMatch, ctx)

        renderOverlay(overlayCanvas.current?.getContext('2d')!, activeMatch, selectedBody, hoveredTile)
    }, [activeMatch])
    useListenEvent(EventType.RENDER, render, [render])

    const fullRender = () => {
        const match = appContext.state.activeMatch
        const ctx = backgroundCanvas.current?.getContext('2d')
        if (!match || !ctx) return
        match.currentTurn.map.staticMap.draw(ctx)
        render()
    }
    useListenEvent(EventType.INITIAL_RENDER, fullRender, [fullRender])

    const updateCanvasDimensions = (canvas: HTMLCanvasElement | null, dims: Vector) => {
        if (!canvas) return
        canvas.width = dims.x * TILE_RESOLUTION
        canvas.height = dims.y * TILE_RESOLUTION
        canvas.getContext('2d')?.scale(TILE_RESOLUTION, TILE_RESOLUTION)
    }
    useEffect(() => {
        const match = appContext.state.activeMatch
        if (!match) return
        updateCanvasDimensions(backgroundCanvas.current, {
            x: match.currentTurn.map.width,
            y: match.currentTurn.map.height
        })
        updateCanvasDimensions(dynamicCanvas.current, {
            x: match.currentTurn.map.width,
            y: match.currentTurn.map.height
        })
        updateCanvasDimensions(overlayCanvas.current, {
            x: match.currentTurn.map.width,
            y: match.currentTurn.map.height
        })
        publishEvent(EventType.INITIAL_RENDER, {})
    }, [appContext.state.activeMatch, backgroundCanvas.current, dynamicCanvas.current, overlayCanvas.current])

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
    const mouseDown = React.useRef(false)
    const mouseDownRightPrev = React.useRef(false)
    const lastFiredDragEvent = React.useRef({ x: -1, y: -1 })
    const onMouseUp = (e: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        mouseDown.current = false
        lastFiredDragEvent.current = { x: -1, y: -1 }
        if (e.button === 2) mouseDownRight(false, e)
    }
    const onMouseDown = (e: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        mouseDown.current = true
        if (e.button === 2) mouseDownRight(true, e)
    }
    const onMouseMove = (e: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        const tile = eventToPoint(e)
        if (tile.x !== hoveredTile?.x || tile.y !== hoveredTile?.y) setHoveredTile(tile)
    }
    const mouseDownRight = (down: boolean, e?: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        if (down === mouseDownRightPrev.current) return
        mouseDownRightPrev.current = down
        if (!down && e) onCanvasClick(e)
        publishEvent(EventType.CANVAS_RIGHT_CLICK, { down: down })
    }
    const onCanvasClick = (e: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        const point = eventToPoint(e)
        const clickedBody = activeGame?.currentMatch?.currentTurn?.bodies.getBodyAtLocation(point.x, point.y)
        setSelectedBody(clickedBody)
        publishEvent(EventType.TILE_CLICK, point)
    }
    const onCanvasDrag = (e: React.MouseEvent<HTMLCanvasElement, MouseEvent>) => {
        const tile = eventToPoint(e)
        if (tile.x !== hoveredTile?.x || tile.y !== hoveredTile?.y) setHoveredTile(tile)
        if (tile.x === lastFiredDragEvent.current.x && tile.y === lastFiredDragEvent.current.y) return
        lastFiredDragEvent.current = tile
        publishEvent(EventType.TILE_DRAG, tile)
    }

    return (
        <div className="w-full h-screen flex items-center justify-center">
            {!activeGame || !activeGame.currentMatch ? (
                <p className="text-white text-center">Select a game from the queue</p>
            ) : (
                <div ref={wrapperRef} className="relative w-full h-full">
                    <canvas
                        className="absolute top-1/2 left-1/2 h-full max-w-full max-h-full"
                        style={{
                            transform: 'translate(-50%, -50%)',
                            zIndex: 0,
                            cursor: 'pointer'
                        }}
                        ref={backgroundCanvas}
                    />
                    <canvas
                        className="absolute top-1/2 left-1/2 h-full max-w-full max-h-full"
                        style={{
                            transform: 'translate(-50%, -50%)',
                            zIndex: 1,
                            cursor: 'pointer'
                        }}
                        ref={dynamicCanvas}
                    />
                    <canvas
                        className="absolute top-1/2 left-1/2 h-full max-w-full max-h-full"
                        style={{
                            transform: 'translate(-50%, -50%)',
                            zIndex: 2,
                            cursor: 'pointer'
                        }}
                        ref={overlayCanvas}
                        onClick={onCanvasClick}
                        onMouseMove={(e) => {
                            if (mouseDown.current) onCanvasDrag(e)
                            onMouseMove(e)
                        }}
                        onMouseDown={onMouseDown}
                        onMouseUp={onMouseUp}
                        onMouseLeave={(e) => {
                            onMouseUp(e)
                            mouseDownRight(false)
                        }}
                        onMouseEnter={(e) => {
                            if (e.buttons === 1) mouseDown.current = true
                        }}
                        onContextMenu={(e) => {
                            e.preventDefault()
                        }}
                    />
                    <Tooltip
                        overlayCanvas={overlayCanvas.current}
                        selectedBody={selectedBody}
                        hoveredBody={hoveredBody}
                        wrapper={wrapperRef}
                    />
                    <HighlightedSquare
                        hoveredTile={hoveredTile}
                        map={activeMatch?.currentTurn.map}
                        wrapperRef={wrapperRef.current}
                        overlayCanvasRef={overlayCanvas.current}
                    />
                </div>
            )}
        </div>
    )
}

interface HighlightedSquareProps {
    overlayCanvasRef: HTMLCanvasElement | null
    wrapperRef: HTMLDivElement | null
    map?: CurrentMap
    hoveredTile?: { x: number; y: number }
}
const HighlightedSquare: React.FC<HighlightedSquareProps> = ({ overlayCanvasRef, wrapperRef, map, hoveredTile }) => {
    if (!hoveredTile || !map || !wrapperRef || !overlayCanvasRef) return <></>
    const overlayCanvasRect = overlayCanvasRef.getBoundingClientRect()
    const wrapperRect = wrapperRef.getBoundingClientRect()
    const mapLeft = overlayCanvasRect.left - wrapperRect.left
    const mapTop = overlayCanvasRect.top - wrapperRect.top
    const tileWidth = overlayCanvasRect.width / map.width
    const tileHeight = overlayCanvasRect.height / map.height
    const tileLeft = mapLeft + tileWidth * hoveredTile.x
    const tileTop = mapTop + tileHeight * (map.height - hoveredTile.y - 1)
    return (
        <div
            className="absolute border-2 border-black/70 z-10 cursor-pointer"
            style={{
                left: tileLeft + 'px',
                top: tileTop + 'px',
                width: overlayCanvasRect.width / map.width + 'px',
                height: overlayCanvasRect.height / map.height + 'px',
                pointerEvents: 'none'
            }}
        />
    )
}

const renderOverlay = (
    ctx: CanvasRenderingContext2D,
    match?: Match,
    selectedBody?: Body,
    hoveredTile?: { x: number; y: number }
) => {
    if (!match) return
    ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)
    if (selectedBody) drawBodyPath(match, ctx, selectedBody)
}

const drawBodyPath = (match: Match, ctx: CanvasRenderingContext2D, body: Body) => {
    const interpolatedCoords = body.getInterpolatedCoords(match.currentTurn)

    let alphaValue = 1
    let radius = TOOLTIP_PATH_INIT_R
    let lastPos: Vector = { x: -1, y: -1 }

    for (const prevPos of [interpolatedCoords].concat(body.prevSquares.slice().reverse())) {
        const color = `rgba(255, 255, 255, ${alphaValue})`

        ctx.beginPath()
        ctx.fillStyle = color
        ctx.ellipse(prevPos.x + 0.5, match.map.height - (prevPos.y + 0.5), radius, radius, 0, 0, 360)
        ctx.fill()

        alphaValue *= TOOLTIP_PATH_DECAY_OPACITY
        radius *= TOOLTIP_PATH_DECAY_R

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

import React, { MutableRefObject, useEffect } from 'react'
import * as cst from '../../constants'
import { useAppContext } from '../../app-context'
import { useListenEvent, EventType } from '../../app-events'
import { useForceUpdate } from '../../util/react-util'
import { Body } from '../../playback/Bodies'
import { Vector } from '../../playback/Vector'
import Match from '../../playback/Match'

type TooltipProps = {
    overlayCanvas: HTMLCanvasElement | undefined
    selectedBody: Body | undefined
    hoveredBody: Body | undefined
    wrapper: MutableRefObject<HTMLDivElement | null>
}

const Tooltip = ({ overlayCanvas, selectedBody, hoveredBody, wrapper }: TooltipProps) => {
    const appContext = useAppContext()
    const forceUpdate = useForceUpdate()
    useListenEvent(EventType.RENDER, forceUpdate)

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

    useEffect(() => {
        const match = appContext.state.activeMatch
        if (!match || !overlayCanvas) return
        const ctx = overlayCanvas.getContext('2d')
        if (!ctx) return
        ctx.clearRect(0, 0, overlayCanvas.width, overlayCanvas.height)
        if (selectedBody) drawBodyPath(match, ctx, selectedBody)
    }, [appContext.state.activeMatch, overlayCanvas, selectedBody])

    if (!overlayCanvas || !wrapper.current) return <></>

    const overlayCanvasRect = overlayCanvas.getBoundingClientRect()
    const map = appContext.state.activeMatch?.currentTurn.map
    if (!map) return <></>

    const wrapperRect = wrapper.current.getBoundingClientRect()
    const mapLeft = overlayCanvasRect.left - wrapperRect.left
    const mapTop = overlayCanvasRect.top - wrapperRect.top
    const tileWidth = overlayCanvasRect.width / map.width
    const tileHeight = overlayCanvasRect.height / map.height

    return (
        <>
            {hoveredBody && (
                <div
                    className="absolute bg-black/70 z-20 text-white p-2 rounded-md text-xs"
                    style={{
                        left: mapLeft + tileWidth * (hoveredBody.pos.x + 0.75) + 'px',
                        top: mapTop + tileHeight * (map.height - hoveredBody.pos.y - 0.25) + 'px'
                    }}
                >
                    {hoveredBody.onHoverInfo().map((v) => (
                        <p>{v}</p>
                    ))}
                </div>
            )}
            {selectedBody && (
                <div
                    className="absolute bg-black/70 z-20 text-white p-2 rounded-md text-md pointer-events-none select-none"
                    style={{
                        left: overlayCanvas.clientLeft + 20 + 'px',
                        top: overlayCanvas.clientTop + 20 + 'px'
                    }}
                >
                    {selectedBody.onHoverInfo().map((v) => (
                        <p>{v}</p>
                    ))}
                </div>
            )}
        </>
    )
}

export default Tooltip

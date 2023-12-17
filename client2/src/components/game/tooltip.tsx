import React, { MutableRefObject, useEffect } from 'react'
import * as cst from '../../constants'
import { useAppContext } from '../../app-context'
import { useListenEvent, EventType } from '../../app-events'
import { useForceUpdate } from '../../util/react-util'
import { Body } from '../../playback/Bodies'

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
                    {hoveredBody.onHoverInfo().map((v, i) => (
                        <p key={i}>{v}</p>
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
                    {selectedBody.onHoverInfo().map((v, i) => (
                        <p key={i}>{v}</p>
                    ))}
                </div>
            )}
        </>
    )
}

export default Tooltip

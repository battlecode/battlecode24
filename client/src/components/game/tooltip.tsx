import React, { MutableRefObject, useEffect } from 'react'
import { useAppContext } from '../../app-context'
import { useListenEvent, EventType } from '../../app-events'
import { useForceUpdate } from '../../util/react-util'
import { Body } from '../../playback/Bodies'
import { ThreeBarsIcon } from '../../icons/three-bars'
import { getRenderCoords } from '../../util/RenderUtil'
import { Vector } from '../../playback/Vector'

type TooltipProps = {
    overlayCanvas: HTMLCanvasElement | null
    selectedBodyID: number | undefined
    hoveredBodyID: number | undefined
    selectedSquare: Vector | undefined
    wrapper: MutableRefObject<HTMLDivElement | null>
}

export const Tooltip = ({ overlayCanvas, selectedBodyID, hoveredBodyID, selectedSquare, wrapper }: TooltipProps) => {
    const appContext = useAppContext()
    const forceUpdate = useForceUpdate()
    useListenEvent(EventType.TURN_PROGRESS, forceUpdate)

    const selectedBody =
        selectedBodyID !== undefined
            ? appContext.state.activeMatch?.currentTurn.bodies.bodies.get(selectedBodyID)
            : undefined
    const hoveredBody =
        hoveredBodyID !== undefined
            ? appContext.state.activeMatch?.currentTurn.bodies.bodies.get(hoveredBodyID)
            : undefined

    const tooltipRef = React.useRef<HTMLDivElement>(null)
    const [tooltipSize, setTooltipSize] = React.useState({ width: 0, height: 0 })
    useEffect(() => {
        const observer = new ResizeObserver((entries) => {
            if (entries[0]) {
                const borderBox = entries[0].borderBoxSize[0]
                setTooltipSize({ width: borderBox.inlineSize, height: borderBox.blockSize })
            }
        })
        if (tooltipRef.current) observer.observe(tooltipRef.current)
        return () => {
            if (tooltipRef.current) observer.unobserve(tooltipRef.current)
        }
    }, [hoveredBody, selectedSquare])

    const map = appContext.state.activeMatch?.currentTurn.map
    if (!overlayCanvas || !wrapper.current || !map) return <></>

    const wrapperRect = wrapper.current.getBoundingClientRect()
    const overlayCanvasRect = overlayCanvas.getBoundingClientRect()
    const tileWidth = overlayCanvasRect.width / map.width
    const tileHeight = overlayCanvasRect.height / map.height
    const mapLeft = overlayCanvasRect.left - wrapperRect.left
    const mapTop = overlayCanvasRect.top - wrapperRect.top

    let tooltipStyle: React.CSSProperties = {
        visibility: 'hidden'
    }
    if ((selectedSquare || hoveredBody) && tooltipRef.current) {
        let tipPos: Vector
        if (hoveredBody) {
            tipPos = getRenderCoords(hoveredBody.pos.x, hoveredBody.pos.y, map.dimension, true)
        } else {
            tipPos = getRenderCoords(selectedSquare!.x, selectedSquare!.y, map.dimension, true)
        }
        const distanceFromBotCenterX = 0.75 * tileWidth
        const distanceFromBotCenterY = 0.75 * tileHeight
        const clearanceLeft = mapLeft + tipPos.x * tileWidth - distanceFromBotCenterX
        const clearanceRight = wrapperRect.width - clearanceLeft - 2 * distanceFromBotCenterX
        const clearanceTop = mapTop + tipPos.y * tileHeight - distanceFromBotCenterY

        if (clearanceTop > tooltipSize.height) {
            tooltipStyle.top = mapTop + tipPos.y * tileHeight - tooltipSize.height - distanceFromBotCenterY + 'px'
        } else {
            tooltipStyle.top = mapTop + tipPos.y * tileHeight + distanceFromBotCenterY + 'px'
        }
        if (clearanceLeft < tooltipSize.width / 2) {
            tooltipStyle.left = mapLeft + tipPos.x * tileWidth + distanceFromBotCenterX + 'px'
        } else if (clearanceRight < tooltipSize.width / 2) {
            tooltipStyle.left = mapLeft + tipPos.x * tileWidth - tooltipSize.width - distanceFromBotCenterX + 'px'
        } else {
            tooltipStyle.left = mapLeft + tipPos.x * tileWidth - tooltipSize.width / 2 + 'px'
        }
        tooltipStyle.visibility = 'visible'
    }

    let showFloatingTooltip = !!((hoveredBody && hoveredBody != selectedBody) || selectedSquare)
    const tooltipContent = hoveredBody
        ? hoveredBody.onHoverInfo()
        : selectedSquare
          ? map.getTooltipInfo(selectedSquare)
          : []
    if (tooltipContent.length === 0) showFloatingTooltip = false

    return (
        <>
            {showFloatingTooltip && (
                <div
                    className="absolute bg-black/70 z-20 text-white p-2 rounded-md text-xs"
                    style={tooltipStyle}
                    ref={tooltipRef}
                >
                    {tooltipContent.map((v, i) => (
                        <p key={i}>{v}</p>
                    ))}
                </div>
            )}

            <Draggable width={wrapperRect.width} height={wrapperRect.height}>
                {selectedBody && (
                    <div className="bg-black/90 z-20 text-white p-2 rounded-md text-xs cursor-pointer relative">
                        {selectedBody.onHoverInfo().map((v, i) => (
                            <p key={i}>{v}</p>
                        ))}
                        <div className="absolute top-0 right-0" style={{ transform: 'scaleX(0.57) scaleY(0.73)' }}>
                            <ThreeBarsIcon />
                        </div>
                    </div>
                )}
            </Draggable>
        </>
    )
}

interface DraggableProps {
    children: React.ReactNode
    width: number
    height: number
    margin?: number
}

const Draggable = ({ children, width, height, margin = 0 }: DraggableProps) => {
    const [dragging, setDragging] = React.useState(false)
    const [pos, setPos] = React.useState({ x: 20, y: 20 })
    const [offset, setOffset] = React.useState({ x: 0, y: 0 })
    const ref = React.useRef<HTMLDivElement>(null)

    const mouseDown = (e: React.MouseEvent) => {
        setDragging(true)
        setOffset({ x: e.clientX - pos.x, y: e.clientY - pos.y })
    }

    const mouseUp = () => {
        setDragging(false)
    }

    const mouseMove = (e: React.MouseEvent) => {
        if (dragging && ref.current) {
            const targetX = e.clientX - offset.x
            const targetY = e.clientY - offset.y
            const realX = Math.min(Math.max(targetX, margin), width - ref.current.clientWidth - margin)
            const realY = Math.min(Math.max(targetY, margin), height - ref.current.clientHeight - margin)
            setPos({ x: realX, y: realY })
        }
    }

    return (
        <div
            ref={ref}
            onMouseDown={mouseDown}
            onMouseUp={mouseUp}
            onMouseLeave={mouseUp}
            onMouseEnter={(e) => {
                if (e.buttons === 1) mouseDown(e)
            }}
            onMouseMove={mouseMove}
            className="absolute z-20"
            style={{
                left: pos.x + 'px',
                top: pos.y + 'px'
            }}
        >
            {children}
        </div>
    )
}

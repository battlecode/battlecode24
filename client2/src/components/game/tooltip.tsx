import React, { Fragment, useEffect } from 'react'
import * as cst from '../../constants'
import { useMousePosition } from '../../util/mouse-pos'
import { useAppContext } from '../../app-context'
import { useListenEvent, EventType } from '../../app-events'
import { useForceUpdate } from '../../util/react-util'
import { Body } from '../../playback/Bodies'
import { Vector } from '../../playback/Vector'
import Match from '../../playback/Match'

type TooltipProps = {
    mapCanvas: HTMLCanvasElement | undefined
    overlayCanvas: HTMLCanvasElement | undefined
    wrapperRef: React.MutableRefObject<HTMLElement | null>
}

const Tooltip = ({ mapCanvas, overlayCanvas, wrapperRef }: TooltipProps) => {
    const mousePos = useMousePosition()
    const appContext = useAppContext()

    const forceUpdate = useForceUpdate()
    useListenEvent(EventType.TURN_PROGRESS, forceUpdate)

    const [clickedBody, setClickedRobot] = React.useState<Body | undefined>(undefined)

    let canvasAbsLeft = 0,
        canvasAbsTop = 0
    let tileLeft = 0,
        tileTop = 0
    let tileWidth = 0,
        tileHeight = 0
    let tileCol = -1,
        tileRow = -1

    if (mapCanvas && wrapperRef.current) {
        const canvasBoundingBox = mapCanvas.getBoundingClientRect()
        const wrapperBoundingBox = wrapperRef.current.getBoundingClientRect()

        const scalingFactorX = mapCanvas.width / canvasBoundingBox.width
        const scalingFactorY = mapCanvas.height / canvasBoundingBox.height

        const localX = (mousePos.x - canvasBoundingBox.left) * scalingFactorX
        const localY = (mousePos.y - canvasBoundingBox.top) * scalingFactorY

        tileCol = Math.floor(localX / cst.TILE_RESOLUTION)
        tileRow = Math.floor(localY / cst.TILE_RESOLUTION)

        canvasAbsLeft = canvasBoundingBox.left - wrapperBoundingBox.left
        canvasAbsTop = canvasBoundingBox.top - wrapperBoundingBox.top

        tileLeft = (tileCol * cst.TILE_RESOLUTION) / scalingFactorX + canvasAbsLeft
        tileTop = (tileRow * cst.TILE_RESOLUTION) / scalingFactorY + canvasAbsTop
        tileWidth = cst.TILE_RESOLUTION / scalingFactorX
        tileHeight = cst.TILE_RESOLUTION / scalingFactorY
    }

    function getHoveredBody() {
        return appContext.state?.activeMatch?.map
            ? appContext.state.activeMatch?.currentTurn.bodies.getByLocation(
              tileCol,
              appContext.state.activeMatch.map.dimension.height - 1 - tileRow
            )
        : undefined
    }

    function onClick(e: Event) {
        const hoveredBody = getHoveredBody()
        setClickedRobot(hoveredBody)
    }

    useEffect(() => {
        if (overlayCanvas) {
            overlayCanvas.addEventListener('click', onClick)
            return () => {
                overlayCanvas.removeEventListener('click', onClick)
            }
        }
    }, [overlayCanvas, mousePos])

    const hoveredBody = getHoveredBody()

    const drawBodyTooltip = (match: Match, ctx: CanvasRenderingContext2D, body: Body, isClicked: boolean) => {
        ctx.beginPath();
        ctx.strokeStyle = "blue";
        ctx.lineWidth = 1 / match.map.width;
        ctx.arc(body.pos.x + 0.5, match.map.height - (body.pos.y + 0.5), Math.sqrt(body.actionRadius), 0, 360);
        ctx.stroke();

        ctx.beginPath();
        ctx.strokeStyle = "red";
        ctx.lineWidth = 1 / match.map.width;
        ctx.arc(body.pos.x + 0.5, match.map.height - (body.pos.y + 0.5), Math.sqrt(body.visionRadius), 0, 360);
        ctx.stroke();

        if (isClicked) {
            let alphaValue = 1;
            let radius = cst.TOOLTIP_PATH_INIT_R;
            let lastPos: Vector = {x: -1, y: -1};

            for (const prevPos of body.prevSquares.slice().reverse()) {
                const color =  `rgba(255, 255, 255, ${alphaValue})`;

                ctx.beginPath();
                ctx.fillStyle = color;

                ctx.ellipse(prevPos.x + 0.5, match.map.height - (prevPos.y + 0.5), radius, radius, 0, 0, 360);

                alphaValue *= cst.TOOLTIP_PATH_DECAY_OPACITY;
                radius *= cst.TOOLTIP_PATH_DECAY_R;
                ctx.fill();

                if (lastPos.x != -1 && lastPos.y != -1) {
                    ctx.beginPath();
                    ctx.strokeStyle = color;
                    ctx.lineWidth = radius / 2;

                    ctx.moveTo(lastPos.x + 0.5, match.map.height - (lastPos.y + 0.5));
                    ctx.lineTo(prevPos.x + 0.5, match.map.height - (prevPos.y + 0.5));

                    ctx.stroke();
                }

                lastPos = prevPos;
            }
        }
    }

    // draw tooltip stuff to overlay canvas
    useEffect(() => {
        const match = appContext.state.activeMatch;
        if (!match || !overlayCanvas) return

        const ctx = overlayCanvas.getContext('2d')
        if (!ctx) return

        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)

        if (hoveredBody) {
            drawBodyTooltip(match, ctx, hoveredBody, false);
        }

        if (clickedBody) {
            drawBodyTooltip(match, ctx, clickedBody, true);
        }
    }, [appContext.state.activeMatch, overlayCanvas, hoveredBody, clickedBody, tileLeft, tileTop,
        appContext.state.activeMatch?.currentTurn.turnNumber]);

    useEffect(() => {
        setClickedRobot(undefined);
    }, [appContext.state.activeMatch])

    useListenEvent(EventType.TURN_PROGRESS, () => {
        const match = appContext.state.activeMatch;
        if (!match) return;

        if (clickedBody) {
            if (!match.currentTurn.bodies.hasId(clickedBody.id)) {
                // TODO maybe show death info instead (i.e. turn it died on, last location, maybe even show it on the map)
                setClickedRobot(undefined);
            }
        }
    });

    if (!mapCanvas || !overlayCanvas || !wrapperRef.current) {
        return <Fragment />
    }

    const canvasBoundingBox = mapCanvas.getBoundingClientRect()
    const hoverVisible = !(
        mousePos.x < canvasBoundingBox.left ||
        mousePos.x > canvasBoundingBox.right ||
        mousePos.y < canvasBoundingBox.top ||
        mousePos.y > canvasBoundingBox.bottom
    )

    return (
        <Fragment>
            {hoverVisible && (
                <Fragment>
                    <div
                        className="absolute border-2 border-black/70 z-10 cursor-pointer"
                        style={{
                            left: tileLeft + 'px',
                            top: tileTop + 'px',
                            width: tileWidth + 'px',
                            height: tileHeight + 'px',
                            pointerEvents: 'none'
                        }}
                    />
                    {hoveredBody && (
                        <div
                            className="absolute bg-black/70 z-20 text-white p-2 rounded-md text-xs"
                            style={{
                                left: tileLeft + tileWidth * 0.75 + 'px',
                                top: tileTop + tileHeight * 0.75 + 'px',
                            }}
                        >
                            {hoveredBody.onHoverInfo().map((v) => <p>{v}</p>)}
                        </div>
                    )}
                </Fragment>
            )}
            {clickedBody !== undefined && (
                <div
                    className="absolute bg-black/70 z-20 text-white p-2 rounded-md text-md"
                    style={{
                        left: overlayCanvas.clientLeft + tileWidth * 0.5 + 'px',
                        top: overlayCanvas.clientTop + tileHeight * 0.5 + 'px'
                    }}
                >
                    {clickedBody.onHoverInfo().map((v) => <p>{v}</p>)}
                </div>
            )}
        </Fragment>
    )
}

export default Tooltip

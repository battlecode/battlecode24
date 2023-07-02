import React, { Fragment } from 'react'
import * as cst from '../../constants'
import { useMousePosition } from '../../util/mouse-pos'

type TooltipProps = {
    canvas: HTMLCanvasElement | undefined
    wrapperRef: React.MutableRefObject<HTMLElement | null>
}

const Tooltip = ({ canvas, wrapperRef }: TooltipProps) => {
    const mousePos = useMousePosition()

    if (!canvas || !wrapperRef.current) {
        return <Fragment />
    }

    const canvasBoundingBox = canvas.getBoundingClientRect()
    const wrapperBoundingBox = wrapperRef.current.getBoundingClientRect()

    const scalingFactorX = canvas.width / canvasBoundingBox.width
    const scalingFactorY = canvas.height / canvasBoundingBox.height

    if (
        mousePos.x < canvasBoundingBox.left ||
        mousePos.x > canvasBoundingBox.right ||
        mousePos.y < canvasBoundingBox.top ||
        mousePos.y > canvasBoundingBox.bottom
    ) {
        return <Fragment />
    }

    const localX = (mousePos.x - canvasBoundingBox.left) * scalingFactorX
    const localY = (mousePos.y - canvasBoundingBox.top) * scalingFactorY

    const tileCol = Math.floor(localX / cst.TILE_RESOLUTION)
    const tileRow = Math.floor(localY / cst.TILE_RESOLUTION)

    const tileLeft = (tileCol * cst.TILE_RESOLUTION) / scalingFactorX + canvasBoundingBox.left - wrapperBoundingBox.left
    const tileTop = (tileRow * cst.TILE_RESOLUTION) / scalingFactorY + canvasBoundingBox.top - wrapperBoundingBox.top
    const tileWidth = cst.TILE_RESOLUTION / scalingFactorX
    const tileHeight = cst.TILE_RESOLUTION / scalingFactorY

    return (
        <Fragment>
            <div
                className="absolute bg-black z-10"
                style={{
                    left: tileLeft + 'px',
                    top: tileTop + 'px',
                    width: tileWidth + 'px',
                    height: tileHeight + 'px'
                }}
            ></div>
        </Fragment>
    )
}

export default Tooltip

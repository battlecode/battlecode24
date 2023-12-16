import React, { useEffect, useRef } from 'react'
import { TEAM_COLORS } from '../../../constants'

export interface LineChartDataPoint {
    turn: number
    brown: number
    white: number
}

interface LineChartProps {
    data: LineChartDataPoint[]
    width: number
    height: number
    lineColor: string
    margin: {
        top: number
        right: number
        bottom: number
        left: number
    }
    resolution?: number
}

export const CanvasLineChart: React.FC<LineChartProps> = ({
    data,
    width,
    height,
    margin,
    lineColor,
    resolution = 1
}) => {
    const canvasRef = useRef<HTMLCanvasElement | null>(null)

    useEffect(() => {
        if (!canvasRef.current) return
        const canvas = canvasRef.current
        const context = canvas.getContext('2d')
        if (!context) return

        canvas.width = width * resolution
        canvas.height = height * resolution
        canvas.style.width = `${width}px`
        canvas.style.height = `${height}px`
        context.scale(resolution, resolution)

        const max = Math.max(...data.map((d) => Math.max(d.brown, d.white)))
        const xScale = (value: number) => (value * (width - margin.left - margin.right)) / data.length + margin.left
        const yScale = (value: number) => height - margin.bottom - (value / max) * (height - margin.top - margin.bottom)

        context.clearRect(0, 0, width, height)
        context.strokeStyle = TEAM_COLORS[0]
        context.beginPath()
        context.moveTo(xScale(0), yScale(0))
        for (const point of data) {
            context.lineTo(xScale(point.turn), yScale(point.white))
        }
        context.stroke()
        context.strokeStyle = TEAM_COLORS[1]
        context.beginPath()
        context.moveTo(xScale(0), yScale(0))
        for (const point of data) {
            context.lineTo(xScale(point.turn), yScale(point.brown))
        }
        context.stroke()

        context.strokeStyle = lineColor
        // Draw x-axis
        context.beginPath()
        context.moveTo(margin.left, height - margin.bottom)
        context.lineTo(width - margin.right, height - margin.bottom)
        context.stroke()

        // Draw x-axis tick marks and labels
        const possibleRangesX = [4, 5, 6, 7]
        let gap = Math.ceil(data.length / possibleRangesX[0])
        for (let i = 0; i < possibleRangesX.length; i++) {
            if (data.length % gap > data.length % possibleRangesX[i]) {
                gap = Math.ceil(data.length / possibleRangesX[i])
            }
        }
        for (let i = 0; i <= data.length; i += gap) {
            const xPos = yScale(i)
            // Draw tick mark
            context.beginPath()
            context.moveTo(margin.left - 6, xPos)
            context.lineTo(margin.left, xPos)
            context.stroke()
            // Draw label
            context.fillStyle = 'black' // Set label color to black
            context.fillText(i.toString(), margin.left - 15, xPos + 5)
        }

        // Draw y-axis
        context.beginPath()
        context.moveTo(margin.left, margin.top)
        context.lineTo(margin.left, height - margin.bottom)
        context.stroke()

        // Draw y-axis tick marks and labels
        const possibleRangesY = [4, 5, 6, 7]
        gap = Math.ceil(max / possibleRangesY[0])
        for (let i = 0; i < possibleRangesY.length; i++) {
            if (max % gap > max % possibleRangesY[i]) {
                gap = Math.ceil(max / possibleRangesY[i])
            }
        }
        for (let i = 0; i <= max; i += gap) {
            const yPos = yScale(i)
            // Draw tick mark
            context.beginPath()
            context.moveTo(margin.left - 6, yPos)
            context.lineTo(margin.left, yPos)
            context.stroke()
            // Draw label
            context.fillStyle = 'black' // Set label color to black
            context.fillText(i.toString(), margin.left - 15, yPos + 5)
        }
    }, [data.length, height, margin, width])

    return <canvas ref={canvasRef} width={width} height={height} />
}

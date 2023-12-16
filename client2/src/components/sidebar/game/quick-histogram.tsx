import React, { useEffect, useRef, useMemo } from 'react'

interface HistogramProps {
    data: number[]
    width: number
    height: number
    color: string
    lineColor: string
    margin: {
        top: number
        right: number
        bottom: number
        left: number
    }
    resolution?: number
}

export const QuickHistogram: React.FC<HistogramProps> = ({
    data,
    width,
    height,
    margin,
    color,
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

        const max = Math.max(...data)
        const xScale = (value: number) => (value * (width - margin.left - margin.right)) / data.length + margin.left
        const yScale = (value: number) => height - margin.bottom - (value / max) * (height - margin.top - margin.bottom)

        context.clearRect(0, 0, width, height)
        context.fillStyle = color
        data.forEach((bin, index) => {
            const barWidth = (width - margin.left - margin.right) / data.length
            const barHeight = yScale(bin)
            const leftPadding = 3
            context.fillRect(
                xScale(index) + leftPadding,
                barHeight,
                barWidth - leftPadding,
                height - margin.bottom - barHeight
            )
        })

        context.strokeStyle = lineColor
        // Draw x-axis
        context.beginPath()
        context.moveTo(margin.left, height - margin.bottom)
        context.lineTo(width - margin.right, height - margin.bottom)
        context.stroke()

        // Draw x-axis tick marks and labels
        data.forEach((_, index) => {
            const xPos = xScale(index) + (width - margin.left - margin.right) / (2 * data.length)
            // Draw tick mark
            context.beginPath()
            context.moveTo(xPos, height - margin.bottom)
            context.lineTo(xPos, height - margin.bottom + 6)
            context.stroke()
            // Draw label
            context.fillStyle = 'black' // Set label color to black
            context.fillText(index.toString(), xPos - 2, height - margin.bottom + 20)
        })

        // Draw y-axis
        context.beginPath()
        context.moveTo(margin.left, margin.top)
        context.lineTo(margin.left, height - margin.bottom)
        context.stroke()

        // Draw y-axis tick marks and labels
        const possibleRanges = [4, 5, 6, 7]
        let gap = Math.ceil(max / possibleRanges[0])
        for (let i = 0; i < possibleRanges.length; i++) {
            if (max % gap > max % possibleRanges[i]) {
                gap = Math.ceil(max / possibleRanges[i])
            }
        }
        for (let i = 0; i <= max; i += gap) {
            const yPos = yScale((max / max) * i)
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

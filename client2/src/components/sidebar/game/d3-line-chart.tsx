import React, { useEffect, useRef } from 'react'
import * as d3 from 'd3'

interface DataPoint {
    round: number
    blue: number
    red: number
}

interface LineChartProps {
    data: DataPoint[]
    width: number
    height: number
    margin: {
        top: number
        right: number
        bottom: number
        left: number
    }
}

export const D3LineChart: React.FC<LineChartProps> = ({ data, width, height, margin }) => {
    const svgRef = useRef<SVGSVGElement | null>(null)

    useEffect(() => {
        if (data.length === 0) return

        // The topleft of this container is the origin of everything. Nothing
        // can be drawn outside this container.
        const svg = d3
            .select(svgRef.current)
            .attr('width', width)
            .attr('height', height)
            .on('pointerenter pointermove', pointerMoved)
            .on('pointerleave', pointerLeft)

        const xScale = d3
            .scaleLinear()
            .domain([0, d3.max(data, (d) => d.round)!])
            .range([margin.left, width - margin.right])

        const yScale = d3
            .scaleLinear()
            .domain([0, d3.max(data, (d) => Math.max(d.red, d.blue))!])
            .range([height - margin.bottom, margin.top])

        // This function returns a function that draws a SVG line between every
        // data point. The "y" of the data point is what is determined by the
        // function passed in, see code below.
        const lineGenerator = (dScaleFunc: (dataPoint: DataPoint) => any) => {
            return d3
                .line<DataPoint>()
                .x((d) => xScale(d.round))
                .y(dScaleFunc)
                .curve(d3.curveMonotoneX)
        }

        svg.selectAll('*').remove()

        // Draws Blue Line
        svg.append('path')
            .datum(data)
            .attr('fill', 'none')
            .attr('stroke', '#04a2d9')
            .attr('stroke-width', 1.5)
            .attr('d', lineGenerator((d: DataPoint) => yScale(d.blue))(data))

        // Draw Red Line
        svg.append('path')
            .datum(data)
            .attr('fill', 'none')
            .attr('stroke', '#ff9194')
            .attr('stroke-width', 1.5)
            .attr('d', lineGenerator((d: DataPoint) => yScale(d.red))(data))

        // Add X-Axis
        svg.append('g')
            .attr('transform', `translate(0, ${height - margin.bottom})`)
            .call(d3.axisBottom(xScale))

        // Add Y-axis
        svg.append('g').attr('transform', `translate(${margin.left}, 0)`).call(d3.axisLeft(yScale))

        // Append tooltipLine before tooltip so it appears behind it
        const tooltipLine = svg.append('line')
        const tooltip = svg.append('g')

        function pointerMoved(event: MouseEvent) {
            if (
                d3.pointer(event)[0] < margin.left ||
                d3.pointer(event)[0] > width - margin.right ||
                d3.pointer(event)[1] < margin.top ||
                d3.pointer(event)[1] > height - margin.bottom
            ) {
                pointerLeft()
                return
            }

            const sortedData = data.map((d) => d.round)
            const maxRound = d3.max(data, (d) => d.round)!
            const i = d3.bisectCenter(
                sortedData,
                ((d3.pointer(event)[0] - margin.left) / (width - margin.left - margin.right)) * maxRound
            )

            tooltip.style('display', null)
            tooltip.attr(
                'transform',
                `translate(${xScale(data[i].round)},${yScale(Math.max(data[i].blue, data[i].red))})`
            )

            const path = tooltip.selectAll('path').data([,]).join('path').attr('fill', 'white').attr('stroke', 'black')

            const text = tooltip
                .selectAll('text')
                .data([,])
                .join('text')
                .call((text) =>
                    text
                        .selectAll('tspan')
                        .data(['Round: ' + data[i].round, 'Blue: ' + data[i].blue, 'Red: ' + data[i].red])
                        .join('tspan')
                        .attr('x', 0)
                        .attr('y', (_, i) => `${i * 1.1}em`)
                        .attr('font-weight', (_, i) => (i ? null : 'bold'))
                        .text((d) => d)
                )

            const { x, y, width: w, height: h } = (text.node() as SVGSVGElement).getBBox()

			// If the data point is low, swap the tooltip render to be on the
			// top. "d" attr in path is SVG code to draw the tooltip icon
            if (yScale(Math.max(data[i].blue, data[i].red)) / height > 0.7) {
                text.attr('transform', `translate(${-w / 2},${y - 15 - 15})`)
                path.attr('d', `M${-w / 2 - 10},-5H-5l5,5l5,-5H${w / 2 + 10}v${-h - 20}h-${w + 20}z`)
            } else {
                text.attr('transform', `translate(${-w / 2},${15 - y})`)
                path.attr('d', `M${-w / 2 - 10},5H-5l5,-5l5,5H${w / 2 + 10}v${h + 20}h-${w + 20}z`)
            }

            // Snap tooltip line to data point
            const dataPointX = (data[i].round / maxRound) * (width - margin.right - margin.left) + margin.left
            tooltipLine
                .attr('x1', dataPointX)
                .attr('y1', margin.top)
                .attr('x2', dataPointX)
                .attr('y2', height - margin.bottom)
                .style('display', null)
                .style('stroke', 'black')
                .style('stroke-width', 2)
        }

        function pointerLeft() {
            tooltip.style('display', 'none')
            tooltipLine.style('display', 'none')
        }
    }, [data, width, height, margin])

    return (
        <svg ref={svgRef} width={width} height={height}>
            <g />
        </svg>
    )
}

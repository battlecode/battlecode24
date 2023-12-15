import React, { useEffect, useRef } from 'react'
import { TEAM_WHITE, TEAM_BROWN } from '../../../constants'
import * as d3 from 'd3'

interface HistogramProps {
    data: number[]
    binCount: number
    width: number
    height: number
	color: string
    margin: {
        top: number
        right: number
        bottom: number
        left: number
    }
}

export const D3Histogram: React.FC<HistogramProps> = ({ data, width, height, margin, color, binCount }) => {
    const svgRef = useRef<SVGSVGElement | null>(null)

    useEffect(() => {
        if (data.length === 0) return

        // append the svg object to the body of the page
        const svg = d3.select(svgRef.current).attr('width', width).attr('height', height)
        //.on('pointerenter pointermove', pointerMoved)
        //.on('pointerleave', pointerLeft)

        const xScale = d3
            .scaleLinear()
            .domain([0, d3.max(data)! + 1]) // Add 1 so data is not cut off
            .range([margin.left, width - margin.right - margin.left])

        const bins = d3
            .histogram()
            .domain(xScale.domain() as any)
            .thresholds(xScale.ticks(binCount))(data)

        const yScale = d3
            .scaleLinear()
            .domain([0, d3.max(bins, function(d) { return d.length; }) as number])
            .range([height - margin.bottom - margin.top, margin.top])

        // Add X-Axis
        svg.append('g')
            .attr('transform', `translate(0, ${height - margin.bottom - margin.top})`)
            .call(d3.axisBottom(xScale))

        // Add Y-axis
        svg.append('g').attr('transform', `translate(${margin.left}, 0)`).call(d3.axisLeft(yScale))

        svg.selectAll('rect')
            .data(bins)
            .join('rect')
            .attr('x', 1)
            .attr('transform', function (d) {
                return `translate(${xScale(d.x0!)} , ${yScale(d.length!)})`
            })
            .attr('width', function (d) {
                return xScale(d.x1!) - xScale(d.x0!) - 2
            })
            .attr('height', function (d) {
                return (height - margin.top - margin.bottom) - yScale(d.length!)
            })
            .style('fill', color)


    }, [data, width, height, margin])

    return (
        <svg ref={svgRef} width={width} height={height}>
            <g />
        </svg>
    )
}

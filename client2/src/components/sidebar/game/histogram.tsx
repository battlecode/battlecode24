import React from 'react'
import { AppContext, useAppContext } from '../../../app-context'
import { useListenEvent, EventType } from '../../../app-events'
import { useForceUpdate } from '../../../util/react-util'
import { D3Histogram } from './d3-histogram'
import assert from 'assert'

interface Props {
    active: boolean
    property: string
    propertyDisplayName: string
    color: string
}

function getChartData(appContext: AppContext, property: string): number[] {
    const match = appContext.state.activeMatch
    if (match === undefined) {
        return []
    }

    /*const values = [0, 1].map((index) =>
        match.stats.map((turnStat) => {
            const teamStat = turnStat.getTeamStat(match.game.teams[index])
            return teamStat.robots[0] % 5;
        })
    )*/

    return [1, 1, 1, 3, 3, 3, 3, 3, 4, 4, 4, 2, 2, 5, 5, 5, 5]
}

export const Histogram: React.FC<Props> = (props: Props) => {
    const appContext = useAppContext()
    const forceUpdate = useForceUpdate()

    useListenEvent(EventType.TURN_PROGRESS, forceUpdate)

    return (
        <div className="mt-2 px-2 w-full">
            <h2 className="mx-auto text-center">{props.propertyDisplayName}</h2>
            <D3Histogram
                data={getChartData(appContext, props.property)}
                width={300 + 40} // Add 40 so that tooltip is visible outside of SVG container
                binCount={5}
                height={300}
                margin={{ top: 20, right: 30, bottom: 20, left: 40 }}
                color={props.color}
            />
        </div>
    )
}

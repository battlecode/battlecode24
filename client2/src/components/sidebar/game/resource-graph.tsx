import React, { useContext, useState } from 'react'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { AppContext, useAppContext } from '../../../app-context'
import { useListenEvent, EventType } from '../../../app-events'
import { useForceUpdate } from '../../../util/react-util'
import { D3LineChart } from './d3-line-chart'
import assert from 'assert'

interface Props {
    active: boolean
    property: string
    propertyDisplayName: string
}

function hasKey<O extends Object>(obj: O, key: PropertyKey): key is keyof O {
    return key in obj
}

function getChartData(appContext: AppContext, property: string): any[] {
    const match = appContext.state.activeMatch
    if (match === undefined) {
        return []
    }

    const values = [0, 1].map((index) =>
        match.stats.map((turnStat) => {
            const teamStat = turnStat.getTeamStat(match.game.teams[index])
            assert(hasKey(teamStat, property))
            return teamStat[property]
        })
    )

    return values[0].map((value, index) => {
        return {
            round: index + 1,
            red: value,
            blue: values[1][index]
        }
    })
}

export const ResourceGraph: React.FC<Props> = (props: Props) => {
    const appContext = useAppContext()
    const forceUpdate = useForceUpdate()

    useListenEvent(EventType.TURN_PROGRESS, forceUpdate)

    return (
        <div className="mt-2 px-2 w-full">
            <h2 className="mx-auto text-center">{props.propertyDisplayName}</h2>
            <ResponsiveContainer aspect={1.5} width="100%" className="text-xs">
                <div className="App">
                    <D3LineChart
                        data={getChartData(appContext, props.property)}
                        width={300 + 40} // Add 40 so that tooltip is visible outside of SVG container
                        height={300}
                        margin={{ top: 20, right: 20 + 20, bottom: 30, left: 40 + 20 }}
                    />
                </div>
            </ResponsiveContainer>
        </div>
    )
}

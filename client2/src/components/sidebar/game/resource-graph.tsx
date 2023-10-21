import React, { useContext, useState } from 'react'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { AppContext, useAppContext } from '../../../app-context'
import { useListenEvent, EventType } from '../../../app-events'
import { useForceUpdate } from '../../../util/react-util'
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
                <LineChart
                    data={props.active ? [] : []}
                    margin={{
                        top: 10,
                        right: 25,
                        left: -25,
                        bottom: 0
                    }}
                    className="mx-auto"
                >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="round" />
                    <YAxis />
                    <Tooltip labelFormatter={(label) => 'Round ' + label} separator=": " />
                    <Line
                        type="linear"
                        name={'Red ' + props.propertyDisplayName}
                        dataKey="red"
                        stroke="#ff9194"
                        dot={false}
                        activeDot={{ r: 4 }}
                        isAnimationActive={false}
                    />
                    <Line
                        type="linear"
                        name={'Blue ' + props.propertyDisplayName}
                        dataKey="blue"
                        stroke="#04a2d9"
                        dot={false}
                        activeDot={{ r: 4 }}
                        isAnimationActive={false}
                    />
                </LineChart>
            </ResponsiveContainer>
        </div>
    )
}

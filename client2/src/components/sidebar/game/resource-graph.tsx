import React, { useContext, useState } from 'react'
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer
} from 'recharts'
import { AppContext, useAppContext } from '../../../app-context'
import { useListenEvent, EventType } from '../../../app-events'
import { useForceUpdate } from '../../../util/react-util'

interface Props {
    active: boolean
}

function getChartData(appContext: AppContext): any[] {
    const match = appContext.state.activeMatch
    if (match === undefined) {
        return []
    }

    const redMana = match.stats.map(turnStat => turnStat.getTeamStat(match.game.teams[0]).mana)
    const blueMana = match.stats.map(turnStat => turnStat.getTeamStat(match.game.teams[1]).mana)

    return redMana.map((value, index) => {
        return {
            round: index + 1,
            red_mana: value,
            blue_mana: blueMana[index]
        }
    })
}

export const ResourceGraph: React.FC<Props> = (props: Props) => {

    const appContext = useAppContext()
    const forceUpdate = useForceUpdate()

    useListenEvent(EventType.TURN_PROGRESS, forceUpdate)

    return (
        <div className="my-2 px-2 w-full">
            <ResponsiveContainer aspect={1.5} width="100%" className="text-xs">
                <LineChart
                    data={props.active ? getChartData(appContext) : []}
                    margin={{
                        top: 10,
                        right: 30,
                        left: 0,
                        bottom: 5
                    }}
                    className="mx-auto"
                >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="round" />
                    <YAxis />
                    <Tooltip labelFormatter={(label) => "Round " + label} separator=': '/>
                    <Legend />
                    <Line type="linear" name="Red Mana" dataKey="red_mana" stroke="#ff9194" dot={false} activeDot={{ r: 4 }} />
                    <Line type="linear" name="Blue Mana" dataKey="blue_mana" stroke="#04a2d9" dot={false} activeDot={{ r: 4 }} />
                </LineChart>
            </ResponsiveContainer>
        </div>
    )
}

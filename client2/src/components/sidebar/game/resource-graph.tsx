import React, { useContext, useEffect, useMemo, useRef, useState } from 'react'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { AppContext, useAppContext } from '../../../app-context'
import { useListenEvent, EventType } from '../../../app-events'
import { useForceUpdate } from '../../../util/react-util'
import { D3LineChart, DataPoint } from './d3-line-chart'
import assert from 'assert'

interface Props {
    active: boolean
    property: string
    propertyDisplayName: string
}

function hasKey<O extends Object>(obj: O, key: PropertyKey): key is keyof O {
    return key in obj
}

export const ResourceGraph: React.FC<Props> = (props: Props) => {
    const cachedData = useRef<DataPoint[]>([])
    const appContext = useAppContext()
    const match = appContext.state.activeMatch
    useEffect(() => {
        cachedData.current = []
    }, [match])

    const forceUpdate = useForceUpdate()
    useListenEvent(EventType.TURN_PROGRESS, forceUpdate)

    if (match && match.currentTurn.turnNumber > cachedData.current.length) {
        for (let i = cachedData.current.length; i < match.currentTurn.turnNumber; i++) {
            const redTeamStat = match.stats[i].getTeamStat(match.game.teams[0])
            const blueTeamStat = match.stats[i].getTeamStat(match.game.teams[1])
            assert(hasKey(redTeamStat, props.property))
            assert(hasKey(blueTeamStat, props.property))
            cachedData.current.push({
                turn: i + 1,
                red: redTeamStat[props.property] as number,
                blue: blueTeamStat[props.property] as number
            })
        }
    }

    const displayData = cachedData.current.slice(0, match ? match.currentTurn.turnNumber : 0)

    return (
        <div className="mt-2 px-2 w-full">
            <h2 className="mx-auto text-center">{props.propertyDisplayName}</h2>
            {/* <ResponsiveContainer aspect={1.5} width="100%" className="text-xs"> */}
            {/* <div className="App"> */}
            <D3LineChart
                data={displayData}
                width={300 + 40} // Add 40 so that tooltip is visible outside of SVG container
                height={300}
                margin={{ top: 20, right: 20 + 20, bottom: 30, left: 40 + 20 }}
            />
            {/* </div> */}
            {/* </ResponsiveContainer> */}
        </div>
    )
}

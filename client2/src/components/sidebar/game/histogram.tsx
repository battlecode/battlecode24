import React from 'react'
import { AppContext, useAppContext } from '../../../app-context'
import { useListenEvent, EventType } from '../../../app-events'
import { useForceUpdate } from '../../../util/react-util'
import { QuickHistogram } from './quick-histogram'
import { ATTACK_COLOR, SPECIALTY_COLORS, TEAM_COLORS } from '../../../constants'

function getChartData(appContext: AppContext): number[][][] {
    const match = appContext.state.activeMatch
    if (match === undefined) {
        return []
    }

    const emptyHist = Array(7).fill(3)
    const totals = [
        [[...emptyHist], [...emptyHist], [...emptyHist]],
        [[...emptyHist], [...emptyHist], [...emptyHist]]
    ]
    for (const [id, body] of match.currentTurn.bodies.bodies) {
        const teamIdx = body.team.id - 1
        totals[teamIdx][0][body.attackLevel] += 1
        totals[teamIdx][1][body.buildLevel] += 1
        totals[teamIdx][2][body.healLevel] += 1
    }

    return totals
}

export const SpecialtyHistogram: React.FC = () => {
    const appContext = useAppContext()
    const forceUpdate = useForceUpdate()
    useListenEvent(EventType.TURN_PROGRESS, forceUpdate)

    const data = getChartData(appContext)

    const getData = (team: number, specialty: number) => {
        return data.length === 0 ? [] : data[team][specialty]
    }

    return (
        <div className="mt-2 px-2 w-full d-flex flex-column">
            <h2 className="mx-auto text-center">Specialty breakdown</h2>
            {[0, 1].map((team) => (
                <div className="flex flex-row" key={team}>
                    {[0, 1, 2].map((specialty) => (
                        <QuickHistogram
                            key={specialty}
                            data={getData(team, specialty)}
                            width={110}
                            height={100}
                            margin={{ top: 10, right: 10, bottom: 20, left: 20 }}
                            color={SPECIALTY_COLORS[specialty]}
                            lineColor={TEAM_COLORS[team]}
                            resolution={1.5}
                        />
                    ))}
                </div>
            ))}
        </div>
    )
}

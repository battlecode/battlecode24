import React from 'react'
import { useAppContext } from '../../../app-context'
import { useForceUpdate, useRefresh } from '../../../util/react-util'
import { useListenEvent, EventType } from '../../../app-events'

interface Props {
    team: 0 | 1
}

export const UnitsTable: React.FC<Props> = (props: Props) => {
    const context = useAppContext();
    const game = context.state.activeGame
    const teamStat = game?.currentMatch?.currentTurn.stat.getTeamStat(game.teams[props.team])

    const columnClassName = 'w-[50px] text-center mx-auto'

    // useRefresh(1000)
    const forceUpdate = useForceUpdate()
    useListenEvent(EventType.TURN_PROGRESS, forceUpdate)

    const columns: Array<[string, React.ReactElement]> = [
        ['HQ', <p className={columnClassName}>HQ</p>],
        ['Robot 1', <p className={columnClassName}>R1</p>],
        ['Robot 2', <p className={columnClassName}>R2</p>],
        ['Robot 3', <p className={columnClassName}>R3</p>],
        ['Robot 4', <p className={columnClassName}>R3</p>],
        ['Robot 5', <p className={columnClassName}>R3</p>]
    ]

    const data: Array<[string, Array<number>]> = [
        ['Count', teamStat?.robots ?? [0, 0, 0, 0, 0, 0]],
        ['Σ(HP)', teamStat?.total_hp ?? [0, 0, 0, 0, 0, 0]]
    ]

    return (
        <table className="my-2">
            <thead>
                <tr className="mb-4">
                    <th className="pb-1"></th>
                    {columns.map((column) => (
                        <th className="pb-1" title={column[0]} key={column[0]}>
                            {column[1]}
                        </th>
                    ))}
                </tr>
            </thead>
            <tbody>
                {data.map((dataRow, rowIndex) => (
                    <tr key={rowIndex}>
                        <th className="text-sm">
                            {dataRow[0]}
                        </th>
                        {dataRow[1].map((value, colIndex) => (
                            <td className="text-center text-sm" key={rowIndex + ':' + colIndex}>
                                {value}
                            </td>
                        ))}
                    </tr>
                ))}
            </tbody>
        </table>
    )
}

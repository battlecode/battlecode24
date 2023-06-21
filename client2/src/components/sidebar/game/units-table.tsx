import React, { useEffect } from 'react'
import { useAppContext } from '../../../app-context'
import { useForceUpdate } from '../../../util/react-util'
import { useListenEvent, EventType } from '../../../app-events'
import { getImageIfLoaded, removeTriggerOnImageLoad, triggerOnImageLoad } from '../../../util/ImageLoader'

interface UnitsIconProps {
    team: 0 | 1
    robotType: string
}

const UnitsIcon: React.FC<UnitsIconProps> = (props: UnitsIconProps) => {
    const color = props.team == 0 ? 'red' : 'blue'
    const imagePath = `robots/${color}_${props.robotType}_smaller.png`

    const imageData = getImageIfLoaded(imagePath)

    const forceUpdate = useForceUpdate()
    useEffect(() => {
        triggerOnImageLoad(forceUpdate, imagePath)
        return () => {
            removeTriggerOnImageLoad(forceUpdate, imagePath)
        }
    }, [])

    return (
        <th key={imagePath} className="pb-1 w-[50px] h-[50px]">
            <img src={imageData?.src} className="w-full h-full"></img>
        </th>
    )
}

interface UnitsTableProps {
    team: 0 | 1
}

export const UnitsTable: React.FC<UnitsTableProps> = (props: UnitsTableProps) => {
    const context = useAppContext()
    const game = context.state.activeGame
    const teamStat = game?.currentMatch?.currentTurn.stat.getTeamStat(game.teams[props.team])

    const forceUpdate = useForceUpdate()
    useListenEvent(EventType.TURN_PROGRESS, forceUpdate)

    const columns: Array<[string, React.ReactElement]> = [
        ['Headquarters', <UnitsIcon team={props.team} robotType='headquarters' />],
        ['Carrier', <UnitsIcon team={props.team} robotType='carrier'/>],
        ['Launcher', <UnitsIcon team={props.team} robotType='launcher'/>],
        ['Amplifier', <UnitsIcon team={props.team} robotType='amplifier'/>],
        ['Destabilizer', <UnitsIcon team={props.team} robotType='destabilizer'/>],
        ['Booster', <UnitsIcon team={props.team} robotType='booster'/>],
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
                    {columns.map((column) => column[1])}
                </tr>
            </thead>
            <tbody>
                {data.map((dataRow, rowIndex) => (
                    <tr key={rowIndex}>
                        <th className="text-sm">{dataRow[0]}</th>
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

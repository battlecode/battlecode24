import React, { useEffect } from 'react'
import { useAppContext } from '../../../app-context'
import { useForceUpdate } from '../../../util/react-util'
import { useListenEvent, EventType } from '../../../app-events'
import { getImageIfLoaded, removeTriggerOnImageLoad, triggerOnImageLoad } from '../../../util/ImageLoader'
import { TEAM_COLOR_NAMES } from '../../../constants'
import { schema } from 'battlecode-schema'
import { TeamTurnStat } from '../../../playback/TurnStat'
import { DoubleChevronUpIcon } from '../../../icons/chevron'

interface UnitsIconProps {
    team: 0 | 1
    robotType: string
}

const UnitsIcon: React.FC<UnitsIconProps> = (props: UnitsIconProps) => {
    const color = TEAM_COLOR_NAMES[props.team].toLowerCase()
    const imagePath = `robots/${color}/${props.robotType}.png`

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
    const forceUpdate = useForceUpdate()
    useListenEvent(EventType.TURN_PROGRESS, forceUpdate)

    const columns: Array<[string, React.ReactElement]> = [
        ['Base', <UnitsIcon team={props.team} robotType="base" key="0" />],
        ['Attack', <UnitsIcon team={props.team} robotType="attack" key="1" />],
        ['Build', <UnitsIcon team={props.team} robotType="build" key="2" />],
        ['Heal', <UnitsIcon team={props.team} robotType="heal" key="3" />]
    ]

    const match = context.state.activeGame?.currentMatch
    const teamStat = match?.currentTurn.stat.getTeamStat(match.game.teams[props.team])
    const totalCount = Math.max(teamStat?.robots.reduce((a, b) => a + b) ?? 0, 1)
    const data: Array<[string, Array<number>]> = [
        ['Count', teamStat?.robots ?? [0, 0, 0, 0]],
        ['Σ(HP)', teamStat?.totalHealth ?? [0, 0, 0, 0]],
        [
            'Avg. Level',
            teamStat?.specializationTotalLevels.map((c) => Math.round((c / totalCount) * 100) / 100) ?? [0, 0, 0, 0]
        ]
    ]

    return (
        <>
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
            <GlobalUpgradeSection teamStat={teamStat} />
        </>
    )
}

const GlobalUpgradeSection: React.FC<{ teamStat: TeamTurnStat | undefined }> = ({ teamStat }) => {
    const upgradeTypes: [schema.GlobalUpgradeType, string][] = [
        [schema.GlobalUpgradeType.ACTION_UPGRADE, 'Global Action Upgrade'],
        [schema.GlobalUpgradeType.CAPTURING_UPGRADE, 'Global Capturing Upgrade'],
        [schema.GlobalUpgradeType.HEALING_UPGRADE, 'Global Healing Upgrade']
    ]
    if (!teamStat) return <> </>
    return (
        <>
            {upgradeTypes.map(
                ([type, name]) =>
                    teamStat.globalUpgrades.has(type) && (
                        <div className="text-sm flex flex-row justify-center font-bold" key={type}>
                            <DoubleChevronUpIcon />
                            {name}
                        </div>
                    )
            )}
        </>
    )
}

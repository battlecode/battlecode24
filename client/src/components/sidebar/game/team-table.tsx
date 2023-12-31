import React, { useEffect } from 'react'
import { useAppContext } from '../../../app-context'
import { useForceUpdate } from '../../../util/react-util'
import { useListenEvent, EventType } from '../../../app-events'
import { getImageIfLoaded, imageSource, removeTriggerOnImageLoad, triggerOnImageLoad } from '../../../util/ImageLoader'
import { TEAM_COLOR_NAMES } from '../../../constants'
import { schema } from 'battlecode-schema'
import { TeamTurnStat } from '../../../playback/TurnStat'
import { DoubleChevronUpIcon } from '../../../icons/chevron'
import { Team } from '../../../playback/Game'
import { CurrentMap } from '../../../playback/Map'

interface UnitsIconProps {
    teamIdx: 0 | 1
    robotType: string
}

const UnitsIcon: React.FC<UnitsIconProps> = (props: UnitsIconProps) => {
    const color = TEAM_COLOR_NAMES[props.teamIdx].toLowerCase()
    const imagePath = `robots/${color}/${props.robotType}.png`

    return (
        <th key={imagePath} className="pb-1 w-[50px] h-[50px]">
            <img src={imageSource(imagePath)} className="w-full h-full"></img>
        </th>
    )
}

interface TeamTableProps {
    teamIdx: 0 | 1
}

export const TeamTable: React.FC<TeamTableProps> = (props: TeamTableProps) => {
    const context = useAppContext()
    const forceUpdate = useForceUpdate()
    useListenEvent(EventType.TURN_PROGRESS, forceUpdate)

    const match = context.state.activeMatch
    const teamStat = match?.currentTurn?.stat.getTeamStat(match.game.teams[props.teamIdx])

    const map = match?.currentTurn?.map

    return (
        <div className="flex flex-col">
            <UnitsTable teamStat={teamStat} teamIdx={props.teamIdx} />
            <ResourceTable map={map} teamStat={teamStat} teamIdx={props.teamIdx} />
            <GlobalUpgradeSection teamStat={teamStat} />
        </div>
    )
}

interface ResourceTableProps {
    teamStat: TeamTurnStat | undefined
    map: CurrentMap | undefined
    teamIdx: 0 | 1
}

export const ResourceTable: React.FC<ResourceTableProps> = ({ map, teamStat, teamIdx }) => {
    let flags = 3
    let carried = 0
    let crumbs = 0
    if (map && teamStat) {
        flags = 0
        for (const flag of map.flagData.values()) {
            if (flag.team === teamIdx) {
                flags++
                if (flag.carrierId) carried++
            }
        }
        crumbs = teamStat.resourceAmount
    }
    return (
        <div className="flex items-center mt-2 mb-1 text-xs font-bold justify-around">
            <div className="flex items-center w-[135px] ml-5">
                Crumbs: {crumbs}
                <div className="w-[30px] h-[30px]">
                    <img style={{ transform: 'scale(1.5)' }} src={imageSource('resources/crumb_1.png')} />
                </div>
            </div>
            <div className="flex items-center w">
                Flags:
                {[1, 2, 3].map((i) => (
                    <div className="w-[40px]" key={i}>
                        <div className="w-[30px] h-[30px] mx-auto">
                            <img
                                className={flags >= i ? '' : 'opacity-25'}
                                src={
                                    carried < i
                                        ? imageSource('resources/bread.png')
                                        : imageSource('resources/bread_outline.png')
                                }
                            />
                        </div>
                    </div>
                ))}
            </div>
        </div>
    )
}

interface UnitsTableProps {
    teamStat: TeamTurnStat | undefined
    teamIdx: 0 | 1
}

export const UnitsTable: React.FC<UnitsTableProps> = ({ teamStat, teamIdx }) => {
    const columns: Array<[string, React.ReactElement]> = [
        ['Base', <UnitsIcon teamIdx={teamIdx} robotType="base" key="0" />],
        ['Attack', <UnitsIcon teamIdx={teamIdx} robotType="attack" key="1" />],
        ['Build', <UnitsIcon teamIdx={teamIdx} robotType="build" key="2" />],
        ['Heal', <UnitsIcon teamIdx={teamIdx} robotType="heal" key="3" />]
    ]

    const totalCount = Math.max(teamStat?.robots.reduce((a, b) => a + b) ?? 0, 1)
    const data: Array<[string, Array<number>]> = [
        ['Count', teamStat?.robots ?? [0, 0, 0, 0]],
        [
            'Avg. Level',
            teamStat?.specializationTotalLevels.map((c) => Math.round((c / totalCount) * 100) / 100) ?? [0, 0, 0, 0]
        ]
    ]

    return (
        <>
            <table className="my-1">
                <thead>
                    <tr className="mb-2">
                        <th className="pb-1"></th>
                        {columns.map((column) => column[1])}
                    </tr>
                </thead>
                <tbody>
                    {data.map((dataRow, rowIndex) => (
                        <tr key={rowIndex}>
                            <th className="text-xs">{dataRow[0]}</th>
                            {dataRow[1].map((value, colIndex) => (
                                <td className="text-center text-xs" key={rowIndex + ':' + colIndex}>
                                    {value}
                                </td>
                            ))}
                        </tr>
                    ))}
                </tbody>
            </table>
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

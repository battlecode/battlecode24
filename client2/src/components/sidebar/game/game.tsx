import React from 'react'
import { ChevronDownIcon, ChevronUpIcon } from '../../../icons/chevron'
import { UnitsTable } from './units-table'
import { ResourceGraph } from './resource-graph'
import { useSearchParamBool } from '../../../app-search-params'
import { useAppContext } from '../../../app-context'
import { SectionHeader } from '../../section-header'

export const GamePage: React.FC = () => {
    const context = useAppContext()
    const activeGame = context.state.activeGame

    const teamBoxClasses = 'w-full h-[50px] flex items-center text-center justify-center'
    const showStatsArrowClasses = 'stroke-2 '

    const [showStats, setShowStats] = useSearchParamBool('showStats', true)

    const NO_GAME_TEAM_NAME = '?????'

    return (
        <div className="flex flex-col">
            <div className={teamBoxClasses + ' bg-team0'}>
                <p>{activeGame?.teams[0].name ?? NO_GAME_TEAM_NAME}</p>
            </div>
            <UnitsTable team={0} />

            <div className="h-[30px]" />

            <div className={teamBoxClasses + ' bg-team1'}>
                <p>{activeGame?.teams[1].name ?? NO_GAME_TEAM_NAME}</p>
            </div>
            <UnitsTable team={1} />

            <SectionHeader
                title="Stats"
                open={showStats}
                onClick={() => setShowStats(!showStats)}
                containerClassName="mt-5"
                titleClassName="mb-3 py-2"
            >
                {/* Note: to keep animation smooth, we should still keep the elements rendered, but we pass showStats into
                    them so that they don't render any data (since we're likely hiding stats to prevent lag) */}
                {
                <ResourceGraph active={showStats} property="resourceAmount" propertyDisplayName='Bread'/>
                }
            </SectionHeader>
        </div>
    )
}

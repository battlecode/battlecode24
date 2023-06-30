import React from 'react'
import { ChevronDownIcon, ChevronUpIcon } from '../../../icons/chevron'
import { UnitsTable } from './units-table'
import { ResourceGraph } from './resource-graph'
import { useSearchParamBool } from '../../../app-search-params'
import { useAppContext } from '../../../app-context'

export const GamePage: React.FC = () => {
    const context = useAppContext()
    const activeGame = context.state.activeGame

    const teamBoxClasses = 'w-full h-[50px] flex items-center text-center justify-center'
    const showStatsArrowClasses = 'stroke-2 '

    const [showStats, setShowStats] = useSearchParamBool('showStats', true)

    const NO_GAME_TEAM_NAME = '?????'

    return (
        <div className="flex flex-col">
            <div className={teamBoxClasses + ' bg-red'}>
                <p>{activeGame?.teams[0].name ?? NO_GAME_TEAM_NAME}</p>
            </div>
            <UnitsTable team={0} />

            <div className="h-[30px]" />

            <div className={teamBoxClasses + ' bg-blue'}>
                <p>{activeGame?.teams[1].name ?? NO_GAME_TEAM_NAME}</p>
            </div>
            <UnitsTable team={1} />

            <button
                onClick={() => setShowStats(!showStats)}
                className="flex flex-row justify-between mt-8 mb-1 font-bold hover:bg-lightHighlight p-3 rounded-md border-black border"
            >
                Stats{' '}
                {showStats ? (
                    <ChevronDownIcon className={showStatsArrowClasses} />
                ) : (
                    <ChevronUpIcon className={showStatsArrowClasses} />
                )}
            </button>

            {/* Note: to keep animation smooth, we should still keep the elements rendered, but we pass showStats into
                      them so that they don't render any data (since we're likely hiding stats to prevent lag) */}
            <div
                className={
                    'flex flex-col transition-max-height overflow-hidden duration-300 ease-in ' +
                    (showStats ? 'max-h-[3000px]' : 'max-h-0')
                }
            >
                <ResourceGraph active={showStats} property="adamantium" propertyDisplayName='Adamantium'/>
                <ResourceGraph active={showStats} property="mana" propertyDisplayName='Mana'/>
                <ResourceGraph active={showStats} property="elixir" propertyDisplayName='Elixir'/>
            </div>
        </div>
    )
}

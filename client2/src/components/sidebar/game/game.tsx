import React from 'react'
import { ChevronDownIcon, ChevronUpIcon } from '../../../icons/chevron'
import { UnitsTable } from './units-table'
import { ResourceGraph } from './resource-graph'
import { useSearchParamBool } from '../../../app-search-params'

export const GamePage: React.FC = () => {
    const teamBoxClasses = 'w-full h-[50px] flex items-center text-center justify-center'
    const showStatsArrowClasses = 'stroke-2 '

    const [showStats, setShowStats] = useSearchParamBool('showStats', true)

    return (
        <div className="flex flex-col">
            <div className={teamBoxClasses + " bg-red"}>
                <p>Team 1</p>
            </div>
            <UnitsTable team={true}/>

            <div className="h-[30px]" />

            <div className={teamBoxClasses + " bg-blue"}>
                <p>Team 2</p>
            </div>
            <UnitsTable team={false}/>

            <button
                onClick={() => setShowStats(!showStats)}
                className="flex flex-row justify-between mt-8 mb-1 font-bold hover:bg-lightHighlight p-3 rounded-md border-black border"
            >
                Stats {showStats ? <ChevronDownIcon className={showStatsArrowClasses}/> : <ChevronUpIcon className={showStatsArrowClasses}/>}
            </button>

            {/* Note: to keep animation smooth, we should still keep the elements rendered, but we pass showStats into
                      them so that they don't render any data (since we're likely hiding stats to prevent lag) */}
            <div className={"flex flex-col transition-max-height overflow-hidden duration-300 ease-in-out " + (showStats ? "max-h-96" : "max-h-0")}>
                <ResourceGraph active={showStats}/>
            </div>
        </div>
    )
}

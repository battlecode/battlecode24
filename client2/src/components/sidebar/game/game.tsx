import React from 'react'
import { ChevronDownIcon, ChevronUpIcon } from '../../../icons/chevron'
import { UnitsTable } from './units-table'

export const GamePage: React.FC = () => {
    const teamBoxClasses =
        'w-full h-[50px] flex items-center text-center justify-center'

    const [showStats, setShowStats] = React.useState(false)

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

            <button onClick={() => setShowStats(!showStats)} className="flex gap-2 my-4 font-bold">
                Stats {showStats ? <ChevronUpIcon className="stroke-2" /> : <ChevronDownIcon className="stroke-2"/>}
            </button>

            {
                showStats && <div className="flex flex-col">
                    <p>One stats components here</p>
                    <p>Another one here</p>
                </div>
            }
        </div>
    )
}

import React from 'react'
import { ChevronDownIcon, ChevronUpIcon } from '../../../icons/chevron'

export const GamePage: React.FC = () => {
    const teamBoxClasses =
        'w-full h-[50px] shadow-centered flex items-center text-center justify-center'

    const [showStats, setShowStats] = React.useState(false)

    return (
        <div className="flex flex-col">
            <div className={teamBoxClasses + " bg-teamRed"}>
                <p>Team 1</p>
            </div>

            <div className="h-[50px]" />

            <div className={teamBoxClasses + " bg-teamBlue"}>
                <p>Team 2</p>
            </div>

            <button onClick={() => setShowStats(!showStats)} className="flex gap-2 my-4">
                Stats {showStats ? <ChevronUpIcon /> : <ChevronDownIcon />}
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

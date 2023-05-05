import React from 'react'
import { useAppContext } from '../components/app-context'
import { ChevronDownIcon, ChevronUpIcon } from '../icons/chevron'

export const GamePage: React.FC = () => {
    const appContext = useAppContext()
    const game = appContext.state.activeGame

    const teamBoxClasses =
        'w-full h-[50px] shadow-centered flex items-center text-center justify-center'

    const [showStats, setShowStats] = React.useState(false)

    return (
        <div className="flex flex-col">
            <div className={teamBoxClasses} style={{ backgroundColor: 'rgba(239,68,68,0.6)' }}>
                <p>Team 1</p>
            </div>

            <div className="h-[50px]" />

            <div className={teamBoxClasses} style={{ backgroundColor: 'rgba(6,182,212,0.6)' }}>
                <p>Team 2</p>
            </div>

            <button onClick={() => setShowStats(!showStats)} className="flex gap-2 my-4">
                Stats {showStats ? <ChevronUpIcon /> : <ChevronDownIcon />}
            </button>
        </div>
    )
}

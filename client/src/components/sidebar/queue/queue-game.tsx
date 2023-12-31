import React, { useState } from 'react'
import Game from '../../../playback/Game'
import Match from '../../../playback/Match'
import { useAppContext } from '../../../app-context'
import { IconContext } from 'react-icons'
import { IoCloseCircle, IoCloseCircleOutline } from 'react-icons/io5'

interface Props {
    game: Game
}

export const QueuedGame: React.FC<Props> = (props) => {
    const context = useAppContext()
    const isTournamentMode = context.state.tournament !== undefined
    const [hoveredClose, setHoveredClose] = useState(false)

    const setMatch = (match: Match) => {
        match.jumpToTurn(0)
        props.game.currentMatch = match
        context.setState({
            ...context.state,
            activeGame: match.game,
            activeMatch: match
        })
    }

    const close = () => {
        context.setState({
            ...context.state,
            queue: context.state.queue.filter((v) => v !== props.game),
            activeGame: context.state.activeGame === props.game ? undefined : context.state.activeGame,
            activeMatch: context.state.activeGame === props.game ? undefined : context.state.activeMatch
        })
    }

    return (
        <div className="relative mr-auto rounded-md bg-lightCard border-gray-500 border mb-4 p-3 w-full shadow-md">
            <div className="text-xs whitespace mb-2">
                <span className="font-bold text-team0">{props.game.teams[0].name}</span>
                <span className="mx-1.5">vs</span>
                <span className="font-bold text-team1">{props.game.teams[1].name}</span>
            </div>
            {props.game.matches.map((match, i) => (
                <p
                    key={i}
                    className={
                        'leading-4 rounded-sm border-gray-500 border my-1.5 py-1 px-2 ' +
                        'bg-light hover:bg-lightHestighlight cursor-pointer ' +
                        (context.state.activeMatch === match ? 'bg-lightHighlight hover:bg-medHighlight' : '')
                    }
                    onClick={() => setMatch(match)}
                >
                    <span className="text-xxs font-bold">{match.map.name}</span>
                    {!isTournamentMode && (
                        <span className="text-xxs leading-tight">
                            <span className="mx-1">-</span>
                            <span className={`font-bold text-team${match.winner.id - 1}`}>{match.winner.name}</span>
                            <span>{` wins after ${match.maxTurn} rounds`}</span>
                        </span>
                    )}
                </p>
            ))}
            <div
                className="absolute -right-3 -top-3 w-6 h-6 cursor-pointer rounded-full bg-white"
                onClick={() => close()}
                onMouseEnter={() => setHoveredClose(true)}
                onMouseLeave={() => setHoveredClose(false)}
            >
                <IconContext.Provider
                    value={{
                        color: 'black',
                        className: 'w-full h-full'
                    }}
                >
                    {hoveredClose ? <IoCloseCircle /> : <IoCloseCircleOutline />}
                </IconContext.Provider>
            </div>
        </div>
    )
}

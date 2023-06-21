import React from 'react'
import Game from '../../../playback/Game'
import Match from '../../../playback/Match'
import { useAppContext } from '../../../app-context';

interface Props {
    game: Game
}

export const QueuedGame: React.FC<Props> = (props) => {
    const context = useAppContext()

    const setMatch = (match: Match) => {
        match.jumpToTurn(0)
        props.game.currentMatch = match
        context.setState({
            ...context.state,
            activeGame: match.game,
            activeMatch: match
        })
    }

    return (
        <div className="mr-auto rounded-md border-black border mb-2 px-2 pt-1 pb-2">
            <span className="text-xs whitespace-nowrap">
                {props.game.teams[0].name} vs {props.game.teams[1].name}
            </span>
            {props.game.matches.map((match, i) => (
                <p
                    key={i}
                    className={"rounded-sm border-black border mt-1 hover:bg-lightHighlight px-2 cursor-pointer " + (context.state.activeMatch === match ? "bg-medHighlight hover:bg-medHighlight" : "")}
                    onClick={() => setMatch(match)}
                >
                    <span className="text-xs">{match.map.name}</span>
                </p>
            ))}
        </div>
    )
}

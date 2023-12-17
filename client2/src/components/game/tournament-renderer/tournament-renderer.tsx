import React from 'react'
import { useAppContext } from '../../../app-context'
import { TournamentGameElement } from './tournament-game';

export const TournamentRenderer: React.FC = () => {
    const appContext = useAppContext()

    return (
        <div className="w-full h-screen flex items-center justify-center">
            {appContext.state.tournament ? (
                [...appContext.state.tournament.games.entries()].map(([id, game]) => (
                    <TournamentGameElement game={game} key={id}/>
                ))
            ) : (
                <>Missing Tournament</>
            )}
        </div>
    )
}

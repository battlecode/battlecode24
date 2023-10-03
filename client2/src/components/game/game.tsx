import React from 'react'
import { GameRenderer } from './game-renderer'
import { useAppContext } from '../../app-context';
import { TournamentRenderer } from './tournament-renderer';

export const Game: React.FC = () => {
    const appContext = useAppContext()

    if (!appContext.state.activeGame && appContext.state.tournament)
        return <TournamentRenderer />

    return <GameRenderer />
}

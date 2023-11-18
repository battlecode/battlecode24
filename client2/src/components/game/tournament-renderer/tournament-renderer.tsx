import React from 'react'
import { useAppContext } from '../../../app-context'
import { Vector } from '../../../playback/Vector'
import { EventType, publishEvent, useListenEvent } from '../../../app-events'
import * as cst from '../../../constants'
import assert from 'assert'
import Tooltip from '../tooltip'
import { Button } from '../../button'
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

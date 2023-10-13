import React from 'react'
import { useAppContext } from '../../../app-context'
import { Vector } from '../../../playback/Vector'
import { EventType, publishEvent, useListenEvent } from '../../../app-events'
import * as cst from '../../../constants'
import assert from 'assert'
import Tooltip from '../tooltip'
import { Button } from '../../button'
import { TournamentGame } from '../../../playback/Tournament'
import Game from '../../../playback/Game'
import { PageType, usePage } from '../../../app-search-params'

interface Props {
    game: TournamentGame
}
export const TournamentGameElement: React.FC<Props> = ({ game }) => {
    const [loadingGame, setLoadingGame] = React.useState(false)
    const [page, setPage] = usePage()
    const appContext = useAppContext()

    const onClick = loadingGame
        ? () => {}
        : () => {
              setLoadingGame(true)

              fetch(game.gameFile).then((response) => {
                  response.arrayBuffer().then((buffer) => {
                      if (buffer.byteLength == 0) {
                          alert('Error: Game file is empty.')
                          setLoadingGame(false)
                          return
                      }
                      const loadedGame = Game.loadFullGameRaw(buffer)
                      appContext.setState({
                          ...appContext.state,
                          activeGame: loadedGame,
                          activeMatch: loadedGame.currentMatch,
                          queue: appContext.state.queue.concat([loadedGame])
                      })
                      game.viewed = true
                      setPage(PageType.GAME)
                      setLoadingGame(false)
                  })
              })
          }

    return (
        <div
            className={
                'bg-orange-500 p-3 m-4 rounded-lg shadow-md d-flex flex-col cursor-pointer ' +
                (loadingGame ? 'opacity-50' : '')
            }
            onClick={onClick}
        >
            <div className={game.viewed && game.winner == 0 ? 'font-bold' : ''}>{game.teams[0]}</div>
            <div className={(game.viewed && game.winner == 1 ? 'font-bold' : '') + ' pt-2'}>{game.teams[1]}</div>
        </div>
    )
}

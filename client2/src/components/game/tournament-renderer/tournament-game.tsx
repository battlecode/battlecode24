import React from 'react'
import { useAppContext } from '../../../app-context'
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
    lines?: { dx: number; dy: number }
}

export const TournamentGameElement: React.FC<Props> = ({ lines, game }) => {
    const [loadingGame, setLoadingGame] = React.useState(false)
    const [page, setPage] = usePage()
    const appContext = useAppContext()

    const onClick = loadingGame
        ? () => {}
        : () => {
              setLoadingGame(true)

              fetch(game.gameFile).then((response) => {
                  response.arrayBuffer().then((buffer) => {
                      if (buffer.byteLength === 0) {
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
                'bg-orange-500 p-3 m-4 rounded-lg shadow-md d-flex flex-col cursor-pointer relative ' +
                (loadingGame ? 'opacity-50' : '')
            }
            onClick={onClick}
        >
            {lines && (
                <svg
                    width={lines.dx * 2}
                    height={lines.dy}
                    style={{
                        position: 'absolute',
                        zIndex: -1,
                        left: '50%',
                        top: '100%',
                        transform: 'translateX(-50%)'
                    }}
                >
                    {/* down 40% */}
                    <line x1="50%" y1="0%" x2="50%" y2="40%" stroke="white" strokeWidth="2" />
                    {/* left and right */}
                    <line x1="50%" y1="40%" x2="0%" y2="40%" stroke="white" strokeWidth="2" />
                    <line x1="50%" y1="40%" x2="100%" y2="40%" stroke="white" strokeWidth="2" />
                    {/* down 60% */}
                    <line x1="0%" y1="40%" x2="0%" y2="100%" stroke="white" strokeWidth="4" />
                    <line x1="100%" y1="40%" x2="100%" y2="100%" stroke="white" strokeWidth="4" />
                </svg>
            )}
            <div className={game.viewed && game.winner === 0 ? 'font-bold' : ''}>{game.teams[0]}</div>
            <div className={(game.viewed && game.winner === 1 ? 'font-bold' : '') + ' pt-2'}>{game.teams[1]}</div>
        </div>
    )
}

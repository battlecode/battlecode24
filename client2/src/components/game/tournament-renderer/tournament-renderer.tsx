import React, { useRef, useState } from 'react'
import { useAppContext } from '../../../app-context'
import { TournamentGameElement } from './tournament-game'
import Tournament from '../../../playback/Tournament'
import { Space } from 'react-zoomable-ui'
import { useForceUpdate } from '../../../util/react-util'

export const TournamentRenderer: React.FC = () => {
    const appContext = useAppContext()

    return (
        <div className="w-full h-screen relative">
            <Space treatTwoFingerTrackPadGesturesLikeTouch={true}>
                {appContext.state.tournament ? (
                    <TournamentTree tournament={appContext.state.tournament} />
                ) : (
                    <>Missing Tournament</>
                )}
            </Space>
        </div>
    )
}

interface TournamentTreeProps {
    tournament: Tournament
}

const TournamentTree: React.FC<TournamentTreeProps> = ({ tournament }) => {
    const [leafWidth, setLeafWidth] = useState<number | undefined>(undefined)
    const leafRef = useRef<HTMLDivElement>(null)

    //force update on this component resize so nothing in the tree is warped, using resize observer
    const forceUpdate = useForceUpdate()
    const onResize = () => {
        if (leafRef.current) setLeafWidth(leafRef.current.offsetWidth)
        console.log(leafWidth)
        forceUpdate()
    }

    const observedRef = React.useRef<HTMLDivElement>(null)
    React.useEffect(() => {
        if (leafRef.current) setLeafWidth(leafRef.current.offsetWidth)
        const resizeObserver = new ResizeObserver(() => {
            onResize()
        })
        resizeObserver.observe(observedRef.current!)
    }, [])

    return (
        <div className="flex flex-col justify-center" ref={observedRef}>
            {[...tournament.gamesByRound].reverse().map(([round, games]) => {
                return (
                    <div className="flex flex-row" key={round}>
                        {games.map((game, index) => {
                            return (
                                <div
                                    className="flex flex-col flex-grow"
                                    ref={round === 1 && index === 0 ? leafRef : undefined}
                                >
                                    <div className="mx-auto">
                                        <TournamentGameElement
                                            game={game}
                                            lines={
                                                round == 1 || !leafWidth
                                                    ? undefined
                                                    : { dy: 35, dx: Math.pow(2, round - 3) * leafWidth }
                                            }
                                        />
                                    </div>
                                </div>
                            )
                        })}
                    </div>
                )
            })}
        </div>
    )
}

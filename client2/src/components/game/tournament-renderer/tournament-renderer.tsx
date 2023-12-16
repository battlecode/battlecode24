import React, { useEffect, useRef, useState } from 'react'
import { useAppContext } from '../../../app-context'
import { TournamentGameElement } from './tournament-game'
import Tournament, { TournamentGame } from '../../../playback/Tournament'
import { Space } from 'react-zoomable-ui'
import { useForceUpdate } from '../../../util/react-util'

export const TournamentRenderer: React.FC = () => {
    const appContext = useAppContext()

    return (
        <div className="w-full h-screen relative">
            <Space treatTwoFingerTrackPadGesturesLikeTouch={false}>
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
    // const [leafWidth, setLeafWidth] = useState<number | undefined>(undefined)
    // const leafRef = useRef<HTMLDivElement>(null)

    // React.useEffect(() => {
    //     if (!leafRef.current) throw new Error('tournament has no round 1 games')
    //     setLeafWidth(leafRef.current.offsetWidth)
    //     const resizeObserver = new ResizeObserver(() => {
    //         if (leafRef.current) setLeafWidth(leafRef.current.offsetWidth)
    //     })
    //     resizeObserver.observe(leafRef.current)
    // }, [])

    const brackets: [TournamentGame, string][] = tournament.losersBracketRoot
        ? [
              [tournament.winnersBracketRoot, 'Winners Bracket'],
              [tournament.losersBracketRoot, 'Losers Bracket']
          ]
        : [[tournament.winnersBracketRoot, '']]

    return (
        <div className="flex flex-row">
            {brackets.map(([rootGame, bracketTitle]) => (
                <div className="flex flex-col justify-center w-max mx-2" key={bracketTitle}>
                    <TournamentGameWrapper game={rootGame} />
                    {bracketTitle && (
                        <div className="text-white pt-2 text-center border-t border-white">{bracketTitle}</div>
                    )}
                </div>
            ))}
        </div>
    )
}

const TournamentGameWrapper: React.FC<{ game: TournamentGame }> = ({ game }) => {
    const wrapperRef = useRef<HTMLDivElement>(null)
    const childWrapper1Ref = useRef<HTMLDivElement>(null)
    const childWrapper2Ref = useRef<HTMLDivElement>(null)

    const [lines, setLines] = useState<{ left: number | undefined; right: number | undefined; down: number }>({
        left: undefined,
        right: undefined,
        down: 0
    })

    useEffect(() => {
        if (!wrapperRef.current) return

        const wrapperRect = wrapperRef.current.getBoundingClientRect()
        const startX = wrapperRect.x + wrapperRect.width / 2
        const startY = wrapperRect.y + wrapperRect.height - 17.45

        let left = undefined
        let right = undefined
        let down = 0

        if (childWrapper1Ref.current) {
            const childWrapper1Rect = childWrapper1Ref.current.getBoundingClientRect()
            const child1X = childWrapper1Rect.x + childWrapper1Rect.width / 2
            const child1Y = childWrapper1Rect.y + 17.45

            left = child1X - startX
            down = child1Y - startY
        }

        if (childWrapper2Ref.current) {
            const childWrapper2Rect = childWrapper2Ref.current.getBoundingClientRect()
            const child2X = childWrapper2Rect.x + childWrapper2Rect.width / 2
            const child2Y = childWrapper2Rect.y + 17.45

            right = child2X - startX
            down = Math.max(down, child2Y - startY)
        }

        setLines({ left, right, down })
    }, [wrapperRef.current, childWrapper1Ref.current, childWrapper2Ref.current])

    return (
        <div className="flex flex-col" key={game.id}>
            <div
                className="flex flex-col flex-grow basis-0 " /*ref={round === 1 && index === 0 ? leafRef : undefined}*/
            >
                <div className="mx-auto relative" ref={wrapperRef}>
                    <TournamentGameElement game={game} />
                    <GameChildrenLines lines={lines} />
                </div>
            </div>
            <div className="flex flex-row">
                {game.dependsOn[0] && (
                    <div className="mx-auto" ref={childWrapper1Ref}>
                        <TournamentGameWrapper game={game.dependsOn[0]} />
                    </div>
                )}
                {game.dependsOn[1] && (
                    <div className="mx-auto" ref={childWrapper2Ref}>
                        <TournamentGameWrapper game={game.dependsOn[1]} />
                    </div>
                )}
            </div>
        </div>
    )
}

const GameChildrenLines: React.FC<{ lines: { left: number | undefined; right: number | undefined; down: number } }> = ({
    lines
}) => {
    if (lines.down === 0) return null

    const buffer = 2
    const fullWidth = Math.max(Math.abs(lines.left ?? 0), Math.abs(lines.right ?? 0)) * 2
    let leftPct = `50%`
    let rightPct = `50%`
    if (fullWidth > 2) {
        const leftPctNum = 50 + ((lines.left ?? 0) / fullWidth) * 100
        leftPct = `calc(${leftPctNum}% + ${buffer}px)`
        const rightPctNum = 50 + ((lines.right ?? 0) / fullWidth) * 100
        rightPct = `calc(${rightPctNum}% - ${buffer}px)`
    }
    return (
        <svg
            width={fullWidth + 2 * buffer}
            height={lines.down}
            style={{
                position: 'absolute',
                zIndex: -1,
                left: '50%',
                top: 'calc(100% - 17.45px)',
                transform: 'translateX(-50%)'
            }}
        >
            {/* down 40% */}
            <line x1="50%" y1="0%" x2="50%" y2="calc(40% + 1px)" stroke="white" strokeWidth="2" />
            {/* left and right */}
            {lines.left !== undefined && (
                <>
                    <line x1="50%" y1="40%" x2={leftPct} y2="40%" stroke="white" strokeWidth="2" />
                    <line x1={leftPct} y1="calc(40% - 1px)" x2={leftPct} y2="100%" stroke="white" strokeWidth="2" />
                </>
            )}
            {lines.right !== undefined && (
                <>
                    <line x1="50%" y1="40%" x2={rightPct} y2="40%" stroke="white" strokeWidth="2" />
                    <line x1={rightPct} y1="calc(40% - 1px)" x2={rightPct} y2="100%" stroke="white" strokeWidth="2" />
                </>
            )}
        </svg>
    )
}

import React, { useEffect, useRef, useState } from 'react'
import { useAppContext } from '../../../app-context'
import { TournamentGameElement } from './tournament-game'
import Tournament, { TournamentGame } from '../../../playback/Tournament'
import { Space } from 'react-zoomable-ui'

export const TournamentRenderer: React.FC = () => {
    const appContext = useAppContext()

    const spaceRef = useRef<Space | null>(null)

    return (
        <div className="w-full h-screen relative">
            <Space ref={spaceRef} treatTwoFingerTrackPadGesturesLikeTouch={false}>
                {appContext.state.tournament && spaceRef.current ? (
                    <TournamentTree
                        tournament={appContext.state.tournament}
                        minRound={appContext.state.tournamentMinRound}
                        spaceRef={spaceRef.current}
                    />
                ) : (
                    <>Missing Tournament</>
                )}
            </Space>
        </div>
    )
}

interface TournamentTreeProps {
    tournament: Tournament
    minRound: number
    spaceRef: Space
}

const TournamentTree: React.FC<TournamentTreeProps> = (props) => {
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

    const brackets: [TournamentGame, string][] = props.tournament.losersBracketRoot
        ? [
              [props.tournament.winnersBracketRoot, 'Winners Bracket'],
              [props.tournament.losersBracketRoot, 'Losers Bracket']
          ]
        : [[props.tournament.winnersBracketRoot, '']]

    return (
        <div className="flex flex-row items-center justify-center w-full h-screen">
            {brackets.map(([rootGame, bracketTitle]) => (
                <div className="flex flex-col justify-center w-max mx-2" key={bracketTitle}>
                    <TournamentGameWrapper game={rootGame} minRound={props.minRound} spaceRef={props.spaceRef} />
                    {bracketTitle && (
                        <div className="text-white pt-2 text-center border-t border-white">{bracketTitle}</div>
                    )}
                </div>
            ))}
        </div>
    )
}

interface TournamentGameWrapperProps {
    game: TournamentGame
    minRound: number
    spaceRef: Space
}

const TournamentGameWrapper: React.FC<TournamentGameWrapperProps> = (props) => {
    const wrapperRef = useRef<HTMLDivElement>(null)
    const childWrapper1Ref = useRef<HTMLDivElement>(null)
    const childWrapper2Ref = useRef<HTMLDivElement>(null)

    const [lines, setLines] = useState<{ left: number | undefined; right: number | undefined; down: number }>({
        left: undefined,
        right: undefined,
        down: 0
    })

    useEffect(() => {
        setTimeout(() => {
            if (!wrapperRef.current) return

            const wrapperRect = wrapperRef.current.getBoundingClientRect()
            const scale = props.spaceRef.viewPort?.zoomFactor ?? 1
            const startX = wrapperRect.x + wrapperRect.width / 2
            const startY = wrapperRect.y + wrapperRect.height - 17.45

            let left = undefined
            let right = undefined
            let down = 0

            if (childWrapper1Ref.current) {
                const childWrapper1Rect = childWrapper1Ref.current.getBoundingClientRect()
                const child1X = childWrapper1Rect.x + childWrapper1Rect.width / 2
                const child1Y = childWrapper1Rect.y + 17.45

                left = Math.abs(child1X - startX) / scale
                down = Math.abs(child1Y - startY)
            }

            if (childWrapper2Ref.current) {
                const childWrapper2Rect = childWrapper2Ref.current.getBoundingClientRect()
                const child2X = childWrapper2Rect.x + childWrapper2Rect.width / 2
                const child2Y = childWrapper2Rect.y + 17.45

                right = Math.abs(child2X - startX) / scale
                down = Math.abs(Math.max(down, child2Y - startY))
            }

            setLines({ left, right, down })
        }, 2)
    }, [wrapperRef.current, childWrapper1Ref.current, childWrapper2Ref.current, props.minRound])

    if (props.game.round < props.minRound) {
        props.game.viewed = true
    }

    return (
        <div className="flex flex-col" key={props.game.id}>
            <div
                className="flex flex-col flex-grow basis-0 " /*ref={round === 1 && index === 0 ? leafRef : undefined}*/
            >
                {props.game.round >= props.minRound && (
                    <div className="mx-auto relative" ref={wrapperRef}>
                        <TournamentGameElement game={props.game} />
                        {props.game.round > props.minRound && <GameChildrenLines lines={lines} />}
                    </div>
                )}
            </div>
            <div className="flex flex-row">
                {props.game.dependsOn[0] && (
                    <div className="mx-auto" ref={childWrapper1Ref}>
                        <TournamentGameWrapper
                            game={props.game.dependsOn[0]}
                            minRound={props.minRound}
                            spaceRef={props.spaceRef}
                        />
                    </div>
                )}
                {props.game.dependsOn[1] && (
                    <div className="mx-auto" ref={childWrapper2Ref}>
                        <TournamentGameWrapper
                            game={props.game.dependsOn[1]}
                            minRound={props.minRound}
                            spaceRef={props.spaceRef}
                        />
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

    const lineWidthValue = 2
    const lineWidth = `${lineWidthValue}px`
    const lineColor = 'white'

    const getSideLines = () => (
        <>
            <div style={{ background: lineColor, width: '100%', minHeight: lineWidth }} />
            <div
                style={{
                    background: lineColor,
                    minWidth: lineWidth,
                    height: '50%',
                    marginTop: `${lines.down / 2 - lineWidthValue}px`
                }}
            />
        </>
    )

    const commonClasses = 'absolute z-[-1] flex items-center'
    return (
        <>
            {/* Top line */}
            <div
                className={commonClasses}
                style={{
                    left: '50%',
                    background: lineColor,
                    width: lineWidth,
                    height: `${lines.down / 2}px`,
                    marginTop: `-${lines.down / 2}px`
                }}
            />
            {/* Left connection */}
            {lines.left !== undefined && (
                <div
                    className={commonClasses}
                    style={{
                        flexDirection: 'row-reverse',
                        left: `calc(50% - ${lines.left}px)`,
                        top: `calc(100% - ${lines.down * 0.5}px)`,
                        width: `${lines.left}px`,
                        height: lines.down
                    }}
                >
                    {getSideLines()}
                </div>
            )}
            {/* Right connection */}
            {lines.right !== undefined && (
                <div
                    className={commonClasses}
                    style={{
                        left: '50%',
                        top: `calc(100% - ${lines.down * 0.5}px)`,
                        width: `${lines.right}px`,
                        height: lines.down
                    }}
                >
                    {getSideLines()}
                </div>
            )}
        </>
    )
}

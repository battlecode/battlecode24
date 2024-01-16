import React from 'react'
import { TeamTable } from './team-table'
import { ResourceGraph } from './resource-graph'
import { SpecialtyHistogram } from './histogram'
import { useSearchParamBool } from '../../../app-search-params'
import { useAppContext } from '../../../app-context'
import { SectionHeader } from '../../section-header'
import { Crown } from '../../../icons/crown'
import { BiMedal } from 'react-icons/bi'
import { EventType, useListenEvent } from '../../../app-events'
import Tooltip from '../../tooltip'
import { useForceUpdate } from '../../../util/react-util'

const NO_GAME_TEAM_NAME = '?????'

interface Props {
    open: boolean
}

export const GamePage: React.FC<Props> = React.memo((props) => {
    const context = useAppContext()
    const activeGame = context.state.activeGame

    const [showStats, setShowStats] = useSearchParamBool('showStats', true)

    const forceUpdate = useForceUpdate()
    useListenEvent(EventType.TURN_PROGRESS, forceUpdate)

    if (!props.open) return null

    const teamBox = (teamIdx: number) => {
        let showMatchWinner =
            !context.state.tournament || (context.state.activeMatch && context.state.activeMatch.currentTurn.isEnd())
        showMatchWinner = showMatchWinner && activeGame && activeGame.currentMatch?.winner === activeGame.teams[teamIdx]
        let showGameWinner =
            !context.state.tournament ||
            (showMatchWinner &&
                context.state.activeMatch ==
                    context.state.activeGame?.matches[context.state.activeGame.matches.length - 1])
        showGameWinner = showGameWinner && activeGame && activeGame.winner === activeGame.teams[teamIdx]

        return (
            <div className={'relative w-full py-2 px-3 text-center ' + (teamIdx == 0 ? 'bg-team0' : 'bg-team1')}>
                <div>{activeGame?.teams[teamIdx].name ?? NO_GAME_TEAM_NAME}</div>
                <div className="absolute top-2 left-3">
                    {showMatchWinner && (
                        <Tooltip text={'Current match winner'} location={'right'}>
                            <BiMedal fontSize={'24px'} width={'20px'} color={'yellow'} />
                        </Tooltip>
                    )}
                </div>
                <div className="absolute top-3 right-3">
                    {showGameWinner && (
                        <Tooltip text={'Overall game winner'} location={'left'}>
                            <Crown />
                        </Tooltip>
                    )}
                </div>
            </div>
        )
    }

    return (
        <div className="flex flex-col overflow-x-hidden">
            {teamBox(0)}
            <TeamTable teamIdx={0} />

            <div className="h-[15px] min-h-[15px]" />

            {teamBox(1)}
            <TeamTable teamIdx={1} />

            <SectionHeader
                title="Stats"
                open={showStats}
                onClick={() => setShowStats(!showStats)}
                containerClassName="mt-2"
                titleClassName="py-2"
            >
                {activeGame ? (
                    <>
                        {/* Note: to keep animation smooth, we should still keep the elements rendered, but we pass showStats into
                            them so that they don't render any data (since we're likely hiding stats to prevent lag) */}
                        <SpecialtyHistogram active={showStats} />
                        <br />
                        <ResourceGraph active={showStats} property="resourceAmount" propertyDisplayName="Crumbs" />
                    </>
                ) : (
                    <div>Select a game to see stats</div>
                )}
            </SectionHeader>
        </div>
    )
})

import React from 'react'
import { UnitsTable } from './units-table'
import { ResourceGraph } from './resource-graph'
import { SpecialtyHistogram } from './histogram'
import { useSearchParamBool } from '../../../app-search-params'
import { useAppContext } from '../../../app-context'
import { SectionHeader } from '../../section-header'
import { Crown } from '../../../icons/crown'
import { EventType, useListenEvent } from '../../../app-events'

export const GamePage: React.FC = () => {
    const context = useAppContext()
    const activeGame = context.state.activeGame

    const teamBoxClasses = 'w-full h-[50px] flex items-center text-center justify-center'

    const [showStats, setShowStats] = useSearchParamBool('showStats', true)

    const NO_GAME_TEAM_NAME = '?????'

    const [showWinner, setShowWinner] = React.useState(
        context.state.tournament === undefined ||
            (context.state.activeMatch && context.state.activeMatch.currentTurn.isEnd())
    )
    useListenEvent(EventType.TURN_PROGRESS, () =>
        setShowWinner(
            context.state.tournament === undefined ||
                (context.state.activeMatch && context.state.activeMatch.currentTurn.isEnd())
        )
    )

    return (
        <div className="flex flex-col">
            <div className={teamBoxClasses + ' bg-team0'}>
                <p className="flex">
                    {activeGame?.teams[0].name ?? NO_GAME_TEAM_NAME}
                    {activeGame && activeGame.winner === activeGame.teams[0] && showWinner && (
                        <Crown className="ml-2 mt-1" />
                    )}
                </p>
            </div>
            <UnitsTable team={0} />

            <div className="h-[30px]" />

            <div className={teamBoxClasses + ' bg-team1'}>
                <p className="flex">
                    {activeGame?.teams[1].name ?? NO_GAME_TEAM_NAME}1
                    {activeGame && activeGame.winner === activeGame.teams[1] && showWinner && (
                        <Crown className="ml-2 mt-1" />
                    )}
                </p>
            </div>
            <UnitsTable team={1} />

            <SectionHeader
                title="Stats"
                open={showStats}
                onClick={() => setShowStats(!showStats)}
                containerClassName="mt-5"
                titleClassName="mb-3 py-2"
            >
                {/* Note: to keep animation smooth, we should still keep the elements rendered, but we pass showStats into
                    them so that they don't render any data (since we're likely hiding stats to prevent lag) */}
                <ResourceGraph active={showStats} property="resourceAmount" propertyDisplayName="Bread" />
                <SpecialtyHistogram />
            </SectionHeader>
        </div>
    )
}

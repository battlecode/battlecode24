import React, { useRef } from 'react'
import * as ControlIcons from '../../icons/controls'
import { ControlsBarButton } from './controls-bar-button'
import { useAppContext } from '../../app-context'
import { useForceUpdate } from '../../util/react-util'
import { useListenEvent, EventType } from '../../app-events'

export const ControlsBarTimeline: React.FC = () => {
    const appContext = useAppContext()
    const forceUpdate = useForceUpdate()
    useListenEvent(EventType.TURN_PROGRESS, forceUpdate)

    let down = useRef(false)
    const timelineHover = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
        if (!down.current) return
        timelineClick(e)
    }

    const timelineDown = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
        down.current = true
        timelineClick(e)
    }

    const timelineUp = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
        down.current = false
    }

    const timelineClick = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
        const rect = e.currentTarget.getBoundingClientRect()
        const x = e.clientX - rect.left
        const turn = Math.floor((x / 350) * 2000)
        appContext.state.activeGame!.currentMatch!.jumpToTurn(turn)
    }

    if (!appContext.state.activeGame || !appContext.state.activeGame.currentMatch)
        return (
            <div className="min-w-[350px] min-h-[30px] bg-bg rounded-md mr-2 relative">
                <p className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-[9px] text-xs pointer-events-none">
                    Upload Game File
                </p>
                <div className="absolute bg-white/10 left-0 right-0 bottom-0 min-h-[5px] rounded"></div>
            </div>
        )

    const turn = appContext.state.activeGame!.currentMatch!.currentTurn.turnNumber
    const maxTurn = appContext.state.tournament ? 2000 : appContext.state.activeGame!.currentMatch!.maxTurn
    const turnPercentage = () => (1 - turn / maxTurn) * 100 + '%'
    return (
        <div className="min-w-[350px] min-h-[30px] bg-bg rounded-md mr-2 relative">
            <p className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-[9px] text-xs select-none">
                Turn: <b>{turn}</b>/{maxTurn}
            </p>
            <div className="absolute bg-white/10 left-0 right-0 bottom-0 min-h-[5px] rounded"></div>
            <div
                className="absolute bg-white/90 left-0 bottom-0 min-h-[5px] rounded min-w-[5px]"
                style={{ right: turnPercentage() }}
            ></div>
            <div
                className="absolute left-0 right-0 top-0 bottom-0 z-index-1 cursor-pointer"
                onMouseMove={timelineHover}
                onMouseDown={timelineDown}
                onMouseUp={timelineUp}
                onMouseLeave={timelineUp}
            ></div>
        </div>
    )
}

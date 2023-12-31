import React, { useRef } from 'react'
import { useAppContext } from '../../app-context'

interface Props {
    currentUPS: number
}

export const ControlsBarTimeline: React.FC<Props> = ({ currentUPS }) => {
    const appContext = useAppContext()

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

    const tilelineEnter = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
        if (e.buttons === 1) timelineDown(e)
    }

    // TODO: should have a defined constant somewhere else
    const maxTurn = appContext.state.tournament ? 2000 : appContext.state.activeGame!.currentMatch!.maxTurn

    const timelineClick = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
        const rect = e.currentTarget.getBoundingClientRect()
        const x = e.clientX - rect.left
        const turn = Math.floor((x / 350) * maxTurn)
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
    const turnPercentage = () => (1 - turn / maxTurn) * 100 + '%'
    return (
        <div className="min-w-[350px] min-h-[30px] bg-bg rounded-md mr-2 relative">
            <p className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-[10px] text-xs select-none whitespace-nowrap">
                Turn: <b>{turn}</b>/{maxTurn} &nbsp; {appContext.state.updatesPerSecond} UPS (
                {appContext.state.updatesPerSecond < 0 && '-'}
                {currentUPS})
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
                onMouseEnter={tilelineEnter}
            ></div>
        </div>
    )
}

import React from 'react'
import * as ControlIcons from '../../icons/controls'
import { ControlsBarButton } from './controls-bar-button'
import { useAppContext } from '../../app-context'

export const ControlsBar: React.FC = () => {
    const [updatesPerSecond, setUpdatesPerSecond] = React.useState(1)
    const appContext = useAppContext()

    const matchLoaded = () => appContext.state.activeGame && appContext.state.activeGame.currentMatch

    const currentRound = () => {
        if (!matchLoaded()) return 0
        return appContext.state.activeGame!.currentMatch!.currentTurn.turnNumber
    }

    const changeUpdatesPerSecond = (val: number) => {
        if (!matchLoaded()) return
        setUpdatesPerSecond(val)
    }

    const multiplyUpdatesPerSecond = (multiplier: number) => {
        if (!matchLoaded()) return
        setUpdatesPerSecond((u) => {
            const sign = Math.sign(u)
            const newMag = Math.max(1 / 4, Math.min(64, Math.abs(u) * multiplier))
            return sign * newMag
        })
    }

    const stepTurn = (delta: number) => {
        if (!matchLoaded()) return
        appContext.state.activeGame!.currentMatch!.stepTurn(delta)
    }

    const jumpToStart = () => {
        if (!matchLoaded()) return
        appContext.state.activeGame!.currentMatch!.jumpToTurn(0)
    }

    const jumpToEnd = () => {
        if (!matchLoaded()) return
        appContext.state.activeGame!.currentMatch!.jumpToEnd()
    }

    return (
        <div className="flex bg-darkHighlight text-white absolute bottom-0 p-1.5 rounded-t-md z-10 gap-1.5">
            <div className="min-w-[350px] min-h-[30px] bg-bg rounded-md mr-2 relative">
                <p className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 text-xs pointer-events-none">
                    Round: <b>{currentRound()}</b>/2000
                </p>
            </div>
            <ControlsBarButton
                icon={<ControlIcons.ReverseIcon />}
                tooltip="Reverse"
                onClick={() => multiplyUpdatesPerSecond(-1)}
            />
            <ControlsBarButton
                icon={<ControlIcons.SkipBackwardsIcon />}
                tooltip="Decrease Speed"
                onClick={() => multiplyUpdatesPerSecond(0.5)}
            />
            <ControlsBarButton
                icon={<ControlIcons.GoPreviousIcon />}
                tooltip="Step Backwards"
                onClick={() => stepTurn(-1)}
            />
            {updatesPerSecond == 0 ? (
                <ControlsBarButton
                    icon={<ControlIcons.GoNextIcon />}
                    tooltip="Play"
                    onClick={() => changeUpdatesPerSecond(1)}
                />
            ) : (
                <ControlsBarButton
                    icon={<ControlIcons.PlaybackPauseIcon />}
                    tooltip="Pause"
                    onClick={() => changeUpdatesPerSecond(0)}
                />
            )}
            <ControlsBarButton icon={<ControlIcons.GoNextIcon />} tooltip="Next Turn" onClick={() => stepTurn(1)} />
            <ControlsBarButton
                icon={<ControlIcons.SkipForwardsIcon />}
                tooltip="Increase Speed"
                onClick={() => multiplyUpdatesPerSecond(2)}
            />
            <ControlsBarButton icon={<ControlIcons.PlaybackStopIcon />} tooltip="Jump To Start" onClick={jumpToStart} />
            <ControlsBarButton icon={<ControlIcons.GoEndIcon />} tooltip="Jump To End" onClick={jumpToEnd} />
        </div>
    )
}

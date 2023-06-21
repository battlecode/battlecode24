import React from 'react'
import * as ControlIcons from '../../icons/controls'
import { ControlsBarButton } from './controls-bar-button'
import { useAppContext } from '../../app-context'
import { ControlsBarTimeline } from './controls-bar-timeline'

export const ControlsBar: React.FC = () => {
    const [updatesPerSecond, setUpdatesPerSecond] = React.useState(0)
    const appContext = useAppContext()

    const matchLoaded = () => appContext.state.activeGame && appContext.state.activeGame.currentMatch

    const changeUpdatesPerSecond = (val: number) => {
        if (!matchLoaded()) return
        setUpdatesPerSecond(val)
    }

    const multiplyUpdatesPerSecond = (multiplier: number) => {
        if (!matchLoaded()) return
        setUpdatesPerSecond((u) => {
            const sign = Math.sign(u * multiplier)
            const newMag = Math.max(1 / 4, Math.min(64, Math.abs(u * multiplier)))
            return sign * newMag
        })
    }

    const stepTurn = (delta: number) => {
        if (!matchLoaded()) return
        appContext.state.activeGame!.currentMatch!.stepTurn(delta)
    }

    const jumpToTurn = (turn: number) => {
        if (!matchLoaded()) return
        appContext.state.activeGame!.currentMatch!.jumpToTurn(turn)
    }

    const jumpToEnd = () => {
        if (!matchLoaded()) return
        appContext.state.activeGame!.currentMatch!.jumpToEnd()
    }

    React.useEffect(() => {
        if (!matchLoaded()) return
        if (updatesPerSecond == 0) return

        const stepInterval = setInterval(() => {
            appContext.state.activeGame!.currentMatch!.stepTurn(Math.sign(updatesPerSecond))
        }, 1000 / Math.abs(updatesPerSecond))

        return () => {
            clearInterval(stepInterval)
        }
    }, [updatesPerSecond, appContext.state.activeGame, appContext.state.activeGame?.currentMatch])

    return (
        <div className="flex bg-darkHighlight text-white absolute bottom-0 p-1.5 rounded-t-md z-10 gap-1.5">
            <ControlsBarTimeline/>
            <ControlsBarButton
                icon={<ControlIcons.ReverseIcon />}
                tooltip="Reverse"
                onClick={() => multiplyUpdatesPerSecond(-1)}
            />
            <ControlsBarButton
                icon={<ControlIcons.SkipBackwardsIcon />}
                tooltip={'Decrease Speed (' + updatesPerSecond + ' ups)'}
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
                tooltip={'Increase Speed (' + updatesPerSecond + ' ups)'}
                onClick={() => multiplyUpdatesPerSecond(2)}
            />
            <ControlsBarButton
                icon={<ControlIcons.PlaybackStopIcon />}
                tooltip="Jump To Start"
                onClick={() => jumpToTurn(0)}
            />
            <ControlsBarButton icon={<ControlIcons.GoEndIcon />} tooltip="Jump To End" onClick={jumpToEnd} />
        </div>
    )
}

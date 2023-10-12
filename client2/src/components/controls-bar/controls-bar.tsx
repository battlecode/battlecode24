import React from 'react'
import * as ControlIcons from '../../icons/controls'
import { ControlsBarButton } from './controls-bar-button'
import { useAppContext } from '../../app-context'
import { useKeyboard } from '../../util/keyboard'
import { ControlsBarTimeline } from './controls-bar-timeline'
import { MAX_SIMULATION_STEPS } from '../../playback/Match'

const SIMULATION_UPDATE_INTERVAL_MS = 17 // About 60 fps

export const ControlsBar: React.FC = () => {
    const [updatesPerSecond, setUpdatesPerSecond] = React.useState(0)
    const [lastKeyPressed, setLastKeyPressed] = React.useState('')
    const appContext = useAppContext()
    const keyboard = useKeyboard()

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
        appContext.state.activeGame!.currentMatch!.roundSimulation()
    }

    const jumpToTurn = (turn: number) => {
        if (!matchLoaded()) return
        appContext.state.activeGame!.currentMatch!.jumpToTurn(turn)
        appContext.state.activeGame!.currentMatch!.roundSimulation()
    }

    const jumpToEnd = () => {
        if (!matchLoaded()) return
        appContext.state.activeGame!.currentMatch!.jumpToEnd()
        appContext.state.activeGame!.currentMatch!.roundSimulation()
    }

    React.useEffect(() => {
        if (!matchLoaded()) return
        if (updatesPerSecond == 0) return

        const msPerUpdate = 1000 / updatesPerSecond
        const updatesPerInterval = SIMULATION_UPDATE_INTERVAL_MS / msPerUpdate
        const simStepsPerInterval = updatesPerInterval * MAX_SIMULATION_STEPS
        const stepInterval = setInterval(() => {
            appContext.state.activeGame!.currentMatch!.stepSimulation(simStepsPerInterval)
        }, SIMULATION_UPDATE_INTERVAL_MS)

        return () => {
            clearInterval(stepInterval)
        }
    }, [updatesPerSecond, appContext.state.activeGame, appContext.state.activeGame?.currentMatch])

    if (!appContext.state.activeGame || !appContext.state.activeGame.playable) return null


    if (keyboard.keyCode !== lastKeyPressed) {
        // If the competitor had manually pressed one of the buttons on the
        // control bar before using a shortcut, unselect it; Most browsers have
        // specific accessibility features that mess with these shortcuts.
        if (keyboard.targetElem instanceof HTMLButtonElement) keyboard.targetElem.blur()

        // Warning: Moving this line of code more below causes a infinite loop
        // due to changing updatesPerSecond recursively calling useEffect. Keep
        // it here.
        setLastKeyPressed(keyboard.keyCode)

        if (keyboard.keyCode === 'Space') changeUpdatesPerSecond(updatesPerSecond === 0 ? 1 : 0)

        if (updatesPerSecond === 0) {
            // Paused
            if (keyboard.keyCode === 'ArrowRight') stepTurn(1)
            if (keyboard.keyCode === 'ArrowLeft') stepTurn(-1)
        } else {
            // Unpaused
            if (keyboard.keyCode === 'ArrowRight') multiplyUpdatesPerSecond(2)
            if (keyboard.keyCode === 'ArrowLeft') multiplyUpdatesPerSecond(0.5)
        }

        if (keyboard.keyCode === 'Comma') jumpToTurn(0)
        if (keyboard.keyCode === 'Period') jumpToEnd()
    }

    return (
        <div className="flex bg-darkHighlight text-white absolute bottom-0 p-1.5 rounded-t-md z-10 gap-1.5">
            <ControlsBarTimeline />
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
                    icon={<ControlIcons.PlaybackPlayIcon />}
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

import React from 'react'
import { useAppContext } from '../../../app-context'

export const HelpPage: React.FC = () => {
    const context = useAppContext()

    const hotkeyElement = (key: string, description: string) => {
        return (
            <div className="font-light">
                <b>{key}</b> - {description}
            </div>
        )
    }

    return (
        <div>
            Shortcuts: <br />
            <br />
            {hotkeyElement(`Space`, 'Pauses / Unpauses game')}
            <br />
            {hotkeyElement(
                `LeftArrow and RightArrow`,
                'Controls speed if game is unpaused, or moves the current round if paused'
            )}
            <br />
            {hotkeyElement(`~ and 1`, 'Scroll through Game, Profiler, Runner, and Queue')}
            <br />
            {hotkeyElement(
                `Shift`,
                'Switches to Queue tab. If you are already on it, prompts you to select a replay file'
            )}
            <br />
            {hotkeyElement(
                `Shift`,
                'Switches to Queue tab. If you are already on it, prompts you to select a replay file'
            )}
            <br />
            {hotkeyElement(`C`, 'Hides and Unhides Game control bar')}
            <br />
            {hotkeyElement(`.`, 'Skip to the very last turn of the current game')}
            <br />
            {hotkeyElement(`,`, 'Skip to the first turn of the current game')}
        </div>
    )
}

import React from 'react'
import { useAppContext } from '../components/app-context'
import Game from '../playback/Game'

export const QueuePage: React.FC = () => {
    const appContext = useAppContext()
    const game = appContext.state.activeGame

    const upload = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!e.target.files || e.target.files.length == 0) return
        const file = e.target.files[0]
        const reader = new FileReader()
        reader.onload = () => {
            appContext.setState({
                ...appContext.state,
                activeGame: Game.loadFullGameRaw(reader.result as ArrayBuffer)
            })
        }
        reader.readAsArrayBuffer(file)
    }

    return (
        <div className="flex flex-col">
            <p>Games (1/1)</p>

            <input type="file" onChange={upload}></input>
        </div>
    )
}

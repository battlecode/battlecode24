import React from 'react'
import { AppContextProvider } from './app-context'
import { ControlsBar } from './controls-bar'
import { GameRenderer } from './game-renderer'
import { Sidebar } from './sidebar'

export const MainPage: React.FC = () => {
    return (
        <AppContextProvider>
            <div className="flex bg-gray-900">
                <Sidebar />
                <ControlsBar />
                <GameRenderer />
            </div>
        </AppContextProvider>
    )
}

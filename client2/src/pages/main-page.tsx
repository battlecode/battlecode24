import React from 'react'
import { AppContextProvider } from '../app-context'
import { ControlsBar } from '../components/controls-bar/controls-bar'
import { Sidebar } from '../components/sidebar/sidebar'
import { Game } from '../components/game/game'

export const MainPage: React.FC = () => {
    return (
        <AppContextProvider>
            <div className="flex bg-dark overflow-hidden">
                <Sidebar />
                <div className="w-full h-screen flex justify-center">
                    <Game />
                    <ControlsBar />
                </div>
            </div>
        </AppContextProvider>
    )
}

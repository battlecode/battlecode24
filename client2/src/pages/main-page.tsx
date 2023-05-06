import React from 'react'
import { AppContextProvider } from '../app-context'
import { ControlsBar } from '../components/controls-bar'
import { Sidebar } from '../components/sidebar'

export const MainPage: React.FC = () => {
    return (
        <AppContextProvider>
            <div className="flex bg-bg">
                <Sidebar />
                <ControlsBar />
                <p className="text-white my-auto mx-auto">Game Rendered Here</p>
            </div>
        </AppContextProvider>
    )
}

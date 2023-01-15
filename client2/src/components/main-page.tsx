import React from 'react'
import { AppContextProvider } from './app-context'
import { ControlsBar } from './controls-bar'
import { Sidebar } from './sidebar'

export const MainPage: React.FC = () => {
    return (
        <AppContextProvider>
            <div className="flex bg-gray-900">
                <Sidebar />
                <ControlsBar />
                <p className="text-white">asd</p>
            </div>
        </AppContextProvider>
    )
}

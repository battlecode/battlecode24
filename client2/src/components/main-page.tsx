import React from 'react'
import { AppContextProvider } from './app-context'
import { Sidebar } from './sidebar'

export const MainPage: React.FC = () => {
    return (
        <AppContextProvider>
            <div className="flex bg-gray-900">
                <Sidebar />
                <p className="text-white">asd</p>
            </div>
        </AppContextProvider>
    )
}

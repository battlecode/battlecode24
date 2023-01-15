import React from 'react'
import { AppContextProvider } from './app-context'
import { Sidebar } from './sidebar'

export const MainPage: React.FC = () => {
    return (
        <AppContextProvider>
            <div className="flex">
                <Sidebar />
                <p>asd</p>
            </div>
        </AppContextProvider>
    )
}

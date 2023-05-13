import React from 'react'
import { PageType } from './definitions'

export interface AppState {
    page: PageType
    queue: number[] // placeholder
}

const DEFAULT_APP_STATE: AppState = {
    page: PageType.QUEUE,
    queue: []
}

export interface AppContext {
    state: AppState
    setState: (value: React.SetStateAction<AppState>) => void
}

interface Props {
    children: React.ReactNode[] | React.ReactNode
}

const appContext = React.createContext({} as AppContext)
export const AppContextProvider: React.FC<Props> = (props) => {
    const [appState, setAppState] = React.useState(DEFAULT_APP_STATE)

    return (
        <appContext.Provider value={{ state: appState, setState: setAppState }}>
            {props.children}
        </appContext.Provider>
    )
}

export const useAppContext = () => React.useContext(appContext)

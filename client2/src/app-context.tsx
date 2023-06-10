import React from 'react'
import { PageType } from '../definitions'
import Game from '../playback/Game'

export interface AppState {
    page: PageType
    activeGame: Game | undefined
}

const DEFAULT_APP_STATE: AppState = {
    page: PageType.QUEUE,
    activeGame: undefined
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

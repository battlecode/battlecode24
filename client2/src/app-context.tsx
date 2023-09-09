import React from 'react'
import Game from './playback/Game'
import Match from './playback/Match'

export interface AppState {
    queue: Game[]
    activeGame: Game | undefined
    activeMatch: Match | undefined
}

const DEFAULT_APP_STATE: AppState = {
    queue: [],
    activeGame: undefined,
    activeMatch: undefined
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

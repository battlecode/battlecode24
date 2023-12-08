import { Listbox, Transition } from '@headlessui/react'
import React, { Fragment, useEffect } from 'react'
import { BATTLECODE_YEAR } from '../../constants'
import { ThreeBarsIcon } from '../../icons/three-bars'
import { GamePage } from './game/game'
import { QueuePage } from './queue/queue'
import { TbSelector } from 'react-icons/tb'
import { BsChevronLeft } from 'react-icons/bs'
import { HelpPage } from './help/help'
import { MapEditorPage } from './map-editor/map-editor'
import { ProfilerPage } from './profiler/profiler'
import { RunnerPage } from './runner/runner'
import { usePage, PageType, useSearchParamBool, useSearchParamString } from '../../app-search-params'
import { useKeyboard } from '../../util/keyboard'
import { Scrollbars } from 'react-custom-scrollbars-2'
import useWindowDimensions from '../../util/window-size'
import { TournamentPage } from './tournament/tournament'
import Tournament, { JsonTournamentGame } from '../../playback/Tournament'
import { useAppContext } from '../../app-context'

const SIDEBAR_BUTTONS: { name: string; page: PageType }[] = [
    { name: 'Game', page: PageType.GAME },
    { name: 'Queue', page: PageType.QUEUE },
    { name: 'Runner', page: PageType.RUNNER },
    { name: 'Profiler', page: PageType.PROFILER },
    { name: 'Map Editor', page: PageType.MAP_EDITOR },
    { name: 'Help', page: PageType.HELP }
]

export const Sidebar: React.FC = () => {
    const { width, height } = useWindowDimensions()
    const [page, setPage] = usePage()
    const keyboard = useKeyboard()
    const context = useAppContext()

    const [open, setOpen] = useSearchParamBool('sidebarOpen', true)

    const minWidth = open ? 'min-w-[390px]' : 'min-w-[64px]'
    const maxWidth = open ? 'max-w-[390px]' : 'max-w-[64px]'

    // Tournament mode loading ====================================================================================================
    const [tournamentMode, setTournamentMode] = useSearchParamBool('tournament', false)
    const [loadingRemoteTournament, setLoadingRemoteTournament] = React.useState(false)
    const [tournamentSource, setTournamentSource] = useSearchParamString(
        'tournamentSource',
        'https://api.battlecode.org/api/compete/bc23/match/tournament/?external_id_private=private_bc23highschool3_54378c83_4bfb_47b2_ae4a_0e711b0e167c&format=json'
    )
    const fetchTournamentPage = (tournamentSource: string, rawGames: JsonTournamentGame[]) => {
        fetch(tournamentSource)
            .then((response) => response.text())
            .then((text) => {
                const data = JSON.parse(text)
                const newGames = data.results as JsonTournamentGame[]
                rawGames.push(...newGames)

                if (data.next) {
                    fetchTournamentPage(data.next, rawGames)
                } else {
                    setLoadingRemoteTournament(false)
                    console.log(rawGames)
                    context.setState({
                        ...context.state,
                        tournament: new Tournament(rawGames)
                    })
                }
            })
    }
    React.useEffect(() => {
        if (tournamentSource) {
            setLoadingRemoteTournament(true)
            fetchTournamentPage(tournamentSource, [])
            setPage(PageType.TOURNAMENT)
        }
    }, [tournamentSource])
    // End tournament mode loading ====================================================================================================

    const renderPage = () => {
        if (!open) return undefined

        switch (page) {
            default:
                return undefined
            case PageType.GAME:
                return <GamePage />
            case PageType.QUEUE:
                return <QueuePage />
            case PageType.RUNNER:
                return <RunnerPage />
            case PageType.PROFILER:
                return <ProfilerPage />
            case PageType.MAP_EDITOR:
                return <MapEditorPage />
            case PageType.HELP:
                return <HelpPage />
            case PageType.TOURNAMENT:
                return <TournamentPage loadingRemoteTournament={loadingRemoteTournament} />
        }
    }

    // Skip going through map and help tab, it's annoying for competitors.
    const hotkeyPageLoop = tournamentMode
        ? [PageType.GAME, PageType.QUEUE, PageType.TOURNAMENT]
        : [PageType.GAME, PageType.QUEUE, PageType.RUNNER, PageType.PROFILER]
    const getNextPage = (currentPage: PageType, previous: boolean) => {
        const index = hotkeyPageLoop.indexOf(currentPage)
        if (index === -1) return currentPage
        const nextIndex = (index + (previous ? -1 : 1) + hotkeyPageLoop.length) % hotkeyPageLoop.length
        return hotkeyPageLoop[nextIndex]
    }

    React.useEffect(() => {
        if (keyboard.keyCode === 'Backquote') setPage(getNextPage(page, false))

        if (keyboard.keyCode === 'ShiftLeft') setPage(PageType.QUEUE)

        if (keyboard.keyCode === 'Digit1') setPage(getNextPage(page, true))
    }, [keyboard.keyCode])

    const activeSidebarButtons = React.useMemo(() => {
        if (tournamentMode) {
            return [
                { name: 'Game', page: PageType.GAME },
                { name: 'Queue', page: PageType.QUEUE },
                { name: 'Tournament', page: PageType.TOURNAMENT }
            ]
        }
        return SIDEBAR_BUTTONS
    }, [tournamentMode])

    return (
        <div
            className={`${minWidth} ${maxWidth} bg-light text-black h-screen transition-[min-width,max-width] overflow-hidden`}
        >
            <Scrollbars
                universal={true}
                autoHide
                autoHideTimeout={1000}
                autoHideDuration={200}
                autoHeight
                autoHeightMax={height}
                autoHeightMin={height}
            >
                <div className="flex flex-col gap-2 p-3">
                    <div className="flex justify-between">
                        {open && (
                            <p className="p-2 whitespace-nowrap font-extrabold text-xl">{`BATTLECODE ${BATTLECODE_YEAR}`}</p>
                        )}
                        <div className="flex gap-3">
                            <button
                                onClick={() => setOpen(!open)}
                                className="p-2 hover:bg-lightHighlight rounded-md"
                                style={{
                                    width: '40px',
                                    height: '40px'
                                }}
                            >
                                {open ? <BsChevronLeft className="mx-auto font-bold stroke-2" /> : <ThreeBarsIcon />}
                            </button>
                        </div>
                    </div>
                    {open && (
                        <>
                            <div className="flex flex-row flex-wrap justify-between mb-2">
                                {activeSidebarButtons.map((sidebarButton) => (
                                    <div
                                        className={
                                            'w-[32%] text-center text-sm py-2 my-1 cursor-pointer hover:bg-lightHighlight border-b-2 ' +
                                            (page == sidebarButton.page ? 'border-gray-800' : 'border-gray-200')
                                        }
                                        onClick={() => setPage(sidebarButton.page)}
                                        key={sidebarButton.page}
                                    >
                                        {sidebarButton.name}
                                    </div>
                                ))}
                            </div>
                            {renderPage()}
                        </>
                    )}
                </div>
            </Scrollbars>
        </div>
    )
}

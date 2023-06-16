import { Listbox, Transition } from '@headlessui/react'
import React, { Fragment } from 'react'
import { BATTLECODE_YEAR } from '../../constants'
import { ThreeBarsIcon } from '../../icons/three-bars'
import { GamePage } from './game/game'
import { QueuePage } from './queue/queue'
import { useAppContext } from '../../app-context'
import { TbSelector } from 'react-icons/tb'
import { BsChevronLeft } from 'react-icons/bs'
import { HelpPage } from './help/help'
import { MapEditorPage } from './map-editor/map-editor'
import { ProfilerPage } from './profiler/profiler'
import { RunnerPage } from './runner/runner'
import { usePage, PageType, useSearchParamBool } from '../../app-search-params'

const SIDEBAR_BUTTONS: { name: string; page: PageType }[] = [
    { name: 'Game', page: PageType.GAME },
    { name: 'Queue', page: PageType.QUEUE },
    { name: 'Runner', page: PageType.RUNNER },
    { name: 'Profiler', page: PageType.PROFILER },
    { name: 'Map Editor', page: PageType.MAP_EDITOR },
    { name: 'Help', page: PageType.HELP }
]

export const Sidebar: React.FC = () => {
    const [page, setPage] = usePage();

    const [open, setOpen] = useSearchParamBool('sidebarOpen', true)
    const [expanded, setExpanded] = React.useState(false)

    const minWidth = open ? 'min-w-[390px]' : 'min-w-[56px]'
    const maxWidth = open ? 'max-w-[390px]' : 'max-w-[56px]'

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
        }
    }

    const updatePage = (newPage: PageType) => {
        setPage(newPage);
    }

    // Minimize the sidebar buttons when a new one has been selected
    React.useEffect(() => {
        setExpanded(false)
    }, [page])

    return (
        <div
            className={`${minWidth} ${maxWidth} h-screen bg-light flex flex-col gap-2 p-2 transition-[min-width,max-width] overflow-x-hidden shadow-centered text-black`}
        >
            <div className="flex justify-between">
                {open && <p className="p-2 whitespace-nowrap font-extrabold text-xl">{`BATTLECODE ${BATTLECODE_YEAR}`}</p>}
                <div className="flex gap-3">
                    <button onClick={() => setOpen(!open)} className="p-2 hover:bg-lightHighlight rounded-md" style={{
                        width: '40px',
                        height: '40px'
                    }}>
                        {open ? <BsChevronLeft className="mx-auto font-bold stroke-2"/> : <ThreeBarsIcon />}
                    </button>
                </div>
            </div>
            {open && <>
                <Listbox value={page} onChange={updatePage}>
                    <Listbox.Button
                        className="text-left flex flex-row justify-between hover:bg-lightHighlight p-3 rounded-md border-black border"
                    >
                        {page}
                        <TbSelector className="text-2xl align-middle"/>
                    </Listbox.Button>
                    <Transition
                        as={Fragment}
                        enter="transition-all ease-out overflow-hidden duration-100"
                        enterFrom="transform scale-95 opacity-0 max-h-0"
                        enterTo="transform scale-100 opacity-100 max-h-96"
                        leave="transition-all ease-in overflow-hidden duration-50"
                        leaveFrom="transform scale-100 opacity-100 max-h-96"
                        leaveTo="transform scale-95 opacity-0 max-h-0"
                    >
                        <Listbox.Options>
                            {SIDEBAR_BUTTONS.map((data) => {
                                return <Listbox.Option
                                    key={data.page}
                                    value={data.page}
                                    className="text-left hover:bg-lightHighlight p-3 py-1 rounded-md cursor-pointer"
                                >
                                    {data.name}
                                </Listbox.Option>
                            })}
                        </Listbox.Options>
                    </Transition>
                </Listbox>

                <hr className="border-gray-800 my-2" />
                {renderPage()}
            </>}
        </div>
    )
}

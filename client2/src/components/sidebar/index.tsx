import { Menu } from '@headlessui/react'
import React from 'react'
import { BATTLECODE_YEAR } from '../../constants'
import { PageType } from '../../definitions'
import { ChevronDownIcon, ChevronUpIcon } from '../../icons/chevron'
import { ThreeBarsIcon } from '../../icons/three-bars'
import { GamePage } from './game'
import { QueuePage } from './queue'
import { useAppContext } from '../../app-context'
import { SidebarButton } from './sidebar-button'

const SIDEBAR_BUTTONS: { name: string; page: PageType }[] = [
    { name: 'Game', page: PageType.GAME },
    { name: 'Queue', page: PageType.QUEUE },
    { name: 'Runner', page: PageType.RUNNER },
    { name: 'Profiler', page: PageType.PROFILER },
    { name: 'Map Editor', page: PageType.MAPEDITOR },
    { name: 'Help', page: PageType.HELP }
]

export const Sidebar: React.FC = () => {
    const context = useAppContext()

    const [open, setOpen] = React.useState(true)
    const [expanded, setExpanded] = React.useState(false)

    const minWidth = open ? 'min-w-[390px]' : 'min-w-[48px]'
    const maxWidth = open ? 'max-w-[390px]' : 'max-w-[48px]'

    const renderPage = () => {
        if (!open) return undefined

        switch (context.state.page) {
            default:
                return undefined
            case PageType.GAME:
                return <GamePage />
            case PageType.QUEUE:
                return <QueuePage />
        }
    }

    // Minimize the sidebar buttons when a new one has been selected
    React.useEffect(() => {
        setExpanded(false)
    }, [context.state.page])

    return (
        <div
            className={`${minWidth} ${maxWidth} h-screen bg-fg flex flex-col gap-2 p-3 transition-[min-width,max-width] overflow-x-hidden shadow-centered text-white`}
        >
            <div className="flex justify-between">
                <div className="flex gap-3">
                    <button onClick={() => setOpen(!open)}>
                        <ThreeBarsIcon />
                    </button>
                    <button onClick={() => setExpanded(!expanded)}>
                        {expanded ? <ChevronUpIcon /> : <ChevronDownIcon />}
                    </button>
                </div>
                <p className="whitespace-nowrap text-white">{`BATTLECODE ${BATTLECODE_YEAR}`}</p>
            </div>
            {SIDEBAR_BUTTONS.map((b, i) => {
                if ((!expanded && b.page == context.state.page) || expanded)
                    return <SidebarButton key={i} name={b.name} page={b.page} />
                return undefined
            })}
            <hr className="border-gray-800 my-2" />
            {renderPage()}
        </div>
    )
}

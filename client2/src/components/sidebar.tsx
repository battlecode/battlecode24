import React from 'react'
import { PageType } from '../definitions'
import { ChevronDownIcon, ChevronUpIcon } from '../icons/chevron'
import { ThreeBarsIcon } from '../icons/three-bars'
import { useAppContext } from './app-context'
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

    const [open, setOpen] = React.useState(false)
    const [expanded, setExpanded] = React.useState(false)

    const minWidth = open ? 'min-w-[390px]' : 'min-w-[48px]'
    const maxWidth = open ? 'max-w-[390px]' : 'max-w-[48px]'

    // Minimize the sidebar buttons when a new one has been selected
    React.useEffect(() => {
        setExpanded(false)
    }, [context.state.page])

    return (
        <div
            className={`${minWidth} ${maxWidth} h-screen bg-pink-700 flex flex-col gap-2 p-3 transition-[min-width,max-width] overflow-x-hidden`}
        >
            <div className="flex gap-3">
                <button onClick={() => setOpen(!open)}>
                    <ThreeBarsIcon />
                </button>
                <button onClick={() => setExpanded(!expanded)}>
                    {expanded ? <ChevronUpIcon /> : <ChevronDownIcon />}
                </button>
            </div>
            {SIDEBAR_BUTTONS.map((b, i) => {
                if ((!expanded && b.page == context.state.page) || expanded)
                    return <SidebarButton key={i} name={b.name} page={b.page} />
                return undefined
            })}
            <hr />
        </div>
    )
}

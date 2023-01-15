import React from 'react'
import { PageType } from '../definitions'
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
    return (
        <div className="min-w-[390] max-w-[390] h-screen bg-pink-700 flex flex-col">
            {SIDEBAR_BUTTONS.map((b, i) => (
                <SidebarButton key={i} name={b.name} page={b.page} />
            ))}
        </div>
    )
}

import React from 'react'
import { PageType } from '../definitions'

interface Props {
    name: string
    page: PageType
}

export const SidebarButton: React.FC<Props> = (props) => {
    return <button>{props.name}</button>
}

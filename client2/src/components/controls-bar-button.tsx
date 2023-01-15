import React from 'react'

interface Props {
    icon: React.ReactElement
    tooltip: string
    onClick?: () => void
}

export const ControlsBarButton: React.FC<Props> = (props) => {
    return (
        <button className="bg-gray-800 p-[0.4rem] max-w-[30px] min-w-[30px] max-h-[30px] min-h-[30px] rounded-md">
            {props.icon}
        </button>
    )
}

import React from 'react'
import Tooltip from '../tooltip'

interface Props {
    icon: React.ReactElement
    tooltip: string
    onClick?: () => void
    disabled?: boolean
}

export const ControlsBarButton: React.FC<Props> = (props) => {
    return (
        <Tooltip text={props.tooltip} location="top">
            <button
                className={
                    (props.disabled ? 'opacity-30 cursor-default' : '') +
                    ' bg-darkHighlight hover:bg-dark p-[0.4rem] max-w-[30px] min-w-[30px] max-h-[30px] min-h-[30px] rounded-md'
                }
                onClick={props.disabled ? () => {} : props.onClick}
            >
                {props.icon}
            </button>
        </Tooltip>
    )
}

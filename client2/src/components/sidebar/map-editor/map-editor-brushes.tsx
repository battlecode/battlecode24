import React from 'react'
import { MapEditorBrush } from './MapEditorBrush'
import { BsChevronRight, BsChevronDown } from 'react-icons/bs'
import { MapEditorBrushRowField } from './map-editor-field'

interface Props {
    brush: MapEditorBrush
    open: boolean
    onClick: () => void
}

export const MapEditorBrushRow: React.FC<Props> = (props: Props) => {
    const name = props.brush.name
    const fields = props.brush.fields
    return (
        <div className="flex flex-col pb-1">
            <div
                className="flex flex-row items-center hover:bg-lightHighlight cursor-pointer rounded-md"
                onClick={props.onClick}
            >
                {props.open ? (
                    <BsChevronDown className="ml-1 mr-2 font-bold stroke-2 text-xs" />
                ) : (
                    <BsChevronRight className="ml-1 mr-2 font-bold stroke-2 text-xs" />
                )}
                {name}
                {props.open && <div className="my-auto border border-gray ml-2 flex-grow"></div>}
            </div>
            {props.open && (
                <div className="flex flex-col border-b-2 border-gray mb-2 pb-3 pl-3">
                    {Object.values(fields).map((field, i) => (
                        <div key={i} className="flex flex-row mt-1">
                            <MapEditorBrushRowField field={field} />
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}

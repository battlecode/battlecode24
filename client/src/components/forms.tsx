import React, { PropsWithChildren } from 'react'

interface SelectProps {
    value?: string | number
    onChange: (value: string) => void
    className?: string
}

export const Select: React.FC<PropsWithChildren<SelectProps>> = (props) => {
    return (
        <select
            className={props.className + ' border border-black py-1 px-1 rounded-md'}
            value={props.value}
            onChange={(e) => props.onChange(e.target.value)}
        >
            {props.children}
        </select>
    )
}

interface NumInputProps {
    min: number
    max: number
    className?: string
    value: number
    changeValue: (newValue: number) => void
}
export const NumInput: React.FC<NumInputProps> = (props) => {
    return (
        <input
            min={props.min}
            max={props.max}
            className={'border border-black py-0.5 px-1 rounded-md w-12 ' + (props.className ?? '')}
            type="number"
            value={props.value}
            onChange={(e) => props.changeValue(parseInt(e.target.value))}
        />
    )
}

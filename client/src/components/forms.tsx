import React, { PropsWithChildren } from 'react'

interface SelectProps {
    value?: string | number
    onChange: (value: string) => void
    className?: string
}

export const Select: React.FC<PropsWithChildren<SelectProps>> = (props) => {
    return (
        <div className="relative">
            <select
                className={
                    props.className +
                    ' appearance-none border border-black py-1 px-1 rounded-md w-full h-full overflow-hidden'
                }
                value={props.value}
                onChange={(e) => props.onChange(e.target.value)}
            >
                {props.children}
            </select>
            <div className="absolute inset-y-0 right-0 flex items-center mx-1 my-2 bg-white bg-opacity-75 pointer-events-none">
                <svg className="w-5 h-5 fill-current text-black" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                    <path
                        fillRule="evenodd"
                        clipRule="evenodd"
                        d="M6.293 7.293a1 1 0 011.414 0L10 9.586l2.293-2.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z"
                    />
                </svg>
            </div>
        </div>
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
    const [tempValue, setTempValue] = React.useState<string | undefined>()

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        // Direct change from arrows
        const value = parseInt(e.target.value)
        if (!isNaN(value) && value >= props.min && value <= props.max) {
            props.changeValue(value)
        }
    }

    const handleInputBlur = () => {
        // Reset temp value after user loses focus
        setTempValue(undefined)
    }

    return (
        <input
            className={'border border-black py-0.5 px-1 rounded-md w-12 ' + (props.className ?? '')}
            type="number"
            value={tempValue ?? props.value}
            onBlur={handleInputBlur}
            onInput={handleInputChange}
            onChange={(e) => setTempValue(e.target.value)}
        />
    )
}

import React from 'react'

interface OptionProp {
    value: any
    selectedClass?: string
    className?: string
}

interface Props {
    options: Record<string, OptionProp>
    onChange: (value: any) => void
}

export const Toggle: React.FC<Props> = (props: Props) => {
    const [value, setValue] = React.useState(Object.values(props.options)[0].value)

    const onClick = (val: any) => {
        props.onChange(val)
        setValue(val)
    }

    return (
        <div className="flex flex-row gap-0.5 border border-black p-0.5 rounded-md">
            {Object.entries(props.options).map(([label, option_props], i) => (
                <button
                    key={i}
                    onClick={() => onClick(option_props.value)}
                    className={
                        option_props.className +
                        ' transition rounded py-1 px-2 ' +
                        (value !== option_props.value ? 'hover:bg-lightHighlight' : (option_props.selectedClass ?? 'text-white bg-darkHighlight'))
                    }
                >
                    {label}
                </button>
            ))}
        </div>
    )
}

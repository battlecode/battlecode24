import React, { MouseEvent, PropsWithChildren } from 'react'

type Props = {
    className?: string,
    onClick: (event: MouseEvent<HTMLButtonElement>) => void,
}

const Button = (props: PropsWithChildren<Props>) => (
    <button className={"text-xs mx-auto px-4 py-3 mt-1 mb-3 flex flex-row bg-light hover:bg-lightHover border-black border rounded-md " + (props.className ?? "")}
            onClick={props.onClick}
    >
        { props.children }
    </button>
)

export default Button

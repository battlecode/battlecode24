import React, { PropsWithChildren } from 'react'
import { useAppContext } from '../app-context'

interface Props {
    open: boolean
    onClose: (confirm: boolean) => void
    title: string
    description?: string
    className?: string
}

export const ConfirmDialog: React.FC<PropsWithChildren<Props>> = (props) => {
    const context = useAppContext()

    React.useEffect(() => {
        context.setState((prevState) => ({ ...prevState, disableHotkeys: props.open }))
    }, [props.open])

    if (!props.open) return <></>

    return (
        <div className="fixed flex flex-col items-center justify-center w-full h-full top-0 left-0 bg-gray-500 bg-opacity-50 z-50">
            <div className="flex flex-col flex-between items-center bg-white border border-black shadow-lg w-5/6 md:w-3/4 lg:w-7/12 rounded-xl py-4 px-6">
                <div className="flex flex-row items-center justify-between w-full">
                    <div className="font-display text-2xl font-medium">{props.title}</div>
                    <span
                        onClick={() => props.onClose(false)}
                        className="cursor-pointer icon-[mingcute--close-line] h-7 w-7"
                    ></span>
                </div>
                {props.description && <div className="text-md w-full mt-3">{props.description}</div>}
                {props.children}
                <div className="flex flex-row justify-between items-center w-full mt-8">
                    <button
                        onClick={() => props.onClose(false)}
                        className="hover:brightness-90 transition cursor-pointer text-md font-display rounded-[6px] px-5 py-1.5"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={() => props.onClose(true)}
                        className={`bg-gradient-to-tr from-purple-gradient-light-start to-purple-gradient-light-end p-[1px] rounded-[7px]`}
                    >
                        <div className="hover:brightness-90 transition cursor-pointer text-md font-display rounded-[6px] px-5 py-1.5">
                            Confirm
                        </div>
                    </button>
                </div>
            </div>
        </div>
    )
}

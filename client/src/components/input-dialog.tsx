import React, { PropsWithChildren } from 'react'

interface InputDialogProps {
    open: boolean
    onClose: (val: string) => void
    title: string
    description?: string
    className?: string
}

export const InputDialog: React.FC<PropsWithChildren<InputDialogProps>> = (props) => {
    const [value, setValue] = React.useState('')

    React.useEffect(() => {
        if (props.open) setValue('')
    }, [props.open])

    if (!props.open) return <></>

    return (
        <div className="fixed flex flex-col items-center justify-center w-full h-full top-0 left-0 bg-gray-500 bg-opacity-50 z-50">
            <div className="flex flex-col flex-between items-center bg-white border border-black shadow-lg w-5/6 md:w-3/4 lg:w-7/12 rounded-xl py-4 px-6">
                <div className="flex flex-row items-center justify-between w-full">
                    <div className="font-display text-2xl font-medium text-smoke-25">{props.title}</div>
                    <span
                        onClick={() => props.onClose('')}
                        className="cursor-pointer icon-[mingcute--close-line] bg-smoke-400 hover:bg-smoke-500 h-7 w-7"
                    ></span>
                </div>
                <div
                    className="w-full my-4 overflow-y-auto"
                    style={{
                        height: '50px'
                    }}
                >
                    <input
                        className="rounded-md px-4 py-2 bg-inherit border-[1px] border-black w-full"
                        type={'text'}
                        value={value}
                        onChange={(e) => setValue(e.target.value)}
                        placeholder="Path..."
                    />
                </div>
                <div className="flex flex-row justify-between items-center w-full">
                    <button
                        onClick={() => props.onClose('')}
                        className="hover:brightness-90 transition cursor-pointer text-md font-display bg-smoke-800 text-smoke-200 rounded-[6px] px-5 py-1.5"
                    >
                        Cancel
                    </button>
                    <button
                        disabled={value == ''}
                        onClick={() => props.onClose(value)}
                        className={`bg-gradient-to-tr from-purple-gradient-light-start to-purple-gradient-light-end p-[1px] rounded-[7px]`}
                    >
                        <div className="hover:brightness-90 transition cursor-pointer text-md font-display bg-smoke-800 text-smoke-saturated-25 rounded-[6px] px-5 py-1.5">
                            Confirm
                        </div>
                    </button>
                </div>
            </div>
        </div>
    )
}

import { Transition } from '@headlessui/react'
import React, { Fragment } from 'react'
import { BsChevronRight, BsChevronDown } from 'react-icons/bs'

interface Props {
    title: string
    open: boolean
    onClick: () => void
    children: React.ReactNode[] | React.ReactNode
    containerClassName?: string
    titleClassName?: string
}

export const SectionHeader: React.FC<Props> = (props: Props) => {
    return (
        <div className={'flex flex-col pb-1 ' + (props.containerClassName ?? '')}>
            <div
                className={
                    'flex flex-row items-center hover:bg-lightHighlight cursor-pointer rounded-md py-1 font-bold ' +
                    (props.titleClassName ?? '')
                }
                onClick={props.onClick}
            >
                {props.open ? (
                    <BsChevronDown className="ml-1 mr-2 font-bold stroke-2 text-xs" />
                ) : (
                    <BsChevronRight className="ml-1 mr-2 font-bold stroke-2 text-xs" />
                )}
                {props.title}
                {props.open && <div className="my-auto border border-gray ml-2 flex-grow"></div>}
            </div>
            <Transition
                as="div"
                enter="transition-all ease-out overflow-hidden duration-100"
                enterFrom="opacity-0 max-h-0"
                enterTo=" opacity-100 max-h-[3000px]"
                leave="transition-all ease-in overflow-hidden duration-30"
                leaveFrom="opacity-50 max-h-[3000px]"
                leaveTo="opacity-0 max-h-0"
                show={props.open}
            >
                {props.children}
            </Transition>
        </div>
    )
}
